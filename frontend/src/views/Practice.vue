<template>
  <div class="practice-container">
    <transition name="fade" mode="out-in">
      <div v-if="!currentQuestion" class="filter-panel">
        <n-card title="练习配置" :bordered="false" size="huge" class="config-card">
          <n-form :label-width="80" size="large">
            <n-grid :cols="1" :y-gap="24">
              <n-grid-item>
                <n-form-item label="选择科目">
                  <n-select v-model:value="filters.subject" :options="subjectOptions" placeholder="全部科目" />
                </n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="题目类型">
                  <n-select v-model:value="filters.type" :options="typeOptions" placeholder="混合题型" />
                </n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-button type="primary" block size="large" @click="startPractice" class="start-btn">
                  开始专注练习
                </n-button>
              </n-grid-item>
            </n-grid>
          </n-form>
        </n-card>
      </div>

      <div v-else class="question-panel">
        <div class="question-header">
          <div class="header-left">
            <n-tag :type="getTypeColor()" size="small" round>
              {{ getTypeLabel() }}
            </n-tag>
            <span class="subject-text">{{ currentQuestion.subject }}</span>
          </div>
          <div class="header-right">
             <n-text depth="3">累计练习 {{ practiceStore.totalPracticeCount + 1 }} 题</n-text>
             <n-button quaternary circle size="small" @click="exitPractice">
               <template #icon><n-icon :component="CloseOutline" /></template>
             </n-button>
          </div>
        </div>

        <div class="question-content">
          {{ currentQuestion.content }}
        </div>

        <div class="options-list">
          <!-- 单选题：圆形选项卡片 -->
          <template v-if="currentQuestion.type === 'single-choice' && options.length > 0">
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
                <n-icon :component="CheckmarkCircle" color="#18a058" size="24"/>
              </div>
               <div v-if="practiceStore.showAnalysis && userAnswer === option.key && userAnswer !== currentQuestion.answer" class="result-icon">
                <n-icon :component="CloseCircle" color="#d03050" size="24"/>
              </div>
            </div>
          </template>

          <!-- 多选题：方形复选框卡片 -->
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
                <n-icon :component="CheckmarkCircle" color="#18a058" size="24"/>
              </div>
            </div>
          </template>
          
          <!-- 选择题（兼容所有选择题类型：single-choice, multiple-choice, choice）-->
          <template v-if="(currentQuestion.type === 'single-choice' || currentQuestion.type === 'multiple-choice' || currentQuestion.type === 'choice') && options.length > 0">
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
                <n-icon :component="CheckmarkCircle" color="#18a058" size="24"/>
              </div>
               <div v-if="practiceStore.showAnalysis && userAnswer === option.key && userAnswer !== currentQuestion.answer" class="result-icon">
                <n-icon :component="CloseCircle" color="#d03050" size="24"/>
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
                <n-icon :component="CheckmarkCircle" color="#18a058" size="24"/>
              </div>
               <div v-if="practiceStore.showAnalysis && userAnswer === val && !isCorrect" class="result-icon">
                <n-icon :component="CloseCircle" color="#d03050" size="24"/>
              </div>
            </div>
          </template>
        </div>

        <div class="action-bar">
          <n-button 
            v-if="!practiceStore.showAnalysis" 
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
            v-else 
            type="primary" 
            round 
            size="large" 
            class="action-btn"
            @click="nextQuestion"
          >
            下一题 (Enter)
          </n-button>
        </div>

        <transition name="slide-up">
          <div v-if="practiceStore.showAnalysis" class="analysis-box">
             <div class="analysis-header">
                <span class="label">答案解析</span>
                <n-tag :type="isCorrect ? 'success' : 'error'" size="small">
                  {{ isCorrect ? '回答正确' : '回答错误' }}
                </n-tag>
             </div>
             <div class="analysis-content">
               <p><strong>正确答案：</strong>{{ displayAnswer }}</p>
               <p v-if="currentQuestion.analysis" class="analysis-detail">
                 {{ currentQuestion.analysis }}
               </p>
               <p v-else class="text-gray">暂无详细解析</p>
             </div>
          </div>
        </transition>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog, NCard, NForm, NFormItem, NGrid, NGridItem, NSelect, NButton, NTag, NText, NIcon } from 'naive-ui'
