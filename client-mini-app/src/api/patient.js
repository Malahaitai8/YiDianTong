import request from '@/utils/request.js'

/**
 * 根据ID查询患者信息
 * @param {Number} id - 患者ID
 * @returns {Promise} 返回患者详细信息
 */
export function getPatientById(id) {
    return request({
        url: `/patient/selectById/${id}`,
        method: 'GET'
    })
}

/**
 * 更新患者信息
 * @param {Number} id - 患者ID
 * @param {Object} data - 患者信息
 * @returns {Promise} 返回更新结果
 */
export function updatePatient(id, data) {
    return request({
        url: `/patient/${id}`,
        method: 'PUT',
        data
    })
}

/**
 * 获取当前登录患者的个人信息
 * @returns {Promise} 返回患者个人信息（合并用户与患者表关键字段）
 */
export function getPatientProfile() {
    return request({
        url: '/patient/profile',
        method: 'GET'
    })
}