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

      <!-- Cyber Status Widget (Bottom Filler) -->
      <transition name="fade">
        <div v-if="!collapsed" class="status-widget">
          <div class="widget-header">
             <n-icon :component="HardwareChipOutline" />
             <span>SYSTEM_DIAGNOSTICS</span>
          </div>
          
          <div class="widget-body">
             <div class="stat-row">
               <span class="stat-label">CPU_LOAD</span>
               <div class="cyber-progress">
                 <div class="cyber-bar" :style="{ width: cpuLoad + '%' }"></div>
               </div>
               <span class="stat-val">{{ cpuLoad }}%</span>
             </div>
             
             <div class="stat-row">
               <span class="stat-label">SYNC_RATE</span>
               <div class="cyber-progress">
                 <div class="cyber-bar" :style="{ width: syncRate + '%', background: 'var(--neon-magenta)' }"></div>
               </div>
               <span class="stat-val">{{ syncRate }}%</span>
             </div>
          </div>

          <div class="widget-footer">
             <button class="cyber-btn" @click="triggerBoost">
               <n-icon :component="FlashOutline" :class="{ 'spinning': isBoosting }" />
               <span>{{ isBoosting ? 'OPTIMIZING...' : 'INJECT_ENERGY' }}</span>
             </button>
          </div>
        </div>
      </transition>
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

          <!-- Middle Fun/Interactive Zone -->
          <div class="header-center desktop-only">
             <div class="fun-text-container">
               <span class="fun-text">✨ Code with Flow</span>
             </div>
             <n-divider vertical />
             <n-switch :value="isEffectsEnabled" @update:value="toggleEffects" size="medium">
                <template #checked-icon>
                  <n-icon :component="SparklesOutline" color="#EAB308" />
                </template>
                <template #unchecked-icon>
                  <n-icon :component="SparklesOutline" color="#9CA3AF" />
                </template>
             </n-switch>
          </div>

          <n-space align="center" :size="24">
            <!-- Quick Actions -->
            <n-button circle secondary type="primary" class="action-btn desktop-only" @click="openSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
            </n-button>
            
            <n-popover trigger="click" placement="bottom-end" :show-arrow="false" style="padding: 0; width: 320px;">
              <template #trigger>
                <n-button circle secondary type="warning" class="action-btn">
                  <template #icon>
                    <n-badge :value="unreadCount" :max="99" :dot="unreadCount > 0" processing>
                      <n-icon :component="NotificationsOutline" />
                    </n-badge>
                  </template>
                </n-button>
              </template>
              
              <!-- Notification Panel -->
              <div class="notification-panel">
                <div class="panel-header">
                  <span class="panel-title">系统通知 ({{ unreadCount }})</span>
                  <n-button text size="small" type="primary" @click="markAllRead" :disabled="unreadCount === 0">
                    全部已读
                  </n-button>
                </div>
                <div class="panel-body">
                  <n-empty v-if="notifications.length === 0" description="暂无通知" style="padding: 24px 0" />
                  <div 
                    v-else 
                    v-for="item in notifications" 
                    :key="item.id" 
                    class="notification-item"
                    :class="{ 'unread': !item.read }"
                    @click="readNotification(item)"
                  >
                    <div class="notif-icon" :class="item.type">
                      <n-icon :component="item.icon" />
                    </div>
                    <div class="notif-content">
                      <div class="notif-title">{{ item.title }}</div>
                      <div class="notif-desc">{{ item.content }}</div>
                      <div class="notif-time">{{ item.time }}</div>
                    </div>
                    <div v-if="!item.read" class="notif-dot"></div>
                  </div>
                </div>
                <div class="panel-footer">
                  <n-button text block size="small">查看全部历史消息</n-button>
                </div>
              </div>
            </n-popover>
            
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
    
    <!-- 全局搜索模态框 -->
    <n-modal
      v-model:show="showSearchModal"
      preset="card"
      :style="{ width: '600px', maxWidth: '90vw' }"
      class="global-search-modal"
    >
      <div class="search-container">
        <n-input
          v-model:value="searchKeyword"
          size="large"
          placeholder="搜索题目、知识点..."
          clearable
          @keyup.enter="handleGlobalSearch"
          class="search-input"
          ref="searchInputRef"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" class="search-icon-prefix" />
          </template>
        </n-input>
        <div class="search-hint">按 Enter 开始搜索</div>
      </div>
    </n-modal>
    <!-- Rocket Launch Overlay -->
    <transition name="fade">
      <div v-if="showLaunchAnimation" class="launch-overlay">
        <div class="stars-bg"></div>
        <div class="rocket-container">
          <div class="rocket-body">
            <n-icon :component="RocketOutline" size="120" color="#fff" />
          </div>
          <div class="thrust-flame"></div>
          <div class="smoke-particle p1"></div>
          <div class="smoke-particle p2"></div>
          <div class="smoke-particle p3"></div>
          <div class="smoke-particle p4"></div>
        </div>
        <div class="launch-text">SYSTEM OPTIMIZING...</div>
      </div>
    </transition>
  </n-layout>
