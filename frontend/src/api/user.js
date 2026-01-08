import request from './request'

/**
 * 获取所有用户列表（仅管理员）
 * 非管理员调用会返回403，使用静默模式不显示错误提示
 */
export function getAllUsers() {
    return request({
        url: '/user/list',
        method: 'get',
        silent: true  // 静默模式，不显示错误提示
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
