<template>
  <div class="rogues-gallery-container">
    <div class="narrator-box">
      <div class="caption-label">MEANWHILE, IN THE DATABASE...</div>
      <n-input v-model:value="searchUsername" placeholder="SEARCH FOR VILLAIN..." class="comic-input" @keyup.enter="fetchUsers">
        <template #prefix>
          <n-icon :component="SearchOutline" />
        </template>
      </n-input>
    </div>

    <div v-if="loading" class="loading-state">
      <n-spin size="large" />
    </div>

    <div v-else class="rogues-grid">
      <div 
        v-for="user in users" 
        :key="user.id" 
        class="rogue-card"
        :class="{ 
          'is-disabled': user.status === 0,
          'is-vaporizing': vaporizingIds.includes(user.id)
        }"
      >
        <div class="card-header">
          <span class="id-badge">#{{ user.id }}</span>
          <div class="role-badge" :class="user.role">
            {{ user.role === 'admin' ? 'BOSS' : 'HENCHMAN' }}
          </div>
        </div>

        <div class="mugshot-area">
          <div class="user-avatar" :style="{ backgroundColor: getUserColor(user.id) }">
            {{ user.username.charAt(0).toUpperCase() }}
          </div>
          <div class="user-details">
            <h3 class="username">{{ user.username }}</h3>
            <p class="nickname">"{{ user.nickname || 'Unknown' }}"</p>
            <p class="email">{{ user.email }}</p>
          </div>
          
          <div v-if="user.status === 0" class="jailed-stamp">
            JAILED
          </div>
        </div>

        <div class="status-indicator">
          <div class="status-ring" :class="{ 'active': user.status === 1 }">
            {{ user.status === 1 ? 'ACTIVE' : 'INACTIVE' }}
          </div>
        </div>

        <div class="action-row">
          <button class="comic-btn btn-edit" @click="toggleStatus(user)">
            <n-icon :component="user.status === 1 ? LockClosedOutline : LockOpenOutline" />
            <span>{{ user.status === 1 ? 'LOCK UP' : 'FREE' }}</span>
          </button>
          
          <n-popconfirm @positive-click="resetPassword(user)">
            <template #trigger>
              <button class="comic-btn btn-reset">
                <n-icon :component="FolderOpenOutline" />
              </button>
            </template>
            RESET SECRET CODE?
          </n-popconfirm>

          <n-popconfirm @positive-click="deleteUser(user)">
             <template #trigger>
              <button class="comic-btn btn-delete">
                <n-icon :component="FlameOutline" />
              </button>
             </template>
             VAPORIZE THIS VILLAIN?
          </n-popconfirm>

          <button class="comic-btn btn-log" @click="handleViewRecords(user)" title="VIEW DATABASE">
            <n-icon :component="DocumentTextOutline" />
          </button>
        </div>
      </div>
    </div>

    <div class="comic-pagination">
      <n-pagination
        v-model:page="pagination.page"
        :page-size="pagination.pageSize"
        :item-count="pagination.itemCount"
        @update:page="handlePageChange"
      />
    </div>

    <!-- Records Modal -->
    <n-modal v-model:show="showRecordsModal" preset="card" title="VILLAIN DOSSIER" style="width: 1100px" class="comic-modal">
      <div v-if="currentRecordUser" class="dossier-header">
        Subject: <span class="dossier-name">{{ currentRecordUser.username }}</span>
      </div>
      <n-data-table
        :columns="recordColumns"
        :data="userRecords"
        :loading="recordsLoading"
        :pagination="false"
        :bordered="false"
        striped
      />
      <div class="dossier-pagination">
        <n-pagination
          v-model:page="recordsPagination.page"
          :item-count="recordsPagination.itemCount"
          :page-size="recordsPagination.pageSize"
          @update:page="handleRecordPageChange"
        />
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { useMessage, NInput, NIcon, NSpin, NPagination, NPopconfirm, NModal, NDataTable } from 'naive-ui'
import { 
  SearchOutline, 
  LockClosedOutline, 
  LockOpenOutline, 
  FolderOpenOutline, 
  FlameOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'
import request from '@/api/request'
import { getPracticeRecords } from '@/api/practice'

const message = useMessage()
const loading = ref(false)
const users = ref([])
const searchUsername = ref('')
const vaporizingIds = ref([])

const pagination = ref({
  page: 1,
  pageSize: 8, // Fits grid better
  itemCount: 0
})

onMounted(() => {
  fetchUsers()
})

function getUserColor(id) {
  const colors = ['#FF0000', '#00FF00', '#0000FF', '#FFFF00', '#FF00FF', '#00FFFF'];
  return colors[id % colors.length];
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
      params: {
        page: pagination.value.page,
        size: pagination.value.pageSize,
        username: searchUsername.value || undefined
      }
    })
    users.value = res.data.records
    pagination.value.itemCount = res.data.total
  } catch (e) {
    console.error('Failed to assemble roster', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.value.page = page
  fetchUsers()
}

async function toggleStatus(user) {
  const previousStatus = user.status;
  // Optimistic UI update
  user.status = user.status === 1 ? 0 : 1;
  
  try {
    await request.put(`/admin/users/${user.id}/status`, { status: user.status })
    message.success(user.status === 1 ? 'VILLAIN ESCAPED!' : 'VILLAIN JAILED!')
  } catch (e) {
    user.status = previousStatus; // Revert
    message.error('PLOT TWIST! ACTION FAILED.')
  }
}

async function resetPassword(user) {
  try {
    await request.put(`/admin/users/${user.id}/reset-password`, { newPassword: '123456' })
    message.success('SECRET FILES UPDATED (123456)')
  } catch (e) {
    message.error('ACCESS DENIED')
  }
}


async function deleteUser(user) {
  // 1. Trigger Animation
  vaporizingIds.value.push(user.id)
  
  // 2. Wait for animation
  setTimeout(async () => {
    try {
      await request.delete(`/admin/users/${user.id}`)
      message.success('VILLAIN VAPORIZED!')
      // Remove from list
      users.value = users.value.filter(u => u.id !== user.id)
    } catch (e) {
      if (e.response && e.response.status === 403) {
         message.error('YOU CANNOT VAPORIZE THE BOSS!')
      } else {
         message.error('VAPORIZER MALFUNCTION!')
      }
      // Revert animation state if failed (so it reappears)
      vaporizingIds.value = vaporizingIds.value.filter(id => id !== user.id)
    }
  }, 800); // 800ms matches animation duration
}

// === Records Log Logic ===

const showRecordsModal = ref(false)
const currentRecordUser = ref(null)
const userRecords = ref([])
const recordsLoading = ref(false)
const recordsPagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

const recordColumns = [
  { title: 'Time', key: 'practiceTime', width: 150, render: (row) => new Date(row.practiceTime).toLocaleString() },
  { title: 'ID', key: 'questionId', width: 60 },
  { title: 'Subject', key: 'questionSubject', width: 80 },
  { title: 'Type', key: 'questionType', width: 100 },
  { 
    title: 'Question', 
    key: 'questionContent',
    ellipsis: { tooltip: true },  // 超长内容显示 tooltip
    render: (row) => h(
      'span',
      { 
        style: { 
          maxWidth: '300px', 
          overflow: 'hidden', 
          textOverflow: 'ellipsis',
          display: 'inline-block',
          whiteSpace: 'nowrap'
        },
        title: row.questionContent  // 鼠标悬停时显示完整内容
      },
      row.questionContent || '-'
    )
  },
  { 
    title: 'Result', 
    key: 'isCorrect',
    width: 80,
    render: (row) => h(
      'span', 
      { style: { color: row.isCorrect ? '#10b981' : '#ef4444', fontWeight: 'bold' } }, 
      row.isCorrect ? 'CORRECT' : 'WRONG'
    ) 
  },
  { title: 'My Answer', key: 'userAnswer', width: 90 },
  { title: 'Right Answer', key: 'correctAnswer', width: 100 }
]

const handleViewRecords = (user) => {
  currentRecordUser.value = user
  recordsPagination.value.page = 1
  showRecordsModal.value = true
  fetchUserRecords()
}

const fetchUserRecords = async () => {
  if (!currentRecordUser.value) return
  recordsLoading.value = true
  try {
    const res = await getPracticeRecords({
      page: recordsPagination.value.page,
      size: recordsPagination.value.pageSize,
      userId: currentRecordUser.value.id
    })
    if (res.code === 200) {
      userRecords.value = res.data.records
      recordsPagination.value.itemCount = res.data.total
    }
  } catch (e) {
    message.error('Failed to fetch dossier.')
  } finally {
    recordsLoading.value = false
  }
}

const handleRecordPageChange = (page) => {
  recordsPagination.value.page = page
  fetchUserRecords()
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bangers&family=Permanent+Marker&display=swap');

.rogues-gallery-container {
  max-width: 1400px;
  padding: 20px;
}

/* --- Narrator Box (Search) --- */
.narrator-box {
  background: #fff9c4; /* Pale Yellow */
  border: 4px solid #000;
  padding: 15px;
  margin-bottom: 30px;
  box-shadow: 8px 8px 0 #000;
  max-width: 600px;
  transform: rotate(-1deg);
}

.caption-label {
  font-family: 'Permanent Marker', cursive;
  font-size: 1.2rem;
  margin-bottom: 10px;
  display: block;
}

.comic-input {
  font-family: 'Courier Prime', monospace;
  font-weight: bold;
}

/* --- Grid --- */
.rogues-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

/* --- Card Design --- */
.rogue-card {
  background: #fff;
  border: 3px solid #000;
  border-radius: 4px; /* Slight rounding for hand-drawn feel */
  box-shadow: 6px 6px 0 #000;
  position: relative;
  transition: all 0.2s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.rogue-card:hover {
  transform: translateY(-5px);
  box-shadow: 10px 10px 0 #000;
  z-index: 10;
}

/* Zebra Striping / Halftone */
.rogue-card:nth-child(even) {
  background-image: radial-gradient(#ddd 10%, transparent 11%), radial-gradient(#ddd 10%, transparent 11%);
  background-size: 10px 10px;
}

/* Disabled State */
.rogue-card.is-disabled {
  filter: grayscale(100%);
  opacity: 0.8;
  border-color: #555;
}

/* Vaporizing Animation */
.rogue-card.is-vaporizing {
  animation: vaporize 0.8s forwards;
  pointer-events: none;
}

@keyframes vaporize {
  0% { transform: scale(1) rotate(0); opacity: 1; filter: hue-rotate(0deg); }
  50% { transform: scale(0.8) rotate(20deg); opacity: 0.8; filter: hue-rotate(90deg) blur(2px); }
  100% { transform: scale(0) rotate(180deg); opacity: 0; filter: blur(10px); }
}

/* Header */
.card-header {
  background: #000;
  color: #fff;
  padding: 8px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'Bangers', cursive;
  font-size: 1.2rem;
  clip-path: polygon(0 0, 100% 0, 100% 85%, 0 100%);
  height: 50px;
}

.role-badge.admin { color: #ffff00; }
.role-badge.user { color: #ccc; }

/* Mugshot Area */
.mugshot-area {
  padding: 20px;
  text-align: center;
  position: relative;
  flex-grow: 1;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border: 3px solid #000;
  border-radius: 50%;
  margin: 0 auto 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Bangers';
  font-size: 3rem;
  color: #fff;
  text-shadow: 2px 2px 0 #000;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.2);
}

.username {
  font-family: 'Bangers', cursive;
  font-size: 1.8rem;
  margin: 0;
  line-height: 1;
}

.nickname {
  font-style: italic;
  color: #666;
  margin: 5px 0;
}

.email {
  font-family: monospace;
  font-size: 0.9rem;
  background: #eee;
  padding: 2px 5px;
  display: inline-block;
  transform: rotate(-1deg);
}

/* Jailed Stamp */
.jailed-stamp {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-15deg);
  border: 4px solid #d32f2f;
  color: #d32f2f;
  font-family: 'Black Ops One', cursive, sans-serif;
  font-size: 3rem;
  padding: 5px 20px;
  text-transform: uppercase;
  opacity: 0.8;
  mask-image: url("data:image/svg+xml;utf8,<svg ...>"); /* Ideally a grunge texture */
  pointer-events: none;
  font-weight: 900;
  letter-spacing: 5px;
}

/* Status Ring */
.status-indicator {
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}

.status-ring {
  background: #000;
  color: #fff;
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 0.8rem;
  border: 2px solid #555;
  transition: all 0.3s;
}

.status-ring.active {
  background: #fff;
  color: #2e7d32;
  border-color: #2e7d32;
  box-shadow: 0 0 10px #2e7d32; /* Green Lantern Glow */
}

/* Action Row */
.action-row {
  padding: 15px;
  border-top: 2px dashed #000;
  display: flex;
  justify-content: space-around;
  background: #f9f9f9;
}

.comic-btn {
  border: 2px solid #000;
  background: #fff;
  padding: 8px;
  cursor: pointer;
  box-shadow: 3px 3px 0 #000;
  transition: all 0.1s;
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: 'Bangers';
  font-size: 1rem;
}

.comic-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 5px 5px 0 #000;
}

.comic-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 #000;
}

.btn-edit { background: #ffeb3b; } /* Yellow */
.btn-reset { background: #b3e5fc; } /* Light Blue */
.btn-delete { background: #ffcdd2; color: #d32f2f; } /* Red */

.loading-state {
  text-align: center;
  padding: 50px;
}

.comic-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
