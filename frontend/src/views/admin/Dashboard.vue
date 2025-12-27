<template>
  <div class="dashboard-comic">
    <!-- Comic Title -->
    <div class="page-header">
      <div class="caption-box">MEANWHILE, AT HEADQUARTERS...</div>
      <h1 class="main-title">MISSION DASHBOARD</h1>
    </div>

    <!-- Stat Panels Row -->
    <div class="comic-panels-row">
      <div class="comic-panel panel-cyan" v-motion-pop>
        <div class="panel-bg"></div>
        <div class="panel-content">
          <div class="panel-icon"><n-icon :component="PeopleOutline" /></div>
          <div class="stat-number">{{ stats.userCount }}</div>
          <div class="stat-label">HEROES RECRUITED</div>
        </div>
        <div class="sound-effect">WOW!</div>
      </div>

      <div class="comic-panel panel-magenta" v-motion-pop>
        <div class="panel-bg"></div>
        <div class="panel-content">
          <div class="panel-icon"><n-icon :component="DocumentTextOutline" /></div>
          <div class="stat-number">{{ stats.questionCount }}</div>
          <div class="stat-label">CHALLENGES READY</div>
        </div>
      </div>

      <div class="comic-panel panel-yellow" v-motion-pop>
        <div class="panel-bg"></div>
        <div class="panel-content">
          <div class="panel-icon"><n-icon :component="TimeOutline" /></div>
          <div class="stat-number">{{ stats.todayLoginCount }}</div>
          <div class="stat-label">LOGINS TODAY</div>
        </div>
      </div>

      <div class="comic-panel panel-black" v-motion-pop>
        <div class="panel-bg"></div>
        <div class="panel-content">
          <div class="panel-icon"><n-icon :component="FlashOutline" /></div>
          <div class="stat-number">{{ stats.todayActiveCount }}</div>
          <div class="stat-label">CURRENTLY ACTIVE</div>
        </div>
        <div class="sound-effect">ZAP!</div>
      </div>
    </div>

    <!-- Bottom Section: System Info + Extra Content to fill space -->
    <div class="bottom-section">
      <!-- System Status (Computer Terminal Style) -->
      <div class="system-monitor">
        <div class="monitor-header">
          <span class="blinking-dot">●</span> SYSTEM_STATUS_LOG
        </div>
        <div class="monitor-screen">
          <div class="code-line">> INITIALIZING CORE SYSTEMS... OK</div>
          <div class="code-line">> DETECTING FRAMEWORK: VUE 3.4.0 ... OK</div>
          <div class="code-line">> DATABASE CONNECTION: MYSQL 8.0 ... STABLE</div>
          <div class="code-line">> SPRING BOOT ENGINE: 2.7.18 ... RUNNING</div>
          <div class="code-line">> SECURITY PROTOCOLS ... ACTIVE</div>
          <div class="code-line blinking-cursor">_</div>
        </div>
      </div>

      <!-- Quick Actions / Daily Briefing (Fills the right side gap) -->
      <div class="daily-briefing">
        <div class="briefing-tape">TOP SECRET</div>
        <div class="briefing-content">
          <h3>DAILY MISSION</h3>
          <p>Review the latest anomaly reports in the <strong>Login Logs</strong> sector.</p>
          <n-button type="warning" dashed class="action-btn" @click="$router.push('/admin/login-logs')">
            GO TO LOGS ->
          </n-button>
          
          <div class="comic-sticker">
            <n-icon :component="PlanetOutline" size="60" />
            <span>HQ</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Background Decorations -->
    <div class="page-footer-deco">
      CITY_LIMITS // SECTOR_7
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { NIcon, NButton } from 'naive-ui'
import { 
  PeopleOutline, 
  DocumentTextOutline, 
  TimeOutline, 
  FlashOutline,
  PlanetOutline
} from '@vicons/ionicons5'
import request from '@/api/request'

const stats = ref({
  userCount: 0,
  questionCount: 0,
  todayLoginCount: 0,
  todayActiveCount: 0
})

