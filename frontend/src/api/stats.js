import request from './request'

// 管理端全局概览统计
export const getAdminOverview = () => {
  return request({
    url: '/api/admin/stats/overview',
    method: 'get'
  })
}

// 管理端：最近一段时间（默认7天）预约按天统计，支持按科室过滤
export const getWeeklyAppointmentsStats = (params) => {
  return request({
    url: '/api/admin/stats/weekly-appointments',
    method: 'get',
    params
  })
}


