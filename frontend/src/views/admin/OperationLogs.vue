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
          
          <!-- File Name Display -->
          <div v-if="getFileName(log)" class="file-name-tag">
            <n-icon :component="DocumentTextOutline" /> {{ getFileName(log) }}
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
      <div class="comic-modal" :style="{ width: (isImportLog || isConvertLog) ? '800px' : '500px' }">
        <div class="modal-header">
           <h2>ACTION DETAILS #{{ selectedLog?.id }}</h2>
        </div>
        <div class="modal-body" v-if="selectedLog">
           <div class="info-row">
             <p><strong>USER:</strong> {{ selectedLog.username }}</p>
             <p><strong>TYPE:</strong> {{ selectedLog.operationType }}</p>
           </div>
           <div class="info-row">
             <p><strong>IP:</strong> {{ selectedLog.requestIp }}</p>
             <p><strong>位置:</strong> {{ selectedLog.requestLocation || '未知位置' }}</p>
           </div>
           <div class="info-row">
             <p><strong>TIME:</strong> {{ formatTime(selectedLog.operationTime) }}</p>
           </div>
           
           <p class="desc-box">{{ selectedLog.operationDesc }}</p>
           
           <!-- Import Specific Details -->
           <div v-if="isImportLog" class="import-details">
              <n-divider dashed>IMPORTED DATA</n-divider>
              <n-space justify="space-between" align="center" style="margin-bottom: 10px;">
                <n-text><strong>File:</strong> {{ getFileName(selectedLog) }}</n-text>
                <n-space>
                   <n-button size="small" type="primary" @click="handleDownload(selectedLog)">
                      <template #icon><n-icon :component="CloudDownloadOutline"/></template>
                      Download Excel
                   </n-button>
                   <n-popconfirm @positive-click="handleDeleteBatch(selectedLog)">
                     <template #trigger>
                       <n-button size="small" type="error">
                          <template #icon><n-icon :component="TrashOutline"/></template>
                          Delete Batch
                       </n-button>
                     </template>
                     Are you sure to delete ALL questions from this import?
                   </n-popconfirm>
                </n-space>
              </n-space>
              
              <n-data-table
                :columns="questionColumns"
                :data="detailQuestions"
                :loading="detailLoading"
                :pagination="detailPagination"
                :max-height="300"
                size="small"
              />
           </div>

           <!-- Convert Specific Details -->
           <div v-if="isConvertLog" class="convert-details">
              <n-divider dashed>CONVERTED FILES</n-divider>
              <n-space vertical :size="12">
                <n-space justify="space-between" align="center">
                  <n-text><strong>原始文件:</strong> {{ selectedLog.sourceFileName || '无' }}</n-text>
                  <n-button 
                    size="small" 
                    type="info" 
                    :disabled="!selectedLog.sourceFileName"
                    @click="handleDownloadConvertSource(selectedLog)"
                  >
                    <template #icon><n-icon :component="DocumentTextOutline"/></template>
                    下载 TXT
                  </n-button>
                </n-space>
                <n-space justify="space-between" align="center">
                  <n-text><strong>转换结果:</strong> {{ selectedLog.resultFileName || '无' }}</n-text>
                  <n-button 
                    size="small" 
                    type="primary"
                    :disabled="!selectedLog.resultFileName"
                    @click="handleDownloadConvertResult(selectedLog)"
                  >
                    <template #icon><n-icon :component="CloudDownloadOutline"/></template>
                    下载 Excel
                  </n-button>
                </n-space>
                
                <!-- 转换统计 -->
                <div v-if="getConvertStats(selectedLog)" class="convert-stats">
                  <n-tag type="info" size="small">选择题: {{ getConvertStats(selectedLog).choiceCount || 0 }}</n-tag>
                  <n-tag type="success" size="small">判断题: {{ getConvertStats(selectedLog).judgeCount || 0 }}</n-tag>
                  <n-tag type="warning" size="small">科目: {{ getConvertStats(selectedLog).subjectName || '未分类' }}</n-tag>
                </div>
              </n-space>
           </div>

           <div class="result-stamp" :class="selectedLog.operationStatus === 1 ? 'success' : 'fail'">
              {{ selectedLog.operationStatus === 1 ? 'SUCCESS' : 'FAILURE' }}
           </div>
           
           <div class="modal-footer">
             <n-popconfirm @positive-click="handleDeleteLog(selectedLog.id)">
                <template #trigger>
                  <n-button type="error" ghost block style="margin-top: 20px;">
                    <template #icon><n-icon :component="TrashOutline" /></template>
                    DELETE LOG ENTRY
                  </n-button>
                </template>
                Are you sure to delete this log entry?
             </n-popconfirm>
           </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, h } from 'vue'
import { NModal, NIcon, NSpin, NPagination, NDataTable, NButton, NSpace, NText, NDivider, NPopconfirm, useMessage, NTag } from 'naive-ui'
import { 
  ConstructOutline, 
  SkullOutline,     
  FlashOutline,     
  CodeWorkingOutline,
  DocumentTextOutline,
  CloudDownloadOutline,
  TrashOutline,
  SwapHorizontalOutline
} from '@vicons/ionicons5'
import { getOperationLogs, deleteOperationLog, downloadConvertSourceFile, downloadConvertResultFile } from '@/api/admin'
import { getQuestionList, exportExcel, clearAllQuestions } from '@/api/question'

const message = useMessage()
const loading = ref(false)
const logs = ref([])
const showDetail = ref(false)
const selectedLog = ref(null)

