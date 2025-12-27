package com.exam.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.UserVO;
import com.exam.entity.User;
import com.exam.entity.UserLoginLog;
import com.exam.mapper.UserLoginLogMapper;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户 Service 实现类
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserLoginLogMapper loginLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = baseMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 使用 BCrypt 加密密码
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname()) 
            ? registerDTO.getNickname() 
            : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setRole("user");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        baseMapper.insert(user);

        log.info("用户注册成功: username={}", user.getUsername());
        return UserVO.fromUser(user);
    }

    @Override
    public String login(LoginDTO loginDTO, String ip, String userAgent) {
        // 查询用户
        User user = baseMapper.selectByUsername(loginDTO.getUsername());
        
        // 创建登录日志
        UserLoginLog loginLog = new UserLoginLog();
        loginLog.setUsername(loginDTO.getUsername());
        loginLog.setLoginIp(ip);
        loginLog.setUserAgent(userAgent);
        loginLog.setLoginTime(LocalDateTime.now());
        
        // 解析 User-Agent 获取浏览器和操作系统信息
        if (userAgent != null) {
            loginLog.setBrowser(parseBrowser(userAgent));
            loginLog.setOs(parseOs(userAgent));
        }

        // 用户不存在
        if (user == null) {
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("用户不存在");
            loginLogMapper.insert(loginLog);
            throw new RuntimeException("用户名或密码错误");
        }

        // 用户被禁用
        if (user.getStatus() == 0) {
            loginLog.setUserId(user.getId());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("账号已被禁用");
            loginLogMapper.insert(loginLog);
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            loginLog.setUserId(user.getId());
            loginLog.setLoginStatus(0);
            loginLog.setFailReason("密码错误");
            loginLogMapper.insert(loginLog);
            throw new RuntimeException("用户名或密码错误");
        }

        // 登录成功，记录日志
        loginLog.setUserId(user.getId());
        loginLog.setLoginStatus(1);
        loginLogMapper.insert(loginLog);

        // 使用 Sa-Token 登录
        StpUtil.login(user.getId());
        
        // 如果选择了"记住我"，延长有效期
        if (loginDTO.getRememberMe() != null && loginDTO.getRememberMe()) {
            StpUtil.renewTimeout(60 * 60 * 24 * 30); // 30天
        }

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return StpUtil.getTokenValue();
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            log.info("用户登出: userId={}", StpUtil.getLoginId());
            StpUtil.logout();
        }
    }

    @Override
    public UserVO getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        User user = baseMapper.selectById(userId);
        return UserVO.fromUser(user);
    }

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 只允许修改自己的信息，除非是管理员
        User currentUser = baseMapper.selectById(userId);
        if (!userId.equals(user.getId()) && !"admin".equals(currentUser.getRole())) {
            throw new RuntimeException("没有权限修改他人信息");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setNickname(user.getNickname());
        updateUser.setEmail(user.getEmail());
        updateUser.setAvatarUrl(user.getAvatarUrl());
        updateUser.setUpdateTime(LocalDateTime.now());

        return baseMapper.updateById(updateUser) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(BCrypt.hashpw(newPassword));
        updateUser.setUpdateTime(LocalDateTime.now());

        return baseMapper.updateById(updateUser) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(BCrypt.hashpw(newPassword));
        updateUser.setUpdateTime(LocalDateTime.now());

        log.info("管理员重置用户密码: userId={}", userId);
        return baseMapper.updateById(updateUser) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long userId, Integer status) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 不能禁用管理员账号
        if ("admin".equals(user.getRole()) && status == 0) {
            throw new RuntimeException("不能禁用管理员账号");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);
        updateUser.setUpdateTime(LocalDateTime.now());

        log.info("修改用户状态: userId={}, status={}", userId, status);
        return baseMapper.updateById(updateUser) > 0;
    }

    @Override
    public Page<User> getUserPage(Long page, Long size, String username, String role, Integer status) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (StringUtils.hasText(username)) {
            wrapper.like("username", username);
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq("role", role);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return baseMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 解析浏览器类型
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("edg")) return "Edge";
        if (userAgent.contains("chrome")) return "Chrome";
        if (userAgent.contains("firefox")) return "Firefox";
        if (userAgent.contains("safari")) return "Safari";
        if (userAgent.contains("opera")) return "Opera";
        if (userAgent.contains("msie") || userAgent.contains("trident")) return "IE";
        return "Unknown";
    }

    /**
     * 解析操作系统
     */
    private String parseOs(String userAgent) {
        if (userAgent == null) return "Unknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("windows")) return "Windows";
        if (userAgent.contains("mac")) return "macOS";
        if (userAgent.contains("linux")) return "Linux";
        if (userAgent.contains("android")) return "Android";
        if (userAgent.contains("iphone") || userAgent.contains("ipad")) return "iOS";
        return "Unknown";
    }
}
