package com.exam.service;

/**
 * 验证码服务接口
 * 
 * @author Exam System
 * @since 2026-01-08
 */
public interface VerificationCodeService {

    /**
     * 发送验证码到指定邮箱
     * 
     * @param email 邮箱地址
     * @throws RuntimeException 如果60秒内重复发送或发送失败
     */
    void sendCode(String email);

    /**
     * 验证验证码是否正确
     * 
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否正确
     */
    boolean verifyCode(String email, String code);

    /**
     * 使验证码失效（注册成功后调用）
     * 
     * @param email 邮箱地址
     */
    void invalidateCode(String email);
}
