package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.UserVO;
import com.exam.entity.User;

/**
 * 用户 Service 接口
 * 
 * @author Exam System
 * @since 2025-12-27
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册成功的用户信息
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     * 
     * @param loginDTO 登录信息
     * @param ip 登录IP
     * @param location 登录地点
     * @param userAgent User-Agent
     * @return Token
     */
    String login(LoginDTO loginDTO, String ip, String location, String userAgent);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    User getByUsername(String username);

    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUserInfo(User user);

    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置用户密码（管理员操作）
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 修改用户状态（管理员操作）
     * 
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long userId, Integer status);

    /**
     * 分页查询用户列表（管理员操作）
     * 
     * @param page 当前页
     * @param size 每页条数
     * @param username 用户名（可选）
     * @param role 角色（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<User> getUserPage(Long page, Long size, String username, String role, Integer status);
}
