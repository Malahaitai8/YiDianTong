<template>
	<view class="home-page">
		<!-- 顶部区域 -->
		<view class="header">
			<view class="greeting">
				<text class="greeting-text">{{ greeting }}，{{ userName }}</text>
				<view class="qa-entry" @click="goToQA" v-if="isLoggedIn">
					<text class="qa-icon">💬</text>
					<text class="qa-text">智能问答</text>
				</view>
				<view class="login-entry" @click="goToLogin" v-else>
					<text class="login-text">登录/注册</text>
				</view>
			</view>
		</view>

		<!-- 搜索框 -->
		<view class="search-container">
			<view class="search-box" @click="goToSearch">
				<text class="search-icon">🔍</text>
				<text class="search-placeholder">搜索科室 / 医生 / 日期</text>
			</view>
		</view>

		<!-- 核心功能区 -->
		<view class="main-section">
			<!-- 主入口卡片 -->
			<view class="main-card" @click="goToAppointment">
				<view class="main-card-content">
					<text class="main-card-icon">📅</text>
					<view class="main-card-text">
						<text class="main-card-title">线上挂号预约</text>
						<text class="main-card-desc">快速预约，无需排队</text>
					</view>
				</view>
				<text class="main-card-arrow">›</text>
			</view>

			<!-- 四宫格导航 -->
			<view class="nav-grid">
				<view class="nav-item" @click="goToDepartments">
					<view class="nav-icon-box">
						<text class="nav-icon">🏥</text>
					</view>
					<text class="nav-text">按科室</text>
				</view>
				<view class="nav-item" @click="goToDoctors">
					<view class="nav-icon-box">
						<text class="nav-icon">👨‍⚕️</text>
					</view>
					<text class="nav-text">找医生</text>
				</view>
				<view class="nav-item" @click="goToMyAppointments">
					<view class="nav-icon-box">
						<text class="nav-icon">📋</text>
					</view>
					<text class="nav-text">我的预约</text>
				</view>
				<view class="nav-item" @click="goToMySubstitute">
					<view class="nav-icon-box">
						<text class="nav-icon">⏰</text>
					</view>
					<text class="nav-text">我的候补</text>
				</view>
			</view>
		</view>

		<!-- 就诊提醒 -->
		<view class="reminder-card" v-if="hasReminder">
			<view class="reminder-header">
				<text class="reminder-icon">🔔</text>
				<text class="reminder-title">就诊提醒</text>
			</view>
			<view class="reminder-content">
				<text class="reminder-text">您有一条预约，明天上午9:00 呼吸内科 李文华医生</text>
			</view>
		</view>

		<!-- 猜你想问 -->
		<view class="faq-section">
			<view class="section-header">
				<text class="section-title">猜你想问</text>
				<text class="section-more" @click="goToQA">更多 ›</text>
			</view>
			<view class="faq-list">
				<view class="faq-item" v-for="(item, index) in faqList" :key="index" @click="goToFAQDetail(item)">
					<text class="faq-q">Q</text>
					<text class="faq-question">{{ item.question }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			hasReminder: false,
			faqList: [
				{ question: '如何预约挂号？', answer: '...' },
				{ question: '挂号费用如何计算？', answer: '...' },
				{ question: '可以取消预约吗？', answer: '...' },
				{ question: '候补功能怎么使用？', answer: '...' }
			]
		};
	},
	computed: {
		isLoggedIn() {
			return !!this.$store.state.user.token;
		},
		userName() {
			const userInfo = this.$store.state.user.userInfo;
			return userInfo.username || '游客';
		},
		greeting() {
			const hour = new Date().getHours();
			if (hour < 12) return '早上好';
			if (hour < 18) return '下午好';
			return '晚上好';
		}
	},
	onLoad() {
		// 不强制弹窗，允许浏览
	},
	methods: {
		goToQA() {
			uni.navigateTo({ url: '/pkg-helper/qa-robot/qa-robot' });
		},
		goToSearch() {
			uni.showToast({ title: '搜索功能开发中', icon: 'none' });
		},
		checkLoginAndGo(url, message) {
			if (!this.isLoggedIn) {
				uni.showModal({
					title: '提示',
					content: '请先登录',
					confirmText: '去登录',
					success: (res) => {
						if (res.confirm) {
							uni.navigateTo({ url: '/pages/login/login' });
						}
					}
				});
				return false;
			}
			if (url) {
				uni.navigateTo({ url });
			} else if (message) {
				uni.showToast({ title: message, icon: 'none' });
			}
			return true;
		},
		goToAppointment() {
			this.checkLoginAndGo('', '预约功能开发中');
		},
		goToDepartments() {
			this.checkLoginAndGo('', '科室列表开发中');
		},
		goToDoctors() {
			this.checkLoginAndGo('', '医生列表开发中');
		},
		goToMyAppointments() {
			this.checkLoginAndGo('', '我的预约开发中');
		},
		goToMySubstitute() {
			this.checkLoginAndGo('/pkg-user/my-substitute/my-substitute');
		},
		goToFAQDetail(item) {
			uni.showToast({ title: item.question, icon: 'none' });
		},
		goToLogin() {
			uni.navigateTo({ url: '/pages/login/login' });
		}
	}
};
</script>

