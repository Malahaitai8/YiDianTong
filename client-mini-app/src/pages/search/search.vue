<template>
	<view class="search-page">
		<!-- 搜索栏 -->
		<view class="search-bar">
			<view class="search-input-box">
				<text class="search-icon">🔍</text>
				<input 
					class="search-input" 
					v-model="keyword" 
					placeholder="搜索科室 / 医生" 
					placeholder-class="placeholder"
					confirm-type="search"
					@confirm="handleSearch"
					focus
				/>
				<text class="clear-icon" v-if="keyword" @click="clearKeyword">✕</text>
			</view>
			<text class="cancel-btn" @click="goBack">取消</text>
		</view>

		<!-- 搜索类型切换 -->
		<view class="search-tabs">
			<view 
				class="tab-item" 
				:class="{ active: searchType === 'department' }" 
				@click="switchSearchType('department')"
			>
				<text class="tab-text">科室</text>
			</view>
			<view 
				class="tab-item" 
				:class="{ active: searchType === 'doctor' }" 
				@click="switchSearchType('doctor')"
			>
				<text class="tab-text">医生</text>
			</view>
		</view>

		<!-- 搜索结果 -->
		<view class="search-results" v-if="keyword && searchResults.length > 0">
			<!-- 科室搜索结果 -->
			<view v-if="searchType === 'department'">
				<view 
					class="department-item" 
					v-for="item in searchResults" 
					:key="item.id" 
					@click="goToDepartmentDetail(item)"
				>
					<view class="department-icon">🏥</view>
					<view class="department-info">
						<text class="department-name">{{ item.name }}</text>
						<text class="department-desc" v-if="item.description">{{ item.description }}</text>
					</view>
					<text class="arrow-icon">›</text>
				</view>
			</view>

			<!-- 医生搜索结果 -->
			<view v-if="searchType === 'doctor'">
				<view 
					class="doctor-item" 
					v-for="item in searchResults" 
					:key="item.id" 
					@click="goToDoctorDetail(item)"
				>
					<view class="doctor-avatar">
						<text class="avatar-text">{{ item.name.substring(0, 1) }}</text>
					</view>
					<view class="doctor-info">
						<view class="doctor-name-row">
							<text class="doctor-name">{{ item.name }}</text>
							<text class="doctor-title">{{ item.title }}</text>
						</view>
						<text class="doctor-department" v-if="item.clinic">{{ item.clinic.name }}</text>
						<text class="doctor-specialty" v-if="item.specialty">擅长: {{ item.specialty }}</text>
					</view>
					<text class="arrow-icon">›</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="keyword && searchResults.length === 0 && !loading">
			<text class="empty-icon">🔍</text>
			<text class="empty-text">未找到相关{{ searchType === 'department' ? '科室' : '医生' }}</text>
		</view>

		<!-- 搜索历史 -->
		<view class="search-history" v-if="!keyword && searchHistory.length > 0">
			<view class="history-header">
				<text class="history-title">搜索历史</text>
				<text class="clear-history" @click="clearHistory">清空</text>
			</view>
			<view class="history-tags">
				<view 
					class="history-tag" 
					v-for="(item, index) in searchHistory" 
					:key="index" 
					@click="selectHistory(item)"
				>
					<text class="tag-text">{{ item }}</text>
				</view>
			</view>
		</view>

		<!-- 热门科室 -->
		<view class="hot-departments" v-if="!keyword && searchType === 'department'">
			<view class="section-title">热门科室</view>
			<view class="hot-list">
				<view 
					class="hot-item" 
					v-for="item in hotDepartments" 
					:key="item.id" 
					@click="goToDepartmentDetail(item)"
				>
					<text class="hot-icon">🔥</text>
					<text class="hot-name">{{ item.name }}</text>
				</view>
			</view>
		</view>

		<!-- 热门医生 -->
		<view class="hot-doctors" v-if="!keyword && searchType === 'doctor'">
			<view class="section-title">热门医生</view>
			<view class="hot-list">
				<view 
					class="hot-item" 
					v-for="item in hotDoctors" 
					:key="item.id" 
					@click="goToDoctorDetail(item)"
				>
					<text class="hot-icon">⭐</text>
					<text class="hot-name">{{ item.name }} - {{ item.title }}</text>
				</view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view class="loading" v-if="loading">
			<text class="loading-text">搜索中...</text>
		</view>
	</view>
</template>

<script>
import { searchDepartments, getDepartmentList } from '@/api/department.js';
import { searchDoctors, getDoctorList } from '@/api/doctor.js';

