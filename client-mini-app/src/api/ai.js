import request from '@/utils/request'; // 你的 request 文件路径

/**
 * AI 对话
 * @param {Object} data
 * @param {String} data.message   用户输入的问题
 * @param {String} data.apiUrl    用户填的后端代理地址（如 https://xxx.ngrok-free.dev）
 */
export function chatWithAI(data) {
  return request({
    url: '/api/ai/chat',
    method: 'POST',
    data: {
      message: data.message,
      apiUrl: data.apiUrl,
      temperature: 0.7,   // 写死
      maxTokens: 1024     // 写死
    }
  })
}

export default chatWithAI;

