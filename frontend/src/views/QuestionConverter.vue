<template>
  <div class="converter-container">
    <div class="converter-header">
      <h2 class="page-title">题库转换工具</h2>
      <p class="page-subtitle">将文本题目快速转换为 Excel 导入格式</p>
    </div>

    <!-- 步骤条 -->
    <n-card :bordered="false" class="steps-card">
      <n-steps :current="currentStep" status="process">
        <n-step
          title="上传文件"
          description="选择包含题目的TXT文件"
        />
        <n-step
          title="解析预览"
          description="确认题目解析结果"
        />
        <n-step
          title="导出Excel"
          description="生成并下载Excel文件"
        />
      </n-steps>
    </n-card>

    <n-space vertical size="large" class="main-content">
      <!-- 步骤 1: 上传文件 -->
      <n-card 
        v-if="currentStep === 1" 
        :bordered="false" 
        class="action-card fade-in"
      >
        <template #header>
          <div class="card-header">
            <n-icon size="24" color="#10b981" :component="DocumentTextOutline" />
            <span>上传题目文件</span>
          </div>
        </template>
        
        <n-alert type="info" title="文件格式要求" class="guide-alert" :show-icon="false">
          <ul class="guide-list">
            <li>仅支持 <strong>.txt</strong> 文本文件</li>
            <li>题目格式：题号开头，选项A-D/E</li>
            <li>答案格式：位于文件末尾，如 <n-tag size="small" :bordered="false">1-5 A B C D E</n-tag></li>
          </ul>
        </n-alert>

        <n-upload
          :max="1"
          accept=".txt"
          :on-before-upload="handleFileSelect"
          :show-file-list="false"
          directory-dnd
        >
          <n-upload-dragger class="custom-dragger">
            <div class="upload-icon-wrapper">
              <n-icon size="64" :depth="3" :component="CloudUploadOutline" />
            </div>
            <n-text class="upload-text">
              点击或拖拽 TXT 文件到此处
            </n-text>
            <n-p depth="3" style="margin-top: 8px">
              系统将自动解析题目内容和答案
            </n-p>
          </n-upload-dragger>
        </n-upload>
      </n-card>

      <!-- 步骤 2: 解析结果 & 补充信息 -->
      <div v-if="currentStep === 2" class="fade-in">
        <n-grid :x-gap="24" :y-gap="24" cols="1 800:2">
          <n-grid-item>
             <n-card :bordered="false" title="基本信息" class="info-card">
                <n-form-item label="科目名称" required>
                  <n-input
                    v-model:value="subjectName"
                    placeholder="例如：计算机网络"
                    size="large"
                  />
                </n-form-item>
                
                <n-form-item label="导出文件名">
                  <n-input
                    v-model:value="exportFileName"
                    placeholder="默认为 '题库'"
                    size="large"
                  >
                    <template #suffix>.xlsx</template>
                  </n-input>
                </n-form-item>
             </n-card>
          </n-grid-item>
          
          <n-grid-item>
            <n-card :bordered="false" title="解析统计" class="stats-card">
              <n-grid cols="2" :y-gap="20">
                 <n-grid-item>
                   <n-statistic label="选择题" :value="parseResult.choiceCount">
                     <template #prefix><n-icon color="#2080f0" :component="ListOutline"/></template>
                   </n-statistic>
                 </n-grid-item>
                 <n-grid-item>
                   <n-statistic label="判断题" :value="parseResult.judgeCount">
                     <template #prefix><n-icon color="#10b981" :component="CheckmarkCircleOutline"/></template>
                   </n-statistic>
                 </n-grid-item>
                 <n-grid-item>
                   <n-statistic label="解析答案数" :value="parseResult.answerCount">
                     <template #prefix><n-icon color="#f0a020" :component="KeyOutline"/></template>
                   </n-statistic>
                 </n-grid-item>
              </n-grid>
            </n-card>
          </n-grid-item>
        </n-grid>

        <n-space justify="space-between" style="margin-top: 24px">
           <n-button @click="resetFlow" size="large">重新上传</n-button>
           <n-button
            type="primary"
            size="large"
            @click="convertToExcel"
            :loading="converting"
            class="convert-btn"
          >
            <template #icon>
              <n-icon :component="DownloadOutline" />
            </template>
            开始转换并导出
          </n-button>
        </n-space>
      </div>
      
       <!-- 步骤 3: 完成 -->
       <n-result
        v-if="currentStep === 3"
        status="success"
        title="转换成功"
        description="Excel 文件已开始下载"
        class="fade-in result-container"
      >
        <template #footer>
          <n-space justify="center">
            <n-button @click="resetFlow">处理下一个文件</n-button>
          </n-space>
        </template>
      </n-result>

    </n-space>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { 
  useMessage, NCard, NSpace, NAlert, NUpload, NButton, NIcon, 
  NTag, NStatistic, NFormItem, NInput, NSteps, NStep, NUploadDragger, NText, NP,
  NGrid, NGridItem, NResult
} from 'naive-ui'
import { 
  DocumentTextOutline, CheckmarkCircle, DownloadOutline, CloudUploadOutline,
  ListOutline, CheckmarkCircleOutline, KeyOutline
} from '@vicons/ionicons5'
import * as XLSX from 'xlsx'

