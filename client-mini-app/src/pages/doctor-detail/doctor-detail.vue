<template>
	<view class="doctor-detail-page">
		<!-- 医生信息卡片 -->
		<view class="doctor-info-card">
			<view class="doctor-header">
				<view class="doctor-avatar-large">
					<text class="avatar-text">{{ doctorInfo.name ? doctorInfo.name.substring(0, 1) : '' }}</text>
				</view>
				<view class="doctor-basic">
					<view class="name-row">
						<text class="doctor-name">{{ doctorInfo.name }}</text>
						<text class="doctor-title" :class="getTitleClass(doctorInfo.title)">{{ doctorInfo.title }}</text>
					</view>
					<text class="doctor-department" v-if="doctorInfo.clinic">{{ doctorInfo.clinic.name }}</text>
				</view>
			</view>
			
			<view class="doctor-specialty" v-if="doctorInfo.specialty">
				<view class="specialty-header">
					<text class="specialty-icon">✨</text>
					<text class="specialty-title">擅长领域</text>
				</view>
				<text class="specialty-content">{{ doctorInfo.specialty }}</text>
			</view>

			<view class="doctor-stats">
				<view class="stat-item">
					<text class="stat-value">{{ getRegistrationFee() }}</text>
					<text class="stat-label">挂号费(元)</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item">
					<text class="stat-value">{{ doctorInfo.appointmentCount || 0 }}</text>
					<text class="stat-label">累计预约</text>
				</view>
				<view class="stat-divider"></view>
				<view class="stat-item">
					<text class="stat-value">{{ calculateSatisfaction() }}%</text>
					<text class="stat-label">满意度</text>
				</view>
			</view>
		</view>

		<!-- 医保报销提示 -->
		<view class="insurance-tip">
			<text class="tip-icon">💰</text>
			<text class="tip-text">学生报销95%，教师报销90%</text>
		</view>

		<!-- 排班信息 -->
		<view class="schedule-section">
			<view class="section-header">
				<text class="section-title">选择就诊时间</text>
			</view>

			<!-- 日期选择 -->
			<scroll-view scroll-x class="date-scroll">
				<view class="date-list">
					<view 
						class="date-item" 
						:class="{
							active: selectedDate === item.date && item.hasSchedule,
							disabled: !item.hasSchedule
						}" 
						v-for="item in dateList" 
						:key="item.date" 
						@click="selectDate(item)"
					>
						<text class="date-week">{{ item.week }}</text>
						<text class="date-day">{{ item.day }}</text>
						<text 
							class="date-status" 
							:class="{
								active: selectedDate === item.date && item.hasSchedule,
								disabled: !item.hasSchedule
							}"
						>
							{{ item.hasSchedule ? '坐诊' : '停诊' }}
						</text>
					</view>
				</view>
			</scroll-view>

			<!-- 时间段选择 -->
			<view class="time-slots">
				<view class="time-period" v-for="period in ['morning', 'afternoon']" :key="period">
					<view class="period-header">
						<text class="period-icon">{{ period === 'morning' ? '🌅' : '🌆' }}</text>
						<text class="period-name">{{ period === 'morning' ? '上午' : '下午' }}</text>
					</view>
					<view class="slot-list">
						<view 
							class="slot-item" 
							:class="getSlotClass(slot)" 
							v-for="slot in getSlotsByPeriod(period)" 
							:key="slot.id" 
							@click="selectSlot(slot)"
						>
							<text class="slot-time">{{ slot.startTime }}-{{ slot.endTime }}</text>
							<text class="slot-status">{{ getSlotStatusText(slot) }}</text>
							<!-- 当号源为0时显示候补按钮 -->
							<view class="waitlist-overlay" v-if="slot.availableSlots === 0" @click.stop="joinWaitlist(slot)">
								<button class="waitlist-btn">候补排队</button>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="no-schedule" v-if="schedules.length === 0 && !loading">
				<text class="no-schedule-icon">📅</text>
				<text class="no-schedule-text">该日期暂无排班</text>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="price-info">
				<view class="price-row">
					<text class="price-label">挂号费：</text>
					<text class="price-original">¥{{ getRegistrationFee() }}</text>
				</view>
				<view class="price-row" v-if="userIdentity">
					<text class="price-label">实付：</text>
					<text class="price-actual">¥{{ actualPrice }}</text>
				</view>
			</view>
			<button class="appointment-btn" :disabled="!selectedSlot" @click="handleAppointment" v-if="selectedSlot && selectedSlot.availableSlots > 0">
				立即挂号
			</button>
			<button class="waitlist-bottom-btn" :disabled="!selectedSlot" @click="joinWaitlist(selectedSlot)" v-else-if="selectedSlot && selectedSlot.availableSlots === 0">
				候补排队
			</button>
			<button class="appointment-btn" disabled v-else>
				请选择时间
			</button>
		</view>

		<!-- 加载状态 -->
		<view class="loading-mask" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getDoctorById, getDoctorSchedules } from '@/api/doctor.js';
