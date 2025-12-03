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
					<text class="nav-text">找门诊</text>
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
		<view class="reminder-card" v-if="reminderInfo">
			<view class="reminder-header">
				<text class="reminder-icon">🔔</text>
				<text class="reminder-title">就诊提醒</text>
			</view>
			<view class="reminder-content">
				<text class="reminder-text">{{ reminderText }}</text>
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
import { promptLogin } from '@/utils/auth.js';
import { getMyAppointments } from '@/api/appointment.js';

export default {
	data() {
		return {
			reminderInfo: null,
			loadingReminder: false,
			faqList: [
				{
					question: '如何预约挂号？',
					answer: '在“线上挂号预约”中选择科室或门诊，挑选医生与时间段后提交即可。提交成功会收到预约凭证。'
				},
				{
					question: '挂号费用如何计算？',
					answer: '费用与医生职称、号别相关，学生报销95%、教师报销90%，报销后差额自动抵扣。'
				},
				{
					question: '可以取消预约吗？',
					answer: '就诊前2小时内可以在“我的-就诊记录”里退号，费用原路退回。就诊后或已取号无法退。'
				},
				{
					question: '候补功能怎么使用？',
					answer: '号源满时可点击“候补排队”，支付预付金后排队。放号成功会自动为您预约并通知。'
				}
			]
		};
	},
	computed: {
		isLoggedIn() {
			// 使用可选链操作符 ?. 来安全地访问深层嵌套的属性
			const token = this.$store.state.user?.token;
			const userId = this.$store.state.user?.userInfo?.userId;
			return !!token && !!userId;
		},
		userName() {
			// 使用可选链操作符 ?. 来安全地访问深层嵌套的属性，避免因 userInfo 不存在而报错
			return this.$store.state.user?.userInfo?.username || '游客';
		},
		greeting() {
			const hour = new Date().getHours();
			if (hour < 12) return '早上好';
			if (hour < 18) return '下午好';
			return '晚上好';
		},
		reminderText() {
			if (!this.reminderInfo) return '';
			const parts = ['您有一条预约', this.reminderInfo.dateText, this.reminderInfo.slotText, this.reminderInfo.clinicName, this.reminderInfo.doctorName]
				.filter(Boolean);
			return parts.join(' ');
		}
	},
	onLoad() {
		// 不强制弹窗，允许浏览
	},
	onShow() {
		this.loadReminder();
	},
	onPullDownRefresh() {
		this.handlePullDownRefresh();
	},
	methods: {
		async handlePullDownRefresh() {
			try {
				await this.loadReminder();
			} finally {
				uni.stopPullDownRefresh();
			}
		},
		guardedNavigate(url) {
			if (!this.isLoggedIn) {
				promptLogin();
				return;
			}
			uni.navigateTo({ url });
		},
		goToQA() {
			this.guardedNavigate('/pages/ai-chat/ai-chat');
		},
		goToSearch() {
			this.guardedNavigate('/pages/search/search');
		},
		checkLoginAndGo(url, message) {
			if (!this.isLoggedIn) {
				promptLogin();
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
			// 线上挂号入口（需要登录）- 进入科室列表
			this.guardedNavigate('/pages/department-list/department-list');
		},
		goToDepartments() {
			// 找门诊入口（需要登录）- 进入科室列表
			this.guardedNavigate('/pages/department-list/department-list');
		},
		goToDoctors() {
			// 直接查看所有医生（需要登录）
			this.guardedNavigate('/pages/doctor-list/doctor-list');
		},
		goToMyAppointments() {
			// 我的预约（需要登录；登录后暂时提示开发中）
			if (!this.isLoggedIn) {
				promptLogin();
				return;
			}
			uni.showToast({ title: '我的预约开发中', icon: 'none' });
		},
		goToMySubstitute() {
			// 我的候补（需要登录）
			this.guardedNavigate('/pkg-user/my-substitute/my-substitute');
		},
		goToFAQDetail(item) {
			uni.showToast({ title: item.question, icon: 'none' });
		},
		goToLogin() {
			uni.navigateTo({ url: '/pages/login/login' });
		},
		async loadReminder() {
			if (!this.isLoggedIn) {
				this.reminderInfo = null;
				return;
			}
			this.loadingReminder = true;
			try {
				const data = await getMyAppointments();
				const list = Array.isArray(data) ? data : ((data && data.list) ? data.list : []);
				const upcoming = list
					.filter(item => this.isUpcomingStatus(item.status))
					.map(item => {
						const date = this.parseDate(item.appointmentTime || item.scheduleDate);
						return {
							raw: item,
							date
						};
					})
					.filter(item => item.date && item.date.getTime() >= Date.now())
					.sort((a, b) => a.date - b.date)[0];
				if (!upcoming) {
					this.reminderInfo = null;
					return;
				}
				const appointment = upcoming.raw;
				const dateText = this.formatReminderDate(upcoming.date);
				const slotText = appointment.timeSlotName || this.mapTimeSlot(appointment.timeSlot);
				const clinicName = appointment.clinicName || appointment.departmentName || appointment.department?.name || '';
				const doctorName = appointment.doctorName || '';
				this.reminderInfo = {
					dateText,
					slotText,
					clinicName,
					doctorName
				};
			} catch (error) {
				console.error('加载预约提醒失败:', error);
				this.reminderInfo = null;
			} finally {
				this.loadingReminder = false;
			}
		},
		isUpcomingStatus(status) {
			const normalized = String(status || '').toUpperCase();
			return normalized === 'PENDING' || normalized === 'CONFIRMED' || normalized === 'SCHEDULED';
		},
		parseDate(value) {
			if (!value) return null;
			const date = new Date(value);
			return Number.isNaN(date.getTime()) ? null : date;
		},
		formatReminderDate(date) {
			if (!date) return '';
			const weekNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			const hour = String(date.getHours()).padStart(2, '0');
			const minute = String(date.getMinutes()).padStart(2, '0');
			return `${month}月${day}日 ${weekNames[date.getDay()]} ${hour}:${minute}`;
		},
		mapTimeSlot(slot) {
			if (!slot) return '';
			const lower = String(slot).toLowerCase();
			if (lower.includes('morning') || lower.includes('上午')) return '上午';
			if (lower.includes('afternoon') || lower.includes('下午')) return '下午';
			if (lower.includes('evening') || lower.includes('晚上')) return '晚上';
			return slot;
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
