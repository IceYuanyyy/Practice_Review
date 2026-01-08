<template>
  <div class="manage-container">
    <n-space vertical :size="24">
      <!-- 顶部操作栏 -->
      <n-card :bordered="false" class="action-card">
        <div class="action-header">
          <div class="left-actions">
            <n-button type="primary" size="medium" class="add-btn" @click="showAddModal = true">
              <template #icon>
                <n-icon :component="AddOutline" />
              </template>
              新增题目
            </n-button>
            <n-button-group>
              <n-button size="medium" @click="showImportModal = true">
                <template #icon>
                  <n-icon :component="CloudUploadOutline" />
                </template>
                导入
              </n-button>
              <n-button size="medium" @click="handleExport">
                <template #icon>
                  <n-icon :component="CloudDownloadOutline" />
                </template>
                导出
              </n-button>
            </n-button-group>
            
            <n-popconfirm
              @positive-click="handleBatchDelete"
              :disabled="checkedRowKeys.length === 0"
            >
              <template #trigger>
                <n-button
                  type="error"
                  ghost
                  size="medium"
                  :disabled="checkedRowKeys.length === 0"
                  class="batch-delete-btn"
                >
                  <template #icon>
                    <n-icon :component="TrashOutline" />
                  </template>
                  批量删除 {{ checkedRowKeys.length > 0 ? `(${checkedRowKeys.length})` : '' }}
                </n-button>
              </template>
              确定删除选中的 {{ checkedRowKeys.length }} 道题目吗？
            </n-popconfirm>
          </div>

          <div class="right-filters">
            <n-input
              v-model:value="filters.keyword"
              placeholder="搜索题目内容..."
              clearable
              size="medium"
              class="search-input"
              @keyup.enter="handleFilterChange"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" class="search-icon" />
              </template>
            </n-input>
            <n-select
              v-if="userOptions.length > 0"
              v-model:value="filters.ownerId"
              placeholder="导入用户"
              searchable
              clearable
              size="medium"
              class="filter-select"
              :options="userOptions"
              @update:value="handleFilterChange"
            />
            
            <n-select
              v-model:value="filters.subject"
              placeholder="全部科目"
              clearable
              size="medium"
              class="filter-select"
              :options="subjectOptions"
              @update:value="handleFilterChange"
            />
            <n-select
              v-model:value="filters.type"
              placeholder="所有题型"
              clearable
              size="medium"
              class="filter-select"
              :options="typeOptions"
              @update:value="handleFilterChange"
            />
            
            <n-button text type="error" @click="showClearModal = true" class="clear-all-btn">
              <template #icon><n-icon :component="TrashOutline"/></template>
              清空
            </n-button>
          </div>
        </div>
      </n-card>

      <!-- 题目列表 -->
      <n-card :bordered="false" class="table-card" content-style="padding: 0;">
        <n-data-table
          :columns="columns"
          :data="questions"
          :loading="loading"
          :pagination="paginationReactive"
          :remote="true"
          :bordered="false"
          :row-key="(row) => row.id"
          v-model:checked-row-keys="checkedRowKeys"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
          striped
          class="custom-table"
        >
          <template #empty>
            <div class="empty-state">
              <div class="empty-icon">📚</div>
              <div class="empty-title">暂无题库</div>
              <div class="empty-desc">请点击上方「导入」按钮导入题目</div>
              <n-button type="primary" size="medium" @click="showImportModal = true" style="margin-top: 16px;">
                <template #icon><n-icon :component="CloudUploadOutline" /></template>
                立即导入
              </n-button>
            </div>
          </template>
        </n-data-table>
      </n-card>
    </n-space>

    <!-- 清空题库对话框 -->
    <n-modal v-model:show="showClearModal" preset="dialog" title="清空题库" style="width: 500px">
      <n-space vertical :size="16">
        <n-alert type="warning" title="危险操作">
          此操作将永久删除题目，不可恢复，请谨慎选择！
        </n-alert>
        
        <n-form label-placement="left" label-width="80px" class="modal-form">
          <n-form-item label="清空范围">
            <n-radio-group v-model:value="clearOptions.mode">
              <n-space vertical>
                <n-radio value="all">清空全部题目</n-radio>
                <n-radio value="bySubject">按科目清空</n-radio>
                <n-radio value="byType">按题型清空</n-radio>
                <n-radio value="custom">自定义条件</n-radio>
              </n-space>
            </n-radio-group>
          </n-form-item>

          <n-form-item v-if="clearOptions.mode === 'bySubject' || clearOptions.mode === 'custom'" label="选择科目">
            <n-select
              v-model:value="clearOptions.subject"
              placeholder="请选择科目"
              clearable
              :options="subjectOptions"
            />
          </n-form-item>

          <n-form-item v-if="clearOptions.mode === 'byType' || clearOptions.mode === 'custom'" label="选择题型">
            <n-select
              v-model:value="clearOptions.type"
              placeholder="请选择题型"
              clearable
              :options="typeOptions"
            />
          </n-form-item>

          <n-form-item v-if="clearOptions.mode === 'custom'" label="选择用户">
            <n-select
              v-model:value="clearOptions.ownerId"
              placeholder="请选择用户"
              clearable
              :options="userOptions"
              :loading="loadingUsers"
            />
          </n-form-item>
        </n-form>

        <n-card v-if="clearPreview" size="small" :bordered="false" class="preview-card">
          <n-text type="error">
            <n-icon :component="TrashOutline" style="margin-right: 4px; vertical-align: bottom;" />
            {{ clearPreview }}
          </n-text>
        </n-card>
      </n-space>

      <template #action>
        <n-space justify="end">
          <n-button @click="showClearModal = false">取消</n-button>
          <n-button type="error" @click="confirmClear">确认清空</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 新增/编辑对话框 -->
    <n-modal v-model:show="showAddModal" preset="card" :title="editingQuestion ? '编辑题目' : '新增题目'" style="width: 750px" class="custom-modal">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="80px" size="large">
        
        <div class="form-section">
          <div class="section-header">
            <span class="section-title">基本信息</span>
            <n-divider style="margin: 0 0 16px 0" />
          </div>
          <n-grid :cols="3" :x-gap="20">
             <n-grid-item>
                <n-form-item label="科目" path="subject">
                  <n-select 
                    v-model:value="formData.subject" 
                    :options="subjectOptions.filter(opt => opt.value).map(opt => ({ label: opt.value, value: opt.value }))"
                    placeholder="选择或输入科目"
                    filterable
                    tag
                  />
                </n-form-item>
             </n-grid-item>
             <n-grid-item>
                <n-form-item label="题型" path="type">
                  <n-select v-model:value="formData.type" :options="typeOptions" placeholder="选择题型" />
                </n-form-item>
             </n-grid-item>
             <n-grid-item>
              <n-form-item label="难度" path="difficulty">
                <n-select v-model:value="formData.difficulty" :options="difficultyOptions" />
              </n-form-item>
            </n-grid-item>
          </n-grid>
        </div>

        <div class="form-section">
          <div class="section-header">
            <span class="section-title">题目详情</span>
            <n-divider style="margin: 0 0 16px 0" />
          </div>
          
          <n-form-item label="题目" path="content" label-placement="top">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入题目内容"
              :rows="3"
            />
          </n-form-item>

          <div v-if="['single-choice', 'multiple-choice', 'choice'].includes(formData.type)" class="options-container">
            <n-grid :cols="2" :x-gap="24" :y-gap="12">
              <n-grid-item>
                <n-form-item label="A" path="optionA" label-width="30px"><n-input v-model:value="formData.optionA" placeholder="选项A" /></n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="B" path="optionB" label-width="30px"><n-input v-model:value="formData.optionB" placeholder="选项B" /></n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="C" path="optionC" label-width="30px"><n-input v-model:value="formData.optionC" placeholder="选项C" /></n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="D" path="optionD" label-width="30px"><n-input v-model:value="formData.optionD" placeholder="选项D" /></n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="E" path="optionE" label-width="30px"><n-input v-model:value="formData.optionE" placeholder="选项E（可选）" /></n-form-item>
              </n-grid-item>
              <n-grid-item>
                <n-form-item label="F" path="optionF" label-width="30px"><n-input v-model:value="formData.optionF" placeholder="选项F（可选）" /></n-form-item>
              </n-grid-item>
            </n-grid>
          </div>
        </div>

        <div class="form-section" style="margin-top: 12px;">
           <div class="section-header">
            <span class="section-title">答案与解析</span>
             <n-divider style="margin: 0 0 16px 0" />
          </div>
          <n-grid :cols="2" :x-gap="24">
            <n-grid-item>
               <n-form-item label="答案" path="answer">
                  <n-select v-if="formData.type === 'judge'" v-model:value="formData.answer" :options="[{label:'正确', value:'正确'}, {label:'错误', value:'错误'}]" placeholder="请选择" />
                  <n-select v-else-if="['single-choice', 'choice'].includes(formData.type)" v-model:value="formData.answer" :options="['A','B','C','D','E','F'].map(v=>({label: v, value: v}))" placeholder="请选择" />
                  <n-input v-else v-model:value="formData.answer" placeholder="多选请输入如 ABCD、ABCDEF" />
              </n-form-item>
            </n-grid-item>
          </n-grid>

          <n-form-item label="解析" path="analysis">
            <n-input
              v-model:value="formData.analysis"
              type="textarea"
              placeholder="输入答案解析..."
              :rows="2"
            />
          </n-form-item>
        </div>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showAddModal = false" size="large">取消</n-button>
          <n-button type="primary" @click="handleSubmit" size="large">保存题目</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 导入对话框 -->
    <n-modal v-model:show="showImportModal" preset="dialog" title="导入Excel题库" style="width: 500px">
      <div class="import-container">
        <n-alert type="info" :show-icon="false" class="import-alert">
          <p>请选择Excel文件并指定科目名称，所有导入的题目将归类到指定科目下。</p>
        </n-alert>
        
        <n-form-item label="归属科目" class="mt-4">
          <n-input 
            v-model:value="importSubject" 
            placeholder="归属科目 (留空则使用文件原有科目)"
            size="large"
          />
        </n-form-item>

        <n-upload
          :show-file-list="true"
          :max="1"
          accept=".xlsx,.xls,.csv"
          :custom-request="handleImportFile"
          @before-upload="beforeUpload"
          class="import-upload"
        >
          <n-upload-dragger>
            <div style="margin-bottom: 12px">
              <n-icon size="48" :depth="3" :component="CloudUploadOutline" />
            </div>
            <n-text style="font-size: 16px">
              点击或拖拽文件到此处
            </n-text>
          </n-upload-dragger>
        </n-upload>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, reactive, computed } from 'vue'
