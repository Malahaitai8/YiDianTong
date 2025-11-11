const TokenKey = 'YDT-Token' // 定义一个 key

export function getToken() {
    return uni.getStorageSync(TokenKey)
}

export function setToken(token) {
    return uni.setStorageSync(TokenKey, token)
}

export function removeToken() {
    return uni.removeStorageSync(TokenKey)
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