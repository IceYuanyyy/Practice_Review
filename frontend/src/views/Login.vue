<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="animated-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="login-content">
      <div class="login-card">
        <!-- Logo 顶部装饰 -->
        <div class="card-decoration"></div>

        <!-- Logo 区域 -->
        <div class="logo-area">
          <div class="logo-wrapper">
            <n-icon :component="SchoolOutline" size="48" color="#fff" />
          </div>
          <h1 class="app-title">Exam Master</h1>
          <p class="app-subtitle">智能题库 · 高效复习 · 轻松过考</p>
        </div>

        <!-- 登录表单 -->
        <n-form 
          ref="formRef" 
          :model="formData" 
          :rules="rules"
          label-placement="left"
          label-width="0"
          size="large"
          class="login-form"
        >
          <n-form-item path="username">
            <n-input 
              v-model:value="formData.username" 
              placeholder="Username / Student ID"
              :input-props="{ autocomplete: 'username' }"
              class="glass-input"
            >
              <template #prefix>
                <n-icon :component="PersonOutline" class="input-icon"/>
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="password">
            <n-input 
              v-model:value="formData.password" 
              type="password"
              placeholder="Password"
              show-password-on="click"
              :input-props="{ autocomplete: 'current-password' }"
              @keyup.enter="handleLogin"
              class="glass-input"
            >
              <template #prefix>
                <n-icon :component="LockClosedOutline" class="input-icon"/>
              </template>
            </n-input>
          </n-form-item>

          <div class="form-options">
            <n-checkbox v-model:checked="formData.rememberMe" class="custom-checkbox">
              <span class="checkbox-label">Keep me logged in</span>
            </n-checkbox>
            <n-button text type="primary" class="forgot-btn" @click="goToRegister">
              Create Account
            </n-button>
          </div>

          <n-button 
            type="primary" 
            block 
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            <span class="btn-text">Sign In</span>
            <template #icon>
              <n-icon>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path d="M294.1 256L167 129c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0l143 143c9.4 9.4 9.4 24.6 0 33.9l-143 143c-9.4 9.4-24.6 9.4-33.9 0s-9.4-24.6 0-33.9l127.1-127z" fill="currentColor"/></svg>
              </n-icon>
            </template>
          </n-button>
        </n-form>
      </div>

      <!-- 底部版权 -->
      <div class="footer-info">
        <p>&copy; 2025 Exam Practice System. Designed for Excellence.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NForm, NFormItem, NInput, NButton, NCheckbox, NIcon } from 'naive-ui'
import { SchoolOutline, PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const formData = reactive({
  username: '',
  password: '',
  rememberMe: true
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度为6-50个字符', trigger: 'blur' }
  ]
}

// 登录处理
async function handleLogin() {
  try {
    await formRef.value?.validate()
    loading.value = true

    await userStore.login({
      username: formData.username,
      password: formData.password,
      rememberMe: formData.rememberMe
    })

    message.success('登录成功，欢迎回来！')
    
    // 跳转到首页或之前的页面
    const redirect = router.currentRoute.value.query.redirect
    router.push(redirect || '/home')
  } catch (e) {
    console.error('登录失败', e)
    if (e.message) {
      message.error(e.message)
    }
  } finally {
    loading.value = false
  }
}

// 跳转到注册页
function goToRegister() {
  router.push('/register')
}
</script>

<style scoped>
/* 字体引入 - 虽然 App.vue 有定义，这里强化一下特定字重 */
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;700&display=swap');

.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  /* Deep Blue/Purple Theme */
  background: #0f172a;
  position: relative;
  overflow: hidden;
  font-family: 'Outfit', sans-serif;
}

/* 动态背景动画 */
.animated-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
  z-index: 0;
}

.blob {
  position: absolute;
  filter: blur(80px);
  opacity: 0.6;
  border-radius: 50%;
  animation: float 10s infinite ease-in-out alternate;
}

.blob-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #4f46e5 0%, #312e81 70%);
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.blob-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #d946ef 0%, #701a75 70%);
  bottom: -50px;
  right: -50px;
  animation-delay: -2s;
  animation-duration: 12s;
}

.blob-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #06b6d4 0%, #0e7490 70%);
  top: 40%;
  left: 60%;
  animation-delay: -4s;
  animation-duration: 15s;
}

