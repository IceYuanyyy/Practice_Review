<template>
  <div class="sketch-register-container">
    <!-- 点阵纸背景 -->
    <div class="paper-dot-bg"></div>
    
    <!-- 装饰元素 -->
    <div class="decorations">
      <div class="decor-spring"></div>
      <div class="sticky-note-sm">Hi!</div>
    </div>

    <div class="register-content">
      <!-- 笔记本页面风格卡片 -->
      <div class="notebook-card">
        <!-- 螺旋装订孔装饰 (纯CSS模拟) -->
        <div class="spiral-binding">
           <div class="hole" v-for="n in 8" :key="n"></div>
        </div>

        <div class="card-body">
          <!-- 头部 -->
          <div class="header-section">
            <h1 class="marker-title">加入我们!</h1>
            <p class="pencil-text">创建你的专属题库账号</p>
            <div class="scribble-line">
              <svg viewBox="0 0 200 10" preserveAspectRatio="none">
                <path d="M0,5 Q5,0 10,5 T20,5 T30,5 T40,5 T50,5 T60,5 T70,5 T80,5 T90,5 T100,5 T110,5 T120,5 T130,5 T140,5 T150,5 T160,5 T170,5 T180,5 T190,5 T200,5" 
                      fill="none" stroke="#10b981" stroke-width="2"/>
              </svg>
            </div>
          </div>

          <!-- 注册表单 -->
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
              <div class="field-group">
                <n-input 
                  v-model:value="formData.username" 
                  placeholder="用户名 (必填)"
                  :input-props="{ autocomplete: 'username' }"
                  class="line-input"
                >
                  <template #prefix>
                    <n-icon :component="PersonOutline" class="field-icon"/>
                  </template>
                </n-input>
              </div>
            </n-form-item>

            <n-form-item path="nickname">
              <div class="field-group">
                <n-input 
                  v-model:value="formData.nickname" 
                  placeholder="昵称 (大家怎么称呼你?)"
                  class="line-input"
                >
                  <template #prefix>
                    <n-icon :component="HappyOutline" class="field-icon"/>
                  </template>
                </n-input>
              </div>
            </n-form-item>

            <n-form-item path="email">
              <div class="field-group">
                <n-input 
                  v-model:value="formData.email" 
                  placeholder="邮箱 (必填，用于接收验证码)"
                  :input-props="{ autocomplete: 'email' }"
                  class="line-input"
                >
                  <template #prefix>
                    <n-icon :component="MailOutline" class="field-icon"/>
                  </template>
                </n-input>
              </div>
            </n-form-item>

            <!-- 验证码输入区域 -->
            <n-form-item path="verificationCode">
              <div class="code-input-group">
                <n-input 
                  v-model:value="formData.verificationCode" 
                  placeholder="邮箱验证码 (6位)"
                  maxlength="6"
                  class="line-input code-input"
                >
                  <template #prefix>
                    <n-icon :component="KeyOutline" class="field-icon"/>
                  </template>
                </n-input>
                <button 
                  type="button"
                  class="send-code-btn"
                  :disabled="sendingCode || countdown > 0 || !formData.email"
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
                </button>
              </div>
            </n-form-item>

            <div class="password-group">
              <n-form-item path="password" class="half-width">
                <div class="field-group">
                  <n-input 
                    v-model:value="formData.password" 
                    type="password"
                    placeholder="密码"
                    show-password-on="click"
                    :input-props="{ autocomplete: 'new-password' }"
                    class="line-input"
                  >
                    <template #prefix>
                      <n-icon :component="LockClosedOutline" class="field-icon"/>
                    </template>
                  </n-input>
                </div>
              </n-form-item>

              <n-form-item path="confirmPassword" class="half-width">
                <div class="field-group">
                  <n-input 
                    v-model:value="formData.confirmPassword" 
                    type="password"
                    placeholder="确认密码"
                    show-password-on="click"
                    :input-props="{ autocomplete: 'new-password' }"
                    @keyup.enter="handleRegister"
                    class="line-input"
                  >
                    <template #prefix>
                      <n-icon :component="CheckmarkCircleOutline" class="field-icon"/>
                    </template>
                  </n-input>
                </div>
              </n-form-item>
            </div>

            <n-form-item>
              <button 
                class="action-btn register-btn"
                :disabled="loading"
                @click="handleRegister"
              >
                {{ loading ? '创建中...' : '注册账号' }}
                <div class="btn-shadow"></div>
              </button>
            </n-form-item>

            <div class="footer-link">
              <span class="text-muted">已经有账号了?</span>
              <button class="text-btn" @click="goToLogin">
                <span class="underline-hover">立即登录</span>
              </button>
            </div>
          </n-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NForm, NFormItem, NInput, NIcon } from 'naive-ui'
