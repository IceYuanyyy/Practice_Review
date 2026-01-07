<template>
  <div class="practice-container" ref="practiceContainerRef">
    <n-scrollbar class="global-scrollbar" content-class="practice-layout-container" trigger="none">
    
      <!-- Background Doodles Layer -->
      <div class="doodles-layer">
        <div 
          v-for="(doodle, index) in doodles" 
          :key="index"
          class="doodle-item"
          :style="doodle.style"
        >
          <svg 
            viewBox="0 0 100 100" 
            fill="none" 
            stroke="currentColor" 
            stroke-width="2" 
            stroke-linecap="round" 
            stroke-linejoin="round"
            :style="{ opacity: doodle.opacity }"
          >
            <path :d="doodle.path" />
          </svg>
        </div>
      </div>

      <!-- Main Content -->
      <div v-if="!currentQuestion" class="filter-panel">
        <div class="notebook-cover-start">
           <div class="spiral-binding-left">
             <div v-for="n in 12" :key="n" class="ring"></div>
           </div>
           
           <div class="cover-body">
              <div class="doodle-sun">
                <svg viewBox="0 0 100 100" width="80" height="80">
                  <circle cx="50" cy="50" r="25" fill="none" stroke="#f59e0b" stroke-width="3" />
                  <path d="M50 10 L50 20 M50 80 L50 90 M10 50 L20 50 M80 50 L90 50 M22 22 L29 29 M71 71 L78 78 M22 78 L29 71 M71 29 L78 22" stroke="#f59e0b" stroke-width="3" />
                </svg>
              </div>

              <div class="config-header">
                <h2 class="hand-title-large">开始专注练习</h2>
                <div class="hand-subtitle-large">准备好笔和纸了吗？让我们开始吧！(模拟)</div>
              </div>
              
              <n-form :label-width="80" size="large" class="sketch-form">
                <n-grid :cols="1" :y-gap="32">
                  <n-grid-item>
                    <div class="hand-label">✏️ 选择科目</div>
                    <n-select 
                      v-model:value="filters.subject" 
                      :options="subjectOptions" 
                      placeholder="全部科目" 
                      class="sketch-select"
                    />
                  </n-grid-item>
                  <n-grid-item>
                    <div class="hand-label">📝 题目类型</div>
                    <n-select 
                      v-model:value="filters.type" 
                      :options="typeOptions" 
                      placeholder="混合题型" 
                      class="sketch-select"
                    />
                  </n-grid-item>
                  <n-grid-item>
                    <button class="sketch-btn-main" @click="startPractice">
                      <span class="btn-text">进入沉浸模式</span>
                      <span class="btn-bg"></span>
                    </button>
                  </n-grid-item>
                </n-grid>
              </n-form>
           </div>
        </div>
      </div>

      <div v-else style="display: contents">
        <!-- Left Sidebar: Stats -->
        <div class="side-panel left-panel paper-effect">
            <div class="side-header">
              <n-icon :component="SchoolOutline" />
              <span>当前状态</span>
            </div>
            <div class="side-content">
                <div class="subject-card">
                  <div class="subject-label">科目</div>
                  <div class="subject-main-text">
                    {{ currentSubject || '随机练习' }}
                  </div>
                  <div class="subject-sub-text" v-if="isWrongBookMode">
                    错题模式
                  </div>
                </div>
                
                <div class="stat-grid">
                  <div class="stat-item">
                      <span class="stat-val">{{ roundNumber }}</span>
                      <span class="stat-label">轮次</span>
                  </div>
                  <div class="stat-item">
                      <span class="stat-val">{{ answeredCount }}</span>
                      <span class="stat-label">已做</span>
                  </div>
                </div>
            </div>
            <div class="side-footer">
              <div class="mode-tag">{{ isWrongBookMode ? '错题模式' : '普通模式' }}</div>
              
              <div class="reset-wrapper" v-if="currentSubject" @click="handleResetRound">
                <n-icon :component="RefreshOutline" class="reset-icon" />
                <span>重置本轮</span>
              </div>
            </div>
            <!-- 卡片 2: 禅意专注 (New Creative Card) -->
            <n-card :bordered="false" class="side-card zen-card glass" :class="{ 'distracted-shake': isDistracted }">
              <div class="zen-header">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <n-switch v-model:value="isZenModeEnabled" size="small" />
                  <div class="zen-title" :class="{ 'text-danger': isDistracted }">
                    {{ isDistracted ? '哎呀！' : '禅意专注' }}
                  </div>
                </div>
                <div class="zen-status" :class="{ 'status-danger': isDistracted }">
                  {{ !isZenModeEnabled ? '未开启' : (isDistracted ? '走神啦 😮' : '专注中...') }}
                </div>
              </div>
              <div class="zen-content">
                <div class="zen-timer" :class="{ 'blur-text': isDistracted }">
                  {{ isDistracted ? '快回来~' : formatZenTime(zenTime) }}
                </div>
                <div class="zen-breath-wrapper">
                  <div class="zen-breath-circle" :class="{ 'circle-danger': isDistracted }"></div>
                </div>
                <div class="zen-quote">
                  “ {{ isDistracted ? '专注当下，切勿分心~' : zenQuote }} ”
                </div>
              </div>
            </n-card>
          </div>

          <!-- Center: Question -->
        <div class="question-wrapper">
          <!-- 顶部工具栏 -->
          <div class="practice-toolbar">
            <div class="toolbar-left">
              <div class="progress-chip">
                <span class="chip-icon">📝</span>
                <span class="chip-text">第 {{ displayCurrentNumber }} 题</span>
                <span class="chip-divider">|</span>
                <span class="chip-total">共 {{ displayTotalCount }} 题</span>
              </div>
            </div>
            <div class="toolbar-right">
              <n-button quaternary circle class="toolbar-btn" @click="showSearchModal = true" title="搜索题目">
                <n-icon :component="SearchOutline" size="18" />
              </n-button>
              <n-button quaternary circle class="toolbar-btn close" @click="exitPractice" title="退出练习">
                <n-icon :component="CloseOutline" size="20" />
              </n-button>
            </div>
          </div>

          <div class="question-panel paper-effect">
            <div class="question-header">
              <div class="header-left">
                <n-tag :type="getTypeColor()" size="small" round class="type-tag">
                  {{ getTypeLabel() }}
                </n-tag>
                <span class="subject-text">{{ currentQuestion.subject }}</span>
              </div>
              <div class="paper-holes"></div>
            </div>

            <div class="question-content">
              {{ currentQuestion.content }}
            </div>

            <div class="options-list">
              <!-- 单选题 -->
              <template v-if="(currentQuestion.type === 'single-choice' || currentQuestion.type === 'choice') && options.length > 0">
                <div 
                  v-for="option in options" 
                  :key="option.key"
                  class="option-item"
                  :class="{ 
                    'selected': userAnswer === option.key,
                    'disabled': practiceStore.showAnalysis,
                    'correct-highlight': practiceStore.showAnalysis && option.key === currentQuestion.answer,
                    'error-highlight': practiceStore.showAnalysis && userAnswer === option.key && userAnswer !== currentQuestion.answer
                  }"
                  @click="handleSelectOption(option.key)"
                >
                  <div class="option-key">{{ option.key }}</div>
                  <div class="option-text">{{ option.text }}</div>
                  <div v-if="practiceStore.showAnalysis && option.key === currentQuestion.answer" class="result-icon">
                    <n-icon :component="CheckmarkCircle" color="#10b981" size="24"/>
                  </div>
                  <div v-if="practiceStore.showAnalysis && userAnswer === option.key && userAnswer !== currentQuestion.answer" class="result-icon">
                    <n-icon :component="CloseCircle" color="#ef4444" size="24"/>
                  </div>
                </div>
              </template>

              <!-- 多选题 -->
              <template v-if="currentQuestion.type === 'multiple-choice' && options.length > 0">
                <div 
                  v-for="option in options" 
                  :key="option.key"
                  class="option-item checkbox-item"
                  :class="{ 
                    'selected': isOptionSelected(option.key),
                    'disabled': practiceStore.showAnalysis,
                    'correct-highlight': practiceStore.showAnalysis && isInCorrectAnswer(option.key),
                    'error-highlight': practiceStore.showAnalysis && isOptionSelected(option.key) && !isInCorrectAnswer(option.key)
                  }"
                  @click="toggleMultipleOption(option.key)"
                >
                  <div class="option-checkbox" :class="{ checked: isOptionSelected(option.key) }">
                    <n-icon v-if="isOptionSelected(option.key)" :component="CheckmarkOutline" />
                  </div>
                  <div class="option-text">{{ option.text }}</div>
                  <div v-if="practiceStore.showAnalysis && isInCorrectAnswer(option.key)" class="result-icon">
                    <n-icon :component="CheckmarkCircle" color="#10b981" size="24"/>
                  </div>
                </div>
              </template>

              <!-- 判断题 -->
              <template v-if="currentQuestion.type === 'judge'">
                <div 
                  v-for="val in ['正确', '错误']" 
                  :key="val"
                  class="option-item"
                  :class="{ 
                    'selected': userAnswer === val,
                    'disabled': practiceStore.showAnalysis,
                    'correct-highlight': practiceStore.showAnalysis && val === currentQuestion.answer,
                    'error-highlight': practiceStore.showAnalysis && userAnswer === val && userAnswer !== currentQuestion.answer
                  }"
                  @click="handleSelectOption(val)"
                >
                  <div class="option-text center-text">{{ val }}</div>
                  <div v-if="practiceStore.showAnalysis && val === displayAnswer" class="result-icon">
                    <n-icon :component="CheckmarkCircle" color="#10b981" size="24"/>
                  </div>
                  <div v-if="practiceStore.showAnalysis && userAnswer === val && !isCorrect" class="result-icon">
                    <n-icon :component="CloseCircle" color="#ef4444" size="24"/>
                  </div>
                </div>
              </template>
            </div>

            <div class="action-bar">
              <n-button 
                quaternary 
                round 
                size="large" 
                class="nav-btn prev-btn"
                :disabled="historyIndex <= 0"
                @click="goToPrevQuestion"
              >
                <n-icon :component="ArrowBackOutline" size="20" />
                上一题
              </n-button>

              <n-button 
                v-if="!isReviewingHistory && !practiceStore.showAnalysis" 
                type="primary" 
                round 
                size="large" 
                class="action-btn"
                :disabled="!userAnswer"
                @click="submitAnswer"
              >
                提交答案
              </n-button>
              
              <n-button 
                v-if="!isReviewingHistory && practiceStore.showAnalysis" 
                type="primary" 
                round 
                size="large" 
                class="action-btn"
                @click="nextQuestion"
              >
                下一题 (Enter)
              </n-button>
              
              <n-button 
                v-if="isReviewingHistory"
                type="primary" 
                round 
                size="large" 
                class="action-btn"
                :disabled="historyIndex >= practiceHistory.length - 1"
                @click="goToNextHistoryQuestion"
              >
                下一题 →
              </n-button>
            </div>

            <transition name="slide-up">
              <div v-if="practiceStore.showAnalysis" class="analysis-box">
                <div class="analysis-header">
                    <div class="analysis-title">
                      <n-icon :component="BookOutline" class="analysis-icon"/>
                      <span>知识点讲解</span>
                    </div>
                    <n-tag :type="isCorrect ? 'success' : 'error'" size="small" round>
                      {{ isCorrect ? '🎉 回答正确' : '🤔 回答错误' }}
                    </n-tag>
                </div>
                <div class="analysis-content">
                  <div class="correct-answer-row">
                    <span class="label">正确答案：</span>
                    <span class="value">{{ displayAnswer }}</span>
                  </div>
                  <div class="analysis-text">
                    {{ currentQuestion.analysis || '暂无详细解析，请参考正确答案进行复习。' }}
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>

          <!-- Right Sidebar -->
          <div class="side-panel right-panel">
            <n-card :bordered="false" class="side-card sheet-card glass">
              <div class="stat-header">
                <n-icon :component="BookOutline" />
                <span>答题卡</span>
              </div>
              
              <div v-if="displayTotalCount > 0" class="sheet-container">
                  <div class="sheet-info">
                    进度：{{ currentIndex + 1 }}/{{ displayTotalCount }}
                  </div>
                  
                  <div class="bubble-grid">
                      <div 
                        v-for="num in currentSheetBubbles" 
                        :key="num"
                        class="bubble"
                        :class="{ 
                          'active': num === (currentIndex + 1),
                          'correct': roundResults[num - 1] === 1,
                          'wrong': roundResults[num - 1] === 2,
                          'done': roundResults[num - 1] !== 0 && roundResults[num - 1] !== undefined
                        }"
                        @click="jumpToRoundIdx(num)"
                      >
                        {{ num }}
                      </div>
                  </div>

                  <!-- 答题卡分页 -->
                  <div v-if="totalSheetPages > 1" class="sheet-pagination">
                    <n-button quaternary circle size="small" :disabled="sheetPage <= 1" @click="sheetPage--">
                      <template #icon><n-icon :component="ArrowBackOutline" /></template>
                    </n-button>
                    <span class="page-num">{{ sheetPage }} / {{ totalSheetPages }}</span>
                    <n-button quaternary circle size="small" :disabled="sheetPage >= totalSheetPages" @click="sheetPage++">
                      <template #icon><n-icon :component="ArrowForwardOutline" /></template>
                    </n-button>
                  </div>
              </div>
              <div v-else class="empty-sheet">
                  随机模式无固定题量
              </div>
            </n-card>
          </div>
      </div>
    </n-scrollbar>
    
    <!-- 搜索模态框 -->
    <n-modal v-model:show="showSearchModal" preset="card" title="🔍 搜索题目" style="width: 500px; max-width: 90vw;">
      <div class="search-modal-content">
        <n-input 
          v-model:value="searchKeyword" 
          placeholder="输入题号或关键词..." 
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-button type="primary" @click="handleSearch" style="margin-top: 16px; width: 100%;">
          搜索
        </n-button>
        
        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="q in searchResults" :key="q.id" class="search-result-item" @click="jumpToQuestion(q)">
            <span class="result-id">#{{ q.id }}</span>
            <span class="result-content">{{ q.content.substring(0, 50) }}...</span>
          </div>
        </div>
      </div>
    </n-modal>

    <!-- 手绘风格未完成提醒 Modal -->
    <n-modal v-model:show="showResumeModal" :auto-focus="false" :mask-closable="false">
      <div class="sketch-modal-card">
        <div class="tape-sticker"></div>
        
        <div class="sketch-modal-header">
          <n-icon size="32" color="#e74c3c" :component="SchoolOutline" />
          <span class="sketch-title">未完成的挑战!</span>
        </div>

        <div class="sketch-modal-content">
          <p class="sketch-text">
            嘿！在 <strong class="subject-highlight">{{ resumeModalData.subject }}</strong> 笔记本里<br>发现你还有没写完的题：
          </p>
          <div class="doodle-progress">
             <span class="progress-fraction">
               <span class="curr">{{ resumeModalData.currentIndex }}</span>
               <span class="mid-line">/</span>
               <span class="total">{{ resumeModalData.totalCount }}</span>
             </span>
          </div>
          <p class="sketch-hint">要接着上次的写，还是翻篇重来？</p>
        </div>

        <div class="sketch-modal-footer">
          <button class="sketch-btn secondary" @click="handleResumeReset">
            <span class="btn-text">🔄 重置本轮</span>
          </button>
          
          <button class="sketch-btn primary" @click="handleResumeContinue">
            <span class="btn-text">✏️ 继续练习</span>
             <!-- 下划线装饰 -->
             <svg class="btn-scribble" viewBox="0 0 100 10" preserveAspectRatio="none">
               <path d="M0,5 Q 50,10 100,5" stroke="currentColor" fill="none" stroke-width="2" />
            </svg>
          </button>
        </div>
      </div>
    </n-modal>

    <!-- 手绘风格退出确认 Modal -->
    <n-modal v-model:show="showExitModal" :auto-focus="false" :mask-closable="true">
      <div class="sketch-modal-card">
        <div class="tape-sticker"></div>
        
        <div class="sketch-modal-header">
           <!-- Doodle Warning Icon -->
          <n-icon size="40" color="#f59e0b" :component="BookOutline" />
          <span class="sketch-title">合上笔记本?</span>
        </div>

        <div class="sketch-modal-content">
          <p class="sketch-text">
            确定要现在结束练习吗？<br>
            <span style="font-size: 14px; color: #94a3b8;">(进度会自动保存，下次可以接着写哦)</span>
          </p>
        </div>

        <div class="sketch-modal-footer">
          <button class="sketch-btn secondary" @click="showExitModal = false">
            <span class="btn-text">再写会儿</span>
          </button>
          
          <button class="sketch-btn primary warning-btn" @click="handleConfirmExit">
            <span class="btn-text">确认退出</span>
          </button>
        </div>
      </div>
    </n-modal>

    <!-- Reset Confirmation Modal -->
    <n-modal v-model:show="showResetModal" :auto-focus="false" :mask-closable="true">
      <div class="sketch-modal-card">
        <div class="tape-sticker"></div>
        <div class="sketch-modal-header">
          <span class="sketch-title">确认重置?</span>
        </div>
        <div class="sketch-modal-content">
          <p>当前轮次的进度将全部清空</p>
          <p>答题卡将变回一张白纸</p>
        </div>
        <div class="sketch-modal-footer">
          <button class="sketch-btn secondary" @click="showResetModal = false">
            <span class="btn-text">再想想</span>
          </button>
          <button class="sketch-btn primary warning-btn" @click="confirmResetAction">
            <span class="btn-text">确定重置</span>
          </button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog, NCard, NForm, NFormItem, NGrid, NGridItem, NSelect, NButton, NTag, NText, NIcon, NModal, NInput, NProgress, NScrollbar, NSwitch } from 'naive-ui'
