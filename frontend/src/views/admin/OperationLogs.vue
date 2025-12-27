<template>
  <div class="cosmic-light-container">
    <div class="comic-header">
      <h1 class="comic-title">ACTION LOGS</h1>
      <div class="comic-subtitle">SYSTEM HISTORY</div>
    </div>

    <!-- Data Table / Grid -->
    <div class="action-grid" v-if="!loading">
      <div 
        v-for="log in logs" 
        :key="log.id" 
        class="action-card"
        :class="getLogTheme(log)"
        @click="openDetail(log)"
      >
        <div class="card-content">
          <!-- Icon -->
          <div class="action-icon-circle">
            <n-icon :component="getIcon(log.operationType)" size="32" />
          </div>

          <div class="action-meta">
            <span class="timestamp">{{ formatTime(log.operationTime) }}</span>
            <h3 class="hero-name">{{ log.username }}</h3>
          </div>

          <div class="action-desc">
             {{ log.operationDesc }}
          </div>

          <div class="status-badge" :class="log.operationStatus === 1 ? 'status-ok' : 'status-err'">
            {{ log.operationStatus === 1 ? 'OK' : 'ERR' }}
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-else class="cosmic-loading">
      <n-spin size="large" />
      <p>LOADING SECTOR DATA...</p>
    </div>

    <!-- Pagination -->
    <div class="comic-pagination">
       <n-pagination
        :page="pagination.page"
        :page-count="Math.ceil(pagination.itemCount / pagination.pageSize)"
        @update:page="handlePageChange"
      />
    </div>

    <!-- Detail Modal -->
    <n-modal v-model:show="showDetail" transform-origin="center">
      <div class="comic-modal">
        <div class="modal-header">
           <h2>ACTION DETAILS #{{ selectedLog?.id }}</h2>
        </div>
        <div class="modal-body" v-if="selectedLog">
           <p><strong>USER:</strong> {{ selectedLog.username }}</p>
           <p><strong>TYPE:</strong> {{ selectedLog.operationType }}</p>
           <p><strong>IP:</strong> {{ selectedLog.requestIp }}</p>
           <p class="desc-box">{{ selectedLog.operationDesc }}</p>
           <div class="result-stamp" :class="selectedLog.operationStatus === 1 ? 'success' : 'fail'">
              {{ selectedLog.operationStatus === 1 ? 'SUCCESS' : 'FAILURE' }}
           </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { NModal, NIcon, NSpin, NPagination } from 'naive-ui'
import { 
  ConstructOutline, 
  SkullOutline,     
  FlashOutline,     
  CodeWorkingOutline 
} from '@vicons/ionicons5'
import request from '@/api/request'

const loading = ref(false)
const logs = ref([])
const showDetail = ref(false)
const selectedLog = ref(null)

const pagination = ref({
  page: 1,
  pageSize: 9, 
  itemCount: 0
})

onMounted(() => {
  fetchLogs()
})

