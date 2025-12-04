import request from './request'

// 审计日志管理 API

/**
 * 查询审计日志列表
 * @param {Object} params - 查询参数
 * @param {string} params.operationType - 操作类型（CREATE/UPDATE/DELETE/APPROVE/REJECT/QUERY 等）
 * @param {string} params.operationModule - 操作模块（SCHEDULE/APPOINTMENT/WAITLIST/AUDIT 等）
 * @param {string} params.username - 操作人用户名
 * @param {string} params.userRole - 操作人角色（patient/doctor/admin）
 * @param {string} params.status - 操作状态（SUCCESS/FAILURE）
 * @param {string} params.targetType - 目标对象类型
 * @param {number} params.targetId - 目标对象ID
 * @param {string} params.startTime - 开始时间 yyyy-MM-dd HH:mm:ss
 * @param {string} params.endTime - 结束时间 yyyy-MM-dd HH:mm:ss
 * @param {number} params.page - 页码，默认 1
 * @param {number} params.pageSize - 每页数量，默认 20，最大 200
 */
export const getAuditLogs = (params) => {
  return request({
    url: '/api/admin/audit-logs',
    method: 'get',
    params
  })
}

/**
 * 查询审计日志详情
 * @param {number} id - 日志ID
 */
export const getAuditLogById = (id) => {
  return request({
    url: `/api/admin/audit-logs/${id}`,
    method: 'get'
  })
}

