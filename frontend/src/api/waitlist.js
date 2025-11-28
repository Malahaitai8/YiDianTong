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
 * 查询多个排班的候补人数
 * @param {Array<number|string>} scheduleIds
 */
export const getWaitlistCounts = (scheduleIds = []) => {
  const validIds = (Array.isArray(scheduleIds) ? scheduleIds : [])
    .map((id) => Number(id))
    .filter((id) => !Number.isNaN(id))

  return request({
    url: '/waitlist/count',
    method: 'get',
    params: { scheduleIds: validIds },
    paramsSerializer: (params) => {
      const searchParams = new URLSearchParams()
      ;(params.scheduleIds || []).forEach((id) => {
        searchParams.append('scheduleIds', id)
      })
      return searchParams.toString()
    }
  })
}

