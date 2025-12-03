<template>
	<view class="dept-appointment-page">
		<!-- 科室信息条 -->
		<view class="dept-header">
			<view class="dept-icon">🏥</view>
			<view class="dept-info">
				<text class="dept-name">{{ departmentName }}</text>
				<text class="dept-sub">请选择预约日期，查看当日上/下午号源</text>
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
							'no-slots': s.availableSlots === 0
						}"
						@click="selectSlot(s)"
					>
						<view class="slot-top">
							<text class="slot-time">{{ s.startTime }}-{{ s.endTime }}</text>
							<text class="slot-remain" :class="{ 'no-slots': s.availableSlots === 0 }">余{{ s.availableSlots }}</text>
						</view>
						<view class="slot-bottom">
							<text class="slot-doctor">{{ s.doctorName || '医生' }}</text>
							<view class="slot-meta">
								<text class="slot-type">{{ getSlotType(s) }}</text>
								<text class="slot-price">¥{{ getSlotPrice(s) }}</text>
							</view>
						</view>
						<!-- 当号源为0时显示候补按钮 -->
						<view class="waitlist-overlay" v-if="s.availableSlots === 0" @click.stop="joinWaitlist(s)">
							<button class="waitlist-btn">候补排队</button>
						</view>
					</view>
				</view>
				<view class="empty-period" v-if="getSlotsByPeriod(p).length === 0 && !loading">
					<text class="empty-text">该时段暂无号源</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-all" v-if="!loading && periods.every(p => getSlotsByPeriod(p).length === 0)">
			<text class="empty-icon">📅</text>
			<text class="empty-tip">该日期暂无科室号源</text>
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
import { getDoctorsByDepartment } from '@/api/department.js';
import { getDoctorSchedules } from '@/api/doctor.js';
import { joinWaitlist, createWaitlistPrepayment, payWaitlistOrder } from '@/api/waitlist.js';

export default {
	data() {
		return {
			departmentId: null,
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
			// 仅展示上午/下午
			return ['morning', 'afternoon'];
		}
	},
	onLoad(query) {
		const id = Number(query.departmentId);
		this.departmentId = Number.isNaN(id) ? null : id;
		this.departmentName = decodeURIComponent(query.departmentName || '');
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
			if (!this.departmentId) return;
			this.loading = true;
			try {
				// 1) 拉取该科室所有医生
				const doctors = await getDoctorsByDepartment(this.departmentId).catch(() => []);
				this.doctors = Array.isArray(doctors) ? doctors : [];
				// 2) 拉取7天内每位医生的排班并汇总（按日期缓存到内存）
				const startDate = this.dateList[0].date;
				const endDate = this.dateList[this.dateList.length - 1].date;
				const all = [];
				for (const d of this.doctors) {
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
				// 缓存7天内全部号源，便于切换日期本地过滤
				this._allSchedules = all;
				this.updateSlotsForSelectedDate(all);
			} finally {
				this.loading = false;
			}
		},
		updateSlotsForSelectedDate(allSchedules) {
			this.selectedSlot = null;
			this.slotsOfSelectedDate = (allSchedules || []).filter(s => s.date === this.selectedDate);
		},
		onClickDate(date) {
			// 若该日期无任何号源，提示
			if (!this.slotsOfSelectedDate.some(s => s.date === date)) {
				// 切换并重新基于缓存判断
			}
			this.selectedDate = date;
			// 由于我们把7天所有号源已拉取存于内存里（通过 slotsOfSelectedDate 当前天视图），
			// 这里只需重新筛选。为简单起见，暂把 allSchedules 放入页面实例上。
			if (this._allSchedules && Array.isArray(this._allSchedules)) {
				this.updateSlotsForSelectedDate(this._allSchedules);
			}
		},
		getSlotsByPeriod(period) {
			return this.slotsOfSelectedDate
				.filter(s => s.period === period)
				.sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''));
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
			// 优先使用后端提供的 slotType，其次根据医生职称映射
			const t = s.slotType || this.mapDoctorTitleToSlotType(s.doctorTitle);
			return t || '普通号';
		},
		getSlotPrice(s) {
			// 1) 若后端直接给了价格字段，优先使用（与后端保持完全一致）
			const raw = s.price ?? s.fee ?? s.amount;
			if (raw != null && !Number.isNaN(Number(raw))) {
				return Number(raw).toFixed(2);
			}

			// 2) 根据号别类型与后端 SystemConfig 中的 FEE_NORMAL/FEE_EXPERT/FEE_VIP 对齐
			const slotType = (s.slotType || '').toString().trim().toUpperCase();
			if (slotType === 'VIP') {
				return '100.00'; // 对齐 FEE_VIP 默认值
			}
			if (slotType === 'EXPERT') {
				return '50.00'; // 对齐 FEE_EXPERT 默认值
			}

			// 3) 兜底：根据职称估算，但金额仍然收敛到「普通号 15 元」
			const fee = this.calcFeeByTitle(s.doctorTitle);
			return fee.toFixed(2);
		},
		mapDoctorTitleToSlotType(title) {
			if (!title) return '普通号';
			if (title.includes('主任')) return '专家号';
			if (title.includes('副主任')) return '副专家号';
			if (title.includes('主治')) return '普通号';
			return '普通号';
		},
		calcFeeByTitle(title) {
			// 与后端 SystemConfig 默认值保持一致：普通号 15，专家号 50
			if (!title) return 15;
			if (title.includes('主任')) return 50;
			// 其他职称都视为普通号
			return 15;
		},
		selectSlot(s) {
			// 即使号源为0也可以选择，用于候补
			this.selectedSlot = s;
		},
		// 加入候补队列
		async joinWaitlist(slot) {
			if (!slot) {
				uni.showToast({ title: '请选择号源', icon: 'none' });
				return;
			}
			
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
			
			const confirmQueue = await this.showConfirmModal(`确定要加入"${slot.doctorName}"医生${this.formatDate(slot.date)}${this.periodName(slot.period)}的候补队列吗？`);
			if (!confirmQueue) return;

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
		},
		// 格式化日期显示
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
			
			// 检查号源是否充足
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
.dept-appointment-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 140rpx;
}
.dept-header {
	background: #fff;
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 24rpx 30rpx;
	margin-bottom: 16rpx;
}
.dept-icon {
	font-size: 40rpx;
}
.dept-info {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}
.dept-name {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
}
.dept-sub {
	font-size: 24rpx;
	color: #999;
}
.section {
	background: #fff;
	padding: 24rpx 30rpx;
	margin-bottom: 16rpx;
}
.section-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 18rpx;
}
.date-scroll {
	width: 100%;
	white-space: nowrap;
}
.date-list {
	display: inline-flex;
	gap: 14rpx;
}
.date-item {
	width: 150rpx;
	padding: 36rpx 0;
	background: #f5f7fa;
	border-radius: 16rpx;
	border: 2rpx solid transparent;
	display: inline-flex;
	flex-direction: column;
	align-items: center;
	gap: 6rpx;
	flex-shrink: 0;
	transition: all 0.3s;
}
.date-item.active {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	box-shadow: 0 8rpx 20rpx rgba(25,118,210,0.25);
	transform: translateY(-2rpx);
	border-color: rgba(255,255,255,0.3);
}
.date-week {
	font-size: 24rpx;
	color: #666;
}
.date-item.active .date-week {
	color: rgba(255, 255, 255, 0.9);
}
.date-day {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}
.date-item.active .date-day {
	color: #fff;
}
.date-item.disabled {
	background: #fafafa;
	border-color: #f0f0f0;
}
.date-item.disabled .date-week,
.date-item.disabled .date-day {
	color: #bbb;
}

