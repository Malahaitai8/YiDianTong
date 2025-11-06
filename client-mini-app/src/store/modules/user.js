import { getToken, setToken, removeToken } from '@/utils/auth.js'
import { login as loginApi } from '@/api/auth.js'

const state = {
    token: getToken(), // 启动时从缓存读取 Token
    userInfo: {} // 存储用户信息（如userId、username、role）
}

const mutations = {
    SET_TOKEN: (state, token) => {
        state.token = token
        setToken(token) // 存入缓存
    },
    SET_USERINFO: (state, userInfo) => {
        state.userInfo = userInfo
    },
    CLEAR_USER: (state) => {
        state.token = ''
        state.userInfo = {}
        removeToken() // 清除缓存
    }
}

const actions = {
    // 登录
    async login({ commit }, loginForm) {
        try {
            const data = await loginApi(loginForm)
            // 从返回的data中获取token和用户信息
            commit('SET_TOKEN', data.token)
            commit('SET_USERINFO', {
                userId: data.userId,
                username: data.username,
                role: data.role
            })
            return Promise.resolve()
        } catch (error) {
            return Promise.reject(error)
        }
    },

    // 退出登录
    logout({ commit }) {
        commit('CLEAR_USER')
    }
}

export default {
    namespaced: true, // 开启命名空间
    state,
    mutations,
    actions
}