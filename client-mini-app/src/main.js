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
  '/pages/login/login'
] // 路由白名单

uni.addInterceptor('navigateTo', {
  invoke(args) {
    // 检查是否在白名单
    if (!whiteList.includes(args.url.split('?')[0])) {
      // 检查是否登录
      if (!store.state.user.token) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        // 未登录，跳转到登录页
        uni.navigateTo({
          url: '/pages/login/login'
        })
        return false // 阻止本次跳转
      }
    }
    return true // 允许跳转
  },
  fail(err) {
    console.log(err)
  }
})
// --- 权限拦截结束 ---


app.$mount()