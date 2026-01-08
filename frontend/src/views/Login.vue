<template>
  <div class="sketch-login-container">
    <!-- 网格纸背景 -->
    <div class="paper-grid-bg"></div>
    
    <!-- 漂浮的手绘装饰 -->
    <div class="doodles">
      <div class="doodle-arrow">
         <svg viewBox="0 0 100 100" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20,80 Q50,20 80,40" />
            <path d="M80,40 L70,35 M80,40 L75,50" />
         </svg>
         <span class="doodle-text">Start Here!</span>
      </div>
      <div class="doodle-star">★</div>
      <div class="doodle-circle"></div>
      <div class="doodle-bulb">💡</div>
    </div>

    <div class="login-content">
      <!-- 拍立得照片风格的卡片 -->
      <div class="sketch-card login-card-rotate">
        <!-- 胶带 -->
        <div class="tape-strip top-center"></div>
        
        <!-- 卡片内容 -->
        <div class="card-inner">
          <!-- Logo 区域 -->
          <div class="brand-area">
            <div class="sketch-logo">
              <n-icon :component="SchoolOutline" size="42" color="#2c3e50" />
            </div>
            <h1 class="hand-title">Exam Master</h1>
            <div class="underline-sketch"></div>
            <p class="hand-subtitle">你的备考手账本</p>
          </div>

          <!-- 登录表单 -->
          <n-form 
            ref="formRef" 
            :model="formData" 
            :rules="rules"
            label-placement="left"
            label-width="0"
            size="large"
            class="sketch-form"
          >
            <n-form-item path="username">
              <div class="input-wrapper">
                <label class="sketch-label">用户名 / 学号</label>
                <n-input 
                  v-model:value="formData.username" 
                  placeholder="请输入你的账号"
                  :input-props="{ autocomplete: 'username' }"
                  class="sketch-input"
                >
                  <template #prefix>
                    <n-icon :component="PersonOutline" class="sketch-icon"/>
                  </template>
                </n-input>
              </div>
            </n-form-item>

            <n-form-item path="password">
              <div class="input-wrapper">
                <label class="sketch-label">密码</label>
                <n-input 
                  v-model:value="formData.password" 
                  type="password"
                  placeholder="请输入你的密码"
                  show-password-on="click"
                  :input-props="{ autocomplete: 'current-password' }"
                  @keyup.enter="handleLogin"
                  class="sketch-input"
                >
                  <template #prefix>
                    <n-icon :component="LockClosedOutline" class="sketch-icon"/>
                  </template>
                </n-input>
              </div>
            </n-form-item>

            <div class="form-extras">
              <n-checkbox v-model:checked="formData.rememberMe" class="sketch-checkbox">
                <span class="checkbox-text">记住我</span>
              </n-checkbox>
              <div class="arrow-link" @click="goToRegister">
                没有账号? 去注册 <span class="arrow">→</span>
              </div>
            </div>

            <button 
              class="sketch-button primary-btn"
              :disabled="loading"
              @click="handleLogin"
            >
              <span class="btn-content">
                {{ loading ? '登录中...' : '开始学习！' }}
                <n-icon v-if="!loading" :component="ArrowForward" />
              </span>
            </button>
          </n-form>
        </div>
      </div>

      <!-- 底部手写版权 -->
      <div class="sketch-footer">
        <p>© 2026 Study Notes. Keep learning!</p>
        <div class="contact-icons">
          <!-- QQ -->
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="icon-wrapper">
                <n-icon :component="ChatbubbleEllipsesOutline" />
              </div>
            </template>
            qq: 2478686497@qq.com
          </n-tooltip>

          <!-- GitHub -->
          <n-tooltip trigger="hover">
            <template #trigger>
              <a href="https://github.com/IceYuanyyy" target="_blank" class="icon-wrapper github-link">
                <n-icon :component="LogoGithub" />
              </a>
            </template>
            GitHub: IceYuanyyy (点击访问)
          </n-tooltip>

          <!-- Gmail -->
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="icon-wrapper">
                <n-icon :component="LogoGoogle" />
              </div>
            </template>
            ercurym86@gmail.com
          </n-tooltip>

          <!-- Student Mail -->
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="icon-wrapper">
                <n-icon :component="SchoolOutline" />
              </div>
            </template>
            2023213873@sdtbu.edu.cn
          </n-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NForm, NFormItem, NInput, NCheckbox, NIcon, NTooltip } from 'naive-ui'
