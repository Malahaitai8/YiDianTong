<template>
	<view class="confirm-page">
		<view class="card">
			<view class="title">確認掛號資訊</view>
			<view class="row">
				<text class="label">醫生</text>
				<text class="value">{{ doctorInfo.name }}（{{ doctorInfo.title }}）</text>
			</view>
			<view class="row">
				<text class="label">科室/門診</text>
				<text class="value">{{ (doctorInfo && doctorInfo.clinic && doctorInfo.clinic.name) || '-' }}</text>
			</view>
			<view class="row">
				<text class="label">就診日期</text>
				<text class="value">{{ schedule.scheduleDate }}</text>
			</view>
			<view class="row">
				<text class="label">時間段</text>
				<text class="value">{{ displayPeriod(schedule.timeSlot) }}（{{ schedule.startTime }} - {{ schedule.endTime }}）</text>
			</view>
			<view class="row">
				<text class="label">掛號費</text>
				<text class="price">¥{{ schedule.fee || displayFee }}</text>
			</view>
		</view>

		<view class="card">
			<view class="title">確認事項</view>
			<view class="hint">- 請於就診前2小時內勿取消以免影響信用</view>
			<view class="hint">- 到診時攜帶身份證件</view>
		</view>

		<view class="bottom">
			<button class="submit-btn" :loading="submitting" @click="submitOrder">提交預約</button>
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
			// 由上一頁傳入的 schedule 關鍵字段（若未傳完整，採用合理預設）
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
		// 可選：從 options 帶入更多顯示字段
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
				uni.showToast({ title: e.msg || '載入醫生資訊失敗', icon: 'none' });
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
			// 後端要求 YYYY-MM-DD HH:mm:ss；若未選具體時間，取開始時間
			const date = this.schedule.scheduleDate;
			const start = this.schedule.startTime || '08:00';
			return `${date} ${start}:00`;
		},
		getFeeBySlotType() {
			// 若後端未返回費用，簡單根據號別估算（最終以後端結算為準）
			return this.displayFee;
		},
		async submitOrder() {
			if (!this.scheduleId) {
				uni.showToast({ title: '參數缺失：scheduleId', icon: 'none' });
				return;
			}
			this.submitting = true;
			try {
				const appointmentTime = this.getAppointmentTime();
				await createAppointment({
					scheduleId: this.scheduleId,
					appointmentTime
				});
				uni.showToast({ title: '預約成功', icon: 'success' });
				setTimeout(() => {
					uni.switchTab({ url: '/pages/records/records' });
				}, 600);
			} catch (e) {
				// 後端可能返回：號源不足/當天次數上限/請勿重複/時間不合法等
				uni.showToast({ title: e.msg || '預約失敗', icon: 'none' });
			} finally {
				this.submitting = false;
			}
		}
	},
	computed: {
		displayFee() {
			// 簡單展示：專家/普通/VIP 可做區別；實際以後端為準
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