import { CloseOutline, CheckmarkCircle, CloseCircle, CheckmarkOutline, SearchOutline, ArrowBackOutline, ArrowForwardOutline, SchoolOutline, BookOutline, RefreshOutline } from '@vicons/ionicons5'
import { getRandomQuestion } from '@/api/question'
import { submitAnswer as submitAnswerApi, startRound, nextRoundQuestion, prevRoundQuestion, resetRound, searchQuestions, startWrongBookPractice, nextWrongQuestion, jumpRoundQuestion, getRoundResults } from '@/api/practice'
import { getAllSubjects } from '@/api/subject'
import { usePracticeStore } from '@/stores/practice'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const practiceStore = usePracticeStore()

const currentQuestion = ref(null)
const userAnswer = ref(null)
const selectedAnswers = ref([]) // 多选题答案数组

// 轮次相关状态
const currentIndex = ref(0)
const totalCount = ref(0)
const roundNumber = ref(1)
const isRoundFinished = ref(false)
const currentSubject = ref('')

// 错题练习模式
const isWrongBookMode = ref(false)
const wrongBookSubject = ref(null)

// 做题历史记录（用于上一题/下一题导航）
const practiceHistory = ref([]) // [{question, userAnswer}]
const historyIndex = ref(-1) // 当前在历史中的位置

// 已答题数量，避免未作答的跳转也计入 "已做"
const answeredCount = computed(() =>
  practiceHistory.value.filter(record => record.userAnswer !== null && record.userAnswer !== undefined && record.userAnswer !== '').length
)

// 是否在回顾历史模式（不在历史末尾）
const isReviewingHistory = computed(() => {
  return historyIndex.value >= 0 && historyIndex.value < practiceHistory.value.length - 1
})

// 恢复轮次提示 Modal
const showResumeModal = ref(false)
const resumeModalData = ref({
  subject: '',
  currentIndex: 0,
  totalCount: 0,
  roundData: null
})

const handleResumeContinue = () => {
  if (resumeModalData.value.roundData) {
    applyRoundState(resumeModalData.value.roundData)
    showResumeModal.value = false
  }
}

const handleResumeReset = async () => {
  const subject = resumeModalData.value.subject
  try {
    const resetRes = await resetRound(subject)
    if (resetRes.data && resetRes.data.question) {
      applyRoundState(resetRes.data)
      roundResults.value = {}
      zenTime.value = 0
      message.success('新的一页，新的开始！') // More casual message
    }
  } catch (e) {
    message.error('哎呀，纸张卡住了 (重置失败)')
  }
  showResumeModal.value = false
}

// 搜索相关
const showSearchModal = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])

// 计算进度百分比（基于做题历史）
const roundProgress = computed(() => {
  if (answeredCount.value === 0) return 0
  // 每 20 题一个轮回
  return Math.round(((answeredCount.value % 20) / 20) * 100)
})

// 展示用题号与总数：优先使用后端返回的总量，随机模式退化为历史长度
const displayCurrentNumber = computed(() => {
  if (isWrongBookMode.value || currentSubject.value) {
    return (currentIndex.value || 0) + 1
  }
  if (practiceHistory.value.length === 0) return 0
  return historyIndex.value + 1
})

const displayTotalCount = computed(() => {
  if (isWrongBookMode.value || currentSubject.value) {
    return totalCount.value || 0
  }
  return practiceHistory.value.length
})

// === 答题卡分页与状态 ===
const roundResults = ref({}) // { index: status (0-未做, 1-正确, 2-错误) }
const sheetPage = ref(1)
const sheetPageSize = 80
const totalSheetPages = computed(() => Math.ceil(displayTotalCount.value / sheetPageSize))
const currentSheetBubbles = computed(() => {
  const start = (sheetPage.value - 1) * sheetPageSize
  const end = Math.min(start + sheetPageSize, displayTotalCount.value)
  const bubbles = []
  for (let i = start + 1; i <= end; i++) {
    bubbles.push(i)
  }
  return bubbles
})

// 获取题目状态
const fetchRoundResults = async () => {
  if (!currentSubject.value) return
  try {
    const res = await getRoundResults(currentSubject.value)
    if (res.data) {
      roundResults.value = res.data
    }
  } catch (err) {
    console.error('获取答题卡状态失败', err)
  }
}

// 跳转到指定题目
const jumpToRoundIdx = async (number) => {
  const index = number - 1
  if (index === currentIndex.value && currentQuestion.value) return
  
  // 先检查历史记录中是否有该题目（根据 roundIndex 匹配）
  const existingRecordIdx = practiceHistory.value.findIndex(
    record => record.roundIndex === index
  )
  
  // 如果历史中已有该题记录，直接从历史中恢复（类似上一题逻辑）
  if (existingRecordIdx !== -1) {
    const record = practiceHistory.value[existingRecordIdx]
    historyIndex.value = existingRecordIdx
    
    currentQuestion.value = record.question
    currentIndex.value = record.roundIndex
    practiceStore.setCurrentQuestion(record.question)
    
    // 恢复用户之前的选择
    if (record.userAnswer) {
      userAnswer.value = record.userAnswer
      // 多选题需要恢复 selectedAnswers（userAnswer 是如 "AB" 这样的字符串）
      if (record.question.type === 'multiple-choice' && record.userAnswer) {
        selectedAnswers.value = record.userAnswer.split('')
      } else {
        selectedAnswers.value = []
      }
      practiceStore.showAnalysis = true // 显示解析（因为已经答过）
    } else {
      userAnswer.value = null
      selectedAnswers.value = []
      practiceStore.showAnalysis = false
    }
    
    message.info(`已跳转至第 ${number} 题`)
    return
  }
  
  // 历史中没有该题记录，从后端获取
  try {
    practiceStore.reset()
    const res = await jumpRoundQuestion(currentSubject.value, index)
    if (res.data && res.data.question) {
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      practiceStore.setCurrentQuestion(res.data.question)
      userAnswer.value = null
      selectedAnswers.value = []
      
      // 将新题目添加到历史记录
      addToHistory(res.data.question, res.data.currentIndex)
      
      message.info(`已跳转至第 ${number} 题`)
    }
  } catch (err) {
    message.error('跳转题目失败')
  }
}

// === 禅意专注卡片逻辑 (Enhanced) ===
const zenTime = ref(0)
const zenTimer = ref(null)
const zenQuote = ref('') 
const isDistracted = ref(false)
const isZenModeEnabled = ref(false)
const practiceContainerRef = ref(null)

const zenQuotes = [
  "心如止水，专注当下。",
  "每一点进步，都值得庆祝。",
  "呼吸，感受思维的流动。",
  "不要急，答案就在心中。",
  "慢慢来，比较快。",
  "知行合一，止于至善。",
  "宁静致远，厚积薄发。"
]

// Current Quote (Reactive)
const currentZenQuote = computed(() => {
    if (zenQuote.value) return zenQuote.value
    return zenQuotes[Math.floor(zenTime.value / 60) % zenQuotes.length]
})

const formatZenTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h > 0 ? h + ':' : ''}${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

const toggleZenMode = (val) => {
  if (val) {
    enterZenMode()
  } else {
    exitZenMode()
  }
}

const enterZenMode = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(() => {})
  }
  startZenTimer()
  message.success('已进入沉浸模式')
}

const exitZenMode = () => {
  if (document.fullscreenElement) {
    document.exitFullscreen().catch(() => {})
  }
  // Optional: stop timer on exit? Or keep running? 
  // Reference implies we might want to stop or just reset state.
  // We'll keep timer running if they just exit fullscreen but stay in app, 
  // BUT usually Zen Mode implies the timer IS the Zen session.
  // Let's NOT stop timer here to avoid losing progress if accidental exit.
}

const startZenTimer = () => {
  if (zenTimer.value) clearInterval(zenTimer.value)
  if (!zenQuote.value) zenQuote.value = zenQuotes[Math.floor(Math.random() * zenQuotes.length)]
  zenTimer.value = setInterval(() => {
    zenTime.value++
  }, 1000)
}

const stopZenTimer = () => {
  if (zenTimer.value) {
    clearInterval(zenTimer.value)
    zenTimer.value = null
  }
  isDistracted.value = false
}

// Watch switch
watch(isZenModeEnabled, (newVal) => {
  toggleZenMode(newVal)
})

// Focus/Blur/Mouse logic
const handleMouseLeave = () => { 
  if (isZenModeEnabled.value) {
    isDistracted.value = true 
  }
}
const handleMouseEnter = () => { isDistracted.value = false }

// Fullscreen listener to sync state
const handleFullscreenChange = () => {
    if (!document.fullscreenElement && isZenModeEnabled.value) {
        isZenModeEnabled.value = false
    }
}

onMounted(async () => {
  // 挂载防走神监听 - 仅针对练习区域容器
  if (practiceContainerRef.value) {
    practiceContainerRef.value.addEventListener('mouseleave', handleMouseLeave)
    practiceContainerRef.value.addEventListener('mouseenter', handleMouseEnter)
  }
  
  // 窗口失焦点也算走神 (可选，保留以增强体验)
  window.addEventListener('blur', handleMouseLeave)
  window.addEventListener('focus', handleMouseEnter)
  document.addEventListener('fullscreenchange', handleFullscreenChange)

  await loadLastFilter()
  startZenTimer()
})

onUnmounted(() => {
  if (zenTimer.value) clearInterval(zenTimer.value)
  // 移除监听
  if (practiceContainerRef.value) {
    practiceContainerRef.value.removeEventListener('mouseleave', handleMouseLeave)
    practiceContainerRef.value.removeEventListener('mouseenter', handleMouseEnter)
  }
  window.removeEventListener('blur', handleMouseLeave)
  window.removeEventListener('focus', handleMouseEnter)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
const filters = reactive({ subject: null, type: null, difficulty: null })

// 选项配置
const subjectOptions = ref([])

// 加载科目列表（保持纯查询，避免后端未启动时报错）
const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    if (res.data && res.data.length > 0) {
      const subjects = res.data.map(subject => ({
        label: `${subject.name} (${subject.questionCount})`,
        value: subject.name
      }))
      // 将"全部科目"作为独立选项，value设为空字符串而非null
      subjectOptions.value = [
        { label: '全部科目', value: '' },
        ...subjects
      ]
    }
  } catch (error) {
    console.error('加载科目列表失败', error)
  }
}

