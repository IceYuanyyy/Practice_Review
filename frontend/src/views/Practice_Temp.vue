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
                <h2 class="hand-title-large">寮€濮嬩笓娉ㄧ粌涔?/h2>
                <div class="hand-subtitle-large">鍑嗗濂界瑪鍜岀焊浜嗗悧锛熻鎴戜滑寮€濮嬪惂锛?妯℃嫙)</div>
              </div>
              
              <n-form :label-width="80" size="large" class="sketch-form">
                <n-grid :cols="1" :y-gap="32">
                  <n-grid-item>
                    <div class="hand-label">鉁忥笍 閫夋嫨绉戠洰</div>
                    <n-select 
                      v-model:value="filters.subject" 
                      :options="subjectOptions" 
                      placeholder="鍏ㄩ儴绉戠洰" 
                      class="sketch-select"
                    />
                  </n-grid-item>
                  <n-grid-item>
                    <div class="hand-label">馃摑 棰樼洰绫诲瀷</div>
                    <n-select 
                      v-model:value="filters.type" 
                      :options="typeOptions" 
                      placeholder="娣峰悎棰樺瀷" 
                      class="sketch-select"
                    />
                  </n-grid-item>
                  <n-grid-item>
                    <button class="sketch-btn-main" @click="startPractice">
                      <span class="btn-text">杩涘叆娌夋蹈妯″紡</span>
                      <span class="btn-bg"></span>
                    </button>
                  </n-grid-item>
                </n-grid>
              </n-form>
           </div>
        </div>
      </div>

      <div v-else style="display: contents">
        <!-- Left Sidebar: Stats (Pinned Notes) -->
        <div class="side-panel left-panel">
            <div class="pinned-note status-note">
                <div class="pin"></div>
                <div class="note-header">
                  <span class="hand-icon">馃搳</span>
                  <span>褰撳墠鐘舵€?/span>
                </div>
                <div class="note-content">
                    <div class="subject-display">
                      <div class="label">姝ｅ湪缁冧範</div>
                      <div class="value">{{ currentSubject || '闅忔満缁冧範' }}</div>
                      <div class="tag-sticker" v-if="isWrongBookMode">閿欓鏈?/div>
                    </div>
                    
                    <div class="hand-divider"></div>
                    
                    <div class="mini-stats">
                      <div class="mini-stat">
                          <span class="val">{{ roundNumber }}</span>
                          <span class="lbl">杞</span>
                      </div>
                      <div class="mini-stat">
                          <span class="val">{{ answeredCount }}</span>
                          <span class="lbl">宸插仛</span>
                      </div>
                    </div>
                </div>
                <div class="note-footer">
                   <div class="reset-link" v-if="currentSubject" @click="handleResetRound">
                    鈫?閲嶇疆鏈疆
                  </div>
                </div>
            </div>

            <!-- Zen Mode (Hanging Tag) -->
            <div class="hanging-tag zen-tag" :class="{ 'shaking': isDistracted }">
              <div class="string"></div>
              <div class="hole"></div>
              <div class="tag-body">
                  <div class="zen-toggle">
                     <span class="zen-label">绂呮剰妯″紡</span>
                     <n-switch v-model:value="isZenModeEnabled" size="small" />
                  </div>
                  <div class="zen-display">
                    <transition name="fade" mode="out-in">
                      <div :key="isDistracted" class="zen-text">
                        {{ isDistracted ? '馃憖 璧扮鍟?' : formatZenTime(zenTime) }}
                      </div>
                    </transition>
                  </div>
                  <div class="zen-breath-dot" :class="{ 'active': !isDistracted && isZenModeEnabled }"></div>
                  <div class="zen-message" v-if="isZenModeEnabled">
                    {{ isDistracted ? '蹇洖鏉' : zenQuote }}
                  </div>
              </div>
            </div>
          </div>

          <!-- Center: Question (Exam Paper) -->
        <div class="question-wrapper">
          <!-- 椤堕儴宸ュ叿鏍?-->
          <div class="practice-toolbar">
            <div class="paper-clip-container">
               <div class="paper-clip"></div>
               <div class="toolbar-info">
                  <span class="info-text">绗?{{ displayCurrentNumber }} / {{ displayTotalCount }} 棰?/span>
               </div>
            </div>
            
            <div class="toolbar-tools">
              <button class="tool-btn icon-btn" @click="showSearchModal = true" title="缈绘壘棰樼洰">
                <span>馃攳</span>
              </button>
              <button class="tool-btn icon-btn close" @click="exitPractice" title="鍚堜笂鏈瓙">
                <span>鉁?/span>
              </button>
            </div>
          </div>

          <div class="question-paper" :class="{ 'paper-piled': true }">
            <div class="paper-holes-left">
              <div v-for="n in 6" :key="n" class="hole"></div>
            </div>
            
            <div class="paper-content-area">
              <div class="question-meta">
                <div class="type-stamp" :class="currentQuestion.type">
                  {{ getTypeLabel() }}
                </div>
                <span class="subject-hand">{{ currentQuestion.subject }}</span>
                <span class="date-hand">{{ new Date().toLocaleDateString() }}</span>
              </div>

              <div class="question-body">
                {{ currentQuestion.content }}
              </div>
              
              <div class="hand-divider-line"></div>

              <div class="options-hand-list">
                <!-- 鍗曢€夐 -->
                <template v-if="(currentQuestion.type === 'single-choice' || currentQuestion.type === 'choice') && options.length > 0">
                  <div 
                    v-for="option in options" 
                    :key="option.key"
                    class="hand-option"
                    :class="{ 
                      'selected': userAnswer === option.key,
                      'disabled': practiceStore.showAnalysis,
                      'correct': practiceStore.showAnalysis && option.key === currentQuestion.answer,
                      'wrong': practiceStore.showAnalysis && userAnswer === option.key && userAnswer !== currentQuestion.answer
                    }"
                    @click="handleSelectOption(option.key)"
                  >
                    <div class="checkbox-circle">{{ option.key }}</div>
                    <div class="option-text-hand">{{ option.text }}</div>
                    
                    <!-- Marking Symbols -->
                    <div v-if="practiceStore.showAnalysis && option.key === currentQuestion.answer" class="mark-symbol correct">鉁?/div>
                    <div v-if="practiceStore.showAnalysis && userAnswer === option.key && userAnswer !== currentQuestion.answer" class="mark-symbol wrong">鉁?/div>
                  </div>
                </template>

                <!-- 澶氶€夐 -->
                <template v-if="currentQuestion.type === 'multiple-choice' && options.length > 0">
                  <div 
                    v-for="option in options" 
                    :key="option.key"
                    class="hand-option"
                    :class="{ 
                      'selected': isOptionSelected(option.key),
                      'disabled': practiceStore.showAnalysis,
                      'correct': practiceStore.showAnalysis && isInCorrectAnswer(option.key),
                      'wrong': practiceStore.showAnalysis && isOptionSelected(option.key) && !isInCorrectAnswer(option.key)
                    }"
                    @click="toggleMultipleOption(option.key)"
                  >
                    <div class="checkbox-square">
                      <span v-if="isOptionSelected(option.key)">鉁?/span>
                    </div>
                    <div class="option-text-hand">{{ option.text }}</div>
                    <div v-if="practiceStore.showAnalysis && isInCorrectAnswer(option.key)" class="mark-symbol correct">鉁?/div>
                  </div>
                </template>

                <!-- 鍒ゆ柇棰?-->
                <template v-if="currentQuestion.type === 'judge'">
                   <div class="judge-options-hand">
                      <div 
                        v-for="val in ['姝ｇ‘', '閿欒']" 
                        :key="val"
                        class="hand-option inline"
                        :class="{ 
                          'selected': userAnswer === val,
                          'disabled': practiceStore.showAnalysis,
                          'correct': practiceStore.showAnalysis && val === currentQuestion.answer,
                          'wrong': practiceStore.showAnalysis && userAnswer === val && userAnswer !== currentQuestion.answer
                        }"
                        @click="handleSelectOption(val)"
                      >
                         <div class="checkbox-circle empty"></div>
                         <span>{{ val }}</span>
                         <div v-if="practiceStore.showAnalysis && val === displayAnswer" class="mark-symbol correct">鉁?/div>
                         <div v-if="practiceStore.showAnalysis && userAnswer === val && !isCorrect" class="mark-symbol wrong">鉁?/div>
                      </div>
                   </div>
                </template>
              </div>

              <div class="hand-actions">
                <button 
                  class="sketch-btn-text" 
                  :disabled="historyIndex <= 0"
                  @click="goToPrevQuestion"
                >
                  &larr; 涓婁竴椤?
                </button>

                <button 
                  v-if="!isReviewingHistory && !practiceStore.showAnalysis" 
                  class="sketch-btn-primary"
                  :disabled="!userAnswer"
                  @click="submitAnswer"
                >
                  <span>鎻愪氦绛旀</span>
                </button>
                
                <button 
                  v-if="!isReviewingHistory && practiceStore.showAnalysis" 
                  class="sketch-btn-primary"
                  @click="nextQuestion"
                >
                  <span>涓嬩竴棰?&rarr;</span>
                </button>
                
                <button 
                  v-if="isReviewingHistory"
                  class="sketch-btn-primary"
                  :disabled="historyIndex >= practiceHistory.length - 1"
                  @click="goToNextHistoryQuestion"
                >
                  <span>缈诲洖 &rarr;</span>
                </button>
              </div>

              <transition name="draw-in">
                <div v-if="practiceStore.showAnalysis" class="analysis-note">
                  <div class="tape-top"></div>
                  <div class="analysis-hand-header">
                      <span class="marker-highlight">馃挕 鐭ヨ瘑鐐硅В鏋?/span>
                      <span class="hand-score" :class="isCorrect ? 'score-good' : 'score-bad'">
                        {{ isCorrect ? '+10鍒?' : '0鍒? }}
                      </span>
                  </div>
                  <div class="analysis-hand-content">
                    <div class="ans-row">
                      <strong>姝ｇ‘绛旀:</strong> <span class="underline">{{ displayAnswer }}</span>
                    </div>
                    <p>{{ currentQuestion.analysis || '鏆傛棤璇︾粏绗旇锛岃璁颁綇姝ｇ‘绛旀鍝︺€? }}</p>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>

          <!-- Right Sidebar: Answer Sheet (Clipboard) -->
          <div class="side-panel right-panel">
            <div class="clipboard-board">
              <div class="clipboard-clip"></div>
              <div class="sheet-paper">
                <div class="sheet-header">
                  <h3>绛旈鍗?/h3>
                  <div class="sheet-meta">P.{{ sheetPage }}</div>
                </div>
                
                <div v-if="displayTotalCount > 0" class="bubbles-grid-hand">
                    <div 
                      v-for="num in currentSheetBubbles" 
                      :key="num"
                      class="bubble-hand"
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
                <div v-else class="empty-sheet-hand">
                    闅忔満鎶介涓?.. <br> 鏃犲浐瀹氶閲?
                </div>

                <div class="sheet-pagination-hand" v-if="totalSheetPages > 1">
                   <span @click="sheetPage > 1 && sheetPage--" class="arrow-btn">&lt;</span>
                   <span class="dots">...</span>
                   <span @click="sheetPage < totalSheetPages && sheetPage++" class="arrow-btn">&gt;</span>
                </div>
              </div>
            </div>
          </div>
      </div>
    </n-scrollbar>
    
    <!-- 鎼滅储妯℃€佹 -->
    <n-modal v-model:show="showSearchModal" preset="card" title="馃攳 鎼滅储棰樼洰" style="width: 500px; max-width: 90vw;">
      <div class="search-modal-content">
        <n-input 
          v-model:value="searchKeyword" 
          placeholder="杈撳叆棰樺彿鎴栧叧閿瘝..." 
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-button type="primary" @click="handleSearch" style="margin-top: 16px; width: 100%;">
          鎼滅储
        </n-button>
        
        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="q in searchResults" :key="q.id" class="search-result-item" @click="jumpToQuestion(q)">
            <span class="result-id">#{{ q.id }}</span>
            <span class="result-content">{{ q.content.substring(0, 50) }}...</span>
          </div>
        </div>
      </div>
    </n-modal>

    <!-- 鎵嬬粯椋庢牸鏈畬鎴愭彁閱?Modal -->
    <n-modal v-model:show="showResumeModal" :auto-focus="false" :mask-closable="false">
      <div class="sketch-modal-card">
        <div class="tape-sticker"></div>
        
        <div class="sketch-modal-header">
          <n-icon size="32" color="#e74c3c" :component="SchoolOutline" />
          <span class="sketch-title">鏈畬鎴愮殑鎸戞垬!</span>
        </div>

        <div class="sketch-modal-content">
          <p class="sketch-text">
            鍢匡紒鍦?<strong class="subject-highlight">{{ resumeModalData.subject }}</strong> 绗旇鏈噷<br>鍙戠幇浣犺繕鏈夋病鍐欏畬鐨勯锛?
          </p>
          <div class="doodle-progress">
             <span class="progress-fraction">
               <span class="curr">{{ resumeModalData.currentIndex }}</span>
               <span class="mid-line">/</span>
               <span class="total">{{ resumeModalData.totalCount }}</span>
             </span>
          </div>
          <p class="sketch-hint">瑕佹帴鐫€涓婃鐨勫啓锛岃繕鏄炕绡囬噸鏉ワ紵</p>
        </div>

        <div class="sketch-modal-footer">
          <button class="sketch-btn secondary" @click="handleResumeReset">
            <span class="btn-text">馃攧 閲嶇疆鏈疆</span>
          </button>
          
          <button class="sketch-btn primary" @click="handleResumeContinue">
            <span class="btn-text">鉁忥笍 缁х画缁冧範</span>
             <!-- 涓嬪垝绾胯楗?-->
             <svg class="btn-scribble" viewBox="0 0 100 10" preserveAspectRatio="none">
               <path d="M0,5 Q 50,10 100,5" stroke="currentColor" fill="none" stroke-width="2" />
            </svg>
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
import { CloseOutline, CheckmarkCircle, CloseCircle, CheckmarkOutline, SearchOutline, ArrowBackOutline, SchoolOutline, BookOutline, RefreshOutline } from '@vicons/ionicons5'
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
const selectedAnswers = ref([]) // 澶氶€夐绛旀鏁扮粍

// 杞鐩稿叧鐘舵€?
const currentIndex = ref(0)
const totalCount = ref(0)
const roundNumber = ref(1)
const isRoundFinished = ref(false)
const currentSubject = ref('')

// 閿欓缁冧範妯″紡
const isWrongBookMode = ref(false)
const wrongBookSubject = ref(null)

// 鍋氶鍘嗗彶璁板綍锛堢敤浜庝笂涓€棰?涓嬩竴棰樺鑸級
const practiceHistory = ref([]) // [{question, userAnswer}]
const historyIndex = ref(-1) // 褰撳墠鍦ㄥ巻鍙蹭腑鐨勪綅缃?

// 宸茬瓟棰樻暟閲忥紝閬垮厤鏈綔绛旂殑璺宠浆涔熻鍏?"宸插仛"
const answeredCount = computed(() =>
  practiceHistory.value.filter(record => record.userAnswer !== null && record.userAnswer !== undefined && record.userAnswer !== '').length
)

// 鏄惁鍦ㄥ洖椤惧巻鍙叉ā寮忥紙涓嶅湪鍘嗗彶鏈熬锛?
const isReviewingHistory = computed(() => {
  return historyIndex.value >= 0 && historyIndex.value < practiceHistory.value.length - 1
})

// 鎭㈠杞鎻愮ず Modal
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
      message.success('鏂扮殑涓€椤碉紝鏂扮殑寮€濮嬶紒') // More casual message
    }
  } catch (e) {
    message.error('鍝庡憖锛岀焊寮犲崱浣忎簡 (閲嶇疆澶辫触)')
  }
  showResumeModal.value = false
}

// 鎼滅储鐩稿叧
const showSearchModal = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])

