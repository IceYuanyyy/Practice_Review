package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Announcement;
import com.exam.entity.AnnouncementRead;
import com.exam.mapper.AnnouncementReadMapper;
import com.exam.mapper.AnnouncementMapper;
import com.exam.service.AnnouncementReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告已读记录 Service 实现类
 * 
 * @author Exam System
 * @since 2026-01-11
 */
@Slf4j
@Service
public class AnnouncementReadServiceImpl extends ServiceImpl<AnnouncementReadMapper, AnnouncementRead> 
        implements AnnouncementReadService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public boolean markAsRead(Long userId, Long announcementId) {
        // 检查是否已标记
        if (hasRead(userId, announcementId)) {
            return true;
        }
        
        AnnouncementRead record = new AnnouncementRead();
        record.setUserId(userId);
        record.setAnnouncementId(announcementId);
        record.setCreateTime(LocalDateTime.now());
        
        boolean success = this.save(record);
        if (success) {
            log.debug("用户 {} 已读公告 {}", userId, announcementId);
        }
        return success;
    }

    @Override
    public List<Announcement> getUnreadAnnouncements(Long userId) {
        // 获取用户已读的公告ID列表
        List<Long> readIds = baseMapper.selectReadAnnouncementIds(userId);
        
        // 查询所有已发布的公告，排除已读的
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1); // 仅已发布状态
        if (readIds != null && !readIds.isEmpty()) {
            wrapper.notIn(Announcement::getId, readIds);
        }
        wrapper.orderByDesc(Announcement::getIsPinned)
               .orderByDesc(Announcement::getCreateTime);
        
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public boolean hasRead(Long userId, Long announcementId) {
        return baseMapper.countByUserAndAnnouncement(userId, announcementId) > 0;
    }
}
