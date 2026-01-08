<template>
  <div class="sketch-login-container">
    <div class="paper-grid-bg"></div>
    
    <div class="login-content">
      <div class="sketch-card login-card-rotate">
        <div class="tape-strip top-center"></div>
        
        <div class="card-inner">
          <div class="brand-area">
            <div class="sketch-logo">
              <n-icon :component="MailOutline" size="42" color="#2c3e50" />
            </div>
            <h1 class="hand-title">邮箱验证</h1>
            <div class="underline-sketch"></div>
            <p class="hand-subtitle">为了您的账号安全，请验证邮箱</p>
          </div>

          <n-form 
            ref="formRef" 
            :model="formData" 
            :rules="rules"
            label-placement="left"
            label-width="0"
            size="large"
            class="sketch-form"
          >
            <n-form-item path="email">
              <div class="input-wrapper">
                <label class="sketch-label">邮箱地址</label>
                <div class="input-group">
                  <n-input 
                    v-model:value="formData.email" 
                    placeholder="请输入邮箱"
                    class="sketch-input"
                  >
                    <template #prefix>
                      <n-icon :component="MailOutline" class="sketch-icon"/>
                    </template>
                  </n-input>
                </div>
              </div>
            </n-form-item>

            <n-form-item path="code">
              <div class="input-wrapper">
                <label class="sketch-label">验证码</label>
                <div class="code-input-row">
                  <n-input 
                    v-model:value="formData.code" 
                    placeholder="6位验证码"
                    maxlength="6"
                    class="sketch-input"
                  >
                    <template #prefix>
                      <n-icon :component="KeyOutline" class="sketch-icon"/>
                    </template>
                  </n-input>
                  <button 
                    type="button" 
                    class="sketch-button small-btn"
                    :disabled="sending || countdown > 0"
                    @click="handleSendCode"
                  >
                    {{ countdown > 0 ? `${countdown}s` : '发送' }}
                  </button>
                </div>
              </div>
            </n-form-item>

            <button 
              class="sketch-button primary-btn"
              :disabled="loading"
              @click="handleVerify"
            >
              <span class="btn-content">
                {{ loading ? '验证中...' : '完成验证' }}
                <n-icon v-if="!loading" :component="ArrowForward" />
              </span>
            </button>
            
            <div class="logout-link" @click="handleLogout">
              切换账号 / 退出登录
            </div>
          </n-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NForm, NFormItem, NInput, NIcon } from 'naive-ui'
import { MailOutline, KeyOutline, ArrowForwardOutline as ArrowForward } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { sendVerificationCode, bindEmail } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const sending = ref(false)
const countdown = ref(0)

const formData = reactive({
  email: '',
  code: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userStore.user) {
    if (userStore.user.isEmailVerified) {
      message.success('邮箱已验证')
      router.push('/home')
    } else {
      formData.email = userStore.user.email || ''
    }
  } else {
    router.push('/login')
  }
})

async function handleSendCode() {
  if (!formData.email) {
    message.warning('请先输入邮箱')
    return
  }
  const emailRegex = /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(formData.email)) {
    message.warning('邮箱格式不正确')
    return
  }

  try {
    sending.value = true
    await sendVerificationCode(formData.email)
    message.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    message.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function handleVerify() {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    await bindEmail({
      email: formData.email,
      code: formData.code
    })
    
    // 更新本地用户信息
    await userStore.fetchUser()
    
    message.success('验证成功！')
    router.push('/home')
  } catch (e) {
    message.error(e.message || '验证失败')
  } finally {
    loading.value = false
  }
}

async function handleLogout() {
  await userStore.logout()
}
</script>

<style scoped>
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
}

.login-content {
  position: relative;
  z-index: 10;
}

.sketch-card {
  width: 380px;
  background: #ffffff;
  border: 3px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  padding: 40px 32px;
  position: relative;
  box-shadow: 10px 10px 0px rgba(44, 62, 80, 0.1);
}

.login-card-rotate {
  transform: rotate(-1deg);
}

.tape-strip {
  position: absolute;
  height: 35px;
  width: 120px;
  background: rgba(254, 243, 199, 0.6);
  top: -15px;
  left: 50%;
  transform: translateX(-50%) rotate(2deg);
  z-index: 10;
}

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
}

.hand-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 32px;
  margin: 0;
}

.underline-sketch {
  height: 4px;
  width: 100px;
  background: #ff6b6b;
  margin: 4px auto 8px;
  border-radius: 100% 0% 100% 5% / 10% 100% 5% 100%;
  transform: rotate(-1deg);
}

.hand-subtitle {
  color: #6b7280;
  margin: 0;
}

.sketch-label {
  display: block;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 6px;
}

/* Naive UI Overrides */
:deep(.sketch-input .n-input-wrapper) {
  border: 2px solid #94a3b8;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  background: #f8fafc;
}

:deep(.sketch-input.n-input--focus .n-input-wrapper) {
  border-color: #2c3e50;
  background: #fff;
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
}

:deep(.n-input__input-el) {
  font-family: 'Patrick Hand', cursive !important;
  font-size: 18px !important;
}

/* Button */
.sketch-button {
  width: 100%;
  height: 56px;
  border: 3px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  background: #ff6b6b;
  color: #fff;
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 4px 4px 0px #2c3e50;
  transition: all 0.2s;
  margin-top: 24px;
}

.sketch-button:hover:not(:disabled) {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px #2c3e50;
}

.sketch-button:disabled {
  background: #94a3b8;
  border-color: #64748b;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logout-link {
  text-align: center;
  margin-top: 16px;
  color: #6b7280;
  cursor: pointer;
  text-decoration: underline;
}

.code-input-row {
  display: flex;
  gap: 12px;
}

.small-btn {
  width: 100px;
  height: 48px; /* Slightly shorter than input */
  margin-top: 0;
  font-size: 18px;
  background: #10b981;
}

.small-btn:hover:not(:disabled) {
  background: #059669;
}
</style>
