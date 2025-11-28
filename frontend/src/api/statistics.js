import request from './request'

// 统计报表管理 API

// 获取全局概览统计
export const getOverviewStats = () => {
  return request({
    url: '/api/admin/stats/overview',
    method: 'get'
  })
}

