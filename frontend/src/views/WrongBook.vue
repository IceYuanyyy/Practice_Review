<template>
  <div class="wrong-book-container">
    <!-- 筛选配置面板 (Hand-Drawn Style) -->
    <div v-if="!showWrongQuestions" class="filter-panel">
      <div class="notebook-cover-start">
         <div class="spiral-binding-left">
           <div v-for="n in 12" :key="n" class="ring"></div>
         </div>
         
         <div class="cover-body">
            <!-- Doodle Decoration -->
            <div class="doodle-sun">
              <svg viewBox="0 0 100 100" width="80" height="80">
                <circle cx="50" cy="50" r="25" fill="none" stroke="#ef4444" stroke-width="3" />
                <path d="M50 10 L50 20 M50 80 L50 90 M10 50 L20 50 M80 50 L90 50 M22 22 L29 29 M71 71 L78 78 M22 78 L29 71 M71 29 L78 22" stroke="#ef4444" stroke-width="3" />
              </svg>
            </div>

            <div class="config-header">
              <h2 class="hand-title-large">错题本复习</h2>
              <div class="hand-subtitle-large">专攻薄弱点，把知识点"画"进脑海里！</div>
            </div>
            
            <n-form :label-width="80" size="large" class="sketch-form">
              <n-grid :cols="1" :y-gap="32">
                <n-grid-item>
                  <div class="hand-label">📚 选择科目</div>
                  <n-select 
                    v-model:value="selectedSubject" 
                    :options="subjectOptions" 
                    placeholder="请选择科目" 
                    class="sketch-select"
                  />
                </n-grid-item>
                
                <n-grid-item>
                  <div class="action-buttons-stack">
                    <button class="sketch-btn-main" @click="startWrongPractice" :disabled="!selectedSubject">
                      <span class="btn-text">🚀 开始复习</span>
                      <span class="btn-bg"></span>
                    </button>
                    
                    <button class="sketch-btn-text secondary" @click="showWrongQuestions = true">
                      <span>📋 翻看所有错题</span>
                    </button>
                  </div>
                </n-grid-item>
              </n-grid>
            </n-form>
         </div>
      </div>
    </div>
    <!-- 错题列表视图 -->
    <div v-else>
      <div class="wall-header">
        <div class="header-content">
          <h2 class="page-title">我的错题集</h2>
          <div class="stats-decoration">
            <span class="count">{{ totalCount }}</span>
            <span class="label">个灵感碎片</span>
          </div>
        </div>
        
        <div class="actions-area">
          <n-button 
            type="primary"
            size="large"
            @click="showWrongQuestions = false"
          >
            <template #icon><n-icon :component="RefreshOutline" /></template>
            🚀 开始复习
          </n-button>
          
          <n-button 
            v-if="totalCount > 0" 
            text
            class="clear-link"
            @click="showClearModal = true"
          >
            <template #icon><n-icon :component="TrashOutline"/></template>
            撕掉错题
          </n-button>
        </div>
      </div>

    <!-- 空状态 -->
    <div v-if="practiceStore.wrongQuestions.length === 0" class="empty-wall">
       <div class="empty-sketch">
         <n-icon :component="HappyOutline" size="80" color="#cbd5e1" />
         <p>墙面空空如也，太棒了！</p>
         <n-button class="sketch-btn primary" @click="router.push('/practice')">
           去收集新灵感 (练习)
         </n-button>
       </div>
    </div>

    <!-- 错题墙 (Grid Layout) -->
    <div v-else>
      <div class="sticky-wall">
        <div
          v-for="(question, index) in wrongQuestions"
          :key="question.id"
          class="sticky-note"
          :class="[getNoteColor(index)]"
          :style="getNoteStyle(index)"
        >
          <!-- 胶带效果 -->
          <div class="tape-strip"></div>
          
          <div class="note-content">
            <div class="note-header">
              <span class="note-index">#{{ (currentPage - 1) * pageSize + index + 1 }}</span>
              <span class="note-date">{{ formatDate(question.updateTime) }}</span>
            </div>
            <div class="note-subheader">
               <div class="doodle-tag" :class="question.type">
                {{ getTypeLabel(question.type) }}
              </div>
              <div class="doodle-tag subject-tag">
                {{ question.subject }}
              </div>
            </div>

            <p class="question-text">{{ question.content }}</p>
            
            <div class="note-actions">
              <n-button text class="action-link master" @click="handleMaster(question.id)">
                ✅ 我学会了
              </n-button>
              <n-button text class="action-link retake" @click="retakeQuestion(question)">
                重练
              </n-button>
            </div>
            
            <!-- 解析部分 -->
            <div class="note-footer">
               <div class="answer-peek">
                 <span>Ans: </span>
                 <span class="correct-val">{{ question.answer }}</span>
               </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页组件 -->
      <div class="pagination-wrapper" v-if="totalCount > pageSize">
        <n-pagination
          v-model:page="currentPage"
          :page-count="Math.ceil(totalCount / pageSize)"
          :page-size="pageSize"
          @update:page="loadWrongQuestions"
        />
      </div>
    </div>
    </div>
    
    <!-- 清空错题弹窗 -->
    <n-modal v-model:show="showClearModal" preset="dialog" title="撕掉错题" style="width: 420px">
      <n-space vertical :size="16">
        <n-alert type="warning" :show-icon="true">
          选择要清空的科目，或清空全部错题。此操作不可恢复！
        </n-alert>
        
        <n-form-item label="选择科目">
          <n-select
            v-model:value="clearSubject"
            placeholder="选择科目（留空则清空全部）"
            clearable
            :options="subjectOptions"
          />
        </n-form-item>
        
        <n-text v-if="clearSubject" type="warning" style="font-size: 14px;">
          将清空「{{ clearSubject }}」的所有错题
        </n-text>
        <n-text v-else type="error" style="font-size: 14px;">
          将清空全部错题
        </n-text>
      </n-space>
      
      <template #action>
        <n-space justify="end">
          <n-button @click="showClearModal = false">取消</n-button>
          <n-button type="error" @click="confirmClear">
            {{ clearSubject ? '撕掉该科目' : '全部撕掉' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon, NButton, NInput, NPagination, NCard, NForm, NFormItem, NSelect, NModal, NAlert, NSpace, NText, useDialog, useMessage } from 'naive-ui'
import { TrashOutline, SearchOutline, HappyOutline, RefreshOutline } from '@vicons/ionicons5'
import { usePracticeStore } from '@/stores/practice'
import { clearWrongBook as clearWrongBookApi, getWrongBookPage, markMastered, getWrongBookSubjects } from '@/api/practice'

const router = useRouter()
const dialog = useDialog()
const message = useMessage()
const practiceStore = usePracticeStore()

// 视图切换
const showWrongQuestions = ref(true)

// 分页相关状态
const wrongQuestions = ref([])
const currentPage = ref(1)
const pageSize = ref(6)

// 清空弹窗相关
const showClearModal = ref(false)
const clearSubject = ref(null)
const totalCount = ref(0)
const loading = ref(false)
const searchKeyword = ref('')

// 科目筛选相关
const subjectStats = ref({})
const selectedSubject = ref(null)
const subjectOptions = ref([])

// 加载错题列表
const loadWrongQuestions = async () => {
  loading.value = true
  try {
    const res = await getWrongBookPage({
      page: currentPage.value,
      size: pageSize.value
    })
    
    if (res.data) {
      // 如果当前页为空且不是第一页 (例如删除了最后一页的唯一数据)，自动回退
      if (res.data.records.length === 0 && currentPage.value > 1) {
        currentPage.value--
        await loadWrongQuestions() // Retry with prev page
        return
      }
      
      wrongQuestions.value = res.data.records || []
      totalCount.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载错题本失败:', error)
    // 降级: 如果接口失败，尝试使用本地 Store 作为缓存展示 (提升体验)
    if (practiceStore.wrongQuestions.length > 0) {
       // 简单的本地分页模拟
       const start = (currentPage.value - 1) * pageSize.value
       wrongQuestions.value = practiceStore.wrongQuestions.slice(start, start + pageSize.value)
       totalCount.value = practiceStore.wrongQuestions.length
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSubjectStats()
  loadWrongQuestions()
})

// 加载科目统计
const loadSubjectStats = async () => {
  try {
    const res = await getWrongBookSubjects()
    if (res.data && Object.keys(res.data).length > 0) {
      subjectStats.value = res.data
      
      // 构建科目选项
      subjectOptions.value = Object.entries(res.data).map(([subject, count]) => ({
        label: `${subject} (${count}题)`,
        value: subject
      }))
      
      // 如果只有一个科目，自动选中
      if (subjectOptions.value.length === 1) {
        selectedSubject.value = subjectOptions.value[0].value
      }
    } else {
      // 清空后没有错题，重置数据
      subjectStats.value = {}
      subjectOptions.value = []
      selectedSubject.value = null
    }
  } catch (error) {
    console.error('加载科目统计失败:', error)
  }
}

// 开始错题专项练习
const startWrongPractice = () => {
  if (!selectedSubject.value) {
    message.warning('请先选择要复习的科目')
    return
  }
  
  router.push({
    path: '/practice',
    query: { wrongBookSubject: selectedSubject.value }
  })
  message.success(`开始 ${selectedSubject.value} 错题复习`)
}

// 随机样式生成器 (使用索引作为种子，保证列表重排时颜色相对稳定，或者简化处理)
const getNoteColor = (index) => {
  const colors = ['note-yellow', 'note-blue', 'note-pink', 'note-green'];
  return colors[index % colors.length];
}

const getNoteStyle = (index) => {
  // 伪随机旋转角度：-2deg 到 2deg
  // 使用简单的哈希算法保证同一个索引的旋转角度一致（避免重绘导致抖动）
  const seed = index * 12345; 
  const rotation = ((seed % 50) - 25) / 10; // -2.5 to 2.5
  return {
    transform: `rotate(${rotation}deg)`
  }
}

const getTypeLabel = (type) => {
  const map = {
    'single-choice': '单选',
    'multiple-choice': '多选',
    'judge': '判断',
    'choice': '选择'
  }
  return map[type] || '其他'
}

const formatDate = (isoString) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 确认清空错题
const confirmClear = async () => {
  try {
    await clearWrongBookApi(clearSubject.value || null)
    // 清空本地状态
    if (!clearSubject.value) {
      practiceStore.wrongQuestions = []
      wrongQuestions.value = []
      totalCount.value = 0
    }
    currentPage.value = 1
    message.success(clearSubject.value ? `已清空「${clearSubject.value}」的错题` : '错题墙已清空')
    // 刷新数据
    showClearModal.value = false
    clearSubject.value = null
    await loadSubjectStats()
    await loadWrongQuestions()
  } catch (error) {
    message.error('操作失败')
  }
}

// 标记已掌握
const handleMaster = async (id) => {
  try {
    await markMastered(id)
    message.success('太棒了！已标记为掌握')
    // 重新加载列表
    await loadWrongQuestions()
    await loadSubjectStats()
  } catch (error) {
    console.error('标记掌握失败:', error)
    message.error('操作失败')
  }
}



const retakeQuestion = (question) => {
  // 跳转到错题练习模式，指定该题的科目
  router.push({
    path: '/practice',
    query: { wrongBookSubject: question.subject }
  })
  message.success(`开始复习「${question.subject}」的错题`)
}
</script>

<style scoped>
.wrong-book-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 80vh;
  font-family: 'Patrick Hand', cursive;
}

/* Filter Panel Styles - 复用 Practice 页面样式 */
.filter-panel {
  width: 100%;
  max-width: 600px; /* Wider for notebook look */
  margin: 0 auto;
  padding-top: 40px;
}

/* Notebook Cover Styles */
.notebook-cover-start {
  display: flex;
  background-color: transparent;
  perspective: 1000px;
}

.spiral-binding-left {
  width: 40px;
  background: #e2e8f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-evenly;
  padding: 20px 0;
  border-radius: 8px 0 0 8px;
  border: 1px solid #cbd5e1;
  box-shadow: inset -5px 0 10px rgba(0,0,0,0.1);
  z-index: 2;
}

.ring {
  width: 50px; /* Wider than container to stick out */
  height: 12px;
  background: linear-gradient(90deg, #94a3b8 0%, #cbd5e1 50%, #64748b 100%);
  border-radius: 6px;
  margin: 15px 0;
  transform: rotate(-5deg);
  box-shadow: 1px 1px 2px rgba(0,0,0,0.3);
}

.cover-body {
  flex: 1;
  background: #fffdf5; /* Cream paper */
  border: 2px solid #2c3e50;
  border-left: none;
  border-radius: 0 15px 15px 0;
  padding: 40px;
  position: relative;
  box-shadow: 8px 8px 0 rgba(44, 62, 80, 0.2);
  min-height: 520px;
  
  /* Texture: Dot Grid */
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 20px 20px;
  
  display: flex;
  flex-direction: column;
}

.doodle-sun {
  position: absolute;
  top: 20px;
  right: 20px;
  animation: spin-slow 20s linear infinite;
  opacity: 0.8;
}
@keyframes spin-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.hand-title-large {
  font-family: 'Gochi Hand', cursive;
  font-size: 42px;
  color: #2c3e50;
  margin-bottom: 12px;
  text-align: center;
  transform: rotate(-2deg);
  text-shadow: 2px 2px 0px rgba(0,0,0,0.05);
}

.hand-subtitle-large {
  text-align: center;
  font-family: 'Patrick Hand', cursive;
  font-size: 18px;
  color: #64748b;
  margin-bottom: 40px;
}

/* Form Styles */
.form-field-wrapper {
  margin-bottom: 24px;
}

.hand-label {
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  margin-bottom: 8px;
  color: #2c3e50;
  display: block;
}

:deep(.sketch-select .n-base-selection) {
  border: 2px solid #2c3e50 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  background: #fff !important;
  box-shadow: 2px 2px 0 rgba(0,0,0,0.1);
  min-height: 46px;
}
:deep(.sketch-select .n-base-selection:hover) {
  box-shadow: 4px 4px 0 rgba(0,0,0,0.15);
}
:deep(.sketch-select .n-base-selection-label) {
  font-family: 'Patrick Hand', cursive; 
  font-size: 18px;
  line-height: 46px;
}

/* Custom Sketch Buttons */
.action-buttons-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.sketch-btn-main {
  position: relative;
  width: 100%;
  height: 60px;
  border: none;
  background: none;
  cursor: pointer;
  padding: 0;
  outline: none;
  transition: transform 0.2s;
}

.sketch-btn-main .btn-text {
  position: absolute;
  top: 0; left: 0; 
  width: 100%; height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #ff6b6b;
  color: #fff;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  z-index: 2;
  transition: transform 0.2s;
}

.sketch-btn-main .btn-bg {
  position: absolute;
  top: 6px; left: 6px;
  width: 100%; height: 100%;
  background: #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  z-index: 1;
}

.sketch-btn-main:hover .btn-text {
  transform: translate(-2px, -2px);
}
.sketch-btn-main:active .btn-text {
  transform: translate(2px, 2px);
}
.sketch-btn-main:disabled {
  opacity: 0.7;
  filter: grayscale(0.8);
  cursor: not-allowed;
}

.sketch-btn-text {
  background: none;
  border: none;
  font-family: 'Gochi Hand', cursive;
  font-size: 18px;
  color: #64748b;
  cursor: pointer;
  text-decoration: underline;
  text-decoration-style: dashed;
  text-underline-offset: 4px;
  padding: 8px;
  transition: all 0.2s;
}
.sketch-btn-text:hover {
  color: #2c3e50;
  text-decoration-style: solid;
  transform: scale(1.05);
}
.sketch-btn-text.secondary {
  color: #94a3b8;
}
.sketch-btn-text.secondary:hover {
  color: #64748b;
}

/* 科目选择器样式 */
.subject-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.subject-btn {
  font-family: 'Patrick Hand', cursive;
  font-size: 14px;
}

.actions-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.page-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 42px;
  color: #1e293b;
  position: relative;
  margin: 0;
  z-index: 1;
}

/* Highlighter Effect */
.page-title::after {
  content: '';
  position: absolute;
  bottom: 5px;
  left: -5px;
  right: -5px;
  height: 15px;
  background: rgba(253, 224, 71, 0.6); /* Yellow highlighter */
  z-index: -1;
  transform: rotate(-1deg);
  border-radius: 4px;
}

.stats-decoration {
  font-size: 18px;
  color: #64748b;
  font-weight: 700;
}
.stats-decoration .count { font-size: 24px; color: #ef4444; margin-right: 4px; }

/* Actions */
.actions-area { display: flex; align-items: center; gap: 24px; }

.sketch-input {
  width: 240px;
  font-family: 'Patrick Hand', cursive;
}
/* Deep selector for input border override */
:deep(.n-input) {
  border: 2px solid #94a3b8 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  background: transparent !important;
  transition: all 0.2s;
}
:deep(.n-input:hover), :deep(.n-input:focus-within) {
  border-color: #2c3e50 !important;
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
}

.clear-link {
  font-size: 18px;
  color: #ef4444;
  font-family: 'Gochi Hand', cursive;
  text-decoration: none;
  border-bottom: 2px dashed #ef4444;
}
.clear-link:hover {
  border-bottom-style: solid;
}

/* Practice Button */
.practice-btn {
  font-family: 'Gochi Hand', cursive;
  font-size: 18px;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  box-shadow: 3px 3px 0px #2c3e50;
  transition: all 0.2s;
}
.practice-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 5px 5px 0px #2c3e50;
}

/* Master Button */
.action-link.master {
  color: #10b981;
  font-weight: bold;
}
.action-link.master:hover {
  text-decoration: underline;
  text-decoration-style: wavy;
}

/* Pagination Wrapper */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px;
}
:deep(.n-pagination) {
  font-family: 'Patrick Hand', cursive;
}
:deep(.n-pagination .n-pagination-item) {
  border: 2px solid #cbd5e1;
  border-radius: 8px;
  margin: 0 4px;
}
:deep(.n-pagination .n-pagination-item--active) {
  border-color: #2c3e50;
  background: #2c3e50;
  color: #fff;
}

/* Empty State */
.empty-wall {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
.empty-sketch {
  text-align: center;
  color: #94a3b8;
  font-size: 24px;
}
.sketch-btn {
  margin-top: 24px;
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  padding: 10px 30px;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  background: #fff;
  cursor: pointer;
  box-shadow: 4px 4px 0px #2c3e50;
  transition: all 0.2s;
}
.sketch-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px #2c3e50;
}

/* Sticky Note Wall Grid */
.sticky-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 32px;
  padding: 20px;
}

