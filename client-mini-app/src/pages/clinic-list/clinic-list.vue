<template>
	<view class="clinic-list-page">
		<!-- 科室信息头部 -->
		<view class="dept-header">
			<view class="dept-icon">🏥</view>
			<view class="dept-info">
				<text class="dept-name">{{ departmentName }}</text>
				<text class="dept-sub">请选择门诊</text>
			</view>
		</view>

		<!-- 门诊列表 -->
		<view class="clinic-list">
			<view 
				class="clinic-card" 
				v-for="clinic in clinics" 
				:key="clinic.id" 
				@click="goToClinicAppointment(clinic)"
			>
				<view class="card-header">
					<view class="clinic-icon-box">
						<text class="clinic-icon">💊</text>
					</view>
					<view class="clinic-main">
						<text class="clinic-name">{{ clinic.name }}</text>
						<text class="clinic-desc" v-if="clinic.description">{{ clinic.description }}</text>
					</view>
					<text class="arrow-icon">›</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="!loading && clinics.length === 0">
			<text class="empty-icon">📋</text>
			<text class="empty-text">该科室暂无门诊或尚未配置</text>
		</view>

		<!-- 加载状态 -->
		<view class="loading-state" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getClinicsByDepartmentId } from '@/api/clinic.js';

export default {
	data() {
		return {
			departmentId: null,
			departmentName: '',
			clinics: [],
			loading: false
		};
	},
	onLoad(options) {
		if (options.departmentId) {
			this.departmentId = options.departmentId;
		}
		if (options.departmentName) {
			this.departmentName = decodeURIComponent(options.departmentName);
		}
		
		// 设置导航栏标题
		uni.setNavigationBarTitle({
			title: this.departmentName || '选择门诊'
		});
		
		this.loadClinics();
	},
	methods: {
		async loadClinics() {
			if (!this.departmentId) {
				uni.showToast({
					title: '科室信息缺失',
					icon: 'none'
				});
				return;
			}
			
			this.loading = true;
			try {
				const data = await getClinicsByDepartmentId(this.departmentId);
				this.clinics = Array.isArray(data) ? data : [];
			} catch (error) {
				console.error('加载门诊列表失败:', error);
				uni.showToast({
					title: error.msg || '加载失败',
					icon: 'none'
				});
				this.clinics = [];
			} finally {
				this.loading = false;
			}
		},
		
		// 跳转到门诊预约页面
		goToClinicAppointment(clinic) {
			uni.navigateTo({
				url: `/pages/clinic-appointment/clinic-appointment?clinicId=${clinic.id}&clinicName=${encodeURIComponent(clinic.name)}&departmentName=${encodeURIComponent(this.departmentName)}`
			});
		}
	}
};
</script>

<style scoped>
.clinic-list-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 30rpx;
}

/* 科室信息头部 */
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
	flex: 1;
}

.dept-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.dept-sub {
	font-size: 24rpx;
	color: #999;
}

/* 门诊列表 */
.clinic-list {
	padding: 0 30rpx;
}

.clinic-card {
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
	overflow: hidden;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.card-header {
	display: flex;
	align-items: center;
	padding: 30rpx;
	gap: 20rpx;
}

.clinic-icon-box {
	width: 80rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.clinic-icon {
	font-size: 40rpx;
}

.clinic-main {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.clinic-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.clinic-desc {
	font-size: 26rpx;
	color: #666;
	line-height: 1.5;
}

.arrow-icon {
	font-size: 48rpx;
	color: #ccc;
	font-weight: 300;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.3;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 加载状态 */
.loading-state {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}
</style>

