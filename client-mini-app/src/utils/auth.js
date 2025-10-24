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