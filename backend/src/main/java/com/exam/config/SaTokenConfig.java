package com.exam.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 配置登录拦截器，定义哪些接口需要登录才能访问
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                // 排除不需要登录的接口
                .excludePathPatterns(
                        // 认证相关
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/send-code",  // 发送验证码（注册用）
                        // 静态资源
                        "/static/**",
                        "/favicon.ico",
                        // Swagger (如果有)
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        // 错误页面
                        "/error"
                );
    }
}