.grid-overlay {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  z-index: 1;
  mask-image: radial-gradient(circle at center, black 40%, transparent 100%);
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, 30px) scale(1.05); }
  100% { transform: translate(-20px, -10px) scale(0.95); }
}

/* 登录内容区 */
.login-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

/* Glassmorphism Card */
.login-card {
  width: 420px;
  padding: 4px; /* padding for the border gradient */
  background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.05) 100%);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.login-form, .logo-area, .form-options {
  background: rgba(15, 23, 42, 0.6); /* Dark semi-transparent inside */
  backdrop-filter: blur(20px);
  padding: 40px;
  border-radius: 20px;
}

.login-card {
  /* Reset inner structure to be single container visually */
  background: rgba(17, 24, 39, 0.5);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  padding: 48px 40px;
  border-radius: 24px;
  overflow: visible;
  position: relative;
}

.card-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #4f46e5, #ec4899);
  border-radius: 24px 24px 0 0;
}

/* Logo Area */
.logo-area {
  padding: 0;
  background: none;
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  width: 90px;
  height: 90px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #4f46e5 0%, #ec4899 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 25px rgba(79, 70, 229, 0.4);
  transform: rotate(-5deg);
  transition: transform 0.3s ease;
}

.login-card:hover .logo-wrapper {
  transform: rotate(0deg) scale(1.05);
}

.app-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(to right, #fff, #cbd5e1);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.app-subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
  font-weight: 400;
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* Form Styles */
.login-form {
  padding: 0;
  background: none;
}

/* Customizing Naive UI Inputs */
/* The 'glass-input' class is applied to the n-input root element, so we target it directly */
:deep(.glass-input) {
  background-color: rgba(0, 0, 0, 0.2) !important; /* Force dark background */
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --n-caret-color: white !important;
  --n-color: rgba(0, 0, 0, 0.2) !important;
  --n-color-focus: rgba(0, 0, 0, 0.4) !important;
}

:deep(.glass-input:hover) {
  border-color: rgba(255, 255, 255, 0.3) !important;
  background-color: rgba(0, 0, 0, 0.3) !important;
  --n-color-hover: rgba(0, 0, 0, 0.3) !important;
}

:deep(.glass-input.n-input--focus) {
  background-color: rgba(0, 0, 0, 0.4) !important;
  border-color: #818cf8 !important;
  box-shadow: 0 0 0 2px rgba(129, 140, 248, 0.2);
}

:deep(.n-input__input-el) {
  color: white !important;
  font-size: 16px;
  height: 48px;
}

:deep(.n-input__placeholder) {
  color: rgba(255, 255, 255, 0.5) !important;
}

/* Handle Webkit Autofill - Ensure text is visible */
:deep(.n-input__input-el:-webkit-autofill),
:deep(.n-input__input-el:-webkit-autofill:hover), 
:deep(.n-input__input-el:-webkit-autofill:focus), 
:deep(.n-input__input-el:-webkit-autofill:active) {
  -webkit-text-fill-color: #fff !important;
  -webkit-box-shadow: 0 0 0 1000px #1e293b inset !important; /* Match dark theme bg */
  transition: background-color 5000s ease-in-out 0s;
  caret-color: white !important;
}

.input-icon {
  color: rgba(255, 255, 255, 0.7);
}

/* Options */
.form-options {
  padding: 0;
  background: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  margin-bottom: 32px;
}

.custom-checkbox {
  --n-text-color: #cbd5e1 !important;
}

.checkbox-label {
  color: #bdc5d6;
  font-size: 14px;
}

.forgot-btn {
  color: #818cf8;
  font-weight: 500;
}

.forgot-btn:hover {
  color: #a5b4fc;
}

/* Login Button */
.login-btn {
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(90deg, #4f46e5, #9333ea);
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.4);
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.login-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.2) 50%, rgba(255,255,255,0) 100%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.5);
}

.login-btn:hover::after {
  transform: translateX(100%);
}

.btn-text {
  margin-right: 8px;
}

/* Footer */
.footer-info p {
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  letter-spacing: 0.5px;
}

/* Responseive */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 32px 24px;
  }
}
</style>
