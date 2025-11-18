<template>
	<view class="appointment-detail-page">
		<!-- 状态卡片 -->
		<view class="status-card" :class="getStatusClass(appointmentDetail.status)">
			<view class="status-icon">{{ getStatusIcon(appointmentDetail.status) }}</view>
			<text class="status-text">{{ getStatusText(appointmentDetail.status) }}</text>
		</view>

		<!-- 就诊信息卡片 -->
		<view class="info-card">
			<view class="card-title">
				<text class="title-icon">🏥</text>
				<text class="title-text">就诊信息</text>
			</view>
			
			<view class="info-row">
				<text class="info-label">就诊医生</text>
				<text class="info-value">{{ appointmentDetail.doctorName || '张医生' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">医生职称</text>
				<text class="info-value">{{ appointmentDetail.doctorTitle || '主任医师' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">科室/门诊</text>
				<text class="info-value">{{ appointmentDetail.clinicName || '消化内科' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊日期</text>
				<text class="info-value">{{ appointmentDetail.appointmentDate || '2025-11-15' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊时间</text>
				<text class="info-value">{{ appointmentDetail.appointmentTime || '08:00-12:00' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">时间段</text>
				<text class="info-value">{{ appointmentDetail.timeSlot || '上午' }}</text>
			</view>
		</view>

		<!-- 费用信息卡片 -->
		<view class="info-card">
			<view class="card-title">
				<text class="title-icon">💰</text>
				<text class="title-text">费用信息</text>
			</view>
			
			<view class="info-row">
				<text class="info-label">挂号费（原价）</text>
				<text class="info-value original-price">¥{{ appointmentDetail.originalFee || '50.00' }}</text>
			</view>
			<view class="info-row" v-if="appointmentDetail.reimbursementRate">
				<text class="info-label">报销比例</text>
				<text class="info-value">{{ appointmentDetail.reimbursementRate || '95%' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">实付金额</text>
				<text class="info-value actual-price">¥{{ appointmentDetail.actualFee || '2.50' }}</text>
			</view>
		</view>

		<!-- 预约信息卡片 -->
		<view class="info-card">
			<view class="card-title">
				<text class="title-icon">📋</text>
				<text class="title-text">预约信息</text>
			</view>
			
			<view class="info-row">
				<text class="info-label">预约编号</text>
				<text class="info-value">{{ appointmentDetail.appointmentNo || 'APT20251115001' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">预约时间</text>
				<text class="info-value">{{ appointmentDetail.createdAt || '2025-11-14 10:30:00' }}</text>
			</view>
			<view class="info-row" v-if="appointmentDetail.reminderTime">
				<text class="info-label">提醒时间</text>
				<text class="info-value">{{ appointmentDetail.reminderTime || '就诊前一天 18:00' }}</text>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-card">
			<view class="tips-title">💡 温馨提示</view>
			<view class="tips-item">• 请于就诊前2小时内勿取消，以免影响信用</view>
			<view class="tips-item">• 到诊时请携带身份证件和预约凭证</view>
			<view class="tips-item">• 如需改约，请在就诊前24小时操作</view>
			<view class="tips-item">• 退号后费用将在3-5个工作日内原路退回</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<!-- 待就诊/已确认状态：显示退号和候补按钮 -->
			<template v-if="canCancel">
				<button class="action-btn waitlist-btn" @click="goToWaitlist">
					<text class="btn-icon">⏰</text>
					<text class="btn-text">加入候补</text>
				</button>
				<button class="action-btn cancel-btn" @click="goToCancel">
					<text class="btn-icon">❌</text>
					<text class="btn-text">退号</text>
				</button>
			</template>
			<!-- 其他状态：只显示返回按钮 -->
			<button v-else class="action-btn back-btn" @click="goBack">
				<text class="btn-text">返回</text>
			</button>
		</view>
	</view>
</template>

<script>
// TODO: 引入API
// import { getAppointmentDetail } from '@/api/appointment.js'

export default {
	name: 'AppointmentDetail',
	data() {
		return {
			appointmentId: null,
			// 硬编码的挂号详情数据（实际应从API获取）
			appointmentDetail: {
				id: 1,
				appointmentNo: 'APT20251115001',
				doctorId: 1,
				doctorName: '张医生',
				doctorTitle: '主任医师',
				clinicName: '消化内科',
				appointmentDate: '2025-11-15',
				appointmentTime: '08:00-12:00',
				timeSlot: '上午',
				status: 'CONFIRMED', // PENDING, CONFIRMED, COMPLETED, CANCELLED
				originalFee: '50.00',
				actualFee: '2.50',
				reimbursementRate: '95%',
				createdAt: '2025-11-14 10:30:00',
				reminderTime: '就诊前一天 18:00'
			}
		};
	},
	computed: {
		// 是否可以取消（待就诊或已确认状态）
		canCancel() {
			const status = this.appointmentDetail.status;
			return status === 'PENDING' || status === 'CONFIRMED';
		}
	},
	onLoad(options) {
		if (options.id) {
			this.appointmentId = options.id;
			// TODO: 调用API获取挂号详情
			// this.loadAppointmentDetail();
		}
		// 如果有传入的数据，可以合并
		if (options.status) {
			this.appointmentDetail.status = options.status;
		}
	},
	methods: {
		// TODO: 加载挂号详情
		// async loadAppointmentDetail() {
		// 	try {
		// 		const data = await getAppointmentDetail(this.appointmentId);
		// 		this.appointmentDetail = data;
		// 	} catch (e) {
		// 		uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
		// 	}
		// },

		// 获取状态文本
		getStatusText(status) {
			const statusMap = {
				'PENDING': '待就诊',
				'CONFIRMED': '已确认',
				'COMPLETED': '已完成',
				'CANCELLED': '已取消'
			};
			return statusMap[status] || '未知状态';
		},

		// 获取状态图标
		getStatusIcon(status) {
			const iconMap = {
				'PENDING': '⏳',
				'CONFIRMED': '✅',
				'COMPLETED': '✔️',
				'CANCELLED': '❌'
			};
			return iconMap[status] || '❓';
		},

		// 获取状态样式类
		getStatusClass(status) {
			const classMap = {
				'PENDING': 'status-pending',
				'CONFIRMED': 'status-confirmed',
				'COMPLETED': 'status-completed',
				'CANCELLED': 'status-cancelled'
			};
			return classMap[status] || '';
		},

		// 跳转到退号页面
		goToCancel() {
			uni.navigateTo({
				url: `/pages/cancel-appointment/cancel-appointment?id=${this.appointmentId || this.appointmentDetail.id}`
			});
		},

		// 跳转到候补页面
		goToWaitlist() {
			uni.navigateTo({
				url: `/pages/waitlist/waitlist?appointmentId=${this.appointmentId || this.appointmentDetail.id}&doctorId=${this.appointmentDetail.doctorId}&scheduleDate=${this.appointmentDetail.appointmentDate}&timeSlot=${this.appointmentDetail.timeSlot}`
			});
		},

		// 返回
		goBack() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped>
.appointment-detail-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 160rpx;
}

/* 状态卡片 */
.status-card {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	margin: 30rpx;
	border-radius: 20rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	color: #fff;
}

.status-card.status-pending {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.status-card.status-confirmed {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
}

.status-card.status-completed {
	background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
}

.status-card.status-cancelled {
	background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
}

.status-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.status-text {
	font-size: 36rpx;
	font-weight: 600;
}

/* 信息卡片 */
.info-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.card-title {
	display: flex;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.title-icon {
	font-size: 32rpx;
	margin-right: 12rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.info-row:last-child {
	border-bottom: none;
}

.info-label {
	font-size: 28rpx;
	color: #666;
}

.info-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.original-price {
	color: #999;
	text-decoration: line-through;
}

.actual-price {
	color: #ff5722;
	font-weight: 600;
	font-size: 32rpx;
}

/* 温馨提示卡片 */
.tips-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	background: linear-gradient(135deg, #fff9e6 0%, #fffbf0 100%);
	border: 2rpx solid #ffe082;
}

.tips-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #f57c00;
	margin-bottom: 20rpx;
}

.tips-item {
	font-size: 26rpx;
	color: #666;
	line-height: 1.8;
	margin-bottom: 12rpx;
}

.tips-item:last-child {
	margin-bottom: 0;
}

/* 底部操作栏 */
.bottom-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
	display: flex;
	gap: 20rpx;
	z-index: 100;
}

.action-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	font-weight: 600;
	border: none;
}

.waitlist-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	color: #fff;
}

.cancel-btn {
	background: linear-gradient(135deg, #f44336 0%, #e57373 100%);
	color: #fff;
}

.back-btn {
	background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
	color: #fff;
}

.btn-icon {
	margin-right: 8rpx;
	font-size: 32rpx;
}

.btn-text {
	font-size: 30rpx;
}
</style>


