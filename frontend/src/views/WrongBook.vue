<template>
  <div class="wrong-book-container">
    <div class="wall-header">
      <div class="header-content">
        <h2 class="page-title">我的错题集</h2>
        <div class="stats-decoration">
          <span class="count">{{ practiceStore.wrongQuestions.length }}</span>
          <span class="label">个灵感碎片</span>
        </div>
      </div>
      
      <div class="actions-area">
        <div class="sketch-input-group">
           <n-input placeholder="搜索关键词..." class="sketch-input" round>
             <template #prefix>
               <n-icon :component="SearchOutline" />
             </template>
           </n-input>
        </div>
        
        <n-button 
          v-if="practiceStore.wrongQuestions.length > 0" 
          text
          class="clear-link"
          @click="clearWrongBook"
        >
          <template #icon><n-icon :component="TrashOutline"/></template>
          全部撕掉 (清空)
        </n-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="practiceStore.wrongQuestions.length === 0" class="empty-wall">
       <div class="empty-sketch">
         <n-icon :component="HappyOutline" size="80" color="#cbd5e1" />
         <p>墙面空空如也，太棒了！</p>
         <n-button class="sketch-btn primary" @click="router.push('/practice')">
           去收集新灵感 (练习)
         </n-button>
       </div>
    </div>

    <!-- 错题墙 (Grid Layout) -->
    <div v-else class="sticky-wall">
      <div
        v-for="(question, index) in practiceStore.wrongQuestions"
        :key="question.id"
        class="sticky-note"
        :class="[getNoteColor(index)]"
        :style="getNoteStyle(index)"
      >
        <!-- 胶带效果 -->
        <div class="tape-strip"></div>
        
        <div class="note-content">
          <div class="note-header">
            <span class="note-index">#{{ index + 1 }}</span>
            <div class="doodle-tag" :class="question.type">
              {{ getTypeLabel(question.type) }}
            </div>
          </div>

          <p class="question-text">{{ question.content }}</p>
          
          <div class="note-actions">
            <!-- 简单的文字链接按钮 -->
            <n-button text class="action-link remove" @click="removeQuestion(question.id)">
              移除
            </n-button>
             <n-button text class="action-link retake" @click="retakeQuestion(question)">
              重练
            </n-button>
          </div>
          
          <!-- 解析部分 (默认折叠/简化显示) -->
          <div class="note-footer">
             <div class="answer-peek">
               <span>Ans: </span>
               <span class="correct-val">{{ question.answer }}</span>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { NIcon, NButton, NInput, useDialog, useMessage } from 'naive-ui'
import { TrashOutline, SearchOutline, HappyOutline } from '@vicons/ionicons5'
import { usePracticeStore } from '@/stores/practice'
import { clearWrongBook as clearWrongBookApi, getWrongQuestions } from '@/api/practice'

const router = useRouter()
const dialog = useDialog()
const message = useMessage()
const practiceStore = usePracticeStore()

// 随机样式生成器 (使用索引作为种子，保证列表重排时颜色相对稳定，或者简化处理)
const getNoteColor = (index) => {
  const colors = ['note-yellow', 'note-blue', 'note-pink', 'note-green'];
  return colors[index % colors.length];
}

const getNoteStyle = (index) => {
  // 伪随机旋转角度：-2deg 到 2deg
  // 使用简单的哈希算法保证同一个索引的旋转角度一致（避免重绘导致抖动）
  const seed = index * 12345; 
  const rotation = ((seed % 50) - 25) / 10; // -2.5 to 2.5
  return {
    transform: `rotate(${rotation}deg)`
  }
}

const getTypeLabel = (type) => {
  const map = {
    'single-choice': '单选',
    'multiple-choice': '多选',
    'judge': '判断',
    'choice': '选择'
  }
  return map[type] || '其他'
}

const clearWrongBook = () => {
  dialog.warning({
    title: '确认清空',
    content: '要把墙上的便利贴都撕掉吗？',
    positiveText: '是的，全部撕掉',
    negativeText: '留着吧',
    onPositiveClick: async () => {
      try {
        await clearWrongBookApi()
        practiceStore.wrongQuestions = []
        message.success('错题墙已清空')
      } catch (error) {
        message.error('操作失败')
      }
    }
  })
}

// 模拟移除单个（实际API可能需补充）
const removeQuestion = (id) => {
  // 这里暂时只操作本地store，实际应调用后端接口
  const idx = practiceStore.wrongQuestions.findIndex(q => q.id === id)
  if (idx > -1) {
    practiceStore.wrongQuestions.splice(idx, 1)
    message.info('便利贴已撕下')
  }
}

