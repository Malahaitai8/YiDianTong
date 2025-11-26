import Vue from 'vue'
import App from './App'
import store from './store' // 1. 引入 store
import { promptLogin } from '@/utils/auth.js'
import webSocketManager from '@/utils/websocket.js'

Vue.config.productionTip = false
App.mpType = 'app'

// 2. 挂载 store
Vue.prototype.$store = store

const app = new Vue({
  ...App,
  store // 3. 将 store 实例挂载到 Vue 实例
})

// --- 权限拦截 (脚手架核心) ---
const whiteList = [
  '/pages/index/index',
  '/pages/login/login',
  '/pages/register/register'
] // 路由白名单（仅允许首页/登录/注册，其他均需登录）

// 检查登录状态的函数
function checkLogin(url) {
  const urlPath = url.split('?')[0]
  // TabBar页面允许访问，但会在页面内部提示登录
  if (whiteList.includes(urlPath)) {
    return true
  }
  // 其他页面需要登录
  if (!store.state.user.token) {
    promptLogin()
    return false
  }
  return true
}

// 拦截 navigateTo
uni.addInterceptor('navigateTo', {
  invoke(args) {
    return checkLogin(args.url)
  },
  fail(err) {
    console.log(err)
  }
})

// 拦截 switchTab（用于TabBar切换）
uni.addInterceptor('switchTab', {
  invoke(args) {
    // TabBar页面：仅首页可自由访问，其余需要登录
    return checkLogin(args.url || '')
  },
  fail(err) {
    console.log(err)
  }
})

// 拦截所有网络请求：未登录直接提示并阻止发起请求
uni.addInterceptor('request', {
  invoke(args) {
    // 允许游客访问的公共接口（仅用于展示，不涉及用户隐私）
    const isPublicApi = (url = '', method = 'GET') => {
      const u = String(url)
      const m = String(method || 'GET').toUpperCase()

      // 登录/注册接口必须允许未登录访问（不限请求方法）
      if (/\/auth\/(login|register)/i.test(u)) {
        return true
      }

      // 医生列表、医生详情、排班数据均为公开展示
      const publicPatterns = [
        /\/doctor\/selectAll/i,
        /\/doctor\/selectById\/\d+/i,
        /\/schedule\/week/i,
        /\/doctor\/\d+\/schedules/i
      ]
      const matched = publicPatterns.some(re => re.test(u))
      // 仅放行 GET 的公共接口
      return matched && m === 'GET'
    }
    if (!store.state.user.token && !isPublicApi(args.url, args.method)) {
      promptLogin()
      return false
    }
    return true
  },
  fail(err) {
    console.log(err)
  }
})
// --- 权限拦截结束 ---

// 监听用户登录状态变化，自动连接/断开WebSocket
store.watch(
  (state) => state.user.token,
  (newToken, oldToken) => {
    if (newToken && !oldToken) {
      // 用户刚登录，连接WebSocket
      console.log('用户登录，连接WebSocket');
      setTimeout(() => {
        webSocketManager.connect();
      }, 1000); // 延迟1秒确保用户信息已加载
    } else if (!newToken && oldToken) {
      // 用户退出登录，断开WebSocket
      console.log('用户退出，断开WebSocket');
      webSocketManager.close();
    }
  }
);

// 应用启动时，如果已登录则连接WebSocket
if (store.state.user.token) {
  setTimeout(() => {
    webSocketManager.connect();
  }, 2000); // 延迟2秒确保应用完全启动
}

app.$mount()