<template>
  <div class="register-page" :class="{ 'show-password': showPassword }">
    <div class="shell">
      <AuthWavesBackground />
      <form @submit.prevent="handleRegister">
        <h2>REGISTER</h2>
        <div class="form-item">
          <label for="username">Username</label>
          <div class="input-wrapper">
            <input
              type="text"
              id="username"
              v-model="registerForm.username"
              placeholder="请输入用户名"
              required
            />
          </div>
        </div>
        <div class="form-item">
          <label for="email">Email</label>
          <div class="input-wrapper">
            <input
              type="email"
              id="email"
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              required
            />
          </div>
        </div>
        <div class="form-item">
          <label for="verificationCode">验证码</label>
          <div class="input-wrapper code-wrapper">
            <input
              type="text"
              id="verificationCode"
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              maxlength="6"
              required
            />
            <button
              type="button"
              class="send-code-btn"
              @click="handleSendCode"
              :disabled="sendingCode || countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </div>
        </div>
        <div class="form-item">
          <label for="nickname">Nickname (可选)</label>
          <div class="input-wrapper">
            <input
              type="text"
              id="nickname"
              v-model="registerForm.nickname"
              placeholder="请输入昵称（可选）"
            />
          </div>
        </div>
        <div class="form-item">
          <label for="phone">Phone (可选)</label>
          <div class="input-wrapper">
            <input
              type="tel"
              id="phone"
              v-model="registerForm.phone"
              placeholder="请输入手机号（可选）"
            />
          </div>
        </div>
        <div class="form-item">
          <label for="gender">Gender (可选)</label>
          <div class="gender-wrapper">
            <label class="radio-label" :class="{ active: registerForm.gender === 0 }">
              <input type="radio" name="gender" v-model="registerForm.gender" :value="0" />
              <span>未知</span>
            </label>
            <label class="radio-label" :class="{ active: registerForm.gender === 1 }">
              <input type="radio" name="gender" v-model="registerForm.gender" :value="1" />
              <span>男</span>
            </label>
            <label class="radio-label" :class="{ active: registerForm.gender === 2 }">
              <input type="radio" name="gender" v-model="registerForm.gender" :value="2" />
              <span>女</span>
            </label>
          </div>
        </div>
        <div class="form-item">
          <label for="password">Password</label>
          <div class="input-wrapper">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              ref="passwordInput"
              v-model="registerForm.password"
              placeholder="请输入密码"
              required
            />
            <button type="button" id="eyeball" @click="togglePassword">
              <div class="eye"></div>
            </button>
            <div id="beam" ref="beam"></div>
          </div>
        </div>
        <button id="submit" type="submit" :disabled="loading">
          {{ loading ? '注册中...' : 'Sign up' }}
        </button>
        <div class="actions">
          <router-link to="/login" class="link">已有账号？去登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AuthWavesBackground from '@/components/common/AuthWavesBackground.vue'
import { register, sendVerificationCode } from '@/api/system/user'

const router = useRouter()

const showPassword = ref(false)
const passwordInput = ref(null)
const beam = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  nickname: '',
  phone: '',
  gender: 0,
  loginIp: '', // 用户IP地址
  verificationCode: '' // 邮箱验证码
})

// 获取用户IP地址
const getUserIP = async () => {
  try {
    // 尝试多个IP查询API，提高成功率
    const apis = [
      'https://api.ipify.org?format=json',
      'https://api.ip.sb/jsonip',
      'https://ipapi.co/json/'
    ]

    for (const api of apis) {
      try {
        const response = await fetch(api, { timeout: 5000 })
        const data = await response.json()
        const ip = data.ip || data.query
        if (ip) {
          registerForm.loginIp = ip
          console.log('✅ 成功获取用户IP:', ip)
          return
        }
      } catch (err) {
        console.warn(`❌ API ${api} 获取失败:`, err.message)
        continue
      }
    }

    console.warn('⚠️ 所有IP获取API都失败，后端将从请求头获取')
    // 设置为null而不是空字符串，让后端知道需要从请求头获取
    registerForm.loginIp = null
  } catch (error) {
    console.error('获取IP地址异常:', error)
    registerForm.loginIp = null
  }
}

// 切换密码显示/隐藏
const togglePassword = () => {
  showPassword.value = !showPassword.value
  passwordInput.value?.focus()
}

// 鼠标移动事件处理
const handleMouseMove = e => {
  if (!beam.value) return

  const rect = beam.value.getBoundingClientRect()
  const mouseX = rect.right + rect.width / 2
  const mouseY = rect.top + rect.height / 2
  const rad = Math.atan2(mouseX - e.pageX, mouseY - e.pageY)
  const degrees = rad * (20 / Math.PI) * -1 - 350

  document.documentElement.style.setProperty('--beamDegrees', `${degrees}deg`)
}

