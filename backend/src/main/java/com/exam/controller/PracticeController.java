package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.PracticeRecord;
import com.exam.entity.Question;
import com.exam.service.PracticeRecordService;
import com.exam.service.QuestionService;
import com.exam.dto.DashboardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 练习 Controller
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PracticeRecordService practiceRecordService;

    /**
     * 提交答题记录
     * 
     * @param record 练习记录
     * @return 答题结果（是否正确、正确答案、解析）
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswer(@RequestBody PracticeRecord record) {
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

        // 保存练习记录
        practiceRecordService.save(record);

        // 更新题目统计
        question.setPracticeCount(question.getPracticeCount() + 1);
        if (!isCorrect) {
            question.setWrongCount(question.getWrongCount() + 1);
        }
        questionService.updateById(question);

        // 返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isCorrect", isCorrect);
        resultMap.put("correctAnswer", question.getAnswer());
        resultMap.put("analysis", question.getAnalysis());
        resultMap.put("userAnswer", record.getUserAnswer());

        return Result.success(resultMap);
    }

    /**
     * 获取错题列表（分页）
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
        
        // 查询做错的题目ID列表
        QueryWrapper<PracticeRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("is_correct", false)
                .select("DISTINCT question_id");
        List<PracticeRecord> wrongRecords = practiceRecordService.list(recordWrapper);
        
        if (wrongRecords.isEmpty()) {
            return Result.success(new PageResult<>(null, 0L, (long) page, (long) size));
        }
        
        // 提取题目ID列表
        List<Long> questionIds = wrongRecords.stream()
                .map(PracticeRecord::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        
        // 分页查询错题
        Page<Question> questionPage = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.in("id", questionIds);
        
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq("subject", subject);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        
        wrapper.orderByDesc("wrong_count");
        questionService.page(questionPage, wrapper);
        
        PageResult<Question> pageResult = new PageResult<>(
                questionPage.getRecords(),
                questionPage.getTotal(),
                questionPage.getCurrent(),
                questionPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 获取统计数据
     * 
     * @return 统计信息
     */
    /**
     * 获取统计数据
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<DashboardDTO> getStatistics() {
        DashboardDTO dto = new DashboardDTO();
        
        // 总题目数
        long totalQuestions = questionService.count();
        dto.setTotalQuestions(totalQuestions);
        
        // 已练习题目数（练习次数 > 0，即去重后的题目数）
        // 修正：直接从练习记录表中查询去重后的题目ID数量，确保与清除操作同步
        QueryWrapper<PracticeRecord> practicedWrapper = new QueryWrapper<>();
        practicedWrapper.select("DISTINCT question_id");
        // 注意：MyBatis-Plus的count方法可能会忽略select distinct，因此这里先查列表再计数，或者使用自定义SQL
        // 为简单起见，且数据量预期不大，使用流处理。若数据量大建议优化为 Mapper XML 查询
        List<Object> practicedQuestionIds = practiceRecordService.listObjs(practicedWrapper);
        dto.setPracticedQuestionCount((long) practicedQuestionIds.size());
        
        // 总练习次数
        long totalPracticeCount = practiceRecordService.count();
        dto.setTotalPracticeCount(totalPracticeCount);
        
        // 正确次数
        QueryWrapper<PracticeRecord> correctWrapper = new QueryWrapper<>();
        correctWrapper.eq("is_correct", true);
        long correctCount = practiceRecordService.count(correctWrapper);
        
        // 正确率
        double correctRate = totalPracticeCount > 0 ? 
                (double) correctCount / totalPracticeCount * 100 : 0;
        dto.setCorrectRate(String.format("%.0f", correctRate)); // 保持整数或根据需求保留小数，UI显示百分号
        
        // 错题数（有错误记录的题目数）
        QueryWrapper<PracticeRecord> wrongRecordWrapper = new QueryWrapper<>();
        wrongRecordWrapper.eq("is_correct", false)
                .select("DISTINCT question_id");
        long wrongQuestionCount = practiceRecordService.list(wrongRecordWrapper).stream()
                .map(PracticeRecord::getQuestionId)
                .distinct()
                .count();
        dto.setWrongQuestionCount(wrongQuestionCount);
        
        return Result.success(dto);
    }
    /**
     * 清空错题本
     * 
     * @return 结果
     */
    @DeleteMapping("/wrong")
    public Result<String> clearWrongQuestions() {
        QueryWrapper<PracticeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("is_correct", false);
        boolean success = practiceRecordService.remove(wrapper);
        
        // 可选：同时重置题目表中的相关统计数据（如果需要更彻底的清理）
        // 但根据需求，只需清空记录即可
        
        return success ? Result.success("已清空错题本") : Result.error("清空失败");
    }
}
