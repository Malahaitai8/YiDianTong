module.exports = {
  devServer: {
    port: 8081, // 开发服务器端口
    open: true, // 自动打开浏览器
    proxy: {
      // 代理配置（可选）
      '/api': {
        target: 'http://localhost:8080', // 后端API地址
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },
  transpileDependencies: ['uview-ui']
}




