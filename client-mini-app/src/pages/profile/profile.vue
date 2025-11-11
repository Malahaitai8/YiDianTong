<template>
	<view class="profile-page">
		<!-- 顶部用户信息 -->
		<view class="user-header">
			<view class="user-avatar-box">
				<text class="user-avatar">{{ userInitial }}</text>
			</view>
			<view class="user-basic">
				<text class="user-name">{{ userInfo.username || '未登录' }}</text>
				<view class="identity-status">
					<text class="status-text" :class="{ verified: isVerified }">
						{{ isVerified ? '✓ 已认证' : '未认证' }}
					</text>
				</view>
			</view>
		</view>

		<!-- 数据统计卡片 -->
		<view class="stats-card">
			<view class="stat-item" @click="goToMyAppointments">
				<text class="stat-number">0</text>
				<text class="stat-label">预约</text>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item" @click="goToRecords">
				<text class="stat-number">0</text>
				<text class="stat-label">就诊</text>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item" @click="goToSubstitute">
				<text class="stat-number">0</text>
				<text class="stat-label">候补</text>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="menu-section">
			<view class="menu-item" @click="goToPersonalInfo">
				<view class="menu-left">
					<text class="menu-icon">👤</text>
					<text class="menu-text">个人信息</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>
			
			<view class="menu-item" @click="goToSubstitute">
				<view class="menu-left">
					<text class="menu-icon">⏰</text>
					<text class="menu-text">我的候补</text>
				</view>
				<view class="menu-right">
					<text class="menu-badge" v-if="substituteCount > 0">{{ substituteCount }}</text>
					<text class="menu-arrow">›</text>
				</view>
			</view>
			
			<view class="menu-item" @click="goToRules">
				<view class="menu-left">
					<text class="menu-icon">📖</text>
					<text class="menu-text">挂号与退号规则</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>
		</view>

		<!-- 退出登录 -->
		<view class="logout-section">
			<button class="logout-btn" @click="handleLogout">退出登录</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			isVerified: false,
			substituteCount: 0
		};
	},
	computed: {
		userInfo() {
			return this.$store.state.user.userInfo || {};
		},
		userInitial() {
			const username = this.userInfo.username || '用';
			return username.substring(0, 1).toUpperCase();
		}
	},
	methods: {
		goToPersonalInfo() {
			uni.navigateTo({ url: '/pages/personal-info/personal-info' });
		},
		goToMyAppointments() {
			uni.showToast({ title: '我的预约功能开发中', icon: 'none' });
		},
		goToRecords() {
			uni.showToast({ title: '就诊记录功能开发中', icon: 'none' });
		},
		goToSubstitute() {
			uni.navigateTo({ url: '/pkg-user/my-substitute/my-substitute' });
		},
		goToRules() {
			uni.showToast({ title: '规则说明功能开发中', icon: 'none' });
		},
		handleLogout() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						this.$store.dispatch('user/logout');
						uni.showToast({ title: '已退出', icon: 'success' });
						setTimeout(() => {
							uni.reLaunch({ url: '/pages/index/index' });
						}, 800);
					}
				}
			});
		}
	}
};
</script>

<style scoped>
.profile-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

/* 顶部用户信息 */
.user-header {
	height: 250rpx;
	background: #fff;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	gap: 25rpx;
}
.user-avatar-box {
	width: 120rpx;
	height: 120rpx;
	background: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
	border-radius: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 20rpx rgba(30, 136, 229, 0.3);
}
.user-avatar {
	font-size: 48rpx;
	color: #fff;
	font-weight: bold;
}
.user-basic {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}
.user-name {
	font-size: 34rpx;
	color: #333;
	font-weight: 600;
}
.identity-status {
	
}
.status-text {
	font-size: 24rpx;
	color: #ff9800;
	background: #fff3e0;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	display: inline-block;
}
.status-text.verified {
	color: #4caf50;
	background: #e8f5e9;
}

/* 数据统计卡片 */
.stats-card {
	margin: 30rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	display: flex;
	justify-content: space-around;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
}
.stat-number {
	font-size: 40rpx;
	color: #1976d2;
	font-weight: bold;
}
.stat-label {
	font-size: 24rpx;
	color: #666;
}
.stat-divider {
	width: 1rpx;
	background: #e0e0e0;
}

/* 功能列表 */
.menu-section {
	margin: 30rpx;
	background: #fff;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.menu-item {
	height: 90rpx;
	padding: 0 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-bottom: 1rpx solid #f5f5f5;
}
.menu-item:last-child {
	border-bottom: none;
}
.menu-left {
	display: flex;
	align-items: center;
	gap: 20rpx;
}
.menu-icon {
	font-size: 36rpx;
	width: 40rpx;
	text-align: center;
}
.menu-text {
	font-size: 28rpx;
	color: #333;
}
.menu-right {
	display: flex;
	align-items: center;
	gap: 10rpx;
}
.menu-badge {
	background: #ff5252;
	color: #fff;
	font-size: 20rpx;
	padding: 4rpx 12rpx;
	border-radius: 20rpx;
	min-width: 32rpx;
	text-align: center;
}
.menu-arrow {
	font-size: 40rpx;
	color: #ccc;
	font-weight: 300;
}

/* 退出登录 */
.logout-section {
	padding: 30rpx;
}
.logout-btn {
	width: 100%;
	height: 88rpx;
	background: #fff;
	color: #f44336;
	border: 2rpx solid #f44336;
	border-radius: 15rpx;
	font-size: 28rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.logout-btn::after {
	border: none;
}
</style>