const message = useMessage()
const fileName = ref('')
const fileContent = ref('')
const parseResult = ref(null)
const converting = ref(false)
const exportFileName = ref('题库')
const subjectName = ref('未分类')
const currentStep = ref(1)

const handleFileSelect = (options) => {
  const file = options.file.file
  fileName.value = file.name

  const reader = new FileReader()
  reader.onload = (e) => {
    fileContent.value = e.target.result
    parseQuestions()
  }
  reader.readAsText(file, 'utf-8')
  
  return false // 阻止自动上传
}

const parseQuestions = () => {
  try {
    const content = fileContent.value
    
    // 分离题目和答案部分
    const lines = content.split('\n')
    let answerStartLineIndex = -1
    
    // 从后往前找答案行
    for (let i = lines.length - 1; i >= 0; i--) {
      const line = lines[i].trim()
      // 答案行特征：包含 "数字-数字" 格式且后面跟着字母答案
      if (/^\d+-\d+\s+[A-E\s]+/.test(line) || /\d+-\d+\s+[A-E]/.test(line)) {
        answerStartLineIndex = i
      } else if (answerStartLineIndex !== -1 && line && !/^\d+-\d+/.test(line)) {
        break
      }
    }
    
    let questionPart = content
    let answerPart = ''
    
    if (answerStartLineIndex > 0) {
      questionPart = lines.slice(0, answerStartLineIndex).join('\n')
      answerPart = lines.slice(answerStartLineIndex).join('\n')
    }
    
    // 解析题目
    const { choiceQuestions, judgeQuestions } = parseQuestionText(questionPart)
    
    // 解析答案
    const answers = parseAnswers(answerPart)
    
    parseResult.value = {
      choiceQuestions,
      judgeQuestions,
      answers,
      choiceCount: choiceQuestions.length,
      judgeCount: judgeQuestions.length,
      answerCount: Object.keys(answers).length
    }
    
    message.success('题目解析成功！')
    currentStep.value = 2 // 进入下一步
  } catch (error) {
    console.error('解析失败:', error)
    message.error('题目解析失败，请检查文件格式')
  }
}

const parseQuestionText = (content) => {
  const choiceQuestions = []
  const judgeQuestions = []
  
  const lines = content.trim().split('\n')
  
  // 跳过第一行（如果不是题号开头）
  let startIndex = 0
  if (lines.length > 0 && !/^\d+[\.\、]?\s*/.test(lines[0])) {
    startIndex = 1
  }
  
  let currentQuestion = null
  
  for (let i = startIndex; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line) continue
    
    // 忽略答案行 (e.g. "1-5 A B C D") 防止被识别为题目
    if (/^\d+-\d+/.test(line)) continue
    
    // 匹配题号
    const questionMatch = line.match(/^(\d+)[\.\、]?\s*(.+)/)
    if (questionMatch) {
      if (currentQuestion) {
        if (Object.keys(currentQuestion.options).length > 0) {
          choiceQuestions.push(currentQuestion)
        } else {
          judgeQuestions.push(currentQuestion)
        }
      }
      
      currentQuestion = {
        num: parseInt(questionMatch[1]),
        content: questionMatch[2].trim(),
        options: {},
        subject: subjectName.value || '未分类',
        difficulty: 'medium'
      }
      continue
    }
    
    // 匹配选项
    const optionMatch = line.match(/^([A-E])\s*[\.\、\:：]?\s*(.+)/)
    if (optionMatch && currentQuestion) {
      const key = optionMatch[1]
      const value = optionMatch[2].trim()
      if (value) {
        currentQuestion.options[key] = value
      }
    }
  }
  
  if (currentQuestion) {
    if (Object.keys(currentQuestion.options).length > 0) {
      choiceQuestions.push(currentQuestion)
    } else {
      judgeQuestions.push(currentQuestion)
    }
  }
  
  return { choiceQuestions, judgeQuestions }
}

