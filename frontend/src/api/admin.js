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
 * 保存题库转换日志
 */
export function saveConvertLog(data) {
    return request({
        url: '/import/convert-log',
        method: 'post',
        data
    })
}

/**
 * 下载转换日志的原始文件
 */
export function downloadConvertSourceFile(logId) {
    return `/api/import/convert-log/${logId}/source`
}

/**
 * 下载转换日志的结果文件
 */
export function downloadConvertResultFile(logId) {
    return `/api/import/convert-log/${logId}/result`
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
