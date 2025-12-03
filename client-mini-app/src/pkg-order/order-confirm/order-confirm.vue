<template>
	<view class="confirm-page">
		<!-- 挂号信息卡片 -->
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
				<text class="value">{{ formatDisplayDate(schedule.scheduleDate) }}</text>
			</view>
			<view class="row">
				<text class="label">时间段</text>
				<text class="value">{{ displayPeriod(schedule.timeSlot) }}（{{ schedule.startTime }} - {{ schedule.endTime }}）</text>
			</view>
		</view>

		<!-- 身份信息卡片 -->
		<view class="card identity-card">
			<view class="card-header">
				<view class="title">身份信息</view>
				<view class="verify-status" :class="identityStatusClass">
					<text class="status-icon">{{ identityStatusIcon }}</text>
					<text class="status-text">{{ identityStatusText }}</text>
				</view>
			</view>
			<view class="identity-info">
				<view class="info-item">
					<text class="info-label">患者姓名</text>
					<text class="info-value">{{ patientInfo.name || userInfo.username }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">身份类型</text>
					<text class="info-value">{{ identityTypeText }}</text>
				</view>
				<view class="info-item" v-if="!isIdentityVerified">
					<text class="hint-text">💡 完成身份认证后可享受医保报销优惠</text>
					<button class="verify-btn" @click="goToVerify">去认证</button>
				</view>
			</view>
		</view>

		<!-- 费用明细卡片 -->
		<view class="card fee-card">
			<view class="title">费用明细</view>
			<view class="fee-row">
				<text class="fee-label">挂号费</text>
				<text class="fee-value">¥{{ originalFee }}</text>
			</view>
			<view class="fee-row" v-if="isIdentityVerified && reimbursementRate > 0">
				<text class="fee-label">医保报销（{{ reimbursementRate }}%）</text>
				<text class="fee-value discount">-¥{{ reimbursementAmount }}</text>
			</view>
			<view class="fee-divider"></view>
			<view class="fee-row total">
				<text class="fee-label">实付金额</text>
				<text class="fee-value actual">¥{{ actualFee }}</text>
			</view>
			<view class="savings-tip" v-if="isIdentityVerified && reimbursementRate > 0">
				💰 您已节省 ¥{{ reimbursementAmount }}
			</view>
		</view>

		<!-- 确认事项 -->
		<view class="card">
			<view class="title">注意事项</view>
			<view class="hint">• 请于就诊前2小时内勿取消，以免影响信用</view>
			<view class="hint">• 到诊时请携带有效身份证件</view>
			<view class="hint">• 如需改约，请先取消后重新预约</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="price-info">
				<text class="price-label">实付</text>
				<text class="price-value">¥{{ actualFee }}</text>
			</view>
			<button class="submit-btn" :loading="submitting" :disabled="submitting || isScheduleFull" @click="submitOrder">
				{{ isScheduleFull ? '号源已满' : (submitting ? '提交中...' : '确认支付') }}
			</button>
		</view>
		
		<!-- 号源已满提示 -->
		<view class="full-schedule-popup" v-if="isScheduleFull">
			<view class="popup-content">
				<text class="popup-icon">⏰</text>
				<text class="popup-title">号源已满</text>
				<text class="popup-desc">该时段号源已满，您可以加入候补队列，当有号源释放时系统将自动为您预约</text>
				<view class="popup-actions">
					<button class="cancel-btn" @click="goBack">返回</button>
					<button class="waitlist-btn" @click="joinWaitlist">加入候补</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getDoctorById } from '@/api/doctor.js'
import { createAppointment } from '@/api/appointment.js'
import { getPatientProfile } from '@/api/patient.js'
import { getScheduleById } from '@/api/schedule.js'
import { joinWaitlist, createWaitlistPrepayment, payWaitlistOrder } from '@/api/waitlist.js'