const typeOptions = [
  { label: '混合题型', value: '' },
  { label: '单选题', value: 'single-choice' },
  { label: '多选题', value: 'multiple-choice' },
  { label: '判断题', value: 'judge' }
]

// 解析选项
const options = computed(() => {
  if (!currentQuestion.value) return []
  const { type, options: rawOpts, id } = currentQuestion.value
  
  // 仅选择题需要解析选项
  if (!['single-choice', 'multiple-choice', 'choice'].includes(type)) return []
  
  try {
    let parsedOpts = rawOpts
    
    // 1. 如果是字符串，尝试 JSON 解析
    if (typeof rawOpts === 'string') {
      try {
        parsedOpts = JSON.parse(rawOpts)
      } catch (e) {
        // 解析失败，可能是非 JSON 字符串，尝试简单分割或报错
        console.warn(`选项非标准JSON格式 (ID: ${id}):`, rawOpts)
        // 尝试按换行符或常见分隔符兜底解析 (可选)
        return []
      }
    }
    
    // 2. 确保是数组
    if (!Array.isArray(parsedOpts)) {
      if (typeof parsedOpts === 'object' && parsedOpts !== null) {
        // 处理对象格式 case: { "A": "content", "B": "content" }
        return Object.entries(parsedOpts).map(([key, text]) => ({ key, text }))
      }
      console.error(`选项数据类型错误 (ID: ${id}):`, parsedOpts)
      return []
    }
    
    if (parsedOpts.length === 0) return []

    // 3. 规范化每个选项
    // 支持格式: { key: 'A', text: '...' } 或 "A:..." 或 "..."
    const letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G']
    
    return parsedOpts.map((opt, index) => {
      // Case A: 对象且已有 key/text
      if (typeof opt === 'object' && opt !== null) {
        if (opt.key && opt.text) return opt
        // 只有 value/label 等其他字段的情况，需做映射适配，暂定:
        return { key: opt.key || letters[index], text: opt.text || opt.value || opt.label || JSON.stringify(opt) }
      }
      
      // Case B: 字符串
      if (typeof opt === 'string') {
        const trimmed = opt.trim()
        // 检测 "A: 内容" 或 "A. 内容" 格式
        const match = trimmed.match(/^([A-Z])[:.、]\s*(.*)$/)
        if (match) {
          return { key: match[1], text: match[2] }
        }
        // 纯内容，自动分配字母
        return { key: letters[index] || `?${index}`, text: trimmed }
      }
      
      return null
    }).filter(item => item !== null && item.text)
    
  } catch (e) {
    console.error(`题目选项解析异常 (ID: ${currentQuestion.value?.id}):`, e)
    return []
  }
})

// 判断题答案显示（转换字母为中文）
const displayAnswer = computed(() => {
  if (!currentQuestion.value) return ''
  
  if (currentQuestion.value.type === 'judge') {
    const answer = currentQuestion.value.answer?.trim().toUpperCase()
    // 如果答案是字母，转换为中文
    if (answer === 'A' || answer === '正确' || answer === 'TRUE' || answer === 'T' || answer === '√') {
      return '正确'
    } else if (answer === 'B' || answer === '错误' || answer === 'FALSE' || answer === 'F' || answer === '×') {
      return '错误'
    }
    return currentQuestion.value.answer || '未知'
  }
  
  return currentQuestion.value.answer || ''
})

const isCorrect = computed(() => {
  if (!currentQuestion.value || !userAnswer.value) return false
  
  // 多选题：需要完全匹配（顺序无关）
  if (currentQuestion.value.type === 'multiple-choice') {
    const userAns = userAnswer.value.split('').sort().join('')
    const correctAns = (currentQuestion.value.answer || '').split('').sort().join('')
    return userAns === correctAns
  }
  
  // 判断题特殊处理
  if (currentQuestion.value.type === 'judge') {
    const userAns = userAnswer.value?.trim()
    const correctAns = currentQuestion.value.answer?.trim().toUpperCase()
    
    // 标准化用户答案
    const normalizedUser = (userAns === '正确' || userAns === 'A') ? 'A' : 'B'
    
    // 标准化正确答案
    let normalizedCorrect = 'A'
    if (correctAns === 'B' || correctAns === '错误' || correctAns === 'FALSE' || correctAns === 'F' || correctAns === '×') {
      normalizedCorrect = 'B'
    }
    
    return normalizedUser === normalizedCorrect
  }
  
  // 选择题直接比较
  return userAnswer.value?.trim().toUpperCase() === currentQuestion.value.answer?.trim().toUpperCase()
})

const handleSelectOption = (key) => {
  if (practiceStore.showAnalysis) return
  userAnswer.value = key
  console.log('选择答案:', key, '题型:', currentQuestion.value?.type)
}

// 多选题答案管理
const toggleMultipleOption = (key) => {
  if (practiceStore.showAnalysis) return
  
  const index = selectedAnswers.value.indexOf(key)
  if (index > -1) {
    selectedAnswers.value.splice(index, 1)
  } else {
    selectedAnswers.value.push(key)
  }
  
  // 排序后组合成答案字符串
  userAnswer.value = selectedAnswers.value.sort().join('')
  console.log('多选答案:', userAnswer.value, '已选:', selectedAnswers.value)
}

const isOptionSelected = (key) => {
  return selectedAnswers.value.includes(key)
}

const isInCorrectAnswer = (key) => {
  if (!currentQuestion.value) return false
  const correctAnswer = currentQuestion.value.answer || ''
  return correctAnswer.includes(key)
}

// 获取题型标签
const getTypeLabel = () => {
  if (!currentQuestion.value) return ''
  const typeMap = {
    'single-choice': '单选题',
    'multiple-choice': '多选题',
    'choice': '选择题',
    'judge': '判断题'
  }
  return typeMap[currentQuestion.value.type] || '未知题型'
}

const getTypeColor = () => {
  if (!currentQuestion.value) return 'default'
  const colorMap = {
    'single-choice': 'info',
    'multiple-choice': 'warning',
    'choice': 'info',
    'judge': 'success'
  }
  return colorMap[currentQuestion.value.type] || 'default'
}

// 绑定回车键下一题
const handleKeyup = (e) => {
  if (e.key === 'Enter' && practiceStore.showAnalysis) {
    nextQuestion()
  }
}

onMounted(() => {
  loadSubjects()
  generateDoodles()
  startZenTimer()
  window.addEventListener('keyup', handleKeyup)
  
  // 检查是否从错题本页面跳转过来
  const query = router.currentRoute.value.query
  
  // 处理全局搜索跳转
  if (query.keyword) {
    searchKeyword.value = query.keyword
    showSearchModal.value = true
    handleSearch()
  } else if (query.wrongBookPractice === 'true') {
    isWrongBookMode.value = true
    wrongBookSubject.value = null
    startWrongBookPracticeMode()
  } else if (query.wrongBookSubject) {
    isWrongBookMode.value = true
    wrongBookSubject.value = query.wrongBookSubject
    startWrongBookPracticeMode()
  }
})

// 开始错题练习模式
const startWrongBookPracticeMode = async () => {
  try {
    const res = await startWrongBookPractice(wrongBookSubject.value)
    if (!res.data || !res.data.question) {
      message.warning('暂无错题')
      return
    }
    
    // Set currentSubject for display in "Current Status" panel
    if (wrongBookSubject.value) {
      currentSubject.value = wrongBookSubject.value
    }

    currentQuestion.value = res.data.question
    currentIndex.value = res.data.currentIndex
    totalCount.value = res.data.totalCount
    practiceStore.setCurrentQuestion(res.data.question)
    userAnswer.value = null
    selectedAnswers.value = []
    
    addToHistory(res.data.question)
    
    message.success(`开始错题练习${wrongBookSubject.value ? ` (${wrongBookSubject.value})` : ''}，共 ${totalCount.value} 题`)
  } catch (error) {
    console.error('开始错题练习失败:', error)
    message.error('开始错题练习失败')
  }
}
onUnmounted(() => window.removeEventListener('keyup', handleKeyup))

// === Doodle Logic ===
const doodles = ref([])
const doodleIcons = [
  // Star
  "M50 5 L61 35 L95 35 L68 55 L79 85 L50 65 L21 85 L32 55 L5 35 L39 35 Z",
  // Spiral/Coil
  "M30,50 A20,20 0 1,1 70,50 A20,20 0 1,1 30,50 M35,50 A15,15 0 1,0 65,50 A15,15 0 1,0 35,50",
  // Wave
  "M10 50 Q 25 20, 40 50 T 70 50 T 100 50",
  // Bulb (Simple)
  "M35 20 C 25 20, 20 30, 20 45 C 20 55, 30 65, 35 70 L 35 80 L 65 80 L 65 70 C 70 65, 80 55, 80 45 C 80 30, 75 20, 65 20 Z M 40 85 L 60 85",
  // Arrow
  "M20 80 Q 50 20, 80 50 L 70 45 M 80 50 L 75 60",
  // Cross/Sparkle
  "M50 10 L50 90 M10 50 L90 50",
  // Triangle
  "M50 15 L85 80 L15 80 Z"
]

const generateDoodles = () => {
  const count = 12 // Number of doodles
  const newDoodles = []
  
  for (let i = 0; i < count; i++) {
    const isLeft = Math.random() > 0.5
    
    // Random position: 
    // Left side: 2% to 15%
    // Right side: 85% to 98%
    const xPos = isLeft 
      ? 2 + Math.random() * 13 
      : 85 + Math.random() * 13
      
    const yPos = Math.random() * 90 + 5 // 5% to 95% height
    
    const size = 30 + Math.random() * 40 // 30px to 70px
    const rotation = Math.random() * 360
    const delay = Math.random() * 5 // 0-5s delay
    const duration = 3 + Math.random() * 4 // 3-7s float duration
    
    newDoodles.push({
      path: doodleIcons[Math.floor(Math.random() * doodleIcons.length)],
      style: {
        left: `${xPos}%`,
        top: `${yPos}%`,
        width: `${size}px`,
        height: `${size}px`,
        transform: `rotate(${rotation}deg)`,
        animationDelay: `-${delay}s`, // start at random time
        animationDuration: `${duration}s`
      },
      opacity: 0.1 + Math.random() * 0.2 // 0.1 to 0.3 opacity
    })
  }
  doodles.value = newDoodles
}

const startPractice = async () => {
  try {
    console.log('======= 开始轮次练习 =======')
    console.log('筛选条件:', filters)

    // 获取选择的科目
    const subject = filters.subject && filters.subject.trim() !== '' ? filters.subject : null
    
    if (!subject) {
      // 如果未选择科目，使用原有的随机抽题模式
      const params = { ...filters, subject: undefined }
      const res = await getRandomQuestion(params)
      if (!res.data) {
        message.warning('没有找到符合条件的题目')
        return
      }
      currentQuestion.value = res.data
      practiceStore.setCurrentQuestion(res.data)
      userAnswer.value = null
      selectedAnswers.value = []
      
      // 添加到做题历史
      addToHistory(res.data)
      
      return
    }

    // 使用轮次 API
    currentSubject.value = subject
    const res = await startRound(subject)
    console.log('轮次响应:', res)
    
    if (!res.data || !res.data.question) {
      message.warning('该科目暂无题目')
      return
    }
    
    // --- 新增：检测是否有进度，提示用户是否重置 (手绘风格 Modal) ---
    if (res.data.currentIndex > 0) {
      resumeModalData.value = {
        subject: subject,
        currentIndex: res.data.currentIndex,
        totalCount: res.data.totalCount,
        roundData: res.data
      }
      showResumeModal.value = true
      return
    }
    // ------------------------------------------
    
    // 无进度或新轮次，直接应用
    applyRoundState(res.data)
    
  } catch (error) {
    console.error('获取题目失败:', error)
    message.error('获取题目失败')
  }
}

// 辅助函数：应用轮次状态
const applyRoundState = (data) => {
  currentQuestion.value = data.question
  currentIndex.value = data.currentIndex || 0
  totalCount.value = data.totalCount || 0
  roundNumber.value = data.roundNumber
  isRoundFinished.value = data.isFinished
  
  practiceStore.setCurrentQuestion(data.question)
  userAnswer.value = null
  selectedAnswers.value = []
  
  addToHistory(data.question, data.currentIndex)
  fetchRoundResults()
  
  console.log(`轮次进度: ${currentIndex.value + 1}/${totalCount.value}, 第${roundNumber.value}轮`)
  
  // 检查选择题是否有有效选项
  const needsOptions = ['single-choice', 'multiple-choice', 'choice'].includes(data.question.type)
  if (needsOptions) {
    nextTick().then(() => {
      if (options.value.length === 0) {
        console.error('题目选项无效，自动跳过', data.question)
        message.warning('题目数据有误，正在获取下一题...')
        setTimeout(() => nextQuestion(), 1000)
      }
    })
  }
}

const submitAnswer = async () => {
  if (!userAnswer.value) return
  try {
    await submitAnswerApi({
      questionId: currentQuestion.value.id,
      userAnswer: userAnswer.value
    })
    practiceStore.submitAnswer(userAnswer.value)
    
    // 保存到做题历史（用于上一题导航）
    // 如果当前不在历史末尾，说明是回顾旧题后提交了新答案，更新该记录
    if (historyIndex.value >= 0 && historyIndex.value < practiceHistory.value.length) {
      practiceHistory.value[historyIndex.value].userAnswer = userAnswer.value
    }
    
    if (isCorrect.value) message.success('回答正确！')
    else message.error('回答错误！')
    
    // 提交后刷新答题卡状态
    fetchRoundResults()
  } catch (error) {
    practiceStore.submitAnswer(userAnswer.value)
  }
}


