<template>
	<view class="department-list-page">
		<!-- 分类导航 -->
		<view class="category-nav">
			<scroll-view scroll-x class="category-scroll">
				<view class="category-list">
					<view 
						class="category-item" 
						:class="{ active: selectedCategory === 'all' }" 
						@click="selectCategory('all')"
					>
						<text class="category-text">全部</text>
					</view>
					<view 
						class="category-item" 
						:class="{ active: selectedCategory === item.key }" 
						v-for="item in categories" 
						:key="item.key" 
						@click="selectCategory(item.key)"
					>
						<text class="category-text">{{ item.name }}</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 科室列表 -->
		<view class="department-list">
			<view 
				class="department-card" 
				v-for="item in filteredDepartments" 
				:key="item.id" 
				@click="goToDoctorList(item)"
			>
				<view class="card-header">
					<view class="department-icon-box">
						<text class="department-icon">{{ item.icon || '🏥' }}</text>
					</view>
					<view class="department-main">
						<text class="department-name">{{ item.name }}</text>
						<view class="department-tags">
							<text class="tag">{{ getDepartmentCategory(item.name) }}</text>
						</view>
					</view>
					<text class="arrow-icon">›</text>
				</view>
				<view class="card-body" v-if="item.description">
					<text class="department-desc">{{ item.description }}</text>
				</view>
				<view class="card-footer" v-if="item.description">
					<view class="info-item">
						<text class="info-icon">📋</text>
						<text class="info-text">点击查看该科室医生</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="filteredDepartments.length === 0 && !loading">
			<text class="empty-icon">🏥</text>
			<text class="empty-text">暂无科室信息</text>
		</view>

		<!-- 加载状态 -->
		<view class="loading" v-if="loading">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { getDepartmentList } from '@/api/department.js';

export default {
	data() {
		return {
			selectedCategory: 'all',
			categories: [
				{ key: 'internal', name: '内科' },
				{ key: 'surgery', name: '外科' },
				{ key: 'pediatrics', name: '儿科' },
				{ key: 'gynecology', name: '妇产科' },
				{ key: 'other', name: '其他' }
			],
			departments: [],
			loading: false
		};
	},
	computed: {
		filteredDepartments() {
			console.log('当前分类:', this.selectedCategory);
			console.log('所有科室:', this.departments);
			
			if (this.selectedCategory === 'all') {
				return this.departments;
			}
			
			// 前端根据科室名称判断分类
			const result = this.departments.filter(dept => {
				const name = dept.name || '';
				switch(this.selectedCategory) {
					case 'internal':
						// 匹配"内科"但排除"外科"
						return name.includes('内科') && !name.includes('外科');
					case 'surgery':
						return name.includes('外科');
					case 'pediatrics':
						return name.includes('儿科');
					case 'gynecology':
						return name.includes('妇产科') || name.includes('妇科');
					case 'other':
						return !name.includes('内科') && !name.includes('外科') && 
						       !name.includes('儿科') && !name.includes('妇产科') && !name.includes('妇科');
					default:
						return true;
				}
			});
			
			console.log('筛选结果:', result);
			return result;
		}
	},
	onLoad() {
		this.loadDepartments();
	},
	methods: {
		// 加载科室列表
		async loadDepartments() {
			this.loading = true;
			try {
				const data = await getDepartmentList();
				this.departments = data || [];
				console.log('加载到的科室数据:', this.departments);
			} catch (error) {
				console.error('加载科室列表失败:', error);
				this.departments = [];
				uni.showToast({
					title: error.msg || '加载失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		},
		
		// 选择分类
		selectCategory(key) {
			this.selectedCategory = key;
		},
		
		// 获取分类名称
		getCategoryName(key) {
			const category = this.categories.find(item => item.key === key);
			return category ? category.name : '';
		},
		
		// 根据科室名称判断分类
		getDepartmentCategory(name) {
			if (!name) return '其他';
			if (name.includes('内科')) return '内科';
			if (name.includes('外科')) return '外科';
			if (name.includes('儿科')) return '儿科';
			if (name.includes('妇产科')) return '妇产科';
			return '其他';
		},
		
		// 跳转到门诊列表
		goToDoctorList(department) {
			// 新流程：科室 → 门诊 → 近七日号源
			uni.navigateTo({
				url: `/pages/clinic-list/clinic-list?departmentId=${department.id}&departmentName=${encodeURIComponent(department.name)}`
			});
		}
	}
};
</script>

<style scoped>
.department-list-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 30rpx;
}

/* 分类导航 */
.category-nav {
	background: #fff;
	padding: 20rpx 0;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}
.category-scroll {
	width: 100%;
	white-space: nowrap;
}
.category-list {
	display: inline-flex;
	padding: 0 30rpx;
	gap: 20rpx;
}
.category-item {
	padding: 12rpx 28rpx;
	background: #f5f7fa;
	border-radius: 30rpx;
	flex-shrink: 0;
	transition: all 0.3s;
}
.category-item.active {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
}
.category-text {
	font-size: 26rpx;
	color: #666;
	white-space: nowrap;
}
.category-item.active .category-text {
	color: #fff;
	font-weight: 600;
}

/* 科室列表 */
.department-list {
	padding: 30rpx;
}
.department-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.card-header {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 20rpx;
}
.department-icon-box {
	width: 80rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.department-icon {
	font-size: 40rpx;
}
.department-main {
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
.department-tags {
	display: flex;
	gap: 10rpx;
}
.tag {
	font-size: 20rpx;
	color: #666;
	background: #f5f7fa;
	padding: 4rpx 12rpx;
	border-radius: 10rpx;
}
.tag.hot {
	color: #ff5722;
	background: #ffebee;
}
.arrow-icon {
	font-size: 40rpx;
	color: #ccc;
	font-weight: 300;
}
.card-body {
	padding-bottom: 20rpx;
	border-bottom: 1rpx solid #f0f0f0;
	margin-bottom: 20rpx;
}
.department-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.6;
}
.card-footer {
	display: flex;
	gap: 40rpx;
}
.info-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
}
.info-icon {
	font-size: 28rpx;
}
.info-text {
	font-size: 24rpx;
	color: #666;
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
</style>

