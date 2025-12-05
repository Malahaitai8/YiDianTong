import request from '@/utils/request'

/**
 * 获取高频问题列表（猜你想问）
 * GET /api/patient/qa/top-questions
 * @param {number} limit - 返回问题数量，默认 50
 * @returns {Promise<Array<{question: string, count: number}>>}
 */
export function getTopQuestions(limit = 50) {
  return request({
    url: '/api/patient/qa/top-questions',
    method: 'GET',
    data: { limit }
  });
}

/**
 * 记录用户问题到问题库
 * POST /api/patient/qa/log-question
 * @param {string} question - 用户问题内容
 * @returns {Promise}
 */
export function logQuestion(question) {
  return request({
    url: '/api/patient/qa/log-question',
    method: 'POST',
    data: { question },
    silent: true // 静默处理，失败不弹提示
  });
}

export default {
  getTopQuestions,
  logQuestion
};


