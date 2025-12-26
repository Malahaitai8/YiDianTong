/**
 * WebSocket 工具类
 * 用于实时接收候补队列变化通知
 */

import config from '@/config'

class WebSocketClient {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000; // 3秒
    this.listeners = new Map();
    this.isConnecting = false;
    this.userId = null;
  }

  /**
   * 获取 WebSocket URL
   * @param {String} userId - 用户ID
   * @returns {String} WebSocket URL
   */
  getWebSocketUrl(userId) {
    // 从配置文件获取 API baseURL
    let baseURL = config.baseURL || 'http://localhost:8080';
    
    // 将 http:// 或 https:// 替换为 ws:// 或 wss://
    let wsUrl = baseURL.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:');
    
    // 在微信小程序中，localhost 无法访问，需要提示用户配置实际 IP
    // #ifdef MP-WEIXIN
    if (wsUrl.includes('localhost') || wsUrl.includes('127.0.0.1')) {
      console.warn('微信小程序中无法使用 localhost，请配置实际的服务器 IP 地址');
      // 尝试使用环境变量中的 WebSocket 配置，默认使用8080端口（与后端Spring Boot一致）
      // 在 H5 环境中优先使用当前页面的 host（方便使用设备局域网IP或本机IP调试）
      const defaultHost = (typeof window !== 'undefined' && window.location && window.location.hostname) ? window.location.hostname : 'localhost';
      const wsHost = process.env.VUE_APP_WS_HOST || defaultHost;
      const wsPort = process.env.VUE_APP_WS_PORT || '8080'; // 修改默认端口为8080
      const wsProtocol = process.env.VUE_APP_WS_PROTOCOL || 'ws';
      wsUrl = `${wsProtocol}://${wsHost}:${wsPort}`;
    }
    // #endif
    
    // 构建完整的 WebSocket URL
    return `${wsUrl}/ws/waitlist/${userId}`;
  }

  /**
   * 连接 WebSocket
   * @param {String} userId - 用户ID
   */
  connect(userId) {
    // 验证 userId
    if (!userId) {
      console.error('WebSocket 连接失败：用户ID不能为空');
      return;
    }

    if (this.isConnecting || (this.ws && this.ws.readyState === 1)) {
      console.log('WebSocket 已连接或正在连接中');
      return;
    }

    this.userId = userId;
    this.isConnecting = true;

    // 构建 WebSocket URL
    const wsUrl = this.getWebSocketUrl(userId);
    
    console.log('正在连接 WebSocket:', wsUrl);

    try {
      // #ifdef H5
      this.ws = new WebSocket(wsUrl);
      this.setupEventHandlers();
      // #endif

      // #ifdef MP-WEIXIN
      this.ws = uni.connectSocket({
        url: wsUrl,
        success: () => {
          console.log('WebSocket 连接请求已发送');
        },
        fail: (err) => {
          console.error('WebSocket 连接失败:', err);
          this.isConnecting = false;
          this.handleReconnect();
        }
      });
      this.setupUniEventHandlers();
      // #endif

      // #ifdef APP-PLUS
      this.ws = uni.connectSocket({
        url: wsUrl,
        success: () => {
          console.log('WebSocket 连接请求已发送');
        },
        fail: (err) => {
          console.error('WebSocket 连接失败:', err);
          this.isConnecting = false;
          this.handleReconnect();
        }
      });
      this.setupUniEventHandlers();
      // #endif
    } catch (error) {
      console.error('WebSocket 连接异常:', error);
      this.isConnecting = false;
      this.handleReconnect();
    }
  }

  /**
   * 设置 H5 环境的事件处理器
   */
  setupEventHandlers() {
    if (!this.ws) return;

    this.ws.onopen = () => {
      console.log('WebSocket 连接成功');
      this.isConnecting = false;
      this.reconnectAttempts = 0;
      this.notifyListeners('open', null);
    };

    this.ws.onmessage = (event) => {
      console.log('WebSocket 收到消息:', event.data);
      try {
        const data = JSON.parse(event.data);
        this.notifyListeners('message', data);
      } catch (error) {
        console.error('解析 WebSocket 消息失败:', error);
      }
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket 错误:', error);
      this.isConnecting = false;
      this.notifyListeners('error', error);
    };

    this.ws.onclose = (event) => {
      console.log('WebSocket 连接关闭:', event.code, event.reason);
      this.isConnecting = false;
      this.notifyListeners('close', event);
      this.handleReconnect();
    };
  }

  /**
   * 设置 uni-app 环境的事件处理器
   */
  setupUniEventHandlers() {
    if (!this.ws) return;

    uni.onSocketOpen(() => {
      console.log('WebSocket 连接成功');
      this.isConnecting = false;
      this.reconnectAttempts = 0;
      this.notifyListeners('open', null);
    });

    uni.onSocketMessage((res) => {
      console.log('WebSocket 收到消息:', res.data);
      try {
        const data = JSON.parse(res.data);
        this.notifyListeners('message', data);
      } catch (error) {
        console.error('解析 WebSocket 消息失败:', error);
      }
    });

    uni.onSocketError((error) => {
      console.error('WebSocket 错误:', error);
      this.isConnecting = false;
      this.notifyListeners('error', error);
    });

    uni.onSocketClose((event) => {
      console.log('WebSocket 连接关闭:', event);
      this.isConnecting = false;
      this.notifyListeners('close', event);
      this.handleReconnect();
    });
  }

  /**
   * 处理重连
   */
  handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('WebSocket 重连次数已达上限，停止重连');
      return;
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    this.reconnectAttempts++;
    console.log(`WebSocket 将在 ${this.reconnectDelay / 1000} 秒后尝试第 ${this.reconnectAttempts} 次重连`);

    this.reconnectTimer = setTimeout(() => {
      if (this.userId) {
        this.connect(this.userId);
      }
    }, this.reconnectDelay);
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    this.reconnectAttempts = 0;

    if (this.ws) {
      try {
        // #ifdef H5
        this.ws.close();
        // #endif

        // #ifndef H5
        uni.closeSocket();
        // #endif

        console.log('WebSocket 已主动断开');
      } catch (error) {
        console.error('断开 WebSocket 失败:', error);
      }
      this.ws = null;
    }

    this.isConnecting = false;
  }

  /**
   * 发送消息
   * @param {Object} data - 要发送的数据
   */
  send(data) {
    if (!this.ws || this.ws.readyState !== 1) {
      console.warn('WebSocket 未连接，无法发送消息');
      return false;
    }

    try {
      const message = typeof data === 'string' ? data : JSON.stringify(data);
      
      // #ifdef H5
      this.ws.send(message);
      // #endif

      // #ifndef H5
      uni.sendSocketMessage({
        data: message,
        success: () => {
          console.log('WebSocket 消息发送成功');
        },
        fail: (err) => {
          console.error('WebSocket 消息发送失败:', err);
        }
      });
      // #endif

      return true;
    } catch (error) {
      console.error('发送 WebSocket 消息失败:', error);
      return false;
    }
  }

  /**
   * 添加事件监听器
   * @param {String} event - 事件名称 (open/message/error/close)
   * @param {Function} callback - 回调函数
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event).push(callback);
  }

  /**
   * 移除事件监听器
   * @param {String} event - 事件名称
   * @param {Function} callback - 回调函数
   */
  off(event, callback) {
    if (!this.listeners.has(event)) return;
    
    const callbacks = this.listeners.get(event);
    const index = callbacks.indexOf(callback);
    if (index > -1) {
      callbacks.splice(index, 1);
    }
  }

  /**
   * 通知所有监听器
   * @param {String} event - 事件名称
   * @param {Any} data - 事件数据
   */
  notifyListeners(event, data) {
    if (!this.listeners.has(event)) return;
    
    const callbacks = this.listeners.get(event);
    callbacks.forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error(`执行 ${event} 事件监听器失败:`, error);
      }
    });
  }

  /**
   * 获取连接状态
   */
  getReadyState() {
    if (!this.ws) return -1;

    // #ifdef H5
    return this.ws.readyState;
    // #endif

    // #ifndef H5
    // uni-app 没有直接获取状态的方法，返回一个估计值
    return this.isConnecting ? 0 : (this.ws ? 1 : 3);
    // #endif
  }

  /**
   * 检查是否已连接
   */
  isConnected() {
    return this.getReadyState() === 1;
  }

  /**
   * 订阅候补队列更新
   * @param {Number} scheduleId - 排班ID
   */
  subscribeWaitlist(scheduleId) {
    if (!this.isConnected()) {
      console.warn('WebSocket 未连接，无法订阅候补队列');
      return;
    }

    const message = {
      type: 'SUBSCRIBE_WAITLIST',
      scheduleId: scheduleId,
      timestamp: Date.now()
    };

    console.log('订阅候补队列更新:', scheduleId);
    this.send(message);
  }
}

// 创建单例
const wsClient = new WebSocketClient();

export default wsClient;



























