async function fetchLogs() {
  loading.value = true
  try {
    const res = await request.get('/admin/operation-logs', {
      params: {
        page: pagination.value.page,
        size: pagination.value.pageSize
      }
    })
    logs.value = res.data.records
    pagination.value.itemCount = res.data.total
  } catch (e) {
    console.error('Failed to retrieve logs', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.value.page = page
  fetchLogs()
}

function openDetail(log) {
  selectedLog.value = log
  showDetail.value = true
}

function getIcon(type) {
  const t = type?.toLowerCase() || ''
  if (t.includes('update') || t.includes('modify')) return ConstructOutline
  if (t.includes('delete')) return SkullOutline
  if (t.includes('add') || t.includes('create')) return FlashOutline
  return CodeWorkingOutline
}

function getLogTheme(log) {
  const t = log.operationType?.toLowerCase() || ''
  if (t.includes('delete')) return 'theme-danger'
  if (t.includes('add') || t.includes('create')) return 'theme-active'
  return 'theme-neutral'
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(5, 16)
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bangers&family=Roboto+Mono:wght@700&display=swap');

.cosmic-light-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.comic-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
}

.comic-title {
  font-family: 'Bangers', cursive;
  font-size: 3.5rem;
  color: #fff;
  text-shadow: 
    3px 3px 0 #000,
    -1px -1px 0 #000,
    1px -1px 0 #000,
    -1px 1px 0 #000,
    1px 1px 0 #000;
  margin: 0;
  /* Since global bg is light, we need strong text shadow or color */
  color: #E23636;
}

.comic-subtitle {
  background: #000;
  color: #fff;
  display: inline-block;
  padding: 5px 15px;
  font-family: 'Roboto Mono', monospace;
  font-weight: bold;
  transform: rotate(-2deg);
  box-shadow: 4px 4px 0 rgba(0,0,0,0.2);
}

/* Grid */
.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.action-card {
  background: #fff;
  border: 4px solid #000;
  padding: 20px;
  position: relative;
  transition: all 0.2s;
  box-shadow: 8px 8px 0 rgba(0,0,0,0.1);
  cursor: pointer;
  overflow: hidden;
}

.action-card:hover {
  transform: translate(-5px, -5px);
  box-shadow: 12px 12px 0 rgba(0,0,0,0.2);
  z-index: 10;
}

/* Halftone Pattern Overlay for Card */
.action-card::before {
  content: '';
  position: absolute;
  top: 0; right: 0;
  width: 60px; height: 60px;
  background-image: radial-gradient(#000 20%, transparent 20%);
  background-size: 6px 6px;
  opacity: 0.1;
  pointer-events: none;
}

.card-content {
  position: relative;
  z-index: 2;
}

.action-icon-circle {
  width: 50px; height: 50px;
  border: 3px solid #000;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 3px 3px 0 #000;
}

.action-meta {
  border-bottom: 2px dashed #ccc;
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.timestamp {
  font-family: 'Roboto Mono', monospace;
  font-size: 0.8rem;
  color: #666;
  background: #eee;
  padding: 2px 5px;
}

.hero-name {
  font-family: 'Bangers', cursive;
  font-size: 1.5rem;
  margin: 5px 0 0;
  letter-spacing: 1px;
}

.action-desc {
  font-size: 0.95rem;
  color: #333;
  line-height: 1.4;
  margin-bottom: 30px; /* Space for badge */
}

.status-badge {
  position: absolute;
  bottom: 10px;
  right: 10px;
  font-weight: bold;
  font-family: 'Bangers';
  font-size: 1.2rem;
  transform: rotate(-10deg);
  border: 2px solid currentColor;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-ok { color: #2e7d32; }
.status-err { color: #c62828; }

/* Themes */
.theme-active .action-icon-circle { background: #E0F7FA; color: #006064; }
.theme-danger .action-icon-circle { background: #FFEBEE; color: #B71C1C; }
.theme-neutral .action-icon-circle { background: #F3E5F5; color: #4A148C; }

/* Pagination */
.comic-pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

/* Modal */
.comic-modal {
  background: #fff;
  border: 4px solid #000;
  width: 500px;
  box-shadow: 15px 15px 0 rgba(0,0,0,0.5);
  max-width: 90vw;
}

.modal-header {
  background: #000;
  color: #fff;
  padding: 15px;
}
.modal-header h2 {
  font-family: 'Bangers'; 
  margin: 0;
  font-size: 2rem;
  color: #FFD700;
}

.modal-body {
  padding: 20px;
}

.desc-box {
  background: #f5f5f5;
  border: 2px solid #000;
  padding: 10px;
  margin-top: 10px;
}

.result-stamp {
  margin-top: 20px;
  font-family: 'Bangers';
  font-size: 3rem;
  text-align: center;
  transform: rotate(-5deg);
  border: 5px solid currentColor;
  opacity: 0.5;
}
.result-stamp.success { color: #2e7d32; }
.result-stamp.fail { color: #c62828; }

.cosmic-loading {
  text-align: center;
  padding: 40px;
  font-family: 'Bangers';
  font-size: 1.5rem;
}
</style>
