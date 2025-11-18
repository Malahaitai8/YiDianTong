<template>
	<view class="profile-page">
		<!-- 顶部用户信息 -->
		<view class="user-header">
			<view class="user-avatar-box">
				<text class="user-avatar">{{ userInitial }}</text>
			</view>
			<view class="user-basic">
				<text class="user-name">{{ userInfo.username || patientInfo.name || '游客' }}</text>
				<view class="identity-status" v-if="isLoggedIn">
					<text class="status-text" :class="{ verified: isVerified }">
						{{ statusText }}
					</text>
					<text class="role-text" v-if="isVerified && patientInfo.specificRole">
						{{ roleText }}
					</text>
				</view>
				<view class="identity-status" v-else>
					<text class="status-text">点击登录按钮登录</text>
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

		<!-- 退出登录/登录按钮 -->
		<view class="logout-section">
			<button class="logout-btn" @click="handleLogout" v-if="isLoggedIn">退出登录</button>
			<button class="login-btn" @click="goToLogin" v-else>立即登录</button>
		</view>
	</view>
</template>

<script>
import { promptLogin } from '@/utils/auth.js';
import { getVerifyStatus } from '@/api/auth.js';
import { getPatientProfile } from '@/api/patient.js';

export default {
	data() {
		return {
			isVerified: false,
			substituteCount: 0,
			patientInfo: {} // 患者信息（包含认证状态）
		};
	},
	computed: {
		// 检查是否已登录
		isLoggedIn() {
			return !!this.$store.state.user.token;
		},
		userInfo() {
			return this.$store.state.user.userInfo || {};
		},
		userInitial() {
			const name = this.userInfo.username || this.patientInfo.name || '游';
			return name.substring(0, 1).toUpperCase();
		},
		// 认证状态文本
		statusText() {
			if (this.isVerified) {
				return '✓ 已认证';
			}
			return '未认证';
		},
		// 角色文本
		roleText() {
			const role = this.patientInfo.specificRole;
			if (role === 'student') return '学生';
			if (role === 'teacher') return '教师';
			if (role === 'outsider') return '外部人员';
			return '';
		}
	},
	onShow() {
		// 页面显示时刷新认证状态
		if (this.isLoggedIn) {
			this.loadVerifyStatus();
		}
	},
	methods: {
		// 加载认证状态
		async loadVerifyStatus() {
			if (!this.isLoggedIn) return;
			
			try {
				// 获取患者个人信息（包含认证状态）
				const data = await getPatientProfile();
				if (data) {
					this.patientInfo = data;
					// 判断是否已认证：idStatus === 'verified' 或 '已认证'
					this.isVerified = data.idStatus === 'verified' || data.idStatus === '已认证';
				}
			} catch (error) {
				console.error('获取认证状态失败:', error);
				// 如果接口不存在或失败，尝试使用认证状态接口
				try {
					const verifyData = await getVerifyStatus();
					if (verifyData) {
						this.patientInfo = verifyData;
						this.isVerified = verifyData.idStatus === 'verified' || verifyData.idStatus === '已认证';
					}
				} catch (err) {
					console.error('获取认证状态失败:', err);
				}
			}
		},
		goToPersonalInfo() {
			if (!this.isLoggedIn) {
				promptLogin();
				return;
			}
			uni.navigateTo({ url: '/pages/personal-info/personal-info' });
		},
		goToMyAppointments() {
			if (!this.isLoggedIn) {
			promptLogin();
				return;
			}
			uni.showToast({ title: '我的预约功能开发中', icon: 'none' });
		},
		goToRecords() {
			if (!this.isLoggedIn) {
			promptLogin();
				return;
			}
			uni.switchTab({ url: '/pages/records/records' });
		},
		goToSubstitute() {
			if (!this.isLoggedIn) {
			promptLogin();
				return;
			}
			uni.navigateTo({ url: '/pkg-user/my-substitute/my-substitute' });
		},
		goToRules() {
			uni.showToast({ title: '规则说明功能开发中', icon: 'none' });
		},
		goToLogin() {
			uni.navigateTo({ url: '/pages/login/login' });
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
.role-text {
	font-size: 22rpx;
	color: #666;
	margin-top: 6rpx;
	display: block;
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
.login-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 15rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
}
.login-btn::after {
	border: none;
}
</style>
