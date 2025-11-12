const TokenKey = 'YDT-Token' // 定义一个 key
const UserInfoKey = 'YDT-UserInfo'

export function getToken() {
    return uni.getStorageSync(TokenKey)
}

export function setToken(token) {
    return uni.setStorageSync(TokenKey, token)
}

export function removeToken() {
    return uni.removeStorageSync(TokenKey)
}

// 用户信息持久化
export function getUserInfo() {
    try {
        const val = uni.getStorageSync(UserInfoKey)
        return val ? JSON.parse(val) : {}
    } catch (e) {
        return {}
    }
}

export function setUserInfo(userInfo) {
    return uni.setStorageSync(UserInfoKey, JSON.stringify(userInfo || {}))
}

export function removeUserInfo() {
    return uni.removeStorageSync(UserInfoKey)
}

export function promptLogin(options = {}) {
    const {
        content = '当前为游客模式，登录后才能使用该服务',
        confirmText = '去登录',
        cancelText = '再逛逛'
    } = options

    return new Promise((resolve) => {
        uni.showModal({
            title: '提示',
            content,
            confirmText,
            cancelText,
            success: (res) => {
                if (res.confirm) {
                    uni.navigateTo({
                        url: '/pages/login/login'
                    })
                    resolve(true)
                } else {
                    resolve(false)
                }
            },
            fail: () => {
                resolve(false)
            }
        })
    })
}