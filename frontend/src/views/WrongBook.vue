<template>
  <n-space vertical :size="16">
    <n-card :bordered="false">
      <n-space justify="space-between" align="center">
        <n-space align="center">
          <n-icon :component="BookmarkOutline" size="24" />
          <n-text style="font-size: 18px; font-weight: bold">我的错题本</n-text>
          <n-tag type="error">{{ practiceStore.wrongQuestions.length }} 题</n-tag>
        </n-space>
        <n-button v-if="practiceStore.wrongQuestions.length > 0" @click="clearWrongBook">
          清空错题本
        </n-button>
      </n-space>
    </n-card>

    <n-card v-if="practiceStore.wrongQuestions.length === 0" :bordered="false">
      <n-empty description="暂无错题，继续加油！">
        <template #icon>
          <n-icon :component="CheckmarkCircleOutline" size="80" color="#18a058" />
        </template>
        <template #extra>
          <n-button type="primary" @click="router.push('/practice')">
            去练习
          </n-button>
        </template>
      </n-empty>
    </n-card>

    <n-list v-else bordered>
      <n-list-item v-for="(question, index) in practiceStore.wrongQuestions" :key="question.id">
        <n-space vertical style="width: 100%">
          <n-space justify="space-between">
            <n-space>
              <n-text strong>第 {{ index + 1 }} 题</n-text>
              <n-tag :type="question.type === 'single-choice' || question.type === 'choice' ? 'info' : question.type === 'multiple-choice' ? 'warning' : 'success'">
                {{ 
                  question.type === 'single-choice' ? '单选题' : 
                  question.type === 'multiple-choice' ? '多选题' : 
                  question.type === 'choice' ? '选择题' : '判断题' 
                }}
              </n-tag>
              <n-tag>{{ question.subject }}</n-tag>
            </n-space>
          </n-space>

          <n-text style="font-size: 15px">{{ question.content }}</n-text>

          <n-space v-if="question.type === 'single-choice' || question.type === 'multiple-choice' || question.type === 'choice'" vertical>
            <n-text v-for="option in parseOptions(question.options)" :key="option">
              {{ option }}
            </n-text>
          </n-space>

          <n-space align="center">
            <n-text strong>正确答案：</n-text>
            <n-tag type="success">{{ question.answer }}</n-tag>
          </n-space>

          <n-space v-if="question.analysis" vertical>
            <n-text strong>答案解析：</n-text>
            <n-text depth="3">{{ question.analysis }}</n-text>
          </n-space>
        </n-space>
      </n-list-item>
    </n-list>
  </n-space>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { NSpace, NCard, NIcon, NText, NTag, NButton, NEmpty, NList, NListItem, useDialog, useMessage } from 'naive-ui'
import { BookmarkOutline, CheckmarkCircleOutline } from '@vicons/ionicons5'
import { usePracticeStore } from '@/stores/practice'

const router = useRouter()
const dialog = useDialog()
const message = useMessage()
const practiceStore = usePracticeStore()

const parseOptions = (options) => {
  try {
    // 后端返回的 options 已经是数组了
    if (Array.isArray(options)) {
      return options
    }
    // 兼容 JSON 字符串格式
    return JSON.parse(options || '[]')
  } catch (e) {
    console.error('解析选项失败:', e)
    return []
  }
}

const clearWrongBook = () => {
  dialog.warning({
    title: '确认',
    content: '确定要清空错题本吗？此操作不可恢复！',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      practiceStore.wrongQuestions = []
      message.success('已清空错题本')
    }
  })
}
</script>
