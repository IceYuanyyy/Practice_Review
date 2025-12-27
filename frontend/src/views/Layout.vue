<template>
  <n-layout has-sider class="main-layout" content-style="min-height: 100vh;">
    <!-- Desktop Sidebar -->
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="80"
      :width="260"
      :collapsed="collapsed"
      show-trigger="bar"
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="glass-sider desktop-sider"
    >
      <div class="logo-container" :class="{ collapsed: collapsed }">
        <div class="logo-icon-bg">
          <n-icon :component="SchoolOutline" size="28" color="#fff" />
        </div>
        <transition name="fade">
          <div v-if="!collapsed" class="brand-text">
            <span class="brand-title">Exam Master</span>
            <span class="brand-subtitle">Pro Edition</span>
          </div>
        </transition>
      </div>
      
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="80"
        :collapsed-icon-size="24"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
        class="custom-menu"
      />
    </n-layout-sider>

    <!-- Mobile Drawer Sidebar -->
    <n-drawer v-model:show="showMobileMenu" :width="260" placement="left">
      <n-drawer-content body-content-style="padding: 0;">
         <div class="logo-container">
          <div class="logo-icon-bg">
            <n-icon :component="SchoolOutline" size="28" color="#fff" />
          </div>
          <div class="brand-text">
            <span class="brand-title">Exam Master</span>
            <span class="brand-subtitle">Pro Edition</span>
          </div>
        </div>
        <n-menu
          :options="menuOptions"
          :value="activeKey"
          @update:value="(key) => { handleMenuSelect(key); showMobileMenu = false; }"
        />
      </n-drawer-content>
    </n-drawer>

    <n-layout class="bg-transparent">
      <!-- Floating Glass Header -->
      <n-layout-header class="glass-header">
        <div class="header-content">
          <n-space align="center" :size="20">
            <!-- Mobile Toggle -->
            <n-button text class="mobile-toggle" @click="showMobileMenu = true">
              <n-icon size="24" :component="MenuOutline" />
            </n-button>

            <!-- Breadcrumb-like Title Area -->
            <div class="page-header">
              <h2 class="page-title">{{ currentRouteName }}</h2>
              <transition name="scale">
                <n-tag 
                  v-if="currentRouteName === '在线练习'" 
                  type="success" 
                  size="small" 
                  round 
                  class="focus-tag"
                >
                  <template #icon><n-icon :component="CheckmarkCircleOutline" /></template>
                  Focus Mode
                </n-tag>
              </transition>
            </div>
          </n-space>

          <n-space align="center" :size="24">
            <!-- Quick Actions -->
            <n-button circle secondary type="primary" class="action-btn desktop-only">
              <template #icon><n-icon :component="SearchOutline" /></template>
            </n-button>
            <n-button circle secondary type="warning" class="action-btn">
              <template #icon><n-icon :component="NotificationsOutline" /></template>
            </n-button>
            
            <div class="divider desktop-only"></div>

            <n-button 
              v-if="userStore.isAdmin" 
              text 
              type="primary"
              class="admin-link desktop-only"
              @click="router.push('/admin/dashboard')"
            >
              <template #icon><n-icon :component="SettingsOutline" /></template>
              Admin
            </n-button>
            
            <!-- User Profile Pill -->
            <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect" trigger="click">
              <div class="user-pill">
                <n-avatar 
                  round 
                  size="medium" 
                  :style="{ backgroundColor: avatarColor, color: '#fff' }"
                  class="user-avatar"
                >
                  {{ userStore.nickname?.charAt(0) || 'U' }}
                </n-avatar>
                <div class="user-info desktop-only">
                  <span class="username">{{ userStore.nickname || 'User' }}</span>
                  <span class="role-badge">{{ userStore.isAdmin ? 'Admin' : 'Student' }}</span>
                </div>
                <n-icon :component="ChevronDownOutline" size="14" class="dropdown-icon desktop-only" />
              </div>
            </n-dropdown>
          </n-space>
        </div>
      </n-layout-header>

      <n-layout-content
        :content-style="contentStyle"
        :native-scrollbar="false"
        class="main-content-wrapper"
        :class="{ 'no-padding': isFullWidthRoute }"
      >
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, h, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { 
  NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NDrawer, NDrawerContent,
  NMenu, NSpace, NText, NTag, NIcon, NAvatar, NDropdown, NButton 
} from 'naive-ui'
import {
  HomeOutline,
  ListOutline,
  SchoolOutline,
  BookmarkOutline,
  StatsChartOutline,
  CheckmarkCircleOutline,
  SwapHorizontalOutline,
  PersonOutline,
  LogOutOutline,
  SettingsOutline,
  ChevronDownOutline,
  MenuOutline,
  SearchOutline,
  NotificationsOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()
const collapsed = ref(false)
const showMobileMenu = ref(false)

const activeKey = computed(() => route.path)

// 头像颜色
const avatarColor = computed(() => {
  const colors = ['#10b981', '#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b']
  const name = userStore.username || ''
  const index = name.charCodeAt(0) % colors.length || 0
  return colors[index]
})

// 页面标题
const currentRouteName = computed(() => {
  const map = {
    '/home': '复习概览',
    '/question-manage': '题目管理',
    '/question-converter': '题库转换',
    '/practice': '在线练习',
    '/wrong-book': '错题回顾',
    '/statistics': '学习统计',
    '/profile': '个人中心'
  }
  return map[route.path] || '期末复习系统'
})

// Whether current route needs full width (Practice or Home)
const isFullWidthRoute = computed(() => ['/practice', '/home'].includes(route.path))

// Dynamic content style
const contentStyle = computed(() => {
  return {
    padding: isFullWidthRoute.value ? '0' : '24px',
    minHeight: 'calc(100vh - 80px)'
  }
})

const renderIcon = (icon) => () => h(NIcon, null, { default: () => h(icon) })

// 菜单配置
const menuOptions = [
  { label: '首页概览', key: '/home', icon: renderIcon(HomeOutline) },
  { label: '题目管理', key: '/question-manage', icon: renderIcon(ListOutline) },
  { label: '题库转换', key: '/question-converter', icon: renderIcon(SwapHorizontalOutline) },
  { label: '开始练习', key: '/practice', icon: renderIcon(SchoolOutline) },
  { label: '错题本', key: '/wrong-book', icon: renderIcon(BookmarkOutline) },
  { label: '统计分析', key: '/statistics', icon: renderIcon(StatsChartOutline) }
]

const userMenuOptions = [
  { label: '个人中心', key: 'profile', icon: renderIcon(PersonOutline) },
  { type: 'divider' },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) }
]

