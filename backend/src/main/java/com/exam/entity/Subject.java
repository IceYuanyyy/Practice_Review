package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 科目实体类
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Data
@TableName("subject")
public class Subject {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 导入用户名（非数据库字段，仅用于前端显示）
     */
    @TableField(exist = false)
    private String ownerName;
}