import {
  NSpace, NCard, NButton, NIcon, NUpload, NSelect, NInput, NDataTable,
  NModal, NForm, NFormItem, NTag, NPopconfirm, NRadioGroup, NRadio, NAlert, NText, useMessage,
  NButtonGroup, NGrid, NGridItem, NUploadDragger, NDivider
} from 'naive-ui'
import {
  AddOutline, CloudUploadOutline, CloudDownloadOutline, SearchOutline,
  CreateOutline, TrashOutline
} from '@vicons/ionicons5'
import { getQuestionList, addQuestion, updateQuestion, deleteQuestion, importExcel, exportExcel, batchDeleteQuestions, clearAllQuestions } from '@/api/question'
import { getAllSubjects } from '@/api/subject'
import { getAllUsers } from '@/api/user'

const message = useMessage()
const loading = ref(false)
const showAddModal = ref(false)
const showClearModal = ref(false)
const showImportModal = ref(false)
const importSubject = ref('')
const editingQuestion = ref(null)
const formRef = ref(null)
const checkedRowKeys = ref([])
const loadingUsers = ref(false)
const userOptions = ref([])

// 清空选项
const clearOptions = reactive({
  mode: 'all',
  subject: null,
  type: null,
  ownerId: null
})

const clearPreview = computed(() => {
  if (clearOptions.mode === 'all') {
    return '将清空全部题目'
  } else if (clearOptions.mode === 'bySubject' && clearOptions.subject) {
    return `将清空科目为"${clearOptions.subject}"的所有题目`
  } else if (clearOptions.mode === 'byType' && clearOptions.type) {
    const typeNameMap = {
      'single-choice': '单选题',
      'multiple-choice': '多选题',
      'choice': '选择题',
      'judge': '判断题'
    }
    const typeName = typeNameMap[clearOptions.type] || clearOptions.type
    return `将清空所有${typeName}`
  } else if (clearOptions.mode === 'custom') {
    const parts = []
    if (clearOptions.subject) parts.push(`科目:"${clearOptions.subject}"`)
    if (clearOptions.type) {
      const typeNameMap = {
        'single-choice': '单选题',
        'multiple-choice': '多选题',
        'choice': '选择题',
        'judge': '判断题'
      }
      const typeName = typeNameMap[clearOptions.type] || clearOptions.type
      parts.push(`题型:${typeName}`)
    }
    if (clearOptions.ownerId !== null && clearOptions.ownerId !== undefined) {
      const user = userOptions.value.find(u => u.value === clearOptions.ownerId)
      if (user) {
        parts.push(`用户:${user.label}`)
      }
    }
    return parts.length > 0 ? `将清空 ${parts.join('，')} 的题目` : '请选择清空条件'
  }
  return ''
})

