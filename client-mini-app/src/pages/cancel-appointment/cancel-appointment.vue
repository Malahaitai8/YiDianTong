<template>
	<view class="cancel-page">
		<!-- 挂号信息卡片 -->
		<view class="info-card">
			<view class="card-title">退号信息确认</view>
			
			<view class="info-row">
				<text class="info-label">预约编号</text>
				<text class="info-value">{{ appointmentInfo.appointmentNo || 'APT20251115001' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊医生</text>
				<text class="info-value">{{ appointmentInfo.doctorName || '张医生' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊日期</text>
				<text class="info-value">{{ appointmentInfo.appointmentDate || '2025-11-15' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊时间</text>
				<text class="info-value">{{ appointmentInfo.appointmentTime || '08:00-12:00' }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">挂号费（已付）</text>
				<text class="info-value price">¥{{ appointmentInfo.actualFee || '2.50' }}</text>
			</view>
		</view>

		<!-- 退号规则说明 -->
		<view class="rules-card">
			<view class="rules-title">📋 退号规则</view>
			<view class="rules-content">
				<view class="rule-item">
					<text class="rule-icon">⏰</text>
					<text class="rule-text">就诊前24小时以上退号：全额退款，无手续费</text>
				</view>
				<view class="rule-item">
					<text class="rule-icon">⏰</text>
					<text class="rule-text">就诊前2-24小时退号：退款80%，扣除20%手续费</text>
				</view>
				<view class="rule-item warning">
					<text class="rule-icon">⚠️</text>
					<text class="rule-text">就诊前2小时内退号：退款50%，扣除50%手续费，可能影响信用</text>
				</view>
				<view class="rule-item">
					<text class="rule-icon">💰</text>
					<text class="rule-text">退款将在3-5个工作日内原路退回</text>
				</view>
			</view>
		</view>

		<!-- 退号原因选择 -->
		<view class="reason-card">
			<view class="card-title">退号原因（选填）</view>
			<view class="reason-list">
				<view 
					class="reason-item" 
					:class="{ active: selectedReason === item.value }"
					v-for="item in reasonList" 
					:key="item.value"
					@click="selectReason(item.value)"
				>
					<text class="reason-text">{{ item.label }}</text>
					<text class="reason-check" v-if="selectedReason === item.value">✓</text>
				</view>
			</view>
			<textarea 
				class="reason-input" 
				v-model="customReason" 
				placeholder="如有其他原因，请在此填写..."
				maxlength="200"
			/>
		</view>

		<!-- 退款金额提示 -->
		<view class="refund-card">
			<view class="refund-title">预计退款金额</view>
			<view class="refund-amount">
				<text class="refund-label">可退金额：</text>
				<text class="refund-value">¥{{ calculateRefund() }}</text>
			</view>
			<view class="refund-tip" v-if="refundRate < 1">
				<text class="tip-text">扣除手续费：¥{{ (appointmentInfo.actualFee * (1 - refundRate)).toFixed(2) }}</text>
			</view>
		</view>

		<!-- 确认提示 -->
		<view class="confirm-tip">
			<text class="tip-icon">💡</text>
			<text class="tip-text">确认退号后，该号源将释放，其他用户可重新预约</text>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<button class="cancel-btn" @click="goBack">取消</button>
			<button class="confirm-btn" :loading="submitting" @click="handleCancel">确认退号</button>
		</view>
	</view>
</template>

<script>
// TODO: 引入API
// import { cancelAppointment, getAppointmentDetail } from '@/api/appointment.js'

export default {
	name: 'CancelAppointment',
	data() {
		return {
			appointmentId: null,
			submitting: false,
			selectedReason: '',
			customReason: '',
			// 硬编码的挂号信息（实际应从API获取）
			appointmentInfo: {
				id: 1,
				appointmentNo: 'APT20251115001',
				doctorName: '张医生',
				appointmentDate: '2025-11-15',
				appointmentTime: '08:00-12:00',
				actualFee: 2.50,
				createdAt: '2025-11-14 10:30:00'
			},
			// 退号原因列表
			reasonList: [
				{ label: '临时有事，无法就诊', value: 'busy' },
				{ label: '已找到其他医生', value: 'other_doctor' },
				{ label: '病情好转，无需就诊', value: 'recovered' },
				{ label: '时间安排冲突', value: 'time_conflict' },
				{ label: '其他原因', value: 'other' }
			]
		};
	},
	computed: {
		// 计算退款比例（根据距离就诊时间）
		refundRate() {
			// TODO: 实际应根据当前时间与就诊时间的差值计算
			// 这里硬编码为示例：假设距离就诊时间超过24小时
			const appointmentTime = new Date(this.appointmentInfo.appointmentDate + ' ' + this.appointmentInfo.appointmentTime.split('-')[0]);
			const now = new Date();
			const hoursDiff = (appointmentTime - now) / (1000 * 60 * 60);
			
			if (hoursDiff > 24) {
				return 1.0; // 全额退款
			} else if (hoursDiff > 2) {
				return 0.8; // 退款80%
			} else {
				return 0.5; // 退款50%
			}
		}
	},
	onLoad(options) {
		if (options.id) {
			this.appointmentId = options.id;
			// TODO: 调用API获取挂号详情
			// this.loadAppointmentInfo();
		}
	},
	methods: {
		// TODO: 加载挂号信息
		// async loadAppointmentInfo() {
		// 	try {
		// 		const data = await getAppointmentDetail(this.appointmentId);
		// 		this.appointmentInfo = data;
		// 	} catch (e) {
		// 		uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
		// 	}
		// },

		// 选择退号原因
		selectReason(value) {
			this.selectedReason = value;
			if (value !== 'other') {
				this.customReason = '';
			}
		},

		// 计算退款金额
		calculateRefund() {
			const fee = parseFloat(this.appointmentInfo.actualFee) || 0;
			return (fee * this.refundRate).toFixed(2);
		},

		// 处理退号
		async handleCancel() {
			// 二次确认
			uni.showModal({
				title: '确认退号',
				content: `确定要退号吗？预计退款金额：¥${this.calculateRefund()}`,
				confirmText: '确认退号',
				confirmColor: '#f44336',
				success: async (res) => {
					if (res.confirm) {
						await this.submitCancel();
					}
				}
			});
		},

		// 提交退号
		async submitCancel() {
			this.submitting = true;
			try {
				// TODO: 调用退号API
				// const reason = this.selectedReason === 'other' ? this.customReason : this.reasonList.find(r => r.value === this.selectedReason)?.label;
				// await cancelAppointment(this.appointmentId, { reason });
				
				// 模拟API调用
				await new Promise(resolve => setTimeout(resolve, 1000));
				
				uni.showToast({
					title: '退号成功',
					icon: 'success'
				});
				
				setTimeout(() => {
					// 返回上一页或跳转到记录页
					uni.navigateBack();
					// 或者：uni.switchTab({ url: '/pages/records/records' });
				}, 1500);
			} catch (e) {
				uni.showToast({
					title: e.msg || '退号失败',
					icon: 'none'
				});
			} finally {
				this.submitting = false;
			}
		},

		// 返回
		goBack() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped>
.cancel-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 180rpx;
}

/* 信息卡片 */
.info-card {
	background: #fff;
	margin: 30rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.card-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 24rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
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

.price {
	color: #ff5722;
	font-weight: 600;
	font-size: 32rpx;
}

/* 退号规则卡片 */
.rules-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	background: linear-gradient(135deg, #fff3e0 0%, #fff8e1 100%);
	border: 2rpx solid #ffcc02;
}

.rules-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #f57c00;
	margin-bottom: 20rpx;
}

.rules-content {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.rule-item {
	display: flex;
	align-items: flex-start;
	padding: 12rpx 0;
}

.rule-item.warning {
	background: #fff3cd;
	padding: 16rpx;
	border-radius: 8rpx;
	border-left: 4rpx solid #ff9800;
}

.rule-icon {
	font-size: 28rpx;
	margin-right: 12rpx;
	margin-top: 4rpx;
}

.rule-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
	flex: 1;
}

/* 退号原因卡片 */
.reason-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.reason-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-bottom: 24rpx;
}

.reason-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx;
	border: 2rpx solid #e0e0e0;
	border-radius: 12rpx;
	background: #fafafa;
	transition: all 0.3s;
}

.reason-item.active {
	border-color: #1976d2;
	background: #e3f2fd;
}

.reason-text {
	font-size: 28rpx;
	color: #333;
}

.reason-check {
	font-size: 32rpx;
	color: #1976d2;
	font-weight: 600;
}

.reason-input {
	width: 100%;
	min-height: 120rpx;
	padding: 20rpx;
	border: 2rpx solid #e0e0e0;
	border-radius: 12rpx;
	font-size: 26rpx;
	color: #333;
	background: #fafafa;
	box-sizing: border-box;
}

/* 退款金额卡片 */
.refund-card {
	background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	border: 2rpx solid #4caf50;
}

.refund-title {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 16rpx;
}

.refund-amount {
	display: flex;
	align-items: baseline;
	margin-bottom: 12rpx;
}

.refund-label {
	font-size: 28rpx;
	color: #666;
}

.refund-value {
	font-size: 48rpx;
	font-weight: 600;
	color: #4caf50;
}

.refund-tip {
	margin-top: 12rpx;
}

.tip-text {
	font-size: 24rpx;
	color: #999;
}

/* 确认提示 */
.confirm-tip {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 24rpx 30rpx;
	display: flex;
	align-items: center;
	border-left: 4rpx solid #2196f3;
}

.tip-icon {
	font-size: 32rpx;
	margin-right: 12rpx;
}

.tip-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
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

.cancel-btn,
.confirm-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-weight: 600;
	border: none;
}

.cancel-btn {
	background: #f5f5f5;
	color: #666;
}

.confirm-btn {
	background: linear-gradient(135deg, #f44336 0%, #e57373 100%);
	color: #fff;
}
</style>


