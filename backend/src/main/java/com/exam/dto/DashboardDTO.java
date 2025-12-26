package com.exam.dto;

import lombok.Data;

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
}