// 筛选条件
const filters = reactive({
  subject: null,
  type: null,
  keyword: '',
  ownerId: null
})

// 分页状态
const currentPage = ref(1)
const currentPageSize = ref(11)
const totalCount = ref(0)

// 分页配置 - 使用 computed 保持响应性
const paginationReactive = computed(() => ({
  page: currentPage.value,
  pageSize: currentPageSize.value,
  itemCount: totalCount.value,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  showQuickJumper: true
}))

// 分页改变处理
const handlePageChange = (page) => {
  currentPage.value = page
  loadQuestions()
}

const handlePageSizeChange = (pageSize) => {
  currentPageSize.value = pageSize
  currentPage.value = 1
  loadQuestions()
}

// 筛选条件改变处理
const handleFilterChange = () => {
  currentPage.value = 1  // 重置到第一页
  loadQuestions()
}

// 题目列表
const questions = ref([])

// 表单数据
const formData = reactive({
  type: 'single-choice',
  subject: '',
  content: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  optionE: '',
  optionF: '',
  answer: '',
  analysis: '',
  difficulty: 'medium'
})

// 表单验证规则
const rules = {
  type: { required: true, message: '请选择题型', trigger: 'change' },
  subject: { required: true, message: '请输入科目', trigger: 'blur' },
  content: { required: true, message: '请输入题目内容', trigger: 'blur' },
  answer: { required: true, message: '请输入答案', trigger: 'blur' }
}

