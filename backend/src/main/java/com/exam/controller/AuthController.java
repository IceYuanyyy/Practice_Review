package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.Result;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.UserVO;
import com.exam.service.UserService;
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
        // 获取客户端IP
        String ip = getClientIp(request);
        // 获取 User-Agent
        String userAgent = request.getHeader("User-Agent");

        // 执行登录
        String token = userService.login(loginDTO, ip, userAgent);

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
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Validated @RequestBody RegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
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

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
