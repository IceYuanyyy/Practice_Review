<template>
  <div class="wrong-book-container">
    <!-- 筛选配置面板 -->
    <div v-if="!showWrongQuestions" class="filter-panel">
      <n-card>
        <div class="config-header">
          <h2 class="config-title">🔥 错题本复习</h2>
          <p class="config-subtitle">选择科目开始针对性复习</p>
        </div>

        <n-form>
          <!-- 科目选择 -->
          <n-form-item>
            <template #label>
              <div class="form-label">📚 选择科目</div>
            </template>
            <n-select
              v-model:value="selectedSubject"
              :options="subjectOptions"
              placeholder="选择要复习的科目"
              size="large"
            />
          </n-form-item>

          <!-- 开始按钮 -->
          <n-button
            type="error"
            block
            size="large"
            @click="startWrongPractice"
            class="start-btn"
            :disabled="!selectedSubject"
          >
            🚀 开始复习
          </n-button>

          <!-- 返回查看列表按钮 -->
          <n-button
            text
            block
            size="medium"
            @click="showWrongQuestions = true"
            class="view-list-btn"
          >
            📋 查看错题列表
          </n-button>
        </n-form>
      </n-card>
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
            @click="clearWrongBook"
          >
            <template #icon><n-icon :component="TrashOutline"/></template>
            全部撕掉
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon, NButton, NInput, NPagination, NCard, NForm, NFormItem, NSelect, useDialog, useMessage } from 'naive-ui'
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
      wrongQuestions.value = res.data.records || []
      totalCount.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载错题本失败:', error)
    // 回退到 store 数据
    wrongQuestions.value = practiceStore.wrongQuestions
    totalCount.value = practiceStore.wrongQuestions.length
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

const clearWrongBook = () => {
  dialog.warning({
    title: '确认清空',
    content: '要把墙上的便利贴都撕掉吗？',
    positiveText: '是的，全部撕掉',
    negativeText: '留着吧',
    onPositiveClick: async () => {
      try {
        await clearWrongBookApi()
        practiceStore.wrongQuestions = []
        message.success('错题墙已清空')
        await loadWrongQuestions()
        await loadSubjectStats()
      } catch (error) {
        message.error('操作失败')
      }
    }
  })
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
  // 跳转到练习页并带上题目信息
  practiceStore.setCurrentQuestion(question)
  router.push('/practice')
  message.success('开始重新练习该题')
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
  max-width: 480px;
  margin: 0 auto;
  padding-top: 60px;
}

:deep(.n-card) {
  background-color: #fff;
  border: 2px solid #2c3e50 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.15) !important;
}

.config-header {
  text-align: center;
  margin-bottom: 24px;
}

.config-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 36px;
  color: #2c3e50;
  margin-bottom: 8px;
  text-shadow: 2px 2px 0px rgba(0,0,0,0.05);
  transform: rotate(-2deg);
}

.config-subtitle {
  font-size: 16px;
  color: #57606a;
  font-family: 'Patrick Hand', cursive;
}

.form-label {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 6px;
  transform: rotate(-1deg);
  display: inline-block;
}

.start-btn {
  height: 56px;
  font-size: 22px;
  font-family: 'Gochi Hand', cursive;
  margin-top: 20px;
  border: 3px solid #dc2626;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  box-shadow: 3px 3px 0px #dc2626;
  background-color: #ff6b6b;
  color: #fff;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.start-btn:hover:not(:disabled) {
  transform: translate(-1px, -1px) rotate(1deg);
  box-shadow: 5px 5px 0px #dc2626;
}

.start-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0px #dc2626;
}

.start-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.view-list-btn {
  margin-top: 16px;
  font-family: 'Patrick Hand', cursive;
  font-size: 16px;
  color: #64748b;
}

.view-list-btn:hover {
  color: #2c3e50;
}

/* Header Styles */
.wall-header {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 48px;
  border-bottom: 3px dashed #cbd5e1;
  padding-bottom: 20px;
}

.header-content { display: flex; align-items: baseline; gap: 20px; }

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