import { PersonOutline, LockClosedOutline, MailOutline, HappyOutline, CheckmarkCircleOutline, KeyOutline } from '@vicons/ionicons5'
import { register, sendVerificationCode } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

// 表单数据
const formData = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

// 验证码发送相关
const sendingCode = ref(false)
const countdown = ref(0)

// 密码一致性验证
function validatePasswordSame(rule, value) {
  return value === formData.password
}

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '用户名必填哦', trigger: 'blur' },
    { min: 3, max: 50, message: '长度3-50个字符', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '太长啦', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '邮箱必填，用于接收验证码', trigger: 'blur' },
    { type: 'email', message: '这个邮箱格式不对', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, max: 50, message: '至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePasswordSame, message: '两次密码不一致', trigger: 'blur' }
  ]
}

// 发送验证码
async function handleSendCode() {
  if (!formData.email) {
    message.warning('请先输入邮箱地址')
    return
  }
  
  // 验证邮箱格式
  const emailRegex = /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(formData.email)) {
    message.warning('请输入有效的邮箱地址')
    return
  }
  
  sendingCode.value = true
  
  try {
    const res = await sendVerificationCode(formData.email)
    if (res.code === 200) {
      message.success('✅ 验证码已发送，请查收邮件')
      // 开始倒计时（60秒）
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      message.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    console.error('验证码发送失败:', error)
    message.error('❌ 验证码发送失败，请重试')
  } finally {
    sendingCode.value = false
  }
}

// 注册处理
async function handleRegister() {
  try {
    await formRef.value?.validate()
    loading.value = true

    await register({
      username: formData.username,
      password: formData.password,
      confirmPassword: formData.confirmPassword,
      nickname: formData.nickname || undefined,
      email: formData.email,
      verificationCode: formData.verificationCode
    })

    message.success('注册成功！快去登录吧')
    router.push('/login')
  } catch (e) {
    console.error('注册失败', e)
    if (e.message) {
      message.error(e.message)
    }
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gochi+Hand&family=Patrick+Hand&display=swap');

.sketch-register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0fdf4; /* 浅绿背景 */
  position: relative;
  overflow: hidden;
  font-family: 'Patrick Hand', cursive;
}

/* 点阵背景 */
.paper-dot-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 0;
}

.decorations {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.sticky-note-sm {
  position: absolute;
  top: 10%;
  right: 20%;
  width: 80px;
  height: 80px;
  background: #fef3c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  box-shadow: 2px 2px 5px rgba(0,0,0,0.1);
  transform: rotate(10deg);
}

.notebook-card {
  width: 600px;
  background: #fff;
  border-radius: 4px; /* Basic round for browser */
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px; /* Sketchy */
  border: 2px solid #334155;
  padding: 0; /* Space handled by inner grid */
  position: relative;
  z-index: 10;
  box-shadow: 10px 15px 0px rgba(51, 65, 85, 0.1);
  display: flex;
}

/* 螺旋孔效果 */
.spiral-binding {
  width: 40px;
  background: #f1f5f9;
  border-right: 2px dashed #cbd5e1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-evenly;
  padding: 20px 0;
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
}

.hole {
  width: 12px;
  height: 12px;
  background: #334155;
  border-radius: 50%;
  position: relative;
}
.hole::after {
  content: '';
  position: absolute;
  top: -8px;
  left: -10px;
  width: 20px;
  height: 10px;
  border-top: 3px solid #64748b;
  border-radius: 50%;
  transform: rotate(-10deg);
}

.card-body {
  flex: 1;
  padding: 30px 40px;
}

.header-section {
  text-align: center;
  margin-bottom: 24px;
}

.marker-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 38px;
  color: #1e293b;
  margin: 0;
  transform: rotate(-2deg);
}

