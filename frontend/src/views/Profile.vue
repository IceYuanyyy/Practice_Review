<template>
  <div class="anti-profile">
    <div class="profile-content">
      <!-- 1. Header: Access Card (User Info) -->
      <div class="access-card-section">
        <h3 class="section-label">ACCESS_CARD // 通行证</h3>
        <div class="id-badge">
          <div class="badge-header">
            <span class="clearance-level">LEVEL 1 CLEARANCE</span>
            <span class="badge-chip"></span>
          </div>
          <div class="badge-body">
            <div class="avatar-zone">
              <div class="avatar-placeholder">
                <span class="avatar-icon">☺</span>
              </div>
              <div class="barcode">||| || ||| | ||</div>
            </div>
            <div class="info-zone">
              <div class="info-row">
                <label>CODENAME / 用户名</label>
                <div class="info-val">{{ userStore.username }}</div>
              </div>
              
              <!-- Editable Nickname -->
              <div class="info-row">
                <label>ALIAS / 昵称</label>
                <div v-if="!isEditing" class="info-val highlight">{{ userStore.nickname || 'UNKNOWN' }}</div>
                <input v-else v-model="formData.nickname" class="terminal-input" placeholder="ENTER ALIAS" />
              </div>

              <!-- Editable Email -->
              <div class="info-row">
                <label>CONTACT / 邮箱</label>
                <div v-if="!isEditing" class="info-val">{{ userStore.user?.email || 'N/A' }}</div>
                <input v-else v-model="formData.email" class="terminal-input" placeholder="ENTER MAIL" />
              </div>

              <div class="info-row">
                <label>ROLE / 权限组</label>
                <div class="role-tag" :class="userStore.isAdmin ? 'admin' : 'user'">
                  {{ userStore.isAdmin ? 'ADMINISTRATOR' : 'OPERATIVE' }}
                </div>
              </div>
            </div>
          </div>
          <div class="badge-footer">
            <button class="action-btn" @click="handleEdit" :disabled="saving">
              {{ isEditing ? 'SAVE_CHANGES [保存]' : 'UPDATE_DOSSIER [编辑]' }}
            </button>
            <div class="stamp">VERIFIED</div>
          </div>
        </div>
      </div>

      <div class="row-layout">
        <!-- 2. Gamification: Achievement Stickers -->
        <div class="stickers-section">
          <h3 class="section-label">ACHIEVEMENTS // 成就贴纸</h3>
          <div class="laptop-lid">
            <div class="sticker sticker-1" title="Bug Hunter">
              <span class="icon">🐛</span>
              <span class="text">BUG_HUNTER</span>
            </div>
            <div class="sticker sticker-2" title="Night Owl">
              <span class="icon">🌙</span>
              <span class="text">NIGHT_OWL</span>
            </div>
            <div class="sticker sticker-3" title="Streak Master">
              <span class="icon">🔥</span>
              <span class="text">STREAK_7</span>
            </div>
            <div class="sticker sticker-4">
              <span class="icon">⚡</span>
              <span class="text">EARLY_BIRD</span>
            </div>
          </div>
        </div>

        <!-- 3. System Config -->
        <div class="config-section">
          <h3 class="section-label">SYS_CONFIG // 系统配置</h3>
          <div class="config-panel">
            <div class="toggle-row">
              <span>HAPTIC_FEEDBACK // 触感反馈</span>
              <div class="toggle-switch active">
                <div class="knob"></div>
              </div>
            </div>
            <div class="toggle-row">
              <span>GLITCH_FX // 故障特效</span>
              <div class="toggle-switch active">
                <div class="knob"></div>
              </div>
            </div>
            <div class="toggle-row">
              <span>AUDIO_OUT // 音效</span>
              <div class="toggle-switch">
                <div class="knob"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. Security Console -->
      <div class="security-section">
        <h3 class="section-label">KEYCARD_RECODE // 密钥重置</h3>
        <div class="terminal-form">
          <div class="form-group">
            <label>CURRENT_KEY // 原密码</label>
            <input 
              v-model="passwordForm.oldPassword" 
              type="password" 
              class="terminal-input"
              placeholder="******" 
            />
          </div>
          <div class="form-group">
            <label>NEW_KEY // 新密码</label>
            <input 
              v-model="passwordForm.newPassword" 
              type="password" 
              class="terminal-input"
              placeholder="Min 6 chars" 
            />
          </div>
          <div class="form-group">
            <label>CONFIRM_KEY // 确认密码</label>
            <input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              class="terminal-input"
              placeholder="Confirm new key" 
            />
          </div>
          <button class="action-btn warning-btn" @click="handleChangePassword" :disabled="changingPassword">
            EXECUTE_RECODE // 修改密码
          </button>
        </div>
      </div>

      <!-- 5. Danger Zone -->
      <div class="danger-section">
        <button class="destruct-btn" @click="handleLogout">
          <div class="stripes"></div>
          <span class="btn-text">INITIATE_SELF_DESTRUCT // 退出登录</span>
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const isEditing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)

