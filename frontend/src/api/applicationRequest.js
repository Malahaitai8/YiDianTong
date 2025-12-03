import request from './request'

// 创建申请（调班/信息修改）
export const createApplicationRequest = (data) => {
  return request({
    url: '/api/application-requests',
    method: 'post',
    data
  })
}

// 管理员查询全部申请
export const getAllApplicationRequests = (params) => {
  return request({
    url: '/api/application-requests',
    method: 'get',
    params
  })
}

// 医生/管理员查询我的申请
export const getMyApplicationRequests = (params) => {
  return request({
    url: '/api/application-requests/my',
    method: 'get',
    params
  })
}

// 根据ID查询申请
export const getApplicationRequestById = (id) => {
  return request({
    url: `/api/application-requests/${id}`,
    method: 'get'
  })
}

// 查询申请完整详情（包含关联信息）
export const getApplicationRequestFullDetail = (id) => {
  return request({
    url: `/api/application-requests/detail/${id}`,
    method: 'get'
  })
}

// 管理员根据状态筛选申请
export const getApplicationRequestsByStatus = (status) => {
  return request({
    url: `/api/application-requests/status/${status}`,
    method: 'get'
  })
}

// 查询待审核申请
export const getPendingApplicationRequests = () => {
  return request({
    url: '/api/application-requests/pending',
    method: 'get'
  })
}

// 审核申请
export const reviewApplicationRequest = (data) => {
  return request({
    url: '/api/application-requests/review',
    method: 'post',
    data
  })
}

// 取消申请
export const cancelApplicationRequest = (requestId) => {
  return request({
    url: `/api/application-requests/${requestId}/cancel`,
    method: 'post'
  })
}

// 删除申请
export const deleteApplicationRequest = (requestId) => {
  return request({
    url: `/api/application-requests/${requestId}`,
    method: 'delete'
  })
}

// 获取申请统计
export const getApplicationRequestStatistics = () => {
  return request({
    url: '/api/application-requests/statistics',
    method: 'get'
  })
}

