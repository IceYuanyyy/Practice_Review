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
 * @param {Object} data - { username, password, confirmPassword, nickname, email, verificationCode }
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 发送注册验证码
 * @param {string} email - 邮箱地址
 */
export function sendVerificationCode(email) {
    return request({
        url: '/auth/send-code',
        method: 'post',
        params: { email }
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
 * 绑定/验证邮箱
 * @param {Object} data - { email, code }
 */
export function bindEmail(data) {
    return request({
        url: '/auth/bind-email',
        method: 'post',
        data
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