// 璁＄畻杩涘害鐧惧垎姣旓紙鍩轰簬鍋氶鍘嗗彶锛?
const roundProgress = computed(() => {
  if (answeredCount.value === 0) return 0
  // 姣?20 棰樹竴涓疆鍥?
  return Math.round(((answeredCount.value % 20) / 20) * 100)
})

// 灞曠ず鐢ㄩ鍙蜂笌鎬绘暟锛氫紭鍏堜娇鐢ㄥ悗绔繑鍥炵殑鎬婚噺锛岄殢鏈烘ā寮忛€€鍖栦负鍘嗗彶闀垮害
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

// === 绛旈鍗″垎椤典笌鐘舵€?===
const roundResults = ref({}) // { index: status (0-鏈仛, 1-姝ｇ‘, 2-閿欒) }
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

// 鑾峰彇棰樼洰鐘舵€?
const fetchRoundResults = async () => {
  if (!currentSubject.value) return
  try {
    const res = await getRoundResults(currentSubject.value)
    if (res.data) {
      roundResults.value = res.data
    }
  } catch (err) {
    console.error('鑾峰彇绛旈鍗＄姸鎬佸け璐?, err)
  }
}

// 璺宠浆鍒版寚瀹氶鐩?
const jumpToRoundIdx = async (number) => {
  const index = number - 1
  if (index === currentIndex.value && currentQuestion.value) return
  
  // 鍏堟鏌ュ巻鍙茶褰曚腑鏄惁鏈夎棰樼洰锛堟牴鎹?roundIndex 鍖归厤锛?
  const existingRecordIdx = practiceHistory.value.findIndex(
    record => record.roundIndex === index
  )
  
  // 濡傛灉鍘嗗彶涓凡鏈夎棰樿褰曪紝鐩存帴浠庡巻鍙蹭腑鎭㈠锛堢被浼间笂涓€棰橀€昏緫锛?
  if (existingRecordIdx !== -1) {
    const record = practiceHistory.value[existingRecordIdx]
    historyIndex.value = existingRecordIdx
    
    currentQuestion.value = record.question
    currentIndex.value = record.roundIndex
    practiceStore.setCurrentQuestion(record.question)
    
    // 鎭㈠鐢ㄦ埛涔嬪墠鐨勯€夋嫨
    if (record.userAnswer) {
      userAnswer.value = record.userAnswer
      // 澶氶€夐闇€瑕佹仮澶?selectedAnswers锛坲serAnswer 鏄 "AB" 杩欐牱鐨勫瓧绗︿覆锛?
      if (record.question.type === 'multiple-choice' && record.userAnswer) {
        selectedAnswers.value = record.userAnswer.split('')
      } else {
        selectedAnswers.value = []
      }
      practiceStore.showAnalysis = true // 鏄剧ず瑙ｆ瀽锛堝洜涓哄凡缁忕瓟杩囷級
    } else {
      userAnswer.value = null
      selectedAnswers.value = []
      practiceStore.showAnalysis = false
    }
    
    message.info(`宸茶烦杞嚦绗?${number} 棰榒)
    return
  }
  
  // 鍘嗗彶涓病鏈夎棰樿褰曪紝浠庡悗绔幏鍙?
  try {
    practiceStore.reset()
    const res = await jumpRoundQuestion(currentSubject.value, index)
    if (res.data && res.data.question) {
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      practiceStore.setCurrentQuestion(res.data.question)
      userAnswer.value = null
      selectedAnswers.value = []
      
      // 灏嗘柊棰樼洰娣诲姞鍒板巻鍙茶褰?
      addToHistory(res.data.question, res.data.currentIndex)
      
      message.info(`宸茶烦杞嚦绗?${number} 棰榒)
    }
  } catch (err) {
    message.error('璺宠浆棰樼洰澶辫触')
  }
}

