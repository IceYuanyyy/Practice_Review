<template>
  <div class="cosmic-light-container">
    <div class="comic-header">
      <h1 class="comic-title">ANNOUNCEMENTS</h1>
      <div class="comic-subtitle">系统公告管理</div>
    </div>

    <!-- 操作栏 -->
    <!-- Action / Narrator Box -->
    <div class="narrator-box">
      <div class="caption-label">MEANWHILE, AT HEADQUARTERS...</div>
      <n-space justify="space-between" align="center">
        <n-button type="primary" class="comic-btn-primary" @click="openCreateModal">
          <template #icon><n-icon :component="AddOutline" /></template>
          BROADCAST MESSAGE
        </n-button>
        
        <n-select
          v-model:value="filterStatus"
          :options="statusOptions"
          placeholder="FILTER SIGNALS"
          style="width: 180px"
          class="comic-select"
          clearable
          @update:value="fetchAnnouncements"
        />
      </n-space>
    </div>

    <!-- 公告列表 -->
    <div class="action-grid" v-if="!loading">
      <div 
        v-for="item in announcements" 
        :key="item.id" 
        class="action-card"
        :class="getCardTheme(item)"
        @click="openDetailModal(item)"
      >
        <div class="card-content">
          <!-- Icon -->
          <div class="action-icon-circle">
             <n-icon :component="MegaphoneOutline" size="28" />
          </div>

          <!-- 置顶标记 -->
          <div class="pin-badge" v-if="item.isPinned">
            <n-icon :component="PinOutline" />
            PINNED
          </div>

          <h3 class="announcement-title">{{ item.title }}</h3>
          
          <div class="announcement-meta">
            <span class="timestamp">{{ formatTime(item.createTime) }}</span>
            <span class="publisher">{{ item.publisherName || 'ADMIN' }}</span>
          </div>

          <div class="announcement-preview">
            {{ truncate(item.content, 80) }}
          </div>

          <div class="status-badge" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="announcements.length === 0" class="empty-state">
        <n-empty description="暂无公告">
          <template #extra>
            <n-button type="primary" size="small" @click="openCreateModal">
              发布第一条公告
            </n-button>
          </template>
        </n-empty>
      </div>
    </div>

    <!-- Loading -->
    <div v-else class="cosmic-loading">
      <n-spin size="large" />
      <p>LOADING DATA...</p>
    </div>

    <!-- Pagination -->
    <div class="comic-pagination" v-if="pagination.itemCount > pagination.pageSize">
      <n-pagination
        :page="pagination.page"
        :page-count="Math.ceil(pagination.itemCount / pagination.pageSize)"
        @update:page="handlePageChange"
      />
    </div>

    <!-- 创建/编辑 Modal -->
    <n-modal v-model:show="showFormModal" transform-origin="center">
      <div class="comic-modal" style="width: 600px;">
        <div class="modal-header">
          <h2>{{ editingAnnouncement ? 'EDIT ANNOUNCEMENT' : 'NEW ANNOUNCEMENT' }}</h2>
        </div>
        <div class="modal-body">
          <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="top">
            <n-form-item label="标题" path="title">
              <n-input v-model:value="formData.title" placeholder="请输入公告标题" />
            </n-form-item>
            <n-form-item label="内容" path="content">
              <n-input 
                v-model:value="formData.content" 
                type="textarea" 
                placeholder="请输入公告内容" 
                :rows="6"
              />
            </n-form-item>
            <n-form-item label="状态" path="status">
              <n-radio-group v-model:value="formData.status">
                <n-space>
                  <n-radio :value="0">草稿</n-radio>
                  <n-radio :value="1">立即发布</n-radio>
                </n-space>
              </n-radio-group>
            </n-form-item>
            <n-form-item label="置顶">
              <n-switch v-model:value="formData.isPinned" />
            </n-form-item>
          </n-form>
          <n-space justify="end" style="margin-top: 20px;">
            <n-button @click="showFormModal = false">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ editingAnnouncement ? '保存修改' : '发布公告' }}
            </n-button>
          </n-space>
        </div>
      </div>
    </n-modal>

    <!-- 详情 Modal -->
    <n-modal v-model:show="showDetailModal" transform-origin="center">
      <div class="comic-modal" style="width: 650px;">
        <div class="modal-header">
          <h2>ANNOUNCEMENT #{{ selectedAnnouncement?.id }}</h2>
        </div>
        <div class="modal-body" v-if="selectedAnnouncement">
          <div class="detail-title">{{ selectedAnnouncement.title }}</div>
          <div class="detail-meta">
            <n-tag :type="getStatusType(selectedAnnouncement.status)" size="small">
              {{ getStatusText(selectedAnnouncement.status) }}
            </n-tag>
            <span v-if="selectedAnnouncement.isPinned">
              <n-tag type="warning" size="small">置顶</n-tag>
            </span>
            <span class="meta-item">发布者: {{ selectedAnnouncement.publisherName || '管理员' }}</span>
            <span class="meta-item">时间: {{ formatTime(selectedAnnouncement.createTime) }}</span>
          </div>
          <div class="detail-content">
            {{ selectedAnnouncement.content }}
          </div>

          <n-divider />

          <n-space justify="space-between" align="center">
            <n-space>
              <!-- 状态操作 -->
              <n-button 
                v-if="selectedAnnouncement.status !== 1" 
                type="success" 
                size="small"
                @click="handlePublish(selectedAnnouncement.id)"
              >
                <template #icon><n-icon :component="CheckmarkCircleOutline" /></template>
                发布
              </n-button>
              <n-button 
                v-if="selectedAnnouncement.status === 1" 
                type="warning" 
                size="small"
                @click="handleRevoke(selectedAnnouncement.id)"
              >
                <template #icon><n-icon :component="CloseCircleOutline" /></template>
                撤回
              </n-button>
              <!-- 置顶操作 -->
              <n-button 
                :type="selectedAnnouncement.isPinned ? 'default' : 'info'" 
                size="small"
                @click="handleTogglePin(selectedAnnouncement.id, !selectedAnnouncement.isPinned)"
              >
                <template #icon><n-icon :component="PinOutline" /></template>
                {{ selectedAnnouncement.isPinned ? '取消置顶' : '置顶' }}
              </n-button>
              <!-- 编辑 -->
              <n-button type="primary" size="small" @click="openEditModal(selectedAnnouncement)">
                <template #icon><n-icon :component="CreateOutline" /></template>
                编辑
              </n-button>
            </n-space>
            <!-- 删除 -->
            <n-popconfirm @positive-click="handleDelete(selectedAnnouncement.id)">
              <template #trigger>
                <n-button type="error" size="small" ghost>
                  <template #icon><n-icon :component="TrashOutline" /></template>
                  删除
                </n-button>
              </template>
              确定要删除这条公告吗？
            </n-popconfirm>
          </n-space>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { 
  NButton, NSpace, NIcon, NSpin, NPagination, NModal, 
  NForm, NFormItem, NInput, NRadioGroup, NRadio, NSwitch,
  NSelect, NTag, NDivider, NEmpty, NPopconfirm, useMessage
} from 'naive-ui'
import { 
  AddOutline,
  PinOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline,
  CreateOutline,
  TrashOutline,
  MegaphoneOutline
} from '@vicons/ionicons5'
import { 
  getAdminAnnouncements, 
  createAnnouncement, 
  updateAnnouncement, 
  deleteAnnouncement,
  publishAnnouncement,
  revokeAnnouncement,
  toggleAnnouncementPin
} from '@/api/announcement'

