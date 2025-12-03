<template>
	<view class="my-substitute-page">
		<view class="page-header">
			<text class="header-title">我的候补</text>
			<text class="header-desc">查看您的候补排队情况</text>
		</view>

		<!-- 筛选标签 -->
		<view class="filter-tabs">
			<view 
				class="tab-item" 
				:class="{ active: currentFilter === 'all' }" 
				@click="filterWaitlists('all')"
			>
				全部
			</view>
			<view 
				class="tab-item" 
				:class="{ active: currentFilter === 'waiting' }" 
				@click="filterWaitlists('waiting')"
			>
				候补中
			</view>
			<view 
				class="tab-item" 
				:class="{ active: currentFilter === 'success' }" 
				@click="filterWaitlists('success')"
			>
				已成功
			</view>
		</view>

		<view class="content">
			<view v-if="!filteredList.length" class="empty-state">
				<text class="empty-icon">⏰</text>
				<text class="empty-text">暂无候补记录</text>
				<text class="empty-desc">您还没有候补记录</text>
			</view>

			<view v-else>
				<view class="waitlist-card" v-for="item in filteredList" :key="item.scheduleId" @click="viewDetail(item)">
					<view class="row">
						<text class="label">候补医生</text>
						<text class="value">{{ item.doctorName || '未知医生' }}</text>
					</view>
					<view class="row">
						<text class="label">就诊时间</text>
						<text class="value">{{ item.scheduleDate || '未知日期' }} {{ item.timeSlotDisplay || '未知时间' }}</text>
					</view>
					<view class="row">
						<text class="label">排队位次</text>
						<text class="value">{{ (item.rank !== null ? (item.rank + 1) : '未知') }} / {{ item.queueSize || '未知' }}</text>
					</view>
					<!-- 候补可视化统计信息 -->
					<view v-if="item.status === 'WAITING'" class="stats-section">
						<view class="stat-item" v-if="item.successRate !== null && item.successRate !== undefined">
							<text class="stat-label">预计成功率</text>
							<text
								class="stat-value"
								:class="item.successRate >= 70 ? 'high' : (item.successRate >= 40 ? 'medium' : 'low')"
							>
								{{ formatSuccessRate(item.successRate) }}%
							</text>
						</view>
						<view class="stat-item" v-if="item.avgWaitTime !== null && item.avgWaitTime !== undefined">
							<text class="stat-label">平均等待时长</text>
							<text class="stat-value">{{ formatWaitTime(item.avgWaitTime) }}</text>
						</view>
					</view>
					<view class="row">
						<text class="label">状态</text>
						<text
							class="value status"
							:class="item.status === 'WAITING' ? 'waiting' : (item.status === 'SUCCESS' ? 'success' : (item.status === 'CANCELLED' ? 'cancelled' : ''))"
						>
							{{ getStatusText(item.status) }}
						</text>
					</view>
					<view class="actions" @click.stop>
						<button v-if="item.status === 'WAITING'" class="btn cancel" @click.stop="cancelWaitlist(item)">退出候补</button>
						<button v-else class="btn detail" @click.stop="viewDetail(item)">查看详情</button>
					</view>
				</view>
			</view>
		</view>

		<!-- 加载提示 -->
		<view class="loading-mask" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getMyWaitlist, cancelWaitlist } from '@/api/waitlist.js'
import { getScheduleDetailsById } from '@/api/schedule.js'

