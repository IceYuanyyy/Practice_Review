<template>
  <div class="wrong-book-container">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">错题本</h2>
        <div class="stats-badge">
          <span class="count">{{ practiceStore.wrongQuestions.length }}</span>
          <span class="label">道错题</span>
        </div>
      </div>
      <n-button 
        v-if="practiceStore.wrongQuestions.length > 0" 
        type="error" 
        ghost
        @click="clearWrongBook"
        class="clear-btn"
      >
        <template #icon><n-icon :component="TrashOutline"/></template>
        清空错题本
      </n-button>
    </div>

    <!-- 空状态 -->
    <n-card v-if="practiceStore.wrongQuestions.length === 0" :bordered="false" class="empty-card">
      <n-empty description="太棒了！目前没有错题" class="empty-state">
        <template #icon>
          <div class="success-icon-bg">
            <n-icon :component="CheckmarkCircle" size="64" color="#10b981" />
          </div>
        </template>
        <template #extra>
          <n-button type="primary" size="large" @click="router.push('/practice')" class="practice-btn">
            去练习新题
          </n-button>
        </template>
      </n-empty>
    </n-card>

    <!-- 错题列表 -->
    <div v-else class="question-list">
      <n-card
        v-for="(question, index) in practiceStore.wrongQuestions"
        :key="question.id"
        :bordered="false"
        class="question-card"
        content-style="padding: 0;"
      >
        <div class="card-content">
          <div class="question-header">
            <span class="index-badge">#{{ index + 1 }}</span>
            <n-tag :bordered="false" size="small" :type="getTypeTagType(question.type)" class="type-tag">
              {{ getTypeLabel(question.type) }}
            </n-tag>
            <n-tag :bordered="false" size="small" type="default" class="subject-tag">
              {{ question.subject }}
            </n-tag>
          </div>

          <div class="question-body">
            <h3 class="question-text">{{ question.content }}</h3>
            
            <div 
              v-if="['single-choice', 'multiple-choice', 'choice'].includes(question.type)" 
              class="options-list"
            >
              <div 
                v-for="option in parseOptions(question.options)" 
                :key="option"
                class="option-item"
              >
                {{ option }}
              </div>
            </div>
          </div>

          <div class="analysis-section">
            <div class="answer-row">
              <span class="label">正确答案：</span>
              <span class="correct-answer">{{ question.answer }}</span>
            </div>
            
            <div v-if="question.analysis" class="analysis-box">
              <div class="analysis-title">
                <n-icon :component="BulbOutline" color="#f59e0b" />
                解析
              </div>
              <p class="analysis-text">{{ question.analysis }}</p>
            </div>
          </div>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { NSpace, NCard, NIcon, NText, NTag, NButton, NEmpty, useDialog, useMessage } from 'naive-ui'
import { BookmarkOutline, CheckmarkCircle, TrashOutline, BulbOutline } from '@vicons/ionicons5'
import { usePracticeStore } from '@/stores/practice'

const router = useRouter()
const dialog = useDialog()
const message = useMessage()
const practiceStore = usePracticeStore()

const getTypeTagType = (type) => {
  const map = {
    'single-choice': 'primary',
    'multiple-choice': 'warning',
    'judge': 'info',
    'choice': 'primary'
  }
  return map[type] || 'default'
}

const getTypeLabel = (type) => {
  const map = {
    'single-choice': '单选题',
    'multiple-choice': '多选题',
    'judge': '判断题',
    'choice': '选择题'
  }
  return map[type] || type
}

const parseOptions = (options) => {
  try {
    if (Array.isArray(options)) {
      return options
    }
    return JSON.parse(options || '[]')
  } catch (e) {
    console.error('解析选项失败:', e)
    return []
  }
}

import { clearWrongBook as clearWrongBookApi, getWrongQuestions } from '@/api/practice'

// ... existing imports

const clearWrongBook = () => {
  dialog.warning({
    title: '确认清空',
    content: '确定要清空错题本吗？此操作不可恢复！',
    positiveText: '确认清空',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await clearWrongBookApi()
        practiceStore.wrongQuestions = []
        message.success('已清空错题本')
        // 重新加载数据以确保同步
        loadData() 
      } catch (error) {
        console.error('清空失败', error)
        message.error('清空失败')
      }
    }
  })
}

const loadData = async () => {
  // 这里假设 store 有 actions 或者直接调用 API 更新 store
  // 如果 store 没有现成的 load action，我们手动调用
  try {
     const res = await getWrongQuestions({ page: 1, size: 1000 }) // 获取足够多的错题用于展示，或者使用 store 的 action
     if (res.code === 200) {
       practiceStore.wrongQuestions = res.data.records
     }
  } catch (e) {
    console.error('加载错题失败', e)
  }
}

// 确保组件挂载时加载数据（如果原有代码没有加载逻辑的话）
// onMounted(() => { loadData() })
</script>

<style scoped>
.wrong-book-container {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.stats-badge {
  background: #fee2e2;
  color: #ef4444;
  padding: 4px 12px;
  border-radius: 20px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stats-badge .count {
  font-weight: 700;
  font-size: 18px;
}

.stats-badge .label {
  font-size: 13px;
  font-weight: 500;
}

.empty-card {
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  padding: 40px 0;
}

.success-icon-bg {
  width: 80px;
  height: 80px;
  background: #ecfdf5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.practice-btn {
  font-weight: 600;
  padding: 0 32px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  overflow: hidden;
  transition: transform 0.2s;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.05);
}

.card-content {
  padding: 24px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.index-badge {
  font-size: 14px;
  color: #9ca3af;
  font-family: monospace;
}

.question-text {
  font-size: 18px;
  color: #1f2937;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}

.option-item {
  background: #f9fafb;
  padding: 8px 12px;
  border-radius: 8px;
  color: #4b5563;
  font-size: 15px;
}

.analysis-section {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  margin-top: 16px;
}

.answer-row {
  font-size: 16px;
  margin-bottom: 12px;
}

.answer-row .label {
  color: #64748b;
}

.correct-answer {
  color: #10b981;
  font-weight: 700;
  font-size: 18px;
}

.analysis-box {
  border-top: 1px solid #e2e8f0;
  padding-top: 12px;
}

.analysis-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #d97706;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
}

.analysis-text {
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}
</style>
