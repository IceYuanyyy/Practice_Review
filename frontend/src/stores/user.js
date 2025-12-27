import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import router from '@/router'

/**
 * 用户状态管理
 * 使用 Pinia 管理用户登录状态和信息
 */
export const useUserStore = defineStore('user', () => {
    // 状态
    const token = ref(null)
    const user = ref(null)

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => user.value?.role === 'admin')
    const username = computed(() => user.value?.username || '')
    const nickname = computed(() => user.value?.nickname || user.value?.username || '用户')

    /**
     * 用户登录
     * @param {Object} loginForm - 登录表单 { username, password, rememberMe }
     */
    async function login(loginForm) {
        const res = await loginApi(loginForm)
        token.value = res.data.token
        user.value = res.data.user
        return res
    }

    /**
     * 用户登出
     */
    async function logout() {
        try {
            await logoutApi()
        } catch (e) {
            console.error('登出失败', e)
        } finally {
            // 无论是否成功，都清除本地状态
            clearUser()
            router.push('/login')
        }
    }

    /**
     * 获取当前用户信息
     */
    async function fetchUser() {
        try {
            const res = await getCurrentUser()
            user.value = res.data
            return res.data
        } catch (e) {
            console.error('获取用户信息失败', e)
            clearUser()
            return null
        }
    }

    /**
     * 清除用户状态
     */
    function clearUser() {
        token.value = null
        user.value = null
    }

    /**
     * 初始化用户状态（从后端验证 token 是否有效）
     */
    async function initUser() {
        if (token.value) {
            await fetchUser()
        }
    }

    return {
        // 状态
        token,
        user,
        // 计算属性
        isLoggedIn,
        isAdmin,
        username,
        nickname,
        // 方法
        login,
        logout,
        fetchUser,
        clearUser,
        initUser
    }
}, {
    // 持久化配置
    persist: {
        key: 'exam-user',
        storage: localStorage,
        paths: ['token', 'user']
    }
})
