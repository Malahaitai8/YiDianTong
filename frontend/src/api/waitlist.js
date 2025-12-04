import request from './request'

/**
 * 管理员弹出指定排班的候补队首
 * @param {number|string} scheduleId
 */
export const popNextWaitlist = (scheduleId) => {
  return request({
    url: `/waitlist/next/${scheduleId}`,
    method: 'post'
  })
}

/**
 * 批量查询多个排班的候补人数
 * @param {Array<number|string>} scheduleIds - 排班ID列表
 * @returns {Promise} 返回格式: { "574": 3, "588": 0, ... }
 */
export const getWaitlistCounts = (scheduleIds = []) => {
  const validIds = (Array.isArray(scheduleIds) ? scheduleIds : [])
    .map((id) => Number(id))
    .filter((id) => !Number.isNaN(id))

  if (validIds.length === 0) {
    return Promise.reject(new Error('scheduleIds 不能为空'))
  }

  return request({
    url: '/waitlist/count',
    method: 'post',
    data: { scheduleIds: validIds }
  })
}

