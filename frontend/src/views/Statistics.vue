<template>
  <div class="stats-dashboard">
    <div class="dashboard-header">
      <h2 class="hand-title">学习数据看板</h2>
      <p class="hand-subtitle">你的每一点进步都值得记录</p>
    </div>

    <!-- 核心指标 (KPI Cards) -->
    <div class="kpi-section">
      <div class="sketch-card kpi-card">
        <div class="kpi-title">总练习数 <div class="wavy-line"></div></div>
        <div class="kpi-value">{{ practiceStore.totalPracticeCount }}</div>
        <div class="kpi-icon"><n-icon :component="SchoolOutline" /></div>
      </div>

      <div class="sketch-card kpi-card">
        <div class="kpi-title">正确数 <div class="wavy-line"></div></div>
        <div class="kpi-value green">{{ practiceStore.correctCount }}</div>
        <div class="kpi-icon"><n-icon :component="CheckmarkCircleOutline" /></div>
      </div>

      <div class="sketch-card kpi-card">
        <div class="kpi-title">错误数 <div class="wavy-line"></div></div>
        <div class="kpi-value red">{{ practiceStore.wrongCount }}</div>
        <div class="kpi-icon"><n-icon :component="CloseCircleOutline" /></div>
      </div>

      <div class="sketch-card kpi-card">
        <div class="kpi-title">正确率 <div class="wavy-line"></div></div>
        <div class="kpi-value orange">{{ practiceStore.correctRate }}<span class="unit">%</span></div>
        <div class="kpi-icon"><n-icon :component="StatsChartOutline" /></div>
      </div>
    </div>

    <!-- 连接箭头 (SVG) -->
    <div class="arrow-container">
       <svg class="connect-arrow" viewBox="0 0 100 50">
         <path d="M10 10 Q 50 40 90 10" fill="none" stroke="#64748b" stroke-width="2" stroke-dasharray="5,5" />
         <path d="M90 10 L 80 5 M 90 10 L 80 15" fill="none" stroke="#64748b" stroke-width="2" />
       </svg>
    </div>

    <div class="dashboard-content">
      <!-- 历史记录 -->
      <div class="sketch-box history-box">
        <div class="box-header">
           <span class="highlight-title">最近练习记录</span>
           <n-button text class="clear-text-btn" @click="clearHistory">
             <template #icon><n-icon :component="TrashOutline"/></template>
             清空记录
           </n-button>
        </div>

        <div class="history-list-container">
          <n-empty v-if="practiceStore.practiceHistory.length === 0" description="暂无记录" class="empty-state">
             <template #icon>
                <div class="empty-doodle">
                  <n-icon :component="DocumentTextOutline" size="48" />
                </div>
             </template>
             <n-button text class="link-btn" @click="router.push('/practice')">去练习 &rarr;</n-button>
          </n-empty>

          <div v-else class="hand-list">
             <div v-for="(record, index) in recentRecords" :key="index" class="hand-item">
               <div class="item-status">
                 <span class="status-dot" :class="{ correct: record.isCorrect }"></span>
               </div>
               <div class="item-content">
                 <div class="item-q">{{ record.question }}</div>
                 <div class="item-meta">
                   <span class="item-time">{{ formatTime(record.timestamp) }}</span>
                   <span class="item-ans">你选了: <span :class="record.isCorrect ? 'green-text' : 'red-text'">{{ record.userAnswer || '-' }}</span></span>
                 </div>
               </div>
             </div>
          </div>
        </div>
      </div>

      <!-- 学习建议 (便利贴/撕纸效果) -->
      <div class="advice-section">
        <div class="torn-paper">
          <div class="paper-top"></div>
          <div class="paper-content">
             <h3 class="advice-title">
               <n-icon :component="BulbOutline" color="#f59e0b" />
               学习小建议
             </h3>
             <ul class="hand-ul">
               <li>保持每天练习的习惯，哪怕只做 5 道题。</li>
               <li>错题不要只需记住答案，理解背后的知识点更重要。</li>
               <li>正确率超过 80% 时，可以尝试挑战更高难度的题目。</li>
               <li>休息也很重要，劳逸结合效率更高！</li>
             </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NIcon, NButton, NEmpty, useDialog, useMessage
} from 'naive-ui'
import {
  SchoolOutline, CheckmarkCircleOutline, CloseCircleOutline,
  StatsChartOutline, DocumentTextOutline, TrashOutline, BulbOutline
} from '@vicons/ionicons5'
import { usePracticeStore } from '@/stores/practice'

const router = useRouter()
const dialog = useDialog()
const message = useMessage()
const practiceStore = usePracticeStore()

// 最近10条记录
const recentRecords = computed(() => {
  return practiceStore.practiceHistory.slice(-10).reverse()
})

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const clearHistory = () => {
  dialog.warning({
    title: '擦除记录',
    content: '要把黑板擦干净吗？记录清空后无法恢复哦。',
    positiveText: '擦干净',
    negativeText: '留着',
    onPositiveClick: () => {
      practiceStore.clearHistory()
      message.success('记录已擦除')
    }
  })
}
</script>