// 添加题目到做题历史
const addToHistory = (question, rIndex = null) => {
  // 如果不在历史末尾，截断后面的历史（新分支）
  if (historyIndex.value < practiceHistory.value.length - 1) {
    practiceHistory.value = practiceHistory.value.slice(0, historyIndex.value + 1)
  }
  // 如果未传入 rIndex，尝试使用当前 currentIndex
  const idx = rIndex !== null ? rIndex : currentIndex.value
  practiceHistory.value.push({
    question: question,
    userAnswer: null,
    roundIndex: idx
  })
  historyIndex.value = practiceHistory.value.length - 1
}

// 上一题（从历史记录中获取）
const goToPrevQuestion = () => {
  if (historyIndex.value <= 0) {
    message.info('已是第一题')
    return
  }
  
  historyIndex.value--
  const record = practiceHistory.value[historyIndex.value]
  
  // 恢复题目和已选答案
  currentQuestion.value = record.question
  if (record.roundIndex !== undefined) currentIndex.value = record.roundIndex
  practiceStore.setCurrentQuestion(record.question)
  
  // 恢复用户之前的选择
  if (record.userAnswer) {
    userAnswer.value = record.userAnswer
    // 多选题需要恢复 selectedAnswers（userAnswer 是如 "AB" 这样的字符串）
    if (record.question.type === 'multiple-choice') {
      selectedAnswers.value = record.userAnswer.split('')
    } else {
      selectedAnswers.value = []
    }
    practiceStore.showAnalysis = true // 显示解析（因为已经答过）
  } else {
    userAnswer.value = null
    selectedAnswers.value = []
    practiceStore.showAnalysis = false
  }
  
  message.info(`返回第 ${historyIndex.value + 1} 题`)
}

// 下一题（在历史记录中前进）
const goToNextHistoryQuestion = () => {
  if (historyIndex.value >= practiceHistory.value.length - 1) {
    // 已经到历史末尾，退出回顾模式，获取新题
    message.info('已到最新题目，继续做新题')
    nextQuestion()
    return
  }
  
  historyIndex.value++
  const record = practiceHistory.value[historyIndex.value]
  
  // 恢复题目和已选答案
  currentQuestion.value = record.question
  if (record.roundIndex !== undefined) currentIndex.value = record.roundIndex
  practiceStore.setCurrentQuestion(record.question)
  
  // 恢复用户之前的选择
  if (record.userAnswer) {
    userAnswer.value = record.userAnswer
    // 多选题需要恢复 selectedAnswers（userAnswer 是如 "AB" 这样的字符串）
    if (record.question.type === 'multiple-choice') {
      selectedAnswers.value = record.userAnswer.split('')
    } else {
      selectedAnswers.value = []
    }
    practiceStore.showAnalysis = true
  } else {
    userAnswer.value = null
    selectedAnswers.value = []
    practiceStore.showAnalysis = false
  }
  
  message.info(`前进到第 ${historyIndex.value + 1} 题`)
}

const nextQuestion = async () => {
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = []
  
  // 错题练习模式
  if (isWrongBookMode.value) {
    try {
      const res = await nextWrongQuestion({
        subject: wrongBookSubject.value,
        currentQuestionId: currentQuestion.value.id
      })
      
      if (res.data.isFinished) {
        dialog.success({
          title: '🎉 恭喜完成！',
          content: `错题练习已全部完成！`,
          positiveText: '返回错题本',
          negativeText: '继续练习',
          onPositiveClick: () => {
            router.push('/wrong-book')
          },
          onNegativeClick: () => {
            isWrongBookMode.value = false
            wrongBookSubject.value = null
            router.replace('/practice')
          }
        })
        return
      }
      
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      totalCount.value = res.data.totalCount
      practiceStore.setCurrentQuestion(res.data.question)
      addToHistory(res.data.question, res.data.currentIndex)
      
    } catch (error) {
      console.error('获取下一题失败:', error)
      message.error('获取下一题失败')
    }
    return
  }
  
  // 如果没有选择科目（随机模式），使用原有逻辑
  if (!currentSubject.value) {
    await startPractice()
    return
  }
  
  try {
    const res = await nextRoundQuestion(currentSubject.value)
    console.log('下一题响应:', res)
    if (res.data && (res.data.totalCount || res.data.totalCount === 0)) {
      totalCount.value = res.data.totalCount
    }
    
    // 检查是否完成本轮
    if (res.data.isFinished && !res.data.question) {
      isRoundFinished.value = true
      // 显示完成对话框
      dialog.success({
        title: '🎉 恭喜完成！',
        content: `您已完成第 ${roundNumber.value} 轮练习，共 ${totalCount.value} 道题目！即将进入第 ${roundNumber.value + 1} 轮，是否继续？`,
        positiveText: '开始新一轮',
        negativeText: '返回首页',
        onPositiveClick: async () => {
          const resetRes = await startRound(currentSubject.value)
          if (resetRes.data && resetRes.data.question) {
            currentQuestion.value = resetRes.data.question
            currentIndex.value = 0
            totalCount.value = resetRes.data.totalCount
            roundNumber.value = resetRes.data.roundNumber
            isRoundFinished.value = false
            practiceStore.setCurrentQuestion(resetRes.data.question)
            message.success(`已开始第 ${roundNumber.value} 轮练习！`)
          }
        },
        onNegativeClick: () => {
          exitPractice()
        }
      })
      return
    }
    
    // 更新状态
    if (res.data.question) {
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      practiceStore.setCurrentQuestion(res.data.question)
      
      // 添加到做题历史
      addToHistory(res.data.question, res.data.currentIndex)
    } else {
      // 没有返回题目，可能是本轮已完成
      message.info('本轮已完成，请开始新一轮')
    }
    
  } catch (error) {
    console.error('获取下一题失败:', error)
    message.error('获取下一题失败')
  }
}

// 上一题
const prevQuestion = async () => {
  if (!currentSubject.value || currentIndex.value <= 0) return
  
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = []
  
  try {
    const res = await prevRoundQuestion(currentSubject.value)
    console.log('上一题响应:', res)
    if (res.data && (res.data.totalCount || res.data.totalCount === 0)) {
      totalCount.value = res.data.totalCount
    }
    
    if (res.data.question) {
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      practiceStore.setCurrentQuestion(res.data.question)
    } else {
      message.info('已是第一题')
    }
  } catch (error) {
    console.error('获取上一题失败:', error)
    message.error('获取上一题失败')
  }
}

// 搜索方法
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    message.warning('请输入搜索关键词')
    return
  }
  
  try {
    const res = await searchQuestions({
      keyword: searchKeyword.value,
      page: 1,
      size: 10
    })
    if (res.data && res.data.records) {
      searchResults.value = res.data.records
      if (res.data.records.length === 0) {
        message.info('未找到相关题目')
      }
    }
  } catch (error) {
    console.error('搜索失败:', error)
    message.error('搜索失败')
  }
}

// 跳转到指定题目
const jumpToQuestion = (question) => {
  showSearchModal.value = false
  searchKeyword.value = ''
  searchResults.value = []
  
  currentQuestion.value = question
  practiceStore.setCurrentQuestion(question)
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = []
  message.success(`已跳转到题目 #${question.id}`)
}



const showExitModal = ref(false)
const showResetModal = ref(false) // New Reset Modal State

const exitPractice = () => {
  showExitModal.value = true
}

const handleConfirmExit = () => {
  showExitModal.value = false
  router.back()
}

// 确认重置逻辑 (From Modal)
const confirmResetAction = async () => {
    try {
    const resetRes = await resetRound(currentSubject.value)
    if (resetRes.data && resetRes.data.question) {
        applyRoundState(resetRes.data)
        roundResults.value = {} // 清空答题卡状态
        zenTime.value = 0 // 重置计时器
        practiceHistory.value = [] // 清空做题历史
        historyIndex.value = -1
        message.success('已重置本轮，轮次计数保持不变，加油再来一次！')
    }
    } catch (e) {
    message.error('重置失败，请重试')
    }
    showResetModal.value = false
}

// 手动重置本轮（从按钮触发）
const handleResetRound = () => {
  if (!currentSubject.value) {
    message.warning('当前为随机模式，无法重置轮次')
    return
  }
  showResetModal.value = true
}

</script>

<style scoped>
/* 全局变量定义在组件作用域内 */
.practice-container {
  --paper-bg: #fffdf7; /* 更亮、更干净的纸张色 */
  --shadow-hard: 4px 4px 0px rgba(0,0,0,0.15);
  
  /* max-width: 850px; REMOVED for global scroll */
  width: 100%;
  margin: 0;
  min-height: 100vh;
  display: block; /* changed from flex center to block for scrollbar */
  padding: 0;
  overflow: hidden;
  
  /* 核心背景风格：点阵纸张 - 更淡雅 */
  background-color: var(--paper-bg);
  background-image: radial-gradient(rgba(0,0,0,0.08) 2px, transparent 2px);
  background-size: 24px 24px;
  
  /* 核心字体 */
  font-family: 'Patrick Hand', cursive;
  color: #2c3e50;
}

/* Filter Configuration */
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
  max-width: 500px; /* Constrain width to look like a book */
  margin: 40px auto;
  position: relative;
}

/* Spiral Binding - Silver/Grey Metal Look */
.spiral-binding-left {
  width: 40px;
  background: transparent; /* Let rings float */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-evenly;
  padding: 30px 0;
  z-index: 10;
  margin-right: -15px; /* Overlap with cover */
}

.ring {
  width: 45px;
  height: 14px;
  background: linear-gradient(to bottom, #bdc3c7 0%, #e0e0e0 40%, #95a5a6 100%); /* Metallic silver */
  border-radius: 4px;
  margin: 12px 0;
  box-shadow: 
    0 2px 4px rgba(0,0,0,0.2),
    inset 0 1px 0 rgba(255,255,255,0.8);
  transform: rotate(-3deg);
  position: relative;
}
.ring::after {
  /* Hole visual */
  content: '';
  position: absolute;
  right: -5px;
  top: 2px;
  width: 10px;
  height: 10px;
  background: #333;
  border-radius: 50%;
  opacity: 0.2;
}

.cover-body {
  flex: 1;
  background: #fff; /* Pure white paper */
  border: 2px solid #2c3e50;
  border-radius: 4px 12px 12px 4px; /* Slightly rounded right corners */
  padding: 40px;
  position: relative;
  min-height: 600px;
  
  /* Deep shadow for thickness */
  box-shadow: 
    5px 5px 0 #bdc3c7, /* Page thickness */
    10px 10px 20px rgba(0,0,0,0.15); /* Drop shadow */
  
  /* Texture: Dot Grid */
  background-image: radial-gradient(#cbd5e1 1.5px, transparent 1.5px);
  background-size: 24px 24px;
  
  display: flex;
  flex-direction: column;
}

.doodle-sun {
  position: absolute;
  top: 30px;
  right: 30px;
  /* Sun doodle style */
}

.hand-title-large {
  font-family: 'Gochi Hand', cursive;
  font-size: 40px;
  color: #2c3e50;
  margin-top: 20px;
  margin-bottom: 8px;
  text-align: center;
  font-weight: bold;
  letter-spacing: 2px;
}

.hand-subtitle-large {
  text-align: center;
  font-family: 'Gochi Hand', cursive;
  font-size: 16px;
  color: #94a3b8;
  margin-bottom: 50px;
}

/* Form Styles */
.hand-label {
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  margin-bottom: 8px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.sketch-select .n-base-selection) {
  border: 2px solid #2c3e50 !important;
  border-radius: 8px !important; /* Straighter edges for book look */
  background: #fff !important;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
  min-height: 48px;
  transition: all 0.2s;
}
:deep(.sketch-select .n-base-selection:hover) {
  transform: translate(-1px, -1px);
  box-shadow: 5px 5px 0 rgba(0,0,0,0.15);
}

/* Custom Sketch Buttons - Red/Pink CTA */
.sketch-btn-main {
  position: relative;
  width: 100%;
  height: 64px;
  border: 3px solid #1e293b;
  background: #ff6b6b; /* The Red/Pink color from screenshot */
  border-radius: 8px; /* Slightly rounded */
  cursor: pointer;
  padding: 0;
  outline: none;
  transition: all 0.2s;
  margin-top: 30px;
  box-shadow: 6px 6px 0 #1e293b;
}

.sketch-btn-main .btn-text {
  width: 100%; height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-family: 'Gochi Hand', cursive;
  font-size: 26px;
  letter-spacing: 2px;
}
/* Remove old layered elements if any */
.sketch-btn-main .btn-bg { display: none; }

.sketch-btn-main:hover {
  transform: translate(-2px, -2px);
  box-shadow: 8px 8px 0 #1e293b;
  background-color: #ff5252;
}
.sketch-btn-main:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 #1e293b;
}

/* 必须穿透 NCard 的样式来应用手绘风 (Remnant from old code, keeping if used elsewhere, else harmless) */
:deep(.n-card) {
  background-color: #fff;
  border: 2px solid #2c3e50 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  box-shadow: var(--shadow-hard) !important;
}

/* Deprecated Config Header & Start Button Styles replaced by above, but keeping .config-header class logic if used generically */
.config-header { text-align: center; margin-bottom: 24px; }

/* Question Wrapper */
.question-wrapper { width: 100%; max-width: 760px; position: relative; }

/* 顶部工具栏 */
.practice-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.progress-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 8px 20px;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  box-shadow: 3px 3px 0px rgba(0,0,0,0.1);
  font-family: 'Patrick Hand', cursive;
  transition: all 0.2s ease;
}

.progress-chip:hover {
  transform: scale(1.02) rotate(-1deg);
  box-shadow: 4px 4px 0px rgba(0,0,0,0.15);
}

.chip-icon {
  font-size: 18px;
}

