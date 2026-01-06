package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.Result;
import com.exam.dto.UserVO;
import com.exam.entity.User;
import com.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户 Controller
 * 处理用户个人信息相关操作
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前用户的个人资料
     * 
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        Long userId = getCurrentUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(UserVO.fromUser(user));
    }

    /**
     * 更新用户个人资料
     * 
     * @param user 用户信息（只允许修改昵称、邮箱、头像）
     * @return 操作结果
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody User user) {
        Long userId = getCurrentUserId();
        
        // 确保只修改当前用户自己的信息
        user.setId(userId);
        
        // 不允许通过此接口修改敏感字段
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        
        boolean success = userService.updateUserInfo(user);
        if (!success) {
            return Result.error("更新失败");
        }
        
        // 返回更新后的用户信息
        User updatedUser = userService.getById(userId);
        log.info("用户更新个人资料: userId={}", userId);
        return Result.success("更新成功", UserVO.fromUser(updatedUser));
    }

    /**
     * 修改密码
     * 
     * @param passwordData 密码数据 { oldPassword, newPassword }
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<String> changePassword(@RequestBody Map<String, String> passwordData) {
        Long userId = getCurrentUserId();
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        
        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (!success) {
            return Result.error("修改失败");
        }
        
        log.info("用户修改密码: userId={}", userId);
        return Result.success("密码修改成功");
    }

    /**
     * 获取所有用户列表（仅管理员）
     * 用于管理功能的用户选择器
     * 
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<java.util.List<UserVO>> getAllUsers() {
        Long userId = getCurrentUserId();
        User currentUser = userService.getById(userId);
        
        // 权限检查：仅管理员可访问
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error("无权访问");
        }
        
        // 获取所有用户
        java.util.List<User> users = userService.list();
        java.util.List<UserVO> userVOList = users.stream()
            .map(UserVO::fromUser)
            .collect(java.util.stream.Collectors.toList());
        
        return Result.success(userVOList);
    }
}
