package com.exam.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 题目导入 DTO
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Data
public class QuestionImportDTO {

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

    @ExcelProperty("选项E")
    private String optionE;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("解析")
    private String analysis;

    @ExcelProperty("科目")
    private String subject;

    @ExcelProperty("难度")
    private String difficulty;
}
