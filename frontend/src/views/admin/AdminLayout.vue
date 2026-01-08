<template>
  <n-layout has-sider class="comic-layout">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="comic-sider"
    >
      <div class="logo-container">
        <div class="comic-badge">
          <n-icon :component="SettingsOutline" size="24" color="#000" />
        </div>
        <span v-if="!collapsed" class="logo-text">ADMIN ZONE</span>
      </div>
      
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
        class="comic-menu"
      />
    </n-layout-sider>

    <n-layout class="comic-main">
      <n-layout-header class="comic-header">
        <n-space justify="space-between" align="center" style="width: 100%">
          <div class="comic-title-box">
             <n-breadcrumb>
              <n-breadcrumb-item @click="router.push('/home')" class="comic-breadcrumb-item">HOME</n-breadcrumb-item>
              <n-breadcrumb-item class="comic-breadcrumb-item">ADMIN</n-breadcrumb-item>
              <n-breadcrumb-item class="comic-breadcrumb-item active">{{ currentTitle }}</n-breadcrumb-item>
            </n-breadcrumb>
          </div>
         
          <n-button class="comic-back-btn" @click="router.push('/home')">
            <template #icon><n-icon :component="ArrowBackOutline" /></template>
            RETURN!
          </n-button>
        </n-space>
      </n-layout-header>

      <n-layout-content :native-scrollbar="false" content-style="padding: 24px; position: relative; min-height: 100%;">
        <router-view v-slot="{ Component }">
          <transition name="slide-in-right" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
        <div class="page-turn-hint"></div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, h, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, 
  NMenu, NIcon, NSpace, NBreadcrumb, NBreadcrumbItem, NButton 
} from 'naive-ui'
import {
  GridOutline,
  PeopleOutline,
  TimeOutline,
  DocumentTextOutline,
  SettingsOutline,
  ArrowBackOutline,
  Star,
  MegaphoneOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const activeKey = computed(() => route.path)

const currentTitle = computed(() => {
  const map = {
    '/admin/dashboard': 'DASHBOARD',
    '/admin/users': 'HEROES',
    '/admin/login-logs': 'LOGS',
    '/admin/operation-logs': 'OPS',
    '/admin/announcements': 'ANNOUNCEMENTS'
  }
  return map[route.path] || 'ZONE'
})

const renderIcon = (icon) => () => h(NIcon, null, { default: () => h(icon) })

// Helper to add 'starburst' to active icon potentially, but simplest is just changing styles based on active class
const menuOptions = [
  { label: 'DASHBOARD', key: '/admin/dashboard', icon: renderIcon(GridOutline) },
  { label: 'MANAGE USERS', key: '/admin/users', icon: renderIcon(PeopleOutline) },
  { label: 'ANNOUNCEMENTS', key: '/admin/announcements', icon: renderIcon(MegaphoneOutline) },
  { label: 'LOGIN LOGS', key: '/admin/login-logs', icon: renderIcon(TimeOutline) },
  { label: 'OP LOGS', key: '/admin/operation-logs', icon: renderIcon(DocumentTextOutline) }
]

const handleMenuSelect = (key) => router.push(key)
</script>

<style>
/* Global Comic Font Import - simulating with logic that it might be available or fallback */
@import url('https://fonts.googleapis.com/css2?family=Bangers&family=Comic+Neue:wght@700&display=swap');
</style>

<style scoped>
:deep(.n-layout-sider-scroll-container) {
  overflow-x: hidden;
}

/* --- VARIABLES --- */
.comic-layout {
  --comic-bg-dark: #2C2C2C;
  --comic-red: #E23636;
  --comic-blue: #1E90FF;
  --comic-yellow: #FFD700;
  --comic-border: 4px solid #000000;
  --comic-font: 'Bangers', 'Impact', sans-serif;
  height: 100vh;
  /* Ben-Day Dots Background for the whole sider area mostly */
}

/* --- SIDER (The Spine) --- */
.comic-sider {
  background-color: var(--comic-bg-dark) !important;
  border-right: var(--comic-border) !important;
  /* Ben-Day Dots Pattern */
  background-image: radial-gradient(#000 20%, transparent 20%),
                    radial-gradient(#000 20%, transparent 20%);
  background-position: 0 0, 4px 4px;
  background-size: 8px 8px;
  /* We want opacity 0.1 for dots effectively. 
     Since we can't opacity the bg-image easily without pseudo, let's trust the #2C2C2C absorbs it or use a blend mode if needed. 
     Standard trick: overlay. But for simplicity, direct pattern on dark grey.
  */
}

.comic-sider :deep(.n-layout-toggle-button) {
  background-color: var(--comic-yellow) !important;
  border: 3px solid #000 !important;
  color: #000 !important;
  top: 50% !important;
  border-radius: 50% !important;
  width: 32px;
  height: 32px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.comic-sider :deep(.n-layout-toggle-button:hover) {
  transform: scale(1.2) rotate(180deg);
}

/* Logo Area */
.logo-container {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: var(--comic-border);
  background: var(--comic-red);
  transform: skew(-2deg);
  margin-bottom: 20px;
  box-shadow: 4px 4px 0 #000;
}

.comic-badge {
  background: #fff;
  border: 2px solid #000;
  border-radius: 50%;
  padding: 4px;
  display: flex;
}

.logo-text {
  font-family: var(--comic-font);
  font-size: 24px;
  letter-spacing: 2px;
  color: #fff;
  text-shadow: 3px 3px 0 #000;
  transform: skew(2deg); /* Counter skew */
}

/* --- MENU (Speech Bubbles) --- */
.comic-menu {
  background: transparent !important;
  font-family: var(--comic-font);
  padding: 10px;
}

/* Targeting Naive UI Menu Items Deeply */
:deep(.n-menu-item) {
  margin-bottom: 12px;
}

:deep(.n-menu-item-content) {
  background: #fff;
  border: 3px solid #000 !important;
  border-radius: 20px !important; /* Dialogue bubble shape */
  position: relative;
  transition: all 0.2s ease;
  overflow: visible !important;
}

:deep(.n-menu-item-content::after) {
  /* Little triangle for speech bubble? fast hack: skip for now as rect captions are also valid */
  display: none; 
}

/* Text Styling */
:deep(.n-menu-item-content-header) {
  font-weight: bold;
  font-size: 18px !important;
  color: #000 !important;
  text-transform: uppercase;
}
:deep(.n-menu-item-content__icon) {
  color: #000 !important;
}

/* Hover Effect */
:deep(.n-menu-item-content:hover) {
  transform: translateX(10px) skew(-3deg);
  box-shadow: -4px 4px 0 rgba(0,0,0,0.5);
  background: #fffef0;
}

/* Active State */
:deep(.n-menu-item-content.n-menu-item-content--selected) {
  background: var(--comic-blue) !important;
  transform: scale(1.05) rotate(-1deg);
  box-shadow: 4px 4px 0 #000 !important;
}
:deep(.n-menu-item-content.n-menu-item-content--selected .n-menu-item-content-header) {
  color: #fff !important;
  text-shadow: 2px 2px 0 #000;
}
:deep(.n-menu-item-content.n-menu-item-content--selected .n-menu-item-content__icon) {
  color: #ffd700 !important; /* Gold icon on blue */
}
:deep(.n-menu-item-content.n-menu-item-content--selected)::before {
  /* Starburst effect hint - CSS only simple active indicator */
  content: '★';
  position: absolute;
  right: 10px;
  color: #ffd700;
  font-size: 20px;
  text-shadow: 2px 2px 0 #000;
  z-index: 10;
}

/* --- HEADER --- */
.comic-header {
  height: 70px;
  background: var(--comic-red) !important;
  border-bottom: var(--comic-border) !important;
  display: flex;
  align-items: center;
  padding: 0 24px;
  z-index: 10;
}

.comic-title-box {
  background: #fff;
  border: 3px solid #000;
  padding: 5px 15px;
  transform: skew(-5deg);
  box-shadow: 4px 4px 0 rgba(0,0,0,0.3);
}

.comic-breadcrumb-item {
  font-family: var(--comic-font);
  font-size: 18px;
  color: #000 !important;
  font-weight: bold;
  transform: skew(5deg); /* Counter skew */
}
.comic-breadcrumb-item :deep(.n-breadcrumb-item__link) {
  color: #000 !important;
}
.comic-breadcrumb-item.active :deep(.n-breadcrumb-item__link) {
  color: var(--comic-red) !important;
  text-shadow: 1px 1px 0 #000;
}

/* Back Button 'RETURN!' */
.comic-back-btn {
  background-color: var(--comic-yellow) !important;
  color: #000 !important;
  font-family: var(--comic-font) !important;
  font-size: 20px !important;
  border: 3px solid #000 !important;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.1s !important;
}
.comic-back-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 #000;
}

/* --- CONTENT AREA --- */
.comic-main {
  background-color: #FFFEF0 !important;
  /* Paper Texture Noise */
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.05'/%3E%3C/svg%3E");
}

.page-turn-hint {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 60px;
  height: 60px;
  background: linear-gradient(-45deg, #000 2px, transparent 2px), linear-gradient(-45deg, #fff 30px, transparent 32px);
  border-top: 2px solid rgba(0,0,0,0.1);
  border-left: 2px solid rgba(0,0,0,0.1);
  box-shadow: -5px -5px 10px rgba(0,0,0,0.1);
  pointer-events: none;
  /* Visual flair for 'curled page' corner */
}

/* --- TRANSITIONS --- */
.slide-in-right-enter-active,
.slide-in-right-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-in-right-enter-from {
  opacity: 0;
  transform: translateX(100px) rotate(2deg);
}

.slide-in-right-leave-to {
  opacity: 0;
  transform: translateX(-100px) rotate(-2deg);
}
</style>
