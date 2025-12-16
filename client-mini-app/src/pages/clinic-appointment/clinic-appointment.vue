<template>
	<view class="clinic-appointment-page">
		<!-- 门诊信息条 -->
		<view class="clinic-header">
			<view class="clinic-icon">💊</view>
			<view class="clinic-info">
				<text class="clinic-name">{{ clinicName }}</text>
				<view class="clinic-sub">
					<text v-if="departmentName">所属科室：{{ departmentName }} · </text>
					<text>请选择预约日期，查看当日上/下午号源</text>
				</view>
			</view>
		</view>

		<!-- 日期选择 -->
		<view class="section">
			<view class="section-title">选择预约日期</view>
			<scroll-view scroll-x class="date-scroll">
				<view class="date-list">
					<view
						class="date-item"
						:class="{ active: selectedDate === d.date }"
						v-for="d in dateList"
						:key="d.date"
						@click="onClickDate(d.date)"
					>
						<text class="date-week">{{ d.week }}</text>
						<text class="date-day">{{ d.day }}</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 当日号源（分上/下午，展示医生） -->
		<view class="section">
			<view class="section-title">当日号源</view>
			<view class="period" v-for="p in periods" :key="p">
				<view class="period-header">
					<text class="period-icon">{{ periodIcon(p) }}</text>
					<text class="period-name">{{ periodName(p) }}</text>
				</view>
				<view class="slot-list">
					<view
						class="slot-card"
						v-for="s in getSlotsByPeriod(p)"
						:key="s.id"
						:class="{
							active: selectedSlot && selectedSlot.id === s.id,
							'no-slots': s.availableSlots === 0,
							expired: isExpiredSlot(s)
						}"
						@click="selectSlot(s)"
					>
						<view class="slot-top">
							<text class="slot-time">{{ slotTimeDisplay(s) }}</text>
							<text 
								class="slot-remain" 
								:class="{ 'no-slots': s.availableSlots === 0, expired: isExpiredSlot(s) }"
							>
								{{ isExpiredSlot(s) ? '已过期' : `余${s.availableSlots}` }}
							</text>
						</view>
						<view class="slot-bottom">
							<text class="slot-doctor">{{ s.doctorName || '医生' }}</text>
							<view class="slot-meta">
								<text class="slot-type">{{ getSlotType(s) }}</text>
								<text class="slot-price">¥{{ getSlotPrice(s) }}</text>
							</view>
						</view>
					</view>
		</view>
				</view>
			</view>

		<!-- 底部操作 -->
		<view class="bottom-bar">
			<button class="primary-btn" :disabled="!selectedSlot" @click="goConfirm" v-if="selectedSlot && selectedSlot.availableSlots > 0">
				立即挂号
			</button>
			<button class="waitlist-bottom-btn" :disabled="!selectedSlot" @click="joinWaitlist(selectedSlot)" v-else-if="selectedSlot && selectedSlot.availableSlots === 0">
				候补排队
			</button>
			<button class="primary-btn" disabled v-else>
				请选择号源
			</button>
		</view>

		<!-- 加载 -->
		<view class="loading-mask" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getDoctorList, getDoctorSchedules } from '@/api/doctor.js';
import { joinWaitlist, createWaitlistPrepayment, payWaitlistOrder } from '@/api/waitlist.js';