.chip-text {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.chip-divider {
  color: #cbd5e1;
  font-weight: 300;
}

.chip-total {
  font-size: 14px;
  color: #64748b;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255,255,255,0.9);
  border: 2px solid #e2e8f0;
  color: #64748b;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.toolbar-btn:hover {
  background: #f8fafc;
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.toolbar-btn.close:hover {
  border-color: #ef4444;
  color: #ef4444;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}
.progress-info { display: flex; align-items: center; gap: 12px; flex: 1; }
.progress-label { font-size: 14px; font-weight: 700; color: #57606a; transform: rotate(-2deg); }
.progress-text { 
  font-size: 16px; 
  font-weight: 700; 
  color: #2c3e50; 
  background: #fff;
  border: 2px solid #2c3e50;
  padding: 2px 10px; 
  border-radius: 12px 22px 14px 24px;
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
  transform: rotate(1deg);
}
.close-btn { 
  color: #2c3e50; 
  transition: all 0.2s; 
  font-weight: bold;
}
.close-btn:hover { 
  transform: rotate(90deg) scale(1.1); 
  color: #ef4444;
}

/* Round Badge */
.round-badge {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #10b981, #059669);
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: 8px;
  transform: rotate(-2deg);
}

/* Search Button */
.search-btn {
  color: #2c3e50;
  margin-right: 8px;
  transition: all 0.2s;
}
.search-btn:hover {
  transform: scale(1.1);
  color: #3b82f6;
}

/* Search Modal */
.search-modal-content {
  font-family: 'Patrick Hand', cursive;
}
.search-results {
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
}
.search-result-item {
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-result-item:hover {
  border-color: #3b82f6;
  background: #f0f9ff;
}
.result-id {
  font-weight: bold;
  color: #3b82f6;
  flex-shrink: 0;
}
.result-content {
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Navigation Buttons */
.nav-btn {
  font-family: 'Patrick Hand', cursive;
  font-size: 16px;
  color: #64748b;
  border: 2px solid #cbd5e1;
  border-radius: 20px;
  transition: all 0.2s;
}
.nav-btn:hover:not(:disabled) {
  color: #2c3e50;
  border-color: #2c3e50;
  transform: translateX(-2px);
}
.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.nav-placeholder {
  width: 100px;
}

/* Paper Card Effect - The Main Stage */
.paper-effect {
  background: #fffdf7; /* Use explicit creamy paper color for the card only */
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  position: relative;
  
  /* Deep shadow for "floating on desk" effect */
  box-shadow: 
    0 1px 1px rgba(0,0,0,0.05), 
    0 2px 2px rgba(0,0,0,0.05), 
    0 4px 4px rgba(0,0,0,0.05), 
    0 8px 8px rgba(0,0,0,0.05),
    0 16px 16px rgba(0,0,0,0.05);
    
  padding: 36px 40px; /* 减少内边距 */
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.paper-effect:hover {
  transform: rotate(-0.3deg);
  box-shadow: 6px 6px 0px rgba(0,0,0,0.2);
}

/* Holes decoration */
.paper-holes {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 32px;
  height: 32px;
  background-image: radial-gradient(#2c3e50 20%, transparent 20%);
  background-size: 8px 8px;
  opacity: 0.1;
  transform: rotate(15deg);
}

.question-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-start; 
  margin-bottom: 24px; 
  border-bottom: 2px dashed #cbd5e1;
  padding-bottom: 12px;
}
.type-tag {
  border: 2px solid #2c3e50 !important;
  font-weight: bold;
  font-family: 'Patrick Hand', cursive;
  box-shadow: 1px 1px 0px rgba(0,0,0,0.1);
}
.subject-text { margin-left: 10px; font-size: 14px; color: #57606a; font-weight: 700; font-family: 'Gochi Hand', cursive; letter-spacing: 1px; }

.question-content { 
  font-size: 24px; /* 恢复较大的字体 */
  font-weight: 600; 
  line-height: 1.6; 
  color: #2c3e50; 
  margin-bottom: 32px; 
  font-family: 'Didact Gothic', 'Patrick Hand', sans-serif;
}

/* Options as Doodle Boxes */
.options-list { display: flex; flex-direction: column; gap: 16px; }

.option-item {
  display: flex; align-items: center; padding: 14px 20px; /* 减少选项内边距 */
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  cursor: pointer; 
  transition: all 0.2s; 
  background: #fff;
  position: relative;
  min-height: 50px;
}

.option-item:hover:not(.disabled) { 
  border-color: #2c3e50; 
  background: #fff; 
  transform: scale(1.01) rotate(-0.3deg); 
  box-shadow: 3px 3px 0px rgba(0,0,0,0.1); 
}

/* Selected: Marker Effect */
.option-item.selected { 
  border-color: #2c3e50; 
  background: #a2d2ff; /* 淡蓝色记号笔 */
  box-shadow: 3px 3px 0px #2c3e50;
  transform: rotate(0.5deg);
}

/* Analysis States */
.option-item.correct-highlight { 
  background-color: #ccfbf1;
  border-color: #10b981;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.15);
}

.option-item.error-highlight { 
  background-color: #fee2e2;
  border-color: #ef4444;
  background-image: repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(239, 68, 68, 0.05) 10px, rgba(239, 68, 68, 0.05) 20px);
}

.option-key { 
  width: 30px; height: 30px; 
  background: #fff; 
  border: 2px solid #2c3e50;
  border-radius: 50% 40% 60% 50% / 40% 50% 60% 50%;
  display: flex; align-items: center; justify-content: center; 
  font-weight: 700; color: #2c3e50; 
  margin-right: 16px; font-size: 16px;
  font-family: 'Gochi Hand', cursive;
  box-shadow: 1px 1px 0px rgba(0,0,0,0.1);
  transition: all 0.2s;
  flex-shrink: 0;
}

.option-item.selected .option-key { 
  background: #2c3e50; 
  color: #fff;
  transform: rotate(-8deg);
}

.option-text { flex: 1; font-size: 17px; color: #334155; font-weight: 600; line-height: 1.35; } 
.center-text { text-align: center; font-size: 20px; font-family: 'Gochi Hand', cursive; }

/* Checkbox Style */
.option-checkbox {
  width: 24px; height: 24px; border: 2px solid #2c3e50; margin-right: 14px;
  border-radius: 4px 8px 3px 9px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.option-checkbox.checked { background: #2c3e50; color: #fff; }

/* Action Bar with Decorations */
.action-bar { 
  margin-top: 40px; 
  display: flex; 
  justify-content: center; 
  align-items: center;
  position: relative;
}

.action-btn { 
  min-width: 140px; 
  height: 52px; 
  font-size: 20px !important; 
  font-family: 'Gochi Hand', cursive; 
  font-weight: 600; 
  letter-spacing: 1px;
  border: 2px solid #2c3e50 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  box-shadow: 4px 4px 0px #2c3e50 !important;
  color: #2c3e50 !important;
  background-color: #fcd34d !important; /* Restore Yellow */
  transition: all 0.2s !important;
}

/* === NEW STYLES for Full Screen + Doodles === */

/* 1. Reset Container to Full Screen */
.practice-container {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden; /* Contains the doodles */
  
  display: flex;
  justify-content: center;
  align-items: flex-start; /* Align top for better scrolling */
  padding: 16px;
  
  /* Transparent background to show Layout's industrial dot matrix */
  background: transparent;
  
  /* Core variables */
  --paper-bg: #fffdf7;
  
  font-family: 'Patrick Hand', cursive;
  color: #2c3e50;
}

/* 2. Doodles Layer - Pencil Sketches on Desk */
.doodles-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* Let clicks pass through */
  z-index: 0;
}

.doodle-item {
  position: absolute;
  color: #94a3b8; /* Slate-400 for doodles - pencil lead color */
  mix-blend-mode: multiply; /* Blend into the desk */
  animation: float ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(2deg); }
}

/* 3. Content Wrapper (The "Safe Zone" for valid UI) */
.content-wrapper {
  position: relative;
  z-index: 1; /* Above doodles */
  width: 100%;
  max-width: 1400px; /* Expanded for 3 columns */
  display: flex;
  justify-content: center;
  padding: 0 16px;
}
.content-wrapper.has-question {
  align-items: flex-start;
}

/* Filter panel stays centered */
.filter-panel { 
  width: 100%;
  max-width: 480px; 
  margin: auto; /* Vertically center in flex column */
}

/* Practice Layout (Grid/Flex for 3 cols) */
.practice-main-layout {
  display: flex;
  gap: 24px;
  width: 100%;
  justify-content: center;
  align-items: flex-start;
  min-width: 0; /* 防止 flex 溢出问题 */
}

/* Adjust question wrapper for layout */
.question-wrapper {
  width: 100%;
  max-width: 760px;
  flex: 1; /* Allow it to fill available space */
  flex-shrink: 1; /* Allow shrinking */
  min-width: 0; /* Prevent overflow */
}

.action-btn:hover {
  transform: translate(-1px, -1px) rotate(-1deg);
  box-shadow: 6px 6px 0px #2c3e50 !important;
}
.action-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px #2c3e50 !important;
}
.action-btn:disabled {
  background-color: #e2e8f0 !important;
  color: #94a3b8 !important;
  border-color: #cbd5e1 !important;
  box-shadow: none !important;
  transform: none !important;
}

/* Decor Arrow */
.decor-arrow-next {
  position: absolute;
  right: -50px;
  top: 50%;
  transform: translateY(-50%) rotate(-10deg);
  width: 50px;
  opacity: 0.7;
  pointer-events: none;
}
.decor-arrow-prev {
  position: absolute;
  left: -50px;
  top: 50%;
  transform: translateY(-50%) rotate(190deg);
  width: 50px;
  opacity: 0.7;
  pointer-events: none;
}

/* Analysis Box */
.analysis-box {
  margin-top: 32px;
  background: #fff;
  border: 2px dashed #2c3e50;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.05);
  transform: rotate(0.2deg);
}
.analysis-title { 
  display: flex; align-items: center; gap: 8px; 
  font-weight: 700; color: #2c3e50; font-size: 18px; 
  font-family: 'Gochi Hand', cursive;
}
.analysis-content { padding-left: 0; border-left: none; margin-top: 12px; }
.correct-answer-row { font-size: 16px; margin-bottom: 8px; font-family: 'Patrick Hand', cursive; }
.correct-answer-row .value { color: #10b981; font-weight: 700; font-size: 20px; text-decoration: underline; text-decoration-style: wavy;}

/* Animations */
.slide-up-enter-active { transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55); } 
.slide-up-enter-from { opacity: 0; transform: translateY(30px) rotate(3deg); }
.fade-leave-active { transition: opacity 0.2s; }
.fade-leave-to { opacity: 0; }

/* === Side Panel Styles === */
.side-panel {
  width: 280px; /* Increased from default */
  background: #fffdf7; /* Creamy paper bg */
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  padding: 24px;
  position: sticky;
  top: 0;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  /* Premium Shadow Effect */
  box-shadow: 
    0 4px 6px rgba(0,0,0,0.02),
    4px 4px 0 rgba(44, 62, 80, 0.9); /* Solid shadow for comic look */
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.side-panel:hover {
  transform: translateY(-4px) rotate(1deg);
  box-shadow: 6px 6px 0 rgba(44, 62, 80, 0.9);
}

.side-header {
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  color: #2c3e50;
  border-bottom: 2px dashed #cbd5e1;
  padding-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.side-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Left Panel Specifics */
.subject-card {
  position: relative;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid #2c3e50;
  box-shadow: 4px 4px 0 rgba(44, 62, 80, 0.1);
  overflow: hidden;
  text-align: center;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.subject-card:hover { 
  transform: translateY(-2px) scale(1.02);
  box-shadow: 6px 6px 0 rgba(44, 62, 80, 0.15);
}

/* Notebook lines background effect */
.subject-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: linear-gradient(#f1f5f9 1px, transparent 1px);
  background-size: 100% 20px;
  opacity: 0.5;
  pointer-events: none;
}

/* Red line margin effect */
.subject-card::after {
  content: '';
  position: absolute;
  top: 0; bottom: 0; left: 20px;
  width: 2px;
  background: #fca5a5;
  opacity: 0.6;
}

.subject-label { 
  position: relative;
  font-size: 12px; 
  color: #94a3b8; 
  margin-bottom: 2px; 
  font-weight: 700; 
  text-align: right;
  z-index: 1;
}

.subject-main-text { 
  position: relative;
  font-weight: 800; 
  font-size: 24px; 
  color: #2c3e50; 
  line-height: 1.2; 
  font-family: 'Gochi Hand', cursive; 
  z-index: 1;
  padding-left: 10px; /* Offset for red line */
}

.subject-sub-text {
  position: relative;
  display: inline-block;
  background: #fee2e2;
  color: #ef4444;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  border: 2px dashed #fca5a5;
  transform: rotate(-2deg);
  margin-top: 6px;
  box-shadow: 2px 2px 0 rgba(239, 68, 68, 0.1);
  z-index: 1;
  font-family: 'Patrick Hand', cursive;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border: 2px solid #2c3e50;
  border-radius: 12px;
  padding: 12px 8px;
  box-shadow: 2px 2px 0 rgba(0,0,0,0.1);
  transition: transform 0.2s;
}
.stat-item:hover { transform: translateY(-2px); }

.stat-val { font-size: 28px; font-weight: 700; font-family: 'Gochi Hand', cursive; color: #2c3e50; line-height: 1; }
.stat-label { font-size: 13px; color: #64748b; margin-top: 4px; font-weight: 600; }

.mode-tag {
  background: #fee2e2;
  color: #ef4444;
  text-align: center;
  padding: 6px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  border: 2px solid #ef4444;
  box-shadow: 2px 2px 0 rgba(239, 68, 68, 0.2);
}

/* Right Panel specific */
.sheet-info { text-align: right; font-size: 14px; color: #64748b; margin-bottom: 12px; font-weight: 600; }
.bubble-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}
.bubble {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  font-size: 14px;
  color: #64748b;
  cursor: default;
  transition: all 0.2s;
  font-family: 'Fredoka One', cursive, sans-serif;
  background: #fff;
}
.bubble.done {
  background: #e2e8f0;
  border-color: #94a3b8;
  color: #475569;
}
.bubble.active {
  border-color: #2c3e50;
  background: #2c3e50;
  color: #fff;
  box-shadow: 3px 3px 0 rgba(0,0,0,0.2);
  transform: scale(1.15);
  font-weight: bold;
}
.empty-sheet {
  text-align: center; color: #94a3b8; padding: 20px 0; font-style: italic;
}
.side-footer { margin-top: auto; display: flex; align-items: center; justify-content: space-between; gap: 8px; }

.reset-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 8px;
  background: #fff8e1; /* Soft yellow background matching theme */
  border: 1px dashed #f59e0b; /* Amber dashed border */
  color: #d97706;
  font-family: 'Gochi Hand', cursive;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 2px 2px 0 rgba(245, 158, 11, 0.2);
}

.reset-wrapper:hover {
  transform: translateY(-2px) rotate(1deg);
  background: #fffbeb;
  box-shadow: 3px 3px 0 rgba(245, 158, 11, 0.3);
}

.reset-wrapper:active {
  transform: translateY(1px);
  box-shadow: 1px 1px 0 rgba(245, 158, 11, 0.2);
}

.reset-icon { font-size: 16px; }

/* Responsive adjustments */
@media (max-width: 1200px) {
  .side-panel { display: none; } /* 仅在小屏幕下隐藏 */
  .question-wrapper { width: 100%; max-width: 800px; margin: 0 auto; }
}

@media (min-width: 1201px) and (max-width: 1350px) {
  /* 中等屏幕：稍微缩小侧边栏和间距 */
  .side-panel { width: 220px; padding: 16px; }
  .practice-main-layout { gap: 16px; }
  .question-wrapper { max-width: 680px; }
  .stat-val { font-size: 22px; }
}


/* Scrollbar & Layout Container Fixes - Appended */
.practice-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--paper-bg);
  position: relative;
}

.global-scrollbar {
  height: 100%;
}


:deep(.practice-layout-container) {
  min-height: 100%;
  position: relative;
  display: flex;
  box-sizing: border-box;
  padding: 40px 16px;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  gap: 24px;
}



/* Practice Layout (Content inside scrollbar) */
.practice-main-layout {
  min-width: 0;
  max-width: 1400px; /* Re-added centering constraint */
  margin: 0 auto;    /* Re-added centering constraint */
}

/* Zen card & Answer Sheet Enhancements - Appended */
.zen-card {
  margin-top: 16px;
  background: linear-gradient(135deg, rgba(255,255,255,0.7) 0%, rgba(240,249,255,0.7) 100%);
}

.zen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.zen-title {
  font-size: 16px;
  font-weight: 800;
  color: #2c3e50;
  font-family: 'Inter', sans-serif;
}

.zen-status {
  font-size: 12px;
  color: #10b981;
  background: #ecfdf5;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.zen-content {
  text-align: center;
  padding: 8px 0;
}

.zen-timer {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  font-family: 'Fredoka One', cursive, sans-serif;
  margin-bottom: 16px;
}

.zen-breath-wrapper {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.zen-breath-circle {
  width: 20px;
  height: 20px;
  background: #3b82f6;
  border-radius: 50%;
  animation: breathe 4s ease-in-out infinite;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.4);
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.5; box-shadow: 0 0 5px rgba(59, 130, 246, 0.2); }
  50% { transform: scale(2.5); opacity: 0.8; box-shadow: 0 0 20px rgba(59, 130, 246, 0.5); }
}

.zen-quote {
  font-size: 13px;
  color: #64748b;
  font-style: italic;
  line-height: 1.5;
  min-height: 40px;
}

/* 禅意专注 - 防走神动画 */
.zen-title.text-danger { color: #f87171 !important; font-weight: bold; font-family: 'Gochi Hand', cursive; }
.zen-status.status-danger {
  background: #fecaca; color: #b91c1c; border-color: #f87171;
  animation: pulse-danger 1s infinite;
}
.zen-timer.blur-text { font-size: 28px; color: #ef4444; font-weight: bold; }
.zen-breath-circle.circle-danger {
  background: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
  animation: shake-hard 0.5s infinite;
}

@keyframes shake-hard {
  0% { transform: translate(1px, 1px) rotate(0deg); }
  10% { transform: translate(-1px, -2px) rotate(-1deg); }
  20% { transform: translate(-3px, 0px) rotate(1deg); }
  30% { transform: translate(3px, 2px) rotate(0deg); }
  40% { transform: translate(1px, -1px) rotate(1deg); }
  50% { transform: translate(-1px, 2px) rotate(-1deg); }
  60% { transform: translate(-3px, 1px) rotate(0deg); }
  70% { transform: translate(3px, 1px) rotate(-1deg); }
  80% { transform: translate(-1px, -1px) rotate(1deg); }
  90% { transform: translate(1px, 2px) rotate(0deg); }
  100% { transform: translate(1px, -2px) rotate(-1deg); }
}

@keyframes pulse-danger {
  0% { transform: scale(1); box-shadow: 0 0 10px rgba(239, 68, 68, 0.5); }
  50% { transform: scale(1.05); box-shadow: 0 0 30px rgba(239, 68, 68, 0.8); }
  100% { transform: scale(1); box-shadow: 0 0 10px rgba(239, 68, 68, 0.5); }
}

.distracted-shake {
  /* Vivid Red Warning Style */
  border: 4px solid #ef4444 !important;
  background: #fecaca !important; /* Soft Red Background */
  transform: rotate(-2deg);
  box-shadow: 0 0 50px rgba(239, 68, 68, 0.6) !important;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: shake-hard 0.6s infinite; /* Continuous shaking */
}
/* Ensure text contrasts well with red bg */
.distracted-shake .zen-title, 
.distracted-shake .zen-timer,
.distracted-shake .zen-quote {
  color: #b91c1c !important; /* Dark Red Text */
  text-shadow: 1px 1px 0px rgba(255,255,255,0.5);
}

/* Bubble Statuses */
.bubble {
  cursor: pointer !important;
  font-weight: 600;
  user-select: none;
}

.bubble:hover {
  transform: scale(1.1);
  border-color: #3b82f6;
}

.bubble.correct {
  background: #dcfce7 !important;
  border-color: #22c55e !important;
  color: #15803d !important;
}

.bubble.wrong {
  background: #fee2e2 !important;
  border-color: #ef4444 !important;
  color: #b91c1c !important;
}

.bubble.active {
  background: #1e293b !important;
  border-color: #1e293b !important;
  color: #fff !important;
  transform: scale(1.1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* Pagination */
.sheet-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}

.page-num {
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
  min-width: 40px;
  text-align: center;
}

/* Filter Configuration */
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
  max-width: 500px; /* Constrain width to look like a book */
  margin: 40px auto;
  position: relative;
}

/* Spiral Binding - Silver/Grey Metal Look */
.spiral-binding-left {
  width: 40px;
  background: transparent; /* Let rings float */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-evenly;
  padding: 30px 0;
  z-index: 10;
  margin-right: -15px; /* Overlap with cover */
}

.ring {
  width: 45px;
  height: 14px;
  background: linear-gradient(to bottom, #bdc3c7 0%, #e0e0e0 40%, #95a5a6 100%); /* Metallic silver */
  border-radius: 4px;
  margin: 12px 0;
  box-shadow: 
    0 2px 4px rgba(0,0,0,0.2),
    inset 0 1px 0 rgba(255,255,255,0.8);
  transform: rotate(-3deg);
  position: relative;
}
.ring::after {
  /* Hole visual */
  content: '';
  position: absolute;
  right: -5px;
  top: 2px;
  width: 10px;
  height: 10px;
  background: #333;
  border-radius: 50%;
  opacity: 0.2;
}

.cover-body {
  flex: 1;
  background: #fff; /* Pure white paper */
  border: 2px solid #2c3e50;
  border-radius: 4px 12px 12px 4px; /* Slightly rounded right corners */
  padding: 40px;
  position: relative;
  min-height: 600px;
  
  /* Deep shadow for thickness */
  box-shadow: 
    5px 5px 0 #bdc3c7, /* Page thickness */
    10px 10px 20px rgba(0,0,0,0.15); /* Drop shadow */
  
  /* Texture: Dot Grid */
  background-image: radial-gradient(#cbd5e1 1.5px, transparent 1.5px);
  background-size: 24px 24px;
  
  display: flex;
  flex-direction: column;
}

.doodle-sun {
  position: absolute;
  top: 30px;
  right: 30px;
  /* Sun doodle style */
}

.hand-title-large {
  font-family: 'Gochi Hand', cursive;
  font-size: 40px;
  color: #2c3e50;
  margin-top: 20px;
  margin-bottom: 8px;
  text-align: center;
  font-weight: bold;
  letter-spacing: 2px;
}

.hand-subtitle-large {
  text-align: center;
  font-family: 'Gochi Hand', cursive;
  font-size: 16px;
  color: #94a3b8;
  margin-bottom: 50px;
}

/* Form Styles */
.hand-label {
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  margin-bottom: 8px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.sketch-select .n-base-selection) {
  border: 2px solid #2c3e50 !important;
  border-radius: 8px !important; /* Straighter edges for book look */
  background: #fff !important;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
  min-height: 48px;
  transition: all 0.2s;
}
:deep(.sketch-select .n-base-selection:hover) {
  transform: translate(-1px, -1px);
  box-shadow: 5px 5px 0 rgba(0,0,0,0.15);
}

/* Custom Sketch Buttons - Red/Pink CTA */
.sketch-btn-main {
  position: relative;
  width: 100%;
  height: 64px;
  border: 3px solid #1e293b;
  background: #ff6b6b; /* The Red/Pink color from screenshot */
  border-radius: 8px; /* Slightly rounded */
  cursor: pointer;
  padding: 0;
  outline: none;
  transition: all 0.2s;
  margin-top: 30px;
  box-shadow: 6px 6px 0 #1e293b;
}

.sketch-btn-main .btn-text {
  width: 100%; height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-family: 'Gochi Hand', cursive;
  font-size: 26px;
  letter-spacing: 2px;
}
/* Remove old layered elements if any */
.sketch-btn-main .btn-bg { display: none; }

.sketch-btn-main:hover {
  transform: translate(-2px, -2px);
  box-shadow: 8px 8px 0 #1e293b;
  background-color: #ff5252;
}
.sketch-btn-main:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 #1e293b;
}

/* 必须穿透 NCard 的样式来应用手绘风 (Remnant from old code, keeping if used elsewhere, else harmless) */
:deep(.n-card) {
  background-color: #fff;
  border: 2px solid #2c3e50 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  box-shadow: var(--shadow-hard) !important;
}

/* Deprecated Config Header & Start Button Styles replaced by above, but keeping .config-header class logic if used generically */
.config-header { text-align: center; margin-bottom: 24px; }

/* Question Wrapper */
.question-wrapper { width: 100%; max-width: 760px; position: relative; }

/* 顶部工具栏 */
.practice-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.progress-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 8px 20px;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  box-shadow: 3px 3px 0px rgba(0,0,0,0.1);
  font-family: 'Patrick Hand', cursive;
  transition: all 0.2s ease;
}

.progress-chip:hover {
  transform: scale(1.02) rotate(-1deg);
  box-shadow: 4px 4px 0px rgba(0,0,0,0.15);
}

.chip-icon {
  font-size: 18px;
}

.chip-text {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.chip-divider {
  color: #cbd5e1;
  font-weight: 300;
}

.chip-total {
  font-size: 14px;
  color: #64748b;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255,255,255,0.9);
  border: 2px solid #e2e8f0;
  color: #64748b;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.toolbar-btn:hover {
  background: #f8fafc;
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.toolbar-btn.close:hover {
  border-color: #ef4444;
  color: #ef4444;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}
.progress-info { display: flex; align-items: center; gap: 12px; flex: 1; }
.progress-label { font-size: 14px; font-weight: 700; color: #57606a; transform: rotate(-2deg); }
.progress-text { 
  font-size: 16px; 
  font-weight: 700; 
  color: #2c3e50; 
  background: #fff;
  border: 2px solid #2c3e50;
  padding: 2px 10px; 
  border-radius: 12px 22px 14px 24px;
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
  transform: rotate(1deg);
}
.close-btn { 
  color: #2c3e50; 
  transition: all 0.2s; 
  font-weight: bold;
}
.close-btn:hover { 
  transform: rotate(90deg) scale(1.1); 
  color: #ef4444;
}

/* Round Badge */
.round-badge {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #10b981, #059669);
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: 8px;
  transform: rotate(-2deg);
}

/* Search Button */
.search-btn {
  color: #2c3e50;
  margin-right: 8px;
  transition: all 0.2s;
}
.search-btn:hover {
  transform: scale(1.1);
  color: #3b82f6;
}

/* Search Modal */
.search-modal-content {
  font-family: 'Patrick Hand', cursive;
}
.search-results {
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
}
.search-result-item {
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-result-item:hover {
  border-color: #3b82f6;
  background: #f0f9ff;
}
.result-id {
  font-weight: bold;
  color: #3b82f6;
  flex-shrink: 0;
}
.result-content {
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Navigation Buttons */
.nav-btn {
  font-family: 'Patrick Hand', cursive;
  font-size: 16px;
  color: #64748b;
  border: 2px solid #cbd5e1;
  border-radius: 20px;
  transition: all 0.2s;
}
.nav-btn:hover:not(:disabled) {
  color: #2c3e50;
  border-color: #2c3e50;
  transform: translateX(-2px);
}
.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.nav-placeholder {
  width: 100px;
}

/* Paper Card Effect - The Main Stage */
.question-paper {
  background: #fff; /* Pure white as seen in screenshot */
  /* Double border effect for sketch look */
  border: 3px solid #cbd5e1;
  outline: 2px solid #2c3e50;
  outline-offset: -8px;
  
  border-radius: 4px 20px 4px 25px / 20px 4px 25px 4px; /* Slight irregularity */
  position: relative;
  
  /* Soft shadow lifting off the 'desk' */
  box-shadow: 
    0 10px 20px rgba(0,0,0,0.08),
    0 2px 4px rgba(0,0,0,0.05);
    
  padding: 40px 60px 40px 70px; /* Spacious padding */
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 600px;
  margin-top: 20px;
  transform: rotate(-0.5deg); /* Subtle tilt */
  max-width: 900px; /* Wider paper */
}

.question-paper:hover {
  transform: rotate(0deg) scale(1.005);
  box-shadow: 0 15px 30px rgba(0,0,0,0.12);
}

/* Paper Piled Effect - subtle underneath layers */
.paper-piled::before {
  content: '';
  position: absolute;
  z-index: -1;
  top: 6px; left: 4px; right: -4px; bottom: -6px;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 4px 20px 4px 25px;
  transform: rotate(1.2deg);
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

/* Holes decoration (Left side) */
.paper-holes-left {
  position: absolute;
  left: 20px;
  top: 50px;
  bottom: 50px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  z-index: 2;
  pointer-events: none;
}

.paper-holes-left .hole {
  width: 16px;
  height: 16px;
  background: #e2e8f0; /* Darker hole fill */
  border-radius: 50%;
  box-shadow: inset 2px 2px 4px rgba(0,0,0,0.2);
}

/* Paper Clip & Toolbar */
.paper-clip-container {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 8px 16px;
  border: 2px solid #2c3e50;
  border-radius: 12px; /* Pill shape */
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
  transform: rotate(-1deg);
}

.paper-clip {
  /* Hide actual clip, use icon or simplified view if needed, 
     or keep simple line as spacer */
  width: 2px;
  height: 20px;
  background: #cbd5e1;
  border: none;
  border-radius: 0;
}

.info-text {
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
}

/* Zen Hanging Tag */
.hanging-tag {
  position: absolute;
  top: -15px;
  right: 20px; /* Moved inward slightly */
  z-index: 20;
  transform-origin: top center;
  /* Simplified animation */
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.hanging-tag:hover {
  transform: translateY(5px);
}

.hanging-tag.shaking {
  animation: shake-tag 0.5s ease-in-out infinite;
}
@keyframes shake-tag {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(5deg); }
  75% { transform: rotate(-5deg); }
}

.hanging-tag .string {
  width: 2px;
  height: 25px;
  background: #cbd5e1; /* Lighter string */
  margin: 0 auto;
}
.hanging-tag .tag-body {
  background: #fff;
  border: 2px solid #2c3e50;
  border-radius: 8px; /* Softer corners */
  padding: 10px 14px;
  text-align: center;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.1);
  min-width: 110px;
}
.zen-toggle { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 6px; }
.zen-label { font-size: 14px; font-weight: bold; color: #2c3e50; font-family: 'Patrick Hand', cursive; }
.zen-display { font-family: 'Gochi Hand', cursive; font-size: 20px; color: #16a34a; font-weight: bold; min-height: 24px; }
.zen-breath-dot { width: 8px; height: 8px; background: #cbd5e1; border-radius: 50%; margin: 6px auto; transition: all 0.3s; }
.zen-breath-dot.active { background: #16a34a; box-shadow: 0 0 10px #16a34a; animation: breathe-dot 4s infinite; }
.zen-message { font-size: 12px; color: #94a3b8; margin-top: 4px; font-family: 'Patrick Hand', cursive; }

@keyframes breathe-dot {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.5); opacity: 1; }
}

.question-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: flex-start; 
  margin-bottom: 24px; 
  border-bottom: 2px dashed #cbd5e1;
  padding-bottom: 12px;
}
.type-tag {
  border: 2px solid #2c3e50 !important;
  font-weight: bold;
  font-family: 'Patrick Hand', cursive;
  box-shadow: 1px 1px 0px rgba(0,0,0,0.1);
}
.subject-text { margin-left: 10px; font-size: 14px; color: #57606a; font-weight: 700; font-family: 'Gochi Hand', cursive; letter-spacing: 1px; }

.question-content { 
  font-size: 24px; /* 恢复较大的字体 */
  font-weight: 600; 
  line-height: 1.6; 
  color: #2c3e50; 
  margin-bottom: 32px; 
  font-family: 'Didact Gothic', 'Patrick Hand', sans-serif;
}

/* Options as Doodle Boxes */
.options-list { display: flex; flex-direction: column; gap: 16px; }

.option-item {
  display: flex; align-items: center; padding: 14px 20px; /* 减少选项内边距 */
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  cursor: pointer; 
  transition: all 0.2s; 
  background: #fff;
  position: relative;
  min-height: 50px;
}

.option-item:hover:not(.disabled) { 
  border-color: #2c3e50; 
  background: #fff; 
  transform: scale(1.01) rotate(-0.3deg); 
  box-shadow: 3px 3px 0px rgba(0,0,0,0.1); 
}

/* Selected: Marker Effect */
.option-item.selected { 
  border-color: #2c3e50; 
  background: #a2d2ff; /* 淡蓝色记号笔 */
  box-shadow: 3px 3px 0px #2c3e50;
  transform: rotate(0.5deg);
}

/* Analysis States */
.option-item.correct-highlight { 
  background-color: #ccfbf1;
  border-color: #10b981;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.15);
}

.option-item.error-highlight { 
  background-color: #fee2e2;
  border-color: #ef4444;
  background-image: repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(239, 68, 68, 0.05) 10px, rgba(239, 68, 68, 0.05) 20px);
}

.option-key { 
  width: 30px; height: 30px; 
  background: #fff; 
  border: 2px solid #2c3e50;
  border-radius: 50% 40% 60% 50% / 40% 50% 60% 50%;
  display: flex; align-items: center; justify-content: center; 
  font-weight: 700; color: #2c3e50; 
  margin-right: 16px; font-size: 16px;
  font-family: 'Gochi Hand', cursive;
  box-shadow: 1px 1px 0px rgba(0,0,0,0.1);
  transition: all 0.2s;
  flex-shrink: 0;
}

.option-item.selected .option-key { 
  background: #2c3e50; 
  color: #fff;
  transform: rotate(-8deg);
}

.option-text { flex: 1; font-size: 17px; color: #334155; font-weight: 600; line-height: 1.35; } 
.center-text { text-align: center; font-size: 20px; font-family: 'Gochi Hand', cursive; }

/* Checkbox Style */
.option-checkbox {
  width: 24px; height: 24px; border: 2px solid #2c3e50; margin-right: 14px;
  border-radius: 4px 8px 3px 9px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.option-checkbox.checked { background: #2c3e50; color: #fff; }

/* Action Bar with Decorations */
.action-bar { 
  margin-top: 40px; 
  display: flex; 
  justify-content: center; 
  align-items: center;
  position: relative;
}

.action-btn { 
  min-width: 140px; 
  height: 52px; 
  font-size: 20px !important; 
  font-family: 'Gochi Hand', cursive; 
  font-weight: 600; 
  letter-spacing: 1px;
  border: 2px solid #2c3e50 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  box-shadow: 4px 4px 0px #2c3e50 !important;
  color: #2c3e50 !important;
  background-color: #fcd34d !important; /* Restore Yellow */
  transition: all 0.2s !important;
}

/* === NEW STYLES for Full Screen + Doodles === */

/* 1. Reset Container to Full Screen */
.practice-container {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden; /* Contains the doodles */
  
  display: flex;
  justify-content: center;
  align-items: flex-start; /* Align top for better scrolling */
  padding: 16px;
  
  /* Transparent background to show Layout's industrial dot matrix */
  background: transparent;
  
  /* Core variables */
  --paper-bg: #fffdf7;
  
  font-family: 'Patrick Hand', cursive;
  color: #2c3e50;
}

/* 2. Doodles Layer - Pencil Sketches on Desk */
.doodles-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* Let clicks pass through */
  z-index: 0;
}

.doodle-item {
  position: absolute;
  color: #94a3b8; /* Slate-400 for doodles - pencil lead color */
  mix-blend-mode: multiply; /* Blend into the desk */
  animation: float ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-15px) rotate(2deg); }
}

/* 3. Content Wrapper (The "Safe Zone" for valid UI) */
.content-wrapper {
  position: relative;
  z-index: 1; /* Above doodles */
  width: 100%;
  max-width: 1400px; /* Expanded for 3 columns */
  display: flex;
  justify-content: center;
  padding: 0 16px;
}
.content-wrapper.has-question {
  align-items: flex-start;
}

/* Filter panel stays centered */
.filter-panel { 
  width: 100%;
  max-width: 480px; 
  margin: auto; /* Vertically center in flex column */
}

/* Practice Layout (Grid/Flex for 3 cols) */
.practice-main-layout {
  display: flex;
  gap: 24px;
  width: 100%;
  justify-content: center;
  align-items: flex-start;
  min-width: 0; /* 防止 flex 溢出问题 */
}

/* Adjust question wrapper for layout */
.question-wrapper {
  width: 100%;
  max-width: 760px;
  flex: 1; /* Allow it to fill available space */
  flex-shrink: 1; /* Allow shrinking */
  min-width: 0; /* Prevent overflow */
}

.action-btn:hover {
  transform: translate(-1px, -1px) rotate(-1deg);
  box-shadow: 6px 6px 0px #2c3e50 !important;
}
.action-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0px #2c3e50 !important;
}
.action-btn:disabled {
  background-color: #e2e8f0 !important;
  color: #94a3b8 !important;
  border-color: #cbd5e1 !important;
  box-shadow: none !important;
  transform: none !important;
}

/* Decor Arrow */
.decor-arrow-next {
  position: absolute;
  right: -50px;
  top: 50%;
  transform: translateY(-50%) rotate(-10deg);
  width: 50px;
  opacity: 0.7;
  pointer-events: none;
}
.decor-arrow-prev {
  position: absolute;
  left: -50px;
  top: 50%;
  transform: translateY(-50%) rotate(190deg);
  width: 50px;
  opacity: 0.7;
  pointer-events: none;
}

/* Analysis Box */
.analysis-box {
  margin-top: 32px;
  background: #fff;
  border: 2px dashed #2c3e50;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.05);
  transform: rotate(0.2deg);
}
.analysis-title { 
  display: flex; align-items: center; gap: 8px; 
  font-weight: 700; color: #2c3e50; font-size: 18px; 
  font-family: 'Gochi Hand', cursive;
}
.analysis-content { padding-left: 0; border-left: none; margin-top: 12px; }
.correct-answer-row { font-size: 16px; margin-bottom: 8px; font-family: 'Patrick Hand', cursive; }
.correct-answer-row .value { color: #10b981; font-weight: 700; font-size: 20px; text-decoration: underline; text-decoration-style: wavy;}

/* Animations */
.slide-up-enter-active { transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55); } 
.slide-up-enter-from { opacity: 0; transform: translateY(30px) rotate(3deg); }
.fade-leave-active { transition: opacity 0.2s; }
.fade-leave-to { opacity: 0; }

/* === Side Panel Styles === */
.side-panel {
  width: 280px; /* Increased from default */
  background: #fffdf7; /* Creamy paper bg */
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  padding: 24px;
  position: sticky;
  top: 0;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  /* Premium Shadow Effect */
  box-shadow: 
    0 4px 6px rgba(0,0,0,0.02),
    4px 4px 0 rgba(44, 62, 80, 0.9); /* Solid shadow for comic look */
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.side-panel:hover {
  transform: translateY(-4px) rotate(1deg);
  box-shadow: 6px 6px 0 rgba(44, 62, 80, 0.9);
}

.side-header {
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  color: #2c3e50;
  border-bottom: 2px dashed #cbd5e1;
  padding-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.side-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Left Panel Specifics */
.subject-card {
  position: relative;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid #2c3e50;
  box-shadow: 4px 4px 0 rgba(44, 62, 80, 0.1);
  overflow: hidden;
  text-align: center;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.subject-card:hover { 
  transform: translateY(-2px) scale(1.02);
  box-shadow: 6px 6px 0 rgba(44, 62, 80, 0.15);
}

/* Notebook lines background effect */
.subject-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background-image: linear-gradient(#f1f5f9 1px, transparent 1px);
  background-size: 100% 20px;
  opacity: 0.5;
  pointer-events: none;
}

/* Red line margin effect */
.subject-card::after {
  content: '';
  position: absolute;
  top: 0; bottom: 0; left: 20px;
  width: 2px;
  background: #fca5a5;
  opacity: 0.6;
}

.subject-label { 
  position: relative;
  font-size: 12px; 
  color: #94a3b8; 
  margin-bottom: 2px; 
  font-weight: 700; 
  text-align: right;
  z-index: 1;
}

.subject-main-text { 
  position: relative;
  font-weight: 800; 
  font-size: 24px; 
  color: #2c3e50; 
  line-height: 1.2; 
  font-family: 'Gochi Hand', cursive; 
  z-index: 1;
  padding-left: 10px; /* Offset for red line */
}

.subject-sub-text {
  position: relative;
  display: inline-block;
  background: #fee2e2;
  color: #ef4444;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  border: 2px dashed #fca5a5;
  transform: rotate(-2deg);
  margin-top: 6px;
  box-shadow: 2px 2px 0 rgba(239, 68, 68, 0.1);
  z-index: 1;
  font-family: 'Patrick Hand', cursive;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border: 2px solid #2c3e50;
  border-radius: 12px;
  padding: 12px 8px;
  box-shadow: 2px 2px 0 rgba(0,0,0,0.1);
  transition: transform 0.2s;
}
.stat-item:hover { transform: translateY(-2px); }

.stat-val { font-size: 28px; font-weight: 700; font-family: 'Gochi Hand', cursive; color: #2c3e50; line-height: 1; }
.stat-label { font-size: 13px; color: #64748b; margin-top: 4px; font-weight: 600; }

.mode-tag {
  background: #fee2e2;
  color: #ef4444;
  text-align: center;
  padding: 6px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  border: 2px solid #ef4444;
  box-shadow: 2px 2px 0 rgba(239, 68, 68, 0.2);
}

/* Right Panel specific */
.sheet-info { text-align: right; font-size: 14px; color: #64748b; margin-bottom: 12px; font-weight: 600; }
.bubble-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}
.bubble {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  font-size: 14px;
  color: #64748b;
  cursor: default;
  transition: all 0.2s;
  font-family: 'Fredoka One', cursive, sans-serif;
  background: #fff;
}
.bubble.done {
  background: #e2e8f0;
  border-color: #94a3b8;
  color: #475569;
}
.bubble.active {
  border-color: #2c3e50;
  background: #2c3e50;
  color: #fff;
  box-shadow: 3px 3px 0 rgba(0,0,0,0.2);
  transform: scale(1.15);
  font-weight: bold;
}
.empty-sheet {
  text-align: center; color: #94a3b8; padding: 20px 0; font-style: italic;
}
.side-footer { margin-top: auto; display: flex; align-items: center; justify-content: space-between; gap: 8px; }

.reset-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 8px;
  background: #fff8e1; /* Soft yellow background matching theme */
  border: 1px dashed #f59e0b; /* Amber dashed border */
  color: #d97706;
  font-family: 'Gochi Hand', cursive;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 2px 2px 0 rgba(245, 158, 11, 0.2);
}

.reset-wrapper:hover {
  transform: translateY(-2px) rotate(1deg);
  background: #fffbeb;
  box-shadow: 3px 3px 0 rgba(245, 158, 11, 0.3);
}

.reset-wrapper:active {
  transform: translateY(1px);
  box-shadow: 1px 1px 0 rgba(245, 158, 11, 0.2);
}

.reset-icon { font-size: 16px; }

/* Responsive adjustments */
@media (max-width: 1200px) {
  .side-panel { display: none; } /* 仅在小屏幕下隐藏 */
  .question-wrapper { width: 100%; max-width: 800px; margin: 0 auto; }
}

@media (min-width: 1201px) and (max-width: 1350px) {
  /* 中等屏幕：稍微缩小侧边栏和间距 */
  .side-panel { width: 220px; padding: 16px; }
  .practice-main-layout { gap: 16px; }
  .question-wrapper { max-width: 680px; }
  .stat-val { font-size: 22px; }
}


/* Scrollbar & Layout Container Fixes - Appended */
.practice-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--paper-bg);
  position: relative;
}

.global-scrollbar {
  height: 100%;
}


:deep(.practice-layout-container) {
  min-height: 100%;
  position: relative;
  display: flex;
  box-sizing: border-box;
  padding: 40px 16px;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  gap: 24px;
}



/* Practice Layout (Content inside scrollbar) */
.practice-main-layout {
  min-width: 0;
  max-width: 1400px; /* Re-added centering constraint */
  margin: 0 auto;    /* Re-added centering constraint */
}

/* Zen card & Answer Sheet Enhancements - Appended */
.zen-card {
  margin-top: 16px;
  background: linear-gradient(135deg, rgba(255,255,255,0.7) 0%, rgba(240,249,255,0.7) 100%);
}

.zen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.zen-title {
  font-size: 16px;
  font-weight: 800;
  color: #2c3e50;
  font-family: 'Inter', sans-serif;
}

.zen-status {
  font-size: 12px;
  color: #10b981;
  background: #ecfdf5;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.zen-content {
  text-align: center;
  padding: 8px 0;
}

.zen-timer {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  font-family: 'Fredoka One', cursive, sans-serif;
  margin-bottom: 16px;
}

.zen-breath-wrapper {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.zen-breath-circle {
  width: 20px;
  height: 20px;
  background: #3b82f6;
  border-radius: 50%;
  animation: breathe 4s ease-in-out infinite;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.4);
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.5; box-shadow: 0 0 5px rgba(59, 130, 246, 0.2); }
  50% { transform: scale(2.5); opacity: 0.8; box-shadow: 0 0 20px rgba(59, 130, 246, 0.5); }
}

.zen-quote {
  font-size: 13px;
  color: #64748b;
  font-style: italic;
  line-height: 1.5;
  min-height: 40px;
}

/* 禅意专注 - 防走神动画 */
.zen-title.text-danger { color: #f87171 !important; font-weight: bold; font-family: 'Gochi Hand', cursive; }
.zen-status.status-danger {
  background: #fecaca; color: #b91c1c; border-color: #f87171;
  animation: pulse-danger 1s infinite;
}
.zen-timer.blur-text { font-size: 28px; color: #ef4444; font-weight: bold; }
.zen-breath-circle.circle-danger {
  background: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
  animation: shake-hard 0.5s infinite;
}

@keyframes shake-hard {
  0% { transform: translate(1px, 1px) rotate(0deg); }
  10% { transform: translate(-1px, -2px) rotate(-1deg); }
  20% { transform: translate(-3px, 0px) rotate(1deg); }
  30% { transform: translate(3px, 2px) rotate(0deg); }
  40% { transform: translate(1px, -1px) rotate(1deg); }
  50% { transform: translate(-1px, 2px) rotate(-1deg); }
  60% { transform: translate(-3px, 1px) rotate(0deg); }
  70% { transform: translate(3px, 1px) rotate(-1deg); }
  80% { transform: translate(-1px, -1px) rotate(1deg); }
  90% { transform: translate(1px, 2px) rotate(0deg); }
  100% { transform: translate(1px, -2px) rotate(-1deg); }
}

@keyframes pulse-danger {
  0% { transform: scale(1); box-shadow: 0 0 10px rgba(239, 68, 68, 0.5); }
  50% { transform: scale(1.05); box-shadow: 0 0 30px rgba(239, 68, 68, 0.8); }
  100% { transform: scale(1); box-shadow: 0 0 10px rgba(239, 68, 68, 0.5); }
}

.distracted-shake {
  /* Vivid Red Warning Style */
  border: 4px solid #ef4444 !important;
  background: #fecaca !important; /* Soft Red Background */
  transform: rotate(-2deg);
  box-shadow: 0 0 50px rgba(239, 68, 68, 0.6) !important;
  transition: all 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: shake-hard 0.6s infinite; /* Continuous shaking */
}
/* Ensure text contrasts well with red bg */
.distracted-shake .zen-title, 
.distracted-shake .zen-timer,
.distracted-shake .zen-quote {
  color: #b91c1c !important; /* Dark Red Text */
  text-shadow: 1px 1px 0px rgba(255,255,255,0.5);
}

/* Bubble Statuses */
.bubble {
  cursor: pointer !important;
  font-weight: 600;
  user-select: none;
}

.bubble:hover {
  transform: scale(1.1);
  border-color: #3b82f6;
}

.bubble.correct {
  background: #dcfce7 !important;
  border-color: #22c55e !important;
  color: #15803d !important;
}

.bubble.wrong {
  background: #fee2e2 !important;
  border-color: #ef4444 !important;
  color: #b91c1c !important;
}

.bubble.active {
  background: #1e293b !important;
  border-color: #1e293b !important;
  color: #fff !important;
  transform: scale(1.1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* Pagination */
.sheet-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  margin-top: 20px;
  padding: 12px 14px 0;
  border-top: 2px dashed #cbd5e1;
}

/* 强化分页按钮可见度 */
.sheet-pagination :deep(.n-button) {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: #0ea5e9;
  border: 2px solid #0284c7;
  color: #fff;
  box-shadow: 0 3px 0 #0b769d;
  transition: all 0.18s ease;
}

.sheet-pagination :deep(.n-button):hover:not(.n-button--disabled) {
  background: #38bdf8;
  border-color: #0ea5e9;
  box-shadow: 0 4px 0 #0a6c8f;
  transform: translateY(-1px);
}

.sheet-pagination :deep(.n-button--disabled) {
  background: #e2e8f0;
  border-color: #cbd5e1;
  color: #94a3b8;
  box-shadow: none;
}

.sheet-pagination :deep(.n-button .n-icon) {
  font-size: 18px;
  color: #fff;
}

.page-num {
  font-size: 14px;
  font-weight: 800;
  color: #0f172a;
  min-width: 48px;
  text-align: center;
  padding: 6px 10px;
  background: #f8fafc;
  border: 2px solid #cbd5e1;
  border-radius: 10px;
  box-shadow: inset 0 -1px 0 #e2e8f0;
}


/* === Hand-Drawn Sketch Modal Styles === */
.sketch-modal-card {
  position: relative;
  width: 450px;
  max-width: 90vw;
  background-color: #fffdf7; /* Cream paper */
  /* Sketchy border */
  border: 3px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  padding: 30px;
  box-shadow: 8px 8px 0px rgba(44, 62, 80, 0.2); /* Hard shadow */
  font-family: 'Patrick Hand', cursive;
  color: #2c3e50;
  transform: rotate(-1deg);
  overflow: visible;
}

/* Tape sticker effect - 胶带贴纸效果 */
.tape-sticker {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%) rotate(1deg);
  width: 130px;
  height: 30px;
  background-color: rgba(255, 230, 230, 0.7); /* Pinkish tape */
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  opacity: 0.9;
  z-index: 10;
  backdrop-filter: blur(2px);
}

.sketch-modal-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 25px;
  border-bottom: 2px dashed #e0e0e0;
  padding-bottom: 15px;
}

.sketch-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 26px;
  font-weight: bold;
  color: #c0392b; 
  letter-spacing: 1px;
}

.sketch-modal-content {
  text-align: center;
  margin-bottom: 35px;
}

.sketch-text {
  font-size: 19px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.subject-highlight {
  position: relative;
  display: inline-block;
  padding: 0 6px;
  color: #2c3e50;
  z-index: 0;
}
/* Highlight marker effect */
.subject-highlight::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 10px;
  background-color: #ffeaa7; /* Marker yellow */
  z-index: -1;
  transform: rotate(-0.5deg);
  border-radius: 4px;
}

.doodle-progress {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px 0;
  position: relative;
  padding: 10px;
}

/* Circle around progress */
.doodle-progress::before {
  content: '';
  position: absolute;
  top: -5px; right: 20%; bottom: -5px; left: 20%;
  border: 2px solid #bdc3c7;
  border-radius: 50% 60% 40% 70% / 60% 40% 70% 30%;
  transform: rotate(-2deg);
  opacity: 0.3;
}

.progress-fraction {
  font-family: 'Gochi Hand', cursive;
  font-size: 48px;
  font-weight: bold;
  display: flex;
  align-items: baseline;
  color: #2c3e50;
  z-index: 1;
}

.curr { color: #d35400; /* Burnt orange */ }
.mid-line { margin: 0 10px; color: #95a5a6; font-size: 36px; transform: rotate(15deg); }
.total { color: #7f8c8d; font-size: 36px; }

.sketch-hint {
  font-size: 16px;
  color: #95a5a6;
  font-family: 'Patrick Hand', cursive;
}

.sketch-modal-footer {
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  gap: 20px;
}

.sketch-btn {
  position: relative;
  border: none;
  background: none;
  cursor: pointer;
  padding: 8px 16px;
  font-family: 'Gochi Hand', cursive;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: visible;
  outline: none;
}

.sketch-btn:hover {
  transform: scale(1.1) rotate(-1deg);
}
.sketch-btn:active {
  transform: scale(0.95);
}

.sketch-btn.secondary { color: #7f8c8d; font-size: 18px; }
.sketch-btn.secondary:hover { color: #e74c3c; } /* Red on hover to signify reset warning */
.sketch-btn.secondary .btn-text { border-bottom: 2px dashed #bdc3c7; }

.sketch-btn.primary { 
  color: #2c3e50; 
  font-weight: bold;
  font-size: 24px;
}

.btn-scribble {
  position: absolute;
  bottom: 0px;
  left: -10%;
  width: 120%;
  height: 12px;
  color: #2ecc71; 
  overflow: visible;
  opacity: 0.8;
  z-index: 0;
}

</style>

/* Sketch Modal Styles - Appended */
.sketch-modal-card {
  position: relative;
  width: 90%;
  max-width: 480px;
  background: #fffdf7; /* Paper color */
  border: 3px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px; /* Hand-drawn shape */
  padding: 30px;
  box-shadow: 
    1px 1px 0px rgba(0,0,0,0.1),
    8px 8px 0px rgba(44, 62, 80, 0.2);
  font-family: 'Patrick Hand', cursive;
  text-align: center;
  margin: auto;
  overflow: hidden; /* For tape containment if needed */
}

.tape-sticker {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%) rotate(-2deg);
  width: 100px;
  height: 30px;
  background: rgba(255, 255, 255, 0.4); 
  border-left: 1px dashed rgba(0,0,0,0.1);
  border-right: 1px dashed rgba(0,0,0,0.1);
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  z-index: 10;
  backdrop-filter: blur(2px);
}

.sketch-modal-header {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.sketch-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 28px;
  color: #2c3e50;
  font-weight: bold;
}

.sketch-modal-content {
  font-size: 18px;
  color: #57606a;
  margin-bottom: 30px;
  line-height: 1.5;
}

.sketch-modal-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.sketch-btn {
  border: 2px solid #2c3e50;
  background: #fff;
  padding: 10px 24px;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sketch-btn:hover {
  transform: scale(1.05) rotate(-1deg);
  box-shadow: 3px 3px 0 rgba(44, 62, 80, 0.1);
}

.sketch-btn.primary {
  background: #fcd34d; /* Yellow */
  color: #2c3e50;
}
.sketch-btn.primary.warning-btn {
  background: #ef4444; /* Red for exit */
  color: #fff;
  border-color: #b91c1c;
}
.sketch-btn.primary.warning-btn:hover { background: #dc2626; box-shadow: 3px 3px 0 #b91c1c; }

.sketch-btn.secondary {
  background: #fff;
  color: #64748b;
  border-color: #cbd5e1;
}
.sketch-btn.secondary:hover {
  border-color: #2c3e50;
  color: #2c3e50;
} 
/* End Sketch Modal Styles */
