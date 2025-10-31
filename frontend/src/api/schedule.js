import request from './request'

// 排班管理API

/**
 * 查询排班列表
 * @param {Object} params - 查询参数
 * @param {number} params.doctorId - 医生ID
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {string} params.slotType - 号别类型
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页大小
 */
export const getScheduleList = (params) => {
  return request({
    url: '/api/admin/schedules',
    method: 'get',
    params
  })
}

/**
 * 根据ID查询排班详情
 * @param {number} id - 排班ID
 */
export const getScheduleById = (id) => {
  return request({
    url: `/api/admin/schedules/${id}`,
    method: 'get'
  })
}

/**
 * 创建单个排班
 * @param {Object} data - 排班数据
 * @param {number} data.doctorId - 医生ID
 * @param {string} data.scheduleDate - 排班日期
 * @param {string} data.timeSlot - 时间段
 * @param {string} data.slotType - 号别类型
 * @param {number} data.totalSlots - 总号源数
 * @param {number} data.availableSlots - 可用号源数
 */
export const createSchedule = (data) => {
  return request({
    url: '/api/admin/schedules',
    method: 'post',
    data
  })
}

/**
 * 批量创建排班
 * @param {Object} data - 批量排班数据
 * @param {number} data.doctorId - 医生ID
 * @param {string} data.startDate - 开始日期
 * @param {string} data.endDate - 结束日期
 * @param {Array} data.timeSlots - 时间段列表
 * @param {string} data.slotType - 号别类型
 * @param {number} data.totalSlots - 总号源数
 * @param {boolean} data.skipWeekends - 是否跳过周末
 * @param {Array} data.excludeDates - 排除日期列表
 */
export const batchCreateSchedule = (data) => {
  return request({
    url: '/api/admin/schedules/batch',
    method: 'post',
    data
  })
}

/**
 * 更新排班
 * @param {number} id - 排班ID
 * @param {Object} data - 更新数据
 */
export const updateSchedule = (id, data) => {
  return request({
    url: `/api/admin/schedules/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除排班
 * @param {number} id - 排班ID
 */
export const deleteSchedule = (id) => {
  return request({
    url: `/api/admin/schedules/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除排班
 * @param {Array} ids - 排班ID列表
 */
export const batchDeleteSchedule = (ids) => {
  return request({
    url: '/api/admin/schedules/batch',
    method: 'delete',
    data: { ids }
  })
}