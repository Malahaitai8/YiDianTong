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
				<view class="record-card" v-for="item in filteredList" :key="item.id || item.scheduleId" @click="viewDetail(item)">
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
						<text class="value">{{ item.appointmentTime || item.scheduleDate || '未知时间' }} {{ item.timeSlotDisplay || '' }}</text>
					</view>
					<view class="row">
						<text class="label">状态</text>
						<text class="value status" :class="String(item.status||'').toLowerCase()">{{ statusName(item.status) }}</text>
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
						<button v-if="item.status==='PENDING' || item.status==='CONFIRMED'" class="btn cancel" @click.stop="cancel(item)">退号</button>
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
import { getMyAppointments, cancelAppointment, deleteAppointment } from '@/api/appointment.js'
import { getMyWaitlist, cancelWaitlist } from '@/api/waitlist.js'

export default {
	data() {
		return {
			appointments: [],
			waitlists: [],
			currentFilter: 'all' // 默认显示全部记录
		};
	},
	computed: {
		// 合并所有记录
		allRecords() {
			// 处理预约记录
			const appointmentRecords = this.appointments.map(item => ({
				...item,
				type: 'appointment'
			}));
			
			// 处理候补记录
			const waitlistRecords = this.waitlists.map(item => ({
				scheduleId: item.scheduleId,
				rank: item.rank,
				queueSize: item.queueSize,
				type: 'waitlist',
				status: 'WAITLIST', // 候补状态
				id: `waitlist_${item.scheduleId}`, // 生成唯一ID
				// 添加一些默认值，以便在界面中显示
				doctorName: '未知医生',
				scheduleDate: '未知日期',
				timeSlotDisplay: this.getTimeSlotDisplay(item.timeSlot)
			}));
			
			// 合并所有记录
			return [...appointmentRecords, ...waitlistRecords];
		},
		// 根据筛选条件过滤列表
		filteredList() {
			if (this.currentFilter === 'all') {
				return this.allRecords;
			} else if (this.currentFilter === 'appointment') {
				// 只显示预约记录（待就诊和已确认）
				return this.allRecords.filter(item => 
					item.type === 'appointment' && (item.status === 'PENDING' || item.status === 'CONFIRMED')
				);
			} else if (this.currentFilter === 'visit') {
				// 只显示就诊记录（已完成）
				return this.allRecords.filter(item => 
					item.type === 'appointment' && item.status === 'COMPLETED'
				);
			} else if (this.currentFilter === 'waitlist') {
				// 只显示候补记录
				return this.allRecords.filter(item => item.type === 'waitlist');
			}
			return this.allRecords;
		}
	},
	onLoad() {
		// 从全局状态获取筛选条件
		const filter = getApp().globalData.recordFilter;
		if (filter) {
			this.currentFilter = filter;
			// 清除全局状态
			getApp().globalData.recordFilter = null;
		}
	},
	onShow() {
		this.loadData();
	},
	methods: {
		async loadData() {
			try {
				// 获取预约记录
				const appointmentData = await getMyAppointments();
				this.appointments = Array.isArray(appointmentData) ? appointmentData : ((appointmentData && appointmentData.list) ? appointmentData.list : []);
				
				// 获取候补记录
				const waitlistData = await getMyWaitlist();
				this.waitlists = Array.isArray(waitlistData) ? waitlistData : ((waitlistData && waitlistData.list) ? waitlistData.list : []);
			} catch (e) {
				console.error('加载数据失败:', e);
				uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
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
		statusName(s) {
			switch (s) {
				case 'PENDING': return '待就诊';
				case 'CONFIRMED': return '已确认';
				case 'COMPLETED': return '已完成';
				case 'CANCELLED': return '已取消';
				case 'WAITLIST': return '候补中';
				default: return s || '-';
			}
		},
		async cancel(item) {
			try {
				await cancelAppointment(item.id);
				uni.showToast({ title: '已取消', icon: 'success' });
				this.loadData();
			} catch (e) {
				uni.showToast({ title: e.msg || '取消失败', icon: 'none' });
			}
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