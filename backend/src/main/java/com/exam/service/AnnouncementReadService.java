package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Announcement;
import com.exam.entity.AnnouncementRead;

import java.util.List;

/**
 * 公告已读记录 Service
 * 
 * @author Exam System
 * @since 2026-01-11
 */
public interface AnnouncementReadService extends IService<AnnouncementRead> {

    /**
     * 标记公告为已读
     */
    boolean markAsRead(Long userId, Long announcementId);

    /**
     * 获取用户未读的公告列表
     */
    List<Announcement> getUnreadAnnouncements(Long userId);

    /**
     * 检查用户是否已读某公告
     */
    boolean hasRead(Long userId, Long announcementId);
}
