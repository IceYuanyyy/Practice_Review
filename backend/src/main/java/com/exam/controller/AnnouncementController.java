package com.exam.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.Announcement;
import com.exam.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统公告 Controller
 * 
 * @author Exam System
 * @since 2026-01-08
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // ==================== 用户端接口（仅查看已发布） ====================

    /**
     * 获取公告列表（用户视角，仅已发布）
     */
    @GetMapping("/announcements")
    @SaCheckLogin
    public Result<PageResult<Announcement>> getAnnouncementsForUser(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        IPage<Announcement> pageResult = announcementService.pageForUser(page, size);
        
        return Result.success(new PageResult<>(
            pageResult.getRecords(),
            pageResult.getTotal(),
            pageResult.getCurrent(),
            pageResult.getSize()
        ));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/announcements/{id}")
    @SaCheckLogin
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        // 用户只能查看已发布的公告
        if (announcement.getStatus() != 1 && !StpUtil.hasRole("admin")) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取公告列表（管理员视角，可查看所有状态）
     */
    @GetMapping("/admin/announcements")
    @SaCheckRole("admin")
    public Result<PageResult<Announcement>> getAnnouncementsForAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        
        IPage<Announcement> pageResult = announcementService.pageForAdmin(page, size, status);
        
        return Result.success(new PageResult<>(
            pageResult.getRecords(),
            pageResult.getTotal(),
            pageResult.getCurrent(),
            pageResult.getSize()
        ));
    }

    /**
     * 创建公告
     */
    @PostMapping("/admin/announcements")
    @SaCheckRole("admin")
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        // 设置发布者ID
        announcement.setUserId(StpUtil.getLoginIdAsLong());
        
        // 保存
        boolean success = announcementService.save(announcement);
        if (success) {
            log.info("管理员 {} 创建了公告: {}", StpUtil.getLoginIdAsLong(), announcement.getTitle());
            return Result.success(announcement);
        }
        return Result.error("创建失败");
    }

    /**
     * 更新公告
     */
    @PutMapping("/admin/announcements/{id}")
    @SaCheckRole("admin")
    public Result<String> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        Announcement existing = announcementService.getById(id);
        if (existing == null) {
            return Result.error("公告不存在");
        }
        
        announcement.setId(id);
        // 保留原有的发布者ID
        announcement.setUserId(existing.getUserId());
        
        boolean success = announcementService.updateById(announcement);
        if (success) {
            log.info("管理员 {} 更新了公告: {}", StpUtil.getLoginIdAsLong(), id);
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/admin/announcements/{id}")
    @SaCheckRole("admin")
    public Result<String> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.removeById(id);
        if (success) {
            log.info("管理员 {} 删除了公告: {}", StpUtil.getLoginIdAsLong(), id);
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 发布公告
     */
    @PatchMapping("/admin/announcements/{id}/publish")
    @SaCheckRole("admin")
    public Result<String> publishAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.publish(id);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }

    /**
     * 撤回公告
     */
    @PatchMapping("/admin/announcements/{id}/revoke")
    @SaCheckRole("admin")
    public Result<String> revokeAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.revoke(id);
        return success ? Result.success("撤回成功") : Result.error("撤回失败");
    }

    /**
     * 设置/取消置顶
     */
    @PatchMapping("/admin/announcements/{id}/pin")
    @SaCheckRole("admin")
    public Result<String> togglePin(@PathVariable Long id, @RequestBody Map<String, Boolean> data) {
        Boolean pinned = data.get("pinned");
        if (pinned == null) {
            return Result.error("参数错误");
        }
        boolean success = announcementService.togglePinned(id, pinned);
        String msg = pinned ? "置顶成功" : "取消置顶成功";
        return success ? Result.success(msg) : Result.error("操作失败");
    }

    // ==================== 用户端已读管理 ====================

    @Autowired
    private com.exam.service.AnnouncementReadService announcementReadService;

    /**
     * 获取当前用户未读公告列表
     */
    @GetMapping("/announcements/unread")
    @SaCheckLogin
    public Result<List<Announcement>> getUnreadAnnouncements() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Announcement> unread = announcementReadService.getUnreadAnnouncements(userId);
        return Result.success(unread);
    }

    /**
     * 标记公告为已读
     */
    @PostMapping("/announcements/{id}/read")
    @SaCheckLogin
    public Result<String> markAsRead(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean success = announcementReadService.markAsRead(userId, id);
        return success ? Result.success("已标记为已读") : Result.error("标记失败");
    }
}