export default {
	data() {
		return {
			keyword: '',
			searchType: 'department', // 'department' or 'doctor'
			searchResults: [],
			searchHistory: [],
			loading: false,
			hotDepartments: [], // 从后端获取
			hotDoctors: [] // 从后端获取
		};
	},
	onLoad() {
		this.loadSearchHistory();
		this.loadHotData();
	},
	watch: {
		keyword(newVal) {
			if (newVal) {
				// 防抖搜索
				clearTimeout(this.searchTimer);
				this.searchTimer = setTimeout(() => {
					this.handleSearch();
				}, 500);
			} else {
				this.searchResults = [];
			}
		}
	},
	methods: {
		// 加载热门数据
		async loadHotData() {
			try {
				// 加载热门科室（取前4个）
				const departments = await getDepartmentList();
				this.hotDepartments = departments.slice(0, 4);
				
				// 加载热门医生（取主任医师前3个）
				const doctors = await getDoctorList();
				this.hotDoctors = doctors
					.filter(d => d.title === '主任医师')
					.slice(0, 3);
			} catch (error) {
				console.error('加载热门数据失败:', error);
			}
		},
		
		// 切换搜索类型
		switchSearchType(type) {
			this.searchType = type;
			this.searchResults = [];
			if (this.keyword) {
				this.handleSearch();
			}
		},
		
		// 执行搜索
		async handleSearch() {
			if (!this.keyword.trim()) {
				return;
			}

			this.loading = true;
			try {
				if (this.searchType === 'department') {
					const data = await searchDepartments(this.keyword);
					this.searchResults = data;
				} else {
					const data = await searchDoctors(this.keyword);
					this.searchResults = data;
				}
				
				// 保存搜索历史
				this.saveSearchHistory(this.keyword);
			} catch (error) {
				console.error('搜索失败:', error);
				uni.showToast({
					title: error.msg || '搜索失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		},
		
		// 清空搜索词
		clearKeyword() {
			this.keyword = '';
			this.searchResults = [];
		},
		
		// 保存搜索历史
		saveSearchHistory(keyword) {
			// 去重并添加到历史记录
			const history = this.searchHistory.filter(item => item !== keyword);
			history.unshift(keyword);
			// 最多保存10条
			this.searchHistory = history.slice(0, 10);
			// 存储到本地
			uni.setStorageSync('searchHistory', this.searchHistory);
		},
		
		// 加载搜索历史
		loadSearchHistory() {
			try {
				const history = uni.getStorageSync('searchHistory');
				if (history) {
					this.searchHistory = history;
				}
			} catch (e) {
				console.error('加载搜索历史失败:', e);
			}
		},
		
		// 清空搜索历史
		clearHistory() {
			uni.showModal({
				title: '提示',
				content: '确定要清空搜索历史吗？',
				success: (res) => {
					if (res.confirm) {
						this.searchHistory = [];
						uni.removeStorageSync('searchHistory');
					}
				}
			});
		},
		
		// 选择历史记录
		selectHistory(keyword) {
			this.keyword = keyword;
			this.handleSearch();
		},
		
		// 跳转到科室详情（实际是医生列表）
		goToDepartmentDetail(department) {
			uni.navigateTo({
				url: `/pages/doctor-list/doctor-list?departmentId=${department.id}&departmentName=${department.name}`
			});
		},
		
		// 跳转到医生详情
		goToDoctorDetail(doctor) {
			uni.navigateTo({
				url: `/pages/doctor-detail/doctor-detail?doctorId=${doctor.id}`
			});
		},
		
		// 返回
		goBack() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped>
.search-page {
	min-height: 100vh;
	background: #f5f7fa;
}

/* 搜索栏 */
.search-bar {
	background: #fff;
	padding: 20rpx 30rpx;
	display: flex;
	align-items: center;
	gap: 20rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.search-input-box {
	flex: 1;
	height: 70rpx;
	background: #f5f7fa;
	border-radius: 35rpx;
	display: flex;
	align-items: center;
	padding: 0 25rpx;
	gap: 15rpx;
}
.search-icon {
	font-size: 32rpx;
	color: #999;
}
.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}
.placeholder {
	color: #999;
}
.clear-icon {
	width: 36rpx;
	height: 36rpx;
	background: #ccc;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 20rpx;
	color: #fff;
}
.cancel-btn {
	font-size: 28rpx;
	color: #1976d2;
}

/* 搜索类型切换 */
.search-tabs {
	background: #fff;
	display: flex;
	border-bottom: 1rpx solid #f0f0f0;
}
.tab-item {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
}
.tab-text {
	font-size: 28rpx;
	color: #666;
	transition: all 0.3s;
}
.tab-item.active .tab-text {
	color: #1976d2;
	font-weight: 600;
}
.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 4rpx;
	background: #1976d2;
	border-radius: 2rpx;
}

/* 搜索结果 */
.search-results {
	padding: 20rpx 30rpx;
}

/* 科室项 */
.department-item {
	background: #fff;
	border-radius: 15rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	display: flex;
	align-items: center;
	gap: 20rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.department-icon {
	width: 80rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 40rpx;
	flex-shrink: 0;
}
.department-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}
.department-name {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
}
.department-desc {
	font-size: 24rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.arrow-icon {
	font-size: 40rpx;
	color: #ccc;
	font-weight: 300;
}

/* 医生项 */
.doctor-item {
	background: #fff;
	border-radius: 15rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	display: flex;
	align-items: center;
	gap: 20rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.doctor-avatar {
	width: 100rpx;
	height: 100rpx;
	background: linear-gradient(135deg, #42a5f5 0%, #1e88e5 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.avatar-text {
	font-size: 40rpx;
	color: #fff;
	font-weight: bold;
}
.doctor-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
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
.doctor-department {
	font-size: 24rpx;
	color: #666;
}
.doctor-specialty {
	font-size: 24rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
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

/* 搜索历史 */
.search-history {
	padding: 30rpx;
}
.history-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}
.history-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}
.clear-history {
	font-size: 24rpx;
	color: #999;
}
.history-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 15rpx;
}
.history-tag {
	background: #fff;
	padding: 12rpx 24rpx;
	border-radius: 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.tag-text {
	font-size: 26rpx;
	color: #666;
}

/* 热门推荐 */
.hot-departments,
.hot-doctors {
	padding: 30rpx;
}
.section-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 20rpx;
}
.hot-list {
	background: #fff;
	border-radius: 15rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.hot-item {
	height: 88rpx;
	padding: 0 30rpx;
	display: flex;
	align-items: center;
	gap: 15rpx;
	border-bottom: 1rpx solid #f5f5f5;
}
.hot-item:last-child {
	border-bottom: none;
}
.hot-icon {
	font-size: 32rpx;
}
.hot-name {
	flex: 1;
	font-size: 28rpx;
	color: #333;
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
</style>