import { CloseOutline, CheckmarkCircle, CloseCircle, CheckmarkOutline } from '@vicons/ionicons5'
import { getRandomQuestion } from '@/api/question'
import { submitAnswer as submitAnswerApi } from '@/api/practice'
import { getAllSubjects } from '@/api/subject'
import { usePracticeStore } from '@/stores/practice'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const practiceStore = usePracticeStore()

const currentQuestion = ref(null)
const userAnswer = ref(null)
const selectedAnswers = ref([]) // 多选题答案数组
const filters = reactive({ subject: null, type: null, difficulty: null })

// 选项配置
const subjectOptions = ref([
  { label: '全部科目', value: null }
])

// 加载科目列表
const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    if (res.data && res.data.length > 0) {
      const subjects = res.data.map(subject => ({
        label: `${subject.name} (${subject.questionCount})`,
        value: subject.name
      }))
      subjectOptions.value = [
        { label: '全部科目', value: null },
        ...subjects
      ]
    }
  } catch (error) {
    console.error('加载科目列表失败', error)
  }
}

const typeOptions = [
  { label: '单选题', value: 'single-choice' },
  { label: '多选题', value: 'multiple-choice' },
  { label: '判断题', value: 'judge' }
]

// 解析选项
const options = computed(() => {
  if (!currentQuestion.value) return []
  const questionType = currentQuestion.value.type
  
  // 选择题类型才需要解析选项
  if (questionType !== 'single-choice' && questionType !== 'multiple-choice' && questionType !== 'choice') return []
  
  try {
    const opts = currentQuestion.value.options
    console.log('原始选项数据:', opts, '类型:', typeof opts)
    
    // 检查选项是否存在且不为空
    if (!opts) {
      console.warn('选项数据不存在，题目ID:', currentQuestion.value.id)
      message.warning('题目选项数据缺失，已跳过')
      return []
    }
    
    // 如果是字符串，尝试解析为JSON
    let parsedOpts = opts
    if (typeof opts === 'string') {
      try {
        parsedOpts = JSON.parse(opts)
      } catch (e) {
        console.error('选项JSON解析失败:', opts)
        message.error('题目选项格式错误')
        return []
      }
    }
    
    // 检查是否为数组且有内容
    if (!Array.isArray(parsedOpts)) {
      console.error('选项不是数组类型:', typeof parsedOpts, parsedOpts)
      message.error('题目选项格式错误')
      return []
    }
    
    if (parsedOpts.length === 0) {
      console.warn('选项数组为空，题目ID:', currentQuestion.value.id)
      message.warning('题目没有可用选项，已跳过')
      return []
    }
    
    // 解析每个选项
    const parsed = parsedOpts.map((opt, index) => {
      // 支持多种格式：
      // 1. "A:选项内容" 
      // 2. "选项内容" (自动分配ABCD)
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
          // 没有冒号，自动分配字母
          const letters = ['A', 'B', 'C', 'D', 'E', 'F']
          return {
            key: letters[index],
            text: opt.trim()
          }
        }
      }
      return null
    }).filter(opt => opt !== null && opt.key && opt.text)
    
    console.log('解析后的选项:', parsed)
    
    if (parsed.length === 0) {
      console.error('所有选项解析后均无效，原始数据:', parsedOpts)
      message.error('题目选项内容无效')
      return []
    }
    
    return parsed
  } catch (e) { 
    console.error('选项解析异常:', e, currentQuestion.value)
    message.error('题目选项解析失败')
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
  window.addEventListener('keyup', handleKeyup)
})
onUnmounted(() => window.removeEventListener('keyup', handleKeyup))

