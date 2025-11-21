import { getToken, setToken, removeToken, getUserInfo as getUserInfoCache, setUserInfo as setUserInfoCache, removeUserInfo as removeUserInfoCache } from '@/utils/auth.js'
import { login as loginApi } from '@/api/auth.js'
import request from '@/utils/request.js'

const state = {
    token: getToken(), // 启动时从缓存读取 Token
    userInfo: getUserInfoCache() || {} // 启动时从缓存读取用户信息
}

const mutations = {
    SET_TOKEN: (state, token) => {
        state.token = token
        setToken(token) // 存入缓存
    },
    SET_USERINFO: (state, userInfo) => {
        state.userInfo = userInfo
        setUserInfoCache(userInfo) // 持久化用户信息
    },
    CLEAR_USER: (state) => {
        state.token = ''
        state.userInfo = {}
        removeToken() // 清除缓存
        removeUserInfoCache()
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
    },

	// [新增] 验证Token有效性
	validateToken({ commit, state }) {
		if (!state.token) {
			return Promise.resolve(false)
		}
		// 发起一个静默的API请求来验证token
		// 这里我们用获取患者信息的接口，因为它需要登录
		// silent: true 选项可以防止在token失效时弹出全局错误提示
		return new Promise(resolve => {
			request({ url: '/patient/profile', method: 'GET', silent: true })
				.then(() => {
					// 请求成功，token有效
					resolve(true)
				})
				.catch(() => {
						// 请求失败，很可能是token过期
						commit('CLEAR_USER') // 清除本地存储的无效信息
						resolve(false)
					})
		})
	}
}

export default {
    namespaced: true, // 开启命名空间
    state,
    mutations,
    actions
}