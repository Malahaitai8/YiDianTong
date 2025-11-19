/**
 * 应用配置文件
 * 集中管理所有配置项
 */

// 从环境变量读取API基础地址
const baseURL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

// 开发环境与生产环境配置
const config = {
  // 开发环境
  development: {
    baseURL: baseURL,
    timeout: 10000
  },
  // 生产环境
  production: {
    baseURL: baseURL,
    timeout: 10000
  }
}

// 根据环境变量自动选择配置
const env = process.env.NODE_ENV || 'development'

export default config[env]