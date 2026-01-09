package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Data
@TableName(value = "question", autoResultMap = true)
public class Question {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目所属用户ID（NULL表示公共题库）
     */
    private Long ownerId;

    /**
     * 题型：single-choice(单选题)/multiple-choice(多选题)/judge(判断题)
     */
    private String type;

    /**
     * 显示顺序（按类型分组，从1开始）
     */
    private Integer displayOrder;

    /**
     * 科目
     */
    private String subject;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目图片URL
     */
    private String imageUrl;

    /**
     * 选项（选择题用）
     * JSON格式：["A:选项1","B:选项2","C:选项3","D:选项4"]
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> options;

    /**
     * 答案
     */
    private String answer;

    /**
     * 答案解析
     */
    private String analysis;

    /**
     * 难度：easy/medium/hard
     */
    private String difficulty;

    /**
     * 是否标记为重点
     */
    private Boolean isMarked;

    /**
     * 做错次数
     */
    private Integer wrongCount;

    /**
     * 练习次数
     */
    private Integer practiceCount;

    /**
     * 关联的导入日志ID
     */
    private Long importLogId;

    /**
     * 题目所属用户名称（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String ownerName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
