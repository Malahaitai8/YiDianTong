import Vue from 'vue'
import App from './App'
import store from './store' // 1. 引入 store

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
  '/pages/register/register',
  '/pages/appointment/appointment',
  '/pages/records/records',
  '/pages/profile/profile'
] // 路由白名单（TabBar页面都在白名单内，但部分功能需要登录后才能使用）

// 检查登录状态的函数
function checkLogin(url) {
  const urlPath = url.split('?')[0]
  // TabBar页面允许访问，但会在页面内部提示登录
  if (whiteList.includes(urlPath)) {
    return true
  }
  // 其他页面需要登录
  if (!store.state.user.token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    uni.navigateTo({
      url: '/pages/login/login'
    })
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
    // TabBar页面都允许访问
    return true
  },
  fail(err) {
    console.log(err)
  }
})
// --- 权限拦截结束 ---


app.$mount()