const parseAnswers = (answerText) => {
  const answers = {}
  if (!answerText || !answerText.trim()) return answers
  
  const allText = answerText.replace(/\n/g, ' ').replace(/\s+/g, ' ').trim()
  const rangePattern = /(\d+)-(\d+)\s+([A-E\s]+?)(?=\d+-\d+|$)/g
  let match
  
  while ((match = rangePattern.exec(allText)) !== null) {
    const start = parseInt(match[1])
    const end = parseInt(match[2])
    const answersStr = match[3].trim()
    
    const answerLetters = answersStr.split(/\s+/).filter(a => /^[A-E]+$/.test(a))
    
    for (let j = 0; j < answerLetters.length && (start + j) <= end; j++) {
      answers[start + j] = answerLetters[j]
    }
  }
  
  return answers
}

const convertToExcel = () => {
  converting.value = true
  
  try {
    const { choiceQuestions, judgeQuestions, answers } = parseResult.value
    
    const wb = XLSX.utils.book_new()
    
    // 创建选择题工作表
    const choiceData = [
      ['题目', '选项A', '选项B', '选项C', '选项D', '选项E', '答案', '解析', '科目', '难度']
    ]
    
    for (const q of choiceQuestions) {
      choiceData.push([
        q.content,
        q.options['A'] || '',
        q.options['B'] || '',
        q.options['C'] || '',
        q.options['D'] || '',
        q.options['E'] || '',
        answers[q.num] || '',
        '',
        subjectName.value || '未分类', // 使用用户输入的科目
        q.difficulty
      ])
    }
    
    const wsChoice = XLSX.utils.aoa_to_sheet(choiceData)
    
    wsChoice['!cols'] = [
      { wch: 50 }, { wch: 30 }, { wch: 30 }, { wch: 30 }, { wch: 30 }, { wch: 30 },
      { wch: 10 }, { wch: 40 }, { wch: 12 }, { wch: 10 }
    ]
    
    XLSX.utils.book_append_sheet(wb, wsChoice, '选择题')
    
    // 创建判断题工作表（如果有）
    if (judgeQuestions.length > 0) {
      const judgeData = [
        ['题目', '答案', '解析', '科目', '难度']
      ]
      
      for (const q of judgeQuestions) {
        let answer = answers[q.num] || ''
        // 将字母转换为判断题答案
        if (['T', 'True', '对', '√', 'A'].includes(answer)) { // 假设 A 是正确
           // 这里需要根据实际情况判断，通常判断题 A=对, B=错
           answer = '正确'
        } else if (['F', 'False', '错', '×', 'B'].includes(answer)) {
           answer = '错误'
        }
        
        judgeData.push([
          q.content,
          answer,
          '',
          subjectName.value || '未分类',
          q.difficulty
        ])
      }
      
      const wsJudge = XLSX.utils.aoa_to_sheet(judgeData)
      wsJudge['!cols'] = [
        { wch: 60 }, { wch: 10 }, { wch: 40 }, { wch: 12 }, { wch: 10 }
      ]
      
      XLSX.utils.book_append_sheet(wb, wsJudge, '判断题')
    }
    
    const customName = exportFileName.value.trim() || '题库'
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
    const filename = `${customName}_${timestamp}.xlsx`
    
    XLSX.writeFile(wb, filename)
    
    message.success('Excel 文件已生成并下载！')
    currentStep.value = 3 // 完成
  } catch (error) {
    console.error('转换失败:', error)
    message.error('转换失败，请重试')
  } finally {
    converting.value = false
  }
}

const resetFlow = () => {
  currentStep.value = 1
  fileName.value = ''
  fileContent.value = ''
  parseResult.value = null
  exportFileName.value = '题库'
  subjectName.value = '未分类'
}
</script>

<style scoped>
.converter-container {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 60px;
}

.converter-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #6b7280;
  font-size: 16px;
}

.steps-card {
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  margin-bottom: 32px;
  padding: 12px 0;
}

.action-card, .info-card, .stats-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  transition: transform 0.2s;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.guide-alert {
  margin-bottom: 24px;
  border-radius: 8px;
}

.guide-list {
  padding-left: 20px;
  margin: 0;
  color: #4b5563;
}
.guide-list li {
  margin-bottom: 4px;
}

.custom-dragger {
  background-color: #f9fafb;
  border-radius: 12px;
  transition: all 0.3s;
}
.custom-dragger:hover {
  background-color: #f0fdf4;
  border-color: #10b981;
}

.upload-icon-wrapper {
  margin-bottom: 16px;
}

.upload-text {
  font-size: 18px;
  font-weight: 500;
}

.convert-btn {
  font-weight: 600;
  padding: 0 32px;
}

.result-container {
  margin-top: 60px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
