<template>
	<view class="records-page">
		<view class="page-header">
			<text class="header-title">就诊记录</text>
			<text class="header-desc">查看您的就诊历史</text>
		</view>

		<!-- 分类筛选标签 -->
		<view class="filter-tabs">
			<view
				class="tab-item"
				:class="{ active: currentFilter === 'all' }"
				@click="filterRecords('all')"
			>
				全部
			</view>
			<view
				class="tab-item"
				:class="{ active: currentFilter === 'appointment' }"
				@click="filterRecords('appointment')"
			>
				预约
			</view>
			<view
				class="tab-item"
				:class="{ active: currentFilter === 'visit' }"
				@click="filterRecords('visit')"
			>
				就诊
			</view>
			<view
				class="tab-item"
				:class="{ active: currentFilter === 'waitlist' }"
				@click="filterRecords('waitlist')"
			>
				候补
			</view>
		</view>

		<view class="content">
			<view v-if="!filteredList.length" class="empty-state">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无记录</text>
				<text class="empty-desc">您还没有相关记录</text>
			</view>

			<view v-else>
				<view class="record-card" v-for="item in filteredList" :key="item.recordKey" @click="onCardClick(item)">
					<view class="row" v-if="item.type !== 'waitlist'">
						<text class="label">就诊医生</text>
						<text class="value">{{ item.doctorName || item.doctorTitle || ('#' + item.doctorId) || '未知医生' }}</text>
					</view>
					<view class="row" v-else>
						<text class="label">候补医生</text>
						<text class="value">{{ item.doctorName || item.doctorTitle || ('#' + item.doctorId) || '未知医生' }}</text>
					</view>
					<view class="row">
						<text class="label">就诊时间</text>
						<text class="value">{{ item.formattedAppointmentTime }} {{ item.timeSlotDisplay || '' }}</text>
					</view>
					<view class="row">
						<text class="label">状态</text>
						<text
							class="value status"
							:class="{
								pending: uiStatus(item) === 'PENDING' || uiStatus(item) === 'scheduled',
								confirmed: uiStatus(item) === 'CONFIRMED',
								completed: uiStatus(item) === 'COMPLETED' || uiStatus(item) === 'completed',
								waitlist: item.type === 'waitlist' || uiStatus(item) === 'WAITLIST',
								cancelled: uiStatus(item) === 'CANCELLED' || uiStatus(item) === 'cancelled',
								expired: uiStatus(item) === 'EXPIRED'
							}"
						>
							{{ getDisplayStatusText(item) }}
						</text>
						<text class="reselect-remain" v-if="item.rescheduleWindowExpires && (new Date(item.rescheduleWindowExpires)).getTime() > nowTs && (String(item.sourceType || '').toUpperCase() !== 'RESELECTED')">
							可重新选择：{{ getReselectRemainingText(item) }}
						</text>
					</view>
					<view class="row" v-if="item.fee !== undefined || item.actualFee !== undefined">
						<text class="label">费用</text>
						<text class="value price">¥{{ item.actualFee != null ? item.actualFee : (item.fee != null ? item.fee : 0) }}</text>
					</view>
					<view class="row" v-if="item.rank !== undefined">
						<text class="label">排队位次</text>
						<text class="value">{{ (item.rank !== null ? (item.rank + 1) : '未知') }} / {{ item.queueSize || '未知' }}</text>
					</view>
					<view class="actions" @click.stop v-if="item.type !== 'waitlist'">
						<button class="btn detail" @click.stop="viewDetail(item)">查看详情</button>
						<!-- 使用 uiStatus(item)（displayStatus 优先）判断是否显示可取消按钮，包含 RESCHEDULED -->
						<button v-if="uiStatus(item) === 'PENDING' || uiStatus(item) === 'scheduled' || uiStatus(item) === 'CONFIRMED' || uiStatus(item) === 'RESCHEDULED' || uiStatus(item) === 'RESELECTED'" class="btn cancel" @click.stop="cancel(item)">退号</button>
						<button v-if="canReselectItem(item)" class="btn reselect" @click.stop="showReselectOptions(item.id)">重新选择</button>
						<button class="btn delete" @click.stop="remove(item)">删除</button>
					</view>
					<view class="actions" @click.stop v-else>
						<button class="btn detail" @click.stop="viewWaitlistDetail(item)">查看详情</button>
						<button class="btn cancel" @click.stop="cancelWaitlist(item)">退出候补</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { deleteAppointment } from '@/api/appointment.js'
