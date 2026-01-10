package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.AnnouncementRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公告已读记录 Mapper
 * 
 * @author Exam System
 * @since 2026-01-11
 */
@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {

    /**
     * 获取用户已读的公告ID列表
     */
    @Select("SELECT announcement_id FROM announcement_read WHERE user_id = #{userId}")
    List<Long> selectReadAnnouncementIds(@Param("userId") Long userId);

    /**
     * 检查用户是否已读某公告
     */
    @Select("SELECT COUNT(1) FROM announcement_read WHERE user_id = #{userId} AND announcement_id = #{announcementId}")
    int countByUserAndAnnouncement(@Param("userId") Long userId, @Param("announcementId") Long announcementId);
}