<style scoped>
.home-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

/* 顶部区域 */
.header {
	height: 200rpx;
	background: linear-gradient(135deg, #1976d2 0%, #2196f3 100%);
	padding: 40rpx 30rpx 20rpx;
}
.greeting {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.greeting-text {
	font-size: 32rpx;
	color: #fff;
	font-weight: 600;
}
.qa-entry {
	display: flex;
	align-items: center;
	gap: 8rpx;
	background: rgba(255, 255, 255, 0.2);
	padding: 12rpx 24rpx;
	border-radius: 30rpx;
}
.qa-icon {
	font-size: 28rpx;
}
.qa-text {
	font-size: 24rpx;
	color: #fff;
}
.login-entry {
	display: flex;
	align-items: center;
	background: rgba(255, 255, 255, 0.95);
	padding: 12rpx 24rpx;
	border-radius: 30rpx;
}
.login-text {
	font-size: 24rpx;
	color: #1976d2;
	font-weight: 600;
}

/* 搜索框 */
.search-container {
	padding: 0 35rpx;
	margin-top: -40rpx;
	position: relative;
	z-index: 10;
}
.search-box {
	width: 680rpx;
	height: 80rpx;
	background: #fff;
	border-radius: 40rpx;
	box-shadow: 0 4rpx 20rpx rgba(25, 118, 210, 0.15);
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	gap: 15rpx;
}
.search-icon {
	font-size: 32rpx;
	color: #999;
}
.search-placeholder {
	font-size: 28rpx;
	color: #999;
}

/* 核心功能区 */
.main-section {
	margin: 30rpx;
}
.main-card {
	background: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 8rpx 30rpx rgba(30, 136, 229, 0.25);
	margin-bottom: 30rpx;
}
.main-card-content {
	display: flex;
	align-items: center;
	gap: 20rpx;
}
.main-card-icon {
	font-size: 56rpx;
}
.main-card-text {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}
.main-card-title {
	font-size: 34rpx;
	color: #fff;
	font-weight: 600;
}
.main-card-desc {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.9);
}
.main-card-arrow {
	font-size: 60rpx;
	color: #fff;
	font-weight: 300;
}

/* 四宫格导航 */
.nav-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 20rpx;
}
.nav-item {
	background: #fff;
	border-radius: 15rpx;
	padding: 30rpx 10rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 15rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.nav-icon-box {
	width: 88rpx;
	height: 88rpx;
	background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}
.nav-icon {
	font-size: 44rpx;
}
.nav-text {
	font-size: 24rpx;
	color: #333;
}

/* 就诊提醒 */
.reminder-card {
	margin: 30rpx;
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	border-left: 6rpx solid #ff9800;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.reminder-header {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 15rpx;
}
.reminder-icon {
	font-size: 28rpx;
}
.reminder-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}
.reminder-content {
	
}
.reminder-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}

/* 猜你想问 */
.faq-section {
	margin: 30rpx;
}
.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}
.section-title {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
}
.section-more {
	font-size: 24rpx;
	color: #1976d2;
}
.faq-list {
	
}
.faq-item {
	background: #fff;
	border-radius: 15rpx;
	padding: 25rpx 30rpx;
	margin-bottom: 15rpx;
	display: flex;
	align-items: center;
	gap: 15rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.faq-q {
	width: 48rpx;
	height: 48rpx;
	background: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
	color: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	font-weight: 600;
	flex-shrink: 0;
}
.faq-question {
	font-size: 26rpx;
	color: #333;
	flex: 1;
}
</style>
