import { getToken, setToken, removeToken } from '@/utils/auth.js'

const state = {
    token: getToken(), // 启动时从缓存读取 Token
    userInfo: {} // 存储用户信息（如学号、姓名）
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
    // 模拟登录 (实际应调用 API)
    login({ commit }, token) {
        commit('SET_TOKEN', token)
        // 实际项目中, 你会在这里调用 api/user.js 的方法去后端登录
        // 然后把后端返回的 token 传给 SET_TOKEN
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