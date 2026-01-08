package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统公告实体类
 * 
 * @author Exam System
 * @since 2026-01-08
 */
@Data
@TableName("announcement")
public class Announcement {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容（支持HTML）
     */
    private String content;

    /**
     * 发布者ID（管理员）
     */
    private Long userId;

    /**
     * 状态：0-草稿, 1-已发布, 2-已撤回
     */
    private Integer status;

    /**
     * 是否置顶：0-否, 1-是
     */
    private Boolean isPinned;

    /**
     * 发布者用户名（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private String publisherName;

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
