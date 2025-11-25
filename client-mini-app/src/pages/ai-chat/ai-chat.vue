<template>
  <view class="ai-chat-page">
    <view class="top-bar">
      <view class="title">智能小助手</view>
      <button class="home-btn" @click="goHome">回到首页</button>
    </view>

    <view class="url-input-card">
      <input
        class="url-input"
        type="text"
        v-model="apiUrlInput"
        placeholder="请输入模型服务 URL（如 https://botanically-stricken-shanell.ngrok-free.dev）"
      />
      <button class="save-btn" @click="saveApiUrl">保存地址</button>
    </view>

    <scroll-view
      class="chat-window"
      scroll-y="true"
      :scroll-into-view="lastMessageId"
    >
      <view
        class="chat-bubble"
        v-for="item in chatHistory"
        :key="item.id"
        :id="item.id"
        :class="[item.role]"
      >
        <view class="bubble-header">
          <text class="sender">{{ item.role === 'user' ? '我' : '小助手' }}</text>
          <text class="timestamp">{{ item.time }}</text>
        </view>
        <text class="bubble-content">{{ item.content }}</text>
      </view>

      <view v-if="!chatHistory.length" class="empty-tip">
        <text>欢迎向智能小助手提问，例如“如何预约挂号？”</text>
      </view>
    </scroll-view>

    <view class="input-area">
      <textarea
        class="message-input"
        v-model="message"
        placeholder="请输入您的问题"
        auto-height
        :maxlength="-1"
      ></textarea>
      <button
        class="send-btn"
        :disabled="loading"
        @click="sendMessage"
      >{{ loading ? '发送中...' : '发送' }}</button>
    </view>
  </view>
</template>

<script>
import chatWithAI from '@/api/ai.js';

export default {
  data() {
    return {
      apiUrlInput: '',
      apiUrl: '',
      message: '',
      chatHistory: [],
      loading: false,
      lastMessageId: ''
    };
  },
  onLoad() {
    const savedUrl = uni.getStorageSync('aiServiceUrl') || '';
    this.apiUrl = savedUrl;
    this.apiUrlInput = savedUrl;
  },
  methods: {
    goHome() {
      uni.switchTab({ url: '/pages/index/index' });
    },
    saveApiUrl() {
      const trimmed = (this.apiUrlInput || '').trim();
      if (!trimmed) {
        uni.showToast({ title: '请输入有效的 URL', icon: 'none' });
        return;
      }
      this.apiUrl = trimmed;
      uni.setStorageSync('aiServiceUrl', trimmed);
      uni.showToast({ title: '已保存', icon: 'success' });
    },
    formatTime(date) {
      const pad = (n) => n.toString().padStart(2, '0');
      return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
    },
    pushMessage(role, content) {
      const id = `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
      const time = this.formatTime(new Date());
      this.chatHistory.push({ id, role, content, time });
      this.$nextTick(() => {
        this.lastMessageId = id;
      });
    },
    async sendMessage() {
      if (!this.apiUrl) {
        uni.showToast({ title: '请先填写模型服务 URL', icon: 'none' });
        return;
      }

      const question = (this.message || '').trim();
      if (!question) {
        uni.showToast({ title: '请输入您的问题', icon: 'none' });
        return;
      }

      this.pushMessage('user', question);
      this.message = '';
      this.loading = true;

      try {
        const reply = await chatWithAI({
          message: question,
          apiUrl: this.apiUrl
        });
        const text =
          typeof reply === 'string'
            ? reply
            : reply?.data || reply?.message || '我暂时没有获取到回答，请稍后重试。';
        this.pushMessage('assistant', text);
      } catch (error) {
        this.pushMessage('assistant', '抱歉，服务暂时不可用，请稍后再试。');
        console.error('AI chat error:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.ai-chat-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fb;
  padding: 20rpx 24rpx 30rpx;
  box-sizing: border-box;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}

.title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2a37;
}

.home-btn {
  font-size: 26rpx;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #e3f2fd;
  color: #1976d2;
  border: none;
}

.url-input-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  display: flex;
  gap: 12rpx;
  align-items: center;
  box-shadow: 0 8rpx 20rpx rgba(31, 42, 55, 0.05);
  margin-bottom: 20rpx;
}

.url-input {
  flex: 1;
  font-size: 26rpx;
  padding: 0 16rpx;
  height: 72rpx;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
}

.save-btn {
  font-size: 26rpx;
  padding: 0 24rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: #1976d2;
  color: #fff;
  border: none;
}

.chat-window {
  flex: 1;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: inset 0 0 0 1rpx #f0f0f0;
}

.chat-bubble {
  margin-bottom: 20rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f3f4f6;
}

.chat-bubble.user {
  background: #e3f2fd;
}

.chat-bubble.assistant {
  background: #f3f4f6;
}

.bubble-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.bubble-content {
  font-size: 28rpx;
  color: #1f2933;
  line-height: 1.6;
  white-space: pre-wrap;
}

.empty-tip {
  text-align: center;
  color: #9ca3af;
  font-size: 26rpx;
}

.input-area {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  min-height: 120rpx;
  border: 1rpx solid #e5e7eb;
  border-radius: 16rpx;
  padding: 16rpx;
  font-size: 28rpx;
  background: #fff;
}

.send-btn {
  width: 160rpx;
  height: 120rpx;
  border-radius: 16rpx;
  background: #1976d2;
  color: #fff;
  font-size: 28rpx;
  border: none;
}

.send-btn:disabled {
  opacity: 0.6;
}
</style>

