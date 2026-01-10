import request from './request'

/**
 * 提交答题记录
 */
export function submitAnswer(data) {
  return request({
    url: '/practice/submit',
    method: 'post',
    data
  })
}

/**
 * 获取错题列表
 */
export function getWrongQuestions(params) {
  return request({
    url: '/practice/wrong',
    method: 'get',
    params
  })
}

/**
 * 获取统计数据
 */
export function getStatistics() {
  return request({
    url: '/practice/statistics',
    method: 'get'
  })
}

/**
 * 清空错题本（支持按科目筛选）
 * @param {string} subject - 可选，科目名称，不传则清空全部
 */
export function clearWrongBook(subject) {
  return request({
    url: '/practice/wrong',
    method: 'delete',
    params: subject ? { subject } : {}
  })
}

// ==================== 轮次刷题 API ====================

/**
 * 开始或继续轮次练习
 */
export function startRound(subject, ownerId) {
  return request({
    url: '/practice/round/start',
    method: 'post',
    params: { subject, ownerId }
  })
}

/**
 * 获取轮次下一题
 */
export function nextRoundQuestion(subject) {
  return request({
    url: '/practice/round/next',
    method: 'get',
    params: { subject }
  })
}

/**
 * 获取轮次上一题
 */
export function prevRoundQuestion(subject) {
  return request({
    url: '/practice/round/prev',
    method: 'get',
    params: { subject }
  })
}

/**
 * 获取轮次进度
 */
export function getRoundProgress(subject) {
  return request({
    url: '/practice/round/progress',
    method: 'get',
    params: { subject }
  })
}

/**
 * 重置轮次（开始新一轮）
 */
export function resetRound(subject, ownerId) {
  return request({
    url: '/practice/round/reset',
    method: 'post',
    params: { subject, ownerId }
  })
}

/**
 * 跳转到指定索引的题目
 */
export function jumpRoundQuestion(subject, index) {
  return request({
    url: '/practice/round/jump',
    method: 'get',
    params: { subject, index }
  })
}

/**
 * 获取当前轮次所有题目的答题状态
 */
export function getRoundResults(subject) {
  return request({
    url: '/practice/round/results',
    method: 'get',
    params: { subject }
  })
}

// ==================== 搜索 API ====================

/**
 * 搜索题目
 */
export function searchQuestions(params) {
  return request({
    url: '/practice/search',
    method: 'get',
    params
  })
}

// ==================== 错题本增强 API ====================

/**
 * 获取错题本分页列表
 */
export function getWrongBookPage(params) {
  return request({
    url: '/practice/wrong-book/list',
    method: 'get',
    params
  })
}

/**
 * 标记错题已掌握
 */
export function markMastered(questionId) {
  return request({
    url: `/practice/wrong-book/master/${questionId}`,
    method: 'post'
  })
}

/**
 * 获取错题本按科目的统计信息
 */
export function getWrongBookSubjects() {
  return request({
    url: '/practice/wrong-book/subjects',
    method: 'get'
  })
}

/**
 * 开始错题专项练习
 */
export function startWrongBookPractice(subject) {
  return request({
    url: '/practice/wrong-book/practice',
    method: 'post',
    params: { subject }
  })
}

/**
 * 获取错题练习的下一题
 */
export function nextWrongQuestion(params) {
  return request({
    url: '/practice/wrong-book/practice/next',
    method: 'get',
    params
  })
}

// ==================== 练习记录查询 API ====================

/**
 * 获取练习记录列表（分页）
 * @param {Object} params - { page, size, userId? }
 */
export function getPracticeRecords(params) {
  return request({
    url: '/practice/records',
    method: 'get',
    params
  })
}