onMounted(async () => {
  try {
    const res = await request.get('/admin/statistics')
    stats.value = res.data
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Bangers&family=Permanent+Marker&family=Share+Tech+Mono&display=swap');

.dashboard-comic {
  max-width: 1400px;
  margin: 0 auto; /* Centered Layout */
  padding: 1rem;
  font-family: 'Bangers', cursive;
  position: relative;
  min-height: 80vh; /* Ensure it takes space */
  display: flex;
  flex-direction: column;
}

/* Header */
.page-header {
  margin-bottom: 2rem;
  position: relative;
}

.caption-box {
  background: #FFD700;
  border: 3px solid #000;
  padding: 5px 15px;
  display: inline-block;
  font-family: 'Permanent Marker', cursive;
  box-shadow: 4px 4px 0 #000;
  margin-bottom: 10px;
  transform: rotate(-2deg);
}

.main-title {
  font-size: 3.5rem;
  color: #E23636;
  text-shadow: 
    3px 3px 0 #000,
    -1px -1px 0 #000,
    1px -1px 0 #000,
    -1px 1px 0 #000,
    1px 1px 0 #000;
  margin: 0;
  letter-spacing: 2px;
  transform: skew(-2deg);
}

/* Comic Panels Row */
.comic-panels-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.comic-panel {
  position: relative;
  height: 200px;
  border: 4px solid #000;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: pointer;
  overflow: hidden;
}

.comic-panel:hover {
  transform: scale(1.05) rotate(2deg);
  z-index: 10;
  box-shadow: 10px 10px 0 rgba(0,0,0,0.2);
}

.panel-bg {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  opacity: 0.1;
  background-image: radial-gradient(#000 20%, transparent 20%);
  background-size: 10px 10px;
  z-index: 0;
}

.panel-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.panel-icon {
  font-size: 3rem;
  margin-bottom: 0.5rem;
  filter: drop-shadow(2px 2px 0 #000);
}

.stat-number {
  font-size: 4rem;
  line-height: 1;
  color: #fff;
  text-shadow: 3px 3px 0 #000;
  transform: rotate(-3deg);
}

.stat-label {
  font-size: 1.2rem;
  background: #000;
  color: #fff;
  padding: 2px 8px;
  margin-top: 5px;
  transform: skew(-5deg);
}

/* Panel Colors */
.panel-cyan { 
  background-color: #00FFFF;
  clip-path: polygon(0 0, 100% 2%, 98% 100%, 2% 98%);
}
.panel-magenta { 
  background-color: #FF00FF; 
  clip-path: polygon(2% 0, 98% 2%, 100% 98%, 0 100%);
}
.panel-yellow { 
  background-color: #FFD700; 
  clip-path: polygon(0 2%, 100% 0, 98% 98%, 2% 100%);
}
.panel-black { 
  background-color: #2C2C2C; 
  color: #fff;
  clip-path: polygon(2% 2%, 98% 0, 100% 100%, 0 98%);
}

.sound-effect {
  position: absolute;
  font-size: 2rem;
  color: #E23636;
  font-weight: bold;
  text-shadow: 2px 2px 0 #fff, 4px 4px 0 #000;
  top: 10px;
  right: 10px;
  transform: rotate(15deg) scale(0);
  transition: transform 0.2s;
}

.comic-panel:hover .sound-effect {
  transform: rotate(15deg) scale(1);
}

/* Bottom Section */
.bottom-section {
  display: grid;
  grid-template-columns: 2fr 1fr; /* Add a block ratio */
  gap: 2rem;
  align-items: stretch;
}

@media (max-width: 768px) {
  .bottom-section {
    grid-template-columns: 1fr;
  }
}

/* System Monitor */
.system-monitor {
  background: #000;
  border: 4px solid #333;
  border-radius: 10px;
  padding: 1rem;
  font-family: 'Share Tech Mono', monospace;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
}

.monitor-header {
  color: #39FF14;
  border-bottom: 2px solid #333;
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.blinking-dot {
  animation: blink 1s infinite;
  color: #FF0055;
}

.monitor-screen {
  color: #39FF14;
  line-height: 1.6;
}

.code-line {
  margin-bottom: 5px;
}

.blinking-cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* Daily Briefing (Fills the gap) */
.daily-briefing {
  background: #FFFEF0;
  border: 4px solid #000; /* Comic border */
  position: relative;
  padding: 2rem;
  transform: rotate(1deg);
  box-shadow: 8px 8px 0 #000;
}

.briefing-tape {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%) rotate(-2deg);
  background: rgba(255, 0, 0, 0.8);
  color: #fff;
  padding: 2px 15px;
  font-family: 'Permanent Marker', cursive;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

.briefing-content h3 {
  font-size: 2rem;
  margin-top: 0;
  text-decoration: underline;
  text-decoration-thickness: 3px;
  text-decoration-color: #E23636;
}

.briefing-content p {
  font-family: 'Permanent Marker', cursive;
  font-size: 1.1rem;
  color: #333;
}

.action-btn {
  margin-top: 1rem;
  font-family: 'Bangers', cursive;
  font-size: 1.2rem;
}

.comic-sticker {
  position: absolute;
  bottom: 10px;
  right: 10px;
  opacity: 0.2;
  transform: rotate(-15deg);
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Footer Deco */
.page-footer-deco {
  margin-top: auto; /* Pushes to bottom */
  padding-top: 2rem;
  text-align: right;
  font-family: 'Share Tech Mono', monospace;
  color: #aaa;
  font-size: 0.8rem;
  letter-spacing: 4px;
}
</style>
