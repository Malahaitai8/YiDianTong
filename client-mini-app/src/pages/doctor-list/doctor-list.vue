<template>
	<view class="doctor-list-page">
		<!-- 筛选栏 -->
		<view class="filter-bar" v-if="doctors.length > 0">
			<view class="filter-left">
				<text class="filter-count">共 {{ filteredDoctors.length }} 位医生</text>
			</view>
			<view class="filter-item" @click="showTitleFilter">
				<text class="filter-text">职称</text>
				<text class="filter-value" v-if="selectedTitle">{{ selectedTitle }}</text>
				<text class="filter-arrow">▼</text>
			</view>
			<view class="filter-item" @click="showSortFilter">
				<text class="filter-text">排序</text>
				<text class="filter-value" v-if="sortType !== 'default'">{{ getSortName(sortType) }}</text>
				<text class="filter-arrow">▼</text>
			</view>
		</view>

		<!-- 医生列表 -->
		<view class="doctor-list">
			<view 
				class="doctor-card" 
				v-for="doctor in filteredDoctors" 
				:key="doctor.id" 
				@click="goToDoctorDetail(doctor)"
			>
				<view class="card-left">
					<view class="doctor-avatar">
						<text class="avatar-text">{{ doctor.name.substring(0, 1) }}</text>
					</view>
				</view>
				<view class="card-middle">
					<view class="doctor-name-row">
						<text class="doctor-name">{{ doctor.name }}</text>
						<text
							class="doctor-title"
							:class="doctor.title === '主任医师' ? 'senior' : (doctor.title === '副主任医师' ? 'associate' : '')"
						>
							{{ doctor.title }}
						</text>
					</view>
					<text class="doctor-department" v-if="doctor.clinic">{{ doctor.clinic.name }}</text>
					<view class="doctor-specialty" v-if="doctor.specialty">
						<text class="specialty-label">擅长：</text>
						<text class="specialty-text">{{ doctor.specialty }}</text>
					</view>
					<view class="doctor-footer">
						<view class="fee-info">
							<text class="fee-label">挂号费</text>
							<text class="fee-value">¥{{ getRegistrationFee(doctor) }}</text>
						</view>
					</view>
				</view>
				<view class="card-right">
					<text 
						class="duty-tag" 
						:class="{ active: doctor.onDutyToday }"
					>
						{{ doctor.onDutyToday ? '今日坐诊' : '今日休诊' }}
					</text>
					<text class="arrow-icon">›</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="filteredDoctors.length === 0 && !loading">
			<text class="empty-icon">👨‍⚕️</text>
			<text class="empty-text">暂无医生信息</text>
		</view>

		<!-- 加载状态 -->
		<view class="loading" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 职称筛选弹窗 -->
		<view class="filter-popup" v-if="showTitlePopup" @click="showTitlePopup = false">
			<view class="popup-content" @click.stop>
				<view class="popup-title">选择职称</view>
				<view class="popup-options">
					<view 
						class="popup-option" 
						:class="{ active: selectedTitle === '' }" 
						@click="selectTitle('')"
					>
						<text class="option-text">全部</text>
					</view>
					<view 
						class="popup-option" 
						:class="{ active: selectedTitle === item }" 
						v-for="item in titleOptions" 
						:key="item" 
						@click="selectTitle(item)"
					>
						<text class="option-text">{{ item }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 排序筛选弹窗 -->
		<view class="filter-popup" v-if="showSortPopup" @click="showSortPopup = false">
			<view class="popup-content" @click.stop>
				<view class="popup-title">选择排序</view>
				<view class="popup-options">
					<view 
						class="popup-option" 
						:class="{ active: sortType === item.key }" 
						v-for="item in sortOptions" 
						:key="item.key" 
						@click="selectSort(item.key)"
					>
						<text class="option-text">{{ item.name }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getDoctorsByDepartment } from '@/api/department.js';
import { getDoctorList } from '@/api/doctor.js';

export default {
	data() {
		return {
			departmentId: null,
			departmentName: '',
			doctors: [],
			selectedTitle: '',
			sortType: 'default',
			showTitlePopup: false,
			showSortPopup: false,
			titleOptions: ['主任医师', '副主任医师', '主治医师', '住院医师'],
			sortOptions: [
				{ key: 'default', name: '默认排序' },
				{ key: 'title', name: '职称优先' },
				{ key: 'fee', name: '费用从低到高' }
			],
			loading: false
		};
	},
	computed: {
		filteredDoctors() {
			let result = [...this.doctors];
			
			// 职称筛选
			if (this.selectedTitle) {
				result = result.filter(doctor => doctor.title === this.selectedTitle);
			}
			
			// 排序：优先展示今日坐诊的医生
			const titleOrder = ['主任医师', '副主任医师', '主治医师', '住院医师'];
			result.sort((a, b) => {
				const dutyDiff = (b.onDutyToday ? 1 : 0) - (a.onDutyToday ? 1 : 0);
				if (dutyDiff !== 0) {
					return dutyDiff;
				}
				if (this.sortType === 'title') {
					return titleOrder.indexOf(a.title) - titleOrder.indexOf(b.title);
				}
				if (this.sortType === 'fee') {
					return (a.registrationFee || 0) - (b.registrationFee || 0);
				}
				return 0;
			});
			
			return result;
		}
	},
	onLoad(options) {
		if (options.departmentId !== undefined) {
			const parsedId = Number(options.departmentId);
			this.departmentId = Number.isNaN(parsedId) ? null : parsedId;
		}
		if (options.departmentName) {
			this.departmentName = decodeURIComponent(options.departmentName);
		}
		this.loadDoctors();
	},
	onPullDownRefresh() {
		this.handlePullDownRefresh();
	},
	methods: {
		async handlePullDownRefresh() {
			try {
				await this.loadDoctors();
			} finally {
				uni.stopPullDownRefresh();
			}
		},
		// 加载医生列表
		async loadDoctors() {
			this.loading = true;
			try {
				if (this.departmentId !== null && this.departmentId !== undefined) {
					let list = await getDoctorsByDepartment(this.departmentId).catch(() => null);
					if (!Array.isArray(list) || list.length === 0) {
						const allDoctors = await getDoctorList();
						list = this.filterDoctorsByDepartment(allDoctors, this.departmentId);
					}
					this.doctors = this.normalizeDoctorList(list);
				} else {
					const data = await getDoctorList();
					this.doctors = this.normalizeDoctorList(data);
				}
			} catch (error) {
				console.error('加载医生列表失败:', error);
				this.doctors = [];
				uni.showToast({
					title: error.msg || '加载失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		},
		
		// 显示职称筛选
		showTitleFilter() {
			this.showTitlePopup = true;
		},
		
		// 显示排序筛选
		showSortFilter() {
			this.showSortPopup = true;
		},
		
		// 选择职称
		selectTitle(title) {
			this.selectedTitle = title;
			this.showTitlePopup = false;
		},
		
		// 选择排序
		selectSort(type) {
			this.sortType = type;
			this.showSortPopup = false;
		},

		normalizeDoctorList(list) {
			if (!Array.isArray(list)) {
				return [];
			}
			return list
				.filter(item => item && item.id)
				.map(item => ({
					...item,
					onDutyToday: !!item.onDutyToday
				}));
		},

		filterDoctorsByDepartment(list, departmentId) {
			if (!Array.isArray(list)) {
				return [];
			}
			return list.filter(doctor => {
				const clinic = doctor?.clinic;
				if (!clinic) return false;
				const deptId = Number(clinic.departmentId);
				return !Number.isNaN(deptId) && deptId === Number(departmentId);
			});
		},
		
		// 获取排序名称
		getSortName(key) {
			const option = this.sortOptions.find(item => item.key === key);
			return option ? option.name : '';
		},
		
		// 获取职称样式类
		getTitleClass(title) {
			if (title === '主任医师') return 'senior';
			if (title === '副主任医师') return 'associate';
			return '';
		},
		
		// 获取挂号费（暂时返回默认值）
		getRegistrationFee(doctor) {
			// 根据职称返回不同挂号费
			if (doctor.title === '主任医师') return 50;
			if (doctor.title === '副主任医师') return 30;
			if (doctor.title === '主治医师') return 20;
			return 15;
		},
		
		// 跳转到医生详情
		goToDoctorDetail(doctor) {
			uni.navigateTo({
				url: `/pages/doctor-detail/doctor-detail?doctorId=${doctor.id}`
			});
		}
	}
};
</script>

<style scoped>
.doctor-list-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 30rpx;
}

/* 筛选栏 */
.filter-bar {
	background: #fff;
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.filter-left {
	flex: 1;
}
.filter-count {
	font-size: 26rpx;
	color: #666;
}
.filter-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
}
.filter-text {
	font-size: 26rpx;
	color: #333;
}
.filter-value {
	font-size: 26rpx;
	color: #1976d2;
	font-weight: 600;
}
.filter-arrow {
	font-size: 20rpx;
	color: #999;
}

/* 医生列表 */
.doctor-list {
	padding: 20rpx 30rpx;
}
.doctor-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	display: flex;
	gap: 20rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.card-left {
	flex-shrink: 0;
}
.doctor-avatar {
	width: 100rpx;
	height: 100rpx;
	background: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}
.avatar-text {
	font-size: 40rpx;
	color: #fff;
	font-weight: bold;
}
.card-middle {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}
.doctor-name-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
}
.doctor-name {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
}
.doctor-title {
	font-size: 22rpx;
	color: #1976d2;
	background: #e3f2fd;
	padding: 4rpx 12rpx;
	border-radius: 10rpx;
}
.doctor-title.senior {
	color: #d32f2f;
	background: #ffebee;
}
.doctor-title.associate {
	color: #f57c00;
	background: #fff3e0;
}
.doctor-department {
	font-size: 24rpx;
	color: #666;
}
.doctor-specialty {
	display: flex;
	gap: 8rpx;
}
.specialty-label {
	font-size: 24rpx;
	color: #999;
	flex-shrink: 0;
}
.specialty-text {
	flex: 1;
	font-size: 24rpx;
	color: #666;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.doctor-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 5rpx;
}
.fee-info {
	display: flex;
	align-items: baseline;
	gap: 8rpx;
}
.fee-label {
	font-size: 22rpx;
	color: #999;
}
.fee-value {
	font-size: 28rpx;
	color: #ff9800;
	font-weight: 600;
}
.available-info {
	
}
.available-text {
	font-size: 22rpx;
	color: #4caf50;
}
.available-text.low {
	color: #ff9800;
}
.card-right {
	display: flex;
	align-items: center;
	gap: 12rpx;
}
.duty-tag {
	font-size: 22rpx;
	color: #999;
	background: #f0f0f0;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
}
.duty-tag.active {
	color: #4caf50;
	background: #e8f5e9;
	font-weight: 600;
}
.arrow-icon {
	font-size: 40rpx;
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
	gap: 20rpx;
}
.empty-icon {
	font-size: 100rpx;
	opacity: 0.3;
}
.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 加载状态 */
.loading {
	display: flex;
	justify-content: center;
	padding: 40rpx 0;
}
.loading-text {
	font-size: 26rpx;
	color: #999;
}

/* 筛选弹窗 */
.filter-popup {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	align-items: flex-end;
}
.popup-content {
	width: 100%;
	background: #fff;
	border-radius: 30rpx 30rpx 0 0;
	padding: 40rpx 30rpx;
	animation: slideUp 0.3s ease;
}
@keyframes slideUp {
	from {
		transform: translateY(100%);
	}
	to {
		transform: translateY(0);
	}
}
.popup-title {
	font-size: 32rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 30rpx;
	text-align: center;
}
.popup-options {
	max-height: 600rpx;
	overflow-y: auto;
}
.popup-option {
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-bottom: 1rpx solid #f0f0f0;
}
.popup-option:last-child {
	border-bottom: none;
}
.option-text {
	font-size: 28rpx;
	color: #333;
}
.popup-option.active .option-text {
	color: #1976d2;
	font-weight: 600;
}
</style>

