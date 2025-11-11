/**
 * 应用配置文件
 * 集中管理所有配置项
 */

// 开发环境与生产环境配置
const config = {
  // 开发环境
  development: {
    baseURL: 'http://localhost:8080',
    timeout: 10000
  },
  // 生产环境
  production: {
    baseURL: 'http://your-production-server.com:8080',
    timeout: 10000
  }
}

// 根据环境变量自动选择配置
const env = process.env.NODE_ENV || 'development'

export default config[env]