// === 绂呮剰涓撴敞鍗＄墖閫昏緫 ===
const zenTime = ref(0)
const zenTimer = ref(null)
const zenQuote = ref('')
// 鏂板锛氶槻璧扮鐘舵€?
const isDistracted = ref(false)
const isZenModeEnabled = ref(false)
const zenQuotes = [
  "蹇冨姝㈡按锛屼笓娉ㄥ綋涓嬨€?,
  "姣忎竴鐐硅繘姝ワ紝閮藉€煎緱搴嗙銆?,
  "鍛煎惛锛屾劅鍙楁€濈淮鐨勬祦鍔ㄣ€?,
  "涓嶈鎬ワ紝绛旀灏卞湪蹇冧腑銆?,
  "鎱㈡參鏉ワ紝姣旇緝蹇€?
]

const startZenTimer = () => {
  if (zenTimer.value) return
  zenQuote.value = zenQuotes[Math.floor(Math.random() * zenQuotes.length)]
  zenTimer.value = setInterval(() => {
    zenTime.value++
  }, 1000)
}

const formatZenTime = (seconds) => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

// 鐩戝惉榧犳爣绉诲嚭/绉诲叆
// 鏂板锛氬鍣?Ref
const practiceContainerRef = ref(null)

const handleMouseLeave = () => { 
  if (isZenModeEnabled.value) {
    isDistracted.value = true 
  }
}
const handleMouseEnter = () => { isDistracted.value = false }

