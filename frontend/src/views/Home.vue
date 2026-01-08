<template>
  <div class="anti-home">
    <!-- 1. Hero: System Wake Terminal -->
    <div class="terminal-hero">
      <div class="terminal-header">
        <span class="status-dot blinking"></span>
        <span class="terminal-title">SYSTEM_WAKE // 系统唤醒</span>
      </div>
      <div class="terminal-body">
        <div class="boot-sequence">
          <div class="glitch-wrapper">
             <h1 class="hero-title" data-text="WAKE UP, STUDENT">
               WAKE UP, <br/>
               <span class="user-highlight">{{ userStore.nickname || 'STUDENT' }}</span>
             </h1>
          </div>
          <p class="console-log">
            > 检测到主机连接... [OK]<br/>
            > 同步考试数据流... [OK]<br/>
            > 距离最终测试还有 <span class="highlight">UNKNOWN</span> 天
          </p>
        </div>
        
        <!-- Integrity Bar -->
        <div class="integrity-monitor">
          <div class="monitor-label">系统完整度 (CORRECT_RATE)</div>
          <div class="progress-track" :style="{ '--progress': statistics.correctRate + '%' }">
            <div class="progress-fill"></div>
            <div class="progress-val">{{ statistics.correctRate }}%</div>
          </div>
        </div>
      </div>
      
      <div class="decorative-tape">
        <span>DO NOT POWER OFF</span>
        <span>请勿断电</span>
        <span>DO NOT POWER OFF</span>
      </div>
    </div>

    <!-- 2. Stats: Chaos Grid (Data Fragments) -->
    <div class="chaos-section">
      <h3 class="section-label">DATA_FRAGMENTS // 数据碎片</h3>
      <div class="chaos-grid">
        <!-- Card 1: Total Loaded -->
        <div class="chaos-card card-yellow">
          <div class="card-screw top-left"></div>
          <div class="card-screw bottom-right"></div>
          <div class="card-header">
            <span class="card-id">#001</span>
            <span class="card-tag">STORAGE</span>
          </div>
          <div class="card-main">
            <div class="card-label">总库载入</div>
            <div class="card-value font-mono">{{ statistics.totalQuestions }}</div>
          </div>
          <div class="card-footer">
            RUNNING...
          </div>
        </div>

        <!-- Card 2: Executed -->
        <div class="chaos-card card-cyan clicked-effect" @click="router.push('/practice')">
          <div class="card-header">
            <span class="card-id">#002</span>
            <span class="card-tag">EXEC</span>
          </div>
          <div class="card-main">
            <div class="card-label">已执行</div>
            <div class="card-value font-mono">{{ statistics.practiced }}</div>
          </div>
          <div class="card-footer">
            STATUS: ACTIVE
          </div>
        </div>

        <!-- Card 3: Exceptions -->
        <div class="chaos-card card-magenta clicked-effect" @click="router.push('/wrong-book')">
          <div class="card-header">
            <span class="card-id">#ERR</span>
            <span class="card-tag">FATAL</span>
          </div>
          <div class="card-main">
            <div class="card-label">异常/错题</div>
            <div class="card-value font-mono">{{ statistics.wrongCount }}</div>
          </div>
          <div class="card-footer">
            ACTION REQUIRED
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Actions: Control Console -->
    <div class="console-section">
      <h3 class="section-label">CONTROL_CONSOLE // 操作台</h3>
      <div class="control-panel">
        <button class="industrial-btn btn-primary" @click="router.push('/practice')">
          <div class="btn-content">
            <span class="btn-icon">▶</span>
            <div class="btn-text">
              <span class="btn-title">启动随机测试</span>
              <span class="btn-sub">INIT_RANDOM_TEST</span>
            </div>
          </div>
          <div class="btn-shutter"></div>
        </button>

        <button class="industrial-btn btn-warning" @click="router.push('/wrong-book')">
          <div class="btn-content">
            <span class="btn-icon">⚡</span>
            <div class="btn-text">
              <span class="btn-title">修复异常模块</span>
              <span class="btn-sub">FIX_EXCEPTIONS</span>
            </div>
          </div>
          <div class="stripe-bg"></div>
        </button>

        <button class="industrial-btn btn-outline" @click="router.push('/question-manage')">
          <div class="btn-content">
            <span class="btn-icon">＋</span>
            <div class="btn-text">
              <span class="btn-title">注入新数据</span>
              <span class="btn-sub">INJECT_DATA_STREAM</span>
            </div>
          </div>
        </button>
      </div>
    </div>

    <!-- 4. Announcements: Bulletin Board -->
    <div class="bulletin-section" v-if="announcements.length > 0">
      <h3 class="section-label">SYSTEM_BULLETIN // 系统公告</h3>
      <div class="bulletin-board">
        <div 
          v-for="item in announcements" 
          :key="item.id" 
          class="bulletin-card"
          :class="{ 'pinned': item.isPinned }"
          @click="openAnnouncementDetail(item)"
        >
          <div class="bulletin-pin" v-if="item.isPinned">📌</div>
          <h4 class="bulletin-title">{{ item.title }}</h4>
          <div class="bulletin-meta">
            <span class="publisher">{{ item.publisherName || '管理员' }}</span>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
          <p class="bulletin-preview">{{ truncate(item.content, 80) }}</p>
        </div>
      </div>
    </div>

    <!-- Announcement Detail Modal -->
    <n-modal v-model:show="showAnnouncementModal" transform-origin="center">
      <div class="announcement-modal">
        <div class="modal-header">
          <h2>{{ selectedAnnouncement?.title }}</h2>
          <button class="close-btn" @click="showAnnouncementModal = false">✕</button>
        </div>
        <div class="modal-body" v-if="selectedAnnouncement">
          <div class="modal-meta">
            <span>发布者: {{ selectedAnnouncement.publisherName || '管理员' }}</span>
            <span>时间: {{ formatTime(selectedAnnouncement.createTime) }}</span>
          </div>
          <div class="modal-content">{{ selectedAnnouncement.content }}</div>
        </div>
      </div>
    </n-modal>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NModal } from 'naive-ui'
