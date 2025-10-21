import store from '@/store' // 引入 Vuex store

// 从 .env.development 文件读取 API 地址
const BASE_URL = process.env.VUE_APP_API_BASE_URL

const request = (options) => {
    return new Promise((resolve, reject) => {
        uni.showLoading({
            title: '加载中...'
        })

        // 1. 自动拼接 API 基础地址
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
                // 假设 200 是成功码
                if (data.code === 200) {
                    resolve(data.data) // 只返回有用的 data 部分
                }
                // 假设 401 是 Token 失效
                else if (data.code === 401) {
                    uni.showToast({ title: '登录已过期', icon: 'none' })
                    store.dispatch('user/logout') // 触发 Vuex 登出
                    uni.navigateTo({ url: '/pages/login/login' }) // 跳转登录页
                    reject(data)
                }
                // 其他业务错误
                else {
                    uni.showToast({ title: data.message || '请求失败', icon: 'none' })
                    reject(data)
                }
            },
            fail: (err) => {
                uni.showToast({ title: '网络错误', icon: 'none' })
                reject(err)
            },
            complete: () => {
                uni.hideLoading()
            }
        })
    })
}

export default request