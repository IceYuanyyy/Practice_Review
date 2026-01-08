import request from './request'

// ==================== 用户端接口 ====================

/**
 * 获取公告列表（用户视角，仅已发布）
 */
export function getAnnouncements(params) {
    return request({
        url: '/announcements',
        method: 'get',
        params
    })
}

/**
 * 获取公告详情
 */
export function getAnnouncementById(id) {
    return request({
        url: `/announcements/${id}`,
        method: 'get'
    })
}

// ==================== 管理员接口 ====================

/**
 * 获取公告列表（管理员视角）
 */
export function getAdminAnnouncements(params) {
    return request({
        url: '/admin/announcements',
        method: 'get',
        params
    })
}

/**
 * 创建公告
 */
export function createAnnouncement(data) {
    return request({
        url: '/admin/announcements',
        method: 'post',
        data
    })
}

/**
 * 更新公告
 */
export function updateAnnouncement(id, data) {
    return request({
        url: `/admin/announcements/${id}`,
        method: 'put',
        data
    })
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id) {
    return request({
        url: `/admin/announcements/${id}`,
        method: 'delete'
    })
}

/**
 * 发布公告
 */
export function publishAnnouncement(id) {
    return request({
        url: `/admin/announcements/${id}/publish`,
        method: 'patch'
    })
}

/**
 * 撤回公告
 */
export function revokeAnnouncement(id) {
    return request({
        url: `/admin/announcements/${id}/revoke`,
        method: 'patch'
    })
}

/**
 * 设置/取消置顶
 */
export function toggleAnnouncementPin(id, pinned) {
    return request({
        url: `/admin/announcements/${id}/pin`,
        method: 'patch',
        data: { pinned }
    })
}