import { getStatistics } from '@/api/practice'
import { getAnnouncements } from '@/api/announcement'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const message = useMessage()

const statistics = ref({
  totalQuestions: 0,
  practiced: 0,
  wrongCount: 0,
  correctRate: 0
})

// 公告相关
const announcements = ref([])
const showAnnouncementModal = ref(false)
const selectedAnnouncement = ref(null)

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

const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncements({ page: 1, size: 5 })
    if (res.code === 200) {
      announcements.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

const openAnnouncementDetail = (item) => {
  selectedAnnouncement.value = item
  showAnnouncementModal.value = true
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const truncate = (str, len) => {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}

onMounted(() => {
  loadStatistics()
  loadAnnouncements()
})
</script>

<style scoped>
/* Industrial/Brutalist Variables */
.anti-home {
  --neon-yellow: #F5E400;
  --neon-cyan: #00E5FF;
  --neon-magenta: #FF3EA5;
  --void-black: #050505;
  --off-white: #FFFDF7;
  
  font-family: 'Courier New', Courier, monospace; /* Fallback monospace */
  min-height: 100vh;
  padding: 40px 20px;
  overflow-x: hidden;
  color: var(--void-black);
  position: relative;

  /* Inherit Background from Layout */
  background: transparent;
}

/* 1. Terminal Hero */
.terminal-hero {
  background: var(--void-black);
  color: var(--neon-cyan);
  border: 4px solid var(--void-black);
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
  box-shadow: 12px 12px 0px rgba(0,0,0,0.2);
}

.terminal-header {
  background: #1a1a1a;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #333;
}
.status-dot {
  width: 12px; height: 12px;
  background: var(--neon-cyan);
  border-radius: 50%;
}
.blinking { animation: blink 1s infinite; }
@keyframes blink { 50% { opacity: 0; } }

.terminal-title {
  font-family: monospace;
  font-size: 14px;
  letter-spacing: 1px;
}

.terminal-body {
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 24px;
}

.hero-title {
  font-family: Impact, sans-serif;
  font-size: 64px;
  line-height: 0.9;
  letter-spacing: -2px;
  margin: 0 0 24px 0;
  color: #fff;
  text-transform: uppercase;
}
.user-highlight {
  color: var(--neon-yellow);
  background: var(--void-black);
  padding: 0 4px;
}

.console-log {
  font-family: monospace;
  color: #888;
  line-height: 1.6;
}
.highlight { color: var(--neon-cyan); }

/* Integrity Bar */
.integrity-monitor {
  min-width: 300px;
  border: 2px solid #333;
  padding: 16px;
  background: #0a0a0a;
}
.monitor-label {
  font-size: 12px;
  margin-bottom: 8px;
  color: #666;
}
.progress-track {
  height: 32px;
  background: #222;
  position: relative;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  width: var(--progress);
  background: repeating-linear-gradient(
    45deg,
    var(--neon-magenta),
    var(--neon-magenta) 10px,
    #d90479 10px,
    #d90479 20px
  );
  transition: width 1s ease-out;
}
.progress-val {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  font-weight: bold;
  font-family: monospace;
  z-index: 2;
  mix-blend-mode: difference;
}

.decorative-tape {
  position: absolute;
  bottom: -15px;
  right: -20px;
  background: var(--neon-yellow);
  color: var(--void-black);
  padding: 5px 40px;
  transform: rotate(-3deg);
  font-weight: bold;
  font-size: 14px;
  border: 2px solid black;
  display: flex;
  gap: 20px;
  box-shadow: 2px 2px 5px rgba(0,0,0,0.2);
}

/* 2. Chaos Grid */
.chaos-section { 
  margin-bottom: 60px; 
  position: relative; 
  z-index: 1;
}
.section-label {
  font-family: monospace;
  font-size: 18px;
  margin-bottom: 30px;
  border-bottom: 3px solid var(--void-black);
  display: inline-block;
  padding-bottom: 4px;
  
  /* Readable BG for section label */
  background: white; 
  padding-left: 8px;
  padding-right: 8px;
  border: 2px solid black;
  box-shadow: 4px 4px 0 black;
}

.chaos-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  padding: 0 20px;
}

.chaos-card {
  flex: 1;
  min-width: 250px;
  background: white;
  border: 3px solid var(--void-black);
  padding: 20px;
  position: relative;
  transition: transform 0.2s;
}
.chaos-card:hover { z-index: 10; transform: scale(1.05) rotate(0deg) !important; }
.clicked-effect:active { transform: scale(0.98) rotate(0deg) !important; }

/* Themes & Rotations */
.card-yellow {
  background: var(--neon-yellow);
  transform: rotate(-2deg);
  box-shadow: 8px 8px 0px var(--void-black);
}
.card-cyan {
  background: var(--neon-cyan);
  transform: rotate(1.5deg) translateY(10px);
  box-shadow: 8px 8px 0px var(--void-black);
}
.card-magenta {
  background: var(--neon-magenta);
  color: white;
  transform: rotate(-1deg) translateY(-5px);
  box-shadow: 8px 8px 0px var(--void-black);
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  font-family: monospace;
  font-size: 12px;
  opacity: 0.7;
}
.card-tag {
  border: 1px solid currentColor;
  padding: 0 4px;
}

.card-label {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 8px;
  text-transform: uppercase;
}
.card-value {
  font-size: 48px;
  font-weight: 900;
  line-height: 1;
}
.font-mono { font-family: 'Courier New', monospace; }

.card-screw {
  width: 8px; height: 8px;
  background: #333;
  border-radius: 50%;
  position: absolute;
}
.top-left { top: 8px; left: 8px; }
.bottom-right { bottom: 8px; right: 8px; }

.card-footer {
  margin-top: 20px;
  font-family: monospace;
  font-size: 10px;
  text-align: right;
  opacity: 0.8;
}

/* 3. Control Console */
.control-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  position: relative;
  z-index: 1;
}