import { joinWaitlist } from '@/api/waitlist.js';
import { promptLogin } from '@/utils/auth.js';

export default {
	data() {
		return {
			doctorId: null,
			doctorInfo: {},
			schedules: [],
			selectedDate: '',
			selectedSlot: null,
			dateList: [],
			loading: false
		};
	},
	computed: {
		// 获取用户身份
		userIdentity() {
			const userInfo = this.$store.state.user.userInfo;
			// 从个人信息中获取身份类型
			return userInfo.identityType || null; // 'student' or 'teacher'
		},
		
		// 计算实际支付价格
		actualPrice() {
			const fee = this.getRegistrationFee();
			if (this.userIdentity === 'student') {
				return (fee * 0.05).toFixed(2); // 学生报销95%
			} else if (this.userIdentity === 'teacher') {
				return (fee * 0.1).toFixed(2); // 教师报销90%
			}
			return fee.toFixed(2);
		}
	},
	onLoad(options) {
		if (options.doctorId) {
			this.doctorId = options.doctorId;
			this.initDateList();
			this.loadDoctorInfo();
		}
	},
	methods: {
		// 初始化日期列表（未来7天）
		initDateList() {
			const dates = [];
			const weeks = ['日', '一', '二', '三', '四', '五', '六'];
			for (let i = 0; i < 7; i++) {
				const date = new Date();
				date.setDate(date.getDate() + i);
				const dateStr = date.toISOString().split('T')[0];
				dates.push({
					date: dateStr,
					week: i === 0 ? '今天' : `周${weeks[date.getDay()]}`,
					day: `${date.getMonth() + 1}/${date.getDate()}`,
					hasSchedule: false
				});
			}
			this.dateList = dates;
			this.selectedDate = dates[0].date;
		},

		// 加载医生信息
		async loadDoctorInfo() {
			this.loading = true;
			try {
				const data = await getDoctorById(this.doctorId);
				this.doctorInfo = data;
				await this.loadSchedules(); // 加载完医生信息后加载排班
			} catch (error) {
				console.error('加载医生信息失败:', error);
				uni.showToast({
					title: error.msg || '加载失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		},

		// 加载排班信息
		async loadSchedules() {
			try {
				const startDate = this.dateList[0].date;
				const endDate = this.dateList[this.dateList.length - 1].date;
				const data = await getDoctorSchedules(this.doctorId, startDate, endDate);
				this.schedules = data;
				this.updateDateScheduleStatus();
			} catch (error) {
				console.error('加载排班信息失败:', error);
			}
		},

		// 更新日期栏的坐诊状态
		updateDateScheduleStatus() {
			const dateMap = new Map();
			this.schedules.forEach(schedule => {
				if (!dateMap.has(schedule.date)) {
					dateMap.set(schedule.date, false);
				}
				if (schedule.status !== 'unavailable') {
					dateMap.set(schedule.date, true);
				}
			});

			this.dateList = this.dateList.map(item => ({
				...item,
				hasSchedule: dateMap.get(item.date) || false
			}));

			// 如果当前选中日期无排班，则自动切换到第一个有排班的日期
			const currentSelected = this.dateList.find(item => item.date === this.selectedDate);
			if (!currentSelected || !currentSelected.hasSchedule) {
				const firstAvailable = this.dateList.find(item => item.hasSchedule);
				if (firstAvailable) {
					this.selectedDate = firstAvailable.date;
				}
				this.selectedSlot = null; // 重置时间段选择
			}
		},

		// 选择日期
		selectDate(item) {
			if (!item.hasSchedule) {
				uni.showToast({ title: '该日期医生未坐诊', icon: 'none' });
				return;
			}
			if (this.selectedDate === item.date) {
				return; // 如果点击的是当前已选中的日期，则不执行任何操作
			}
			this.selectedDate = item.date;
			this.selectedSlot = null; // 切换日期时清空已选时间段
		},

		// 根据时段（上午/下午）获取号源
		getSlotsByPeriod(period) {
			return this.schedules.filter(schedule => {
				return schedule.date === this.selectedDate && schedule.period === period;
			});
		},

		// 选择时间段
		selectSlot(slot) {
			this.selectedSlot = slot;
		},

		// 获取时间段的样式
		getSlotClass(slot) {
			const classes = [];
			if (this.selectedSlot && this.selectedSlot.id === slot.id) {
				classes.push('active');
			}
			if (slot.availableSlots === 0) {
				classes.push('full');
			}
			if (slot.status === 'unavailable') {
				classes.push('unavailable');
			}
			return classes.join(' ');
		},

		// 获取时间段状态文本
		getSlotStatusText(slot) {
			if (slot.availableSlots > 0) {
				return `余${slot.availableSlots}`;
			} else if (slot.availableSlots === 0) {
				return '约满';
			} else {
				return '停诊';
			}
		},

		// 获取职称样式
		getTitleClass(title) {
			if (title === '主任医师') return 'senior';
			if (title === '副主任医师') return 'associate';
			return '';
		},

		// 获取挂号费
		getRegistrationFee() {
			if (!this.doctorInfo.title) return 0;
			if (this.doctorInfo.title === '主任医师') return 50;
			if (this.doctorInfo.title === '副主任医师') return 30;
			if (this.doctorInfo.title === '主治医师') return 20;
			return 15;
		},

		// 计算满意度（临时）
		calculateSatisfaction() {
			return this.doctorInfo.satisfactionRate || 95; // 默认95%
		},

		// 处理挂号
		handleAppointment() {
			if (!this.$store.state.user.token) {
				promptLogin();
				return;
			}
			if (!this.selectedSlot) {
				uni.showToast({ title: '请选择就诊时间', icon: 'none' });
				return;
			}
			if (this.selectedSlot.availableSlots === 0) {
				uni.showToast({ title: '号源已满，请选择候补', icon: 'none' });
				return;
			}
			uni.navigateTo({
				url: `/pkg-order/order-confirm/order-confirm?doctorId=${this.doctorId}&scheduleId=${this.selectedSlot.id}`
			});
		},

		// 加入候补
		async joinWaitlist(slot) {
			if (!slot) {
				uni.showToast({ title: '请选择号源', icon: 'none' });
				return;
			}
			const token = this.$store.state.user.token;
			console.log('Token:', token);
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				setTimeout(() => {
					uni.navigateTo({ url: '/pages/login/login' });
				}, 1000);
				return;
			}
			uni.showModal({
				title: '候补排队',
				content: `确定要加入"${this.doctorInfo.name}"医生${this.formatDate(slot.date)}${this.getPeriodName(slot.period)}的候补队列吗？`,
				confirmText: '确认加入',
				success: async (res) => {
					if (res.confirm) {
						try {
							await joinWaitlist({ scheduleId: slot.id });
							uni.showToast({
								title: '已加入候补队列',
								icon: 'success',
								success: () => {
									uni.navigateTo({
										url: `/pages/waitlist/waitlist?scheduleId=${slot.id}&doctorId=${this.doctorId}&scheduleDate=${slot.date}&timeSlot=${slot.period}`
									});
								}
							});
						} catch (e) {
							uni.showToast({ title: e.msg || '加入失败', icon: 'none' });
						}
					}
				}
			});
		},

		// 获取时段名称
		getPeriodName(period) {
			if (period === 'morning') return '上午';
			if (period === 'afternoon') return '下午';
			return period;
		},

		// 格式化日期
		formatDate(dateStr) {
			if (!dateStr) return '';
			const date = new Date(dateStr);
			return `${date.getMonth() + 1}月${date.getDate()}日`;
		}}};</script>

<style scoped>
.doctor-detail-page {
	min-height: 100vh;
	background-color: #f7f8fa; /* 使用更柔和的背景色 */
	padding: 20rpx 0 180rpx; /* 增加上下和底部安全距离 */
}

/* 医生信息卡片 */
.doctor-info-card {
	background: #ffffff;
	margin: 0 30rpx 20rpx; /* 左右留边距 */
	border-radius: 20rpx; /* 增加圆角 */
	padding: 40rpx 30rpx;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.05); /* 添加柔和阴影 */
}
.doctor-header {
	display: flex;
	align-items: center;
	gap: 25rpx;
	margin-bottom: 30rpx;
	padding-bottom: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}
.doctor-avatar-large {
	width: 120rpx;
	height: 120rpx;
	background: linear-gradient(135deg, #5c9eff 0%, #3a7afe 100%); /* 更新渐变色 */
	border-radius: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	box-shadow: 0 8rpx 20rpx rgba(58, 122, 254, 0.35); /* 匹配新颜色的阴影 */
}
.avatar-text {
	font-size: 50rpx;
	color: #fff;
	font-weight: 700;
}
.doctor-basic {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}
.name-row {
	display: flex;
	align-items: center;
	gap: 15rpx;
}
.doctor-name {
	font-size: 40rpx; /* 加大字号 */
	color: #1f2329; /* 加深颜色 */
	font-weight: 600;
}
.doctor-title {
	font-size: 22rpx;
	color: #3a7afe;
	background: #eef3ff;
	padding: 6rpx 16rpx;
	border-radius: 12rpx;
	font-weight: 500; /* 增加一点字重 */
}
.doctor-title.senior {
	color: #d32f2f;
	background: #ffebee;
}
.doctor-title.associate {
	color: #f57c00;
	background: #fff3e0;
}
.doctor-department {
	font-size: 28rpx; /* 统一字号 */
	color: #6c757d; /* 使用更柔和的灰色 */
}

/* 擅长领域 */
.doctor-specialty {
	margin-bottom: 30rpx;
	padding: 25rpx;
	background-color: #f7f8fa; /* 浅灰色背景 */
	border-radius: 15rpx;
}
.specialty-header {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 15rpx;
}
.specialty-icon {
	font-size: 28rpx;
}
.specialty-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}
.specialty-content {
	font-size: 26rpx;
	color: #666;
	line-height: 1.8;
	display: block;
}

/* 统计信息 */
.doctor-stats {
	display: flex;
	justify-content: space-around;
	padding: 20rpx 0;
	margin-top: 10rpx; /* 与上方内容增加间距 */
}
.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10rpx;
}
.stat-value {
	font-size: 38rpx; /* 加大字号 */
	color: #1f2329; /* 加深颜色 */
	font-weight: 700;
}
.stat-label {
	font-size: 24rpx;
	color: #999;
}
.stat-divider {
	width: 1rpx;
	background: #e0e0e0;
	align-self: stretch; /* 让分割线撑满高度 */
}

/* 医保提示 */
.insurance-tip {
	background: #eef3ff;
	margin: 0 30rpx 20rpx;
	padding: 20rpx 30rpx;
	border-radius: 15rpx;
	display: flex;
	align-items: center;
	gap: 15rpx;
	border: 1rpx solid #dbeaff;
}
.tip-icon {
	font-size: 32rpx;
}
.tip-text {
	font-size: 26rpx;
	color: #3a7afe;
	font-weight: 500;
}

/* 排班信息 */
.schedule-section {
	background: #ffffff;
	margin: 0 30rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.05);
}
.section-header {
	margin-bottom: 25rpx;
}
.section-title {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
}

