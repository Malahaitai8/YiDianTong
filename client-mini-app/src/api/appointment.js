import request from '@/utils/request.js'

/**
 * 搜索可预约时段
 * GET /appointment/search
 * @param {Object} params
 * @param {number} [params.departmentId]
 * @param {number} [params.doctorId]
 * @param {string} params.startDate YYYY-MM-DD
 * @param {string} params.endDate YYYY-MM-DD
 * @param {string} [params.timeSlot] 上午/下午/晚上 或 morning/afternoon/evening
 */
export function searchAvailable(params) {
	return request({
		url: '/appointment/search',
		method: 'GET',
		data: params
	})
}

/**
 * 创建预约
 * POST /appointment
 * @param {Object} body
 * @param {number} body.scheduleId
 * @param {string} body.appointmentTime YYYY-MM-DD HH:mm:ss
 */
export function createAppointment(body) {
	return request({
		url: '/appointment',
		method: 'POST',
		data: body
	})
}

/**
 * 查询我的预约
 * GET /appointment/me
 */
export function getMyAppointments() {
	return request({
		url: '/appointment/me',
		method: 'GET'
	})
}

/**
 * 取消预约
 * PUT /appointment/{id}/cancel
 * @param {number} id
 */
export function cancelAppointment(id) {
	return request({
		url: `/appointment/${id}/cancel`,
		method: 'PUT'
	})
}

/**
 * 删除预约
 * DELETE /appointment/{id}
 * @param {number} id
 */
export function deleteAppointment(id) {
	return request({
		url: `/appointment/${id}`,
		method: 'DELETE'
	})
}

/**
 * 改约
 * PUT /appointment/{id}/reschedule
 * @param {number} id
 * @param {number} newScheduleId
 */
export function rescheduleAppointment(id, newScheduleId) {
	return request({
		url: `/appointment/${id}/reschedule`,
		method: 'PUT',
		data: { newScheduleId }
	})
}


