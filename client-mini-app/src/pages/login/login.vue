<template>
	<view class="page">
		<view class="logo-area">
			<image class="logo" src="/static/logo.png" mode="aspectFit" />
			<text class="app-name">医点通</text>
			<text class="subtitle">校医院挂号系统</text>
		</view>

		<view class="card">
			<view class="title">用户登录</view>
			<view class="form-item">
				<input 
					class="input" 
					v-model="loginForm.username" 
					type="text" 
					placeholder="请输入用户名" 
				/>
			</view>
			<view class="form-item">
				<input 
					class="input" 
					v-model="loginForm.password" 
					type="password" 
					placeholder="请输入密码" 
				/>
			</view>
			<button class="login-btn" :loading="loading" @click="handleLogin">登录</button>
			<view class="link-wrapper">
				<text class="link" @click="goToRegister">还没有账号？立即注册</text>
			</view>
		</view>
		
		<view class="tips-box">
			<text class="tips-title">💡 使用说明</text>
			<text class="tips-item">• 注册成功后请完成身份认证</text>
			<text class="tips-item">• 医生账号由管理员创建</text>
			<text class="tips-item">• 首次使用请先注册账号</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			loginForm: { username: '', password: '' },
			loading: false
		};
	},
	methods: {
		async handleLogin() {
			if (!this.loginForm.username || !this.loginForm.password) {
				uni.showToast({ title: '请输入完整信息', icon: 'none' });
				return;
			}
			this.loading = true;
			try {
				await this.$store.dispatch('user/login', this.loginForm);
				uni.showToast({ title: '登录成功', icon: 'success' });
				setTimeout(() => {
					uni.switchTab({ url: '/pages/index/index' });
				}, 800);
			} catch (e) {
				console.error(e);
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
.page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	background: linear-gradient(180deg, #2196f3 0%, #64b5f6 100%);
	padding-top: 120rpx;
	padding-bottom: 80rpx;
}
.logo-area {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 80rpx;
}
.logo {
	width: 160rpx;
	height: 160rpx;
	margin-bottom: 20rpx;
}
.app-name {
	font-size: 44rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 10rpx;
}
.subtitle {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.9);
}
.card {
	width: 620rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 60rpx 40rpx;
	box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.15);
	margin-bottom: 40rpx;
}
.title {
	font-size: 36rpx;
	font-weight: 600;
	color: #0d47a1;
	margin-bottom: 40rpx;
	text-align: center;
}
.form-item {
	margin-bottom: 30rpx;
}
.input {
	width: 100%;
	height: 90rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}
.login-btn {
	width: 100%;
	height: 90rpx;
	background: #1e88e5;
	color: #fff;
	border-radius: 10rpx;
	font-size: 32rpx;
	font-weight: 600;
	border: none;
	margin-top: 10rpx;
}
.login-btn::after { border: none; }
.link-wrapper { text-align: center; margin-top: 30rpx; }
.link { color: #1565c0; font-size: 26rpx; }
.tips-box {
	width: 620rpx;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 15rpx;
	padding: 30rpx;
}
.tips-title {
	display: block;
	font-size: 28rpx;
	color: #1976d2;
	font-weight: 600;
	margin-bottom: 15rpx;
}
.tips-item {
	display: block;
	font-size: 24rpx;
	color: #666;
	line-height: 1.8;
	margin-bottom: 8rpx;
}
</style>