.period {
	margin-bottom: 14rpx;
}
.period-header {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin: 10rpx 0 16rpx;
}
.period-icon {
	font-size: 26rpx;
}
.period-name {
	font-size: 26rpx;
	color: #666;
	font-weight: 600;
}
.slot-list {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 14rpx;
}
.slot-card {
	background: #f5f7fa;
	border-radius: 14rpx;
	padding: 18rpx;
	border: 2rpx solid transparent;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
	transition: all 0.2s;
	position: relative;
}
.slot-card.active {
	background: #e3f2fd;
	border-color: #1976d2;
}
.slot-card.no-slots {
	background: #ffebee;
	border-color: #ffcdd2;
	opacity: 0.8;
}
.slot-top {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.slot-time {
	font-size: 26rpx;
	color: #333;
}
.slot-remain {
	font-size: 22rpx;
	color: #4caf50;
}
.slot-remain.no-slots {
	color: #f44336;
}
.slot-bottom {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.slot-doctor {
	font-size: 24rpx;
	color: #666;
}
.slot-meta {
	display: inline-flex;
	align-items: center;
	gap: 12rpx;
}
.slot-type {
	font-size: 22rpx;
	color: #1976d2;
	background: #e3f2fd;
	padding: 4rpx 10rpx;
	border-radius: 999rpx;
}
.slot-price {
	font-size: 24rpx;
	color: #ff5722;
	font-weight: 600;
}
.empty-period {
	text-align: center;
	padding: 20rpx 0;
}
.empty-text {
	font-size: 24rpx;
	color: #999;
}

/* 候补按钮覆盖层 */
.waitlist-overlay {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.6);
	border-radius: 14rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.waitlist-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	color: #fff;
	border: none;
	border-radius: 30rpx;
	padding: 10rpx 20rpx;
	font-size: 24rpx;
	font-weight: 600;
}

.empty-all {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10rpx;
	padding: 80rpx 0;
}
.empty-icon {
	font-size: 72rpx;
	opacity: 0.3;
}
.empty-tip {
	font-size: 26rpx;
	color: #999;
}

.bottom-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	padding: 16rpx 24rpx;
	box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}
.primary-btn, .waitlist-bottom-btn {
	height: 80rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 40rpx;
	font-size: 28rpx;
	font-weight: 600;
}
.waitlist-bottom-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}
.primary-btn::after, .waitlist-bottom-btn::after {
	border: none;
}
.primary-btn[disabled], .waitlist-bottom-btn[disabled] {
	background: #ccc;
}

.loading-mask {
	position: fixed;
	left: 0;
	right: 0;
	top: 0;
	bottom: 0;
	background: rgba(255,255,255,0.9);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 999;
}
.loading-text {
	font-size: 28rpx;
	color: #999;
}
</style>