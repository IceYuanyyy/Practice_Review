import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  // 注册页
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  // 主布局
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/question-manage',
        name: 'QuestionManage',
        component: () => import('@/views/QuestionManage.vue'),
        meta: { title: '题目管理' }
      },
      {
        path: '/question-converter',
        name: 'QuestionConverter',
        component: () => import('@/views/QuestionConverter.vue'),
        meta: { title: '题库转换' }
      },
      {
        path: '/practice',
        name: 'Practice',
        component: () => import('@/views/Practice.vue'),
        meta: { title: '开始练习' }
      },
      {
        path: '/wrong-book',
        name: 'WrongBook',
        component: () => import('@/views/WrongBook.vue'),
        meta: { title: '错题本' }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 管理员后台
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'login-logs',
        name: 'LoginLogs',
        component: () => import('@/views/admin/LoginLogs.vue'),
        meta: { title: '登录日志' }
      },
      {
        path: 'operation-logs',
        name: 'OperationLogs',
        component: () => import('@/views/admin/OperationLogs.vue'),
        meta: { title: '操作日志' }
      }
    ]
  },
  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 期末复习系统` : '期末复习系统'

  // 获取用户状态（不要在这里调用 useUserStore()，因为 pinia 可能还没初始化）
  const userState = localStorage.getItem('exam-user')
  let isLoggedIn = false
  let isAdmin = false

  if (userState) {
    try {
      const { token, user } = JSON.parse(userState)
      isLoggedIn = !!token
      isAdmin = user?.role === 'admin'
    } catch (e) {
      console.error('解析用户状态失败', e)
    }
  }

  // 访客页面（登录、注册）
  if (to.meta.guest) {
    // 已登录用户访问登录/注册页，重定向到首页
    if (isLoggedIn) {
      next('/home')
      return
    }
    next()
    return
  }

  // 需要登录的页面
  if (to.meta.requiresAuth || to.matched.some(record => record.meta.requiresAuth)) {
    if (!isLoggedIn) {
      // 未登录，重定向到登录页，并记录原目标页面
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 需要管理员权限
    if (to.meta.requiresAdmin || to.matched.some(record => record.meta.requiresAdmin)) {
      if (!isAdmin) {
        // 不是管理员，重定向到首页
        next('/home')
        return
      }
    }
  }

  next()
})

export default router