export default {
	name: 'OrderConfirm',
	data() {
		return {
			doctorId: null,
			scheduleId: null,
			doctorInfo: {},
			patientInfo: {
				name: '',
				specificRole: '',
				idStatus: ''
			},
			schedule: {
				id: null,
				scheduleDate: '',
				timeSlot: '',
				startTime: '08:00',
				endTime: '12:00',
				fee: null,
				availableSlots: 1 // 默认有号源
			},
			submitting: false,
			loading: false,
			isScheduleFull: false // 标记号源是否已满
		};
	},
	computed: {
		// 获取用户信息
		userInfo() {
			return this.$store.state.user.userInfo || {};
		},
		
		// 是否已认证身份
		isIdentityVerified() {
			const idStatus = this.patientInfo.idStatus || '';
			return idStatus === 'verified' || idStatus === '已认证';
		},
		
		// 身份类型文本
		identityTypeText() {
			const type = this.patientInfo.specificRole || '';
			if (type === 'student') return '学生';
			if (type === 'teacher') return '教师';
			if (type === 'outsider') return '外部人员';
			return type || '普通患者';
		},
		
		// 认证状态样式类
		identityStatusClass() {
			return this.isIdentityVerified ? 'verified' : 'not-verified-status';
		},
		
		// 认证状态图标
		identityStatusIcon() {
			return this.isIdentityVerified ? '✅' : '⚠️';
		},
		
		// 认证状态文本
		identityStatusText() {
			return this.isIdentityVerified ? '已认证' : '未认证';
		},
		
		// 报销比例
		reimbursementRate() {
			if (!this.isIdentityVerified) return 0;
			const type = this.patientInfo.specificRole;
			if (type === 'student') return 95; // 学生报销95%
			if (type === 'teacher') return 90; // 教师报销90%
			return 0;
		},
		
		// 原价（与后端接口规则对齐：normal/expert/VIP -> 15/30/50）
		originalFee() {
			// 1) 若后端排班中已经带有 fee/price 字段，直接使用
			const raw = this.schedule.price ?? this.schedule.fee;
			if (raw != null && !Number.isNaN(Number(raw))) {
				return Number(raw).toFixed(2);
			}

			// 2) 否则根据 slotType 映射到与后端相同的默认金额
			const fee = this.estimateFeeBySlotType(this.schedule.slotType);
			return Number(fee).toFixed(2);
		},
		
		// 报销金额
		reimbursementAmount() {
			const amount = (Number(this.originalFee) * this.reimbursementRate / 100);
			return amount.toFixed(2);
		},
		
		// 实付金额
		actualFee() {
			const actual = Number(this.originalFee) - Number(this.reimbursementAmount);
			return actual.toFixed(2);
		}
	},
	onLoad(options) {
		this.doctorId = Number(options.doctorId || 0);
		this.scheduleId = Number(options.scheduleId || 0);
		this.schedule.id = this.scheduleId;

		this.loadData();
	},
	methods: {
		// 加载所有数据
		async loadData() {
			this.loading = true;
			try {
				// 加载排班信息（必须先加载，因为需要获取日期和时间）
				await this.loadSchedule();
				// 加载医生信息
				await this.loadDoctor();
				// 加载患者信息
				await this.loadPatientInfo();
			} catch (e) {
				console.error('加载数据失败:', e);
			} finally {
				this.loading = false;
			}
		},
		
		// 加载排班信息
		async loadSchedule() {
			try {
				if (!this.scheduleId) return;
				const data = await getScheduleById(this.scheduleId);
				console.log('获取到的排班信息:', data); // 调试日志
				
				if (data) {
					// 根据时段设置默认开始时间
					let defaultStartTime = '08:00';
					let defaultEndTime = '12:00';
					
					if (data.timeSlot === 'morning' || data.timeSlot === '上午') {
						defaultStartTime = '08:00';
						defaultEndTime = '12:00';
					} else if (data.timeSlot === 'afternoon' || data.timeSlot === '下午') {
						defaultStartTime = '14:00';
						defaultEndTime = '18:00';
					} else if (data.timeSlot === 'evening' || data.timeSlot === '晚上') {
						defaultStartTime = '18:00';
						defaultEndTime = '21:00';
					}
					
					this.schedule = {
						id: data.id,
						scheduleDate: data.scheduleDate,
						timeSlot: data.timeSlot,
						startTime: defaultStartTime,
						endTime: defaultEndTime,
						fee: this.estimateFeeBySlotType(data.slotType),
						availableSlots: data.availableSlots || 0
					};
					
					// 检查号源是否已满
					if (this.schedule.availableSlots === 0) {
						this.isScheduleFull = true;
					}
					
					// 如果从URL参数没有获取到doctorId，从排班信息中获取
					if (!this.doctorId && data.doctorId) {
						this.doctorId = data.doctorId;
					}
				}
			} catch (e) {
				console.error('加载排班信息失败:', e);
				uni.showToast({ 
					title: '加载排班信息失败', 
					icon: 'none' 
				});
			}
		},
		
		// 加载医生信息
		async loadDoctor() {
			try {
				if (!this.doctorId) return;
				const data = await getDoctorById(this.doctorId);
				this.doctorInfo = data || {};
			} catch (e) {
				console.error('加载医生信息失败:', e);
			}
		},
		
		// 加载患者信息
		async loadPatientInfo() {
			try {
				// 从API获取最新的患者信息（包含认证状态）
				const data = await getPatientProfile();
				console.log('获取到的患者信息:', data); // 调试日志
				
				this.patientInfo = {
					name: data.name || '',
					specificRole: data.specificRole || '',
					idStatus: data.idStatus || ''
				};
				
				console.log('设置后的patientInfo:', this.patientInfo); // 调试日志
				console.log('认证状态:', this.isIdentityVerified); // 调试日志
				
				// 同步更新store中的用户信息
				this.$store.commit('user/SET_USERINFO', {
					...this.userInfo,
					name: data.name,
					specificRole: data.specificRole,
					idStatus: data.idStatus
				});
			} catch (e) {
				console.error('加载患者信息失败:', e);
				// 如果API失败，尝试从store获取
				const userInfo = this.userInfo;
				this.patientInfo = {
					name: userInfo.name || userInfo.username || '',
					specificRole: userInfo.specificRole || '',
					idStatus: userInfo.idStatus || ''
				};
			}
		},
		
		// 根据号别类型估算费用（与后端接口约定保持一致：normal=15, expert=30, VIP=50）
		estimateFeeBySlotType(slotType) {
			const s = String(slotType || '').toLowerCase();
			if (s === 'vip') return 50;
			if (s === 'expert' || s === '专家') return 30;
			// 默认 normal/普通
			return 15;
		},
		
		// 格式化显示日期
		formatDisplayDate(dateStr) {
			if (!dateStr) return '';
			const date = new Date(dateStr);
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
			const weekDay = weekDays[date.getDay()];
			return `${year}-${month}-${day} ${weekDay}`;
		},
		
		// 显示时间段
		displayPeriod(slot) {
			const s = String(slot || '').toLowerCase();
			if (s.includes('morning') || s.includes('上午')) return '上午';
			if (s.includes('afternoon') || s.includes('下午')) return '下午';
			if (s.includes('evening') || s.includes('晚上')) return '晚上';
			return slot || '-';
		},
		
		// 生成预约时间（后端要求格式：yyyy-MM-dd HH:mm:ss）
		getAppointmentTime() {
			let dateStr = this.schedule.scheduleDate;
			const startTime = this.schedule.startTime || '08:00';
			
			console.log('原始 scheduleDate:', dateStr);
			console.log('原始 startTime:', startTime);
			
			// 验证日期是否存在
			if (!dateStr) {
				console.error('scheduleDate为空！schedule对象:', this.schedule);
				return null;
			}
			
			// 处理日期格式，确保为 yyyy-MM-dd 格式
			if (dateStr.includes('T')) {
				// 如果是 ISO 格式 (2025-11-18T00:00:00)，提取日期部分
				dateStr = dateStr.split('T')[0];
			} else if (dateStr.length > 10) {
				// 如果包含时间部分，只取日期
				dateStr = dateStr.substring(0, 10);
			}
			
			// 确保时间格式为 HH:mm:ss
			let time = startTime;
			if (time.length === 5) {
				// HH:mm 格式，补充秒
				time = `${time}:00`;
			} else if (time.length === 8) {
				// 已经是 HH:mm:ss 格式
				// 不需要处理
			} else {
				// 其他情况，默认补充
				time = `${time}:00`;
			}
			
			const result = `${dateStr} ${time}`;
			console.log('最终拼接的预约时间:', result);
			console.log('日期部分:', dateStr, '时间部分:', time);
			
			return result;
		},
		
		// 跳转到身份认证页面
		goToVerify() {
			uni.showModal({
				title: '身份认证提示',
				content: '进行身份认证后可享受医保报销优惠，是否前往认证？',
				confirmText: '去认证',
				cancelText: '稍后再说',
				success: (res) => {
					if (res.confirm) {
						uni.navigateTo({
							url: '/pages/personal-info/personal-info'
						});
					}
				}
			});
		},
		
		// 提交预约
		async submitOrder() {
			// 如果号源已满，不允許提交
			if (this.isScheduleFull) {
				uni.showToast({ 
					title: '号源已满，请加入候补队列', 
					icon: 'none' 
				});
				return;
			}
			
			if (!this.scheduleId) {
				uni.showToast({ title: '参数缺失：scheduleId', icon: 'none' });
				return;
			}
			
			const appointmentTime = this.getAppointmentTime();
			
			// 验证预约时间是否生成成功
			if (!appointmentTime) {
				uni.showToast({ 
					title: '无法获取排班时间，请重试', 
					icon: 'none' 
				});
				return;
			}
			
			// 验证格式：必须包含日期和时间两部分
			if (!appointmentTime.includes(' ') || appointmentTime.split(' ').length !== 2) {
				console.error('预约时间格式错误:', appointmentTime);
				uni.showToast({ 
					title: '时间格式错误，请重试', 
					icon: 'none' 
				});
				return;
			}
			
			const [datePart, timePart] = appointmentTime.split(' ');
			if (!datePart || datePart.length !== 10) {
				console.error('日期部分格式错误:', datePart);
				uni.showToast({ 
					title: '日期格式错误，请重试', 
					icon: 'none' 
				});
				return;
			}
			if (!timePart || timePart.length !== 8) {
				console.error('时间部分格式错误:', timePart);
				uni.showToast({ 
					title: '时间格式错误，请重试', 
					icon: 'none' 
				});
				return;
			}
			
			this.submitting = true;
			try {
				console.log('=== 提交预约开始 ===')
				console.log('scheduleId:', this.scheduleId);
				console.log('appointmentTime:', appointmentTime);
				console.log('appointmentTime 类型:', typeof appointmentTime);
				console.log('appointmentTime 长度:', appointmentTime.length);
				
				const requestData = {
					scheduleId: this.scheduleId,
					appointmentTime: appointmentTime
				};
				console.log('请求数据:', JSON.stringify(requestData));
				
				await createAppointment(requestData);
				
				// 请求订阅消息授权（预约成功通知）
				try {
					const { requestAppointmentSubscribe } = require('@/utils/wechat-subscribe')
					await requestAppointmentSubscribe()
				} catch (e) {
					console.log('订阅消息授权失败（不影响预约）:', e)
				}
				
				uni.showToast({ 
					title: '预约成功', 
					icon: 'success',
					success: () => {
						setTimeout(() => {
							uni.switchTab({ url: '/pages/records/records' });
						}, 1500);
					}
				});
			} catch (e) {
				console.error('=== 预约失败 ===');
				console.error('错误对象:', e);
				uni.showToast({ 
					title: e.msg || '预约失败', 
					icon: 'none',
					duration: 2500
				});
			} finally {
				this.submitting = false;
			}
		},
		
		// 返回上一页
		goBack() {
			uni.navigateBack();
		},
		
		// 加入候补队列
		async joinWaitlist() {
			// 检查是否已登录 - 修复登录检查逻辑
			const token = this.$store.state.user.token;
			console.log('Token:', token);
			
			if (!token) {
				uni.showToast({ title: '请先登录', icon: 'none' });
				// 延迟跳转到登录页面
				setTimeout(() => {
					uni.navigateTo({ url: '/pages/login/login' });
				}, 1000);
				return;
			}
			
			const confirmQueue = await this.showConfirmModal(`确认加入候补队列吗？候补成功后将自动为您预约。`);
			if (!confirmQueue) return;

			try {
				uni.showLoading({ title: '创建预支付...' });
				const order = await createWaitlistPrepayment({ scheduleId: this.scheduleId });
				uni.hideLoading();

				const payConfirmed = await this.confirmPayment(order.actualFee);
				if (!payConfirmed) return;

				await payWaitlistOrder({
					orderNo: order.orderNo,
					paymentMethod: 'WECHAT',
					paidAmount: order.actualFee
				});

				await joinWaitlist({ scheduleId: this.scheduleId, waitlistId: order.waitlistId });

				uni.showToast({ 
					title: '已加入候补队列', 
					icon: 'success',
					success: () => {
						uni.navigateTo({
							url: `/pages/waitlist/waitlist?scheduleId=${this.scheduleId}&doctorId=${this.doctorId}&scheduleDate=${this.schedule.scheduleDate}&timeSlot=${this.schedule.timeSlot}`
						});
					}
				});
			} catch (e) {
				uni.hideLoading();
				uni.showToast({ title: e.msg || '加入失败', icon: 'none' });
			}
		},
		showConfirmModal(message) {
			return new Promise((resolve) => {
				uni.showModal({
					title: '候补排队',
					content: message,
					confirmText: '确认',
					success: (res) => resolve(res.confirm === true),
					fail: () => resolve(false)
				});
			});
		},
		confirmPayment(amount) {
			const displayAmount = (Number(amount) || 0).toFixed(2);
			return new Promise((resolve) => {
				uni.showModal({
					title: '预支付确认',
					content: `加入候补需预支付挂号费 ${displayAmount} 元，候补成功将自动消耗，未成功或取消将退款，是否继续？`,
					confirmText: '立即支付',
					success: (res) => resolve(res.confirm === true),
					fail: () => resolve(false)
				});
			});
		}
	}
};
</script>

