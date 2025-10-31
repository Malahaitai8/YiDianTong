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
  const isPatient = computed(() => user.value?.role === 'patient')

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
      // 只使用统一登录接口，不根据用户名做任何假设
      const response = await login(credentials)
      
      if (response.code === '200') {
        // 存储Token和用户信息（完全来自后端）
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
      // 调用后端登出接口
      const response = await logout()
      console.log('登出响应:', response)
      
      // 清除本地存储的Token和用户信息
      clearAuth()
      
      return { success: true, message: response.msg || '登出成功' }
    } catch (error) {
      console.error('登出请求失败:', error)
      
      // 即使后端请求失败，也要清除本地状态
      clearAuth()
      
      return { success: false, message: error.message || '登出失败' }
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
    isPatient,
    // 方法
    setToken,
    setUser,
    loginUser,
    logoutUser,
    clearAuth
  }
})