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
        <div class="kpi-value">{{ stats.totalPracticeCount || 0 }}</div>
        <div class="kpi-icon"><n-icon :component="SchoolOutline" /></div>
      </div>

      <div class="sketch-card kpi-card">
        <div class="kpi-title">已练题目 <div class="wavy-line"></div></div>
        <div class="kpi-value green">{{ stats.practicedQuestionCount || 0 }}</div>
        <div class="kpi-icon"><n-icon :component="CheckmarkCircleOutline" /></div>
      </div>

      <div class="sketch-card kpi-card">
        <div class="kpi-title">错题数 <div class="wavy-line"></div></div>
        <div class="kpi-value red">{{ stats.wrongQuestionCount || 0 }}</div>
        <div class="kpi-icon"><n-icon :component="CloseCircleOutline" /></div>
      </div>

      <div class="sketch-card kpi-card">
        <div class="kpi-title">正确率 <div class="wavy-line"></div></div>
        <div class="kpi-value orange">{{ stats.correctRate || 0 }}<span class="unit">%</span></div>
        <div class="kpi-icon"><n-icon :component="StatsChartOutline" /></div>
      </div>
    </div>

    <!-- ECharts 图表区域 -->
    <div class="charts-section">
      <!-- 科目分布饼图 -->
      <div class="sketch-box chart-box">
        <div class="box-header">
          <span class="highlight-title">科目分布</span>
        </div>
        <v-chart class="chart" :option="subjectChartOption" autoresize />
      </div>

      <!-- 题型分布饼图 -->
      <div class="sketch-box chart-box">
        <div class="box-header">
          <span class="highlight-title">题型分布</span>
        </div>
        <v-chart class="chart" :option="typeChartOption" autoresize />
      </div>

      <!-- 难度分布柱状图 -->
      <div class="sketch-box chart-box">
        <div class="box-header">
          <span class="highlight-title">难度分布</span>
        </div>
        <v-chart class="chart" :option="difficultyChartOption" autoresize />
      </div>

      <!-- 练习趋势折线图 -->
      <div class="sketch-box chart-box">
        <div class="box-header">
          <span class="highlight-title">最近7天练习趋势</span>
        </div>
        <v-chart class="chart" :option="trendChartOption" autoresize />
      </div>
    </div>

    <!-- 练习历史记录区域 -->
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
            <div v-for="(record, index) in paginatedRecords" :key="index" class="hand-item">
              <div class="item-status">
                <span class="status-icon" :class="record.isCorrect ? 'correct' : 'wrong'">
                  {{ record.isCorrect ? '✔' : '❌' }}
                </span>
              </div>
              <div class="item-content">
                <div class="item-q">{{ formatQuestion(record.question) }}</div>
                <div class="item-meta">
                  <span class="item-time">{{ formatTime(record.timestamp) }}</span>
                  <span class="item-ans">你选了: <span :class="record.isCorrect ? 'green-text' : 'red-text'">{{ record.userAnswer || '-' }}</span></span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 分页组件 -->
          <div class="pagination-wrapper" v-if="totalRecords > pageSize">
            <n-pagination
              v-model:page="currentPage"
              :page-count="Math.ceil(totalRecords / pageSize)"
              :page-size="pageSize"
            />
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NIcon, NButton, NEmpty, NPagination, useDialog, useMessage
} from 'naive-ui'
import {
  SchoolOutline, CheckmarkCircleOutline, CloseCircleOutline,
  StatsChartOutline, BulbOutline, DocumentTextOutline, TrashOutline
} from '@vicons/ionicons5'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { getStatistics } from '@/api/practice'
import { usePracticeStore } from '@/stores/practice'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  PieChart,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const router = useRouter()
const dialog = useDialog()
const message = useMessage()
const practiceStore = usePracticeStore()

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)

// 总记录数
const totalRecords = computed(() => practiceStore.practiceHistory.length)

// 分页后的记录
const paginatedRecords = computed(() => {
  const allRecords = [...practiceStore.practiceHistory].reverse()
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return allRecords.slice(start, end)
})

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatQuestion = (question) => {
  if (!question) return ''
  // Remove leading empty parentheses like ( ) or （ ）
  return question.replace(/^[\(（]\s*[\)）]\s*/, '')
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


const stats = ref({
  totalQuestions: 0,
  practicedQuestionCount: 0,
  totalPracticeCount: 0,
  wrongQuestionCount: 0,
  correctRate: '0',
  markedQuestionCount: 0,
  subjectDistribution: {},
  typeDistribution: {},
  difficultyDistribution: {},
  recentPractice: []
})

// 通用手绘字体配置
const handTextStyle = { 
  fontFamily: '"Patrick Hand", cursive',
  fontSize: 16
}

// 通用颜色配置 (Pastel colors)
const sketchColors = ['#ffadad', '#ffd6a5', '#fdffb6', '#caffbf', '#9bf6ff', '#a0c4ff', '#bdb2ff', '#ffc6ff']

// 科目分布饼图配置
const subjectChartOption = computed(() => ({
  color: sketchColors,
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} 题 ({d}%)',
    textStyle: handTextStyle,
    backgroundColor: '#fffdf5',
    borderColor: '#2c3e50',
    borderWidth: 2,
    extraCssText: 'box-shadow: 4px 4px 0 rgba(0,0,0,0.1); border-radius: 4px;'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    textStyle: handTextStyle
  },
  series: [
    {
      name: '科目分布',
      type: 'pie',
      radius: ['40%', '70%'], // Donut chart for modern doodle look
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '20',
          fontWeight: 'bold',
          fontFamily: '"Gochi Hand", cursive',
          color: '#2c3e50'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      labelLine: {
        show: false
      },
      data: Object.entries(stats.value.subjectDistribution || {}).map(([name, value]) => ({
        name,
        value
      }))
    }
  ]
}))