import { getMyWaitlist, cancelWaitlist } from '@/api/waitlist.js'
import request from '@/utils/request.js'

export default {
	data() {
		return {
			appointments: [],
			waitlists: [],
			currentFilter: 'all', // 默认显示全部记录
			fromWaitlistSuccess: false, // 是否从候补成功跳转过来
			targetScheduleId: null, // 目标排班ID
			pollTimer: null, // 轮询定时器
			pollCount: 0, // 轮询次数
			maxPollCount: 6 // 最大轮询次数（3秒*6=18秒）
			,
			nowTs: Date.now(),
			reselectInterval: null
		};
	},
	computed: {
		// 合并所有记录
		allRecords() {
			// 处理预约记录
			const appointmentRecords = this.appointments.map((item, index) => {
				// 格式化就诊时间
				let formattedTime = '未知时间';
				if (item.appointmentTime) {
					const date = new Date(item.appointmentTime);
					formattedTime = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
				} else if (item.scheduleDate) {
					const date = new Date(item.scheduleDate);
					formattedTime = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
				}

				return {
					...item,
					type: 'appointment',
					formattedAppointmentTime: formattedTime,
					timeSlotDisplay: this.getTimeSlotDisplay(item.timeSlot),
					recordKey: item.id != null ? `appointment_${item.id}` : `appointment_idx_${index}`,
					displayStatus: this.deriveAppointmentStatus(item)
				};
			});

			// 处理候补记录（使用后端返回的医生与排班信息，确保与数据库一致）
			const waitlistRecords = this.waitlists.map((item, index) => {
				let formattedTime = '未知日期';
				if (item.scheduleDate) {
					const d = new Date(item.scheduleDate);
					if (!isNaN(d.getTime())) {
						formattedTime = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
					}
				}
				return {
					scheduleId: item.scheduleId,
					rank: item.rank,
					queueSize: item.queueSize,
					type: 'waitlist',
					status: 'WAITLIST', // 候补状态
					id: `waitlist_${item.scheduleId}`,
					doctorName: item.doctorName || (item.doctorId ? ('#'+item.doctorId) : '未知医生'),
					formattedAppointmentTime: formattedTime,
					timeSlotDisplay: this.getTimeSlotDisplay(item.timeSlot) || item.timeSlotName || '',
					recordKey: item.scheduleId != null ? `waitlist_${item.scheduleId}` : `waitlist_idx_${index}`
				};
			});

			// 合并所有记录
			return [...appointmentRecords, ...waitlistRecords];
		},
		// 复合展示状态：如果是已重新安排，则在状态后追加“（已重新安排）”
		// 计算重新选择剩余时间显示文本（注意：此函数已移至 methods，避免在 computed 中调用带参数的函数）
		// 复合展示状态：如果是已重新安排，则在状态后追加“（已重新安排）”
		// 根据筛选条件过滤列表
		filteredList() {
			if (this.currentFilter === 'all') {
				return this.allRecords;
			} else if (this.currentFilter === 'appointment') {
				// 只显示预约记录（待就诊、已确认、以及重新安排/重新选择的预约）
				return this.allRecords.filter(item => {
					if (item.type !== 'appointment') return false;
					const derived = this.deriveAppointmentStatus(item);
					const src = (item.sourceType || '').toString().toUpperCase();
					// 包含派生为 RESCHEDULED 的，以及来源为 RESELECTED 的记录
					return derived === 'PENDING' || derived === 'CONFIRMED' || derived === 'RESCHEDULED' || src === 'RESELECTED';
				});
			} else if (this.currentFilter === 'visit') {
				// 只显示就诊记录（已完成）
				return this.allRecords.filter(item =>
					item.type === 'appointment' && (item.status === 'COMPLETED' || item.status === 'completed')
				);
			} else if (this.currentFilter === 'waitlist') {
				// 只显示候补记录
				return this.allRecords.filter(item => item.type === 'waitlist');
			}
			return this.allRecords;
		}
	},
	onLoad(options) {
		// 检查是否从候补成功跳转过来（URL参数方式）
		if (options.from === 'waitlist_success') {
			this.fromWaitlistSuccess = true;
			this.targetScheduleId = options.scheduleId ? parseInt(options.scheduleId) : null;
			this.currentFilter = 'appointment'; // 直接切换到预约筛选
		}

		// 从全局状态获取筛选条件
		const filter = getApp().globalData.recordFilter;
		if (filter && !this.fromWaitlistSuccess) {
			this.currentFilter = filter;
			// 清除全局状态
			getApp().globalData.recordFilter = null;
		}
	},
	onShow() {
		// 检查全局数据中是否有候补成功标记（switchTab方式传递）
		const waitlistSuccess = getApp().globalData.waitlistSuccess;
		if (waitlistSuccess && waitlistSuccess.from === 'waitlist_success') {
			console.log('检测到候补成功标记（全局数据）:', waitlistSuccess);
			this.fromWaitlistSuccess = true;
			this.targetScheduleId = waitlistSuccess.scheduleId ? parseInt(waitlistSuccess.scheduleId) : null;
			this.currentFilter = 'appointment'; // 直接切换到预约筛选
			// 清除全局标记
			getApp().globalData.waitlistSuccess = null;
		}

		this.loadData();

		// 拉取未读站内通知（回退方案：当 WS 不在线或漏推时，打开页面能看到通知）
		this.fetchAndShowUnreadNotifications();

		// 如果是从候补成功跳转过来，启动短期轮询
		if (this.fromWaitlistSuccess) {
			console.log('启动候补成功轮询...');
			this.startSuccessPolling();
		}

		// 添加事件监听
		uni.$on('appointment-rescheduled', this.handleAppointmentRescheduled);
		uni.$on('appointment-cancelled-refund', this.handleAppointmentCancelledRefund);
		// 启动重新选择倒计时刷新（用于卡片倒计时显示）
		if (!this.reselectInterval) {
			this.reselectInterval = setInterval(() => {
				this.nowTs = Date.now();
			}, 60 * 1000); // 每分钟更新一次
		}
	},
	onUnload() {
		// 清理轮询定时器
		this.stopPolling();
		// 移除事件监听
		uni.$off('appointment-rescheduled', this.handleAppointmentRescheduled);
		uni.$off('appointment-cancelled-refund', this.handleAppointmentCancelledRefund);
		// 清理重新选择倒计时
		if (this.reselectInterval) {
			clearInterval(this.reselectInterval);
			this.reselectInterval = null;
		}
	},
	// fetchAndShowUnreadNotifications moved into methods section to be accessible via this.*
	onPullDownRefresh() {
		this.handlePullDownRefresh();
	},
	methods: {
		// 复合展示状态：处理“已重新安排/已重新选择”提示
		getDisplayStatusText(item) {
			try {
				const base = this.statusName(this.uiStatus(item));
				const src = item?.sourceType || item?.displayStatus || item?.status || '';
				const srcUpper = String(src).toUpperCase();
				// 患者主动重新选择的状态，显示“已重新选择”
				if (srcUpper === 'RESELECTED') {
					return `${base}（已重新选择）`;
				}
				// 系统自动重新安排，或 autoAssigned 标记，显示“已重新安排”
				if (srcUpper === 'RESCHEDULED' || srcUpper === 'RESCHEDULE' || item?.autoAssigned) {
					return `${base}（已重新安排）`;
				}
				return base;
			} catch (e) {
				return this.statusName(this.uiStatus(item));
			}
		},
		// 计算重新选择剩余时间显示文本（可带参数调用）
		getReselectRemainingText(item) {
			try {
				const expires = item.rescheduleWindowExpires || item.reschedule_window_expires;
				if (!expires) return '';
				const expTs = (new Date(expires)).getTime();
				const diff = expTs - (this.nowTs || Date.now());
				if (diff <= 0) return '已过期';
				const hours = Math.floor(diff / (1000 * 60 * 60));
				const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
				if (hours > 0) return `${hours}小时${minutes}分`;
				return `${minutes}分`;
			} catch (e) {
				return '';
			}
		},
		// 计算重新选择剩余时间显示文本（移动到 methods 中，供模板调用）
		// 占位，实际实现位于 methods.getReselectRemainingText
		// 列表项是否允许直接重新选择（与详情页逻辑一致）
		canReselectItem(item) {
			try {
				// 仅允许操作自己的预约（或管理员/有特殊权限的用户）
				const currentUserId = this.$store && this.$store.state && this.$store.state.user && this.$store.state.user.userInfo
					? this.$store.state.user.userInfo.userId : null;
				if (currentUserId && item.patientId && Number(currentUserId) !== Number(item.patientId)) {
					return false;
				}
				// 仅允许系统自动分配/重新安排（RESCHEDULED）由患者重新选择一次，已重新选择（RESELECTED）不可再次选择
				if (item?.autoAssigned) {
					// 只有当原始来源是 RESCHEDULED 时允许患者操作
					const srcAuto = (item.sourceType || '').toString().toUpperCase();
					if (srcAuto !== 'RESCHEDULED') return false;
				} else {
					if (this.deriveAppointmentStatus(item) !== 'RESCHEDULED') return false;
				}
				const src = (item.sourceType || item.displayStatus || item.status || '').toString().toUpperCase();
				if (src === 'RESELECTED') return false;
				const expires = item.rescheduleWindowExpires || item.reschedule_window_expires || item.rescheduleWindowExpires;
				if (!expires) return true;
				const expTs = (new Date(expires)).getTime();
				return Date.now() <= expTs;
			} catch (e) {
				return false;
			}
		},
		// 获取号别类型的中文显示
		getSlotTypeDisplay(slotType) {
			if (!slotType) return '';
			const type = slotType.toUpperCase();
			switch (type) {
				case 'NORMAL':
					return '普通号';
				case 'EXPERT':
					return '专家号';
				case 'VIP':
					return '特需号';
				default:
					return slotType;
			}
		},
		// 拉取并展示未读的 IN_APP 通知（回退方案）
		async fetchAndShowUnreadNotifications() {
			try {
				const res = await request({ url: '/notifications', method: 'GET', silent: true });
				const notes = Array.isArray(res) ? res : (res && res.list ? res.list : []);
				if (!notes || notes.length === 0) return;

				// 只处理未读的 IN_APP 类型通知（后端已写入 IN_APP）
				const unreadInApp = notes.filter(n => !n.isRead && n.channel === 'IN_APP');
				if (unreadInApp && unreadInApp.length > 0) {
					// 展示第一个未读通知（避免一次弹太多）
					const n = unreadInApp[0];
					uni.showModal({
						title: n.title || '通知',
						content: n.content || '',
						confirmText: '查看详情',
						cancelText: '忽略',
						success: (resModal) => {
							// 无论确认还是忽略，都将该通知标记为已读，避免反复弹窗
							(async () => {
								try {
									await request({ url: `/notifications/${n.id}/read`, method: 'POST', silent: true });
								} catch (e) {
									console.warn('标记通知已读失败', e);
								}
							})();

							if (resModal.confirm) {
								// 跳转到预约列表/详情，若有关联预约则进入详情
								if (n.relatedType === 'APPOINTMENT' && n.relatedId) {
									uni.navigateTo({ url: `/pages/appointment-detail/appointment-detail?id=${n.relatedId}` });
								} else {
									uni.switchTab({ url: '/pages/records/records' });
								}
							}
						}
					});
				}
			} catch (e) {
				console.warn('拉取未读通知失败（静默）', e);
			}
		},
		async handlePullDownRefresh() {
			try {
				await this.loadData();
			} finally {
				uni.stopPullDownRefresh();
			}
		},
		onCardClick(item) {
			// 卡片区域点击：根据类型跳转到正确的详情页
			if (item && item.type === 'waitlist') {
				this.viewWaitlistDetail(item);
			} else {
				this.viewDetail(item);
			}
		},

		async loadData(silent = false) {
			// 分开加载，避免一个失败导致另一个也无法加载，特别是预约记录为空时
			try {
				const data = await request({ url: '/appointment/me', method: 'GET', silent: true });
				this.appointments = Array.isArray(data) ? data : ((data && data.list) ? data.list : []);
				if (!silent) {
					console.log('预约记录加载完成:', this.appointments.length, '条');
					console.log('预约记录详情:', this.appointments);
				}
			} catch (err) {
				// 没有预约记录时静默处理，不弹toast
				console.log('加载预约记录失败（可能无记录）:', err);
				this.appointments = [];
			}

			try {
				const data = await getMyWaitlist({ silent: true });
				const rawWaitlists = Array.isArray(data) ? data : ((data && data.list) ? data.list : []);
				// 只保留仍在候补中的记录，避免同一号源同时显示预约和候补
				this.waitlists = rawWaitlists.filter(item => {
					const status = (item.status || '').toUpperCase();
					return status === '' || status === 'WAITING';
				});
				if (!silent) {
					console.log('候补记录加载完成:', this.waitlists.length, '条');
				}
			} catch (err) {
				console.error('加载候补记录失败:', err);
				if (!silent) {
					uni.showToast({ title: err.msg || '加载候补记录失败', icon: 'none' });
				}
				this.waitlists = []; // 确保清空
			}
		},

		// 启动候补成功后的智能轮询 - 匹配微信小程序逻辑
		startSuccessPolling() {
			console.log('启动候补成功智能轮询，查找新预约，目标scheduleId:', this.targetScheduleId);

			const pollOnce = async () => {
				this.pollCount++;
				console.log(`轮询第 ${this.pollCount} 次...`);

				try {
					// 重新加载数据（静默模式）
					await this.loadData(true);

					console.log('当前预约列表:', this.appointments?.length || 0, '条记录');
					console.log('查找条件 - scheduleId:', this.targetScheduleId);

					// 查找匹配的预约（必须是从候补转换来的新预约）
					const foundAppointment = this.appointments?.find(apt => {
						console.log('检查预约:', {
							id: apt.id,
							scheduleId: apt.scheduleId,
							sourceType: apt.sourceType,
							status: apt.status,
							createTime: apt.createTime
						});

						// 必须满足：1. scheduleId匹配 2. 来源是候补 3. 状态是待就诊
						const isMatch = apt.scheduleId === this.targetScheduleId &&
							   apt.sourceType === 'WAITLIST' &&
							   (apt.status === 'PENDING' || apt.status === 'scheduled');

						if (isMatch) {
							console.log('找到匹配的候补预约:', apt);
						}

						return isMatch;
					});

					if (foundAppointment) {
						console.log('✅ 找到候补转换的新预约！', foundAppointment);
						uni.showToast({
							title: '已获取到新预约',
							icon: 'success',
							duration: 2000
						});

						// 停止轮询
						this.stopPolling();
						this.fromWaitlistSuccess = false;
						return;
					}

					// 如果未达到最大次数，继续轮询
					if (this.pollCount < this.maxPollCount) {
						// 动态调整轮询间隔：前3次3秒，后续5秒
						const interval = this.pollCount <= 3 ? 3000 : 5000;
						console.log(`未找到新预约，${interval/1000}秒后进行第${this.pollCount + 1}次轮询...`);
						this.pollTimer = setTimeout(pollOnce, interval);
					} else {
						console.log('轮询达到最大次数，停止轮询');
						uni.showToast({
							title: '预约可能正在处理中，请稍后手动刷新查看',
							icon: 'none',
							duration: 3000
						});
						this.stopPolling();
						this.fromWaitlistSuccess = false;
					}
				} catch (error) {
					console.error('轮询过程中出错:', error);
					// 出错时重试，但减少重试频率
					if (this.pollCount < this.maxPollCount) {
						console.log('轮询出错，10秒后重试...');
						this.pollTimer = setTimeout(pollOnce, 10000);
					} else {
						this.stopPolling();
						this.fromWaitlistSuccess = false;
					}
				}
			};

			// 延迟1秒后开始第一次轮询，给后端更多处理时间
			console.log('1秒后开始轮询...');
			this.pollTimer = setTimeout(pollOnce, 1000);
		},

		// 停止轮询
		stopPolling() {
			if (this.pollTimer) {
				clearTimeout(this.pollTimer);
				this.pollTimer = null;
				console.log('轮询已停止');
			}
		},
		// 筛选记录
		filterRecords(type) {
			this.currentFilter = type;
		},
		// 获取时间段显示文本
		getTimeSlotDisplay(timeSlot) {
			const timeSlotMap = {
				'morning': '上午',
				'afternoon': '下午',
				'evening': '晚上'
			};
			return timeSlotMap[timeSlot] || timeSlot || '';
		},
		// 将后端状态与当前时间结合，得出前端展示状态
		deriveAppointmentStatus(item) {
			const raw = (item?.status || '').toUpperCase();
		// 直接映射的终态
			if (raw === 'CANCELLED' || raw === 'CANCEL' || raw === 'REFUNDED' || raw === 'REFUND') return 'CANCELLED';
			if (raw === 'COMPLETED') return 'COMPLETED';
			if (raw === 'WAITLIST') return 'WAITLIST';
        // 系统自动重新安排视为已重新安排，前端显示“已重新安排”但仍允许重新选择/取消
        if (raw === 'RESCHEDULED' || raw === 'RESCHEDULE') return 'RESCHEDULED';

			// 动态态：待就诊/已过号
			const { start, end } = this.getAppointmentTimeRange(item);
			if (!start) return raw || 'PENDING';

			const nowTs = Date.now();
			const endTs = end || start;
			if (nowTs > endTs) return 'EXPIRED';

			// 未过号且已确认保持确认态，否则待就诊
			if (raw === 'CONFIRMED') return 'CONFIRMED';
			return 'PENDING';
		},
		uiStatus(item) {
			const status = item?.displayStatus || item?.status;
			return typeof status === 'string' ? status : '';
		},
		getAppointmentTimeRange(item) {
			const result = { start: null, end: null };
			if (!item) return result;

			// 优先使用精确的预约时间字段
			if (item.appointmentTime) {
				const startDate = new Date(item.appointmentTime);
				if (!Number.isNaN(startDate.getTime())) {
					result.start = startDate.getTime();
					if (item.appointmentEndTime) {
						const endDate = new Date(item.appointmentEndTime);
						if (!Number.isNaN(endDate.getTime())) result.end = endDate.getTime();
					} else {
						result.end = result.start;
					}
					return result;
				}
			}

			// 兜底：日期 + 时间段组合
			const dateStr = item.scheduleDate || item.appointmentDate;
			if (!dateStr) return result;
			const slotRange = this.getTimeSlotRange(item.timeSlot || item.timeSlotName);
			const startTime = item.startTime || item.beginTime || item.start || (slotRange && slotRange.start);
			const endTime = item.endTime || item.finishTime || item.end || (slotRange && slotRange.end) || startTime;

			if (startTime) {
				const ts = this.buildDateTime(dateStr, startTime);
				if (ts) result.start = ts;
			}
			if (endTime) {
				const ts = this.buildDateTime(dateStr, endTime);
				if (ts) result.end = ts;
			}

			return result;
		},
		getTimeSlotRange(slot) {
			const map = {
				morning: { start: '08:00', end: '12:00' },
				afternoon: { start: '14:00', end: '18:00' },
				fternoon: { start: '14:00', end: '18:00' },
				evening: { start: '18:00', end: '23:00' }
			};
			return map[slot] || null;
		},
		buildDateTime(dateStr, timeStr) {
			if (!dateStr || !timeStr) return null;
			const normalizedDate = dateStr.includes('T') ? dateStr.split('T')[0] : dateStr;
			const normalizedTime = timeStr.length === 5 ? `${timeStr}:00` : timeStr;
			const dt = new Date(`${normalizedDate}T${normalizedTime}`);
			return Number.isNaN(dt.getTime()) ? null : dt.getTime();
		},
		statusName(s) {
			if (!s) return '-';
			const status = s.toString();
			switch (s) {
				case 'PENDING': return '待就诊';
				case 'scheduled': return '待就诊';
				// 对于重新安排/重新选择，基础状态仍然显示为“待就诊”，复合后缀由 getDisplayStatusText 追加
				case 'RESCHEDULED': return '待就诊';
				case 'RESCHEDULE': return '待就诊';
				case 'RESELECTED': return '待就诊';
				case 'CONFIRMED': return '已确认';
				case 'COMPLETED': return '已完成';
				case 'completed': return '已完成';
				case 'CANCELLED': return '已退号';
				case 'cancelled': return '已退号';
				case 'WAITLIST': return '候补中';
				case 'EXPIRED': return '已过号';
				default: return status;
			}
		},
		cancel(item) {
			if (!item.id) {
				uni.showToast({ title: '缺少预约ID', icon: 'none' });
				return;
			}
			uni.navigateTo({
				url: `/pages/cancel-appointment/cancel-appointment?id=${item.id}`
			});
		},
		async remove(item) {
			try {
				await deleteAppointment(item.id);
				uni.showToast({ title: '已删除', icon: 'success' });
				this.loadData();
			} catch (e) {
				uni.showToast({ title: e.msg || '删除失败', icon: 'none' });
			}
		},
		async cancelWaitlist(item) {
			uni.showModal({
				title: '退出候补',
				content: '确定要退出候补吗？退出后将无法恢复',
				confirmText: '确认退出',
				confirmColor: '#f44336',
				success: async (res) => {
					if (res.confirm) {
						try {
							await cancelWaitlist(item.scheduleId);
							uni.showToast({ title: '已退出候补', icon: 'success' });
							this.loadData();
						} catch (e) {
							uni.showToast({ title: e.msg || '退出失败', icon: 'none' });
						}
					}
				}
			});
		},
		viewDetail(item) {
			// 跳转到挂号详情页
			uni.navigateTo({
				url: `/pages/appointment-detail/appointment-detail?id=${item.id}&status=${item.status || 'PENDING'}`
			});
		},
		viewWaitlistDetail(item) {
			// 跳转到候补详情页
			uni.navigateTo({
				url: `/pages/waitlist/waitlist?scheduleId=${item.scheduleId}`
			});
		},

		// 处理预约重新安排通知
		handleAppointmentRescheduled(data) {
			console.log('收到预约重新安排通知:', data);

			uni.showModal({
				title: '预约已重新安排',
				content: `您的预约因医生调班已重新安排至${data.doctorName}医生，时间：${data.appointmentDate} ${data.timeSlot}。您可以在24小时内重新选择医生，否则将按当前安排就诊。`,
				confirmText: '重新选择',
				cancelText: '接受安排',
				success: (res) => {
					if (res.confirm) {
						// 显示重新选择选项
						this.showReselectOptions(data.appointmentId);
					} else {
						// 接受当前安排
						uni.showToast({
							title: '已接受当前安排',
							icon: 'success',
							duration: 2000
						});
					}
				}
			});
		},

		// 处理预约取消退款通知
		handleAppointmentCancelledRefund(data) {
			console.log('收到预约取消退款通知:', data);

			uni.showModal({
				title: '预约已取消',
				content: `您的预约因医生调班已取消，费用已退还至原支付账户。时间：${data.appointmentDate} ${data.timeSlot}`,
				showCancel: false,
				confirmText: '知道了',
				success: () => {
					// 刷新数据
					this.loadData();
				}
			});
		},

		// 显示重新选择选项
		async showReselectOptions(appointmentId) {
			try {
				// ownership check: prevent calling API for appointments that don't belong to current user
				const appointment = this.appointments.find(a => a.id === appointmentId) || {};
				const currentUserId = this.$store && this.$store.state && this.$store.state.user && this.$store.state.user.userInfo
					? this.$store.state.user.userInfo.userId : null;
				if (currentUserId && appointment.patientId && Number(currentUserId) !== Number(appointment.patientId)) {
					uni.showToast({ title: '无权限操作此预约', icon: 'none' });
					return;
				}
				console.debug('showReselectOptions called, appointmentId=', appointmentId);
				uni.showLoading({ title: '加载选项中...' });

				// 获取可重新选择的选项
				const response = await request({
					url: `/appointment/${appointmentId}/reselect-options`,
					method: 'GET'
				});

				uni.hideLoading();
				console.debug('reselect-options response:', response);

				// 兼容多种后端返回格式：{ list: [...] } 或直接返回数组
				const list = Array.isArray(response) ? response : (Array.isArray(response?.list) ? response.list : []);

				if (list && list.length > 0) {
					// 显示选项列表让用户选择
					this.showOptionSelector(appointmentId, list);
				} else {
					uni.showModal({
						title: '无可用选项',
						content: '当前没有其他可选择的医生时段，是否接受当前安排？',
						confirmText: '接受安排',
						cancelText: '稍后再选',
						success: (res) => {
							if (res.confirm) {
								uni.showToast({
									title: '已接受当前安排',
									icon: 'success'
								});
							}
						}
					});
				}
			} catch (error) {
				uni.hideLoading();
				console.error('获取重新选择选项失败:', error);
				uni.showToast({
					title: error?.msg || error?.message || '获取选项失败',
					icon: 'none'
				});
			}
		},

		// 显示选项选择器
		showOptionSelector(appointmentId, options) {
			const optionList = options.map((option, index) => {
				const dateStr = option.date ? new Date(option.date).toLocaleDateString('zh-CN') : '';
				const timeSlot = option.timeSlot || '';
				const doctorTitle = option.doctorTitle || '';
				const department = option.departmentName || '';
				const slotType = this.getSlotTypeDisplay(option.slotType);
				const fee = option.fee ? `¥${option.fee}` : '';
				const available = option.availableSlots ? `剩余${option.availableSlots}个` : '';

				// 构建美观的显示文本
				let displayText = `${option.doctorName}`;
				if (doctorTitle) displayText += `(${doctorTitle})`;
				if (department) displayText += ` - ${department}`;
				displayText += `\n${dateStr} ${timeSlot}`;
				if (slotType || fee || available) {
					const details = [slotType, fee, available].filter(Boolean).join(' ');
					displayText += `\n${details}`;
				}

				return {
					text: displayText,
					value: option.scheduleId,
					index: index,
					option: option
				};
			});

			uni.showActionSheet({
				itemList: optionList.map(item => item.text),
				success: (res) => {
					const selectedOption = optionList[res.tapIndex];
					this.confirmReselect(appointmentId, selectedOption.value, selectedOption.text);
				},
				fail: (res) => {
					console.log('用户取消选择');
				}
			});
		},

		// 确认重新选择
		confirmReselect(appointmentId, newScheduleId, optionText) {
			uni.showModal({
				title: '确认重新选择',
				content: optionText,
				confirmText: '确认',
				cancelText: '取消',
				success: async (res) => {
					if (res.confirm) {
						try {
							uni.showLoading({ title: '重新选择中...' });

							const response = await request({
								url: `/appointment/${appointmentId}/reselect`,
								method: 'POST',
								data: {
									newScheduleId: newScheduleId
								}
							});

							uni.hideLoading();

							if (response) {
								uni.showToast({
									title: '重新选择成功',
									icon: 'success',
									duration: 2000
								});
								// 刷新数据
								this.loadData();
							}
						} catch (error) {
							uni.hideLoading();
							uni.showToast({
								title: error.msg || '重新选择失败',
								icon: 'none'
							});
						}
					}
				}
			});
		}
	}
};
</script>