.industrial-btn {
  position: relative;
  background: white;
  border: 3px solid var(--void-black);
  padding: 24px;
  cursor: pointer;
  text-align: left;
  transition: transform 0.1s;
  overflow: hidden;
  box-shadow: 6px 6px 0px var(--void-black);
}
.industrial-btn:active {
  transform: translate(4px, 4px);
  box-shadow: 2px 2px 0px var(--void-black);
}

.btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 16px;
}
.btn-icon {
  font-size: 32px;
}
.btn-title {
  display: block;
  font-weight: 900;
  font-size: 20px;
  text-transform: uppercase;
}
.btn-sub {
  display: block;
  font-family: monospace;
  font-size: 12px;
  margin-top: 4px;
  color: #666;
}

/* Button Variants */
.btn-primary:hover { background: var(--neon-cyan); }
.btn-warning { background: #fff; }
.btn-warning:hover .stripe-bg { opacity: 0.2; }
.stripe-bg {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: repeating-linear-gradient(
    45deg,
    #000,
    #000 10px,
    var(--neon-yellow) 10px,
    var(--neon-yellow) 20px
  );
  opacity: 0.05;
  transition: opacity 0.2s;
}

.btn-outline {
  background: transparent;
  border-style: dashed;
}
.btn-outline:hover {
  background: var(--void-black);
  color: white;
}
.btn-outline:hover .btn-sub { color: #aaa; }

/* Responsive adjustments */
@media (max-width: 768px) {
  .hero-title { font-size: 42px; }
  .chaos-grid { flex-direction: column; gap: 30px; }
  .chaos-card { transform: none !important; width: 100%; }
  .control-panel { grid-template-columns: 1fr; }
}

/* 4. Bulletin Board */
.bulletin-section {
  margin-top: 60px;
  position: relative;
  z-index: 1;
}

.bulletin-board {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 0 20px;
}

.bulletin-card {
  background: white;
  border: 3px solid var(--void-black);
  padding: 20px;
  position: relative;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 6px 6px 0 var(--void-black);
}

.bulletin-card:hover {
  transform: translate(-3px, -3px);
  box-shadow: 9px 9px 0 var(--void-black);
}

.bulletin-card.pinned {
  background: linear-gradient(135deg, #fffbeb 0%, #fff 100%);
  border-color: #f59e0b;
}

.bulletin-pin {
  position: absolute;
  top: -8px;
  right: 10px;
  font-size: 20px;
}

.bulletin-title {
  font-family: Impact, sans-serif;
  font-size: 18px;
  margin: 0 0 10px 0;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.bulletin-meta {
  display: flex;
  gap: 15px;
  font-family: monospace;
  font-size: 11px;
  color: #666;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 2px dashed #eee;
}

.bulletin-preview {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin: 0;
}

/* Announcement Modal */
.announcement-modal {
  background: white;
  border: 4px solid var(--void-black);
  max-width: 600px;
  width: 90vw;
  box-shadow: 12px 12px 0 rgba(0,0,0,0.3);
}

.announcement-modal .modal-header {
  background: var(--void-black);
  color: white;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-modal .modal-header h2 {
  margin: 0;
  font-family: Impact, sans-serif;
  font-size: 22px;
  text-transform: uppercase;
}

.announcement-modal .close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
}

.announcement-modal .close-btn:hover {
  color: var(--neon-yellow);
}

.announcement-modal .modal-body {
  padding: 20px;
}

.announcement-modal .modal-meta {
  display: flex;
  gap: 20px;
  font-family: monospace;
  font-size: 12px;
  color: #666;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px dashed #eee;
}

.announcement-modal .modal-content {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  white-space: pre-wrap;
  max-height: 400px;
  overflow-y: auto;
}
</style>
