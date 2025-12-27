<template>
  <div class="case-file-container">
    <div class="folder-tab">CONFIDENTIAL</div>
    <div class="manila-folder">
      <div class="casebook-header">
        <h1 class="case-title">CASE FILES: LOGIN LOGS</h1>
        <div class="evidence-tag">EVIDENCE #842</div>
      </div>

      <!-- Loading State: Magnifying Glass -->
      <div v-if="loading" class="detective-loading">
        <div class="magnifying-glass">
          <div class="glass"></div>
          <div class="handle"></div>
        </div>
        <p class="loading-text">SCANNING ARCHIVES...</p>
      </div>

      <!-- Data List: Typewriter List -->
      <div v-else class="evidence-board">
        <transition-group name="typewriter-list" tag="div" class="strip-container">
          <div v-for="log in logs" :key="log.id" class="evidence-strip">
            
            <!-- TIME (Left) -->
            <div class="strip-time">
              <span class="time-face">{{ formatTime(log.loginTime) }}</span>
              <span class="date-face">{{ formatDate(log.loginTime) }}</span>
            </div>

            <!-- CONTEXT (Center) -->
            <div class="strip-context">
              <div class="suspect-line">
                <span class="label">SUBJECT:</span>
                <span class="value">{{ log.username }}</span>
              </div>
              <div class="location-line">
                <span class="label">LOC:</span>
                <span class="value">{{ log.loginLocation || 'UNKNOWN' }}</span>
                <span class="ip-tag">IP: {{ log.loginIp }}</span>
              </div>
              <div class="meta-line">
                <span class="value monospace">{{ log.browser }} // {{ log.os }}</span>
              </div>
            </div>

            <!-- STATUS (Right) -->
            <div class="strip-status">
              <div v-if="log.loginStatus === 1" class="stamp-granted">
                <div class="stamp-border">
                  GRANTED
                </div>
              </div>
              <div v-else class="stamp-denied">
                <div class="denied-text">REJECTED</div>
                <div class="reason-scribble">{{ log.failReason || 'SECURITY PROTOCOL' }}</div>
              </div>
            </div>

          </div>
        </transition-group>

        <!-- Pagination -->
        <div class="case-pagination">
          <n-pagination
            :page="pagination.page"
            :page-count="Math.ceil(pagination.itemCount / pagination.pageSize)"
            @update:page="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { NPagination } from 'naive-ui'
import request from '@/api/request'

const loading = ref(false)
const logs = ref([])