<style scoped>
.records-page {
	min-height: 100vh;
	background: #f5f7fa;
}
.page-header {
	background: linear-gradient(135deg, #1976d2 0%, #2196f3 100%);
	padding: 40rpx 30rpx;
	color: #fff;
}
.header-title {
	font-size: 36rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}
.header-desc {
	font-size: 24rpx;
	opacity: 0.9;
}
/* 筛选标签样式 */
.filter-tabs {
	display: flex;
	background: #fff;
	padding: 20rpx 0;
	margin-bottom: 20rpx;
}
.tab-item {
	flex: 1;
	text-align: center;
	padding: 15rpx 0;
	font-size: 28rpx;
	color: #666;
}
.tab-item.active {
	color: #1976d2;
	font-weight: bold;
	border-bottom: 4rpx solid #1976d2;
}
.content {
	padding: 0 30rpx 60rpx;
}
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 120rpx 0;
}
.empty-icon {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.3;
}
.empty-text {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 15rpx;
}
.empty-desc {
	font-size: 24rpx;
	color: #999;
}
.record-card{
	background:#fff;
	border-radius:16rpx;
	padding:26rpx 26rpx 16rpx;
	margin-bottom:20rpx;
	cursor: pointer;
	transition: all 0.3s;
}
.record-card:active{
	transform: scale(0.98);
	opacity: 0.9;
}
.row{
	display:flex;
	justify-content:space-between;
	padding:12rpx 0;
	border-bottom:1rpx solid #f5f5f5;
}
.row:last-child{ border-bottom:none; }
.label{ color:#666; font-size:26rpx; }
.value{ color:#333; font-size:28rpx; }
.price{ color:#ff5722; font-weight:600; }
.status{ font-weight:600; }
.status.pending{ color:#ff9800; }
.status.confirmed{ color:#1976d2; }
.status.completed{ color:#4caf50; }
.status.cancelled{ color:#999; }
.status.waitlist{ color:#9c27b0; }
.status.expired{ color:#f57c00; }
.actions{
	display:flex;
	justify-content:flex-end;
	gap:16rpx;
	padding-top:10rpx;
}
.btn{
	font-size:26rpx;
	height:64rpx;
	line-height:64rpx;
	padding:0 24rpx;
	border-radius:32rpx;
}
.detail{ background:#e3f2fd; color:#1976d2; }
.cancel{ background:#fff3e0; color:#f57c00; }
.delete{ background:#ffebee; color:#d32f2f; }
</style>