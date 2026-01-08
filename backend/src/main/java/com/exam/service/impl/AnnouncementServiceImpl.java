package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Announcement;
import com.exam.entity.User;
import com.exam.mapper.AnnouncementMapper;
import com.exam.mapper.UserMapper;
import com.exam.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统公告 Service 实现类
 * 
 * @author Exam System
 * @since 2026-01-08
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<Announcement> pageForAdmin(int page, int size, Integer status) {
        Page<Announcement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        
        // 按置顶优先、创建时间倒序
        wrapper.orderByDesc(Announcement::getIsPinned)
               .orderByDesc(Announcement::getCreateTime);
        
        IPage<Announcement> result = this.page(pageParam, wrapper);
        
        // 填充发布者用户名
        result.getRecords().forEach(this::fillPublisherName);
        
        return result;
    }

    @Override
    public IPage<Announcement> pageForUser(int page, int size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        
        // 只查看已发布的公告（status=1）
        wrapper.eq(Announcement::getStatus, 1);
        
        // 按置顶优先、创建时间倒序
        wrapper.orderByDesc(Announcement::getIsPinned)
               .orderByDesc(Announcement::getCreateTime);
        
        IPage<Announcement> result = this.page(pageParam, wrapper);
        
        // 填充发布者用户名
        result.getRecords().forEach(this::fillPublisherName);
        
        return result;
    }

    @Override
    @Transactional
    public boolean publish(Long id) {
        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, id)
               .set(Announcement::getStatus, 1);
        boolean success = this.update(wrapper);
        if (success) {
            log.info("公告 {} 已发布", id);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean revoke(Long id) {
        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, id)
               .set(Announcement::getStatus, 2);
        boolean success = this.update(wrapper);
        if (success) {
            log.info("公告 {} 已撤回", id);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean togglePinned(Long id, boolean pinned) {
        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, id)
               .set(Announcement::getIsPinned, pinned);
        boolean success = this.update(wrapper);
        if (success) {
            log.info("公告 {} 置顶状态: {}", id, pinned);
        }
        return success;
    }

    /**
     * 填充发布者用户名（非数据库字段）
     */
    private void fillPublisherName(Announcement announcement) {
        if (announcement.getUserId() != null) {
            User user = userMapper.selectById(announcement.getUserId());
            if (user != null) {
                announcement.setPublisherName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
    }
}