const pagination = ref({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

onMounted(() => {
  fetchLogs()
})

async function fetchLogs() {
  loading.value = true
  logs.value = [] 
  try {
    const res = await request.get('/admin/login-logs', {
      params: {
        page: pagination.value.page,
        size: pagination.value.pageSize
      }
    })
    setTimeout(() => {
        logs.value = res.data.records
        pagination.value.itemCount = res.data.total
        loading.value = false
    }, 600)
    
  } catch (e) {
    console.error('Failed to open case files', e)
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.value.page = page
  fetchLogs()
}

function formatTime(dateTime) {
  if (!dateTime) return '00:00';
  return dateTime.split(' ')[1];
}

function formatDate(dateTime) {
    if (!dateTime) return 'YYYY-MM-DD';
    return dateTime.split(' ')[0];
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Courier+Prime:wght@400;700&family=Permanent+Marker&display=swap');

/* Main Container */
.case-file-container {
  padding: 20px;
  position: relative;
  max-width: 1000px;
  margin: 0 auto;
}

.folder-tab {
  width: 200px;
  height: 40px;
  background: #fdf5e6; /* Old Paper */
  border: 3px solid #3e2723;
  border-bottom: none;
  border-radius: 10px 10px 0 0;
  text-align: center;
  font-family: 'Courier Prime', monospace;
  font-weight: bold;
  color: #d32f2f; /* Red Ink */
  line-height: 36px;
  margin-left: 20px;
  position: relative;
  top: 3px;
  z-index: 1;
}

.manila-folder {
  background: #fdf5e6;
  border: 3px solid #3e2723;
  padding: 30px;
  min-height: 600px;
  box-shadow: 10px 10px 0 rgba(0,0,0,0.2);
  position: relative;
  z-index: 2;
  /* Paper Texture */
  background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100' height='100' filter='url(%23noise)' opacity='0.08'/%3E%3C/svg%3E");
}

/* Header */
.casebook-header {
  border-bottom: 2px dashed #3e2723;
  padding-bottom: 20px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.case-title {
  font-family: 'Courier Prime', monospace;
  font-size: 2.5rem;
  color: #3e2723;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: -1px;
}

.evidence-tag {
  font-family: 'Permanent Marker', cursive;
  color: #d32f2f;
  font-size: 1.5rem;
  transform: rotate(-5deg);
  border: 2px solid #d32f2f;
  padding: 5px 10px;
  border-radius: 5px;
}

/* List Items */
.evidence-strip {
  display: flex;
  background: rgba(255,255,255,0.5);
  border-bottom: 1px solid #aaa;
  margin-bottom: 10px;
  padding: 15px;
  align-items: center;
  font-family: 'Courier Prime', monospace;
  transition: all 0.2s;
}

.evidence-strip:hover {
  background: #fff;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  transform: scale(1.01);
}

.strip-time {
  display: flex;
  flex-direction: column;
  width: 120px;
  border-right: 1px solid #ccc;
  padding-right: 15px;
  margin-right: 20px;
  text-align: right;
  color: #555;
}

.time-face { font-size: 1.2rem; font-weight: bold; }
.date-face { font-size: 0.8rem; }

.strip-context {
  flex: 1;
}

.label { color: #888; font-size: 0.9rem; margin-right: 5px; }
.value { color: #000; font-weight: bold; font-size: 1.1rem; }
.monospace { font-family: 'Courier New', monospace; font-size: 0.9rem; color: #444; }

.ip-tag {
  background: #eee;
  padding: 2px 5px;
  border-bottom: 1px solid #999;
  font-size: 0.8rem;
  margin-left: 10px;
}

/* Stamps */
.strip-status {
  width: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stamp-granted .stamp-border {
  border: 3px double #2e7d32;
  color: #2e7d32;
  padding: 5px 10px;
  font-weight: bold;
  transform: rotate(-10deg);
  font-size: 1.2rem;
  opacity: 0.8;
  mask-image: url("data:image/svg+xml;utf8,<svg ...>");
}

.stamp-denied {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.denied-text {
  color: #c62828;
  border: 4px solid #c62828;
  padding: 2px 8px;
  font-weight: 900;
  font-size: 1.4rem;
  transform: rotate(15deg);
  opacity: 0.9;
}

.reason-scribble {
  font-family: 'Permanent Marker', cursive;
  color: #c62828;
  font-size: 0.8rem;
  margin-top: 5px;
}

/* Loading */
.detective-loading {
  text-align: center;
  padding: 50px;
  color: #5d4037;
  font-family: 'Courier Prime', monospace;
}
.magnifying-glass {
  width: 60px; height: 60px;
  border: 5px solid #5d4037;
  border-radius: 50%;
  margin: 0 auto 20px;
  position: relative;
  animation: search 2s infinite;
}
.handle {
  width: 30px; height: 8px; background: #5d4037;
  position: absolute; bottom: -5px; right: -20px;
  transform: rotate(45deg);
}
@keyframes search {
  0% { transform: translate(-20px, 0); }
  50% { transform: translate(20px, 0); }
  100% { transform: translate(-20px, 0); }
}

/* Pagination */
.case-pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

:deep(.n-pagination .n-pagination-item) {
  font-family: 'Courier Prime', monospace;
  border: 1px solid #3e2723 !important;
  color: #3e2723 !important;
  background: transparent !important;
}

:deep(.n-pagination .n-pagination-item--active) {
  background: #3e2723 !important;
  color: #fdf5e6 !important;
}
</style>