// 选项
const subjectOptions = ref([])

// 加载科目列表
const loadSubjects = async () => {
  try {
    const res = await getAllSubjects()
    if (res.data) {
      const options = res.data.map(subject => {
        // 构建显示标签：科目名 (题数) - 用户名
        let label = `${subject.name} (${subject.questionCount})`
        if (subject.ownerName) {
          label += ` - ${subject.ownerName}`
        }
        return {
          label: label,
          value: subject.name
        }
      })
      
      // 添加"全部科目"选项
      subjectOptions.value = [
        { label: '全部科目', value: null },
        ...options
      ]
      
      // 如果后端有未分类的数据，但没在列表中，这里可以保留逻辑，但目前先只展示全部+实际科目
    }
  } catch (error) {
    console.error('加载科目列表失败', error)
    // 静默处理，不显示错误提示
  }
}

// 加载用户列表（仅管理员）
const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await getAllUsers()
    if (res.code === 200 && res.data) {
      userOptions.value = [
        { label: '公共题库', value: -1 },
        ...res.data.map(user => ({
          label: `${user.username} (${user.nickname || '无昵称'})`,
          value: user.id
        }))
      ]
    }
  } catch (error) {
    // 非管理员会失败，静默处理，不输出任何错误
  } finally {
    loadingUsers.value = false
  }
}

