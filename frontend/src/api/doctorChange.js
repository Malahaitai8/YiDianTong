import request from './request'

// 按医生ID查询变更申请
export const getDoctorChangeByDoctor = (doctorId) => {
  return request({
    url: `/api/doctor-change/by-doctor/${doctorId}`,
    method: 'get'
  })
}

// 管理员查询变更申请列表（可按状态过滤）
export const getAdminDoctorChangeList = (status) => {
  return request({
    url: '/api/doctor-change/admin/list',
    method: 'get',
    params: { status }
  })
}

// 管理员审核变更申请
export const reviewDoctorChange = (data) => {
  return request({
    url: '/api/doctor-change/admin/review',
    method: 'post',
    data
  })
}

