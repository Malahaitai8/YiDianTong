<template>
	<view class="confirm-page">
		<view class="card">
			<view class="title">确认挂号信息</view>
			<view class="row">
				<text class="label">医生</text>
				<text class="value">{{ doctorInfo.name }}（{{ doctorInfo.title }}）</text>
			</view>
			<view class="row">
				<text class="label">科室/门诊</text>
				<text class="value">{{ (doctorInfo && doctorInfo.clinic && doctorInfo.clinic.name) || '-' }}</text>
			</view>
			<view class="row">
				<text class="label">就诊日期</text>
				<text class="value">{{ schedule.scheduleDate }}</text>
			</view>
			<view class="row">
				<text class="label">时间段</text>
				<text class="value">{{ displayPeriod(schedule.timeSlot) }}（{{ schedule.startTime }} - {{ schedule.endTime }}）</text>
			</view>
			<view class="row">
				<text class="label">挂号费</text>
				<text class="price">¥{{ schedule.fee || displayFee }}</text>
			</view>
		</view>

		<view class="card">
			<view class="title">确认事项</view>
			<view class="hint">- 请于就诊前2小时内勿取消以免影响信用</view>
			<view class="hint">- 到诊时携带身份证件</view>
		</view>

		<view class="bottom">
			<button class="submit-btn" :loading="submitting" @click="submitOrder">提交预约</button>
		</view>
	</view>
</template>

<script>
import { getDoctorById } from '@/api/doctor.js'
import { createAppointment } from '@/api/appointment.js'

export default {
	name: 'OrderConfirm',
	data() {
		return {
			doctorId: null,
			scheduleId: null,
			doctorInfo: {},
			// 由上一页传入的 schedule 关键字段（若未传完整，采用合理预设）
			schedule: {
				id: null,
				scheduleDate: '',
				timeSlot: '',
				startTime: '08:00',
				endTime: '12:00',
				fee: null
			},
			submitting: false
		};
	},
	onLoad(options) {
		this.doctorId = Number(options.doctorId || 0);
		this.scheduleId = Number(options.scheduleId || 0);
		// 可选：从 options 带入更多显示字段
		if (options.scheduleDate) this.schedule.scheduleDate = options.scheduleDate;
		if (options.timeSlot) this.schedule.timeSlot = options.timeSlot;
		if (options.startTime) this.schedule.startTime = options.startTime;
		if (options.endTime) this.schedule.endTime = options.endTime;
		if (options.fee) this.schedule.fee = Number(options.fee);
		this.schedule.id = this.scheduleId;

		this.loadDoctor();
	},
	methods: {
		async loadDoctor() {
			try {
				if (!this.doctorId) return;
				const data = await getDoctorById(this.doctorId);
				this.doctorInfo = data || {};
			} catch (e) {
				uni.showToast({ title: e.msg || '加载医生信息失败', icon: 'none' });
			}
		},
		displayPeriod(slot) {
			const s = String(slot || '').toLowerCase();
			if (s.includes('morning') || s.includes('上午')) return '上午';
			if (s.includes('afternoon') || s.includes('下午')) return '下午';
			if (s.includes('evening') || s.includes('晚上')) return '晚上';
			return slot || '-';
		},
		getAppointmentTime() {
			// 后端要求 YYYY-MM-DD HH:mm:ss；若未选具体时间，取开始时间
			const date = this.schedule.scheduleDate;
			const start = this.schedule.startTime || '08:00';
			return `${date} ${start}:00`;
		},
		getFeeBySlotType() {
			// 若后端未返回费用，简单根据号别估算（最终以后端结算为准）
			return this.displayFee;
		},
		async submitOrder() {
			if (!this.scheduleId) {
				uni.showToast({ title: '参数缺失：scheduleId', icon: 'none' });
				return;
			}
			this.submitting = true;
			try {
				const appointmentTime = this.getAppointmentTime();
				await createAppointment({
					scheduleId: this.scheduleId,
					appointmentTime
				});
				uni.showToast({ title: '预约成功', icon: 'success' });
				setTimeout(() => {
					uni.switchTab({ url: '/pages/records/records' });
				}, 600);
			} catch (e) {
				// 后端可能返回：号源不足/当天次数上限/请勿重复/时间不合法等
				uni.showToast({ title: e.msg || '预约失败', icon: 'none' });
			} finally {
				this.submitting = false;
			}
		}
	},
	computed: {
		displayFee() {
			// 简单展示：专家/普通/VIP 可做区别；实际以后端为准
			const slotType = (this.schedule.slotType || '').toLowerCase();
			if (slotType === 'vip') return 200;
			if (slotType === 'expert') return 50;
			return 30;
		}
	}
};
</script>

<style scoped>
.confirm-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 30rpx;
	box-sizing: border-box;
}
.card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}
.title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 20rpx;
}
.row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}
.row:last-child { border-bottom: none; }
.label { color: #666; font-size: 26rpx; }
.value { color: #333; font-size: 28rpx; }
.price { color: #ff5722; font-weight: 600; }
.hint { color: #999; font-size: 24rpx; line-height: 1.7; padding: 4rpx 0; }
.bottom {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	padding: 20rpx 30rpx env(safe-area-inset-bottom);
	box-shadow: 0 -6rpx 20rpx rgba(0,0,0,0.06);
}
.submit-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-weight: 600;
}
</style>