</template>

<script setup>
import { ref, h, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { 
  NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NDrawer, NDrawerContent,
  NMenu, NSpace, NText, NTag, NIcon, NAvatar, NDropdown, NButton, NPopover, NBadge, NEmpty, NModal, NInput, NSwitch, NDivider
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
  NotificationsOutline,
  InformationCircleOutline,
  AlertCircleOutline,
  TrophyOutline,
  SparklesOutline,
  FlashOutline,
  PulseOutline,

  HardwareChipOutline,
  RocketOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { isEffectsEnabled, toggleEffects } from '@/utils/mouseEffects'

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

// === 搜索功能 ===
const showSearchModal = ref(false)
const searchKeyword = ref('')
const searchInputRef = ref(null)

const openSearch = () => {
  showSearchModal.value = true
  // 自动聚焦
  nextTick(() => {
    // Naive UI Input 聚焦需要等待 DOM 渲染
    // 这里简单处理，用户手动点击也可以
  })
}

const handleGlobalSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  
  showSearchModal.value = false
  // 跳转到练习页面并带上搜索参数
  router.push({
    path: '/practice',
    query: { keyword }
  })
  searchKeyword.value = ''
}

// === 通知功能 (Mock Data) ===
const notifications = ref([
  { 
    id: 1, 
    type: 'info',
    title: '系统通知', 
    content: '期末复习系统 Pro 版已上线！', 
    time: '刚刚', 
    read: false,
    icon: InformationCircleOutline
  },
  { 
    id: 2, 
    type: 'warning',
    title: '错题提醒', 
    content: '您昨天有 5 道新错题待复习，点击前往错题本。', 
    time: '2小时前', 
    read: false,
    icon: AlertCircleOutline
  },
  { 
    id: 3, 
    type: 'success',
    title: '达成成就', 
    content: '恭喜！您已连续打卡 3 天。', 
    time: '1天前', 
    read: true,
    icon: TrophyOutline
  }
])

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const markAllRead = () => {
  notifications.value.forEach(n => n.read = true)
  message.success('已全部标记为已读')
}

const readNotification = (item) => {
  if (!item.read) {
    item.read = true
  }
  // 简单的跳转逻辑
  if (item.title === '错题提醒') {
    router.push('/wrong-book')
  }
}
// === Cyber Status Widget Logic ===
const cpuLoad = ref(42)
const syncRate = ref(98)
const isBoosting = ref(false)
const showLaunchAnimation = ref(false)

// Simulating Fluctuations
setInterval(() => {
  if (!isBoosting.value && !showLaunchAnimation.value) {
    cpuLoad.value = Math.max(20, Math.min(80, cpuLoad.value + Math.floor(Math.random() * 10) - 5))
    syncRate.value = Math.max(90, Math.min(100, syncRate.value + Math.floor(Math.random() * 3) - 1))
  }
}, 2000)

const triggerBoost = () => {
    if (isBoosting.value || showLaunchAnimation.value) return
    isBoosting.value = true
    showLaunchAnimation.value = true
    
    // Simulate Boost Calculation
    let interval = setInterval(() => {
        cpuLoad.value = Math.floor(Math.random() * 100)
        syncRate.value = Math.floor(Math.random() * 100)
    }, 100)
    
    // Animation Duration
    setTimeout(() => {
        clearInterval(interval)
        cpuLoad.value = 12
        syncRate.value = 100
        isBoosting.value = false
        showLaunchAnimation.value = false
        message.success('System Optimized: Efficiency +200%')
    }, 3000)
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
.desktop-sider :deep(.n-layout-sider-scroll-container) {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.desktop-sider {
  background: var(--off-white);
  border-right: 4px solid var(--void-black) !important;
  z-index: 50;
}

/* Cyber Status Widget */
.status-widget {
  margin-top: 12px; /* Follows menu closely */
  margin-bottom: 24px;
  margin-left: 12px;
  margin-right: 12px;
  border: 2px solid var(--void-black);
  background: white;
  box-shadow: 4px 4px 0 var(--void-black);
  padding: 12px;
  font-family: 'Courier New', Courier, monospace;
}

.widget-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 900;
  font-size: 12px;
  border-bottom: 2px solid var(--void-black);
  padding-bottom: 8px;
  margin-bottom: 12px;
  color: var(--void-black);
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 10px;
  font-weight: bold;
}

.stat-label { width: 60px; }

.cyber-progress {
  flex: 1;
  height: 8px;
  background: #eee;
  border: 1px solid var(--void-black);
  position: relative;
}

.cyber-bar {
  height: 100%;
  background: var(--neon-cyan);
  transition: width 0.5s ease;
}

.stat-val { width: 30px; text-align: right; }

.cyber-btn {
  width: 100%;
  background: var(--void-black);
  color: var(--neon-yellow);
  border: none;
  padding: 8px;
  font-family: inherit;
  font-weight: bold;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  transition: all 0.1s;
}

.cyber-btn:hover {
  background: var(--neon-magenta);
  color: white;
  transform: translate(-1px, -1px);
  box-shadow: 2px 2px 0 rgba(0,0,0,0.2);
}

.spinning { animation: spin 1s linear infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }

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

.header-center {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(245, 245, 245, 0.5);
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px dashed var(--void-black);
}

.fun-text {
  font-family: 'Gochi Hand', cursive;
  font-weight: bold;
  font-size: 16px;
  background: linear-gradient(90deg, #FF3EA5, #00E5FF);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-3px); }
  100% { transform: translateY(0px); }
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

/* === Notification Panel Styles (Enhanced Tech Style) === */
.notification-panel {
  background: white;
  border: 4px solid var(--void-black);
  box-shadow: 8px 8px 0 rgba(0,0,0,0.2);
  width: 340px; /* Slightly wider */
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 4px solid var(--void-black);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--neon-yellow); /* Yellow Header */
}

.panel-title {
  font-weight: 900;
  font-size: 16px;
  color: var(--void-black);
  text-transform: uppercase;
  font-family: 'Courier New', Courier, monospace;
  letter-spacing: -0.5px;
}

.panel-body {
  max-height: 400px;
  overflow-y: auto;
  background: 
    linear-gradient(90deg, transparent 95%, rgba(0,0,0,0.03) 95%),
    linear-gradient(transparent 95%, rgba(0,0,0,0.03) 95%);
  background-size: 20px 20px;
}

.notification-item {
  padding: 16px;
  border-bottom: 2px solid var(--void-black);
  display: flex;
  gap: 16px; /* More gap */
  cursor: pointer;
  position: relative;
  transition: all 0.1s;
  background: white;
  margin-bottom: -2px; /* Overlap borders */
}

.notification-item:hover {
  background: var(--off-white);
  transform: translateX(-4px);
  box-shadow: 4px 4px 0 var(--neon-cyan);
  z-index: 10;
  border-color: var(--void-black);
}

.notification-item.unread {
  background: #fff;
}
/* Unread indicator via left border */
.notification-item.unread::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 6px;
  background: var(--neon-magenta);
}

.notif-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--void-black);
  font-size: 20px;
  flex-shrink: 0;
  background: white;
  box-shadow: 3px 3px 0 rgba(0,0,0,0.2);
}

