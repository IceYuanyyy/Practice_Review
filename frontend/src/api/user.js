import request from './request'

/**
 * 获取所有用户列表（仅管理员）
 */
export function getAllUsers() {
    return request({
        url: '/user/list',
        method: 'get'
    })
}

/**
 * 获取当前用户信息
 */
export function getUserProfile() {
    return request({
        url: '/user/profile',
        method: 'get'
    })
}

/**
 * 更新用户信息
 */
export function updateUserProfile(data) {
    return request({
        url: '/user/profile',
        method: 'put',
        data
    })
}

/**
 * 修改密码
 */
export function changePassword(data) {
    return request({
        url: '/user/password',
        method: 'put',
        data
    })
}
