import request from './request'

export const getWhitelistList = () => {
  return request({
    url: '/admin/whitelist/selectAll',
    method: 'get'
  })
}

export const getWhitelistById = (id) => {
  return request({
    url: `/admin/whitelist/selectById/${id}`,
    method: 'get'
  })
}

export const getWhitelistByRoleType = (roleType) => {
  return request({
    url: `/admin/whitelist/selectByRoleType/${roleType}`,
    method: 'get'
  })
}

export const createWhitelist = (data) => {
  return request({
    url: '/admin/whitelist',
    method: 'post',
    data
  })
}

export const updateWhitelist = (id, data) => {
  return request({
    url: `/admin/whitelist/${id}`,
    method: 'put',
    data
  })
}

export const deleteWhitelist = (id) => {
  return request({
    url: `/admin/whitelist/${id}`,
    method: 'delete'
  })
}







