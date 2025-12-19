import request from './request'

/**
 * 获取题目列表（分页）
 */
export function getQuestionList(params) {
  return request({
    url: '/questions',
    method: 'get',
    params
  })
}

/**
 * 获取题目详情
 */
export function getQuestionById(id) {
  return request({
    url: `/questions/${id}`,
    method: 'get'
  })
}

/**
 * 新增题目
 */
export function addQuestion(data) {
  return request({
    url: '/questions',
    method: 'post',
    data
  })
}

/**
 * 更新题目
 */
export function updateQuestion(id, data) {
  return request({
    url: `/questions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除题目
 */
export function deleteQuestion(id) {
  return request({
    url: `/questions/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除题目
 */
export function batchDeleteQuestions(ids) {
  return request({
    url: '/questions/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 清空题库
 */
export function clearAllQuestions(params) {
  return request({
    url: '/questions/clear',
    method: 'delete',
    params
  })
}

/**
 * 获取随机题目
 */
export function getRandomQuestion(params) {
  return request({
    url: '/questions/random',
    method: 'get',
    params
  })
}

/**
 * 导入 Excel
 */
export function importExcel(file, subject) {
  const formData = new FormData()
  formData.append('file', file)
  if (subject) {
    formData.append('subject', subject)
  }
  return request({
    url: '/import/excel',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出 Excel
 */
export function exportExcel() {
  return request({
    url: '/import/export',
    method: 'get',
    responseType: 'blob'
  })
}
