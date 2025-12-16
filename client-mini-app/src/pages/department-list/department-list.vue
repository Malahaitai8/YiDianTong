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
			>
				<view class="card-header" @click="goToDoctorList(item)">
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
				<!-- 门诊下拉区域 -->
				<view class="clinic-section">
					<view 
						class="clinic-toggle" 
						@click.stop="toggleClinicList(item.id)"
					>
						<text class="toggle-text">
							{{ expandedClinics[item.id] ? '收起门诊' : '展开门诊' }}
						</text>
						<text class="toggle-icon" :class="{ expanded: expandedClinics[item.id] }">▼</text>
					</view>
					<view 
						class="clinic-list" 
						v-if="expandedClinics[item.id]"
					>
						<!-- 加载状态 -->
						<view class="clinic-loading" v-if="loadingClinics[item.id]">
							<text class="loading-text">加载中...</text>
						</view>
						<!-- 门诊列表 -->
						<template v-else>
							<view 
								class="clinic-item" 
								v-for="clinic in (item.clinics || [])" 
								:key="clinic.id"
								:data-clinic-id="clinic.id"
								:data-department-id="item.id"
								:data-clinic-name="clinic.name"
								@click.stop="onClinicTap"
							>
								<text class="clinic-name">{{ clinic.name }}</text>
								<text class="clinic-arrow">›</text>
							</view>
							<view class="clinic-empty" v-if="!item.clinics || item.clinics.length === 0">
								<text class="empty-text">该科室暂无门诊</text>
							</view>
						</template>
					</view>
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
import { getClinicsByDepartmentId, getClinicById } from '@/api/clinic.js';

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
			loading: false,
			expandedClinics: {}, // 记录哪些科室的门诊列表已展开
			loadingClinics: {} // 记录哪些科室的门诊正在加载
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
		
		// 跳转到门诊列表（科室详情页）
		goToDoctorList(department) {
			// 新流程：科室 → 门诊 → 近七日号源
			uni.navigateTo({
				url: `/pages/clinic-list/clinic-list?departmentId=${department.id}&departmentName=${encodeURIComponent(department.name)}`
			});
		},
		
		// 切换门诊列表展开/收起
		async toggleClinicList(departmentId) {
			// 切换展开状态
			this.$set(this.expandedClinics, departmentId, !this.expandedClinics[departmentId]);
			
			// 如果展开，每次都刷新门诊列表（避免旧数据为科室信息）
			if (this.expandedClinics[departmentId]) {
				const department = this.departments.find(d => d.id === departmentId);
				if (department) {
					// 标记为加载中
					this.$set(this.loadingClinics, departmentId, true);
					try {
						const clinics = await getClinicsByDepartmentId(departmentId);
						// 将门诊数据添加到科室对象中，并确保每个门诊都有 departmentId / id / name
						const clinicsWithDeptId = (Array.isArray(clinics) ? clinics : []).map(clinic => {
							const normalizedId = clinic.id ?? clinic.clinicId ?? clinic.clinic_id;
							const normalizedName = clinic.name || clinic.clinicName || clinic.clinic_name || '';
							const normalizedDeptId = Number(departmentId);
							console.log('加载到的门诊数据:', clinic, '规范化后:', {
								id: Number(normalizedId),
								name: normalizedName,
								departmentId: normalizedDeptId
							});
							return {
								...clinic,
								id: Number(normalizedId), // 确保有 id 字段且为数字
								departmentId: normalizedDeptId, // 确保门诊对象包含 departmentId
								// 确保 name 字段是门诊名称（不是科室名称）
								name: normalizedName
							};
						}).sort((a, b) => (Number(a.id) || 0) - (Number(b.id) || 0)); // 按门诊ID排序
						console.log('处理后的门诊列表:', clinicsWithDeptId);
						this.$set(department, 'clinics', clinicsWithDeptId);
					} catch (error) {
						console.error('加载门诊列表失败:', error);
						uni.showToast({
							title: error.msg || '加载门诊失败',
							icon: 'none'
						});
						this.$set(department, 'clinics', []);
					} finally {
						this.$set(this.loadingClinics, departmentId, false);
					}
				}
			}
		},
		
		// 通过事件数据跳转，避免参数错位
		async onClinicTap(e) {
			const { clinicId, departmentId, clinicName } = e.currentTarget.dataset || {};
			const deptIdNum = Number(departmentId);
			const clinicIdNum = Number(clinicId);
			console.log('onClinicTap 入参 dataset:', e.currentTarget.dataset);
			if (!clinicIdNum) {
				uni.showToast({ title: '门诊信息不完整', icon: 'none' });
				return;
			}
			// 从当前科室列表中查找对应门诊对象
			const department = this.departments.find(d => Number(d.id) === deptIdNum);
			let clinic = null;
			if (department && Array.isArray(department.clinics)) {
				clinic = department.clinics.find(c => Number(c.id) === clinicIdNum);
			}
			// 兜底直接构造
			if (!clinic) {
				clinic = { id: clinicIdNum, departmentId: deptIdNum, name: clinicName || '' };
			}
			await this.goToClinicDetail(clinic);
		},
		
		// 跳转到门诊详情页（完全模仿clinic-list页面的跳转逻辑）
		async goToClinicDetail(clinic) {
			// 兜底：支持 id / clinicId / clinic_id，并强制转为数字，避免拿到科室ID
			const clinicId = Number(clinic?.id ?? clinic?.clinicId ?? clinic?.clinic_id);
			if (!clinic || Number.isNaN(clinicId)) {
				uni.showToast({
					title: '门诊信息不完整',
					icon: 'none'
				});
				return;
			}
			
			// 基础信息（先用本地，后面会用后端覆盖）
			let clinicName = clinic.name || clinic.clinicName || clinic.clinic_name || '';
			let departmentName = '';
			let departmentId = clinic.departmentId ? Number(clinic.departmentId) : null;

			// 如果本地数据缺少名称/科室信息，调用接口获取详情，确保使用后台返回的真实门诊名称
			try {
				const detail = await getClinicById(clinicId);
				if (detail) {
					clinicName = detail.name || clinicName;
					if (detail.department) {
						departmentName = detail.department.name || departmentName;
					}
					if (!departmentId && detail.departmentId) {
						departmentId = detail.departmentId;
					}
				}
			} catch (e) {
				console.warn('获取门诊详情失败，使用本地数据', e);
			}

			if (!clinicName) {
				console.error('门诊名称缺失，门诊对象:', clinic);
				uni.showToast({
					title: '门诊名称缺失',
					icon: 'none'
				});
				return;
			}
			
			// 获取科室名称：优先用 departmentId，其次查找包含该门诊的科室
			if (departmentId) {
				const department = this.departments.find(d => d.id === departmentId);
				departmentName = department ? department.name : departmentName;
			}
			if (!departmentName) {
				const department = this.departments.find(d => 
					d.clinics && d.clinics.some(c => c.id === clinicId)
				);
				departmentName = department ? department.name : departmentName;
			}
			
			console.log('跳转到门诊详情页:', {
				clinicId: clinicId,
				clinicName: clinicName,
				departmentName: departmentName,
				clinicObject: clinic,
				注意: 'clinicName应该是门诊名称（如"内分泌科门诊"），不是科室名称（如"内科"）'
			});
			
			// 完全按照clinic-list.vue的goToClinicAppointment方法进行跳转
			uni.navigateTo({
				url: `/pages/clinic-appointment/clinic-appointment?clinicId=${clinicId}&clinicName=${encodeURIComponent(clinicName)}&departmentName=${encodeURIComponent(departmentName)}`
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

/* 门诊下拉区域 */
.clinic-section {
	border-top: 1rpx solid #f0f0f0;
	margin-top: 20rpx;
	padding-top: 20rpx;
}
.clinic-toggle {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 0;
	cursor: pointer;
}
.toggle-text {
	font-size: 26rpx;
	color: #1976d2;
	font-weight: 500;
}
.toggle-icon {
	font-size: 24rpx;
	color: #1976d2;
	transition: transform 0.3s;
}
.toggle-icon.expanded {
	transform: rotate(180deg);
}
.clinic-list {
	margin-top: 10rpx;
	padding-left: 20rpx;
}
.clinic-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	cursor: pointer;
}
.clinic-item:last-child {
	border-bottom: none;
}
.clinic-item:active {
	background-color: #f5f7fa;
}
.clinic-name {
	font-size: 28rpx;
	color: #333;
}
.clinic-arrow {
	font-size: 32rpx;
	color: #ccc;
	font-weight: 300;
}
.clinic-empty {
	padding: 30rpx 0;
	text-align: center;
}
.clinic-empty .empty-text {
	font-size: 24rpx;
	color: #999;
}
.clinic-loading {
	padding: 30rpx 0;
	text-align: center;
}
.clinic-loading .loading-text {
	font-size: 24rpx;
	color: #999;
}
</style>