<style scoped>
.confirm-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding: 30rpx;
	padding-bottom: 180rpx;
	box-sizing: border-box;
}

/* 卡片通用样式 */
.card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 20rpx;
}

/* 挂号信息行 */
.row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}
.row:last-child { 
	border-bottom: none; 
}
.label { 
	color: #666; 
	font-size: 26rpx; 
}
.value { 
	color: #333; 
	font-size: 28rpx; 
	flex: 1;
	text-align: right;
}
.price { 
	color: #ff5722; 
	font-size: 32rpx;
	font-weight: 600; 
}

/* 身份信息卡片 */
.identity-card {
	background: linear-gradient(135deg, #f3f9ff 0%, #e8f4ff 100%);
	border: 2rpx solid #bbdefb;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.verify-status {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
}

.verify-status.verified {
	background: #e8f5e9;
}

.verify-status.not-verified-status {
	background: #fff3e0;
}

.status-icon {
	font-size: 24rpx;
}

.status-text {
	font-size: 22rpx;
	color: #333;
	font-weight: 600;
}

.verify-status.verified .status-text {
	color: #4caf50;
}

.verify-status.not-verified-status .status-text {
	color: #f57c00;
}

.identity-info {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.info-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 12rpx 0;
}

.info-label {
	font-size: 26rpx;
	color: #666;
}

.info-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.hint-text {
	font-size: 24rpx;
	color: #f57c00;
	margin-bottom: 12rpx;
}

.verify-btn {
	width: 100%;
	height: 70rpx;
	background: #fff;
	color: #1976d2;
	border: 2rpx solid #1976d2;
	border-radius: 12rpx;
	font-size: 26rpx;
}

/* 费用明细卡片 */
.fee-card {
	background: linear-gradient(135deg, #fff9f0 0%, #fff3e0 100%);
	border: 2rpx solid #ffe0b2;
}

.fee-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16rpx 0;
}

.fee-label {
	font-size: 26rpx;
	color: #666;
}

.fee-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}

