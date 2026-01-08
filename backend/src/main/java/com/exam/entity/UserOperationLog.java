package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户操作日志实体类
 * 记录用户关键操作信息，如导入、导出、批量删除等
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Data
@TableName("user_operation_log")
public class UserOperationLog {

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
     * 用户名
     */
    private String username;

    /**
     * 操作类型: IMPORT/EXPORT/BATCH_DELETE/CLEAR等
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作数据(JSON格式)
     */
    private String operationData;

    /**
     * 原始文件内容(Base64编码)
     */
    private String sourceFileContent;

    /**
     * 原始文件名
     */
    private String sourceFileName;

    /**
     * 结果文件内容(Base64编码)
     */
    private String resultFileContent;

    /**
     * 结果文件名
     */
    private String resultFileName;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求IP归属地
     */
    private String requestLocation;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 操作状态: 0失败/1成功
     */
    private Integer operationStatus;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 执行耗时(毫秒)
     */
    private Long costTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
}