const retakeQuestion = (question) => {
  // 简单的重练逻辑：跳转到练习页并带上题目ID，或者弹窗
  // 这里简单提示
  message.success('开始重新练习该题')
  // 实际逻辑可扩展
}
</script>

<style scoped>
.wrong-book-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 80vh;
  font-family: 'Patrick Hand', cursive;
}

/* Header Styles */
.wall-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 48px;
  border-bottom: 3px dashed #cbd5e1;
  padding-bottom: 20px;
}

.header-content { display: flex; align-items: baseline; gap: 20px; }

.page-title {
  font-family: 'Gochi Hand', cursive;
  font-size: 42px;
  color: #1e293b;
  position: relative;
  margin: 0;
  z-index: 1;
}

/* Highlighter Effect */
.page-title::after {
  content: '';
  position: absolute;
  bottom: 5px;
  left: -5px;
  right: -5px;
  height: 15px;
  background: rgba(253, 224, 71, 0.6); /* Yellow highlighter */
  z-index: -1;
  transform: rotate(-1deg);
  border-radius: 4px;
}

.stats-decoration {
  font-size: 18px;
  color: #64748b;
  font-weight: 700;
}
.stats-decoration .count { font-size: 24px; color: #ef4444; margin-right: 4px; }

/* Actions */
.actions-area { display: flex; align-items: center; gap: 24px; }

.sketch-input {
  width: 240px;
  font-family: 'Patrick Hand', cursive;
}
/* Deep selector for input border override */
:deep(.n-input) {
  border: 2px solid #94a3b8 !important;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px !important;
  background: transparent !important;
  transition: all 0.2s;
}
:deep(.n-input:hover), :deep(.n-input:focus-within) {
  border-color: #2c3e50 !important;
  box-shadow: 2px 2px 0px rgba(0,0,0,0.1);
}

.clear-link {
  font-size: 18px;
  color: #ef4444;
  font-family: 'Gochi Hand', cursive;
  text-decoration: none;
  border-bottom: 2px dashed #ef4444;
}
.clear-link:hover {
  border-bottom-style: solid;
}

/* Empty State */
.empty-wall {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
.empty-sketch {
  text-align: center;
  color: #94a3b8;
  font-size: 24px;
}
.sketch-btn {
  margin-top: 24px;
  font-family: 'Gochi Hand', cursive;
  font-size: 20px;
  padding: 10px 30px;
  border: 2px solid #2c3e50;
  border-radius: 255px 15px 225px 15px / 15px 225px 15px 255px;
  background: #fff;
  cursor: pointer;
  box-shadow: 4px 4px 0px #2c3e50;
  transition: all 0.2s;
}
.sketch-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0px #2c3e50;
}

/* Sticky Note Wall Grid */
.sticky-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 32px;
  padding: 20px;
}

/* Sticky Note Component */
.sticky-note {
  position: relative;
  padding: 24px;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 4px 8px rgba(0,0,0,0.15);
  transition: transform 0.3s ease;
  font-size: 18px;
  color: #334155;
}

.sticky-note:hover {
  transform: scale(1.05) rotate(0deg) !important; /* Suspend rotation on hover for readability */
  z-index: 10;
  box-shadow: 5px 10px 15px rgba(0,0,0,0.2);
}

/* Tape Strip */
.tape-strip {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%) rotate(-2deg);
  width: 100px;
  height: 30px;
  background-color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255,255,255,0.2);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  backdrop-filter: blur(2px); /* Frosted glass tape effect */
}

/* Note Colors */
.note-yellow { background: linear-gradient(135deg, #fef9c3 0%, #fef3c7 100%); }
.note-blue { background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%); }
.note-pink { background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%); }
.note-green { background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%); }

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.note-index {
  font-weight: 700;
  color: rgba(0,0,0,0.4);
  font-size: 20px;
}

/* Doodle Tag */
.doodle-tag {
  font-size: 14px;
  font-weight: 700;
  padding: 2px 8px;
  border: 2px solid currentColor;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
}
.doodle-tag.single-choice { color: #0284c7; }
.doodle-tag.multiple-choice { color: #d97706; }
.doodle-tag.judge { color: #16a34a; }

.question-text {
  flex: 1;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
}

.note-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px dashed rgba(0,0,0,0.1);
  padding-top: 12px;
}

.action-link {
  font-family: 'Gochi Hand', cursive;
  font-size: 16px;
  padding: 0 5px;
}
.action-link:hover {
  text-decoration: underline;
  text-decoration-style: wavy;
}
.action-link.remove { color: #ef4444; }
.action-link.retake { color: #4f46e5; }

.note-footer {
  margin-top: 8px;
  font-size: 14px;
  color: rgba(0,0,0,0.5);
}
.correct-val { font-weight: bold; color: #16a34a; }
</style>
