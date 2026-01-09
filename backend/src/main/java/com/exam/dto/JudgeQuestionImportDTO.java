package com.exam.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 判断题导入 DTO
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Data
public class JudgeQuestionImportDTO {

    @ExcelProperty("题目")
    private String content;

    @ExcelProperty("图片URL")
    private String imageUrl;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("解析")
    private String analysis;

    @ExcelProperty("科目")
    private String subject;

    @ExcelProperty("难度")
    private String difficulty;
}