const startPractice = async () => {
  try {
    const res = await getRandomQuestion(filters)
    if (!res.data) {
      message.warning('没有找到符合条件的题目')
      return
    }
    console.log('获取到题目:', res.data)
    console.log('题目类型:', res.data.type)
    console.log('选项数据:', res.data.options)
    console.log('选项类型:', typeof res.data.options, Array.isArray(res.data.options))
    
    currentQuestion.value = res.data
    practiceStore.setCurrentQuestion(res.data)
    userAnswer.value = null
    selectedAnswers.value = [] // 重置多选答案
    
    // 检查选择题/多选题是否有有效选项
    const needsOptions = ['single-choice', 'multiple-choice', 'choice'].includes(res.data.type)
    if (needsOptions) {
      // 等待下一个tick，让computed属性计算完成
      await nextTick()
      if (options.value.length === 0) {
        console.error('题目选项无效，自动跳过', res.data)
        message.warning('题目数据有误，正在获取下一题...')
        // 延迟后自动获取下一题
        setTimeout(() => {
          startPractice()
        }, 1000)
      }
    }
  } catch (error) {
    console.error('获取题目失败:', error)
    message.error('获取题目失败')
  }
}

const submitAnswer = async () => {
  if (!userAnswer.value) return
  try {
    await submitAnswerApi({
      questionId: currentQuestion.value.id,
      userAnswer: userAnswer.value,
      costTime: 0
    })
    practiceStore.submitAnswer(userAnswer.value)
    if (isCorrect.value) message.success('回答正确！')
    else message.error('回答错误！')
  } catch (error) {
    practiceStore.submitAnswer(userAnswer.value)
  }
}

const nextQuestion = async () => {
  practiceStore.reset()
  userAnswer.value = null
  selectedAnswers.value = [] // 重置多选答案
  await startPractice()
}

const exitPractice = () => {
  dialog.warning({
    title: '确认',
    content: '确定要退出练习吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      practiceStore.reset()
      currentQuestion.value = null
      userAnswer.value = null
      selectedAnswers.value = [] // 重置多选答案
    }
  })
}
</script>

<style scoped>
.practice-container {
  max-width: 800px;
  margin: 0 auto;
  min-height: 80vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 筛选面板 */
.filter-panel {
  width: 100%;
  max-width: 400px;
}
.config-card {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
}
.start-btn {
  height: 48px;
  font-size: 16px;
  margin-top: 12px;
}

/* 题目面板 */
.question-panel {
  width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
  position: relative;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left {
  display: flex;
  gap: 12px;
  align-items: center;
}
.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}
.subject-text {
  color: #666;
  font-size: 14px;
}

.question-content {
  font-size: 18px;
  line-height: 1.6;
  color: #1f2225;
  font-weight: 500;
  margin-bottom: 32px;
}

/* 选项列表 */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.option-item:hover:not(.disabled) {
  border-color: #18a058;
  background-color: #f0fdf4;
}

.option-item.selected {
  border-color: #18a058;
  background-color: #e7fcf0;
}

.option-item.disabled {
  cursor: default;
}

/* 结果高亮 */
.option-item.correct-highlight {
  border-color: #18a058;
  background-color: #e7fcf0;
}
.option-item.error-highlight {
  border-color: #d03050;
  background-color: #fff0f0;
}

.option-key {
  width: 32px;
  height: 32px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 16px;
  color: #666;
  flex-shrink: 0;
}
.option-item.selected .option-key {
  background: #18a058;
  color: #fff;
  border-color: #18a058;
}

/* 多选框样式 */
.option-checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #ddd;
  border-radius: 4px;  /* 方形 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  transition: all 0.2s;
  flex-shrink: 0;
  font-size: 16px;
}

.option-checkbox.checked {
  background: #18a058;
  border-color: #18a058;
  color: #fff;
}

.checkbox-item .option-text {
  flex: 1;
}

.option-text {
  font-size: 16px;
  color: #333;
  flex: 1;
}
.center-text {
  width: 100%;
  text-align: center;
}

.result-icon {
  margin-left: auto;
}

/* 底部操作栏 */
.action-bar {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}
.action-btn {
  width: 200px;
}

/* 解析盒子 */
.analysis-box {
  margin-top: 32px;
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #eee;
}
.analysis-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.analysis-header .label {
  font-weight: bold;
  color: #333;
}
.analysis-content {
  font-size: 15px;
  color: #4b5563;
  line-height: 1.6;
}
.text-gray {
  color: #999;
}

/* 动画 */
.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.3s ease-out;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
</style>
