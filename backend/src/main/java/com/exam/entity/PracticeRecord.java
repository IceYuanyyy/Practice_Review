package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 练习记录实体类
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Data
@TableName("practice_record")
public class PracticeRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（练习记录所属用户）
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 是否正确
     */
    private Boolean isCorrect;

    /**
     * 答题耗时（秒）
     */
    private Integer costTime;

    /**
     * 练习时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime practiceTime;
}
