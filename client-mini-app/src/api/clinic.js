import request from '@/utils/request.js'

/**
 * 获取全部门诊列表
 * @returns {Promise<Array>} 门诊数组
 */
export function getClinicList() {
    return request({
        url: '/clinic/selectAll',
        method: 'GET'
    })
}

/**
 * 根据门诊 ID 获取详情
 * @param {number|string} id 门诊ID
 * @returns {Promise<Object>} 门诊详细信息
 */
export function getClinicById(id) {
    return request({
        url: `/clinic/selectById/${id}`,
        method: 'GET'
    })
}

/**
 * 根据科室ID获取门诊列表（使用后端接口）
 * @param {number|string} departmentId 科室ID
 * @returns {Promise<Array>} 门诊列表
 */
export function getClinicsByDepartmentId(departmentId) {
    return request({
        url: `/clinic/selectByDepartmentId/${departmentId}`,
        method: 'GET'
    })
}

/**
 * 搜索门诊（前端过滤）
 * @param {string} keyword 搜索关键词
 * @returns {Promise<Array>} 匹配的门诊列表
 */
export function searchClinics(keyword) {
    return getClinicList().then(data => {
        if (!keyword) {
            return data
        }
        const lowerKeyword = keyword.toLowerCase()
        return (data || []).filter(item => {
            const name = (item.name || '').toLowerCase()
            const description = (item.description || '').toLowerCase()
            return name.includes(lowerKeyword) || description.includes(lowerKeyword)
        })
    })
}

