<template>
	<view class="waitlist-page">
		<!-- 医生信息卡片 -->
		<view class="doctor-card" v-if="doctorInfo.name">
			<view class="doctor-header">
				<view class="doctor-avatar">
					<text class="avatar-text">{{ doctorInfo.name ? doctorInfo.name.substring(0, 1) : '医' }}</text>
				</view>
				<view class="doctor-info">
					<view class="doctor-name-row">
						<text class="doctor-name">{{ doctorInfo.name || '张医生' }}</text>
						<text class="doctor-title">{{ doctorInfo.title || '主任医师' }}</text>
					</view>
					<text class="doctor-clinic">{{ doctorInfo.clinicName || '消化内科' }}</text>
				</view>
			</view>
			<view class="appointment-info">
				<view class="info-item">
					<text class="info-label">就诊日期</text>
					<text class="info-value">{{ scheduleInfo.date || '2025-11-15' }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">时间段</text>
					<text class="info-value">{{ scheduleInfo.timeSlotDisplay || '上午' }}</text>
				</view>
			</view>
		</view>

		<!-- 候补状态卡片 -->
		<view class="status-card">
			<view class="status-header">
				<text class="status-icon">⏰</text>
				<text class="status-title">候补排队中</text>
			</view>
			<view class="queue-info">
				<view class="queue-number">
					<text class="number-label">当前排队位次</text>
					<text class="number-value">{{ waitlistInfo.rank !== undefined ? (waitlistInfo.rank + 1) : '加载中' }}</text>
				</view>
				<view class="queue-tip">
					<text class="tip-text">号源释放时，系统将按排队顺序自动为您预约</text>
				</view>
			</view>
		</view>

		<!-- 候补队列信息 -->
		<view class="queue-size-card">
			<view class="card-header">
				<text class="card-icon">👥</text>
				<text class="card-title">队列信息</text>
			</view>
			<view class="queue-details">
				<view class="detail-item">
					<text class="detail-label">队列总人数</text>
					<text class="detail-value">{{ waitlistInfo.queueSize || 0 }}人</text>
				</view>
				<view class="detail-item">
					<text class="detail-label">您的位置</text>
					<text class="detail-value">{{ waitlistInfo.rank !== undefined ? (waitlistInfo.rank + 1) : '加载中' }} / {{ waitlistInfo.queueSize || 0 }}</text>
				</view>
			</view>
		</view>

		<!-- 实时更新提示 -->
		<view class="update-tip">
			<text class="tip-icon">🔄</text>
			<text class="tip-text">数据实时更新中，号源释放或新用户加入时会自动刷新</text>
		</view>

		<!-- 候补规则说明 -->
		<view class="rules-card">
			<view class="rules-title">📋 候补规则说明</view>
			<view class="rules-list">
				<view class="rule-item">
					<text class="rule-number">1</text>
					<text class="rule-text">当有号源释放时，系统将按排队顺序自动为您预约</text>
				</view>
				<view class="rule-item">
					<text class="rule-number">2</text>
					<text class="rule-text">预约成功后，将通过短信、微信等方式通知您</text>
				</view>
				<view class="rule-item">
					<text class="rule-number">3</text>
					<text class="rule-text">如就诊时间已过仍未候补成功，候补将自动取消</text>
				</view>
				<view class="rule-item">
					<text class="rule-number">4</text>
					<text class="rule-text">您可以随时取消候补，取消后无法恢复</text>
				</view>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<button class="cancel-btn" @click="handleCancelWaitlist">取消候补</button>
			<button class="refresh-btn" @click="refreshData">
				<text class="btn-icon">🔄</text>
				<text class="btn-text">刷新数据</text>
			</button>
		</view>

		<!-- 加载遮罩 -->
		<view class="loading-mask" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getMyWaitlist, cancelWaitlist, joinWaitlist } from '@/api/waitlist.js'
import { searchAvailable } from '@/api/appointment.js'

