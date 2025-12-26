import request from './request'

// 批量查询候补人数
export const getWaitlistCounts = (scheduleIds) => {
  const params = new URLSearchParams()
  scheduleIds.forEach(id => params.append('scheduleIds', id))

  return request({
    url: `/api/admin/schedules/waitlist-count?${params.toString()}`,
    method: 'get'
  })
}

// 弹出下一个候补患者（管理员手动操作）
export const popNextWaitlist = (scheduleId) => {
  return request({
    url: `/waitlist/next/${scheduleId}`,
    method: 'post'
  })
}

// 加入候补队列
export const addToWaitlist = (data) => {
  return request({
    url: '/waitlist',
    method: 'post',
    data
  })
}

// 查看我的候补
export const getMyWaitlist = () => {
  return request({
    url: '/waitlist/me',
    method: 'get'
  })
}

// 退出候补队列
export const cancelWaitlist = (scheduleId) => {
  return request({
    url: `/waitlist/${scheduleId}`,
    method: 'delete'
  })
}