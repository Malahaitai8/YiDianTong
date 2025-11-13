<template>
	<view class="edit-info-page">
		<!-- 页面标题 -->
		<view class="page-title">
			<text class="title-text">修改信息</text>
			<text class="title-desc">修改您的个人信息</text>
		</view>

		<!-- 编辑表单 -->
		<view class="form-section">
			<view class="form-card">
				<view class="card-title">基本信息</view>
				
				<!-- 用户名（只读） -->
				<view class="form-item">
					<text class="label">用户名</text>
					<text class="readonly-value">{{ userInfo.username || '未设置' }}</text>
				</view>
				
				<!-- 电话号码（可编辑） -->
				<view class="form-item">
					<text class="label">电话号码<text class="required">*</text></text>
					<input 
						class="input" 
						v-model="editForm.phoneNumber" 
						type="number"
						placeholder="请输入电话号码"
						placeholder-class="placeholder"
						maxlength="11"
					/>
				</view>
			</view>
		</view>

		<!-- 保存按钮 -->
		<view class="action-section">
			<button class="save-btn" @click="handleSave" :loading="saving">
				{{ saving ? '保存中...' : '保存' }}
			</button>
		</view>
	</view>
</template>

<script>
import { getPatientProfile, updatePatientProfile } from '@/api/patient.js';
import { promptLogin } from '@/utils/auth.js';

export default {
	data() {
		return {
			patientInfo: {}, // 患者信息
			saving: false, // 保存中
			editForm: {
				phoneNumber: '' // 电话号码
			}
		};
	},
	computed: {
		// 检查是否已登录
		isLoggedIn() {
			return !!this.$store.state.user.token;
		},
		userInfo() {
			return this.$store.state.user.userInfo || {};
		}
	},
	onLoad() {
		// 检查登录状态
		if (!this.isLoggedIn) {
			promptLogin();
			setTimeout(() => {
				uni.navigateBack();
			}, 1500);
			return;
		}
		// 加载患者信息
		this.loadPatientInfo();
	},
	methods: {
		// 加载患者信息
		async loadPatientInfo() {
			if (!this.isLoggedIn) return;
			
			try {
				const data = await getPatientProfile();
				if (data) {
					this.patientInfo = data;
					// 填充编辑表单
					this.editForm.phoneNumber = data.phoneNumber || '';
				}
			} catch (error) {
				console.error('获取患者信息失败:', error);
				uni.showToast({
					title: '获取信息失败',
					icon: 'none'
				});
			}
		},
		// 保存信息
		async handleSave() {
			const { phoneNumber } = this.editForm;
			
			// 验证电话号码
			if (!phoneNumber || !/^1[3-9]\d{9}$/.test(phoneNumber)) {
				uni.showToast({
					title: '请输入有效的手机号码',
					icon: 'none'
				});
				return;
			}
			
			this.saving = true;
			try {
				// 更新患者信息（只能更新电话号码）
				await updatePatientProfile({
					phoneNumber: phoneNumber
				});
				
				uni.showToast({
					title: '保存成功',
					icon: 'success'
				});
				
				// 延迟返回，让用户看到成功提示
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
			} catch (error) {
				console.error('保存信息失败:', error);
				uni.showToast({
					title: (error && (error.msg || error.message)) || '保存失败，请重试',
					icon: 'none',
					duration: 3000
				});
			} finally {
				this.saving = false;
			}
		}
	}
};
</script>

<style scoped>
.edit-info-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 120rpx;
}

/* 页面标题 */
.page-title {
	background: linear-gradient(135deg, #1976d2 0%, #2196f3 100%);
	padding: 50rpx 30rpx;
	color: #fff;
}
.title-text {
	font-size: 36rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 12rpx;
}
.title-desc {
	font-size: 24rpx;
	opacity: 0.9;
}

/* 表单区域 */
.form-section {
	padding: 30rpx;
}

/* 表单卡片 */
.form-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.card-title {
	font-size: 32rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

/* 表单项 */
.form-item {
	margin-bottom: 35rpx;
}
.form-item:last-child {
	margin-bottom: 0;
}
.label {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
	display: block;
	margin-bottom: 15rpx;
}
.required {
	color: #f44336;
	margin-left: 4rpx;
}
.input {
	height: 80rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	color: #333;
	border: 2rpx solid transparent;
	transition: all 0.3s;
}
.input:focus {
	background: #fff;
	border-color: #1976d2;
}
.placeholder {
	color: #999;
	font-size: 26rpx;
}
.readonly-value {
	font-size: 28rpx;
	color: #999;
	padding: 20rpx 0;
	display: block;
}

/* 操作按钮区域 */
.action-section {
	padding: 0 30rpx;
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding-top: 30rpx;
	padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 15rpx rgba(0, 0, 0, 0.05);
}
.save-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 15rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
}
.save-btn::after {
	border: none;
}
</style>

