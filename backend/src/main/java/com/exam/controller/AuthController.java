package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.Result;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.UserVO;
import com.exam.service.UserService;
import com.exam.service.VerificationCodeService;
import com.exam.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller
 * 处理用户登录、注册、登出等操作
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 用户登录
     * 
     * @param loginDTO 登录信息
     * @param request HTTP请求
     * @return Token和用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO, 
                                              HttpServletRequest request) {
        // 获取客户端真实IP
        String ip = IpUtil.getClientIp(request);
        // 获取IP归属地
        String location = IpUtil.getIpLocation(ip);
        // 获取 User-Agent
        String userAgent = request.getHeader("User-Agent");

        // 执行登录（传入位置信息）
        String token = userService.login(loginDTO, ip, location, userAgent);

        // 获取用户信息
        UserVO userVO = userService.getCurrentUser();

        // 返回 Token 和用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userVO);

        log.info("用户登录成功: username={}", loginDTO.getUsername());
        return Result.success("登录成功", result);
    }

    /**
     * 发送注册验证码
     * 
     * @param email 邮箱地址
     * @return 发送结果
     */
    @PostMapping("/send-code")
    public Result<String> sendVerificationCode(@RequestParam String email) {
        // 简单验证邮箱格式
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.error("邮箱格式不正确");
        }
        
        try {
            verificationCodeService.sendCode(email);
            return Result.success("验证码已发送，请查收邮件");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 绑定/验证邮箱（用于老用户补全验证）
     * 
     * @param body map包含email和code
     * @return 结果
     */
    @PostMapping("/bind-email")
    public Result<String> bindEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录");
        }
        
        // 验证验证码
        boolean isValid = verificationCodeService.verifyCode(email, code);
        if (!isValid) {
            return Result.error("验证码错误或已过期");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        userService.bindEmail(userId, email);
        
        // 使验证码失效
        verificationCodeService.invalidateCode(email);
        
        return Result.success("验证成功");
    }

    /**
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Validated @RequestBody RegisterDTO registerDTO) {
        // 验证验证码
        boolean isValid = verificationCodeService.verifyCode(
            registerDTO.getEmail(), 
            registerDTO.getVerificationCode()
        );
        if (!isValid) {
            return Result.error("验证码错误或已过期");
        }
        
        // 执行注册
        UserVO userVO = userService.register(registerDTO);
        
        // 注册成功后使验证码失效
        verificationCodeService.invalidateCode(registerDTO.getEmail());
        
        log.info("用户注册成功: username={}", registerDTO.getUsername());
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登出
     * 
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        userService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/user")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        if (userVO == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userVO);
    }

    /**
     * 检查登录状态
     * 
     * @return 登录状态
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkLogin() {
        Map<String, Object> result = new HashMap<>();
        result.put("isLogin", StpUtil.isLogin());
        if (StpUtil.isLogin()) {
            result.put("userId", StpUtil.getLoginId());
            result.put("tokenTimeout", StpUtil.getTokenTimeout());
        }
        return Result.success(result);
    }

}