.fee-value.discount {
	color: #4caf50;
}

.fee-value.actual {
	font-size: 36rpx;
	color: #ff5722;
}

.fee-divider {
	height: 1rpx;
	background: #e0e0e0;
	margin: 12rpx 0;
}

.fee-row.total {
	padding-top: 16rpx;
}

.savings-tip {
	background: #e8f5e9;
	color: #4caf50;
	padding: 12rpx 16rpx;
	border-radius: 8rpx;
	font-size: 24rpx;
	text-align: center;
	margin-top: 16rpx;
}

/* 注意事项 */
.hint {
	color: #999;
	font-size: 24rpx;
	line-height: 1.8;
	padding: 6rpx 0;
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
	box-shadow: 0 -6rpx 20rpx rgba(0,0,0,0.06);
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.price-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.price-label {
	font-size: 24rpx;
	color: #999;
}

.price-value {
	font-size: 36rpx;
	color: #ff5722;
	font-weight: bold;
}

.submit-btn {
	flex: 2;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
}

.submit-btn[disabled] {
	opacity: 0.6;
}

/* 号源已满提示弹窗 */
.full-schedule-popup {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.6);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
}

.popup-content {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
	width: 80%;
	text-align: center;
}

.popup-icon {
	font-size: 60rpx;
	margin-bottom: 20rpx;
	display: block;
}

.popup-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 20rpx;
	display: block;
}

.popup-desc {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 30rpx;
	display: block;
}

.popup-actions {
	display: flex;
	gap: 20rpx;
}

.cancel-btn, .waitlist-btn {
	flex: 1;
	height: 70rpx;
	border-radius: 35rpx;
	font-size: 28rpx;
	font-weight: 600;
	border: none;
}

.cancel-btn {
	background: #f5f5f5;
	color: #666;
}

.waitlist-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	color: #fff;
}
</style>