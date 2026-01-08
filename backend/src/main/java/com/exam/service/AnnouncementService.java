package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Announcement;

/**
 * 系统公告 Service 接口
 * 
 * @author Exam System
 * @since 2026-01-08
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告（管理员视角，可查看所有状态）
     * 
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选（可选）
     * @return 公告分页数据
     */
    IPage<Announcement> pageForAdmin(int page, int size, Integer status);

    /**
     * 分页查询公告（用户视角，仅查看已发布）
     * 
     * @param page 页码
     * @param size 每页数量
     * @return 公告分页数据
     */
    IPage<Announcement> pageForUser(int page, int size);

    /**
     * 发布公告（状态改为1）
     * 
     * @param id 公告ID
     * @return 是否成功
     */
    boolean publish(Long id);

    /**
     * 撤回公告（状态改为2）
     * 
     * @param id 公告ID
     * @return 是否成功
     */
    boolean revoke(Long id);

    /**
     * 设置/取消置顶
     * 
     * @param id 公告ID
     * @param pinned 是否置顶
     * @return 是否成功
     */
    boolean togglePinned(Long id, boolean pinned);
}