export default {
	data() {
		return {
			clinicId: null,
			clinicName: '',
			departmentName: '',
			dateList: [],
			selectedDate: '',
			doctors: [],
			slotsOfSelectedDate: [],
			selectedSlot: null,
			loading: false
		};
	},
	computed: {
		periods() {
			return ['morning', 'afternoon'];
		}
	},
	onLoad(query) {
		// 测试：确认进入了正确的页面
		console.log('========== 门诊预约页面已加载 ==========');
		console.log('页面路径: pages/clinic-appointment/clinic-appointment');
		console.log('接收到的参数:', query);
		
		const id = Number(query.clinicId);
		this.clinicId = Number.isNaN(id) ? null : id;
		this.clinicName = decodeURIComponent(query.clinicName || '');
		this.departmentName = decodeURIComponent(query.departmentName || '');
		
		console.log('解析后的参数:', {
			clinicId: this.clinicId,
			clinicName: this.clinicName,
			departmentName: this.departmentName
		});
		
		// 必须要有门诊ID或门诊名称
		if (!this.clinicId && !this.clinicName) {
			uni.showToast({
				title: '门诊信息缺失',
				icon: 'none'
			});
			setTimeout(() => {
				uni.navigateBack();
			}, 1500);
			return;
		}
		
		this.initDates();
		this.loadDoctorsAndSchedules();
	},
	onPullDownRefresh() {
		this.handlePullDownRefresh();
	},
	methods: {
		async handlePullDownRefresh() {
			try {
				await this.loadDoctorsAndSchedules();
			} finally {
				uni.stopPullDownRefresh();
			}
		},
		initDates() {
			const list = [];
			const weeks = ['日', '一', '二', '三', '四', '五', '六'];
			for (let i = 0; i < 7; i++) {
				const d = new Date();
				d.setDate(d.getDate() + i);
				const y = d.getFullYear();
				const m = String(d.getMonth() + 1).padStart(2, '0');
				const day = String(d.getDate()).padStart(2, '0');
				const date = `${y}-${m}-${day}`;
				list.push({
					date,
					week: i === 0 ? '今天' : `周${weeks[d.getDay()]}`,
					day: `${d.getMonth() + 1}/${d.getDate()}`
				});
			}
			this.dateList = list;
			this.selectedDate = list[0].date;
		},
		async loadDoctorsAndSchedules() {
			console.log('========== 开始加载医生和排班 ==========');
			console.log('过滤条件 - clinicId:', this.clinicId, 'clinicName:', this.clinicName);
			
			if (!this.clinicId && !this.clinicName) {
				console.warn('loadDoctorsAndSchedules: clinicId 和 clinicName 都为空');
				return;
			}
			this.loading = true;
			try {
				// 1) 拉取所有医生并按门诊过滤（主要使用门诊名称）
				const allDoctors = await getDoctorList().catch(() => []);
				console.log('获取到所有医生数量:', allDoctors ? allDoctors.length : 0);
				
				// 关键：必须过滤，不能使用所有医生
				this.doctors = this.filterDoctorsByClinic(allDoctors, this.clinicId, this.clinicName);
				console.log('过滤后的医生数量:', this.doctors ? this.doctors.length : 0);
				console.log('过滤后的医生列表:', this.doctors.map(d => ({ id: d.id, name: d.name, clinicName: d?.clinic?.name })));
				
				// 如果过滤后没有医生，直接返回，不查询排班
				if (!this.doctors || this.doctors.length === 0) {
					console.warn('过滤后没有找到匹配的医生，不查询排班');
					this._allSchedules = [];
					this.updateSlotsForSelectedDate([]);
					return;
				}
				
				// 2) 拉取7天内每位医生的排班并汇总（只查询过滤后的医生）
				console.log('开始查询', this.doctors.length, '个医生的排班...');
				const startDate = this.dateList[0].date;
				const endDate = this.dateList[this.dateList.length - 1].date;
				const all = [];
				for (const d of this.doctors) {
					console.log('查询医生', d.id, d.name, '的排班');
					const list = await getDoctorSchedules(d.id, startDate, endDate).catch(() => []);
					for (const s of list) {
						all.push({
							...s,
							doctorId: d.id,
							doctorName: d.name,
							doctorTitle: d.title
						});
					}
				}
				console.log('总共获取到', all.length, '个号源');
				this._allSchedules = all;
				this.updateSlotsForSelectedDate(all);
			} finally {
				this.loading = false;
			}
		},
		filterDoctorsByClinic(list, clinicId, clinicName) {
			if (!Array.isArray(list) || list.length === 0) {
				return [];
			}
			
			// 必须要有过滤条件，否则返回空数组（不返回所有医生）
			if (!clinicId && (!clinicName || !String(clinicName).trim())) {
				return [];
			}
			
			// 优先使用门诊ID过滤（最准确，因为ID是唯一的）
			const filtered = [];
			for (const d of list) {
				if (!d) continue;
				
				let matched = false;
				
				// 首先使用门诊ID匹配（最可靠，因为ID是唯一的）
				if (clinicId) {
					const targetClinicId = Number(clinicId);
					const cid = d?.clinic?.id ?? d?.clinicId ?? d?.clinic_id;
					if (cid != null && Number(cid) === targetClinicId) {
						matched = true;
					}
				}
				
				// 如果门诊ID匹配失败，再尝试使用门诊名称匹配（作为备选）
				// 注意：门诊名称应该是具体的门诊名称（如"内分泌科门诊"），不是科室名称（如"内科"）
				if (!matched && clinicName && String(clinicName).trim()) {
					// 检查多种可能的字段名：clinic.name, clinicName, clinic_name
					const doctorClinicName = d?.clinic?.name ?? d?.clinicName ?? d?.clinic_name;
					if (doctorClinicName && String(doctorClinicName).trim() === String(clinicName).trim()) {
						matched = true;
					}
				}
				
				if (matched) {
					filtered.push(d);
				}
			}
			
			return filtered;
		},
		updateSlotsForSelectedDate(allSchedules) {
			this.selectedSlot = null;
			this.slotsOfSelectedDate = (allSchedules || []).filter(s => s.date === this.selectedDate);
		},
		onClickDate(date) {
			this.selectedDate = date;
			if (this._allSchedules && Array.isArray(this._allSchedules)) {
				this.updateSlotsForSelectedDate(this._allSchedules);
			}
		},
		getSlotsByPeriod(period) {
			return this.slotsOfSelectedDate
				.filter(s => s.period === period)
				.sort((a, b) => {
					const rangeA = this.getSlotTimeRange(a);
					const rangeB = this.getSlotTimeRange(b);
					return (rangeA.start || '').localeCompare(rangeB.start || '');
				});
		},
		slotTimeDisplay(slot) {
			const range = this.getSlotTimeRange(slot);
			return `${range.start || '--'}-${range.end || '--'}`;
		},
		getSlotTimeRange(slot) {
			if (!slot) return { start: '', end: '' };
			const directStart = slot.startTime || slot.beginTime || slot.start || slot.start_time;
			const directEnd = slot.endTime || slot.finishTime || slot.end || slot.end_time;
			const period = slot.period || slot.timeSlot || slot.time_slot;
			const fallback = this.getPeriodTimeRange(period);
			// 优先用 period 对应的标准时间段；只有在无 period 时才使用接口提供的起止时间
			const start = fallback.start || directStart || '';
			const end = fallback.end || directEnd || start;
			console.log('[slot-range]', {
				id: slot.id,
				period: slot.period,
				timeSlot: slot.timeSlot,
				time_slot: slot.time_slot,
				directStart,
				directEnd,
				finalStart: start,
				finalEnd: end
			});
			return { start, end };
		},
		getPeriodTimeRange(period) {
			const key = (period || '').toLowerCase();
			if (key === 'morning') {
				return { start: '08:00', end: '12:00' };
			}
			if (key === 'afternoon' || key === 'fternoon') {
				return { start: '14:00', end: '18:00' };
			}
			return { start: '08:00', end: '12:00' };
		},
		normalizeTimeString(timeStr) {
			if (!timeStr) return '';
			if (timeStr.length === 5) {
				return `${timeStr}:00`;
			}
			return timeStr;
		},
		isExpiredSlot(slot) {
			// 仅对“今天”的号源进行过期判断，其余日期不受影响
			if (!slot || this.selectedDate !== this.dateList?.[0]?.date) return false;
			const now = new Date();
			const range = this.getSlotTimeRange(slot);
			const end = range.end || range.start;
			if (!end) return false;
			const endDateTime = new Date(`${this.selectedDate}T${this.normalizeTimeString(end)}`);
			if (Number.isNaN(endDateTime.getTime())) return false;
			return now.getTime() > endDateTime.getTime();
		},
		periodIcon(p) {
			if (p === 'morning') return '🌅';
			if (p === 'afternoon') return '🌆';
			return '';
		},
		periodName(p) {
			if (p === 'morning') return '上午';
			if (p === 'afternoon') return '下午';
			return '';
		},
		slotCardClass(s) {
			return {
				active: this.selectedSlot && this.selectedSlot.id === s.id,
				'no-slots': s.availableSlots === 0
			};
		},
		getSlotType(s) {
			const t = s.slotType || this.mapDoctorTitleToSlotType(s.doctorTitle);
			return t || '普通号';
		},
		getSlotPrice(s) {
			// 1) 若后端直接给出价格字段，则以接口为准
			const raw = s.price ?? s.fee ?? s.amount;
			if (raw != null && !Number.isNaN(Number(raw))) {
				return Number(raw).toFixed(2);
			}

			// 2) 若有 slotType，则按与后端 SystemConfig 相同的规则映射
			const slotType = (s.slotType || '').toString().trim().toUpperCase();
			if (slotType === 'VIP') {
				return '100.00';
			}
			if (slotType === 'EXPERT') {
				return '50.00';
			}

			// 3) 兜底：根据职称估价，但金额仍然与普通号 15 元保持一致
			if (s.doctorTitle === '主任医师') return '50.00';
			// 其他职称统一按普通号处理
			return '15.00';
		},
		mapDoctorTitleToSlotType(title) {
			if (title === '主任医师') return '专家号';
			if (title === '副主任医师') return '副主任号';
			if (title === '主治医师') return '主治号';
			return '普通号';
		},
		selectSlot(s) {
			if (!s) return;
			if (this.isExpiredSlot(s)) {
				uni.showToast({ title: '该号源已过期', icon: 'none' });
				return;
			}
			this.selectedSlot = s;
		},
		async joinWaitlist(slot) {
			if (!slot) return;
			const queueConfirm = await this.showConfirmModal(`确定要加入"${slot.doctorName}"医生${this.formatDate(slot.date)}${this.periodName(slot.period)}的候补队列吗？`);
			if (!queueConfirm) return;

			try {
				uni.showLoading({ title: '创建预支付...' });
				const order = await createWaitlistPrepayment({ scheduleId: slot.id });
				uni.hideLoading();

				const payConfirmed = await this.confirmPayment(order.actualFee);
				if (!payConfirmed) return;

				await payWaitlistOrder({
					orderNo: order.orderNo,
					paymentMethod: 'WECHAT',
					paidAmount: order.actualFee
				});

				await joinWaitlist({ scheduleId: slot.id, waitlistId: order.waitlistId });

				uni.showToast({
					title: '已加入候补队列',
					icon: 'success',
					success: () => {
						uni.navigateTo({
							url: `/pages/waitlist/waitlist?scheduleId=${slot.id}&doctorId=${slot.doctorId}&scheduleDate=${slot.date}&timeSlot=${slot.period}`
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
					success: (res) => resolve(res.confirm === true)
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
		},
		formatDate(dateStr) {
			if (!dateStr) return '';
			const date = new Date(dateStr);
			return `${date.getMonth() + 1}月${date.getDate()}日`;
		},
		goConfirm() {
			if (!this.selectedSlot) {
				uni.showToast({ title: '请选择号源', icon: 'none' });
				return;
			}
			if (this.selectedSlot.availableSlots === 0) {
				uni.showToast({ title: '号源已满，请选择候补', icon: 'none' });
				return;
			}
			uni.navigateTo({
				url: `/pkg-order/order-confirm/order-confirm?doctorId=${this.selectedSlot.doctorId}&scheduleId=${this.selectedSlot.id}`
			});
		}
	}
};
</script>

<style scoped>
.clinic-appointment-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 140rpx;
}
.clinic-header {
	background: #fff;
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 24rpx 30rpx;
	margin-bottom: 16rpx;
}
.clinic-icon {
	font-size: 40rpx;
}
.clinic-info { display: flex; flex-direction: column; gap: 6rpx; }
.clinic-name { font-size: 32rpx; font-weight: bold; color: #333; }
.clinic-sub { font-size: 24rpx; color: #999; }
.section { background: #fff; margin-top: 16rpx; padding: 20rpx 0; }
.section-title { font-size: 28rpx; font-weight: 600; color: #333; padding: 0 30rpx 10rpx; }
.date-scroll { white-space: nowrap; }
.date-list { display: flex; gap: 16rpx; padding: 0 30rpx; }
.date-item { width: 120rpx; height: 120rpx; border-radius: 16rpx; background: #f8faff; color: #1976d2; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4rpx; }
.date-item.active { background: linear-gradient(135deg, #1976d2 0%, #2196f3 100%); color: #fff; }
.date-week { font-size: 24rpx; }
.date-day { font-size: 32rpx; font-weight: bold; }
.period { padding: 10rpx 30rpx 30rpx; }
.period-header { display: flex; align-items: center; gap: 10rpx; margin: 10rpx 0 16rpx; }
.period-icon { font-size: 32rpx; }
.period-name { font-size: 28rpx; color: #333; font-weight: 600; }
.slot-list { display: grid; grid-template-columns: repeat(1, 1fr); gap: 16rpx; }
.slot-card { background: #fff; border-radius: 12rpx; padding: 16rpx; border: 1rpx solid #eef2f7; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03); }
.slot-card.active { border-color: #1976d2; box-shadow: 0 2rpx 10rpx rgba(25,118,210,0.15); }
.slot-card.expired { background: #f7f7f7; color: #999; border-color: #eee; }
.slot-card .slot-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
.slot-time { font-size: 28rpx; color: #333; font-weight: 600; }
.slot-remain { font-size: 24rpx; color: #1976d2; }
.slot-remain.no-slots { color: #f44336; }
.slot-remain.expired { color: #999; }
.slot-bottom { display: flex; align-items: center; justify-content: space-between; }
.slot-doctor { font-size: 26rpx; color: #333; }
.slot-meta { display: flex; align-items: center; gap: 10rpx; }
.slot-type { font-size: 22rpx; color: #1976d2; background: #e3f2fd; padding: 4rpx 10rpx; border-radius: 8rpx; }
.slot-price { font-size: 26rpx; color: #ff6f00; font-weight: 600; }
.bottom-bar { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; padding: 16rpx 30rpx; box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.06); }
.primary-btn { width: 100%; height: 88rpx; background: linear-gradient(135deg, #1976d2 0%, #2196f3 100%); color: #fff; border-radius: 44rpx; font-size: 30rpx; font-weight: 600; }
.waitlist-bottom-btn { width: 100%; height: 88rpx; background: #fff3e0; color: #fb8c00; border-radius: 44rpx; font-size: 30rpx; font-weight: 600; border: 2rpx solid #ffe0b2; }
.loading-mask { position: fixed; left: 0; right: 0; top: 0; bottom: 0; background: rgba(255,255,255,0.6); display: flex; align-items: center; justify-content: center; }
.loading-text { font-size: 28rpx; color: #999; }
</style>

