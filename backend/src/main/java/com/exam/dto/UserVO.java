package com.exam.dto;

import com.exam.entity.User;
import lombok.Data;

/**
 * 用户信息响应 DTO（不包含敏感信息）
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 邮箱是否已验证
     */
    private Boolean isEmailVerified;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 从 User 实体转换
     * 
     * @param user 用户实体
     * @return UserVO
     */
    public static UserVO fromUser(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setIsEmailVerified(user.getIsEmailVerified());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