const typeOptions = [
  { label: '单选题', value: 'single-choice' },
  { label: '多选题', value: 'multiple-choice' },
  { label: '判断题', value: 'judge' }
]

const difficultyOptions = [
  { label: '简单', value: 'easy' },
  { label: '中等', value: 'medium' },
  { label: '困难', value: 'hard' }
]

// 表格列定义
const columns = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 70, align: 'center' },
  {
    title: '序号',
    key: 'index',
    width: 70,
    align: 'center',
    render: (_row, index) => (currentPage.value - 1) * currentPageSize.value + index + 1
  },
  {
    title: '题型',
    key: 'type',
    width: 100,
    align: 'center',
    render: (row) => {
      const typeMap = {
        'single-choice': { type: 'info', text: '单选题' },
        'multiple-choice': { type: 'warning', text: '多选题' },
        'choice': { type: 'info', text: '选择题' },
        'judge': { type: 'success', text: '判断题' }
      }
      const config = typeMap[row.type] || { type: 'default', text: row.type }
      return h(NTag, 
        { type: config.type, size: 'small', round: true, bordered: false },
        { default: () => config.text }
      )
    }
  },
  { 
    title: '科目', 
    key: 'subject', 
    width: 120,
    render: (row) => h(NTag, { size: 'small', bordered: true, style: { color: '#666' } }, { default: () => row.subject }) 
  },
  {
    title: '导入者',
    key: 'ownerName',
    width: 100,
    align: 'center',
    render: (row) => h(NTag, { type: 'info', size: 'small', bordered: false }, { default: () => row.ownerName || '未知' })
  },
  {
    title: '导入时间',
    key: 'createTime',
    width: 150,
    align: 'center',
    render: (row) => {
        if (!row.createTime) return '-'
        const dateStr = row.createTime.replace('T', ' ')
        // 使用更优雅的展示方式
        return h('div', { 
          style: { 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center',
            gap: '2px'
          } 
        }, [
          h(NTag, { 
            bordered: false, 
            type: 'primary', 
            size: 'small', 
            style: { 
              fontSize: '12px', 
              fontWeight: '500',
              opacity: 0.9
            } 
          }, { default: () => dateStr.substring(0, 10) }), // 日期
          h('span', { 
            style: { 
              fontSize: '12px', 
              color: '#9ca3af',
              fontFamily: 'Monaco, monospace'
            } 
          }, dateStr.substring(11, 16)) // 时间 HH:mm
        ])
    }
  },
  { 
     title: '题目内容', 
     key: 'content', 
     ellipsis: { tooltip: true },
     render: (row) => h('span', { style: { fontWeight: '500' } }, row.content)
  },
  { 
     title: '答案', 
     key: 'answer', 
     width: 80,
     align: 'center',
     render: (row) => h(NText, { type: 'success', strong: true }, { default: () => row.answer }) 
  },
  {
    title: '难度',
    key: 'difficulty',
    width: 80,
    align: 'center',
    render: (row) => {
      const typeMap = {
        easy: { color: '#10b981', text: '简单' },
        medium: { color: '#f59e0b', text: '中等' },
        hard: { color: '#ef4444', text: '困难' }
      }
      const config = typeMap[row.difficulty] || typeMap.medium
      return h('span', { style: { color: config.color, fontSize: '13px' } }, config.text)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    align: 'center',
    render: (row) => {
      return h(NSpace, { justify: 'center' }, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              quaternary: true,
              type: 'primary',
              onClick: () => handleEdit(row)
            },
            { default: () => '编辑' }
          ),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.id)
            },
            {
              default: () => '确定删除该题目吗？',
              trigger: () =>
                h(
                  NButton,
                  {
                    size: 'small',
                    quaternary: true,
                    type: 'error'
                  },
                  { default: () => '删除' }
                )
            }
          )
        ]
      })
    }
  }
]

// 加载题目列表
const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: currentPageSize.value
    }
    // 只添加非空的筛选条件
    if (filters.subject) params.subject = filters.subject
    if (filters.type) params.type = filters.type
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.ownerId) params.ownerId = filters.ownerId
    
    //console.log('loadQuestions params:', params)
    
    const res = await getQuestionList(params)
    questions.value = res.data.records
    totalCount.value = res.data.total
    
  } catch (error) {
    console.error('加载题目失败', error)
    message.error('加载题目失败')
  } finally {
    loading.value = false
  }
}