export default {
	name: 'Waitlist',
	data() {
		return {
			appointmentId: null,
			doctorId: null,
			scheduleId: null,
			scheduleDate: '',
			timeSlot: '',
			loading: false,
			refreshTimer: null,
			// 医生信息
			doctorInfo: {
				id: null,
				name: '',
				title: '',
				clinicName: ''
			},
			// 排班信息
			scheduleInfo: {
				date: '',
				timeSlot: '',
				timeSlotDisplay: ''
			},
			// 候补信息
			waitlistInfo: {
				rank: undefined,
				queueSize: 0
			}
		};
	},
	onLoad(options) {
		console.log('候补页面参数:', options);
		
		// 检查是否已登录
		const token = this.$store.state.user.token;
		if (!token) {
			uni.showToast({ title: '请先登录', icon: 'none' });
			// 延迟跳转到登录页面
			setTimeout(() => {
				uni.navigateTo({ url: '/pages/login/login' });
			}, 1000);
			return;
		}
		
		if (options.scheduleId) {
			this.scheduleId = parseInt(options.scheduleId);
		}
		
		if (options.doctorId) {
			this.doctorId = parseInt(options.doctorId);
		}
		
		if (options.scheduleDate) {
			this.scheduleDate = options.scheduleDate;
			this.scheduleInfo.date = options.scheduleDate;
		}
		
		if (options.timeSlot) {
			this.timeSlot = options.timeSlot;
			this.scheduleInfo.timeSlot = options.timeSlot;
			
			// 设置时间段显示文本
			const timeSlotMap = {
				'morning': '上午',
				'afternoon': '下午',
				'evening': '晚上'
			};
			this.scheduleInfo.timeSlotDisplay = timeSlotMap[options.timeSlot] || options.timeSlot;
		}
		
		// 加载候补信息
		this.loadWaitlistInfo();
		
		// 启动定时刷新（每30秒刷新一次）
		this.startAutoRefresh();
	},
	onUnload() {
		// 清除定时器
		if (this.refreshTimer) {
			clearInterval(this.refreshTimer);
		}
	},
	methods: {
		// 加载候补信息
		async loadWaitlistInfo() {
			this.loading = true;
			try {
				// 获取我的候补列表
				const waitlistData = await getMyWaitlist();
				console.log('候补列表数据:', waitlistData);
				
				// 查找当前排班的候补信息
				if (Array.isArray(waitlistData) && this.scheduleId) {
					const currentWaitlist = waitlistData.find(item => item.scheduleId === this.scheduleId);
					if (currentWaitlist) {
						this.waitlistInfo.rank = currentWaitlist.rank;
						this.waitlistInfo.queueSize = currentWaitlist.queueSize;
					}
				}
				
				// 如果有医生ID，获取医生信息
				if (this.doctorId) {
					// 这里应该调用获取医生详情的API，暂时使用模拟数据
					// 在实际应用中，应该调用API获取医生详情
					this.doctorInfo.id = this.doctorId;
					this.doctorInfo.name = '张医生';
					this.doctorInfo.title = '主任医师';
					this.doctorInfo.clinicName = '消化内科';
				}
				
				// 如果没有医生信息但有排班ID，尝试通过搜索获取排班信息
				if (!this.doctorInfo.name && this.scheduleId && this.scheduleDate) {
					const searchData = await searchAvailable({
						startDate: this.scheduleDate,
						endDate: this.scheduleDate
					});
					
					if (Array.isArray(searchData) && searchData.length > 0) {
						const schedule = searchData.find(item => item.scheduleId === this.scheduleId);
						if (schedule) {
							this.doctorInfo.name = schedule.doctorName;
							this.doctorInfo.title = ''; // 需要从API获取
							this.doctorInfo.clinicName = schedule.departmentName;
						}
					}
				}
			} catch (e) {
				console.error('加载候补信息失败:', e);
				uni.showToast({
					title: e.msg || '加载失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		},

		// 启动自动刷新
		startAutoRefresh() {
			// 每30秒刷新一次数据
			this.refreshTimer = setInterval(() => {
				this.loadWaitlistInfo();
			}, 30000);
		},

		// 手动刷新数据
		async refreshData() {
			uni.showLoading({ title: '刷新中...' });
			await this.loadWaitlistInfo();
			uni.hideLoading();
			uni.showToast({
				title: '刷新成功',
				icon: 'success',
				duration: 1500
			});
		},

		// 取消候补
		handleCancelWaitlist() {
			uni.showModal({
				title: '取消候补',
				content: '确定要取消候补吗？取消后无法恢复，需重新排队',
				confirmText: '确认取消',
				confirmColor: '#f44336',
				success: async (res) => {
					if (res.confirm) {
						await this.cancelWaitlist();
					}
				}
			});
		},

		// 提交取消候补
		async cancelWaitlist() {
			if (!this.scheduleId) {
				uni.showToast({
					title: '缺少排班信息',
					icon: 'none'
				});
				return;
			}
			
			this.loading = true;
			try {
				// 调用取消候补API
				await cancelWaitlist(this.scheduleId);
				
				uni.showToast({
					title: '已取消候补',
					icon: 'success'
				});
				
				// 返回上一页
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
			} catch (e) {
				console.error('取消候补失败:', e);
				uni.showToast({
					title: e.msg || '取消失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		}
	}
};
</script>

<style scoped>
.waitlist-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 180rpx;
}

/* 医生信息卡片 */
.doctor-card {
	background: #fff;
	margin: 30rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.doctor-header {
	display: flex;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 24rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.doctor-avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 24rpx;
}

.avatar-text {
	font-size: 40rpx;
	color: #fff;
	font-weight: 600;
}

.doctor-info {
	flex: 1;
}

.doctor-name-row {
	display: flex;
	align-items: center;
	margin-bottom: 12rpx;
}

.doctor-name {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-right: 16rpx;
}

.doctor-title {
	font-size: 24rpx;
	color: #1976d2;
	background: #e3f2fd;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
}

.doctor-clinic {
	font-size: 26rpx;
	color: #666;
}

.appointment-info {
	display: flex;
	gap: 40rpx;
}

.info-item {
	display: flex;
	flex-direction: column;
}

.info-label {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 8rpx;
}

.info-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

/* 候补状态卡片 */
.status-card {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	color: #fff;
}

.status-header {
	display: flex;
	align-items: center;
	margin-bottom: 24rpx;
}

.status-icon {
	font-size: 40rpx;
	margin-right: 12rpx;
}

.status-title {
	font-size: 32rpx;
	font-weight: 600;
}

.queue-info {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.queue-number {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 16rpx;
}

.number-label {
	font-size: 26rpx;
	opacity: 0.9;
	margin-bottom: 12rpx;
}

.number-value {
	font-size: 80rpx;
	font-weight: 700;
	line-height: 1;
}

.queue-tip {
	margin-top: 12rpx;
}

.tip-text {
	font-size: 24rpx;
	opacity: 0.9;
	text-align: center;
	line-height: 1.6;
}

/* 队列信息卡片 */
.queue-size-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.card-header {
	display: flex;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.card-icon {
	font-size: 32rpx;
	margin-right: 12rpx;
}

.card-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}

.queue-details {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.detail-label {
	font-size: 26rpx;
	color: #666;
}

.detail-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}

/* 更新提示 */
.update-tip {
	background: #e3f2fd;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 20rpx 30rpx;
	display: flex;
	align-items: center;
	border-left: 4rpx solid #2196f3;
}

.tip-icon {
	font-size: 28rpx;
	margin-right: 12rpx;
}

.tip-text {
	font-size: 24rpx;
	color: #666;
	line-height: 1.6;
}

/* 规则说明卡片 */
.rules-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.rules-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 24rpx;
}

.rules-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.rule-item {
	display: flex;
	align-items: flex-start;
}

.rule-number {
	width: 40rpx;
	height: 40rpx;
	border-radius: 50%;
	background: #1976d2;
	color: #fff;
	font-size: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 16rpx;
	flex-shrink: 0;
	margin-top: 4rpx;
}

.rule-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.8;
	flex: 1;
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
.refresh-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 44rpx;
	font-size: 30rpx;
	font-weight: 600;
	border: none;
	display: flex;
	align-items: center;
	justify-content: center;
}

.cancel-btn {
	background: #f5f5f5;
	color: #666;
}

.refresh-btn {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
}

.btn-icon {
	margin-right: 8rpx;
	font-size: 28rpx;
}

.btn-text {
	font-size: 30rpx;
}

/* 加载遮罩 */
.loading-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.3);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}

.loading-text {
	color: #fff;
	font-size: 28rpx;
}
</style>