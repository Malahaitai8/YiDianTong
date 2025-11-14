<template>
	<view class="records-page">
		<view class="page-header">
			<text class="header-title">就诊记录</text>
			<text class="header-desc">查看您的就诊历史</text>
		</view>

		<view class="content">
			<view v-if="!list.length" class="empty-state">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无就诊记录</text>
				<text class="empty-desc">您还没有就诊记录</text>
			</view>

			<view v-else>
				<view class="record-card" v-for="item in list" :key="item.id">
					<view class="row">
						<text class="label">就诊医生</text>
						<text class="value">{{ item.doctorName || ('#' + item.doctorId) }}</text>
					</view>
					<view class="row">
						<text class="label">就诊时间</text>
						<text class="value">{{ item.appointmentTime }}</text>
					</view>
					<view class="row">
						<text class="label">状态</text>
						<text class="value status" :class="String(item.status||'').toLowerCase()">{{ statusName(item.status) }}</text>
					</view>
					<view class="row">
						<text class="label">费用</text>
						<text class="value price">¥{{ item.actualFee != null ? item.actualFee : (item.fee != null ? item.fee : 0) }}</text>
					</view>
					<view class="actions">
						<button v-if="item.status==='PENDING' || item.status==='CONFIRMED'" class="btn cancel" @click="cancel(item)">取消预约</button>
						<button class="btn delete" @click="remove(item)">删除</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getMyAppointments, cancelAppointment, deleteAppointment } from '@/api/appointment.js'

export default {
	data() {
		return {
			list: []
		};
	},
	onShow() {
		this.loadData();
	},
	methods: {
		async loadData() {
			try {
				const data = await getMyAppointments();
				this.list = Array.isArray(data) ? data : ((data && data.list) ? data.list : []);
			} catch (e) {
				uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
			}
		},
		statusName(s) {
			switch (s) {
				case 'PENDING': return '待就诊';
				case 'CONFIRMED': return '已确认';
				case 'COMPLETED': return '已完成';
				case 'CANCELLED': return '已取消';
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
.content {
	padding: 60rpx 30rpx;
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
.cancel{ background:#fff3e0; color:#f57c00; }
.delete{ background:#ffebee; color:#d32f2f; }
</style>
