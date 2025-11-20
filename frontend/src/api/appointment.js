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

