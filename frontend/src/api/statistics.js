import request from './request'

// 统计报表管理 API

/**
 * 获取全局概览统计
 */
export const getOverviewStats = () => {
  return request({
    url: '/api/admin/stats/overview',
    method: 'get'
  })
}

/**
 * 获取预约统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getAppointmentStats = (params) => {
  return request({
    url: '/api/admin/stats/appointments',
    method: 'get',
    params
  })
}

/**
 * 获取科室负荷统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getDepartmentWorkloadStats = (params) => {
  return request({
    url: '/api/admin/stats/departments/workload',
    method: 'get',
    params
  })
}

/**
 * 获取医生工作量统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 * @param {number} params.doctorId - 医生ID（可选）
 */
export const getDoctorWorkloadStats = (params) => {
  return request({
    url: '/api/admin/stats/doctors/workload',
    method: 'get',
    params
  })
}

/**
 * 获取收入统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getRevenueStats = (params) => {
  return request({
    url: '/api/admin/stats/revenue',
    method: 'get',
    params
  })
}

/**
 * 获取收入统计（按科室）
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getRevenueStatsByDepartment = (params) => {
  return request({
    url: '/api/admin/stats/revenue/departments',
    method: 'get',
    params
  })
}

/**
 * 获取号别分布统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getSlotTypeDistributionStats = (params) => {
  return request({
    url: '/api/admin/stats/slot-type-distribution',
    method: 'get',
    params
  })
}

/**
 * 获取时间段分布统计
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getTimeSlotDistributionStats = (params) => {
  return request({
    url: '/api/admin/stats/time-slot-distribution',
    method: 'get',
    params
  })
}

/**
 * 获取退号率统计（按科室）
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getCancellationRateByDepartment = (params) => {
  return request({
    url: '/api/admin/stats/cancellation-rate/departments',
    method: 'get',
    params
  })
}

/**
 * 获取退号率统计（按医生）
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 * @param {number} params.doctorId - 医生ID（可选）
 */
export const getCancellationRateByDoctor = (params) => {
  return request({
    url: '/api/admin/stats/cancellation-rate/doctors',
    method: 'get',
    params
  })
}

/**
 * 获取趋势分析
 * @param {Object} params - 查询参数
 * @param {string} params.startDate - 开始日期 yyyy-MM-dd
 * @param {string} params.endDate - 结束日期 yyyy-MM-dd
 */
export const getTrendStats = (params) => {
  return request({
    url: '/api/admin/stats/trends',
    method: 'get',
    params
  })
}