const handleMenuSelect = (key) => router.push(key)

async function handleUserMenuSelect(key) {
  if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'logout') {
    await userStore.logout()
    message.success('已退出登录')
  }
}
</script>

<style scoped>
/* 
  === TECH ANTI-DESIGN THEME === 
  Industrial / Brutalist / Cyber-grunge 
*/
.main-layout {
  --neon-yellow: #F5E400;
  --neon-cyan: #00E5FF;
  --neon-magenta: #FF3EA5;
  --void-black: #050505;
  --off-white: #FFFDF7;
  --grid-line: #e0e0e0;
  
  /* Dot Matrix Background (Clean Industrial) */
  background-color: var(--off-white);
  background-image: radial-gradient(rgba(0,0,0,0.1) 2px, transparent 2px);
  background-size: 24px 24px;
  font-family: 'Courier New', Courier, monospace;
}

.bg-transparent {
  background: transparent;
}

/* === SIDEBAR (CONTROL PANEL) === */
.desktop-sider {
  background: var(--off-white);
  border-right: 4px solid var(--void-black) !important;
  z-index: 50;
}

/* Logo Area */
.logo-container {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: var(--neon-yellow);
  border-bottom: 4px solid var(--void-black);
  margin-bottom: 0px; /* Flush */
  position: relative;
  overflow: hidden;
}

/* Industrial Stripes on Logo Bg */
.logo-container::after {
  content: '';
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: repeating-linear-gradient(
    90deg,
    transparent,
    transparent 4px,
    rgba(0,0,0,0.1) 4px,
    rgba(0,0,0,0.1) 8px
  );
  pointer-events: none;
}

.logo-icon-bg {
  width: 40px;
  height: 40px;
  background: var(--void-black);
  color: var(--neon-cyan);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.2);
  z-index: 2;
}

