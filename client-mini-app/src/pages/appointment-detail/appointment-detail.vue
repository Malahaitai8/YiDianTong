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
				<text class="info-value">{{ appointmentDetail.timeSlotDisplay || '上午' }}</text>
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
			<!-- 待就诊/已确认状态：仅显示退号按钮；加入候补不应出现在预约详情页 -->
			<template v-if="canCancel">
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
import { getMyAppointments, cancelAppointment } from '@/api/appointment.js'

export default {
	name: 'AppointmentDetail',
	data() {
		return {
			appointmentId: null,
			// 挂号详情数据
			appointmentDetail: {
				id: null,
				appointmentNo: '',
				doctorId: null,
				doctorName: '',
				doctorTitle: '',
				clinicName: '',
				appointmentDate: '',
				appointmentTime: '',
				timeSlot: '',
				timeSlotDisplay: '',
				status: 'pending', // Changed from 'PENDING' to 'pending' to match backend
				originalFee: '0.00',
				actualFee: '0.00',
				reimbursementRate: '',
				createdAt: '',
				reminderTime: ''
			}
		};
	},
	computed: {
		// 是否可以取消（待就诊或已确认状态）
		canCancel() {
			const status = this.appointmentDetail.status;
			return status === 'PENDING' || status === 'pending' || 
				   status === 'CONFIRMED' || status === 'confirmed' ||
				   status === 'SCHEDULED' || status === 'scheduled';
		}
	},
	onLoad(options) {
		console.log('挂号详情页面参数:', options);
		
		if (options.id) {
			this.appointmentId = parseInt(options.id);
			// 调用API获取挂号详情
			this.loadAppointmentDetail();
		}
		
		// 如果有传入的数据，可以合并
		if (options.status) {
			this.appointmentDetail.status = options.status;
		}
	},
	methods: {
		// 加载挂号详情
		async loadAppointmentDetail() {
			try {
				// 获取我的所有预约
				const data = await getMyAppointments();
				const appointments = Array.isArray(data) ? data : (data.list || []);
				
				// 查找当前预约
				const appointment = appointments.find(item => item.id === this.appointmentId);
				if (appointment) {
					this.appointmentDetail = {
						...this.appointmentDetail,
						...appointment
					};
					
					// 处理时间段显示
					const timeSlotMap = {
						'morning': '上午',
						'afternoon': '下午',
						'evening': '晚上'
					};
					this.appointmentDetail.timeSlotDisplay = timeSlotMap[appointment.timeSlot] || appointment.timeSlot;
				} else {
					uni.showToast({ title: '未找到预约信息', icon: 'none' });
				}
			} catch (e) {
				console.error('加载挂号详情失败:', e);
				uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
			}
		},

		// 获取状态文本
		getStatusText(status) {
			const statusMap = {
				'PENDING': '待就诊',
				'pending': '待就诊',
				'CONFIRMED': '已确认',
				'confirmed': '已确认',
				'SCHEDULED': '已确认',
				'scheduled': '已确认',
				'COMPLETED': '已完成',
				'completed': '已完成',
				'CANCELLED': '已取消',
				'cancelled': '已取消'
			};
			return statusMap[status] || '未知状态';
		},

		// 获取状态图标
		getStatusIcon(status) {
			const iconMap = {
				'PENDING': '⏳',
				'pending': '⏳',
				'CONFIRMED': '✅',
				'confirmed': '✅',
				'SCHEDULED': '✅',
				'scheduled': '✅',
				'COMPLETED': '✔️',
				'completed': '✔️',
				'CANCELLED': '❌',
				'cancelled': '❌'
			};
			return iconMap[status] || '❓';
		},

		// 获取状态样式类
		getStatusClass(status) {
			const classMap = {
				'PENDING': 'status-pending',
				'pending': 'status-pending',
				'CONFIRMED': 'status-confirmed',
				'confirmed': 'status-confirmed',
				'SCHEDULED': 'status-confirmed',
				'scheduled': 'status-confirmed',
				'COMPLETED': 'status-completed',
				'completed': 'status-completed',
				'CANCELLED': 'status-cancelled',
				'cancelled': 'status-cancelled'
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
				url: `/pages/waitlist/waitlist?scheduleId=${this.appointmentDetail.scheduleId}&doctorId=${this.appointmentDetail.doctorId}&scheduleDate=${this.appointmentDetail.appointmentDate}&timeSlot=${this.appointmentDetail.timeSlot}`
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