import request from '@/utils/request.js'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 返回登录结果，包含token和用户信息
 */
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'POST',
        data
    })
}

/**
 * 用户注册（仅支持患者注册）
 * @param {Object} data - 注册数据
 * @param {string} data.username - 用户名（必填，唯一）
 * @param {string} data.password - 密码（必填）
 * @param {string} data.name - 真实姓名（可选，默认使用username）
 * @param {string} data.specificRole - 具体角色（可选，默认"普通患者"）
 * @param {string} data.phoneNumber - 手机号（可选）
 * @param {string} data.idCardNumber - 身份证号（可选）
 * @returns {Promise} 返回注册结果
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'POST',
        data
    })
}