.brand-text {
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.brand-title {
  font-size: 18px;
  font-weight: 900;
  color: var(--void-black);
  text-transform: uppercase;
  letter-spacing: -1px;
}

.brand-subtitle {
  font-size: 10px;
  font-weight: 700;
  color: var(--void-black);
  background: white;
  padding: 0 4px;
  text-align: center;
  border: 1px solid black;
}

/* Custom Menu Styling */
:deep(.n-menu .n-menu-item) {
  margin: 12px 12px;
}

:deep(.n-menu .n-menu-item-content) {
  border: 2px solid transparent;
  border-radius: 0 !important; /* Brutalist square */
  transition: all 0.1s;
  padding-left: 16px !important;
}

:deep(.n-menu .n-menu-item-content:hover) {
  border-color: var(--void-black);
  background: white;
  transform: translate(-2px, -2px);
  box-shadow: 4px 4px 0 var(--void-black);
}

:deep(.n-menu .n-menu-item-content--selected) {
  background: var(--neon-cyan) !important;
  border: 2px solid var(--void-black) !important;
  box-shadow: 4px 4px 0 var(--void-black);
  transform: translate(-2px, -2px);
}

:deep(.n-menu .n-menu-item-content--selected .n-menu-item-content-header) {
  font-weight: 900;
  color: var(--void-black) !important;
}

:deep(.n-menu .n-menu-item-content .n-menu-item-content__icon) {
  color: var(--void-black) !important;
}

/* === HEADER (SYSTEM STATUS BAR) === */
.glass-header {
  height: 80px;
  background: white;
  border-bottom: 4px solid var(--void-black);
  z-index: 40;
  position: sticky;
  top: 0;
  padding: 0 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 900;
  text-transform: uppercase;
  background: var(--void-black);
  color: var(--neon-yellow);
  padding: 4px 12px;
  transform: skew(-10deg);
  display: inline-block;
  box-shadow: 3px 3px 0 rgba(0,0,0,0.2);
}

.focus-tag {
  background: var(--neon-magenta) !important;
  color: white !important;
  border: 2px solid var(--void-black) !important;
  border-radius: 0 !important;
  font-weight: bold;
}

/* Action Buttons */
.action-btn {
  border: 2px solid var(--void-black) !important;
  border-radius: 0 !important;
  color: var(--void-black) !important;
  transition: all 0.1s !important;
}
.action-btn:hover {
  background: var(--neon-yellow) !important;
  transform: translate(-2px, -2px);
  box-shadow: 3px 3px 0 var(--void-black);
}

.divider {
  width: 2px;
  height: 24px;
  background: var(--void-black);
  transform: rotate(15deg);
}

.admin-link {
  font-family: monospace;
  font-weight: bold;
}
.admin-link:hover {
  text-decoration: underline;
  text-decoration-style: wavy;
  text-decoration-color: var(--neon-magenta);
}

/* User Pill */
.user-pill {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px 4px 4px;
  background: white;
  border: 2px solid var(--void-black);
  cursor: pointer;
  transition: all 0.2s ease;
}

.user-pill:hover {
  background: var(--neon-cyan);
  box-shadow: 4px 4px 0 var(--void-black);
  transform: translate(-2px, -2px);
}

.user-avatar {
  border: 2px solid var(--void-black);
  border-radius: 0 !important; /* Square Avatar */
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.username {
  font-size: 14px;
  font-weight: 800;
  text-transform: uppercase;
  color: var(--void-black);
}

.role-badge {
  font-size: 10px;
  font-family: monospace;
  background: var(--void-black);
  color: white;
  padding: 0 4px;
  display: inline-block;
  margin-right: auto;
}

/* Mobile Responsive */
.mobile-toggle {
  display: none;
}

@media (max-width: 768px) {
  .desktop-sider {
    display: none;
  }
  
  .glass-header {
    padding: 0 16px;
  }
  
  .mobile-toggle {
    display: flex;
    border: 2px solid black !important;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .desktop-only {
    display: none !important;
  }
  
  .user-pill {
    padding: 2px;
    border: none;
    background: transparent;
  }
  .user-pill:hover {
    box-shadow: none;
    transform: none;
  }
  
  .main-content-wrapper:not(.no-padding) {
    padding: 16px !important;
  }
}

/* Transitions - Glitchy Fade */
.slide-fade-enter-active {
  transition: all 0.2s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.1s ease-in;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
  filter: grayscale(100%);
}
</style>