// 发送验证码
const handleSendCode = async () => {
  if (!registerForm.email || registerForm.email.trim() === '') {
    ElMessage.warning('请先输入邮箱地址')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  sendingCode.value = true

  try {
    const res = await sendVerificationCode(registerForm.email, 'register')
    
    if (res.code === 200) {
      ElMessage.success('✅ 验证码已发送，请查收邮件')
      // 开始倒计时（60秒）
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    console.error('验证码发送失败:', error)
    ElMessage.error('❌ 验证码发送失败，请重试')
  } finally {
    sendingCode.value = false
  }
}

// 注册处理
const handleRegister = async () => {
  if (!registerForm.username || !registerForm.email || !registerForm.password || !registerForm.verificationCode) {
    ElMessage.warning('请填写完整信息（包括验证码）')
    return
  }

  // 简单的邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.email)) {
    ElMessage.error('请输入正确的邮箱格式')
    return
  }

  // 密码长度验证
  if (registerForm.password.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }

  // 用户名长度验证
  if (registerForm.username.length < 3 || registerForm.username.length > 20) {
    ElMessage.error('用户名长度必须在3-20之间')
    return
  }

  // 手机号验证（如果填写）
  if (registerForm.phone && !/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.error('请输入正确的手机号格式')
    return
  }

  // 验证码长度验证
  if (registerForm.verificationCode.length !== 6) {
    ElMessage.error('验证码必须为6位数字')
    return
  }

  loading.value = true

  try {
    await register(registerForm)
    ElMessage.success('✅ 注册成功！请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  document.documentElement.addEventListener('mousemove', handleMouseMove)
  // 获取用户IP地址
  getUserIP()
})

onUnmounted(() => {
  document.documentElement.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style lang="scss" scoped>
* {
  box-sizing: border-box;
  transition: 0.2s;
}

:root {
  --bgColor: white;
  --border: black;
  --inputColor: black;
  --outlineColor: rgb(60, 115, 235);
}

.register-page {
  --bgColor: rgba(255, 255, 255, 0.98);
  --border: black;
  --inputColor: black;
  --outlineColor: rgb(60, 115, 235);

  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  overflow: hidden;

  &.show-password {
    --bgColor: rgba(0, 0, 0, 0.95);
    --inputColor: white;
    --border: rgb(255, 255, 255);

    .shell {
      &::before {
        opacity: 0.3;
      }
    }

    input {
      box-shadow: inset 0 0 0 2px black;
      border: 2px dashed white;
    }

    input:focus {
      outline: none;
      border-color: rgb(255, 255, 145);
    }

    #beam {
      background: rgb(255, 255, 145);
    }
  }
}

.shell {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f40c3f;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 50% 50%, transparent 0%, rgba(0, 0, 0, 0.2) 100%);
    pointer-events: none;
    z-index: 2;
    transition: opacity 0.5s;
  }
}

form {
  position: relative;
  z-index: 100;
  transform: translate3d(0, 0, 0);
  padding: 50px;
  border: 20px solid var(--border);
  border-radius: 10px;
  box-shadow:
    10px 10px 30px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  background: var(--bgColor);
  backdrop-filter: blur(10px);
  animation: formEntry 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);

  > * + * {
    margin-top: 15px;
  }
}

@keyframes formEntry {
  from {
    opacity: 0;
    transform: translate3d(0, 30px, 0) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }
}

.form-item {
  > * + * {
    margin-top: 0.5rem;
  }
}

h2,
label,
input,
button {
  font-size: 2rem;
  font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  color: var(--inputColor);
}

h2 {
  font-size: 4rem;
  margin: 0;
  font-weight: bold;
}

label:focus,
input:focus,
button:focus {
  outline-offset: 2px;
}

button {
  border: none;
}

#submit {
  cursor: pointer;
  width: 100%;
  margin: 20px 0 0 2px;
  padding: 0.75rem 1.25rem;
  color: var(--bgColor);
  background-color: var(--inputColor);
  box-shadow: 4px 4px 0 rgba(30, 144, 255, 0.2);
  border-radius: 5px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition:
      width 0.6s,
      height 0.6s;
  }

  &:hover {
    box-shadow: 6px 6px 0 rgba(30, 144, 255, 0.3);
    transform: translateY(-2px);

    &::before {
      width: 300px;
      height: 300px;
    }
  }

  &:active {
    transform: translateY(1px);
    box-shadow: 2px 2px 0 rgba(30, 144, 255, 0.2);
  }
}