// 编辑表单
const formData = reactive({
  nickname: '',
  email: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  formData.nickname = userStore.nickname || ''
  formData.email = userStore.user?.email || ''
})

// 编辑/保存
async function handleEdit() {
  if (isEditing.value) {
    try {
      saving.value = true
      await request({
        url: '/user/profile',
        method: 'put',
        data: {
          id: userStore.user?.id,
          nickname: formData.nickname,
          email: formData.email
        }
      })
      message.success('ARCHIVE UPDATED // 档案已更新')
      await userStore.fetchUser()
      isEditing.value = false
    } catch (e) {
      console.error(e)
      message.error('UPDATE FAILED // 更新失败')
    } finally {
      saving.value = false
    }
  } else {
    isEditing.value = true
  }
}

// 修改密码
async function handleChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.warning('MISSING_INPUT // 请输入完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('KEY_MISMATCH // 密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    message.error('WEAK_KEY // 密码太短')
    return
  }

  try {
    changingPassword.value = true
    await request({
      url: '/user/password',
      method: 'put',
      data: {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      }
    })
    message.success('RECODE SUCCESS // 密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    console.error(e)
    message.error('RECODE FAILED // 修改失败')
  } finally {
    changingPassword.value = false
  }
}

// Logout
function handleLogout() {
  userStore.logout()
  message.success('SYSTEM SHUTDOWN // 已退出')
  router.push('/login')
}
</script>

<style scoped>
/* Profile Anti-Design Variables */
.anti-profile {
  --neon-yellow: #F5E400;
  --neon-cyan: #00E5FF;
  --neon-magenta: #FF3EA5;
  --void-black: #050505;
  --off-white: #FFFDF7;
  
  font-family: 'Courier New', Courier, monospace;
  min-height: 100vh;
  padding: 40px 20px;
  color: var(--void-black);
  
  /* Inherit Background from Layout */
  background: transparent;
}

.profile-content {
  max-width: 800px;
  margin: 0 auto;
}

.section-label {
  font-family: Impact, sans-serif;
  font-size: 24px;
  letter-spacing: 1px;
  background: var(--void-black);
  color: var(--neon-yellow);
  padding: 4px 12px;
  display: inline-block;
  transform: skew(-10deg);
  margin-bottom: 20px;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.2);
}

/* 1. Access Card */
.access-card-section { margin-bottom: 50px; }
.id-badge {
  background: #f0f0f0;
  border: 4px solid var(--void-black);
  padding: 0;
  box-shadow: 10px 10px 0 var(--void-black);
  position: relative;
}