const message = useMessage()
const loading = ref(false)
const announcements = ref([])
const filterStatus = ref(null)

const pagination = ref({
  page: 1,
  pageSize: 9,
  itemCount: 0
})

// Form Modal
const showFormModal = ref(false)
const formRef = ref(null)
const editingAnnouncement = ref(null)
const submitting = ref(false)
const formData = reactive({
  title: '',
  content: '',
  status: 0,
  isPinned: false
})
const formRules = {
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  content: { required: true, message: '请输入内容', trigger: 'blur' }
}

// Detail Modal
const showDetailModal = ref(false)
const selectedAnnouncement = ref(null)

// Status options
const statusOptions = [
  { label: '全部', value: null },
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已撤回', value: 2 }
]

onMounted(() => {
  fetchAnnouncements()
})

async function fetchAnnouncements() {
  loading.value = true
  try {
    const res = await getAdminAnnouncements({
      page: pagination.value.page,
      size: pagination.value.pageSize,
      status: filterStatus.value
    })
    announcements.value = res.data.records
    pagination.value.itemCount = res.data.total
  } catch (e) {
    console.error(e)
    message.error('加载公告失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.value.page = page
  fetchAnnouncements()
}

function openCreateModal() {
  editingAnnouncement.value = null
  formData.title = ''
  formData.content = ''
  formData.status = 0
  formData.isPinned = false
  showFormModal.value = true
}

function openEditModal(item) {
  editingAnnouncement.value = item
  formData.title = item.title
  formData.content = item.content
  formData.status = item.status
  formData.isPinned = item.isPinned
  showDetailModal.value = false
  showFormModal.value = true
}

function openDetailModal(item) {
  selectedAnnouncement.value = item
  showDetailModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    if (editingAnnouncement.value) {
      // 更新
      await updateAnnouncement(editingAnnouncement.value.id, formData)
      message.success('更新成功')
    } else {
      // 创建
      await createAnnouncement(formData)
      message.success('发布成功')
    }
    showFormModal.value = false
    fetchAnnouncements()
  } catch (e) {
    console.error(e)
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handlePublish(id) {
  try {
    await publishAnnouncement(id)
    message.success('发布成功')
    showDetailModal.value = false
    fetchAnnouncements()
  } catch (e) {
    message.error('发布失败')
  }
}

async function handleRevoke(id) {
  try {
    await revokeAnnouncement(id)
    message.success('撤回成功')
    showDetailModal.value = false
    fetchAnnouncements()
  } catch (e) {
    message.error('撤回失败')
  }
}

async function handleTogglePin(id, pinned) {
  try {
    await toggleAnnouncementPin(id, pinned)
    message.success(pinned ? '置顶成功' : '取消置顶成功')
    showDetailModal.value = false
    fetchAnnouncements()
  } catch (e) {
    message.error('操作失败')
  }
}

async function handleDelete(id) {
  try {
    await deleteAnnouncement(id)
    message.success('删除成功')
    showDetailModal.value = false
    fetchAnnouncements()
  } catch (e) {
    message.error('删除失败')
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

function truncate(str, len) {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}

function getStatusText(status) {
  const map = { 0: '草稿', 1: '已发布', 2: '已撤回' }
  return map[status] || '未知'
}

function getStatusClass(status) {
  const map = { 0: 'status-draft', 1: 'status-published', 2: 'status-revoked' }
  return map[status] || ''
}

function getStatusType(status) {
  const map = { 0: 'default', 1: 'success', 2: 'warning' }
  return map[status] || 'default'
}

function getCardTheme(item) {
  if (item.isPinned) return 'theme-pinned'
  if (item.status === 1) return 'theme-published'
  if (item.status === 2) return 'theme-revoked'
  return 'theme-draft'
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
  margin-bottom: 30px;
}

.comic-title {
  font-family: 'Bangers', cursive;
  font-size: 3rem;
  color: #E23636;
  text-shadow: 
    3px 3px 0 #000,
    -1px -1px 0 #000,
    1px -1px 0 #000,
    -1px 1px 0 #000;
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

/* Narrator Box */
.narrator-box {
  background: #fff9c4; /* Pale Yellow */
  border: 4px solid #000;
  padding: 15px;
  margin-bottom: 30px;
  box-shadow: 8px 8px 0 #000;
  max-width: 800px;
  transform: rotate(-0.5deg);
  margin-left: auto;
  margin-right: auto;
}

.caption-label {
  font-family: 'Bangers', cursive;
  font-size: 1.2rem;
  margin-bottom: 10px;
  display: block;
  letter-spacing: 1px;
}

.comic-btn-primary {
  font-family: 'Bangers';
  letter-spacing: 1px;
  border: 2px solid #000;
}

.comic-select {
  font-family: 'Roboto Mono';
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
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
  display: flex; /* Flex layout if needed, though block is fine */
  flex-direction: column;
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

.action-card:hover {
  transform: translate(-5px, -5px);
  box-shadow: 12px 12px 0 rgba(0,0,0,0.2);
  z-index: 10;
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

.pin-badge {
  position: absolute;
  top: 0px;
  right: 0px;
  background: #f59e0b;
  color: #fff;
  font-family: 'Bangers';
  font-size: 1rem;
  padding: 2px 10px;
  border: 2px solid #000;
  transform: rotate(5deg);
  box-shadow: 2px 2px 0 rgba(0,0,0,0.2);
  z-index: 5;
}

.announcement-title {
  font-family: 'Bangers', cursive;
  font-size: 1.6rem;
  margin: 10px 0;
  letter-spacing: 1px;
  line-height: 1.1;
}

.announcement-meta {
  display: flex;
  gap: 15px;
  font-size: 0.8rem;
  color: #666;
  margin-bottom: 12px;
  border-bottom: 2px dashed #000;
  padding-bottom: 10px;
  opacity: 0.7;
}

.timestamp {
  background: #eee;
  padding: 2px 6px;
  font-family: 'Roboto Mono', monospace;
}

.announcement-preview {
  font-size: 0.9rem;
  color: #444;
  line-height: 1.5;
  min-height: 60px;
}

.status-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  font-weight: bold;
  font-family: 'Bangers';
  font-size: 1rem;
  transform: rotate(-10deg);
  border: 2px solid currentColor;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-draft { color: #9ca3af; }
.status-published { color: #10b981; }
.status-revoked { color: #f59e0b; }

.theme-pinned { border-color: #f59e0b; background: #fffbeb; }
.theme-pinned .action-icon-circle { background: #FFF8E1; color: #FF6F00; }

.theme-published { border-color: #10b981; }
.theme-published .action-icon-circle { background: #E8F5E9; color: #1B5E20; }

.theme-revoked { border-color: #f59e0b; background: #fefce8; }
.theme-revoked .action-icon-circle { background: #FFF3E0; color: #E65100; }

.theme-draft { border-color: #9ca3af; background: #f9fafb; }
.theme-draft .action-icon-circle { background: #F5F5F5; color: #616161; }

.empty-state {
  grid-column: 1 / -1;
  padding: 60px 20px;
  text-align: center;
}

.cosmic-loading {
  text-align: center;
  padding: 60px;
  font-family: 'Bangers';
  font-size: 1.5rem;
}

.comic-pagination {
  margin-top: 30px;
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
  font-size: 1.8rem;
  color: #FFD700;
}

.modal-body {
  padding: 20px;
}

.detail-title {
  font-family: 'Bangers', cursive;
  font-size: 1.8rem;
  margin-bottom: 10px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 2px dashed #eee;
}

.meta-item {
  font-size: 0.85rem;
  color: #666;
}

.detail-content {
  background: #f5f5f5;
  border: 2px solid #000;
  padding: 15px;
  white-space: pre-wrap;
  line-height: 1.6;
  max-height: 300px;
  overflow-y: auto;
}
</style>