.notif-icon.info { color: var(--neon-cyan); }
.notif-icon.warning { color: var(--neon-yellow); }
.notif-icon.success { color: var(--neon-magenta); }

/* Invert colors on specific types for variety */
.notif-icon.success { background: var(--neon-magenta); color: white; }

.notif-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.notif-title {
  font-weight: 800;
  font-size: 14px;
  color: var(--void-black);
  font-family: 'Courier New', Courier, monospace;
  margin-bottom: 4px;
}

.notif-desc {
  font-size: 12px;
  color: #555;
  margin-bottom: 6px;
  line-height: 1.4;
  font-family: sans-serif;
  font-weight: 500;
}

.notif-time {
  font-size: 10px;
  color: var(--void-black);
  font-family: monospace;
  background: #eee;
  padding: 2px 4px;
  align-self: flex-start;
  border: 1px solid #ccc;
}

.notif-dot {
  display: none; /* Replaced by left strip */
}

.panel-footer {
  padding: 12px;
  border-top: 4px solid var(--void-black);
  text-align: center;
  background: var(--void-black);
}

.panel-footer :deep(.n-button) {
  color: var(--neon-cyan) !important;
  font-family: monospace;
  font-weight: bold;
}
.panel-footer :deep(.n-button:hover) {
  text-decoration: underline;
  text-decoration-style: wavy;
}


/* === Global Search Modal (Brutalist) === */
.global-search-modal .n-card {
  border: 4px solid var(--void-black) !important;
  border-radius: 0 !important;
  box-shadow: 12px 12px 0 var(--void-black) !important;
  background: var(--neon-yellow) !important; /* Bold Background */
}

