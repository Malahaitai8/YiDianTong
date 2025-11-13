import request from './request'

// 排班规则管理 API

// 查询所有排班规则
export const listScheduleRules = (params) => {
  return request({
    url: '/api/admin/schedule-rules',
    method: 'get',
    params
  })
}

// 查询规则详情
export const getScheduleRuleById = (ruleId) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}`,
    method: 'get'
  })
}

export const getScheduleRuleDetail = (ruleId) => {
  return request({
    url: `/api/admin/schedule-rules/detail/${ruleId}`,
    method: 'get'
  })
}

// 创建排班规则
export const createScheduleRule = (data) => {
  return request({
    url: '/api/admin/schedule-rules',
    method: 'post',
    data
  })
}

// 更新排班规则
export const updateScheduleRule = (ruleId, data) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}`,
    method: 'put',
    data
  })
}

// 删除排班规则
export const deleteScheduleRule = (ruleId) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}`,
    method: 'delete'
  })
}

// 启用排班规则
export const enableScheduleRule = (ruleId) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}/enable`,
    method: 'post'
  })
}

// 禁用排班规则
export const disableScheduleRule = (ruleId) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}/disable`,
    method: 'post'
  })
}

// 应用规则生成排班
export const applyScheduleRule = (ruleId, data) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}/apply`,
    method: 'post',
    data
  })
}

// 检测规则冲突
export const detectScheduleRuleConflicts = (ruleId) => {
  return request({
    url: `/api/admin/schedule-rules/${ruleId}/conflicts`,
    method: 'get'
  })
}

// 按状态查询规则
export const listScheduleRulesByStatus = (status) => {
  return request({
    url: `/api/admin/schedule-rules/status/${status}`,
    method: 'get'
  })
}

// 查询医生的排班规则
export const listScheduleRulesByDoctor = (doctorId) => {
  return request({
    url: `/api/admin/schedule-rules/doctor/${doctorId}`,
    method: 'get'
  })
}
