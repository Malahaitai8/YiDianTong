import request from './request'

// 查询所有预约
export const getAppointmentList = () => {
  return request({
    url: '/appointment/selectAll',
    method: 'get'
  })
}

// 根据ID查询预约
export const getAppointmentById = (id) => {
  return request({
    url: `/appointment/selectById/${id}`,
    method: 'get'
  })
}

// 删除预约
export const deleteAppointment = (id) => {
  return request({
    url: `/appointment/${id}`,
    method: 'delete'
  })
}

// 医生查看患者详情（包含就诊历史）
export const getDoctorPatients = () => {
  return request({
    url: '/appointment/doctor/patients',
    method: 'get'
  })
}
