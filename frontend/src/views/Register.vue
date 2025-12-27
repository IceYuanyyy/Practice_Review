<template>
  <div class="register-container">
    <div class="register-card">
      <!-- Logo 区域 -->
      <div class="logo-area">
        <div class="logo-icon">
          <n-icon :component="SchoolOutline" size="40" color="#fff" />
        </div>
        <h1 class="title">创建账号</h1>
        <p class="subtitle">加入我们，开始高效复习之旅</p>
      </div>

      <!-- 注册表单 -->
      <n-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules"
        label-placement="left"
        label-width="0"
        size="large"
      >
        <n-form-item path="username">
          <n-input 
            v-model:value="formData.username" 
            placeholder="请输入用户名（3-50个字符）"
            :input-props="{ autocomplete: 'username' }"
          >
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="nickname">
          <n-input 
            v-model:value="formData.nickname" 
            placeholder="请输入昵称（可选）"
          >
            <template #prefix>
              <n-icon :component="HappyOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="email">
          <n-input 
            v-model:value="formData.email" 
            placeholder="请输入邮箱（可选）"
            :input-props="{ autocomplete: 'email' }"
          >
            <template #prefix>
              <n-icon :component="MailOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input 
            v-model:value="formData.password" 
            type="password"
            placeholder="请输入密码（6-50个字符）"
            show-password-on="click"
            :input-props="{ autocomplete: 'new-password' }"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="confirmPassword">
          <n-input 
            v-model:value="formData.confirmPassword" 
            type="password"
            placeholder="请再次输入密码"
            show-password-on="click"
            :input-props="{ autocomplete: 'new-password' }"
            @keyup.enter="handleRegister"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item>
          <n-button 
            type="primary" 
            block 
            strong
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </n-button>
        </n-form-item>

        <n-form-item>
          <div class="form-footer">
            <span>已有账号？</span>
            <n-button text type="primary" @click="goToLogin">立即登录</n-button>
          </div>
        </n-form-item>
      </n-form>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NForm, NFormItem, NInput, NButton, NIcon } from 'naive-ui'
import { SchoolOutline, PersonOutline, LockClosedOutline, MailOutline, HappyOutline } from '@vicons/ionicons5'
import { register } from '@/api/auth'

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
  confirmPassword: ''
})

// 密码一致性验证
function validatePasswordSame(rule, value) {
  return value === formData.password
}

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度为6-50个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePasswordSame, message: '两次输入的密码不一致', trigger: 'blur' }
  ]
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
      email: formData.email || undefined
    })

    message.success('注册成功，请登录')
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

// 跳转到登录页
function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.register-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.logo-area {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 12px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.3);
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.form-footer {
  width: 100%;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: #fff;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: #fff;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: #fff;
  top: 50%;
  left: 10%;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .register-card {
    width: 90%;
    padding: 28px 20px;
  }
  
  .logo-icon {
    width: 60px;
    height: 60px;
  }
  
  .title {
    font-size: 20px;
  }
}
</style>
