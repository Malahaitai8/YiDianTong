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
 * @param {number} body.waitlistId
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

/**
 * 创建候补预支付订单
 * POST /prepayment/waitlist
 */
export function createWaitlistPrepayment(data) {
	return request({
		url: '/prepayment/waitlist',
		method: 'POST',
		data
	})
}

/**
 * 支付候补预支付订单
 * POST /prepayment/pay
 */
export function payWaitlistOrder(data) {
	return request({
		url: '/prepayment/pay',
		method: 'POST',
		data
	})
}