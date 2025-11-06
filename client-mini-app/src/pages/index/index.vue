<template>
	<view class="home-page">
		<view class="header">
			<image class="logo" src="/static/logo.png" mode="aspectFit" />
			<text class="app-name">医点通</text>
			<text class="slogan">智慧医疗，一点就通</text>
		</view>

		<!-- 未登录状态 -->
		<view v-if="!isLoggedIn" class="welcome-card">
			<text class="welcome-title">欢迎使用医点通</text>
			<text class="welcome-desc">便捷预约，高效就医</text>
			<view class="btn-group">
				<button class="btn-primary" @click="goToLogin">登录</button>
				<button class="btn-secondary" @click="goToRegister">新用户注册</button>
			</view>
		</view>

		<!-- 已登录状态 -->
		<view v-else class="user-card">
			<view class="user-info">
				<text class="welcome-text">欢迎回来，{{ userInfo.username || '用户' }}</text>
				<text class="role-tag">{{ roleText }}</text>
			</view>
			<view class="quick-actions">
				<button class="action-btn" @click="goToProfile">
					<text class="icon">👤</text>
					<text>个人信息</text>
				</button>
				<button class="action-btn" @click="goToAppointment">
					<text class="icon">📅</text>
					<text>预约挂号</text>
				</button>
				<button class="action-btn" @click="goToMyAppointments">
					<text class="icon">📋</text>
					<text>我的预约</text>
				</button>
				<button class="action-btn" @click="goToSubstitute">
					<text class="icon">⏰</text>
					<text>我的候补</text>
				</button>
			</view>
			<button class="btn-logout" @click="handleLogout">退出登录</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {};
	},
	computed: {
		isLoggedIn() {
			return !!this.$store.state.user.token;
		},
		userInfo() {
			return this.$store.state.user.userInfo || {};
		},
		roleText() {
			const role = this.userInfo.role;
			const roleMap = {
				patient: '患者',
				doctor: '医生',
				admin: '管理员'
			};
			return roleMap[role] || '用户';
		}
	},
	methods: {
		goToLogin() {
			uni.navigateTo({ url: '/pages/login/login' });
		},
		goToRegister() {
			uni.navigateTo({ url: '/pages/register/register' });
		},
		goToProfile() {
			uni.navigateTo({ url: '/pages/profile/profile' });
		},
		goToAppointment() {
			uni.showToast({ title: '功能开发中', icon: 'none' });
		},
		goToMyAppointments() {
			uni.showToast({ title: '功能开发中', icon: 'none' });
		},
		goToSubstitute() {
			uni.navigateTo({ url: '/pkg-user/my-substitute/my-substitute' });
		},
		handleLogout() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						this.$store.dispatch('user/logout');
						uni.showToast({ title: '已退出', icon: 'success' });
					}
				}
			});
		}
	}
};
</script>

<style scoped>
.home-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #1976d2 0%, #42a5f5 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 80rpx 40rpx;
}
.header {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 80rpx;
}
.logo {
	width: 160rpx;
	height: 160rpx;
	margin-bottom: 30rpx;
}
.app-name {
	font-size: 48rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 15rpx;
}
.slogan {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.9);
}
.welcome-card, .user-card {
	width: 640rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 60rpx 40rpx;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.15);
}
.welcome-title {
	display: block;
	font-size: 36rpx;
	font-weight: 600;
	color: #0d47a1;
	text-align: center;
	margin-bottom: 20rpx;
}
.welcome-desc {
	display: block;
	font-size: 28rpx;
	color: #666;
	text-align: center;
	margin-bottom: 50rpx;
}
.btn-group {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}
.btn-primary {
	width: 100%;
	height: 90rpx;
	background: #1e88e5;
	color: #fff;
	border-radius: 10rpx;
	font-size: 32rpx;
	font-weight: 600;
	border: none;
}
.btn-primary::after { border: none; }
.btn-secondary {
	width: 100%;
	height: 90rpx;
	background: #fff;
	color: #1e88e5;
	border: 2rpx solid #1e88e5;
	border-radius: 10rpx;
	font-size: 32rpx;
	font-weight: 600;
}
.btn-secondary::after { border: none; }
.user-info {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 40rpx;
	padding-bottom: 30rpx;
	border-bottom: 1rpx solid #e0e0e0;
}
.welcome-text {
	font-size: 32rpx;
	color: #0d47a1;
	font-weight: 600;
}
.role-tag {
	font-size: 24rpx;
	color: #1e88e5;
	background: #e3f2fd;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
}
.quick-actions {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 20rpx;
	margin-bottom: 40rpx;
}
.action-btn {
	height: 140rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	border: none;
	font-size: 26rpx;
	color: #333;
}
.action-btn::after { border: none; }
.icon {
	font-size: 48rpx;
}
.btn-logout {
	width: 100%;
	height: 80rpx;
	background: #fff;
	color: #f44336;
	border: 2rpx solid #f44336;
	border-radius: 10rpx;
	font-size: 28rpx;
}
.btn-logout::after { border: none; }
</style>
