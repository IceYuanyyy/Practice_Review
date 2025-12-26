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
 * 清空错题本
 */
export function clearWrongBook() {
  return request({
    url: '/practice/wrong',
    method: 'delete'
  })
}