/* Sticky Note Component */
.sticky-note {
  position: relative;
  padding: 24px;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 4px 8px rgba(0,0,0,0.15);
  transition: transform 0.3s ease;
  font-size: 18px;
  color: #334155;
}

.sticky-note:hover {
  transform: scale(1.05) rotate(0deg) !important; /* Suspend rotation on hover for readability */
  z-index: 10;
  box-shadow: 5px 10px 15px rgba(0,0,0,0.2);
}

/* Tape Strip */
.tape-strip {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%) rotate(-2deg);
  width: 100px;
  height: 30px;
  background-color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255,255,255,0.2);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  backdrop-filter: blur(2px); /* Frosted glass tape effect */
}

/* Note Colors */
.note-yellow { background: linear-gradient(135deg, #fef9c3 0%, #fef3c7 100%); }
.note-blue { background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%); }
.note-pink { background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%); }
.note-green { background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%); }

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px dashed rgba(0,0,0,0.1);
  padding-bottom: 8px;
}
.note-index {
  font-weight: 700;
  color: rgba(0,0,0,0.4);
  font-size: 20px;
}
.note-date {
  font-size: 12px;
  color: rgba(0,0,0,0.5);
  font-weight: 600;
}

.note-subheader {
  display: flex;
  margin-bottom: 8px;
}

/* Doodle Tag */
.doodle-tag {
  font-size: 14px;
  font-weight: 700;
  padding: 2px 8px;
  border: 2px solid currentColor;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
}
.doodle-tag.single-choice { color: #0284c7; }
.doodle-tag.multiple-choice { color: #d97706; }
.doodle-tag.judge { color: #16a34a; }
.doodle-tag.subject-tag { color: #64748b; margin-left: 8px; }

.question-text {
  flex: 1;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
}

.note-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px dashed rgba(0,0,0,0.1);
  padding-top: 12px;
}

.action-link {
  font-family: 'Gochi Hand', cursive;
  font-size: 16px;
  padding: 0 5px;
}
.action-link:hover {
  text-decoration: underline;
  text-decoration-style: wavy;
}
.action-link.remove { color: #ef4444; }
.action-link.retake { color: #4f46e5; }

.note-footer {
  margin-top: 8px;
  font-size: 14px;
  color: rgba(0,0,0,0.5);
}
.correct-val { font-weight: bold; color: #16a34a; }
</style>