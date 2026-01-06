import request from './request'

/**
 * 获取操作日志
 */
export function getOperationLogs(params) {
    return request({
        url: '/admin/operation-logs',
        method: 'get',
        params
    })
}

/**
 * 删除操作日志
 */
export function deleteOperationLog(id) {
    return request({
        url: `/admin/operation-logs/${id}`,
        method: 'delete'
    })
}

/**
 * 获取系统统计
 */
export function getStatistics() {
    return request({
        url: '/admin/statistics',
        method: 'get'
    })
}