// 编辑题目
const handleEdit = (row) => {
  // 先重置表单，确保所有字段都是干净的
  resetForm()
  
  editingQuestion.value = row
  
  // 复制基本字段
  formData.type = row.type || 'single-choice'
  formData.subject = row.subject || ''
  formData.content = row.content || ''
  formData.answer = row.answer || ''
  formData.analysis = row.analysis || ''
  formData.difficulty = row.difficulty || 'medium'
  
  // 解析选项
  if ((row.type === 'single-choice' || row.type === 'multiple-choice' || row.type === 'choice') && row.options) {
    try {
      // 后端返回的 options 已经是数组了
      const options = Array.isArray(row.options) 
        ? row.options 
        : JSON.parse(row.options)
      
      options.forEach(opt => {
        // 支持 "A:内容" 或 "A: 内容" 格式
        const colonIndex = opt.indexOf(':')
        if (colonIndex !== -1) {
          const key = opt.substring(0, colonIndex).trim().toUpperCase()
          const value = opt.substring(colonIndex + 1).trim()
          if (['A', 'B', 'C', 'D', 'E', 'F'].includes(key)) {
            formData[`option${key}`] = value
          }
        }
      })
    } catch (e) {
      console.error('解析选项失败', e)
    }
  }
  
  showAddModal.value = true
}

