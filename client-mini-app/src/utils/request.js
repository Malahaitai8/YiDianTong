import store from '@/store' // 引入 Vuex store

// 从 .env.development 文件读取 API 地址，如果没有则使用默认值
const BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

const request = (options) => {
    return new Promise((resolve, reject) => {
        uni.showLoading({
            title: '加载中...'
        })

        // 1. 自动拼接 API 基础地址
        if (!BASE_URL) {
            console.error('API基础地址未配置，请在 .env.development 文件中设置 VUE_APP_API_BASE_URL')
            uni.showToast({ 
                title: 'API地址未配置', 
                icon: 'none',
                duration: 3000
            })
            reject(new Error('API地址未配置'))
            return
        }
        options.url = BASE_URL + options.url

        // 2. 注入 JWT Token (关键)
        if (store.state.user.token) {
            options.header = {
                ...options.header,
                'Authorization': 'Bearer ' + store.state.user.token
            }
        }

        uni.request({
            ...options,
            success: (res) => {
                const data = res.data
                // API返回code为字符串"200"表示成功
                if (data.code === '200' || data.code === 200) {
                    resolve(data.data) // 只返回有用的 data 部分
                }
                // 假设 401 是 Token 失效
                else if (data.code === '401' || data.code === 401) {
                    uni.showToast({ title: '登录已过期', icon: 'none' })
                    store.dispatch('user/logout') // 触发 Vuex 登出
                    uni.navigateTo({ url: '/pages/login/login' }) // 跳转登录页
                    reject(data)
                }
                // 其他业务错误
                else {
                    uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
                    reject(data)
                }
            },
            fail: (err) => {
                console.error('请求失败:', err)
                // 更详细的错误提示
                let errorMsg = '网络错误'
                if (err.errMsg) {
                    if (err.errMsg.includes('timeout')) {
                        errorMsg = '请求超时，请检查网络'
                    } else if (err.errMsg.includes('fail')) {
                        errorMsg = '无法连接到服务器，请检查后端服务是否启动'
                    } else {
                        errorMsg = err.errMsg
                    }
                }
                uni.showToast({ 
                    title: errorMsg, 
                    icon: 'none',
                    duration: 3000
                })
                reject(err)
            },
            complete: () => {
                uni.hideLoading()
            }
        })
    })
}

export default request