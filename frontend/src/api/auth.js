import request from './request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 医生登录（兼容接口）
export const doctorLogin = (data) => {
  return request({
    url: '/auth/doctor/login',
    method: 'post',
    data
  })
}

// 管理员登录（兼容接口）
export const adminLogin = (data) => {
  return request({
    url: '/auth/admin/login',
    method: 'post',
    data
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/userinfo',
    method: 'get'
  })
}