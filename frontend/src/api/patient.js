import request from './request'

export const getPatientList = () => {
  return request({
    url: '/patient/selectAll',
    method: 'get'
  })
}

export const getPatientById = (id) => {
  return request({
    url: `/patient/selectById/${id}`,
    method: 'get'
  })
}

export const createPatient = (data) => {
  return request({
    url: '/patient',
    method: 'post',
    data
  })
}

export const updatePatient = (id, data) => {
  return request({
    url: `/patient/${id}`,
    method: 'put',
    data
  })
}

export const deletePatient = (id) => {
  return request({
    url: `/patient/${id}`,
    method: 'delete'
  })
}

// 审核患者认证状态
export const approvePatient = (id) => {
  return request({
    url: `/patient/${id}/approve`,
    method: 'post'
  })
}