// 题型分布饼图配置
const typeChartOption = computed(() => ({
  color: sketchColors.slice(2).concat(sketchColors.slice(0, 2)), // Rotate colors
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} 题 ({d}%)',
    textStyle: handTextStyle,
    backgroundColor: '#fffdf5',
    borderColor: '#2c3e50',
    borderWidth: 2,
    extraCssText: 'box-shadow: 4px 4px 0 rgba(0,0,0,0.1); border-radius: 4px;'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    textStyle: handTextStyle
  },
  series: [
    {
      name: '题型分布',
      type: 'pie',
      radius: '60%',
      // Rose Type
      roseType: 'area',
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      data: Object.entries(stats.value.typeDistribution || {}).map(([name, value]) => ({
        name,
        value
      })),
      label: { textStyle: handTextStyle }
    }
  ]
}))

// 难度分布柱状图配置
const difficultyChartOption = computed(() => ({
  color: ['#a0c4ff'], // Light Blue
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    textStyle: handTextStyle,
    backgroundColor: '#fffdf5',
    borderColor: '#2c3e50',
    borderWidth: 2
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: Object.keys(stats.value.difficultyDistribution || {}),
    axisLabel: { ...handTextStyle, rotate: 10 },
    axisLine: { lineStyle: { color: '#2c3e50', width: 2, type: 'dashed' } }
  },
  yAxis: {
    type: 'value',
    axisLabel: handTextStyle,
    splitLine: { lineStyle: { type: 'dotted', color: '#cbd5e1' } }
  },
  series: [
    {
      name: '题目数量',
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        borderRadius: [5, 5, 0, 0],
        borderWidth: 2,
        borderColor: '#2c3e50',
        color: '#a0c4ff'
      },
      data: Object.values(stats.value.difficultyDistribution || {})
    }
  ]
}))
// 练习趋势折线图配置 (Hand Drawn)
const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    textStyle: handTextStyle,
    backgroundColor: '#fffdf5',
    borderColor: '#2c3e50',
    borderWidth: 2
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: (stats.value.recentPractice || []).map(item => item.date),
    axisLabel: { ...handTextStyle, rotate: 10 },
    axisLine: { lineStyle: { color: '#2c3e50', width: 2, type: 'dashed' } }
  },
  yAxis: {
    type: 'value',
    axisLabel: handTextStyle,
    splitLine: { lineStyle: { type: 'dotted', color: '#cbd5e1' } }
  },
  series: [
    {
      name: '练习次数',
      type: 'line',
      smooth: 0.5, // Wobbly line
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: '#34d399',
        borderWidth: 2,
        borderColor: '#2c3e50'
      },
      lineStyle: {
        color: '#34d399',
        width: 3,
        type: 'solid',
        shadowColor: 'rgba(0,0,0,0.2)',
        shadowOffsetY: 3
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(52, 211, 153, 0.4)' },
            { offset: 1, color: 'rgba(52, 211, 153, 0)' }
          ]
        }
      },
      data: (stats.value.recentPractice || []).map(item => item.count)
    }
  ]
}))

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      stats.value = res.data
    } else {
      message.error(res.message || '加载统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    message.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.stats-dashboard {
  max-width: 1200px;
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
  margin-bottom: 40px;
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
  color: #2c3e50;
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

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.sketch-box {
  background: #fff;
  border: 2px solid #2c3e50;
  border-radius: 12px 20px 15px 25px;
  padding: 24px;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.1);
}

.chart-box {
  min-height: 350px;
  position: relative;
  overflow: hidden;
  min-width: 0; /* Fix for Grid item overflow */
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

.chart {
  width: 100%;
  height: 300px;
  position: relative;
}

/* Dashboard Content Grid */
.dashboard-content {
  margin-bottom: 40px;
}

/* History Box */
.history-box {
  width: 100%;
}

.history-list-container { min-height: 200px; }

.hand-list { display: flex; flex-direction: column; gap: 12px; }

.hand-item {
  display: flex;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e2e8f0;
}
.hand-item:last-child { border-bottom: none; }

.status-icon {
  font-size: 18px;
  margin-right: 12px;
  line-height: 1;
}
.status-icon.correct { color: #10b981; }
.status-icon.wrong { color: #ef4444; }

.item-content { flex: 1; }
.item-q { font-size: 18px; margin-bottom: 4px; font-weight: 600; }
.item-meta { font-size: 14px; color: #94a3b8; }
.item-time { margin-right: 16px; }
.green-text { color: #16a34a; font-weight: bold; }
.red-text { color: #ef4444; font-weight: bold; }

/* Empty State */
.empty-state { padding: 40px 0; }
.empty-doodle {
  width: 80px; height: 80px; margin: 0 auto 16px;
  border: 2px dashed #cbd5e1; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.link-btn { font-size: 18px; color: #3b82f6; font-weight: bold; }

.clear-text-btn {
  font-family: 'Patrick Hand', cursive;
  font-size: 16px;
  color: #94a3b8;
}
.clear-text-btn:hover { color: #ef4444; text-decoration: underline; }

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* Advice Section - Torn Paper */
.advice-section {
  position: relative;
  margin-top: 40px;
}

.torn-paper {
  background: #fefce8;
  position: relative;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  filter: drop-shadow(2px 2px 2px rgba(0,0,0,0.1));
}

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

@media (max-width: 768px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>
