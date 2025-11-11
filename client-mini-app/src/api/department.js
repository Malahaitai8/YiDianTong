import request from '@/utils/request.js'

/**
 * 获取全部科室列表
 * @returns {Promise<Array>} 科室数组
 */
export function getDepartmentList() {
    return request({
        url: '/department/selectAll',
        method: 'GET'
    })
}

/**
 * 根据科室 ID 获取详情
 * @param {number|string} id 科室ID
 * @returns {Promise<Object>} 科室详细信息
 */
export function getDepartmentById(id) {
    return request({
        url: `/department/selectById/${id}`,
        method: 'GET'
    })
}

/**
 * 搜索科室（前端过滤）
 * @param {string} keyword 搜索关键词
 * @returns {Promise<Array>} 匹配的科室列表
 */
export function searchDepartments(keyword) {
    return getDepartmentList().then(data => {
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

/**
 * 获取指定科室的医生列表
 * @param {number|string} departmentId 科室ID
 * @returns {Promise<Array>} 医生列表
 */
export function getDoctorsByDepartment(departmentId) {
    return request({
        url: `/department/${departmentId}/doctors`,
        method: 'GET'
    })
}
