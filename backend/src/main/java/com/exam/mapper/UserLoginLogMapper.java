package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户登录日志 Mapper 接口
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {

    /**
     * 获取今日登录次数
     * 
     * @return 登录次数
     */
    @Select("SELECT COUNT(*) FROM user_login_log WHERE DATE(login_time) = CURDATE() AND login_status = 1")
    Integer countTodayLogin();

    /**
     * 获取今日活跃用户数
     * 
     * @return 活跃用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM user_login_log WHERE DATE(login_time) = CURDATE() AND login_status = 1 AND user_id IS NOT NULL")
    Integer countTodayActiveUsers();
}
