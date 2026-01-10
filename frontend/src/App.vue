<template>
  <n-config-provider :theme="naiveTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <GlobalProvider />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { h, defineComponent, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, useMessage, useDialog } from 'naive-ui'
import { initMouseEffects } from '@/utils/mouseEffects'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'

const themeStore = useThemeStore()
const { naiveTheme, themeOverrides } = storeToRefs(themeStore)

// 全局 Provider 组件，用于暴露 message 到 window
const GlobalProvider = defineComponent({
  name: 'GlobalProvider',
  setup() {
    // 将 message 暴露到 window，供 request.js 使用
    window.$message = useMessage()
    const dialog = useDialog()
    
    // 初始化全局鼠标特效和显示系统更新提示
    onMounted(() => {
      initMouseEffects()
      themeStore.applyTheme() // Ensure theme is applied on load
      
      // 检查是否已经显示过系统更新提示
      const hasShownNotice = localStorage.getItem('systemUpdateNoticeShown_2026')
      if (!hasShownNotice) {
        // 延迟500ms显示弹窗，确保页面已经加载完成
        setTimeout(() => {
          dialog.warning({
            title: '🔔 温馨提示',
            content: '系统已完成升级更新！\n\n由于服务器重新部署，之前的用户数据已清空。\n\n非常抱歉给您带来不便，请重新注册账号并导入您的数据。\n\n感谢您的理解与支持！',
            positiveText: '我知道了',
            maskClosable: false,
            closable: false,
            style: {
              width: '480px'
            },
            onPositiveClick: () => {
              // 用户点击确认后，记录已显示过提示，避免重复显示
              localStorage.setItem('systemUpdateNoticeShown_2026', 'true')
            }
          })
        }, 500)
      }
    })
    
    return () => h(RouterView)
  }
})
</script>

<style>
/* 引入 Inter 字体 (如果项目中没有本地字体，这里只是备选方案，主要依赖系统字体) */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: var(--font-family-base, 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--color-n-50, #f8fafc);
  color: var(--color-n-800, #1e293b);
}

#app {
  width: 100%;
  height: 100vh;
}

/* 滚动条美化 (Mac OS风格) */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-thumb {
  background: var(--color-n-300, #cbd5e1);
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: content-box;
}
::-webkit-scrollbar-thumb:hover {
  background-color: var(--color-n-400, #94a3b8);
}
::-webkit-scrollbar-track {
  background: transparent;
}

/* Global Mouse Effect Canvas Styles - Force Top Layer */
canvas:not([data-zr-dom-id]),
.js-cursor-container {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 2147483647 !important; /* Max Z-Index to ensure it's on top of EVERYTHING */
  pointer-events: none !important; /* Allow clicks to pass through */
  display: block !important;
}
</style>
