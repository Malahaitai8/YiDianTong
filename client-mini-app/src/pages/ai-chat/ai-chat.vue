<template>
  <view class="ai-chat-page">
    <view class="top-bar">
      <view class="title">智能小助手</view>
      <view class="top-bar-actions">
        <button class="clear-btn" @click="clearChat">清除对话</button>
        <button class="home-btn" @click="goHome">回到首页</button>
      </view>
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

    <!-- 猜你想问：高频问题折叠卡片 -->
    <view class="suggest-card" v-if="topQuestions.length">
      <view class="suggest-header" @click="toggleSuggest">
        <view class="suggest-title-box">
          <text class="suggest-title">猜你想问</text>
          <text class="suggest-subtitle">根据近期高频问题智能推荐</text>
        </view>
        <view class="suggest-right">
          <text class="suggest-count">{{ topQuestions.length }} 个问题</text>
          <text class="suggest-toggle-text">
            {{ suggestExpanded ? '收起' : '展开' }}
          </text>
        </view>
      </view>

      <view v-if="suggestExpanded" class="suggest-body">
        <view
          v-for="item in topQuestions"
          :key="item.question"
          class="suggest-chip"
          @click="askSuggested(item.question)"
        >
          <text class="suggest-chip-text">{{ item.question }}</text>
          <text class="suggest-chip-count">×{{ item.count }}</text>
        </view>
      </view>
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
import { getTopQuestions, logQuestion } from '@/api/qa.js';

export default {
  data() {
    return {
      apiUrlInput: '',
      apiUrl: '',
      message: '',
      chatHistory: [],
      loading: false,
      lastMessageId: '',
      topQuestions: [],
      suggestExpanded: true
    };
  },
  onLoad(options = {}) {
    const questionFromQuery = (() => {
      const raw = options.question || '';
      try {
        return decodeURIComponent(raw);
      } catch (e) {
        return raw;
      }
    })().trim();

    const savedUrl = uni.getStorageSync('aiServiceUrl') || '';
    this.apiUrl = savedUrl;
    this.apiUrlInput = savedUrl;

    // 恢复历史对话
    const savedHistory = uni.getStorageSync('aiChatHistory') || [];
    if (Array.isArray(savedHistory) && savedHistory.length) {
      this.chatHistory = savedHistory;
      const last = savedHistory[savedHistory.length - 1];
      if (last && last.id) {
        this.lastMessageId = last.id;
      }
    }

    // 优先使用本地缓存的高频问题，没有再从后端拉取
    const cachedTop = uni.getStorageSync('aiTopQuestions') || [];
    if (Array.isArray(cachedTop) && cachedTop.length) {
      this.topQuestions = cachedTop;
    } else {
      this.fetchTopQuestions();
    }

    // 若从首页“猜你想问”跳转，预填问题
    if (questionFromQuery) {
      this.message = questionFromQuery;
    }
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
      // 保存到本地，保证刷新/重进后还能看到历史记录
      try {
        uni.setStorageSync('aiChatHistory', this.chatHistory);
      } catch (e) {
        console.error('保存聊天记录失败', e);
      }
      this.$nextTick(() => {
        this.lastMessageId = id;
      });
    },
    async sendMessage() {
      const question = (this.message || '').trim();
      if (!question) {
        uni.showToast({ title: '请输入您的问题', icon: 'none' });
        return;
      }
      await this.askQuestion(question);
    },
    async askQuestion(question) {
      if (!this.apiUrl) {
        uni.showToast({ title: '请先填写模型服务 URL', icon: 'none' });
        return;
      }

      this.pushMessage('user', question);
      this.message = '';
      this.loading = true;

      // 记录问题到问题库（静默处理，失败不影响用户体验）
      this.recordQuestion(question);

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
    },
    // 记录问题到问题库（静默处理）
    async recordQuestion(question) {
      // 检查是否登录
      const token = this.$store.state.user?.token;
      if (!token) {
        // 未登录，不记录
        return;
      }

      try {
        await logQuestion(question);
      } catch (error) {
        // 静默处理，失败不影响用户体验
        console.warn('记录问题失败:', error);
      }
    },
    async fetchTopQuestions() {
      try {
        const list = await getTopQuestions(50);
        const arr = Array.isArray(list) ? list : [];
        this.topQuestions = arr;
        // 缓存高频问题，避免每次进入都走慢接口
        try {
          uni.setStorageSync('aiTopQuestions', arr);
        } catch (e) {
          console.error('缓存高频问题失败', e);
        }
      } catch (e) {
        console.error('加载高频问题失败', e);
      }
    },
    toggleSuggest() {
      this.suggestExpanded = !this.suggestExpanded;
    },
    async askSuggested(question) {
      await this.askQuestion(question);
    },
    clearChat() {
      if (!this.chatHistory.length) {
        return;
      }
      uni.showModal({
        title: '确认清除',
        content: '确定要清除所有对话记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.chatHistory = [];
            this.lastMessageId = '';
            try {
              uni.removeStorageSync('aiChatHistory');
            } catch (e) {
              console.error('清除聊天记录失败', e);
            }
            uni.showToast({ title: '已清除', icon: 'success' });
          }
        }
      });
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

.top-bar-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.clear-btn {
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: #fee2e2;
  color: #b91c1c;
  border: none;
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

.suggest-card {
  margin-top: 16rpx;
  margin-bottom: 16rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 20rpx rgba(31, 42, 55, 0.05);
  padding: 20rpx 24rpx;
}

.suggest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggest-title-box {
  display: flex;
  flex-direction: column;
}

.suggest-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
}

.suggest-subtitle {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #9ca3af;
}

.suggest-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 22rpx;
  color: #6b7280;
}

.suggest-toggle-text {
  margin-top: 4rpx;
  color: #2563eb;
}

.suggest-body {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.suggest-chip {
  max-width: 100%;
  display: inline-flex;
  align-items: center;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #f3f4ff;
  color: #1d4ed8;
  font-size: 24rpx;
}

.suggest-chip-text {
  max-width: 460rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.suggest-chip-count {
  margin-left: 8rpx;
  font-size: 22rpx;
  color: #6b7280;
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

