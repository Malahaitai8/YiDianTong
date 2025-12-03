import request from './request'

export const getAdminList = () => {
  return request({
    url: '/admin/selectAll',
    method: 'get'
  })
}

export const getAdminById = (id) => {
  return request({
    url: `/admin/selectById/${id}`,
    method: 'get'
  })
}

