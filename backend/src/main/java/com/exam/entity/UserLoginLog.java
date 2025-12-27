package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 * 记录用户登录信息，包括IP、地点、浏览器等
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Data
@TableName("user_login_log")
public class UserLoginLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（登录失败时可能为NULL）
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 登录状态: 0失败/1成功
     */
    private Integer loginStatus;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