import { 
  SchoolOutline, 
  PersonOutline, 
  LockClosedOutline, 
  ArrowForwardOutline as ArrowForward,
  LogoGithub,
  LogoGoogle,
  MailOutline,
  ChatbubbleEllipsesOutline
} from '@vicons/ionicons5'
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
    { required: true, message: '哎呀，忘了填名字！', trigger: 'blur' },
    { min: 3, max: 50, message: '名字太短啦 (3-50字符)', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '口令是必填的哦', trigger: 'blur' },
    { min: 6, max: 50, message: '口令至少要6位', trigger: 'blur' }
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

    message.success('欢迎回来！准备好学习了吗？')
    
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

function goToRegister() {
  router.push('/register')
}
</script>

<style scoped>
/* 引入手写字体 */
@import url('https://fonts.googleapis.com/css2?family=Gochi+Hand&family=Patrick+Hand&display=swap');

.sketch-login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  position: relative;
  overflow: hidden;
  font-family: 'Patrick Hand', cursive;
  color: #2c3e50;
}

/* 网格纸背景 */
.paper-grid-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(#e5e7eb 1px, transparent 1px),
    linear-gradient(90deg, #e5e7eb 1px, transparent 1px);
  background-size: 24px 24px;
  background-position: center;
  z-index: 0;
}

/* 涂鸦装饰 */
.doodles {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.doodle-arrow {
  position: absolute;
  top: 15%;
  left: 20%;
  width: 100px;
  height: 100px;
  color: #2c3e50;
  transform: rotate(-15deg);
  opacity: 0.6;
}
.doodle-text {
  position: absolute;
  top: -20px;
  left: 10px;
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
}

.doodle-bulb {
  position: absolute;
  top: 10%;
  right: 15%;
  font-size: 64px;
  transform: rotate(15deg);
  filter: drop-shadow(4px 4px 0px rgba(0,0,0,0.1));
  animation: float 3s ease-in-out infinite;
}

.doodle-star {
  position: absolute;
  bottom: 15%;
  left: 10%;
  font-size: 48px;
  color: #f59e0b;
  transform: rotate(-10deg);
}

.doodle-circle {
  position: absolute;
  bottom: 20%;
  right: 20%;
  width: 60px;
  height: 60px;
  border: 3px solid #ef4444;
  border-radius: 50% 60% 40% 70% / 50% 50% 60% 50%;
  transform: rotate(45deg);
}

@keyframes float {
  0%, 100% { transform: rotate(15deg) translateY(0); }
  50% { transform: rotate(15deg) translateY(-10px); }
}

/* 内容区域 */
.login-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

/* 手绘风格卡片 */
.sketch-card {
  width: 460px;
  background: #ffffff;
  border: 3px solid #2c3e50;
  border-radius: 2px 2px 2px 2px; /* Slight imperfection handled by transforms usually, but border-radius helps */
  /* 不完美的边框圆角 */
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  
  padding: 40px 32px;
  position: relative;
  box-shadow: 10px 10px 0px rgba(44, 62, 80, 0.1);
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.login-card-rotate {
  transform: rotate(-2deg);
}

.sketch-card:hover {
  transform: rotate(0deg) scale(1.02);
  box-shadow: 15px 15px 0px rgba(44, 62, 80, 0.15);
  z-index: 20;
}

/* 胶带效果 */
.tape-strip {
  position: absolute;
  height: 35px;
  width: 120px;
  background-color: rgba(255, 255, 255, 0.4);
  border-left: 1px dashed rgba(0,0,0,0.1);
  border-right: 1px dashed rgba(0,0,0,0.1);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  backdrop-filter: blur(2px);
  z-index: 10;
  opacity: 0.8;
}

.tape-strip.top-center {
  top: -15px;
  left: 50%;
  transform: translateX(-50%) rotate(2deg);
  background: rgba(254, 243, 199, 0.6); /* 淡黄色胶带 */
}

/* Logo 区域 */
.brand-area {
  text-align: center;
  margin-bottom: 32px;
}

.sketch-logo {
  display: inline-flex;
  padding: 12px;
  border: 3px solid #2c3e50;
  border-radius: 50% 60% 40% 70% / 40% 50% 60% 50%;
  margin-bottom: 12px;
  background: #fff;
}

.hand-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 36px;
  margin: 0;
  color: #2c3e50;
  letter-spacing: 1px;
}

.underline-sketch {
  height: 4px;
  width: 120px;
  background: #ff6b6b;
  margin: 4px auto 8px;
  border-radius: 100% 0% 100% 5% / 10% 100% 5% 100%;
  transform: rotate(-1deg);
}

.hand-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

/* 表单组件定制 */
.sketch-form {
  margin-top: 20px;
}

.input-wrapper {
  margin-bottom: 4px;
  width: 100%;
}

.sketch-label {
  display: block;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #2c3e50;
}

/* 覆盖 Naive UI Input 样式 */
:deep(.sketch-input .n-input), :deep(.sketch-input.n-input) {
  background-color: transparent !important;
  border: none !important;
  box-shadow: none !important;
  border-radius: 0 !important;
}

/* 自定义 Input 容器 */
:deep(.sketch-input .n-input-wrapper) {
  border: 2px solid #94a3b8;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  padding: 0 12px;
  background: #f8fafc;
  transition: all 0.3s ease;
}

:deep(.sketch-input.n-input--focus .n-input-wrapper),
:deep(.sketch-input:hover .n-input-wrapper) {
  border-color: #2c3e50;
  background: #fff;
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
  transform: scale(1.01);
}

:deep(.n-input__input-el) {
  font-family: 'Patrick Hand', cursive !important;
  font-size: 18px !important;
  height: 48px !important;
  color: #2c3e50 !important;
}

.sketch-icon {
  color: #64748b;
}

:deep(.n-input--focus .sketch-icon) {
  color: #2c3e50;
}

/* 额外选项区域 */
.form-extras {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 32px;
}

.sketch-checkbox {
  --n-color-checked: #2c3e50 !important;
  --n-border-checked: #2c3e50 !important;
}
:deep(.n-checkbox-box) {
  border-radius: 4px !important;
  border: 2px solid #94a3b8 !important;
}
:deep(.n-checkbox--checked .n-checkbox-box) {
  border: 2px solid #2c3e50 !important;
  background: #2c3e50 !important;
}

.checkbox-text {
  font-size: 16px;
  color: #4b5563;
}

.arrow-link {
  cursor: pointer;
  font-size: 16px;
  color: #4b5563;
  border-bottom: 2px dashed #9ca3af;
  padding-bottom: 2px;
  transition: all 0.2s;
}

.arrow-link:hover {
  color: #2c3e50;
  border-bottom-style: solid;
  border-color: #2c3e50;
}

.arrow {
  display: inline-block;
  transition: transform 0.2s;
}
.arrow-link:hover .arrow {
  transform: translateX(4px);
}

/* 手绘风格按钮 */
.sketch-button {
  width: 100%;
  height: 56px;
  border: 3px solid #2c3e50;
  /* 手绘圆角 */
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  background: #ff6b6b;
  color: #fff;
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 4px 4px 0px #2c3e50;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.sketch-button:hover:not(:disabled) {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px #2c3e50;
  background: #ff5252;
}

.sketch-button:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px #2c3e50;
}

.sketch-button:disabled {
  background: #94a3b8;
  border-color: #64748b;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* Footer */
.sketch-footer {
  margin-top: 32px;
  font-family: 'Patrick Hand', cursive;
  color: #6b7280;
  font-size: 14px;
  transform: rotate(1deg);
  text-align: center;
}

.contact-icons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
}

.icon-wrapper {
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px dashed #9ca3af;
}

.icon-wrapper:hover {
  color: #2c3e50;
  border-color: #2c3e50;
  transform: scale(1.1) rotate(-5deg);
  background: white;
}

.github-link {
  color: inherit;
  text-decoration: none;
}

/* 响应式 */
@media (max-width: 480px) {
  .sketch-card {
    width: 90%;
    padding: 24px;
    transform: none;
  }
  .sketch-card:hover { transform: none; }
  .doodle-bulb, .doodle-arrow { display: none; }
}
</style>
