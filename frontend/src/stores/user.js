import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isDoctor = computed(() => user.value?.role === 'doctor')
  const isAdmin = computed(() => user.value?.role === 'admin')

  // 方法
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('user')
    }
  }

  const loginUser = async (credentials) => {
    try {
      const response = await login(credentials)
      if (response.code === '200') {
        setToken(response.data.token)
        setUser(response.data)
        return { success: true, data: response.data }
      } else {
        return { success: false, message: response.msg }
      }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  const logoutUser = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      setToken('')
      setUser(null)
    }
  }

  const clearAuth = () => {
    setToken('')
    setUser(null)
  }

  return {
    // 状态
    token,
    user,
    // 计算属性
    isLoggedIn,
    isDoctor,
    isAdmin,
    // 方法
    setToken,
    setUser,
    loginUser,
    logoutUser,
    clearAuth
  }
})