watch(isZenModeEnabled, (newVal) => {
  if (!newVal) {
    isDistracted.value = false
  }
})

onMounted(async () => {
  // 鎸傝浇闃茶蛋绁炵洃鍚?- 浠呴拡瀵圭粌涔犲尯鍩熷鍣?
  if (practiceContainerRef.value) {
    practiceContainerRef.value.addEventListener('mouseleave', handleMouseLeave)
    practiceContainerRef.value.addEventListener('mouseenter', handleMouseEnter)
  }
  
  // 绐楀彛澶辩劍鐐逛篃绠楄蛋绁?(鍙€夛紝淇濈暀浠ュ寮轰綋楠?
  window.addEventListener('blur', handleMouseLeave)
  window.addEventListener('focus', handleMouseEnter)

  await loadLastFilter()
  startZenTimer()
})

onUnmounted(() => {
  if (zenTimer.value) clearInterval(zenTimer.value)
  // 绉婚櫎鐩戝惉
  if (practiceContainerRef.value) {
    practiceContainerRef.value.removeEventListener('mouseleave', handleMouseLeave)
    practiceContainerRef.value.removeEventListener('mouseenter', handleMouseEnter)
  }
  window.removeEventListener('blur', handleMouseLeave)
  window.removeEventListener('focus', handleMouseEnter)
})
const filters = reactive({ subject: null, type: null, difficulty: null })

// 閫夐」閰嶇疆
const subjectOptions = ref([])

// 鍔犺浇绉戠洰鍒楄〃锛堜繚鎸佺函鏌ヨ锛岄伩鍏嶅悗绔湭鍚姩鏃舵姤閿欙級
const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    if (res.data && res.data.length > 0) {
      const subjects = res.data.map(subject => ({
        label: `${subject.name} (${subject.questionCount})`,
        value: subject.name
      }))
      // 灏?鍏ㄩ儴绉戠洰"浣滀负鐙珛閫夐」锛寁alue璁句负绌哄瓧绗︿覆鑰岄潪null
      subjectOptions.value = [
        { label: '鍏ㄩ儴绉戠洰', value: '' },
        ...subjects
      ]
    }
  } catch (error) {
    console.error('鍔犺浇绉戠洰鍒楄〃澶辫触', error)
  }
}

