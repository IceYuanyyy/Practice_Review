package com.exam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池配置属性
 * 
 * @author Exam System
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "thread.pool.executor.config")
public class ThreadPoolConfigProperties {

    /** 核心线程数 */
    private Integer corePoolSize = 10;

    /** 最大线程数 */
    private Integer maxPoolSize = 20;

    /** 线程活跃时间（毫秒） */
    private Long keepAliveTime = 5000L;

    /** 阻塞队列大小 */
    private Integer blockQueueSize = 5000;

    /** 拒绝策略 (CallerRunsPolicy, AbortPolicy, DiscardPolicy, DiscardOldestPolicy) */
    private String policy = "CallerRunsPolicy";
}
