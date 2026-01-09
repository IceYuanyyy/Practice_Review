package com.exam.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.exam.entity.Question;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目导出 DTO
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Data
public class QuestionExportDTO {

    @ExcelProperty("题目ID")
    private Long id;

    @ExcelProperty("题型")
    private String type;

    @ExcelProperty("科目")
    private String subject;

    @ExcelProperty("题目")
    private String content;

    @ExcelProperty("图片URL")
    private String imageUrl;

    @ExcelProperty("选项A")
    private String optionA;

    @ExcelProperty("选项B")
    private String optionB;

    @ExcelProperty("选项C")
    private String optionC;

    @ExcelProperty("选项D")
    private String optionD;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("解析")
    private String analysis;

    @ExcelProperty("难度")
    private String difficulty;

    @ExcelProperty("练习次数")
    private Integer practiceCount;

    @ExcelProperty("错误次数")
    private Integer wrongCount;

    /**
     * 从 Question 实体转换为导出 DTO
     */
    public static QuestionExportDTO fromQuestion(Question question) {
        QuestionExportDTO dto = new QuestionExportDTO();
        dto.setId(question.getId());
        dto.setType(question.getType().equals("choice") ? "选择题" : "判断题");
        dto.setSubject(question.getSubject());
        dto.setContent(question.getContent());
        dto.setImageUrl(question.getImageUrl());
        dto.setAnswer(question.getAnswer());
        dto.setAnalysis(question.getAnalysis());
        
        // 处理难度
        String difficulty = question.getDifficulty();
        if ("easy".equals(difficulty)) {
            dto.setDifficulty("简单");
        } else if ("medium".equals(difficulty)) {
            dto.setDifficulty("中等");
        } else if ("hard".equals(difficulty)) {
            dto.setDifficulty("困难");
        } else {
            dto.setDifficulty(difficulty);
        }
        
        dto.setPracticeCount(question.getPracticeCount());
        dto.setWrongCount(question.getWrongCount());
        
        // 处理选项
        if (question.getType().equals("choice") && question.getOptions() != null) {
            List<String> options = question.getOptions();
            for (String option : options) {
                if (option.startsWith("A:")) {
                    dto.setOptionA(option.substring(2));
                } else if (option.startsWith("B:")) {
                    dto.setOptionB(option.substring(2));
                } else if (option.startsWith("C:")) {
                    dto.setOptionC(option.substring(2));
                } else if (option.startsWith("D:")) {
                    dto.setOptionD(option.substring(2));
                }
            }
        }
        
        return dto;
    }
}
