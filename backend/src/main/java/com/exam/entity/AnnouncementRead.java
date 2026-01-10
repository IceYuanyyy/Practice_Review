package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告已读记录实体
 * 
 * @author Exam System
 * @since 2026-01-11
 */
@Data
@TableName("announcement_read")
public class AnnouncementRead {

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
     * 公告ID
     */
    private Long announcementId;

    /**
     * 阅读时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
