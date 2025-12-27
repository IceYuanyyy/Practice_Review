package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.PracticeRecord;
import com.exam.entity.Question;
import com.exam.service.PracticeRecordService;
import com.exam.service.QuestionService;
import com.exam.service.UserQuestionStatsService;
import com.exam.dto.DashboardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

        // 保存练习记录
        practiceRecordService.save(record);

        // 更新用户题目统计（替代原来的全局统计）
        userQuestionStatsService.updatePracticeStats(userId, record.getQuestionId(), isCorrect);

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
        
        return Result.success(dto);
    }

    /**
     * 清空错题本 - 仅清空当前用户的
     * 
     * @return 结果
     */
    @DeleteMapping("/wrong")
    public Result<String> clearWrongQuestions() {
        Long userId = getCurrentUserId();
        
        // 清除用户题目统计表中的错题记录
        userQuestionStatsService.clearAllWrongRecords(userId);
        
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
}