.global-search-modal :deep(.n-card-header) {
  border-bottom: 4px solid var(--void-black) !important;
  background: white !important;
  padding: 16px 24px !important;
}

.global-search-modal :deep(.n-card__content) {
  background: white !important;
  padding: 30px !important;
}

.search-container {
  text-align: left;
}

/* Custom Search Input */
:deep(.search-input .n-input-wrapper) {
  padding: 0 12px !important;
  background: white !important;
}

:deep(.search-input .n-input__border),
:deep(.search-input .n-input__state-border) {
  border: 3px solid var(--void-black) !important;
  border-radius: 0 !important;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1) !important;
  transition: all 0.2s !important;
}

:deep(.search-input:hover .n-input__state-border) {
  box-shadow: 6px 6px 0 rgba(0,0,0,0.2) !important;
  border-color: var(--void-black) !important;
}

:deep(.search-input.n-input--focus .n-input__state-border) {
  box-shadow: 6px 6px 0 var(--neon-cyan) !important;
  border-color: var(--void-black) !important;
}

:deep(.search-input .n-input__input-el) {
  font-family: 'Courier New', monospace !important;
  font-weight: bold !important;
  font-size: 18px !important;
  height: 48px !important;
  color: var(--void-black) !important;
}

:deep(.search-input .n-input__placeholder) {
  font-style: italic;
  font-weight: normal;
}

.search-icon-prefix {
  font-size: 24px;
  color: var(--void-black);
  margin-right: 8px;
}

.search-hint {
  margin-top: 16px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  font-weight: bold;
  color: var(--void-black);
  background: var(--neon-cyan);
  display: inline-block;
  padding: 4px 8px;
  border: 2px solid var(--void-black);
  box-shadow: 3px 3px 0 rgba(0,0,0,0.1);
  transform: rotate(-1deg);
}
</style>
/* === ROCKET LAUNCH ANIMATION === */
.launch-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: linear-gradient(to bottom, #0f172a, #1e293b);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.stars-bg {
  position: absolute;
  top: 0; 
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(white 1px, transparent 1px);
  background-size: 50px 50px;
  animation: starsScroll 3s linear infinite;
  opacity: 0.5;
}

.rocket-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: rocketFly 3s ease-in-out forwards;
}

.rocket-body {
  z-index: 10;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
  animation: shake 0.5s ease-in-out infinite;
}

.thrust-flame {
  width: 20px;
  height: 60px;
  background: linear-gradient(to bottom, #f59e0b, #ef4444, transparent);
  border-radius: 50%;
  margin-top: -10px;
  filter: blur(5px);
  animation: flameFlicker 0.1s infinite alternate;
  transform-origin: top center;
}

.smoke-particle {
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  bottom: -40px;
  filter: blur(8px);
}
.smoke-particle.p1 { width: 40px; height: 40px; left: -30px; animation: smoke 1s linear infinite 0.1s; }
.smoke-particle.p2 { width: 50px; height: 50px; right: -30px; animation: smoke 1s linear infinite 0.2s; }
.smoke-particle.p3 { width: 30px; height: 30px; left: -10px; animation: smoke 0.8s linear infinite 0ms; }
.smoke-particle.p4 { width: 45px; height: 45px; right: -10px; animation: smoke 1.2s linear infinite 0.3s; }

.launch-text {
  margin-top: 50px;
  font-family: 'Courier New', monospace;
  font-size: 24px;
  font-weight: bold;
  color: var(--neon-cyan);
  text-shadow: 0 0 10px var(--neon-cyan);
  letter-spacing: 4px;
  animation: pulseText 1s infinite alternate;
}

@keyframes rocketFly {
  0% { transform: translateY(100vh) scale(0.5); }
  20% { transform: translateY(20vh) scale(1); }
  60% { transform: translateY(0vh) scale(1); }
  100% { transform: translateY(-120vh) scale(1); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-2px); }
  75% { transform: translateX(2px); }
}

@keyframes flameFlicker {
  0% { transform: scaleY(1); opacity: 0.8; }
  100% { transform: scaleY(1.2); opacity: 1; }
}

@keyframes starsScroll {
  0% { background-position: 0 0; }
  100% { background-position: 0 100px; }
}

@keyframes smoke {
  0% { transform: translateY(0) scale(1); opacity: 0.8; }
  100% { transform: translateY(100px) scale(2); opacity: 0; }
}

@keyframes pulseText {
  from { opacity: 0.6; }
  to { opacity: 1; }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
} 
/* End Rocket Launch Animation */