.badge-header {
  background: var(--void-black);
  color: white;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.clearance-level { font-weight: bold; letter-spacing: 2px; }
.badge-chip {
  width: 40px; height: 25px;
  background: repeating-linear-gradient(90deg, #d4af37, #d4af37 2px, #f9d868 4px);
  border-radius: 4px;
  border: 1px solid #999;
}

.badge-body {
  padding: 30px;
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.avatar-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.avatar-placeholder {
  width: 120px; height: 140px;
  background: #ddd;
  border: 2px solid var(--void-black);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  filter: grayscale(100%);
}
.barcode {
  font-family: 'Libre Barcode 39', cursive; /* Fallback if not avail */
  font-size: 24px;
  letter-spacing: 4px;
  height: 30px;
  overflow: hidden;
  white-space: nowrap;
}

.info-zone {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-row {
  display: flex;
  flex-direction: column;
}
.info-row label {
  font-size: 10px;
  color: #666;
  font-weight: bold;
}
.info-val {
  font-size: 20px;
  font-weight: bold;
  border-bottom: 2px solid var(--void-black);
  padding-bottom: 4px;
}
.highlight { color: var(--neon-magenta); }

.role-tag {
  display: inline-block;
  padding: 4px 8px;
  font-size: 14px;
  font-weight: bold;
  border: 2px solid var(--void-black);
  margin-top: 4px;
}
.role-tag.admin { background: var(--neon-yellow); }
.role-tag.user { background: var(--neon-cyan); }

.badge-footer {
  padding: 20px 30px;
  border-top: 2px dashed #999;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stamp {
  font-family: Impact, sans-serif;
  color: red;
  border: 4px solid red;
  padding: 5px 10px;
  font-size: 20px;
  transform: rotate(-15deg);
  opacity: 0.7;
}

/* 2. & 3. Row Layout */
.row-layout {
  display: flex;
  gap: 40px;
  margin-bottom: 50px;
  flex-wrap: wrap;
}
.stickers-section, .config-section { flex: 1; min-width: 300px; }

/* Stickers */
.laptop-lid {
  background: #222;
  border: 4px solid #000;
  border-radius: 12px;
  padding: 20px;
  min-height: 200px;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.8);
}
.sticker {
  position: absolute;
  background: white;
  border: 2px solid var(--void-black);
  padding: 8px;
  text-align: center;
  font-weight: bold;
  font-size: 12px;
  box-shadow: 2px 2px 5px rgba(0,0,0,0.5);
  cursor: help;
  transition: transform 0.2s;
}
.sticker:hover { transform: scale(1.1) !important; z-index: 10; }
.sticker .icon { display: block; font-size: 24px; }

/* Sticker Placements */
.sticker-1 { top: 20px; left: 20px; transform: rotate(-10deg); background: var(--neon-cyan); }
.sticker-2 { top: 40px; right: 30px; transform: rotate(5deg); background: var(--neon-yellow); }
.sticker-3 { bottom: 30px; left: 60px; transform: rotate(15deg); background: var(--neon-magenta); color: white; }
.sticker-4 { bottom: 50px; right: 50px; transform: rotate(-5deg); border-radius: 50%; }

/* Config */
.config-panel {
  background: white;
  border: 3px solid var(--void-black);
  padding: 20px;
  box-shadow: 8px 8px 0 var(--void-black);
}
.toggle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: 14px;
}
.toggle-switch {
  width: 50px; height: 26px;
  background: #ccc;
  border: 2px solid black;
  border-radius: 20px;
  position: relative;
  cursor: pointer;
}
.toggle-switch.active { background: var(--neon-cyan); }
.knob {
  width: 20px; height: 20px;
  background: white;
  border: 2px solid black;
  border-radius: 50%;
  position: absolute;
  top: 1px; left: 2px;
  transition: left 0.2s;
}
.toggle-switch.active .knob { left: 24px; }

/* 4. Security Console */
.security-section { margin-bottom: 50px; }
.terminal-form {
  background: var(--void-black);
  color: var(--neon-cyan);
  padding: 30px;
  border: 4px solid #333;
  font-family: monospace;
}
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 12px; color: #888; }

.terminal-input {
  width: 100%;
  background: #111;
  border: 1px solid #444;
  color: white;
  padding: 10px;
  font-family: monospace;
  outline: none;
}
.terminal-input:focus {
  border-color: var(--neon-cyan);
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.2);
}

.action-btn {
  background: var(--neon-cyan);
  border: 2px solid var(--void-black);
  padding: 10px 20px;
  font-weight: 900;
  cursor: pointer;
  text-transform: uppercase;
  transition: all 0.1s;
}
.action-btn:hover { background: #fff; transform: translate(-2px, -2px); box-shadow: 4px 4px 0 var(--void-black); }
.action-btn:active { transform: translate(0, 0); box-shadow: none; }

.warning-btn {
  background: var(--neon-magenta);
  color: white;
  width: 100%;
  margin-top: 10px;
}

/* 5. Danger Zone */
.danger-section { text-align: center; padding-bottom: 40px; }
.destruct-btn {
  background: #ff0000;
  color: white;
  border: 4px solid black;
  padding: 20px 40px;
  font-size: 20px;
  font-family: Impact, sans-serif;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
  transition: transform 0.1s;
}
.destruct-btn:hover { transform: scale(1.05); }
.destruct-btn:active { transform: scale(0.95); }

.stripes {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: repeating-linear-gradient(
    45deg,
    rgba(0,0,0,0.3),
    rgba(0,0,0,0.3) 10px,
    transparent 10px,
    transparent 20px
  );
  pointer-events: none;
}
.btn-text { position: relative; z-index: 2; letter-spacing: 2px; }

@media (max-width: 768px) {
  .badge-body { flex-direction: column; align-items: center; text-align: center; }
  .info-val { text-align: center; }
  .row-layout { flex-direction: column; }
}
</style>