<style scoped>
.stats-dashboard {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px 60px;
  font-family: 'Patrick Hand', cursive;
  color: #2c3e50;
}

/* Header */
.dashboard-header { text-align: center; margin-bottom: 40px; }
.hand-title { 
  font-family: 'Gochi Hand', cursive; 
  font-size: 48px; 
  margin: 0; 
  color: #1e293b;
  text-shadow: 2px 2px 0px rgba(0,0,0,0.1);
}
.hand-subtitle { color: #64748b; font-size: 18px; margin-top: 8px; }

/* KPI Section */
.kpi-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  margin-bottom: 10px;
}

.sketch-card {
  background: #fff;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  padding: 24px;
  position: relative;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.1);
  transition: transform 0.2s;
}
.sketch-card:hover { transform: translateY(-3px) rotate(-1deg); }

.kpi-title { 
  font-size: 18px; 
  color: #64748b; 
  margin-bottom: 12px; 
  position: relative;
  display: inline-block;
}
.wavy-line {
  height: 4px;
  width: 100%;
  background-image: radial-gradient(circle, #cbd5e1 1px, transparent 1.5px);
  background-size: 6px 6px;
  margin-top: 4px;
}

.kpi-value {
  font-family: 'Gochi Hand', cursive;
  font-size: 48px;
  line-height: 1;
  color: #2c3e50; /* Ink color */
}
.kpi-value.green { color: #16a34a; }
.kpi-value.red { color: #ef4444; }
.kpi-value.orange { color: #f59e0b; }
.unit { font-size: 24px; color: #94a3b8; margin-left: 4px; }

.kpi-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  opacity: 0.1;
  font-size: 48px;
  color: #2c3e50;
}

/* Connect Arrow */
.arrow-container {
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}
.connect-arrow { width: 100px; height: 50px; opacity: 0.6; }

/* Dashboard Content Grid */
.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
}
@media (max-width: 768px) {
  .dashboard-content { grid-template-columns: 1fr; }
}

/* History Box */
.sketch-box {
  background: #fff;
  border: 2px solid #2c3e50;
  border-radius: 12px 20px 15px 25px;
  padding: 24px;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.1);
}

.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.highlight-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 24px;
  background: linear-gradient(120deg, rgba(186, 230, 253, 0.5) 0%, rgba(186, 230, 253, 0) 100%);
  padding: 0 8px;
  border-radius: 4px;
}

.clear-text-btn {
  font-family: 'Patrick Hand', cursive;
  font-size: 16px;
  color: #94a3b8;
}
.clear-text-btn:hover { color: #ef4444; text-decoration: underline; }

.history-list-container { min-height: 200px; }

.hand-list { display: flex; flex-direction: column; gap: 12px; }

.hand-item {
  display: flex;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e2e8f0;
}
.hand-item:last-child { border-bottom: none; }

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ef4444;
  margin-top: 6px;
  margin-right: 12px;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #ef4444;
}
.status-dot.correct { background: #10b981; box-shadow: 0 0 0 1px #10b981; }

.item-content { flex: 1; }
.item-q { font-size: 18px; margin-bottom: 4px; font-weight: 600; }
.item-meta { font-size: 14px; color: #94a3b8; }
.green-text { color: #16a34a; font-weight: bold; }
.red-text { color: #ef4444; font-weight: bold; }

/* Empty State Doodle */
.empty-state { padding: 40px 0; }
.empty-doodle {
  width: 80px; height: 80px; margin: 0 auto 16px;
  border: 2px dashed #cbd5e1; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.link-btn { font-size: 18px; color: #3b82f6; font-weight: bold; }

/* Advice Section - Torn Paper */
.advice-section {
  position: relative;
}

.torn-paper {
  background: #fefce8; /* pale yellow */
  position: relative;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  /* 简单的CSS模拟撕裂边缘不太容易完美，这里用简单的波浪边框代替，或者 CSS Mask */
  filter: drop-shadow(2px 2px 2px rgba(0,0,0,0.1));
}

/* Top jagged edge simulation using gradient */
.paper-top {
  height: 10px;
  background-image: linear-gradient(135deg, transparent 50%, #fefce8 50%), linear-gradient(45deg, #fefce8 50%, transparent 50%);
  background-size: 20px 20px;
  background-repeat: repeat-x;
  background-position: bottom;
  position: absolute;
  top: -10px;
  left: 0;
  width: 100%;
}

.paper-content {
  font-family: 'Patrick Hand', cursive;
  color: #4b5563;
}
.advice-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 22px;
  display: flex; 
  align-items: center; 
  gap: 8px;
  margin-bottom: 16px;
  color: #d97706;
}

.hand-ul {
  list-style: none;
  padding: 0;
}
.hand-ul li {
  position: relative;
  padding-left: 20px;
  margin-bottom: 10px;
  line-height: 1.4;
  font-size: 16px;
}
.hand-ul li::before {
  content: '-';
  position: absolute;
  left: 0;
  font-weight: bold;
  color: #d97706;
}
</style>
