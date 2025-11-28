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
						<text class="doctor-name">{{ displayValue(doctorInfo.name) }}</text>
						<text class="doctor-title">{{ displayValue(doctorInfo.title) }}</text>
					</view>
					<text class="doctor-clinic">{{ displayValue(doctorInfo.clinicName) }}</text>
				</view>
			</view>
			<view class="appointment-info">
				<view class="info-item">
					<text class="info-label">就诊日期</text>
					<text class="info-value">{{ displayValue(scheduleInfo.date) }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">时间段</text>
					<text class="info-value">{{ displayValue(scheduleInfo.timeSlotDisplay) }}</text>
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
import { getScheduleDetailsById } from '@/api/schedule.js'
import request from '@/utils/request.js'
import webSocketManager from '@/utils/websocket.js'

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
			checkAppointmentTimer: null, // 检查预约的定时器
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
			},
			// 上次检查的候补状态
			lastWaitlistStatus: null,
			// 检查预约的间隔（毫秒）
			checkInterval: 10000, // 10秒
			// 最大检查次数
			maxCheckCount: 30, // 最多检查5分钟
			currentCheckCount: 0
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
			console.log('排班ID:', this.scheduleId);
		}
		
		if (options.doctorId) {
			this.doctorId = parseInt(options.doctorId);
		}
		
		if (options.scheduleDate) {
			this.scheduleDate = options.scheduleDate;
			this.scheduleInfo.date = options.scheduleDate;
			console.log('传入的排班日期:', this.scheduleInfo.date);
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
			console.log('传入的时间段:', this.scheduleInfo.timeSlotDisplay);
		}
		
		// 加载候补信息
		this.loadWaitlistInfo();
		
		// 启动定时刷新（每30秒刷新一次）
		this.startAutoRefresh();
		
		// 设置WebSocket事件监听
		uni.$on('waitlist-rank-update', this.handleRankUpdate);
		uni.$on('waitlist-success', this.handleWaitlistSuccessEvent);
		
		// 确保WebSocket连接
		this.ensureWebSocketConnection();
	},
	onUnload() {
		// 清除所有定时器
		if (this.refreshTimer) {
			clearInterval(this.refreshTimer);
			this.refreshTimer = null;
		}
		if (this.checkAppointmentTimer) {
			clearInterval(this.checkAppointmentTimer);
			this.checkAppointmentTimer = null;
		}
		
		// 移除WebSocket事件监听
		uni.$off('waitlist-rank-update', this.handleRankUpdate);
		uni.$off('waitlist-success', this.handleWaitlistSuccessEvent);
	},
	methods: {
		displayValue(value, fallback = '--') {
			return value === undefined || value === null || value === '' ? fallback : value;
		},
		// 加载候补信息
		async loadWaitlistInfo(silent = false) {
			if (!silent) {
				this.loading = true;
			}
			try {
				// 获取我的候补列表
				const waitlistData = await getMyWaitlist({ silent: true });
				console.log('候补列表数据:', waitlistData);

				// 查找当前排班的候补信息
				let currentWaitlist = null;
				if (Array.isArray(waitlistData) && this.scheduleId) {
					currentWaitlist = waitlistData.find(item => item.scheduleId === this.scheduleId);
					if (currentWaitlist) {
						this.waitlistInfo.rank = currentWaitlist.rank;
						this.waitlistInfo.queueSize = currentWaitlist.queueSize;
						// 设置候补可视化统计数据
						this.waitlistInfo.successRate = currentWaitlist.successRate;
						this.waitlistInfo.avgWaitTime = currentWaitlist.avgWaitTime;
					}
				}

				// 检测候补状态变化
				await this.checkWaitlistSuccess(currentWaitlist);

				// 通过排班ID获取详细信息（包含医生、科室等）
				if (this.scheduleId) {
					try {
						const scheduleDetails = await getScheduleDetailsById(this.scheduleId);
						console.log('排班详细信息:', scheduleDetails);
						console.log('当前前端日期:', this.scheduleInfo.date);
						console.log('后端返回日期:', scheduleDetails?.scheduleDate);

						if (scheduleDetails) {
							// 设置医生信息
							this.doctorInfo.id = scheduleDetails.doctorId;
							this.doctorInfo.name = scheduleDetails.doctorName || '未知医生';
							this.doctorInfo.title = ''; // 排班信息中没有职称，需要时可以额外查询
							this.doctorInfo.clinicName = scheduleDetails.departmentName || '未知科室';

							// 设置排班信息 - 优先使用前端传入的日期，避免后端返回错误日期覆盖
							// 只有在没有传入日期时才使用后端返回的日期
							if (!this.scheduleInfo.date || this.scheduleInfo.date === '') {
								this.scheduleInfo.date = this.formatDate(scheduleDetails.scheduleDate);
							}
							
							// 如果传入的日期和后端返回的日期不一致，使用传入的日期并记录警告
							const backendDate = this.formatDate(scheduleDetails.scheduleDate);
							if (this.scheduleInfo.date && backendDate && this.scheduleInfo.date !== backendDate) {
								console.warn('日期不一致警告:', {
									前端传入: this.scheduleInfo.date,
									后端返回: backendDate,
									排班ID: this.scheduleId
								});
								// 保持使用前端传入的日期
							}
							
							// 时间段信息
							if (scheduleDetails.timeSlot) {
								this.scheduleInfo.timeSlot = scheduleDetails.timeSlot;
							}

							// 设置时间段显示文本
							const timeSlotMap = {
								'morning': '上午',
								'afternoon': '下午',
								'evening': '晚上'
							};
							const lowerCaseTimeSlot = scheduleDetails.timeSlot ? scheduleDetails.timeSlot.toLowerCase() : '';
							this.scheduleInfo.timeSlotDisplay = timeSlotMap[lowerCaseTimeSlot] || scheduleDetails.timeSlot;
						}
					} catch (e) {
						console.error('获取排班详细信息失败:', e);
						// 如果获取失败，保留原有的参数信息
					}
				}
			} catch (e) {
				console.error('加载候补信息失败:', e);
				uni.showToast({
					title: e.msg || '加载失败',
					icon: 'none'
				});
			} finally {
				if (!silent) {
					this.loading = false;
				}
			}
		},

		// 检测候补状态变化
		async checkWaitlistSuccess(currentWaitlist) {
			// 记录当前候补状态
			const currentStatus = currentWaitlist ? 'waiting' : 'none';

			// 第一次检测，只记录状态
			if (this.lastWaitlistStatus === null) {
				this.lastWaitlistStatus = currentStatus;
				console.log('初始化候补状态:', currentStatus);

				// 如果有候补记录，启动预约检查定时器
				if (currentWaitlist) {
					this.startAppointmentCheck();
				}
				return;
			}

			// 检测候补记录消失
			if (this.lastWaitlistStatus === 'waiting' && currentStatus === 'none') {
				console.log('检测到候补记录消失');

				// 停止所有定时器
				this.stopAllTimers();

				// 检查是否有新预约
				const hasAppointment = await this.checkForNewAppointment();

				if (hasAppointment) {
					console.log('✅ 候补成功！已找到新预约');
					this.handleWaitlistSuccess();
				} else {
					console.log('❌ 候补记录消失但未找到新预约（可能是手动退出）');
					// 返回上一页
					setTimeout(() => {
						uni.navigateBack();
					}, 1000);
				}
			}

			// 更新状态
			this.lastWaitlistStatus = currentStatus;
		},

		// 启动预约检查定时器
		startAppointmentCheck() {
			console.log('启动预约检查定时器，每', this.checkInterval / 1000, '秒检查一次');

			// 清除旧的定时器
			if (this.checkAppointmentTimer) {
				clearInterval(this.checkAppointmentTimer);
			}

			// 启动新的定时器
			this.checkAppointmentTimer = setInterval(async () => {
				this.currentCheckCount++;
				console.log(`第 ${this.currentCheckCount} 次检查预约...`);

				// 达到最大检查次数
				if (this.currentCheckCount >= this.maxCheckCount) {
					console.log('达到最大检查次数，停止检查');
					this.stopAllTimers();
					return;
				}

				// 检查是否有新预约
				const hasAppointment = await this.checkForNewAppointment();
				if (hasAppointment) {
					console.log('✅ 检测到新预约！候补成功');
					this.stopAllTimers();
					this.handleWaitlistSuccess();
				}
			}, this.checkInterval);
		},

		// 检查是否有新预约
		async checkForNewAppointment() {
			try {
				const data = await request({ url: '/appointment/me', method: 'GET', silent: true });
				const appointments = Array.isArray(data) ? data : ((data && data.list) ? data.list : []);

				console.log('当前预约列表:', appointments);

				// 查找匹配的预约：scheduleId匹配 且 来源是候补
				const newAppointment = appointments.find(apt => {
					const match = apt.scheduleId === this.scheduleId && apt.sourceType === 'WAITLIST';
					if (match) {
						console.log('找到匹配的预约:', apt);
					}
					return match;
				});

				return !!newAppointment;
			} catch (err) {
				console.error('检查预约失败:', err);
				return false;
			}
		},

		// 停止所有定时器
		stopAllTimers() {
			if (this.refreshTimer) {
				clearInterval(this.refreshTimer);
				this.refreshTimer = null;
			}
			if (this.checkAppointmentTimer) {
				clearInterval(this.checkAppointmentTimer);
				this.checkAppointmentTimer = null;
			}
		},

		// 处理候补成功
		async handleWaitlistSuccess() {
			// 写入消息中心
			this.addSuccessMessage();

			// 请求订阅消息授权（候补成功通知）
			try {
				const { requestWaitlistSubscribe } = require('@/utils/wechat-subscribe')
				await requestWaitlistSubscribe()
			} catch (e) {
				console.log('订阅消息授权失败（不影响候补）:', e)
			}

			// 显示成功弹窗
			this.showSuccessModal();
		},

		// 添加成功消息到消息中心
		addSuccessMessage() {
			try {
				const messages = uni.getStorageSync('user_messages') || [];
				const newMessage = {
					id: Date.now(),
					type: 'waitlist_success',
					title: '候补成功',
					content: `您的候补已成功转为预约，医生：${this.doctorInfo.name || '未知医生'}，时间：${this.scheduleInfo.date} ${this.scheduleInfo.timeSlotDisplay}`,
					time: new Date().toISOString(),
					read: false
				};
				messages.unshift(newMessage);
				// 只保留最近50条消息
				if (messages.length > 50) {
					messages.splice(50);
				}
				uni.setStorageSync('user_messages', messages);
				console.log('候补成功消息已写入消息中心');
			} catch (e) {
				console.error('写入消息中心失败:', e);
			}
		},

		// 显示候补成功弹窗
		showSuccessModal() {
			uni.showModal({
				title: '🎉 候补成功',
				content: '您的候补已成功转为预约！请前往就诊记录查看详情，或稍后查看消息通知。',
				confirmText: '查看记录',
				cancelText: '稍后查看',
				success: (res) => {
					if (res.confirm) {
						// 通过全局数据传递参数，因为switchTab不支持URL参数
						getApp().globalData.waitlistSuccess = {
							from: 'waitlist_success',
							scheduleId: this.scheduleId,
							timestamp: Date.now()
						};
						// 跳转到记录页面
						uni.switchTab({
							url: '/pages/records/records'
						});
					} else {
						// 返回上一页
						uni.navigateBack();
					}
				}
			});
		},

		// 启动自动刷新
		startAutoRefresh() {
			console.log('启动自动刷新，每30秒刷新一次候补状态');
			// 每30秒刷新一次数据，使用静默模式
			this.refreshTimer = setInterval(() => {
				this.loadWaitlistInfo(true); // 静默刷新
			}, 30000);
		},

		// 手动刷新数据
		async refreshData() {
			uni.showLoading({ title: '刷新中...' });
			await this.loadWaitlistInfo(false); // 非静默刷新
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
		},

		// 格式化日期 - 避免时区问题
		formatDate(dateStr) {
			if (!dateStr) return '';
			
			// 如果已经是 YYYY-MM-DD 格式，直接返回
			if (typeof dateStr === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
				return dateStr;
			}
			
			// 如果是 YYYY-MM-DDTHH:mm:ss 格式，只取日期部分
			if (typeof dateStr === 'string' && dateStr.length >= 10) {
				const datePart = dateStr.substring(0, 10);
				if (/^\d{4}-\d{2}-\d{2}$/.test(datePart)) {
					return datePart;
				}
			}
			
			// 其他情况，尝试解析日期（使用本地时区）
			try {
				const date = new Date(dateStr);
				if (isNaN(date.getTime())) {
					return '';
				}
				// 使用本地时区的年月日，避免UTC转换
				const year = date.getFullYear();
				const month = String(date.getMonth() + 1).padStart(2, '0');
				const day = String(date.getDate()).padStart(2, '0');
				return `${year}-${month}-${day}`;
			} catch (e) {
				console.error('日期格式化失败:', dateStr, e);
				return '';
			}
		},
		// 格式化成功率
		formatSuccessRate(rate) {
			if (rate === null || rate === undefined) return '--';
			return rate.toFixed(0);
		},
		// 格式化等待时长
		formatWaitTime(hours) {
			if (hours === null || hours === undefined) return '--';
			if (hours < 1) {
				return `${Math.round(hours * 60)}分钟`;
			} else if (hours < 24) {
				return `${hours.toFixed(1)}小时`;
			} else {
				const days = Math.floor(hours / 24);
				const remainingHours = hours % 24;
				if (remainingHours < 1) {
					return `${days}天`;
				}
				return `${days}天${remainingHours.toFixed(1)}小时`;
			}
		},
		// 获取成功率样式类
		getSuccessRateClass(rate) {
			if (rate === null || rate === undefined) return '';
			if (rate >= 70) return 'high';
		},
		
		// ========== WebSocket实时事件处理 ==========
		
		// 处理排队位次更新
		handleRankUpdate(data) {
			console.log('收到排队位次更新:', data);
			
			// 更新排队位次
			if (this.waitlistInfo.rank !== undefined) {
				const oldRank = this.waitlistInfo.rank;
				this.waitlistInfo.rank = data.rank;
				this.waitlistInfo.queueSize = data.queueSize;
				
				// 显示更新提示
				const rankText = `第${data.rank + 1}位`;
				if (oldRank !== data.rank) {
					uni.showToast({
						title: `位次更新：${rankText}`,
						icon: 'none',
						duration: 2000
					});
				}
				
				// 如果排名靠前，可以订阅候补状态
				if (data.rank < 3 && this.scheduleId) {
					webSocketManager.subscribeWaitlist(this.scheduleId);
				}
			}
		},
		
		// 处理候补成功事件
		handleWaitlistSuccessEvent(data) {
			console.log('收到候补成功通知:', data);
			
			// [修复] 验证通知中的scheduleId是否与当前页面的scheduleId匹配
			if (data.scheduleId && this.scheduleId && data.scheduleId !== this.scheduleId) {
				console.warn('候补成功通知的scheduleId不匹配:', {
					通知中的scheduleId: data.scheduleId,
					当前页面的scheduleId: this.scheduleId
				});
				// 如果不是当前页面的候补，不处理
				return;
			}
			
			// 保存当前正确的日期信息，避免被后端返回的错误日期覆盖
			const savedDate = this.scheduleInfo.date;
			const savedTimeSlot = this.scheduleInfo.timeSlot;
			
			// 刷新页面数据
			this.loadWaitlistInfo();
			
			// 恢复正确的日期信息（如果后端返回的日期错误）
			if (savedDate && this.scheduleInfo.date !== savedDate) {
				console.warn('检测到日期被错误覆盖，恢复正确日期:', {
					保存的日期: savedDate,
					后端返回: this.scheduleInfo.date
				});
				this.scheduleInfo.date = savedDate;
			}
			if (savedTimeSlot && this.scheduleInfo.timeSlot !== savedTimeSlot) {
				this.scheduleInfo.timeSlot = savedTimeSlot;
			}
			
			// 显示成功提示
			uni.showModal({
				title: '🎉 候补成功',
				content: `恭喜您！候补已成功转为预约\n医生：${data.doctorName}\n时间：${data.appointmentDate} ${data.timeSlot}`,
				confirmText: '查看预约',
				cancelText: '知道了',
				success: (res) => {
					if (res.confirm) {
						// 跳转到预约记录页面
						uni.switchTab({
							url: '/pages/records/records'
						});
					}
				}
			});
		},
		
		// 确保WebSocket连接
		ensureWebSocketConnection() {
			console.log('检查WebSocket连接状态');
			
			if (!webSocketManager.isConnected()) {
				console.log('WebSocket未连接，尝试连接');
				webSocketManager.connect();
			} else {
				console.log('WebSocket已连接');
				// 如果已连接且有scheduleId，订阅候补状态
				if (this.scheduleId) {
					webSocketManager.subscribeWaitlist(this.scheduleId);
				}
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

/* 候补可视化统计卡片 */
.stats-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.stats-content {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.stat-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.stat-row:last-child {
	border-bottom: none;
}

.stat-left {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.stat-label {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.stat-desc {
	font-size: 22rpx;
	color: #999;
}

.stat-value {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.stat-value.high {
	color: #4caf50;
}

.stat-value.medium {
	color: #ff9800;
}

.stat-value.low {
	color: #f44336;
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