const typeOptions = [
  { label: '娣峰悎棰樺瀷', value: '' },
  { label: '鍗曢€夐', value: 'single-choice' },
  { label: '澶氶€夐', value: 'multiple-choice' },
  { label: '鍒ゆ柇棰?, value: 'judge' }
]

// 瑙ｆ瀽閫夐」
const options = computed(() => {
  if (!currentQuestion.value) return []
  const questionType = currentQuestion.value.type
  
  // 閫夋嫨棰樼被鍨嬫墠闇€瑕佽В鏋愰€夐」
  if (questionType !== 'single-choice' && questionType !== 'multiple-choice' && questionType !== 'choice') return []
  
  try {
    const opts = currentQuestion.value.options
    console.log('鍘熷閫夐」鏁版嵁:', opts, '绫诲瀷:', typeof opts)
    
    // 妫€鏌ラ€夐」鏄惁瀛樺湪涓斾笉涓虹┖
    if (!opts) {
      console.warn('閫夐」鏁版嵁涓嶅瓨鍦紝棰樼洰ID:', currentQuestion.value.id)
      message.warning('棰樼洰閫夐」鏁版嵁缂哄け锛屽凡璺宠繃')
      return []
    }
    
    // 濡傛灉鏄瓧绗︿覆锛屽皾璇曡В鏋愪负JSON
    let parsedOpts = opts
    if (typeof opts === 'string') {
      try {
        parsedOpts = JSON.parse(opts)
      } catch (e) {
        console.error('閫夐」JSON瑙ｆ瀽澶辫触:', opts)
        message.error('棰樼洰閫夐」鏍煎紡閿欒')
        return []
      }
    }
    
    // 妫€鏌ユ槸鍚︿负鏁扮粍涓旀湁鍐呭
    if (!Array.isArray(parsedOpts)) {
      console.error('閫夐」涓嶆槸鏁扮粍绫诲瀷:', typeof parsedOpts, parsedOpts)
      message.error('棰樼洰閫夐」鏍煎紡閿欒')
      return []
    }
    
    if (parsedOpts.length === 0) {
      console.warn('閫夐」鏁扮粍涓虹┖锛岄鐩甀D:', currentQuestion.value.id)
      message.warning('棰樼洰娌℃湁鍙敤閫夐」锛屽凡璺宠繃')
      return []
    }
    
    // 瑙ｆ瀽姣忎釜閫夐」
    const parsed = parsedOpts.map((opt, index) => {
      // 鏀寔澶氱鏍煎紡锛?
      // 1. "A:閫夐」鍐呭" 
      // 2. "閫夐」鍐呭" (鑷姩鍒嗛厤ABCD)
      if (typeof opt === 'string' && opt.trim()) {
        if (opt.includes(':')) {
          const [key, ...rest] = opt.split(':')
          const text = rest.join(':').trim()
          if (text) {
            return { 
              key: key.trim(), 
              text: text
            }
          }
        } else {
          // 娌℃湁鍐掑彿锛岃嚜鍔ㄥ垎閰嶅瓧姣?
          const letters = ['A', 'B', 'C', 'D', 'E', 'F']
          return {
            key: letters[index],
            text: opt.trim()
          }
        }
      }
      return null
    }).filter(opt => opt !== null && opt.key && opt.text)
    
    console.log('瑙ｆ瀽鍚庣殑閫夐」:', parsed)
    
    if (parsed.length === 0) {
      console.error('鎵€鏈夐€夐」瑙ｆ瀽鍚庡潎鏃犳晥锛屽師濮嬫暟鎹?', parsedOpts)
      message.error('棰樼洰閫夐」鍐呭鏃犳晥')
      return []
    }
    
    return parsed
  } catch (e) { 
    console.error('閫夐」瑙ｆ瀽寮傚父:', e, currentQuestion.value)
    message.error('棰樼洰閫夐」瑙ｆ瀽澶辫触')
    return [] 
  }
})

// 鍒ゆ柇棰樼瓟妗堟樉绀猴紙杞崲瀛楁瘝涓轰腑鏂囷級
const displayAnswer = computed(() => {
  if (!currentQuestion.value) return ''
  
  if (currentQuestion.value.type === 'judge') {
    const answer = currentQuestion.value.answer?.trim().toUpperCase()
    // 濡傛灉绛旀鏄瓧姣嶏紝杞崲涓轰腑鏂?
    if (answer === 'A' || answer === '姝ｇ‘' || answer === 'TRUE' || answer === 'T' || answer === '鈭?) {
      return '姝ｇ‘'
    } else if (answer === 'B' || answer === '閿欒' || answer === 'FALSE' || answer === 'F' || answer === '脳') {
      return '閿欒'
    }
    return currentQuestion.value.answer || '鏈煡'
  }
  
  return currentQuestion.value.answer || ''
})

const isCorrect = computed(() => {
  if (!currentQuestion.value || !userAnswer.value) return false
  
  // 澶氶€夐锛氶渶瑕佸畬鍏ㄥ尮閰嶏紙椤哄簭鏃犲叧锛?
  if (currentQuestion.value.type === 'multiple-choice') {
    const userAns = userAnswer.value.split('').sort().join('')
    const correctAns = (currentQuestion.value.answer || '').split('').sort().join('')
    return userAns === correctAns
  }
  
  // 鍒ゆ柇棰樼壒娈婂鐞?
  if (currentQuestion.value.type === 'judge') {
    const userAns = userAnswer.value?.trim()
    const correctAns = currentQuestion.value.answer?.trim().toUpperCase()
    
    // 鏍囧噯鍖栫敤鎴风瓟妗?
    const normalizedUser = (userAns === '姝ｇ‘' || userAns === 'A') ? 'A' : 'B'
    
    // 鏍囧噯鍖栨纭瓟妗?
    let normalizedCorrect = 'A'
    if (correctAns === 'B' || correctAns === '閿欒' || correctAns === 'FALSE' || correctAns === 'F' || correctAns === '脳') {
      normalizedCorrect = 'B'
    }
    
    return normalizedUser === normalizedCorrect
  }
  
  // 閫夋嫨棰樼洿鎺ユ瘮杈?
  return userAnswer.value?.trim().toUpperCase() === currentQuestion.value.answer?.trim().toUpperCase()
})

const handleSelectOption = (key) => {
  if (practiceStore.showAnalysis) return
  userAnswer.value = key
  console.log('閫夋嫨绛旀:', key, '棰樺瀷:', currentQuestion.value?.type)
}

// 澶氶€夐绛旀绠＄悊
const toggleMultipleOption = (key) => {
  if (practiceStore.showAnalysis) return
  
  const index = selectedAnswers.value.indexOf(key)
  if (index > -1) {
    selectedAnswers.value.splice(index, 1)
  } else {
    selectedAnswers.value.push(key)
  }
  
  // 鎺掑簭鍚庣粍鍚堟垚绛旀瀛楃涓?
  userAnswer.value = selectedAnswers.value.sort().join('')
  console.log('澶氶€夌瓟妗?', userAnswer.value, '宸查€?', selectedAnswers.value)
}

const isOptionSelected = (key) => {
  return selectedAnswers.value.includes(key)
}

const isInCorrectAnswer = (key) => {
  if (!currentQuestion.value) return false
  const correctAnswer = currentQuestion.value.answer || ''
  return correctAnswer.includes(key)
}

// 鑾峰彇棰樺瀷鏍囩
const getTypeLabel = () => {
  if (!currentQuestion.value) return ''
  const typeMap = {
    'single-choice': '鍗曢€夐',
    'multiple-choice': '澶氶€夐',
    'choice': '閫夋嫨棰?,
    'judge': '鍒ゆ柇棰?
  }
  return typeMap[currentQuestion.value.type] || '鏈煡棰樺瀷'
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

// 缁戝畾鍥炶溅閿笅涓€棰?
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
  
  // 妫€鏌ユ槸鍚︿粠閿欓鏈〉闈㈣烦杞繃鏉?
  const query = router.currentRoute.value.query
  
  // 澶勭悊鍏ㄥ眬鎼滅储璺宠浆
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

// 寮€濮嬮敊棰樼粌涔犳ā寮?
const startWrongBookPracticeMode = async () => {
  try {
    const res = await startWrongBookPractice(wrongBookSubject.value)
    if (!res.data || !res.data.question) {
      message.warning('鏆傛棤閿欓')
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
    
    message.success(`寮€濮嬮敊棰樼粌涔?{wrongBookSubject.value ? ` (${wrongBookSubject.value})` : ''}锛屽叡 ${totalCount.value} 棰榒)
  } catch (error) {
    console.error('寮€濮嬮敊棰樼粌涔犲け璐?', error)
    message.error('寮€濮嬮敊棰樼粌涔犲け璐?)
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
    console.log('======= 寮€濮嬭疆娆＄粌涔?=======')
    console.log('绛涢€夋潯浠?', filters)

    // 鑾峰彇閫夋嫨鐨勭鐩?
    const subject = filters.subject && filters.subject.trim() !== '' ? filters.subject : null
    
    if (!subject) {
      // 濡傛灉鏈€夋嫨绉戠洰锛屼娇鐢ㄥ師鏈夌殑闅忔満鎶介妯″紡
      const params = { ...filters, subject: undefined }
      const res = await getRandomQuestion(params)
      if (!res.data) {
        message.warning('娌℃湁鎵惧埌绗﹀悎鏉′欢鐨勯鐩?)
        return
      }
      currentQuestion.value = res.data
      practiceStore.setCurrentQuestion(res.data)
      userAnswer.value = null
      selectedAnswers.value = []
      
      // 娣诲姞鍒板仛棰樺巻鍙?
      addToHistory(res.data)
      
      return
    }

    // 浣跨敤杞 API
    currentSubject.value = subject
    const res = await startRound(subject)
    console.log('杞鍝嶅簲:', res)
    
    if (!res.data || !res.data.question) {
      message.warning('璇ョ鐩殏鏃犻鐩?)
      return
    }
    
    // --- 鏂板锛氭娴嬫槸鍚︽湁杩涘害锛屾彁绀虹敤鎴锋槸鍚﹂噸缃?(鎵嬬粯椋庢牸 Modal) ---
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
    
    // 鏃犺繘搴︽垨鏂拌疆娆★紝鐩存帴搴旂敤
    applyRoundState(res.data)
    
  } catch (error) {
    console.error('鑾峰彇棰樼洰澶辫触:', error)
    message.error('鑾峰彇棰樼洰澶辫触')
  }
}

// 杈呭姪鍑芥暟锛氬簲鐢ㄨ疆娆＄姸鎬?
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
  
  console.log(`杞杩涘害: ${currentIndex.value + 1}/${totalCount.value}, 绗?{roundNumber.value}杞甡)
  
  // 妫€鏌ラ€夋嫨棰樻槸鍚︽湁鏈夋晥閫夐」
  const needsOptions = ['single-choice', 'multiple-choice', 'choice'].includes(data.question.type)
  if (needsOptions) {
    nextTick().then(() => {
      if (options.value.length === 0) {
        console.error('棰樼洰閫夐」鏃犳晥锛岃嚜鍔ㄨ烦杩?, data.question)
        message.warning('棰樼洰鏁版嵁鏈夎锛屾鍦ㄨ幏鍙栦笅涓€棰?..')
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
    
    // 淇濆瓨鍒板仛棰樺巻鍙诧紙鐢ㄤ簬涓婁竴棰樺鑸級
    // 濡傛灉褰撳墠涓嶅湪鍘嗗彶鏈熬锛岃鏄庢槸鍥為【鏃ч鍚庢彁浜や簡鏂扮瓟妗堬紝鏇存柊璇ヨ褰?
    if (historyIndex.value >= 0 && historyIndex.value < practiceHistory.value.length) {
      practiceHistory.value[historyIndex.value].userAnswer = userAnswer.value
    }
    
    if (isCorrect.value) message.success('鍥炵瓟姝ｇ‘锛?)
    else message.error('鍥炵瓟閿欒锛?)
    
    // 鎻愪氦鍚庡埛鏂扮瓟棰樺崱鐘舵€?
    fetchRoundResults()
  } catch (error) {
    practiceStore.submitAnswer(userAnswer.value)
  }
}
// 鎵嬪姩閲嶇疆鏈疆锛堜粠鎸夐挳瑙﹀彂锛?
const handleResetRound = () => {
  if (!currentSubject.value) {
    message.warning('褰撳墠涓洪殢鏈烘ā寮忥紝鏃犳硶閲嶇疆杞')
    return
  }
  
  dialog.warning({
    title: '纭閲嶇疆鏈疆',
    content: '閲嶇疆鍚庯紝褰撳墠杞鐨勬墍鏈夎繘搴﹀皢娓呯┖锛岀瓟棰樺崱灏嗘仮澶嶄负鍏ㄧ櫧銆傜‘瀹氳閲嶇疆鍚楋紵',
    positiveText: '纭畾閲嶇疆',
    negativeText: '鍙栨秷',
    onPositiveClick: async () => {
      try {
        const resetRes = await resetRound(currentSubject.value)
        if (resetRes.data && resetRes.data.question) {
          applyRoundState(resetRes.data)
          roundResults.value = {} // 娓呯┖绛旈鍗＄姸鎬?
          zenTime.value = 0 // 閲嶇疆璁℃椂鍣?
          practiceHistory.value = [] // 娓呯┖鍋氶鍘嗗彶
          historyIndex.value = -1
          message.success('宸查噸缃湰杞紝杞璁℃暟淇濇寔涓嶅彉锛屽姞娌瑰啀鏉ヤ竴娆★紒')
        }
      } catch (e) {
        message.error('閲嶇疆澶辫触锛岃閲嶈瘯')
      }
    }
  })
}

// 娣诲姞棰樼洰鍒板仛棰樺巻鍙?
const addToHistory = (question, rIndex = null) => {
  // 濡傛灉涓嶅湪鍘嗗彶鏈熬锛屾埅鏂悗闈㈢殑鍘嗗彶锛堟柊鍒嗘敮锛?
  if (historyIndex.value < practiceHistory.value.length - 1) {
    practiceHistory.value = practiceHistory.value.slice(0, historyIndex.value + 1)
  }
  // 濡傛灉鏈紶鍏?rIndex锛屽皾璇曚娇鐢ㄥ綋鍓?currentIndex
  const idx = rIndex !== null ? rIndex : currentIndex.value
  practiceHistory.value.push({
    question: question,
    userAnswer: null,
    roundIndex: idx
  })
  historyIndex.value = practiceHistory.value.length - 1
}

// 涓婁竴棰橈紙浠庡巻鍙茶褰曚腑鑾峰彇锛?
const goToPrevQuestion = () => {
  if (historyIndex.value <= 0) {
    message.info('宸叉槸绗竴棰?)
    return
  }
  
  historyIndex.value--
  const record = practiceHistory.value[historyIndex.value]
  
  // 鎭㈠棰樼洰鍜屽凡閫夌瓟妗?
  currentQuestion.value = record.question
  if (record.roundIndex !== undefined) currentIndex.value = record.roundIndex
  practiceStore.setCurrentQuestion(record.question)
  
  // 鎭㈠鐢ㄦ埛涔嬪墠鐨勯€夋嫨
  if (record.userAnswer) {
    userAnswer.value = record.userAnswer
    // 澶氶€夐闇€瑕佹仮澶?selectedAnswers锛坲serAnswer 鏄 "AB" 杩欐牱鐨勫瓧绗︿覆锛?
    if (record.question.type === 'multiple-choice') {
      selectedAnswers.value = record.userAnswer.split('')
    } else {
      selectedAnswers.value = []
    }
    practiceStore.showAnalysis = true // 鏄剧ず瑙ｆ瀽锛堝洜涓哄凡缁忕瓟杩囷級
  } else {
    userAnswer.value = null
    selectedAnswers.value = []
    practiceStore.showAnalysis = false
  }
  
  message.info(`杩斿洖绗?${historyIndex.value + 1} 棰榒)
}

// 涓嬩竴棰橈紙鍦ㄥ巻鍙茶褰曚腑鍓嶈繘锛?
const goToNextHistoryQuestion = () => {
  if (historyIndex.value >= practiceHistory.value.length - 1) {
    // 宸茬粡鍒板巻鍙叉湯灏撅紝閫€鍑哄洖椤炬ā寮忥紝鑾峰彇鏂伴
    message.info('宸插埌鏈€鏂伴鐩紝缁х画鍋氭柊棰?)
    nextQuestion()
    return
  }
  
  historyIndex.value++
  const record = practiceHistory.value[historyIndex.value]
  
  // 鎭㈠棰樼洰鍜屽凡閫夌瓟妗?
  currentQuestion.value = record.question
  if (record.roundIndex !== undefined) currentIndex.value = record.roundIndex
  practiceStore.setCurrentQuestion(record.question)
  
  // 鎭㈠鐢ㄦ埛涔嬪墠鐨勯€夋嫨
  if (record.userAnswer) {
    userAnswer.value = record.userAnswer
    // 澶氶€夐闇€瑕佹仮澶?selectedAnswers锛坲serAnswer 鏄 "AB" 杩欐牱鐨勫瓧绗︿覆锛?
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
  
  message.info(`鍓嶈繘鍒扮 ${historyIndex.value + 1} 棰榒)
}

const nextQuestion = async () => {
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = []
  
  // 閿欓缁冧範妯″紡
  if (isWrongBookMode.value) {
    try {
      const res = await nextWrongQuestion({
        subject: wrongBookSubject.value,
        currentQuestionId: currentQuestion.value.id
      })
      
      if (res.data.isFinished) {
        dialog.success({
          title: '馃帀 鎭枩瀹屾垚锛?,
          content: `閿欓缁冧範宸插叏閮ㄥ畬鎴愶紒`,
          positiveText: '杩斿洖閿欓鏈?,
          negativeText: '缁х画缁冧範',
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
      console.error('鑾峰彇涓嬩竴棰樺け璐?', error)
      message.error('鑾峰彇涓嬩竴棰樺け璐?)
    }
    return
  }
  
  // 濡傛灉娌℃湁閫夋嫨绉戠洰锛堥殢鏈烘ā寮忥級锛屼娇鐢ㄥ師鏈夐€昏緫
  if (!currentSubject.value) {
    await startPractice()
    return
  }
  
  try {
    const res = await nextRoundQuestion(currentSubject.value)
    console.log('涓嬩竴棰樺搷搴?', res)
    if (res.data && (res.data.totalCount || res.data.totalCount === 0)) {
      totalCount.value = res.data.totalCount
    }
    
    // 妫€鏌ユ槸鍚﹀畬鎴愭湰杞?
    if (res.data.isFinished && !res.data.question) {
      isRoundFinished.value = true
      // 鏄剧ず瀹屾垚瀵硅瘽妗?
      dialog.success({
        title: '馃帀 鎭枩瀹屾垚锛?,
        content: `鎮ㄥ凡瀹屾垚绗?${roundNumber.value} 杞粌涔狅紝鍏?${totalCount.value} 閬撻鐩紒鍗冲皢杩涘叆绗?${roundNumber.value + 1} 杞紝鏄惁缁х画锛焋,
        positiveText: '寮€濮嬫柊涓€杞?,
        negativeText: '杩斿洖棣栭〉',
        onPositiveClick: async () => {
          const resetRes = await startRound(currentSubject.value)
          if (resetRes.data && resetRes.data.question) {
            currentQuestion.value = resetRes.data.question
            currentIndex.value = 0
            totalCount.value = resetRes.data.totalCount
            roundNumber.value = resetRes.data.roundNumber
            isRoundFinished.value = false
            practiceStore.setCurrentQuestion(resetRes.data.question)
            message.success(`宸插紑濮嬬 ${roundNumber.value} 杞粌涔狅紒`)
          }
        },
        onNegativeClick: () => {
          exitPractice()
        }
      })
      return
    }
    
    // 鏇存柊鐘舵€?
    if (res.data.question) {
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      practiceStore.setCurrentQuestion(res.data.question)
      
      // 娣诲姞鍒板仛棰樺巻鍙?
      addToHistory(res.data.question, res.data.currentIndex)
    } else {
      // 娌℃湁杩斿洖棰樼洰锛屽彲鑳芥槸鏈疆宸插畬鎴?
      message.info('鏈疆宸插畬鎴愶紝璇峰紑濮嬫柊涓€杞?)
    }
    
  } catch (error) {
    console.error('鑾峰彇涓嬩竴棰樺け璐?', error)
    message.error('鑾峰彇涓嬩竴棰樺け璐?)
  }
}

// 涓婁竴棰?
const prevQuestion = async () => {
  if (!currentSubject.value || currentIndex.value <= 0) return
  
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = []
  
  try {
    const res = await prevRoundQuestion(currentSubject.value)
    console.log('涓婁竴棰樺搷搴?', res)
    if (res.data && (res.data.totalCount || res.data.totalCount === 0)) {
      totalCount.value = res.data.totalCount
    }
    
    if (res.data.question) {
      currentQuestion.value = res.data.question
      currentIndex.value = res.data.currentIndex
      practiceStore.setCurrentQuestion(res.data.question)
    } else {
      message.info('宸叉槸绗竴棰?)
    }
  } catch (error) {
    console.error('鑾峰彇涓婁竴棰樺け璐?', error)
    message.error('鑾峰彇涓婁竴棰樺け璐?)
  }
}

// 鎼滅储鏂规硶
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    message.warning('璇疯緭鍏ユ悳绱㈠叧閿瘝')
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
        message.info('鏈壘鍒扮浉鍏抽鐩?)
      }
    }
  } catch (error) {
    console.error('鎼滅储澶辫触:', error)
    message.error('鎼滅储澶辫触')
  }
}

// 璺宠浆鍒版寚瀹氶鐩?
const jumpToQuestion = (question) => {
  showSearchModal.value = false
  searchKeyword.value = ''
  searchResults.value = []
  
  currentQuestion.value = question
  practiceStore.setCurrentQuestion(question)
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = []
  message.success(`宸茶烦杞埌棰樼洰 #${question.id}`)
}


const exitPractice = () => {
  dialog.warning({
    title: '纭',
    content: '纭畾瑕侀€€鍑虹粌涔犲悧锛?,
    positiveText: '纭畾',
    negativeText: '鍙栨秷',
    onPositiveClick: () => {
      practiceStore.reset()
      currentQuestion.value = null
      userAnswer.value = null
      selectedAnswers.value = [] // 閲嶇疆澶氶€夌瓟妗?
    }
  })
}
</script>
