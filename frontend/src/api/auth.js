import request from './request'

/**
 * 用户登录
 * @param {Object} data - { username, password, rememberMe }
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

/**
 * 用户注册
 * @param {Object} data - { username, password, confirmPassword, nickname, email }
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 用户登出
 */
export function logout() {
    return request({
        url: '/auth/logout',
        method: 'post'
    })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
    return request({
        url: '/auth/user',
        method: 'get'
    })
}

/**
 * 检查登录状态
 */
export function checkLogin() {
    return request({
        url: '/auth/check',
        method: 'get'
    })
}
