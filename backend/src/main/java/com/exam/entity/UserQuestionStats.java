package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户题目统计实体类
 * 记录每个用户对每道题目的练习统计（替代原来 Question 表中的全局统计字段）
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Data
@TableName("user_question_stats")
public class UserQuestionStats {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 练习次数
     */
    private Integer practiceCount;

    /**
     * 错误次数
     */
    private Integer wrongCount;

    /**
     * 是否收藏
     */
    private Boolean isMarked;

    /**
     * 最后练习时间
     */
    private LocalDateTime lastPracticeTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
