package com.exam.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardDTO {
    private Long totalQuestions;
    // 练习过的题目数（去重）
    private Long practicedQuestionCount;
    // 总练习记录数（次数）
    private Long totalPracticeCount;
    // 错题数（去重）
    private Long wrongQuestionCount;
    // 正确率（百分比字符串）
    private String correctRate;
    
    // === 新增：ECharts 图表数据 ===
    // 科目分布 (科目名 -> 题目数)
    private Map<String, Long> subjectDistribution;
    // 题型分布 (题型 -> 题目数)
    private Map<String, Long> typeDistribution;
    // 难度分布 (难度 -> 题目数)
    private Map<String, Long> difficultyDistribution;
    // 收藏题目数
    private Long markedQuestionCount;
    // 最近7天练习次数 (日期 -> 练习次数)
    private List<Map<String, Object>> recentPractice;
}
