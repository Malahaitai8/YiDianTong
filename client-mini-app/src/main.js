import Vue from 'vue'
import App from './App'
import store from './store' // 1. 引入 store
import { promptLogin } from '@/utils/auth.js'
import webSocketManager from '@/utils/websocket.js'
import request from '@/utils/request.js'

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

// WebSocket 连接辅助函数
function connectWaitlistWebSocket() {
  const userId = store.state.user?.userInfo?.userId;
  if (!userId) {
    console.warn('无法连接WebSocket：用户ID不存在');
    return;
  }
  console.log('连接WebSocket，用户ID:', userId);
  webSocketManager.connect(userId);
}

function disconnectWaitlistWebSocket() {
  if (webSocketManager && typeof webSocketManager.disconnect === 'function') {
    webSocketManager.disconnect();
  } else if (webSocketManager && typeof webSocketManager.close === 'function') {
    webSocketManager.close();
  }
}

// 监听用户登录状态变化，自动连接/断开WebSocket
store.watch(
  (state) => state.user.token,
  (newToken, oldToken) => {
    if (newToken && !oldToken) {
      // 用户刚登录，连接WebSocket
      console.log('用户登录，准备连接WebSocket');
      setTimeout(() => {
        connectWaitlistWebSocket();
      }, 1000); // 延迟1秒确保用户信息已加载
    } else if (!newToken && oldToken) {
      // 用户退出登录，断开WebSocket
      disconnectWaitlistWebSocket();
    }
  }
);

// ========== WebSocket消息路由 ==========
function setupWebSocketMessageRouting() {
  // 监听WebSocket消息并路由到uni事件
  webSocketManager.on('message', (data) => {
    console.log('WebSocket消息路由:', data);

    try {
      const messageType = data.type;
      const messageData = data.data || {};

      switch (messageType) {
        case 'WAITLIST_SUCCESS':
          // 候补成功消息
          console.log('路由候补成功消息到页面');
          uni.$emit('waitlist-success', messageData);
          break;

        case 'RANK_UPDATE':
          // 排队位次更新消息
          console.log('路由排队位次更新消息到页面');
          uni.$emit('waitlist-rank-update', messageData);
          break;

        case 'SLOT_AVAILABLE':
          // 号源释放通知
          console.log('路由号源释放通知到页面');
          uni.$emit('SLOT_AVAILABLE', messageData);
          break;

        case 'APPOINTMENT_RESCHEDULED':
          // 预约重新安排消息 - 只路由，统一由全局 listener 处理弹窗与已读标记
          console.log('收到预约重新安排消息:', messageData);
          uni.$emit('appointment-rescheduled', messageData);
          break;

        case 'APPOINTMENT_CANCELLED_REFUND':
          // 预约取消退款消息
          console.log('路由预约取消退款消息到页面');
          uni.$emit('appointment-cancelled-refund', messageData);
          break;

        default:
          console.log('未识别的WebSocket消息类型:', messageType);
          break;
      }
    } catch (error) {
      console.error('WebSocket消息路由失败:', error);
    }
  });
}

// 设置WebSocket消息路由
setupWebSocketMessageRouting();

// 应用启动时，如果已登录则连接WebSocket
if (store.state.user.token) {
  setTimeout(() => {
    connectWaitlistWebSocket();
  }, 2000); // 延迟2秒确保应用完全启动
}

app.$mount()

// 全局处理关键通知（在任何页面都弹窗提示用户）
uni.$on('appointment-rescheduled', (data) => {
  try {
    console.log('全局收到预约重新安排:', data);
    if (!data || !data.appointmentId) return;

    const content = `您的预约已调整为：${data.doctorName} 医生，${data.appointmentDate} ${data.timeSlot}。系统已为您自动安排。若不满意，请在24小时内重新选择。`;

    uni.showModal({
      title: '预约已调整',
      content: content,
      confirmText: '重新选择',
      cancelText: '接受安排',
      success: async (res) => {
        try {
          // 标记相关 IN_APP 通知为已读（通过关联 appointmentId 查找）
          const notesRes = await request({ url: '/notifications', method: 'GET', silent: true });
          const list = Array.isArray(notesRes) ? notesRes : (notesRes && notesRes.list ? notesRes.list : []);
          if (list && list.length > 0) {
            const target = list.find(n => !n.isRead && n.channel === 'IN_APP' && n.relatedType === 'APPOINTMENT' && n.relatedId === data.appointmentId);
            if (target && target.id) {
              try {
                await request({ url: `/notifications/${target.id}/read`, method: 'POST', silent: true });
              } catch (err) {
                console.warn('标记通知已读失败', err);
              }
            }
          }
        } catch (e) {
          console.warn('处理通知已读时出错', e);
        }

        if (res.confirm) {
          // 跳转到预约详情以便用户重新选择
          uni.navigateTo({ url: `/pages/appointment-detail/appointment-detail?id=${data.appointmentId}` });
        } else {
          uni.showToast({ title: '已接受当前安排', icon: 'success' });
        }
      }
    });
  } catch (e) {
    console.error('全局处理预约重新安排失败:', e);
  }
});

uni.$on('appointment-cancelled-refund', (data) => {
  try {
    console.log('全局收到预约取消退款:', data);
    uni.showModal({
      title: '预约已取消并退款',
      content: `您的预约因医生调班已取消，费用已退还。时间：${data.appointmentDate} ${data.timeSlot}`,
      showCancel: false,
      confirmText: '知道了'
    });
  } catch (e) {
    console.error('全局处理预约取消退款失败:', e);
  }
});