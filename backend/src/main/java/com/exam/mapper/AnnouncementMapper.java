package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告 Mapper 接口
 * 
 * @author Exam System
 * @since 2026-01-08
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
