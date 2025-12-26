<template>
  <div class="home-container">
    <div class="welcome-section">
      <div class="welcome-text">
        <h2 class="welcome-title">早安，同学！<span class="wave">👋</span></h2>
        <p class="welcome-subtitle">距离期末考试越来越近了，保持节奏，你没问题的！</p>
      </div>
      <div class="welcome-decoration">
        <n-icon :component="SchoolOutline" />
      </div>
    </div>

    <n-grid :x-gap="24" :y-gap="24" :cols="4" item-responsive responsive="screen" class="stats-grid">
      <n-grid-item span="4 m:2 l:1">
        <div class="stat-card blue-card" @click="router.push('/question-manage')">
          <div class="stat-content">
            <span class="stat-label">题目总库</span>
            <span class="stat-value">{{ statistics.totalQuestions }}</span>
          </div>
          <div class="stat-icon-bg">
            <n-icon :component="ListOutline" />
          </div>
        </div>
      </n-grid-item>

      <n-grid-item span="4 m:2 l:1">
        <div class="stat-card green-card" @click="router.push('/practice')">
          <div class="stat-content">
            <span class="stat-label">已刷题目</span>
            <span class="stat-value">{{ statistics.practiced }}</span>
          </div>
          <div class="stat-icon-bg">
            <n-icon :component="CreateOutline" />
          </div>
        </div>
      </n-grid-item>

      <n-grid-item span="4 m:2 l:1">
        <div class="stat-card orange-card" @click="router.push('/wrong-book')">
          <div class="stat-content">
            <span class="stat-label">待攻克错题</span>
            <span class="stat-value">{{ statistics.wrongCount }}</span>
          </div>
          <div class="stat-icon-bg">
            <n-icon :component="BookmarkOutline" />
          </div>
        </div>
      </n-grid-item>

      <n-grid-item span="4 m:2 l:1">
        <div class="stat-card purple-card">
          <div class="stat-content">
            <span class="stat-label">正确率</span>
            <span class="stat-value">{{ statistics.correctRate }}%</span>
          </div>
          <div class="stat-icon-bg">
            <n-icon :component="StatsChartOutline" />
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <div class="section-title">快速开始</div>
    <n-grid :x-gap="24" :y-gap="24" :cols="3" item-responsive responsive="screen">
      <n-grid-item span="3 m:1">
        <div class="action-card" @click="router.push('/practice')">
          <div class="action-icon green">
            <n-icon :component="PlayOutline" />
          </div>
          <div class="action-info">
            <div class="action-title">随机练习</div>
            <div class="action-desc">从题库中随机抽取题目，模拟真实考试环境</div>
          </div>
          <div class="action-arrow">→</div>
        </div>
      </n-grid-item>
      
      <n-grid-item span="3 m:1">
        <div class="action-card" @click="router.push('/wrong-book')">
          <div class="action-icon orange">
            <n-icon :component="FlashOutline" />
          </div>
          <div class="action-info">
            <div class="action-title">错题突击</div>
            <div class="action-desc">专注于做错的题目，查漏补缺，巩固知识点</div>
          </div>
          <div class="action-arrow">→</div>
        </div>
      </n-grid-item>

      <n-grid-item span="3 m:1">
        <div class="action-card" @click="router.push('/question-manage')">
          <div class="action-icon blue">
            <n-icon :component="CloudUploadOutline" />
          </div>
          <div class="action-info">
            <div class="action-title">导入题库</div>
            <div class="action-desc">批量上传Excel文件，快速更新和扩展题库</div>
          </div>
          <div class="action-arrow">→</div>
        </div>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NGrid, NGridItem, NIcon, useMessage } from 'naive-ui'
import { 
  ListOutline, CreateOutline, BookmarkOutline, StatsChartOutline, 
  SchoolOutline, PlayOutline, FlashOutline, CloudUploadOutline 
} from '@vicons/ionicons5'
import { getStatistics } from '@/api/practice'

const router = useRouter()
const message = useMessage()

const statistics = ref({
  totalQuestions: 0,
  practiced: 0,
  wrongCount: 0,
  correctRate: 0
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getStatistics()
    if (res.code === 200) {
      statistics.value.totalQuestions = res.data.totalQuestions || 0
      statistics.value.practiced = res.data.practicedQuestionCount || 0
      statistics.value.wrongCount = res.data.wrongQuestionCount || 0
      statistics.value.correctRate = res.data.correctRate || 0
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
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(120deg, #10b981 0%, #059669 100%);
  border-radius: 24px;
  padding: 40px 48px;
  color: white;
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px -10px rgba(16, 185, 129, 0.4);
}

.welcome-text {
  position: relative;
  z-index: 2;
}

.welcome-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  font-weight: 500;
  max-width: 600px;
}

.wave {
  display: inline-block;
  animation: wave 2s infinite;
  transform-origin: 70% 70%;
}

@keyframes wave {
  0% { transform: rotate(0deg); }
  10% { transform: rotate(14deg); }
  20% { transform: rotate(-8deg); }
  30% { transform: rotate(14deg); }
  40% { transform: rotate(-4deg); }
  50% { transform: rotate(10deg); }
  60% { transform: rotate(0deg); }
  100% { transform: rotate(0deg); }
}

.welcome-decoration {
  position: absolute;
  right: -20px;
  bottom: -40px;
  opacity: 0.15;
  font-size: 200px;
  transform: rotate(-15deg);
  pointer-events: none;
}

/* 统计卡片 */
.stats-grid {
  margin-bottom: 40px;
}

.stat-card {
  border-radius: 20px;
  padding: 24px;
  height: 140px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(0,0,0,0.05);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px -10px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: -1px;
}

.stat-icon-bg {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  opacity: 0.8;
}

/* 卡片颜色主题 */
.blue-card { background: #eff6ff; }
.blue-card .stat-icon-bg { background: #dbeafe; color: #3b82f6; }
.blue-card:hover { border-color: #bfdbfe; }

.green-card { background: #f0fdf4; }
.green-card .stat-icon-bg { background: #dcfce7; color: #10b981; }
.green-card:hover { border-color: #bbf7d0; }

.orange-card { background: #fff7ed; }
.orange-card .stat-icon-bg { background: #ffedd5; color: #f97316; }
.orange-card:hover { border-color: #fed7aa; }

.purple-card { background: #faf5ff; }
.purple-card .stat-icon-bg { background: #f3e8ff; color: #a855f7; }
.purple-card:hover { border-color: #e9d5ff; }


.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 20px;
  background: #10b981;
  border-radius: 2px;
  margin-right: 12px;
}

/* 快捷入口 */
.action-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  border: 1px solid #f1f5f9;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.02);
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.05), 0 10px 10px -5px rgba(0,0,0,0.02);
  border-color: #e2e8f0;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
}

.action-icon.green { background: linear-gradient(135deg, #10b981 0%, #059669 100%); box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.3); }
.action-icon.orange { background: linear-gradient(135deg, #f97316 0%, #ea580c 100%); box-shadow: 0 10px 15px -3px rgba(249, 115, 22, 0.3); }
.action-icon.blue { background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.3); }

.action-info {
  flex: 1;
}

.action-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
}

.action-arrow {
  font-size: 20px;
  color: #cbd5e1;
  font-weight: bold;
  transition: all 0.3s;
}

.action-card:hover .action-arrow {
  transform: translateX(4px);
  color: #1e293b;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .welcome-section {
    padding: 32px;
  }
  .welcome-title {
    font-size: 24px;
  }
  .welcome-decoration {
    font-size: 150px;
    right: -40px;
  }
}
</style>
