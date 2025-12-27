import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加 Token
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取用户状态
    const userState = localStorage.getItem('exam-user')
    if (userState) {
      try {
        const { token } = JSON.parse(userState)
        if (token) {
          // 在请求头中携带 Token
          config.headers['Authorization'] = token
        }
      } catch (e) {
        console.error('解析用户状态失败', e)
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 如果返回的状态码不是 200，则认为是错误
    if (res.code !== 200) {
      // 特殊处理 Token 失效的情况
      if (res.code === 401) {
        window.$message?.warning(res.message || '登录已过期，请重新登录')
        localStorage.removeItem('exam-user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        return Promise.reject(new Error(res.message || '登录过期'))
      }

      // 显示错误提示
      window.$message?.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || error.message

      switch (status) {
        case 401:
          // 未登录或 Token 过期
          window.$message?.warning(message || '登录已过期，请重新登录')
          // 清除本地存储的用户信息
          localStorage.removeItem('exam-user')
          // 跳转到登录页
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          break
        case 403:
          // 权限不足
          window.$message?.error(message || '权限不足')
          break
        case 500:
          // 服务器错误
          window.$message?.error(message || '服务器错误，请稍后重试')
          break
        default:
          window.$message?.error(message || '网络错误')
      }
    } else {
      window.$message?.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