const pagination = ref({
  page: 1,
  pageSize: 9, 
  itemCount: 0
})

// Detail Data
const detailQuestions = ref([])
const detailLoading = ref(false)
const detailPagination = ref({ pageSize: 5 })

const isImportLog = computed(() => {
  return selectedLog.value?.operationType === 'IMPORT'
})

const isConvertLog = computed(() => {
  return selectedLog.value?.operationType === 'CONVERT'
})

const questionColumns = [
  { title: 'ID', key: 'id', width: 60 },
  { 
    title: 'Content', 
    key: 'content', 
    ellipsis: { tooltip: true }
  },
  { title: 'Subject', key: 'subject', width: 100 },
  { title: 'Type', key: 'type', width: 80, render: (row) => row.type === 'single-choice' ? '单选' : (row.type === 'judge' ? '判断' : '多选') }
]

onMounted(() => {
  fetchLogs()
})

async function fetchLogs() {
  loading.value = true
  try {
    const res = await getOperationLogs({
      page: pagination.value.page,
      size: pagination.value.pageSize
    })
    logs.value = res.data.records
    pagination.value.itemCount = res.data.total
  } catch (e) {
    console.error('Failed to retrieve logs', e)
    message.error('加载日志失败')
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
  
  if (log.operationType === 'IMPORT') {
    fetchDetailQuestions(log.id)
  }
}

async function fetchDetailQuestions(logId) {
  detailLoading.value = true
  try {
    // Determine subject from operationData if needed? 
    // Actually we filter by importLogId so subject is implicit but we can leave it open
    const res = await getQuestionList({
      page: 1,
      size: 100, // Load first 100 as preview
      importLogId: logId
    })
    detailQuestions.value = res.data.records
  } catch (e) {
    console.error(e)
  } finally {
    detailLoading.value = false
  }
}

function getIcon(type) {
  const t = type?.toLowerCase() || ''
  if (t.includes('import')) return CloudDownloadOutline
  if (t.includes('convert')) return SwapHorizontalOutline
  if (t.includes('update') || t.includes('modify')) return ConstructOutline
  if (t.includes('delete')) return SkullOutline
  if (t.includes('add') || t.includes('create')) return FlashOutline
  return CodeWorkingOutline
}

function getLogTheme(log) {
  const t = log.operationType?.toLowerCase() || ''
  if (t.includes('import')) return 'theme-import'
  if (t.includes('convert')) return 'theme-convert'
  if (t.includes('delete')) return 'theme-danger'
  if (t.includes('add') || t.includes('create')) return 'theme-active'
  return 'theme-neutral'
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(5, 16)
}

function getFileName(log) {
  if (!log.operationData) return null
  try {
    const data = JSON.parse(log.operationData)
    return data.fileName
  } catch(e) {
    return null
  }
}

async function handleDownload(log) {
  try {
    const res = await exportExcel({ importLogId: log.id })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    
    let fileName = `Download_${log.id}.xlsx`
    try {
        const data = JSON.parse(log.operationData)
        if(data.fileName) fileName = data.fileName
    } catch(e){}
    
    a.download = fileName
    a.click()
    window.URL.revokeObjectURL(url)
    message.success('Download triggered')
  } catch (e) {
    message.error('Download failed')
  }
}

async function handleDeleteBatch(log) {
  try {
    const res = await clearAllQuestions({ importLogId: log.id })
    message.success(res.data || 'Batch deleted')
    fetchDetailQuestions(log.id) // check if empty
    showDetail.value = false // Close modal as data is gone? Or refresh
  } catch (e) {
    message.error('Delete batch failed')
  }
}

async function handleDeleteLog(id) {
  try {
    await deleteOperationLog(id)
    message.success('Log deleted')
    showDetail.value = false
    fetchLogs()
  } catch (e) {
    message.error('Delete log failed')
  }
}

// 获取转换日志的统计信息
function getConvertStats(log) {
  if (!log || !log.operationData) return null
  try {
    return JSON.parse(log.operationData)
  } catch (e) {
    return null
  }
}

// 下载转换日志的原始 TXT 文件
function handleDownloadConvertSource(log) {
  if (!log || !log.id) return
  const url = downloadConvertSourceFile(log.id)
  window.open(url, '_blank')
}

// 下载转换日志的结果 Excel 文件
function handleDownloadConvertResult(log) {
  if (!log || !log.id) return
  const url = downloadConvertResultFile(log.id)
  window.open(url, '_blank')
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
  margin-bottom: 10px; 
}

.file-name-tag {
    font-size: 0.85rem;
    color: #1976d2;
    background: #e3f2fd;
    padding: 2px 8px;
    border-radius: 4px;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 20px;
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
.theme-import .action-icon-circle { background: #E8F5E9; color: #1B5E20; }
.theme-import { border-color: #2E7D32; }

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

.info-row {
    display: flex;
    gap: 20px;
    margin-bottom: 10px;
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

.import-details {
    margin-top: 20px;
    border: 2px dashed #ccc;
    padding: 15px;
    border-radius: 8px;
}

.convert-details {
    margin-top: 20px;
    border: 2px dashed #6366f1;
    padding: 15px;
    border-radius: 8px;
    background: #f5f3ff;
}

.convert-stats {
    display: flex;
    gap: 10px;
    margin-top: 12px;
    flex-wrap: wrap;
}

.theme-convert .action-icon-circle { 
    background: #EEF2FF; 
    color: #4F46E5; 
}
.theme-convert { 
    border-color: #6366F1; 
}
</style>

