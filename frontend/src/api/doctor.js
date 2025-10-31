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