import request from './request'

// 查询所有医生
export const getDoctorList = () => {
  return request({
    url: '/doctor/selectAll',
    method: 'get'
  })
}

// 根据ID查询医生
export const getDoctorById = (id) => {
  return request({
    url: `/doctor/selectById/${id}`,
    method: 'get'
  })
}

// 创建医生
export const createDoctor = (data) => {
  return request({
    url: '/doctor',
    method: 'post',
    data
  })
}

// 更新医生信息
export const updateDoctor = (id, data) => {
  return request({
    url: `/doctor/${id}`,
    method: 'put',
    data
  })
}

// 删除医生
export const deleteDoctor = (id) => {
  return request({
    url: `/doctor/${id}`,
    method: 'delete'
  })
}

// 管理员创建医生账号
export const createDoctorAccount = (data) => {
  return request({
    url: '/admin/doctor/create',
    method: 'post',
    data
  })
}

// 重置医生密码
export const resetDoctorPassword = (id, data) => {
  return request({
    url: `/admin/doctor/${id}/reset-password`,
    method: 'post',
    data
  })
}

// 停用医生账号
export const disableDoctor = (id) => {
  return request({
    url: `/admin/doctor/${id}/disable`,
    method: 'post'
  })
}

// 启用医生账号
export const enableDoctor = (id) => {
  return request({
    url: `/admin/doctor/${id}/enable`,
    method: 'post'
  })
}

// 一键重置所有医生密码
export const resetAllDoctorPasswords = () => {
  return request({
    url: '/admin/doctor/reset-all-passwords',
    method: 'post'
  })
}

// 提交医生信息变更申请
export const submitDoctorChangeRequest = (data) => {
  return request({
    url: '/api/doctor/change-request',
    method: 'post',
    data
  })
}

// 获取待审核医生列表
export const getPendingDoctors = () => {
  return request({
    url: '/api/admin/approval/doctors/pending',
    method: 'get'
  })
}

// 获取待审核医生详情
export const getPendingDoctorDetail = (userId) => {
  return request({
    url: `/api/admin/approval/doctors/${userId}`,
    method: 'get'
  })
}

// 审核通过医生注册
export const approveDoctorRegistration = (data) => {
  return request({
    url: '/api/admin/approval/doctors/approve',
    method: 'post',
    data
  })
}

// 拒绝医生注册
export const rejectDoctorRegistration = (data) => {
  return request({
    url: '/api/admin/approval/doctors/reject',
    method: 'post',
    data
  })
}

// 批量审核通过医生注册
export const batchApproveDoctors = (data) => {
  return request({
    url: '/api/admin/approval/doctors/approve/batch',
    method: 'post',
    data
  })
}

// 医生端：查看我的排班（基于登录医生身份）
export const getMySchedules = (params) => {
  return request({
    url: '/doctor/my-schedules',
    method: 'get',
    params
  })
}

// 医生端：获取当前登录医生的基础信息（用于校验映射关系）
export const getMyInfo = () => {
  return request({
    url: '/doctor/my-info',
    method: 'get'
  })
}

// 医生端：查看预约患者列表（基于登录医生身份）
export const getMyPatients = (params) => {
  return request({
    url: '/doctor/my-patients',
    method: 'get',
    params
  })
}

// 医生端：提交个人信息修改申请
export const applyDoctorInfoUpdate = (data) => {
  return request({
    url: '/doctor/apply-info-update',
    method: 'post',
    data
  })
}

// 医生端：查看我的信息修改申请列表
export const getMyInfoApplications = () => {
  return request({
    url: '/doctor/my-info-applications',
    method: 'get'
  })
}

export const getTodayPatients = (params) => {
  return request({
    url: '/doctor/today-patients',
    method: 'get',
    params
  })
}

// 医生端：获取Dashboard数据
export const getDoctorDashboard = () => {
  return request({
    url: '/doctor/dashboard',
    method: 'get'
  })
}