// 删除题目
const handleDelete = async (id) => {
  try {
    await deleteQuestion(id)
    message.success('删除成功')
    loadQuestions()
  } catch (error) {
    message.error('删除失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    const data = { ...formData }
    
    // 处理选项
    if (data.type === 'single-choice' || data.type === 'multiple-choice' || data.type === 'choice') {
      const options = []
      if (data.optionA) options.push(`A:${data.optionA}`)
      if (data.optionB) options.push(`B:${data.optionB}`)
      if (data.optionC) options.push(`C:${data.optionC}`)
      if (data.optionD) options.push(`D:${data.optionD}`)
      if (data.optionE) options.push(`E:${data.optionE}`)
      if (data.optionF) options.push(`F:${data.optionF}`)
      // 后端期望的是数组，不是 JSON 字符串
      data.options = options
    }
    
    // 删除单独的选项字段
    delete data.optionA
    delete data.optionB
    delete data.optionC
    delete data.optionD
    delete data.optionE
    delete data.optionF
    
    if (editingQuestion.value) {
      await updateQuestion(editingQuestion.value.id, data)
      message.success('更新成功')
    } else {
      await addQuestion(data)
      message.success('新增成功')
    }
    
    showAddModal.value = false
    resetForm()
    loadQuestions()
  } catch (error) {
    if (error?.errors) return
    message.error('操作失败')
  }
}

// 重置表单
const resetForm = () => {
  editingQuestion.value = null
  Object.assign(formData, {
    type: 'single-choice',
    subject: '',
    content: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    optionE: '',
    optionF: '',
    answer: '',
    analysis: '',
    difficulty: 'medium'
  })
}

// 导入前验证
const beforeUpload = ({ file }) => {
  if (!importSubject.value || importSubject.value.trim() === '') {
    message.error('请先输入科目名称')
    return false
  }
  return true
}

// 导入Excel
const handleImportFile = async ({ file }) => {
  if (!importSubject.value || importSubject.value.trim() === '') {
    message.error('请先输入科目名称')
    return
  }

  try {
    const res = await importExcel(file.file, importSubject.value)
    message.success(`导入成功！总计：${res.data.total}，成功：${res.data.success}，失败：${res.data.fail}`)
    showImportModal.value = false
    importSubject.value = ''
    // 重新加载科目列表和题目列表
    await loadSubjects()
    loadQuestions()
  } catch (error) {
    message.error('导入失败')
  }
}

// 导出Excel
const handleExport = async () => {
  try {
    const res = await exportExcel()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `题目列表_${new Date().getTime()}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (checkedRowKeys.value.length === 0) {
    message.warning('请先选择要删除的题目')
    return
  }
  try {
    await batchDeleteQuestions(checkedRowKeys.value)
    message.success(`成功删除 ${checkedRowKeys.value.length} 道题目`)
    checkedRowKeys.value = []
    loadQuestions()
  } catch (error) {
    message.error('批量删除失败')
  }
}

// 确认清空题库
const confirmClear = async () => {
  try {
    const params = {}
    
    // 根据模式设置参数
    if (clearOptions.mode === 'bySubject' || clearOptions.mode === 'custom') {
      if (clearOptions.subject) {
        params.subject = clearOptions.subject
      }
    }
    if (clearOptions.mode === 'byType' || clearOptions.mode === 'custom') {
      if (clearOptions.type) {
        params.type = clearOptions.type
      }
    }
    if (clearOptions.mode === 'custom' && clearOptions.ownerId !== null && clearOptions.ownerId !== undefined) {
      params.ownerId = clearOptions.ownerId
    }
    
    const res = await clearAllQuestions(params)
    message.success(res.data)
    showClearModal.value = false
    
    // 重置清空选项
    clearOptions.mode = 'all'
    clearOptions.subject = null
    clearOptions.type = null
    clearOptions.ownerId = null
    
    checkedRowKeys.value = []
    loadQuestions()
  } catch (error) {
    message.error('清空题库失败')
  }
}

onMounted(() => {
  loadSubjects()
  loadUsers()
  loadQuestions()
})
</script>

<style scoped>
.manage-container {
  max-width: 95%;
  margin: 0 auto;
  padding-bottom: 24px;
}

.filter-select {
  width: 200px;
}

/* Action Card - Floating Toolbar */
.action-card {
  position: sticky;
  top: 24px;
  z-index: 100;
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-full);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  padding: 4px;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding: 4px 8px;
}

.left-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.add-btn {
  border-radius: 20px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.right-filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 240px;
}

/* Rounded Inputs & Glow Effect */
:deep(.n-input), :deep(.n-base-selection) {
  border-radius: 20px !important;
  background-color: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid rgba(0, 0, 0, 0.05) !important;
  transition: all 0.3s;
}

:deep(.n-input:hover), :deep(.n-base-selection:hover) {
  background-color: #fff !important;
  border-color: var(--color-primary) !important;
}

:deep(.n-input.n-input--focus), :deep(.n-base-selection.n-base-selection--active) {
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1) !important;
  background-color: #fff !important;
  border-color: var(--color-primary) !important;
}

/* Table Card */
.table-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

/* Custom Table Styles */
.custom-table {
  --n-th-font-weight: 700;
  --n-td-padding: 16px;
  --n-border-radius: 12px;
}

:deep(.n-data-table .n-data-table-th) {
  background-color: rgba(248, 250, 252, 0.5) !important;
  color: var(--color-n-600);
  border-bottom: 1px solid rgba(0,0,0,0.05) !important;
  border-right: none !important; /* Remove vertical borders */
}

:deep(.n-data-table .n-data-table-td) {
  background-color: transparent;
  border-bottom: 1px solid rgba(0,0,0,0.03) !important;
  border-right: none !important; /* Remove vertical borders */
  transition: all 0.2s;
  color: var(--color-n-700);
}

/* Hover Row Effect */
:deep(.n-data-table .n-data-table-tr:hover .n-data-table-td) {
  background-color: rgba(16, 185, 129, 0.03) !important; 
}

/* Tags */
:deep(.n-tag) {
  font-weight: 600;
}

/* Modals */
.import-container {
  text-align: center;
  padding: 20px 0;
}

.import-upload {
  margin-top: 24px;
}

.import-alert {
  text-align: left;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-primary); 
  /* Use theme color */
  white-space: nowrap;
}

.form-section {
  margin-bottom: 8px;
}

.options-container {
  background: rgba(249, 250, 251, 0.6);
  padding: 16px;
  border-radius: 8px;
  border: 1px dashed rgba(0,0,0,0.1);
  margin-bottom: 16px;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #94a3b8;
}
</style>
