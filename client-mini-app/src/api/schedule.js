import request from '@/utils/request.js'

/**
 * 根据ID查询排班详情
 * @param {Number} id - 排班ID
 * @returns {Promise} 返回排班详细信息
 */
export function getScheduleById(id) {
	return request({
		url: `/schedule/selectById/${id}`,
		method: 'GET'
	})
}

/**
 * 根据ID查询排班详细信息（包含医生、科室等信息）
 * @param {Number} id - 排班ID
 * @returns {Promise} 返回排班详细信息，包含医生姓名、科室名称等
 */
export function getScheduleDetailsById(id) {
	return request({
		url: `/schedule/details/${id}`,
		method: 'GET'
	})
}
