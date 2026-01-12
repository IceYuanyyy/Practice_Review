package com.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 
 * @author Exam System
 */
@Configuration
public class ThreadPoolConfig {

    @Resource
    private ThreadPoolConfigProperties properties;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setKeepAliveSeconds(properties.getKeepAliveTime().intValue() / 1000);
        executor.setQueueCapacity(properties.getBlockQueueSize());
        executor.setThreadNamePrefix("exam-task-");

        // 设置拒绝策略
        String policy = properties.getPolicy();
        if ("CallerRunsPolicy".equalsIgnoreCase(policy)) {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        } else if ("AbortPolicy".equalsIgnoreCase(policy)) {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        } else if ("DiscardPolicy".equalsIgnoreCase(policy)) {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        } else if ("DiscardOldestPolicy".equalsIgnoreCase(policy)) {
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        }

        executor.initialize();
        return executor;
    }
}
