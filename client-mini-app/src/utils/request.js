import store from '@/store' // 引入 Vuex store
import config from '@/config' // 引入配置文件

// 从配置文件读取 API 地址
const BASE_URL = config.baseURL

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
        // 排除认证相关接口（登录、注册），这些接口不需要token
        const authUrls = ['/auth/login', '/auth/register', '/auth/admin/login']
        const isAuthRequest = authUrls.some(url => options.url.includes(url))
        
        if (store.state.user.token && !isAuthRequest) {
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
                    uni.showToast({ title: '登录已过期', icon: 'none' });
                    store.dispatch('user/logout'); // 触发 Vuex 登出
                    uni.navigateTo({ url: '/pages/login/login' }); // 跳转登录页
                    reject(data);
                }
                // 其他业务错误
                else {
                    uni.showToast({ title: data.msg || '请求失败', icon: 'none' });
                    reject(data);
                }
            },
            fail: (err) => {
                // 更详细的错误提示
                let errorMsg = '网络错误';
                if (err.errMsg) {
                    if (err.errMsg.includes('timeout')) {
                        errorMsg = '请求超时，请检查网络';
                    } else if (err.errMsg.includes('fail')) {
                        errorMsg = '无法连接到服务器，请检查后端服务是否启动';
                    } else {
                        errorMsg = err.errMsg;
                    }
                }
                uni.showToast({ 
                    title: errorMsg, 
                    icon: 'none',
                    duration: 3000
                });
                reject(err);
            },
            complete: () => {
                uni.hideLoading()
            }
        })
    })
}

export default request