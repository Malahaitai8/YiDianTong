<template>
	<view class="page">
		<view class="logo-area">
			<image class="logo" src="/static/logo.png" mode="aspectFit" />
			<text class="app-name">医点通</text>
			<text class="subtitle">校医院挂号系统</text>
		</view>

		<view class="card">
			<view class="title">用户注册</view>
			
			<!-- 用户名 -->
			<view class="form-item">
				<text class="label">用户名 <text class="required">*</text></text>
				<input 
					class="input" 
					v-model="form.username" 
					type="text" 
					placeholder="请输入用户名" 
				/>
				<text class="hint">用户名将作为登录账号</text>
			</view>
			
			<!-- 密码 -->
			<view class="form-item">
				<text class="label">密码 <text class="required">*</text></text>
				<input 
					class="input" 
					v-model="form.password" 
					type="password" 
					placeholder="请设置登录密码（至少6位）" 
				/>
			</view>
			
			<!-- 确认密码 -->
			<view class="form-item">
				<text class="label">确认密码 <text class="required">*</text></text>
				<input 
					class="input" 
					v-model="form.confirmPassword" 
					type="password" 
					placeholder="请再次输入密码" 
				/>
			</view>
			
			<!-- 手机号 -->
			<view class="form-item">
				<text class="label">手机号 <text class="required">*</text></text>
				<input 
					class="input" 
					v-model="form.phoneNumber" 
					type="number" 
					maxlength="11" 
					placeholder="请输入手机号" 
				/>
				<text class="hint">用于接收就诊提醒和账号安全</text>
			</view>

			<button class="primary-btn" :loading="loading" @click="handleRegister">立即注册</button>
			
			<view class="tips-box">
				<text class="tips-text">💡 注册成功后，请前往个人中心完成身份认证</text>
			</view>
			
			<view class="link-wrapper">
				<text @click="goToLogin" class="link">已有账号？去登录</text>
			</view>
		</view>
	</view>
</template>

<script>
import { register } from '@/api/auth.js';

export default {
	data() {
		return {
			loading: false,
			form: {
				username: '',
				password: '',
				confirmPassword: '',
				phoneNumber: ''
			}
		};
	},
	methods: {
		async handleRegister() {
			// 必填校验
			if (!this.form.username) {
				uni.showToast({ title: '请输入用户名', icon: 'none' });
				return;
			}
			if (this.form.username.length < 3) {
				uni.showToast({ title: '用户名至少3个字符', icon: 'none' });
				return;
			}
			if (!this.form.password) {
				uni.showToast({ title: '请输入密码', icon: 'none' });
				return;
			}
			if (this.form.password.length < 6) {
				uni.showToast({ title: '密码至少6位', icon: 'none' });
				return;
			}
			if (this.form.password !== this.form.confirmPassword) {
				uni.showToast({ title: '两次密码不一致', icon: 'none' });
				return;
			}
			if (!this.form.phoneNumber) {
				uni.showToast({ title: '请输入手机号', icon: 'none' });
				return;
			}
			if (!/^1[3-9]\d{9}$/.test(this.form.phoneNumber)) {
				uni.showToast({ title: '手机号格式不正确', icon: 'none' });
				return;
			}

			this.loading = true;
			try {
				// 准备注册数据（完全按照后端API）
				const registerData = {
					username: this.form.username,
					password: this.form.password,
					phoneNumber: this.form.phoneNumber
				};
				
				await register(registerData);
				uni.showToast({ title: '注册成功', icon: 'success' });
				setTimeout(() => {
					uni.navigateTo({ url: '/pages/login/login' });
				}, 1500);
			} catch (e) {
				console.error('注册失败:', e);
			} finally {
				this.loading = false;
			}
		},
		goToLogin() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped>
.page {
	min-height: 100vh;
	background: linear-gradient(180deg, #1976d2 0%, #42a5f5 100%);
	padding: 80rpx 40rpx;
}
.logo-area {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 60rpx;
}
.logo {
	width: 140rpx;
	height: 140rpx;
	margin-bottom: 20rpx;
}
.app-name {
	font-size: 42rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 10rpx;
}
.subtitle {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.9);
}
.card {
	background: #fff;
	border-radius: 20rpx;
	padding: 50rpx 40rpx;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.15);
}
.title {
	font-size: 34rpx;
	font-weight: 600;
	color: #0d47a1;
	text-align: center;
	margin-bottom: 40rpx;
}
.form-item {
	margin-bottom: 30rpx;
}
.label {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 15rpx;
	font-weight: 500;
}
.required {
	color: #f44336;
}
.input {
	width: 100%;
	height: 88rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}
.hint {
	display: block;
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}
.primary-btn {
	width: 100%;
	height: 90rpx;
	background: #1e88e5;
	color: #fff;
	border-radius: 10rpx;
	font-size: 32rpx;
	font-weight: 600;
	border: none;
	margin-top: 20rpx;
}
.primary-btn::after {
	border: none;
}
.tips-box {
	background: #fff8e1;
	border-left: 4rpx solid #ffa726;
	padding: 20rpx;
	margin-top: 30rpx;
	border-radius: 8rpx;
}
.tips-text {
	font-size: 24rpx;
	color: #e65100;
	line-height: 1.6;
}
.link-wrapper {
	text-align: center;
	margin-top: 30rpx;
}
.link {
	color: #1565c0;
	font-size: 26rpx;
}
</style>
