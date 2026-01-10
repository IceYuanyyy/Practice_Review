package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.PracticeRecord;
import com.exam.entity.PracticeRound;
import com.exam.entity.Question;
import com.exam.service.PracticeRecordService;
import com.exam.service.PracticeRoundService;
import com.exam.service.QuestionService;
import com.exam.service.UserQuestionStatsService;
import com.exam.service.WrongBookService;
import com.exam.dto.DashboardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 练习 Controller
 * 已修改为多用户模式：所有数据按当前登录用户隔离
 * 
 * @author Exam System
 * @since 2025-12-19
 * @modified 2025-12-27 添加用户隔离
 * @modified 2026-01-03 添加轮次刷题和错题本功能
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PracticeRecordService practiceRecordService;

    @Autowired
    private UserQuestionStatsService userQuestionStatsService;

    @Autowired
    private PracticeRoundService practiceRoundService;

    @Autowired
    private WrongBookService wrongBookService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 提交答题记录
     * 
     * @param record 练习记录
     * @return 答题结果（是否正确、正确答案、解析）
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswer(@RequestBody PracticeRecord record) {
        Long userId = getCurrentUserId();
        
        // 获取题目
        Question question = questionService.getById(record.getQuestionId());
        if (question == null) {
            return Result.error("题目不存在");
        }

        // 判断答案是否正确
        boolean isCorrect = false;
        String userAnswer = record.getUserAnswer();
        String correctAnswer = question.getAnswer();
        
        // 多选题：需要完全匹配（顺序无关）
        if ("multiple-choice".equals(question.getType())) {
            // 将答案拆分成字符数组，排序后比较
            char[] userChars = userAnswer.toCharArray();
            char[] correctChars = correctAnswer.toCharArray();
            java.util.Arrays.sort(userChars);
            java.util.Arrays.sort(correctChars);
            isCorrect = java.util.Arrays.equals(userChars, correctChars);
        } else {
            // 单选题和判断题：直接比较
            isCorrect = correctAnswer.equalsIgnoreCase(userAnswer);
        }
        
        record.setIsCorrect(isCorrect);
        // 设置用户ID（数据隔离关键）
        record.setUserId(userId);

        // --- 新增：关联当前轮次 ---
        try {
            // 获取当前题目的科目
            String subject = question.getSubject();
            if (subject != null) {
                PracticeRound currentRound = practiceRoundService.getProgress(userId, subject);
                if (currentRound != null) {
                    record.setRoundNumber(currentRound.getRoundNumber());
                } else {
                    record.setRoundNumber(1); // 默认第一轮
                }
            }
        } catch (Exception e) {
            // 容错处理，避免影响提交
            record.setRoundNumber(1);
        }
        // ------------------------

        // 保存练习记录
        practiceRecordService.save(record);

        // 更新用户题目统计（替代原来的全局统计）
        userQuestionStatsService.updatePracticeStats(userId, record.getQuestionId(), isCorrect);
        
        // 答错时自动添加到错题本
        if (!isCorrect) {
            wrongBookService.addWrongQuestion(userId, record.getQuestionId());
        }

        // 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isCorrect", isCorrect);
        resultMap.put("correctAnswer", question.getAnswer());
        resultMap.put("analysis", question.getAnalysis());
        resultMap.put("userAnswer", record.getUserAnswer());

        return Result.success(resultMap);
    }

    /**
     * 获取错题列表（分页）- 按当前用户过滤
     * 
     * @param page 页码
     * @param size 每页大小
     * @param subject 科目筛选
     * @param type 题型筛选
     * @return 错题分页数据
     */
    @GetMapping("/wrong")
    public Result<PageResult<Question>> getWrongQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type) {
        
        Long userId = getCurrentUserId();
        
        // 从用户题目统计表获取错题ID列表
        List<Long> wrongQuestionIds = userQuestionStatsService.getWrongQuestionIds(userId);
        
        if (wrongQuestionIds.isEmpty()) {
            return Result.success(new PageResult<>(null, 0L, (long) page, (long) size));
        }
        
        // 分页查询错题
        Page<Question> questionPage = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.in("id", wrongQuestionIds);
        
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq("subject", subject);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        
        // 按用户统计表的错误次数排序（这里简化处理，实际可以 join 查询）
        wrapper.orderByDesc("id");
        questionService.page(questionPage, wrapper);
        
        // 为每道题设置当前用户的错误次数（用于前端显示）
        for (Question q : questionPage.getRecords()) {
            int wrongCount = userQuestionStatsService.getWrongCount(userId, q.getId());
            q.setWrongCount(wrongCount);
            // 设置收藏状态
            boolean isMarked = userQuestionStatsService.isMarked(userId, q.getId());
            q.setIsMarked(isMarked);
        }
        
        PageResult<Question> pageResult = new PageResult<>(
                questionPage.getRecords(),
                questionPage.getTotal(),
                questionPage.getCurrent(),
                questionPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 获取统计数据 - 按当前用户统计
     * 包含基础统计、科目/题型/难度分布、最近练习趋势
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<DashboardDTO> getStatistics() {
        Long userId = getCurrentUserId();
        DashboardDTO dto = new DashboardDTO();
        
        // 总题目数（用户可见的题目：自己的 + 公共题库）
        QueryWrapper<Question> questionWrapper = new QueryWrapper<>();
        questionWrapper.isNull("owner_id").or().eq("owner_id", userId);
        long totalQuestions = questionService.count(questionWrapper);
        dto.setTotalQuestions(totalQuestions);
        
        // 已练习题目数（当前用户练习过的）
        QueryWrapper<PracticeRecord> practicedWrapper = new QueryWrapper<>();
        practicedWrapper.eq("user_id", userId)
                       .select("DISTINCT question_id");
        List<Object> practicedQuestionIds = practiceRecordService.listObjs(practicedWrapper);
        dto.setPracticedQuestionCount((long) practicedQuestionIds.size());
        
        // 总练习次数（当前用户）
        QueryWrapper<PracticeRecord> countWrapper = new QueryWrapper<>();
        countWrapper.eq("user_id", userId);
        long totalPracticeCount = practiceRecordService.count(countWrapper);
        dto.setTotalPracticeCount(totalPracticeCount);
        
        // 正确次数（当前用户）
        QueryWrapper<PracticeRecord> correctWrapper = new QueryWrapper<>();
        correctWrapper.eq("user_id", userId).eq("is_correct", true);
        long correctCount = practiceRecordService.count(correctWrapper);
        
        // 正确率
        double correctRate = totalPracticeCount > 0 ? 
                (double) correctCount / totalPracticeCount * 100 : 0;
        dto.setCorrectRate(String.format("%.0f", correctRate));
        
        // 错题数（当前用户）
        List<Long> wrongIds = userQuestionStatsService.getWrongQuestionIds(userId);
        dto.setWrongQuestionCount((long) wrongIds.size());
        
        // 收藏题目数（当前用户）
        List<Long> markedIds = userQuestionStatsService.getMarkedQuestionIds(userId);
        dto.setMarkedQuestionCount((long) markedIds.size());
        
        // === 分布统计（基于用户可见题目） ===
        List<Question> visibleQuestions = questionService.list(
            new QueryWrapper<Question>().isNull("owner_id").or().eq("owner_id", userId)
        );
        
        // 科目分布
        Map<String, Long> subjectDist = visibleQuestions.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                q -> q.getSubject() != null ? q.getSubject() : "未分类",
                java.util.stream.Collectors.counting()
            ));
        dto.setSubjectDistribution(subjectDist);
        
        // 题型分布（转换为中文显示）
        Map<String, Long> typeDist = visibleQuestions.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                q -> {
                    String type = q.getType();
                    if ("single-choice".equals(type)) return "单选题";
                    if ("multiple-choice".equals(type)) return "多选题";
                    if ("judge".equals(type)) return "判断题";
                    return type != null ? type : "其他";
                },
                java.util.stream.Collectors.counting()
            ));
        dto.setTypeDistribution(typeDist);
        
        // 难度分布（转换为中文显示）
        Map<String, Long> diffDist = visibleQuestions.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                q -> {
                    String diff = q.getDifficulty();
                    if ("easy".equals(diff)) return "简单";
                    if ("medium".equals(diff)) return "中等";
                    if ("hard".equals(diff)) return "困难";
                    return diff != null ? diff : "未设置";
                },
                java.util.stream.Collectors.counting()
            ));
        dto.setDifficultyDistribution(diffDist);
        
        // 最近7天练习趋势
        List<Map<String, Object>> recentPractice = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
            
            // 查询该日期的练习次数
            QueryWrapper<PracticeRecord> dayWrapper = new QueryWrapper<>();
            dayWrapper.eq("user_id", userId)
                     .apply("DATE(practice_time) = {0}", date.toString());
            long dayCount = practiceRecordService.count(dayWrapper);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("count", dayCount);
            recentPractice.add(dayData);
        }
        dto.setRecentPractice(recentPractice);
        
        return Result.success(dto);
    }

    /**
     * 清空错题本 - 仅清空当前用户的
     * 支持按科目筛选清空
     * 
     * @param subject 科目名称（可选，不传则清空全部）
     * @return 结果
     */
    @DeleteMapping("/wrong")
    public Result<String> clearWrongQuestions(@RequestParam(required = false) String subject) {
        Long userId = getCurrentUserId();
        
        if (StrUtil.isNotBlank(subject)) {
            // 按科目清空
            // 1. 获取该科目下的所有错题ID
            List<Long> wrongQuestionIds = wrongBookService.getWrongQuestionIds(userId);
            if (!wrongQuestionIds.isEmpty()) {
                // 2. 查询该科目的题目
                QueryWrapper<Question> qWrapper = new QueryWrapper<>();
                qWrapper.in("id", wrongQuestionIds).eq("subject", subject);
                List<Question> subjectQuestions = questionService.list(qWrapper);
                
                // 3. 获取要删除的题目ID列表
                List<Long> toDeleteIds = subjectQuestions.stream()
                        .map(Question::getId)
                        .collect(Collectors.toList());
                
                if (!toDeleteIds.isEmpty()) {
                    // 4. 从错题本删除
                    wrongBookService.removeByQuestionIds(userId, toDeleteIds);
                    
                    // 5. 清理相关统计
                    userQuestionStatsService.clearWrongRecordsByQuestionIds(userId, toDeleteIds);
                }
            }
            return Result.success("已清空 " + subject + " 的错题");
        }
        
        // 清空全部
        // 清除用户题目统计表中的错题记录
        userQuestionStatsService.clearAllWrongRecords(userId);
        
        // 清空错题本表（wrong_book）中的记录
        wrongBookService.clearAllWrongQuestions(userId);
        
        // 可选：同时删除练习记录中的错误记录
        QueryWrapper<PracticeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_correct", false);
        practiceRecordService.remove(wrapper);
        
        return Result.success("已清空错题本");
    }

    /**
     * 切换题目收藏状态
     * 
     * @param questionId 题目ID
     * @return 新的收藏状态
     */
    @PostMapping("/mark/{questionId}")
    public Result<Map<String, Object>> toggleMark(@PathVariable Long questionId) {
        Long userId = getCurrentUserId();
        
        // 验证题目存在
        Question question = questionService.getById(questionId);
        if (question == null) {
            return Result.error("题目不存在");
        }
        
        boolean newMarkStatus = userQuestionStatsService.toggleMark(userId, questionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isMarked", newMarkStatus);
        result.put("questionId", questionId);
        
        return Result.success(result);
    }

    /**
     * 获取收藏的题目列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 收藏题目分页数据
     */
    @GetMapping("/marked")
    public Result<PageResult<Question>> getMarkedQuestions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = getCurrentUserId();
        
        // 获取收藏的题目ID列表
        List<Long> markedIds = userQuestionStatsService.getMarkedQuestionIds(userId);
        
        if (markedIds.isEmpty()) {
            return Result.success(new PageResult<>(null, 0L, (long) page, (long) size));
        }
        
        // 分页查询
        Page<Question> questionPage = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.in("id", markedIds);
        questionService.page(questionPage, wrapper);
        
        // 设置收藏状态
        for (Question q : questionPage.getRecords()) {
            q.setIsMarked(true);
        }
        
        PageResult<Question> pageResult = new PageResult<>(
                questionPage.getRecords(),
                questionPage.getTotal(),
                questionPage.getCurrent(),
                questionPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    // ==================== 轮次刷题 API ====================

    /**
     * 开始或继续某科目的练习轮次
     * 如果有未完成的轮次则继续，否则创建新轮次（题目随机打乱）
     * 
     * @param subject 科目名称
     * @return 当前题目
     */
    @PostMapping("/round/start")
    public Result<Map<String, Object>> startRound(@RequestParam String subject, @RequestParam(required = false) Long ownerId) {
        Long userId = getCurrentUserId();
        
        Question question = practiceRoundService.startOrContinueRound(userId, subject, ownerId);
        if (question == null) {
            return Result.error("该科目暂无题目");
        }
        
        PracticeRound progress = practiceRoundService.getProgress(userId, subject);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("currentIndex", progress.getCurrentIndex());
        result.put("totalCount", progress.getTotalCount());
        result.put("roundNumber", progress.getRoundNumber());
        result.put("isFinished", progress.getIsFinished());
        
        return Result.success(result);
    }

    /**
     * 获取轮次中的下一题
     * 
     * @param subject 科目名称
     * @return 下一题，如果已完成本轮则返回完成提示
     */
    @GetMapping("/round/next")
    public Result<Map<String, Object>> nextRoundQuestion(@RequestParam String subject) {
        Long userId = getCurrentUserId();
        
        Question question = practiceRoundService.nextQuestion(userId, subject);
        PracticeRound progress = practiceRoundService.getProgress(userId, subject);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("currentIndex", progress.getCurrentIndex());
        result.put("totalCount", progress.getTotalCount());
        result.put("isFinished", progress.getIsFinished());
        
        if (question == null && progress.getIsFinished()) {
            result.put("message", "恭喜！本轮已全部完成，共 " + progress.getTotalCount() + " 道题目");
        }
        
        return Result.success(result);
    }

    /**
     * 获取轮次中的上一题
     * 
     * @param subject 科目名称
     * @return 上一题
     */
    @GetMapping("/round/prev")
    public Result<Map<String, Object>> prevRoundQuestion(@RequestParam String subject) {
        Long userId = getCurrentUserId();
        
        Question question = practiceRoundService.prevQuestion(userId, subject);
        PracticeRound progress = practiceRoundService.getProgress(userId, subject);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("currentIndex", progress != null ? progress.getCurrentIndex() : 0);
        result.put("totalCount", progress != null ? progress.getTotalCount() : 0);
        
        if (question == null) {
            result.put("message", "已是第一题");
        }
        
        return Result.success(result);
    }

    /**
     * 获取当前轮次进度
     * 
     * @param subject 科目名称
     * @return 进度信息
     */
    @GetMapping("/round/progress")
    public Result<Map<String, Object>> getRoundProgress(@RequestParam String subject) {
        Long userId = getCurrentUserId();
        
        PracticeRound progress = practiceRoundService.getProgress(userId, subject);
        
        Map<String, Object> result = new HashMap<>();
        if (progress == null) {
            result.put("hasRound", false);
        } else {
            result.put("hasRound", true);
            result.put("currentIndex", progress.getCurrentIndex());
            result.put("totalCount", progress.getTotalCount());
            result.put("roundNumber", progress.getRoundNumber());
            result.put("isFinished", progress.getIsFinished());
            // 当前进度百分比
            int percent = progress.getTotalCount() > 0 
                ? (int) ((progress.getCurrentIndex() + 1) * 100.0 / progress.getTotalCount()) 
                : 0;
            result.put("progressPercent", percent);
        }
        
        return Result.success(result);
    }

    /**
     * 重置轮次（开始新一轮）
     * 
     * @param subject 科目名称
     * @return 新轮次的第一题
     */
    @PostMapping("/round/reset")
    public Result<Map<String, Object>> resetRound(@RequestParam String subject, @RequestParam(required = false) Long ownerId) {
        Long userId = getCurrentUserId();
        
        Question question = practiceRoundService.resetRound(userId, subject, ownerId);
        if (question == null) {
            return Result.error("该科目暂无题目");
        }
        
        PracticeRound progress = practiceRoundService.getProgress(userId, subject);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("currentIndex", 0);
        result.put("totalCount", progress.getTotalCount());
        result.put("roundNumber", progress.getRoundNumber());
        result.put("message", "已开始第 " + progress.getRoundNumber() + " 轮练习");
        
        return Result.success(result);
    }

    /**
     * 跳转到指定索引的题目
     * 
     * @param subject 科目名称
     * @param index 索引位置
     * @return 对应的题目信息
     */
    @GetMapping("/round/jump")
    public Result<Map<String, Object>> jumpRoundQuestion(@RequestParam String subject, @RequestParam int index) {
        Long userId = getCurrentUserId();
        
        Question question = practiceRoundService.jumpToIndex(userId, subject, index);
        if (question == null) {
            return Result.error("跳转失败，索引超出范围或题目不存在");
        }
        
        PracticeRound progress = practiceRoundService.getProgress(userId, subject);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("currentIndex", progress.getCurrentIndex());
        result.put("totalCount", progress.getTotalCount());
        result.put("isFinished", progress.getIsFinished());
        
        return Result.success(result);
    }

    /**
     * 获取当前轮次所有题目的答题状态
     * 
     * @param subject 科目名称
     * @return 状态映射（索引 -> 状态: 0-未做, 1-正确, 2-错误）
     */
    @GetMapping("/round/results")
    public Result<Map<Integer, Integer>> getRoundResults(@RequestParam String subject) {
        Long userId = getCurrentUserId();
        return Result.success(practiceRoundService.getRoundResults(userId, subject));
    }

    // ==================== 搜索 API ====================

    /**
     * 搜索题目（按题号或内容关键词）
     * 
     * @param keyword 搜索关键词（可以是题目ID或内容片段）
     * @param subject 科目筛选（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Result<PageResult<Question>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Question> questionPage = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        
        // 判断是否为数字（题目ID）
        if (keyword.matches("\\d+")) {
            wrapper.eq("id", Long.parseLong(keyword));
        } else {
            // 按内容模糊搜索
            wrapper.like("content", keyword);
        }
        
        // 科目筛选
        if (StrUtil.isNotBlank(subject)) {
            wrapper.eq("subject", subject);
        }
        
        wrapper.orderByDesc("id");
        questionService.page(questionPage, wrapper);
        
        PageResult<Question> result = new PageResult<>(
                questionPage.getRecords(),
                questionPage.getTotal(),
                questionPage.getCurrent(),
                questionPage.getSize()
        );
        
        return Result.success(result);
    }

    // ==================== 错题本增强 API ====================

    /**
     * 手动添加题目到错题本
     * 
     * @param questionId 题目ID
     * @return 操作结果
     */
    @PostMapping("/wrong-book/add/{questionId}")
    public Result<String> addToWrongBook(@PathVariable Long questionId) {
        Long userId = getCurrentUserId();
        
        Question question = questionService.getById(questionId);
        if (question == null) {
            return Result.error("题目不存在");
        }
        
        wrongBookService.addWrongQuestion(userId, questionId);
        return Result.success("已添加到错题本");
    }

    /**
     * 标记错题已掌握
     * 
     * @param questionId 题目ID
     * @return 操作结果
     */
    @PostMapping("/wrong-book/master/{questionId}")
    public Result<String> markMastered(@PathVariable Long questionId) {
        Long userId = getCurrentUserId();
        
        wrongBookService.markMastered(userId, questionId);
        return Result.success("已标记为掌握");
    }

    /**
     * 获取错题本分页列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 错题分页数据
     */
    @GetMapping("/wrong-book/list")
    public Result<PageResult<Question>> getWrongBookPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = getCurrentUserId();
        Page<Question> questionPage = wrongBookService.getWrongQuestionPage(userId, (long) page, (long) size);
        
        PageResult<Question> result = new PageResult<>(
                questionPage.getRecords(),
                questionPage.getTotal(),
                questionPage.getCurrent(),
                questionPage.getSize()
        );
        
        return Result.success(result);
    }

    /**
     * 获取错题本按科目的统计信息
     * 
     * @return 各科目的错题数量统计
     */
    @GetMapping("/wrong-book/subjects")
    public Result<Map<String, Long>> getWrongBookSubjects() {
        Long userId = getCurrentUserId();
        
        // 获取错题本中的所有题目ID（使用 wrong_book 表）
        List<Long> wrongQuestionIds = wrongBookService.getWrongQuestionIds(userId);
        
        if (wrongQuestionIds.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        
        // 查询错题的科目分布
        List<Question> questions = questionService.listByIds(wrongQuestionIds);
        Map<String, Long> subjectCount = questions.stream()
                .collect(Collectors.groupingBy(Question::getSubject, Collectors.counting()));
        
        return Result.success(subjectCount);
    }

    /**
     * 开始错题专项练习（按科目）
     * 
     * @param subject 科目名称（可选，不传则练习所有错题）
     * @return 第一道错题
     */
    @PostMapping("/wrong-book/practice")
    public Result<Map<String, Object>> startWrongBookPractice(@RequestParam(required = false) String subject) {
        Long userId = getCurrentUserId();
        
        // 获取错题本中的题目ID列表（使用 wrong_book 表）
        List<Long> wrongQuestionIds = wrongBookService.getWrongQuestionIds(userId);
        
        if (wrongQuestionIds.isEmpty()) {
            return Result.error("暂无错题");
        }
        
        // 如果指定了科目，筛选该科目的错题
        List<Question> questions;
        if (StrUtil.isNotBlank(subject)) {
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.in("id", wrongQuestionIds)
                   .eq("subject", subject);
            questions = questionService.list(wrapper);
        } else {
            questions = questionService.listByIds(wrongQuestionIds);
        }
        
        if (questions.isEmpty()) {
            return Result.error("该科目暂无错题");
        }
        
        // 按 wrongQuestionIds 的顺序对 questions 进行排序（确保顺序一致性）
        questions.sort(java.util.Comparator.comparingInt(q -> wrongQuestionIds.indexOf(q.getId())));
        
        // 移除随机打乱，因为无状态的 next 接口无法获知打乱后的顺序
        // Collections.shuffle(questions);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", questions.get(0));
        result.put("totalCount", questions.size());
        result.put("subject", subject);
        result.put("currentIndex", 0);
        
        return Result.success(result);
    }

    /**
     * 获取错题练习的下一题
     * 
     * @param subject 科目名称（可选）
     * @param currentQuestionId 当前题目ID
     * @return 下一道错题
     */
    @GetMapping("/wrong-book/practice/next")
    public Result<Map<String, Object>> nextWrongQuestion(
            @RequestParam(required = false) String subject,
            @RequestParam Long currentQuestionId) {
        Long userId = getCurrentUserId();
        
        // 获取错题本中的题目ID列表（使用 wrong_book 表）
        List<Long> wrongQuestionIds = wrongBookService.getWrongQuestionIds(userId);
        
        if (wrongQuestionIds.isEmpty()) {
            return Result.error("暂无错题");
        }
        
        // 筛选科目
        List<Question> questions;
        if (StrUtil.isNotBlank(subject)) {
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.in("id", wrongQuestionIds)
                   .eq("subject", subject);
            questions = questionService.list(wrapper);
        } else {
            questions = questionService.listByIds(wrongQuestionIds);
        }
        
        if (questions.isEmpty()) {
            return Result.error("暂无更多错题");
        }

        // 关键修复：必须再次按 wrongQuestionIds 排序，确保与 start 接口的顺序一致
        questions.sort(java.util.Comparator.comparingInt(q -> wrongQuestionIds.indexOf(q.getId())));
        
        // 找到当前题目的索引
        int currentIndex = -1;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId().equals(currentQuestionId)) {
                currentIndex = i;
                break;
            }
        }
        
        // 获取下一题
        int nextIndex = currentIndex + 1;
        if (nextIndex >= questions.size()) {
            Map<String, Object> result = new HashMap<>();
            result.put("question", null);
            result.put("isFinished", true);
            result.put("message", "恭喜！错题已全部练习完成");
            return Result.success(result);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", questions.get(nextIndex));
        result.put("currentIndex", nextIndex);
        result.put("totalCount", questions.size());
        result.put("isFinished", false);
        
        return Result.success(result);
    }

    // ==================== 练习记录查询 ====================

    @Autowired
    private com.exam.service.UserService userService;

    /**
     * 获取练习记录列表（分页，管理员可查看任意用户，普通用户只能查自己）
     */
    @GetMapping("/records")
    public Result<PageResult<Map<String, Object>>> getPracticeRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId) {
        
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = userService.getById(currentUserId) != null 
                && "admin".equals(userService.getById(currentUserId).getRole());
        
        // 权限检查：普通用户只能查自己
        Long queryUserId = currentUserId;
        if (isAdmin && userId != null) {
            queryUserId = userId;
        }
        
        Page<PracticeRecord> recordPage = new Page<>(page, size);
        QueryWrapper<PracticeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", queryUserId);
        wrapper.orderByDesc("practice_time");
        
        practiceRecordService.page(recordPage, wrapper);
        
        // 组装返回数据，包含题目信息
        List<Map<String, Object>> records = recordPage.getRecords().stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("userAnswer", record.getUserAnswer());
            map.put("isCorrect", record.getIsCorrect());
            map.put("practiceTime", record.getPracticeTime());
            map.put("roundNumber", record.getRoundNumber());
            
            // 添加题目信息
            Question question = questionService.getById(record.getQuestionId());
            if (question != null) {
                map.put("questionId", question.getId());
                map.put("questionContent", question.getContent());
                map.put("questionSubject", question.getSubject());
                map.put("questionType", question.getType());
                map.put("correctAnswer", question.getAnswer());
            }
            return map;
        }).collect(Collectors.toList());
        
        PageResult<Map<String, Object>> pageResult = new PageResult<>(
            records,
            recordPage.getTotal(),
            recordPage.getCurrent(),
            recordPage.getSize()
        );
        
        return Result.success(pageResult);
    }
}
