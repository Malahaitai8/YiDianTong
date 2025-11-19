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
 * 获取排班详情（管理员接口）
 * GET /api/admin/schedules/{id}
 * @param {number} id
 */
export function getScheduleDetail(id) {
	return request({
		url: `/api/admin/schedules/${id}`,
		method: 'GET'
	})
}
