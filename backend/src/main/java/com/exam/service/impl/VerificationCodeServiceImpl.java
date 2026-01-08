package com.exam.service.impl;

import com.exam.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现类
 * 使用内存存储验证码，服务重启后验证码会失效
 * 
 * @author Exam System
 * @since 2026-01-08
 */
@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 验证码存储：邮箱 -> 验证码信息
     */
    private final Map<String, CodeInfo> codeCache = new ConcurrentHashMap<>();

    /**
     * 验证码有效期（5分钟）
     */
    private static final long CODE_EXPIRE_TIME = 5 * 60 * 1000;

    /**
     * 发送间隔（60秒）
     */
    private static final long SEND_INTERVAL = 60 * 1000;

    @Override
    public void sendCode(String email) {
        // 检查是否在发送间隔内
        CodeInfo existingCode = codeCache.get(email);
        if (existingCode != null) {
            long timeSinceLastSend = System.currentTimeMillis() - existingCode.sendTime;
            if (timeSinceLastSend < SEND_INTERVAL) {
                long remainingSeconds = (SEND_INTERVAL - timeSinceLastSend) / 1000;
                throw new RuntimeException("请等待 " + remainingSeconds + " 秒后再发送");
            }
        }

        // 生成6位随机验证码
        String code = generateCode();

        // 发送邮件
        sendEmail(email, code);

        // 存储验证码
        codeCache.put(email, new CodeInfo(code, System.currentTimeMillis()));
        log.info("验证码已发送: email={}, code={}", email, code);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        CodeInfo codeInfo = codeCache.get(email);
        if (codeInfo == null) {
            log.warn("验证码不存在: email={}", email);
            return false;
        }

        // 检查是否过期
        if (System.currentTimeMillis() - codeInfo.sendTime > CODE_EXPIRE_TIME) {
            log.warn("验证码已过期: email={}", email);
            codeCache.remove(email);
            return false;
        }

        // 验证码比对（忽略大小写）
        boolean isValid = codeInfo.code.equalsIgnoreCase(code);
        if (!isValid) {
            log.warn("验证码错误: email={}, expected={}, got={}", email, codeInfo.code, code);
        }
        return isValid;
    }

    @Override
    public void invalidateCode(String email) {
        codeCache.remove(email);
        log.info("验证码已失效: email={}", email);
    }

    /**
     * 生成6位随机数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 发送验证码邮件
     */
    private void sendEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【练习系统】注册验证码");

            String content = buildEmailContent(code);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("✅ 验证码邮件发送成功: toEmail={}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ 验证码邮件发送失败: {}", e.getMessage(), e);
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建邮件HTML内容
     */
    /**
     * 构建邮件HTML内容
     */
    private String buildEmailContent(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Exam Master Verification</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Comic Sans MS', 'Chalkboard SE', 'Microsoft YaHei', sans-serif; background-color: #f0fdf4;">
                <div style="padding: 40px 20px;">
                    <!-- 卡片容器 -->
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 3px solid #1e293b; border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px; box-shadow: 10px 10px 0px rgba(30, 41, 59, 0.1); overflow: hidden;">
                        
                        <!-- 头部 - 胶带效果 -->
                        <div style="text-align: center; position: relative; padding-top: 30px;">
                            <div style="background-color: rgba(254, 243, 199, 0.8); display: inline-block; padding: 5px 30px; transform: rotate(-2deg); border: 1px dashed #d1d5db; box-shadow: 0 2px 4px rgba(0,0,0,0.05); color: #854d0e; font-weight: bold; font-size: 14px;">
                                FROM: Exam Master HQ
                            </div>
                        </div>

                        <!-- 头部 Logo -->
                        <div style="text-align: center; padding: 20px 0 10px;">
                            <h1 style="color: #1e293b; font-size: 36px; margin: 0; transform: rotate(-1deg); text-shadow: 2px 2px 0px rgba(0,0,0,0.1);">
                                <span style="color: #10b981;">Exam</span> Master
                            </h1>
                            <p style="margin: 5px 0 0; color: #64748b; font-size: 16px;">
                                你的终极备考手账本 📝
                            </p>
                        </div>
                        
                        <div style="padding: 0 40px 40px;">
                            <hr style="border: none; border-top: 2px dashed #e2e8f0; margin: 20px 0;">

                            <!-- 问候与场景 -->
                            <div style="text-align: center; margin-bottom: 30px;">
                                <div style="font-size: 48px; margin-bottom: 10px; display: inline-block; animation: bounce 1s infinite;">🚀</div>
                                <h2 style="color: #1e293b; font-size: 24px; margin: 0 0 10px; text-transform: uppercase; letter-spacing: 1px;">WELCOME ABOARD!</h2>
                                <p style="color: #475569; font-size: 15px; line-height: 1.6; margin: 0;">
                                    感谢注册 Exam Master！准备好开启你的学习之旅了吗？<br>请使用下方的验证码解锁你的专属题库账号。
                                </p>
                            </div>
                            
                            <!-- 验证码卡片 -->
                            <div style="background-color: #f0f9ff; border: 2px solid #0ea5e9; border-radius: 12px; padding: 30px 20px; text-align: center; position: relative; margin: 30px 0;">
                                <div style="position: absolute; top: -12px; left: 50%%; transform: translateX(-50%%); background: #0ea5e9; color: white; padding: 2px 15px; border-radius: 20px; font-size: 12px; font-weight: bold;">
                                    VERIFICATION CODE
                                </div>
                                <div style="font-family: 'Courier New', monospace; font-size: 42px; font-weight: 900; color: #0369a1; letter-spacing: 8px; background: white; border: 2px dashed #bae6fd; padding: 15px; display: inline-block; border-radius: 8px;">
                                    %s
                                </div>
                                <p style="margin: 15px 0 0; color: #64748b; font-size: 13px;">
                                    👉 双击上方代码即可复制 • 有效期 5 分钟
                                </p>
                            </div>
                            
                            <!-- 安全提示 -->
                            <div style="background: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; border-radius: 4px; color: #92400e; font-size: 13px; line-height: 1.5;">
                                <strong>⚠️ 安全贴士：</strong> 验证码仅用于本次操作，请勿泄露给他人。如非本人操作，请忽略此邮件。
                            </div>

                        </div>
                        
                        <!-- 底部联系方式 -->
                        <div style="background-color: #1e293b; color: #94a3b8; padding: 30px 20px 40px; text-align: center; font-size: 13px;">
                            <div style="margin-bottom: 20px; display: flex; justify-content: center; gap: 15px; flex-wrap: wrap;">
                                <span style="background: rgba(255,255,255,0.1); padding: 5px 10px; border-radius: 4px;">QQ: 2478686497</span>
                                <span style="background: rgba(255,255,255,0.1); padding: 5px 10px; border-radius: 4px;">GitHub: IceYuanyyy</span>
                            </div>
                            <div style="margin-bottom: 20px; color: #64748b;">
                                📧 ercurym86@gmail.com <br>
                                🏫 2023213873@sdtbu.edu.cn
                            </div>
                            <div style="border-top: 1px solid #334155; padding-top: 20px; color: #64748b;">
                                <p style="margin: 0;">© 2026 Exam Master. Keep Learning!</p>
                                <p style="margin: 5px 0 0; font-size: 12px; opacity: 0.6;">Designed by IceYuanyyy</p>
                            </div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }

    /**
     * 验证码信息内部类
     */
    private record CodeInfo(String code, long sendTime) {
    }
}
