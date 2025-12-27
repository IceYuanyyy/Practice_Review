package com.exam.config;

import cn.dev33.satoken.stp.StpInterface;
import com.exam.entity.User;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限接口实现
 * 用于 @SaCheckRole 等注解的权限校验
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserService userService;

    /**
     * 获取用户角色列表
     * 
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return 角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.parseLong(loginId.toString()));
        if (user == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(user.getRole());
    }

    /**
     * 获取用户权限列表（暂不使用细粒度权限）
     * 
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return 权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
