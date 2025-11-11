<template>
	<view class="login-page">
		<!-- 顶部装饰 -->
		<view class="header-bg">
			<view class="circle circle-1"></view>
			<view class="circle circle-2"></view>
		</view>

		<!-- Logo区域 -->
		<view class="logo-section">
			<view class="logo-box">
				<text class="logo-icon">🏥</text>
			</view>
			<text class="app-name">医点通</text>
			<text class="app-desc">校医院预约挂号系统</text>
		</view>

		<!-- 登录表单 -->
		<view class="form-card">
			<text class="form-title">用户登录</text>
			
			<view class="form-item">
				<view class="input-box">
					<text class="input-icon">👤</text>
					<input 
						class="input" 
						v-model="loginForm.username" 
						placeholder="请输入用户名" 
						placeholder-class="placeholder"
					/>
				</view>
			</view>

			<view class="form-item">
				<view class="input-box">
					<text class="input-icon">🔒</text>
					<input 
						class="input" 
						v-model="loginForm.password" 
						type="password" 
						placeholder="请输入密码"
						placeholder-class="placeholder"
					/>
				</view>
			</view>

			<button class="login-btn" @click="handleLogin" :loading="loading">
				{{ loading ? '登录中...' : '登录' }}
			</button>

			<view class="form-footer">
				<text class="footer-text">还没有账号？</text>
				<text class="footer-link" @click="goToRegister">立即注册</text>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-section">
			<text class="tips-icon">💡</text>
			<text class="tips-text">登录后请完成身份认证，以享受医保报销服务</text>
		</view>
	</view>
</template>

<script>
import { mapActions } from 'vuex';

export default {
	data() {
		return {
			loginForm: {
				username: '',
				password: ''
			},
			loading: false
		};
	},
	methods: {
		...mapActions('user', ['login']),
		async handleLogin() {
			const { username, password } = this.loginForm;
			
			if (!username || !password) {
				uni.showToast({
					title: '请填写用户名和密码',
					icon: 'none'
				});
				return;
			}

			this.loading = true;
			try {
				await this.login(this.loginForm);
				uni.showToast({
					title: '登录成功',
					icon: 'success'
				});
				setTimeout(() => {
					uni.switchTab({ url: '/pages/index/index' });
				}, 800);
			} catch (error) {
				console.error('登录失败:', error);
				uni.showToast({
					title: error.msg || '登录失败，请检查用户名和密码',
					icon: 'none',
					duration: 2000
				});
			} finally {
				this.loading = false;
			}
		},
		goToRegister() {
			uni.navigateTo({ url: '/pages/register/register' });
		}
	}
};
</script>

<style scoped>
.login-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #1976d2 0%, #42a5f5 100%);
	padding: 40rpx 30rpx;
	position: relative;
	overflow: hidden;
}

/* 顶部装饰 */
.header-bg {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	height: 400rpx;
	overflow: hidden;
	z-index: 0;
}
.circle {
	position: absolute;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.1);
}
.circle-1 {
	width: 300rpx;
	height: 300rpx;
	top: -100rpx;
	left: -80rpx;
}
.circle-2 {
	width: 200rpx;
	height: 200rpx;
	top: 100rpx;
	right: -60rpx;
}

/* Logo区域 */
.logo-section {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-top: 80rpx;
	margin-bottom: 60rpx;
}
.logo-box {
	width: 140rpx;
	height: 140rpx;
	background: #fff;
	border-radius: 70rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.15);
	margin-bottom: 30rpx;
}
.logo-icon {
	font-size: 70rpx;
}
.app-name {
	font-size: 48rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 12rpx;
}
.app-desc {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.9);
}

/* 登录表单 */
.form-card {
	position: relative;
	z-index: 1;
	background: #fff;
	border-radius: 25rpx;
	padding: 50rpx 40rpx;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
}
.form-title {
	font-size: 36rpx;
	color: #333;
	font-weight: bold;
	display: block;
	margin-bottom: 40rpx;
	text-align: center;
}
.form-item {
	margin-bottom: 30rpx;
}
.input-box {
	height: 88rpx;
	background: #f5f7fa;
	border-radius: 15rpx;
	display: flex;
	align-items: center;
	padding: 0 25rpx;
	gap: 15rpx;
	border: 2rpx solid transparent;
	transition: all 0.3s;
}
.input-box:focus-within {
	background: #fff;
	border-color: #1976d2;
}
.input-icon {
	font-size: 32rpx;
	opacity: 0.6;
}
.input {
	flex: 1;
	height: 100%;
	font-size: 28rpx;
	color: #333;
}
.placeholder {
	color: #999;
	font-size: 28rpx;
}
.login-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 15rpx;
	font-size: 32rpx;
	font-weight: 600;
	margin-top: 20rpx;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
}
.login-btn::after {
	border: none;
}
.form-footer {
	display: flex;
	justify-content: center;
	align-items: center;
	gap: 8rpx;
	margin-top: 30rpx;
}
.footer-text {
	font-size: 24rpx;
	color: #666;
}
.footer-link {
	font-size: 24rpx;
	color: #1976d2;
	font-weight: 600;
}

/* 温馨提示 */
.tips-section {
	position: relative;
	z-index: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	margin-top: 40rpx;
}
.tips-icon {
	font-size: 28rpx;
}
.tips-text {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.95);
	text-align: center;
}
</style>
