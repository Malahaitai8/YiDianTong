import request from './request'

// 排班设置管理 API

// 查询全局上限配置
export const getGlobalScheduleSettings = () => {
  return request({
    url: '/api/admin/schedule-settings',
    method: 'get'
  })
}

// 更新全局上限配置
export const updateGlobalScheduleSettings = (data) => {
  return request({
    url: '/api/admin/schedule-settings',
    method: 'put',
    data
  })
}

// 查询医生级上限
export const getDoctorScheduleSettings = (doctorId) => {
  return request({
    url: `/api/admin/schedule-settings/doctor/${doctorId}`,
    method: 'get'
  })
}

// 更新医生级上限
export const updateDoctorScheduleSettings = (doctorId, data) => {
  return request({
    url: `/api/admin/schedule-settings/doctor/${doctorId}`,
    method: 'put',
    data
  })
}

// 查询门诊级上限
export const getClinicScheduleSettings = (clinicId) => {
  return request({
    url: `/api/admin/schedule-settings/clinic/${clinicId}`,
    method: 'get'
  })
}

// 更新门诊级上限
export const updateClinicScheduleSettings = (clinicId, data) => {
  return request({
    url: `/api/admin/schedule-settings/clinic/${clinicId}`,
    method: 'put',
    data
  })
}