/* 日期选择 */
.date-scroll {
	width: 100%;
	white-space: nowrap;
	margin-bottom: 30rpx;
}
.date-list {
	display: inline-flex;
	gap: 15rpx;
}
.date-item {
	width: 140rpx;
	padding: 20rpx 0;
	background: #f7f8fa;
	border: 1rpx solid #e5e6e7;
	border-radius: 15rpx;
	display: inline-flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
	flex-shrink: 0;
	transition: all 0.3s;
}
.date-item.active {
	background: linear-gradient(135deg, #5c9eff 0%, #3a7afe 100%);
	border-color: transparent;
}
.date-item.disabled {
	background: #f2f3f5;
	opacity: 0.6;
}
.date-week {
	font-size: 24rpx;
	color: #6c757d;
}
.date-item.active .date-week {
	color: rgba(255, 255, 255, 0.9);
}
.date-item.disabled .date-week {
	color: #b0b3b8;
}
.date-day {
	font-size: 28rpx;
	color: #1f2329;
	font-weight: 600;
}
.date-item.active .date-day {
	color: #fff;
}
.date-item.disabled .date-day {
	color: #b0b3b8;
}
.date-status {
	font-size: 20rpx;
	color: #4caf50;
}
.date-status.active {
	color: rgba(255, 255, 255, 0.9);
}
.date-status.disabled {
	color: #b0b3b8;
}

/* 时间段 */
.time-period {
	margin-bottom: 30rpx;
}
.time-period:last-child {
	margin-bottom: 0;
}
.period-header {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 20rpx;
}
.period-icon {
	font-size: 28rpx;
}
.period-name {
	font-size: 28rpx; /* 加大字号 */
	color: #1f2329;
	font-weight: 600;
}
.slot-list {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 15rpx;
}
.slot-item {
	height: 100rpx;
	background: #f7f8fa;
	border: 1rpx solid #e5e6e7;
	border-radius: 12rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	transition: all 0.3s;
	position: relative;
}
.slot-item.active {
	background: #eef3ff;
	border-color: #3a7afe;
}
.slot-item.full {
	background: #f2f3f5;
	opacity: 0.8;
}
.slot-item.unavailable {
	background: #f2f3f5;
	opacity: 0.4;
}
.slot-time {
	font-size: 24rpx;
	color: #333;
}
.slot-status {
	font-size: 20rpx;
	color: #4caf50;
}
.slot-item.full .slot-status,
.slot-item.unavailable .slot-status {
	color: #b0b3b8;
}

/* 候补按钮覆盖层 */
.waitlist-overlay {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(255, 255, 255, 0.5); /* 使用半透明白色遮罩 */
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.waitlist-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	color: #fff;
	border: none;
	border-radius: 30rpx;
	padding: 10rpx 20rpx;
	font-size: 24rpx;
	font-weight: 600;
	/* 移除uni-app按钮默认样式 */
	line-height: 1.5;
	margin: 0;
}

/* 无排班 */
.no-schedule {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 0;
	gap: 20rpx;
}
.no-schedule-icon {
	font-size: 80rpx;
	opacity: 0.3;
}
.no-schedule-text {
	font-size: 26rpx;
	color: #999;
}

/* 底部操作栏 */
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #ffffff;
	padding: 20rpx 30rpx calc(20rpx + env(safe-area-inset-bottom)); /* 适配iPhone X等机型 */
	border-top: 1rpx solid #f0f0f0;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
	display: flex;
	align-items: center;
	gap: 20rpx;
	z-index: 100;
}
.price-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}
.price-row {
	display: flex;
	align-items: baseline;
	gap: 8rpx;
}
.price-label {
	font-size: 24rpx;
	color: #999;
}
.price-original {
	font-size: 24rpx;
	color: #999;
	text-decoration: line-through;
}
.price-actual {
	font-size: 38rpx; /* 加大字号 */
	color: #ff5722;
	font-weight: 700;
}
.appointment-btn,
.waitlist-bottom-btn {
	width: 280rpx; /* 增加宽度 */
	height: 88rpx; /* 增加高度 */
	background: linear-gradient(135deg, #5c9eff 0%, #3a7afe 100%);
	color: #fff;
	border: none;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(58, 122, 254, 0.35);
}
.waitlist-bottom-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	box-shadow: 0 8rpx 20rpx rgba(255, 152, 0, 0.35);
}
.appointment-btn::after,
.waitlist-bottom-btn::after {
	border: none;
}
.appointment-btn[disabled],
.waitlist-bottom-btn[disabled] {
	background: #ccc;
	box-shadow: none;
	opacity: 0.8;
}

/* 加载遮罩 */
.loading-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(255, 255, 255, 0.9);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}
.loading-text {
	font-size: 28rpx;
	color: #999;
}
</style>