export default {
	name: 'MySubstitute',
	data() {
		return {
			waitlists: [],
			currentFilter: 'all', // 默认显示全部候补记录
			loading: false
		};
	},
	computed: {
		// 根据筛选条件过滤列表
		filteredList() {
			if (this.currentFilter === 'all') {
				return this.waitlists;
			} else if (this.currentFilter === 'waiting') {
				// 只显示候补中的记录
				return this.waitlists.filter(item => item.status === 'WAITING');
			} else if (this.currentFilter === 'success') {
				// 只显示已成功的记录
				return this.waitlists.filter(item => item.status === 'SUCCESS');
			}
			return this.waitlists;
		}
	},
	onLoad() {
		// 从全局状态获取筛选条件
		const filter = getApp().globalData.waitlistFilter;
		if (filter) {
			this.currentFilter = filter;
			// 清除全局状态
			getApp().globalData.waitlistFilter = null;
		}
	},
	onShow() {
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
		
		this.loadData();
	},
	onPullDownRefresh() {
		this.handlePullDownRefresh();
	},
	methods: {
		async handlePullDownRefresh() {
			try {
				await this.loadData();
			} finally {
				uni.stopPullDownRefresh();
			}
		},
		async loadData() {
			this.loading = true;
			try {
				// 获取候补记录
				const waitlistData = await getMyWaitlist();
				console.log('候补列表数据:', waitlistData);

				// 处理候补记录数据，并获取每个排班的详细信息
				if (Array.isArray(waitlistData) && waitlistData.length > 0) {
					// 并行获取所有排班的详细信息
					const detailsPromises = waitlistData.map(item =>
						getScheduleDetailsById(item.scheduleId)
							.then(details => ({
								...item,
								status: 'WAITING', // 候补状态
								doctorName: details.doctorName || '未知医生',
								scheduleDate: this.formatDate(details.scheduleDate) || '未知日期',
								timeSlot: details.timeSlot,
								timeSlotDisplay: this.getTimeSlotDisplay(details.timeSlot) || '未知时间',
								departmentName: details.departmentName || '未知科室'
							}))
							.catch(err => {
								console.error(`获取排班 ${item.scheduleId} 详情失败:`, err);
								// 如果获取失败，返回一个特殊状态，以便UI可以识别
								return {
									...item,
									status: 'ERROR',
									doctorName: '加载失败',
									scheduleDate: '请刷新重试',
									timeSlotDisplay: '',
									departmentName: ''
								};
							})
					);

					const resolvedWaitlists = await Promise.all(detailsPromises);

					// 检查是否有任何候补记录加载失败
					if (resolvedWaitlists.some(item => item.status === 'ERROR')) {
						uni.showToast({
							title: '部分候补详情加载失败，请稍后重试',
							icon: 'none'
						});
					}

					this.waitlists = resolvedWaitlists;
				} else {
					this.waitlists = [];
				}

				console.log('处理后的候补列表:', this.waitlists);
			} catch (e) {
				console.error('加载候补数据失败:', e);
				uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
			} finally {
				this.loading = false;
			}
		},
		// 筛选候补记录
		filterWaitlists(type) {
			this.currentFilter = type;
		},
		// 获取状态文本
		getStatusText(status) {
			switch (status) {
				case 'WAITING': return '候补中';
				case 'SUCCESS': return '已成功';
				case 'CANCELLED': return '已取消';
				default: return status || '-';
			}
		},
		// 获取状态样式类
		getStatusClass(status) {
			return String(status || '').toLowerCase();
		},
		// 退出候补
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
		// 查看详情
		viewDetail(item) {
			// 跳转到候补详情页
			uni.navigateTo({
				url: `/pages/waitlist/waitlist?scheduleId=${item.scheduleId}`
			});
		},
		// 格式化日期
		formatDate(dateStr) {
			if (!dateStr) return '';
			const date = new Date(dateStr);
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			return `${year}-${month}-${day}`;
		},
		// 获取时间段显示文本
		getTimeSlotDisplay(timeSlot) {
			if (!timeSlot) return '';
			const lowerCaseTimeSlot = timeSlot.toLowerCase();
			const timeSlotMap = {
				'morning': '上午',
				'afternoon': '下午',
				'evening': '晚上'
			};
			return timeSlotMap[lowerCaseTimeSlot] || timeSlot || '';
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
			if (rate >= 40) return 'medium';
			return 'low';
		}
	}
};
</script>

<style scoped>
.my-substitute-page {
	min-height: 100vh;
	background: #f5f7fa;
}
.page-header {
	background: linear-gradient(135deg, #9c27b0 0%, #e91e63 100%);
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
	color: #9c27b0;
	font-weight: bold;
	border-bottom: 4rpx solid #9c27b0;
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
.waitlist-card{
	background:#fff;
	border-radius:16rpx;
	padding:26rpx 26rpx 16rpx;
	margin-bottom:20rpx;
	cursor: pointer;
	transition: all 0.3s;
}
.waitlist-card:active{
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
.status{ font-weight:600; }
.status.waiting{ color:#ff9800; }
.status.success{ color:#4caf50; }
.status.cancelled{ color:#999; }
/* 候补统计信息样式 */
.stats-section{
	background:#f8f9fa;
	border-radius:8rpx;
	padding:16rpx;
	margin:12rpx 0;
	display:flex;
	flex-direction:column;
	gap:12rpx;
}
.stat-item{
	display:flex;
	justify-content:space-between;
	align-items:center;
}
.stat-label{
	color:#666;
	font-size:24rpx;
}
.stat-value{
	color:#333;
	font-size:26rpx;
	font-weight:600;
}
.stat-value.high{ color:#4caf50; }
.stat-value.medium{ color:#ff9800; }
.stat-value.low{ color:#f44336; }
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