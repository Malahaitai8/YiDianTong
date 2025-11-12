<template>
	<view class="register-page">
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
			<text class="app-desc">新用户注册</text>
		</view>

		<!-- 注册表单 -->
		<view class="form-card">
			<text class="form-title">创建您的账号</text>
			
			<view class="form-item">
				<view class="input-box">
					<text class="input-icon">👤</text>
					<input 
						class="input" 
						v-model="registerForm.username" 
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
						v-model="registerForm.password" 
						type="password" 
						placeholder="请设置登录密码"
						placeholder-class="placeholder"
					/>
				</view>
			</view>

			<view class="form-item">
				<view class="input-box">
					<text class="input-icon">✅</text>
					<input 
						class="input" 
						v-model="registerForm.confirmPassword" 
						type="password" 
						placeholder="请再次输入密码"
						placeholder-class="placeholder"
					/>
				</view>
			</view>

			<view class="form-item">
				<view class="input-box">
					<text class="input-icon">📱</text>
					<input 
						class="input" 
						v-model="registerForm.phoneNumber" 
						type="number" 
						placeholder="请输入手机号"
						placeholder-class="placeholder"
						maxlength="11"
					/>
				</view>
			</view>

			<button class="register-btn" @click="handleRegister" :loading="loading">
				{{ loading ? '注册中...' : '注册' }}
			</button>

			<view class="form-footer">
				<text class="footer-text">已有账号？</text>
				<text class="footer-link" @click="goToLogin">立即登录</text>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-section">
			<text class="tips-icon">💡</text>
			<text class="tips-text">注册后请登录并完善身份信息，以享受医保报销服务</text>
		</view>
	</view>
</template>

<script>
import { register } from '@/api/auth.js';

export default {
	data() {
		return {
			registerForm: {
				username: '',
				password: '',
				confirmPassword: '',
				phoneNumber: ''
			},
			loading: false
		};
	},
	methods: {
		async handleRegister() {
			const { username, password, confirmPassword, phoneNumber } = this.registerForm;
			
			// 表单验证
			if (!username || !password || !confirmPassword || !phoneNumber) {
				uni.showToast({
					title: '请填写所有必填项',
					icon: 'none'
				});
				return;
			}

			if (password !== confirmPassword) {
				uni.showToast({
					title: '两次密码输入不一致',
					icon: 'none'
				});
				return;
			}

			if (!/^1[3-9]\d{9}$/.test(phoneNumber)) {
				uni.showToast({
					title: '请输入有效的手机号',
					icon: 'none'
				});
				return;
			}

			this.loading = true;
			try {
				await register({
					username,
					password,
					phoneNumber
				});
				uni.showToast({
					title: '注册成功',
					icon: 'success',
					duration: 1500
				});
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
			} catch (error) {
				console.error('注册失败:', error);
				uni.showToast({
					title: error.msg || '注册失败',
					icon: 'none',
					duration: 2000
				});
			} finally {
				this.loading = false;
			}
		},
		goToLogin() {
			uni.redirectTo({ url: '/pages/login/login' });
		}
	}
};
</script>

<style scoped>
.register-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #1976d2 0%, #42a5f5 100%);
	padding: 40rpx 30rpx;
	padding-bottom: 60rpx;
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
	right: -80rpx;
}
.circle-2 {
	width: 200rpx;
	height: 200rpx;
	top: 150rpx;
	left: -60rpx;
}

/* Logo区域 */
.logo-section {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-top: 60rpx;
	margin-bottom: 50rpx;
}
.logo-box {
	width: 120rpx;
	height: 120rpx;
	background: #fff;
	border-radius: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.15);
	margin-bottom: 25rpx;
}
.logo-icon {
	font-size: 60rpx;
}
.app-name {
	font-size: 44rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 10rpx;
}
.app-desc {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.9);
}

/* 注册表单 */
.form-card {
	position: relative;
	z-index: 1;
	background: #fff;
	border-radius: 25rpx;
	padding: 45rpx 40rpx;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
}
.form-title {
	font-size: 34rpx;
	color: #333;
	font-weight: bold;
	display: block;
	margin-bottom: 35rpx;
	text-align: center;
}
.form-item {
	margin-bottom: 25rpx;
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
.register-btn {
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
.register-btn::after {
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
	margin-top: 30rpx;
}
.tips-icon {
	font-size: 28rpx;
}
.tips-text {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.95);
	text-align: center;
	line-height: 1.6;
}
</style>
