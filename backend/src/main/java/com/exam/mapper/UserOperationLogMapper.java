package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.UserOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户操作日志 Mapper 接口
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Mapper
public interface UserOperationLogMapper extends BaseMapper<UserOperationLog> {
}