.pencil-text {
  color: #64748b;
  font-size: 18px;
  margin: 5px 0 10px;
}

.scribble-line {
  height: 10px;
  width: 100%;
  overflow: hidden;
  opacity: 0.6;
}

/* 表单样式：横线风格 */
.field-group {
  margin-bottom: 4px;
}

:deep(.line-input .n-input-wrapper) {
  background: transparent;
  border: none;
  border-bottom: 2px solid #cbd5e1;
  padding: 0;
  border-radius: 0;
  transition: all 0.3s;
}

:deep(.line-input:hover .n-input-wrapper) {
  border-bottom-color: #64748b;
}

:deep(.line-input.n-input--focus .n-input-wrapper) {
  border-bottom-color: #10b981; /* Green accent */
  box-shadow: 0 4px 6px -4px rgba(16, 185, 129, 0.2);
}

:deep(.n-input__input-el) {
  font-family: 'Patrick Hand', cursive !important;
  font-size: 18px !important;
  color: #334155 !important;
  height: 42px !important;
}

.field-icon {
  color: #94a3b8;
  margin-right: 8px;
}
:deep(.n-input--focus .field-icon) {
  color: #10b981;
}

.password-group {
  display: flex;
  gap: 16px;
}
.half-width {
  flex: 1;
}

/* 注册按钮：3D 按压风格 */
.register-btn {
  width: 100%;
  height: 52px;
  border: 3px solid #1e293b;
  background: #10b981;
  color: #fff;
  border-radius: 12px;
  font-family: 'Gochi Hand', cursive;
  font-size: 22px;
  cursor: pointer;
  position: relative;
  transition: transform 0.1s;
  margin-top: 10px;
  z-index: 2;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: #059669;
}

.register-btn:active {
  transform: translateY(2px);
}

/* 纯CSS模拟的按钮立体阴影 */
.register-btn::after {
  content: '';
  position: absolute;
  top: 6px;
  left: 6px;
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: #1e293b;
  z-index: -1;
  transition: transform 0.1s;
}

.register-btn:active::after {
  transform: translate(-3px, -3px);
}

/* 验证码输入区域 */
.code-input-group {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  padding: 10px 16px;
  background: #10b981;
  color: #fff;
  border: 2px solid #1e293b;
  border-radius: 8px;
  font-family: 'Patrick Hand', cursive;
  font-size: 16px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
  min-width: 100px;
  height: 42px;
}

.send-code-btn:hover:not(:disabled) {
  background: #059669;
  transform: translateY(-1px);
}

.send-code-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  opacity: 0.7;
  border-color: #94a3b8;
}

.footer-link {
  text-align: center;
  margin-top: 16px;
}

.text-muted {
  color: #64748b;
  margin-right: 8px;
}

.text-btn {
  background: none;
  border: none;
  font-family: 'Patrick Hand', cursive;
  font-size: 18px;
  color: #10b981;
  font-weight: 700;
  cursor: pointer;
}

.underline-hover {
  position: relative;
}
.underline-hover::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: currentColor;
  transform: scaleX(0);
  transition: transform 0.3s;
  border-radius: 2px;
}
.text-btn:hover .underline-hover::after {
  transform: scaleX(1);
}

/* 响应式 */
@media (max-width: 580px) {
  .notebook-card {
    width: 95%;
    flex-direction: column;
  }
  .spiral-binding {
    width: 100%;
    height: 30px;
    flex-direction: row;
    border-right: none;
    border-bottom: 2px dashed #cbd5e1;
    border-radius: 10px 10px 0 0;
    padding: 0 10px;
  }
  .hole {
    transform: rotate(90deg);
  }
  .password-group {
    flex-direction: column;
    gap: 0;
  }
}
</style>
