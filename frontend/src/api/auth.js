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

// 获取下一个可用的用户ID
export const getNextUserId = () => {
  return request({
    url: '/auth/next-user-id',
    method: 'get'
  })
}

// 创建用户和医生（管理员添加医生时使用）
export const createUserAndDoctor = (data) => {
  return request({
    url: '/auth/create-user-doctor',
    method: 'post',
    data
  })
}

// 患者注册（管理员也可用于代创建患者账号）
export const registerPatient = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}