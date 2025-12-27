package com.exam.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.Question;
import com.exam.entity.User;
import com.exam.entity.UserLoginLog;
import com.exam.entity.UserOperationLog;
import com.exam.mapper.UserLoginLogMapper;
import com.exam.mapper.UserOperationLogMapper;
import com.exam.service.QuestionService;
import com.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员 Controller
 * 处理管理员后台功能：用户管理、日志查询、统计数据
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@SaCheckRole("admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserLoginLogMapper loginLogMapper;

    @Autowired
    private UserOperationLogMapper operationLogMapper;

    // ==================== 统计接口 ====================

    /**
     * 获取系统统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 用户总数
        long userCount = userService.count();
        stats.put("userCount", userCount);

        // 题目总数
        long questionCount = questionService.count();
        stats.put("questionCount", questionCount);

        // 今日登录次数
        Integer todayLoginCount = loginLogMapper.countTodayLogin();
        stats.put("todayLoginCount", todayLoginCount != null ? todayLoginCount : 0);

        // 今日活跃用户数
        Integer todayActiveCount = loginLogMapper.countTodayActiveUsers();
        stats.put("todayActiveCount", todayActiveCount != null ? todayActiveCount : 0);

        return Result.success(stats);
    }

    // ==================== 用户管理接口 ====================

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        
        Page<User> userPage = userService.getUserPage(page, size, username, role, status);
        
        PageResult<User> pageResult = new PageResult<>(
            userPage.getRecords(),
            userPage.getTotal(),
            userPage.getCurrent(),
            userPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 修改用户状态（启用/禁用）
     */
    @PutMapping("/users/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> data) {
        Integer status = data.get("status");
        if (status == null) {
            return Result.error("状态不能为空");
        }
        
        boolean success = userService.updateStatus(id, status);
        String msg = status == 1 ? "用户已启用" : "用户已禁用";
        return success ? Result.success(msg) : Result.error("操作失败");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/users/{id}/reset-password")
    public Result<String> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> data) {
        String newPassword = data.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            newPassword = "123456"; // 默认密码
        }
        
        boolean success = userService.resetPassword(id, newPassword);
        return success ? Result.success("密码已重置") : Result.error("重置失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        // 不允许删除自己
        if (id.equals(StpUtil.getLoginIdAsLong())) {
            return Result.error("不能删除自己");
        }
        
        // 不允许删除管理员
        User user = userService.getById(id);
        if (user != null && "admin".equals(user.getRole())) {
            return Result.error("不能删除管理员账号");
        }
        
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ==================== 日志查询接口 ====================

    /**
     * 获取登录日志列表（分页）
     */
    @GetMapping("/login-logs")
    public Result<PageResult<UserLoginLog>> getLoginLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer loginStatus) {
        
        Page<UserLoginLog> logPage = new Page<>(page, size);
        QueryWrapper<UserLoginLog> wrapper = new QueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        if (loginStatus != null) {
            wrapper.eq("login_status", loginStatus);
        }
        
        wrapper.orderByDesc("login_time");
        loginLogMapper.selectPage(logPage, wrapper);
        
        PageResult<UserLoginLog> pageResult = new PageResult<>(
            logPage.getRecords(),
            logPage.getTotal(),
            logPage.getCurrent(),
            logPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 获取操作日志列表（分页）
     */
    @GetMapping("/operation-logs")
    public Result<PageResult<UserOperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType) {
        
        Page<UserOperationLog> logPage = new Page<>(page, size);
        QueryWrapper<UserOperationLog> wrapper = new QueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq("operation_type", operationType);
        }
        
        wrapper.orderByDesc("operation_time");
        operationLogMapper.selectPage(logPage, wrapper);
        
        PageResult<UserOperationLog> pageResult = new PageResult<>(
            logPage.getRecords(),
            logPage.getTotal(),
            logPage.getCurrent(),
            logPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    // ==================== 用户题库统计接口 ====================

    /**
     * 获取指定用户的题库统计
     */
    @GetMapping("/users/{id}/question-stats")
    public Result<Map<String, Object>> getUserQuestionStats(@PathVariable Long id) {
        Map<String, Object> stats = new HashMap<>();

        // 用户的题目数量
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_id", id);
        long questionCount = questionService.count(wrapper);
        stats.put("questionCount", questionCount);

        return Result.success(stats);
    }

    // ==================== 系统健康接口 ====================

    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // 系统基本信息
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // JVM 信息
        Runtime runtime = Runtime.getRuntime();
        health.put("jvmMaxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        health.put("jvmTotalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        health.put("jvmFreeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        health.put("jvmUsedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        health.put("availableProcessors", runtime.availableProcessors());
        
        // 数据库连接检查
        try {
            long userCount = userService.count();
            health.put("dbStatus", "OK");
            health.put("dbUserCount", userCount);
        } catch (Exception e) {
            health.put("dbStatus", "ERROR");
            health.put("dbError", e.getMessage());
        }
        
        return Result.success(health);
    }

    /**
     * 获取题库总览
     */
    @GetMapping("/question-overview")
    public Result<Map<String, Object>> getQuestionOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 总题目数
        long totalQuestions = questionService.count();
        overview.put("totalQuestions", totalQuestions);
        
        // 公共题库数量（owner_id IS NULL）
        QueryWrapper<Question> publicWrapper = new QueryWrapper<>();
        publicWrapper.isNull("owner_id");
        long publicQuestions = questionService.count(publicWrapper);
        overview.put("publicQuestions", publicQuestions);
        
        // 用户私有题目数量
        long privateQuestions = totalQuestions - publicQuestions;
        overview.put("privateQuestions", privateQuestions);
        
        // 按题型统计
        QueryWrapper<Question> singleWrapper = new QueryWrapper<>();
        singleWrapper.eq("type", "single-choice");
        overview.put("singleChoiceCount", questionService.count(singleWrapper));
        
        QueryWrapper<Question> multiWrapper = new QueryWrapper<>();
        multiWrapper.eq("type", "multiple-choice");
        overview.put("multipleChoiceCount", questionService.count(multiWrapper));
        
        QueryWrapper<Question> judgeWrapper = new QueryWrapper<>();
        judgeWrapper.eq("type", "judge");
        overview.put("judgeCount", questionService.count(judgeWrapper));
        
        return Result.success(overview);
    }

    /**
     * 获取用户活跃度统计（最近7天）
     */
    @GetMapping("/user-activity")
    public Result<Map<String, Object>> getUserActivity() {
        Map<String, Object> activity = new HashMap<>();
        
        // 总用户数
        activity.put("totalUsers", userService.count());
        
        // 正常状态用户数
        QueryWrapper<User> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("status", 1);
        activity.put("activeUsers", userService.count(activeWrapper));
        
        // 禁用用户数
        QueryWrapper<User> disabledWrapper = new QueryWrapper<>();
        disabledWrapper.eq("status", 0);
        activity.put("disabledUsers", userService.count(disabledWrapper));
        
        // 今日新注册用户数
        QueryWrapper<User> todayWrapper = new QueryWrapper<>();
        todayWrapper.apply("DATE(create_time) = CURDATE()");
        activity.put("todayNewUsers", userService.count(todayWrapper));
        
        // 今日登录次数
        Integer todayLogins = loginLogMapper.countTodayLogin();
        activity.put("todayLogins", todayLogins != null ? todayLogins : 0);
        
        // 今日活跃用户数
        Integer todayActive = loginLogMapper.countTodayActiveUsers();
        activity.put("todayActiveUsers", todayActive != null ? todayActive : 0);
        
        return Result.success(activity);
    }
}

