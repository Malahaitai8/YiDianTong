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