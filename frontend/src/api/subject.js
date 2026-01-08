import request from './request'

/**
 * 获取所有科目列表
 */
export const getAllSubjects = (ownerId) => {
  return request.get('/subjects', { params: { ownerId } })
}

/**
 * 重新统计所有科目的题目数量
 */
export const recountAllSubjects = () => {
  return request.post('/subjects/recount')
}

/**
 * 重新统计某个科目的题目数量
 */
export const recountSubject = (subjectName) => {
  return request.post(`/subjects/recount/${subjectName}`)
}