.input-wrapper {
  position: relative;
}

input {
  padding: 0.75rem 4rem 0.75rem 0.75rem;
  width: 100%;
  border: 2px solid transparent;
  border-radius: 0;
  background-color: transparent;
  box-shadow:
    inset 0 0 0 2px black,
    inset 6px 6px 0 rgba(30, 144, 255, 0.2),
    3px 3px 0 rgba(30, 144, 255, 0.2);
  -webkit-appearance: none;

  &:focus {
    outline-offset: 1px;
  }

  &::placeholder {
    color: rgba(0, 0, 0, 0.3);
    font-size: 1.5rem;
  }
}

.show-password input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

// 眼睛按钮样式（只在密码框显示）
#eyeball {
  --size: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  outline: none;
  position: absolute;
  top: 50%;
  right: 0.75rem;
  border: none;
  background-color: transparent;
  transform: translateY(-50%);
  padding: 0.5rem;

  &:active {
    transform: translateY(calc(-50% + 1px));
  }
}

.eye {
  width: var(--size);
  height: var(--size);
  border: 2px solid var(--inputColor);
  border-radius: calc(var(--size) / 1.5) 0;
  transform: rotate(45deg);
  position: relative;

  &:before,
  &:after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    margin: auto;
    border-radius: 100%;
  }

  &:before {
    width: 35%;
    height: 35%;
    background-color: var(--inputColor);
  }

  &:after {
    width: 65%;
    height: 65%;
    border: 2px solid var(--inputColor);
    border-radius: 100%;
  }
}

// 光束样式
#beam {
  position: absolute;
  top: 50%;
  right: 1.75rem;
  clip-path: polygon(100% 50%, 100% 50%, 0 0, 0 100%);
  width: 100vw;
  height: 25vw;
  z-index: 1;
  mix-blend-mode: multiply;
  transition: transform 200ms ease-out;
  transform-origin: 100% 50%;
  transform: translateY(-50%) rotate(var(--beamDegrees, 0));
  pointer-events: none;
}

.gender-wrapper {
  display: flex;
  gap: 1.5rem;
  padding: 0.75rem 0;

  .radio-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    font-size: 1.6rem;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    border: 2px solid transparent;
    transition: all 0.3s ease;
    background: rgba(0, 0, 0, 0.05);

    input[type='radio'] {
      width: 1.5rem;
      height: 1.5rem;
      cursor: pointer;
      accent-color: var(--inputColor);
    }

    span {
      user-select: none;
    }

    &:hover {
      color: rgb(60, 115, 235);
      border-color: rgba(60, 115, 235, 0.3);
      background: rgba(60, 115, 235, 0.1);
    }

    // 选中状态的高亮效果
    &.active {
      color: rgb(60, 115, 235);
      border-color: rgb(60, 115, 235);
      background: rgba(60, 115, 235, 0.15);
      font-weight: 600;
      box-shadow: 0 0 10px rgba(60, 115, 235, 0.3);

      input[type='radio'] {
        accent-color: rgb(60, 115, 235);
      }
    }
  }
}

.show-password .gender-wrapper .radio-label {
  background: rgba(255, 255, 255, 0.1);

  input[type='radio'] {
    accent-color: white;
  }

  &:hover {
    color: rgb(255, 255, 145);
    border-color: rgba(255, 255, 145, 0.5);
    background: rgba(255, 255, 255, 0.15);
  }

  &.active {
    color: rgb(255, 255, 145);
    border-color: rgb(255, 255, 145);
    background: rgba(255, 255, 145, 0.2);
    box-shadow: 0 0 15px rgba(255, 255, 145, 0.4);

    input[type='radio'] {
      accent-color: rgb(255, 255, 145);
    }
  }
}

.actions {
  text-align: center;
  margin-top: 20px;

  .link {
    color: var(--inputColor);
    text-decoration: none;
    font-size: 1.2rem;
    transition: all 0.3s;

    &:hover {
      color: rgb(60, 115, 235);
      text-decoration: underline;
    }
  }
}

// 验证码输入框样式
.code-wrapper {
  display: flex;
  gap: 10px;

  input {
    flex: 1;
    padding: 0.75rem;
  }

  .send-code-btn {
    padding: 0.75rem 1.5rem;
    background: #67C23A;
    color: white;
    border: none;
    border-radius: 5px;
    font-size: 1.4rem;
    cursor: pointer;
    white-space: nowrap;
    transition: all 0.3s;
    font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
    min-width: 120px;

    &:hover:not(:disabled) {
      background: #5daf34;
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      background: #a0cfff;
      cursor: not-allowed;
      opacity: 0.7;
    }
  }
}
</style>
