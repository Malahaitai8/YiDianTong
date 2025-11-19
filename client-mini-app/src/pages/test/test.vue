<template>
	<view class="test-page">
		<view class="header">
			<text class="title">用户状态测试</text>
		</view>
		
		<view class="content">
			<view class="info-card">
				<text class="label">Token:</text>
				<text class="value">{{ token || '未登录' }}</text>
			</view>
			
			<view class="info-card">
				<text class="label">用户信息:</text>
				<text class="value">{{ userInfoText }}</text>
			</view>
			
			<button class="btn" @click="checkLogin">检查登录状态</button>
			<button class="btn" @click="goToLogin" v-if="!token">去登录</button>
			<button class="btn" @click="logout" v-else>退出登录</button>
		</view>
	</view>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
	computed: {
		...mapState('user', ['token', 'userInfo']),
		userInfoText() {
			return JSON.stringify(this.userInfo, null, 2);
		}
	},
	methods: {
		...mapActions('user', ['logout']),
		checkLogin() {
			console.log('Token:', this.token);
			console.log('UserInfo:', this.userInfo);
			uni.showToast({
				title: this.token ? '已登录' : '未登录',
				icon: this.token ? 'success' : 'none'
			});
		},
		goToLogin() {
			uni.navigateTo({ url: '/pages/login/login' });
		}
	}
};
</script>

<style scoped>
.test-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 30rpx;
}

.header {
	text-align: center;
	margin-bottom: 40rpx;
}

.title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.content {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.info-card {
	margin-bottom: 30rpx;
	padding: 20rpx;
	background: #f5f7fa;
	border-radius: 15rpx;
}

.label {
	display: block;
	font-weight: bold;
	margin-bottom: 10rpx;
	color: #666;
}

.value {
	display: block;
	font-family: monospace;
	white-space: pre-wrap;
	word-break: break-all;
	color: #333;
}

.btn {
	width: 100%;
	height: 80rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 15rpx;
	font-size: 28rpx;
	font-weight: 600;
	margin-bottom: 20rpx;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
}

.btn::after {
	border: none;
}
</style>