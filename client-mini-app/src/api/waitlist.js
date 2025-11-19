import request from '@/utils/request.js'

/**
 * 查看我的候补列表
 * GET /waitlist/me
 */
export function getMyWaitlist() {
	return request({
		url: '/waitlist/me',
		method: 'GET'
	})
}

/**
 * 加入候补队列
 * POST /waitlist
 * @param {Object} body
 * @param {number} body.scheduleId
 */
export function joinWaitlist(body) {
	return request({
		url: '/waitlist',
		method: 'POST',
		data: body
	})
}

/**
 * 退出候补队列
 * DELETE /waitlist/{scheduleId}
 * @param {number} scheduleId
 */
export function cancelWaitlist(scheduleId) {
	return request({
		url: `/waitlist/${scheduleId}`,
		method: 'DELETE'
	})
}