<template>
	<view class="personal-info-page">
		<!-- 页面标题 -->
		<view class="page-title">
			<text class="title-text">个人信息</text>
			<text class="title-desc">{{ isVerified ? '您的个人信息' : '完善信息并完成认证' }}</text>
		</view>

		<!-- 未认证状态：可编辑信息 -->
		<view v-if="!isVerified" class="info-section">
			<!-- 基本信息卡片 -->
			<view class="info-card">
				<view class="card-title">基本信息</view>
				
				<!-- 用户名（只读） -->
				<view class="info-item">
					<text class="info-label">用户名</text>
					<text class="info-value">{{ userInfo.username || '未设置' }}</text>
				</view>
				
				<!-- 电话号码 -->
				<view class="info-item">
					<text class="info-label">电话号码</text>
					<text class="info-value">{{ patientInfo.phoneNumber || '未设置' }}</text>
				</view>
				
				<!-- 认证状态 -->
				<view class="info-item">
					<text class="info-label">认证状态</text>
					<view class="status-badge unverified">
						<text class="status-icon">⚠</text>
						<text class="status-text">未认证</text>
					</view>
				</view>
			</view>

			<!-- 修改信息按钮 -->
			<view class="action-section">
				<button class="edit-btn" @click="handleEditInfo">
					修改信息
				</button>
			</view>

			<!-- 认证入口 -->
			<view class="verify-section">
				<view class="verify-card">
					<view class="verify-header">
						<text class="verify-icon">🔐</text>
						<text class="verify-title">身份认证</text>
					</view>
					<text class="verify-desc">完成身份认证后可享受医保报销服务</text>
					<button class="verify-btn" @click="showVerifyForm = !showVerifyForm">
						{{ showVerifyForm ? '收起认证表单' : '开始认证' }}
					</button>
				</view>

				<!-- 认证表单（可展开/收起） -->
				<view class="verify-form-card" v-if="showVerifyForm">
					<!-- 真实姓名 -->
					<view class="form-item">
						<text class="label">真实姓名<text class="required">*</text></text>
						<input 
							class="input" 
							v-model="verifyForm.name" 
							placeholder="请输入您的真实姓名（仅支持汉字或英文字母）"
							placeholder-class="placeholder"
							@input="handleNameInput"
						/>
						<text class="hint">真实姓名只能包含汉字或英文字母</text>
					</view>

					<!-- 学号/工号 -->
					<view class="form-item">
						<text class="label">学号/工号<text class="required">*</text></text>
						<input 
							class="input" 
							v-model="verifyForm.identityNumber" 
							placeholder="请输入您的学号或工号"
							placeholder-class="placeholder"
						/>
						<text class="hint">系统将根据白名单自动识别您的身份类型（学生/教师/外部人员）</text>
					</view>

					<!-- 身份证号 -->
					<view class="form-item">
						<text class="label">身份证号<text class="required">*</text></text>
						<input 
							class="input" 
							v-model="verifyForm.idCardNumber" 
							type="idcard"
							placeholder="请输入身份证号"
							placeholder-class="placeholder"
							maxlength="18"
						/>
						<text class="hint">身份证号将加密存储，仅用于身份核验</text>
					</view>

					<!-- 提交认证按钮 -->
					<button class="submit-verify-btn" @click="handleVerify" :loading="verifying">
						{{ verifying ? '认证中...' : '提交认证' }}
					</button>
				</view>
			</view>
		</view>

		<!-- 已认证状态：展示全部信息 -->
		<view v-else class="info-section">
			<view class="info-card">
				<view class="card-title">个人信息</view>
				
				<!-- 用户名 -->
				<view class="info-item">
					<text class="info-label">用户名</text>
					<text class="info-value">{{ userInfo.username || '未设置' }}</text>
				</view>
				
				<!-- 电话号码 -->
				<view class="info-item">
					<text class="info-label">电话号码</text>
					<text class="info-value">{{ patientInfo.phoneNumber || '未设置' }}</text>
				</view>
				
				<!-- 认证状态 -->
				<view class="info-item">
					<text class="info-label">认证状态</text>
					<view class="status-badge verified">
						<text class="status-icon">✓</text>
						<text class="status-text">已认证</text>
					</view>
				</view>
				
				<!-- 真实姓名 -->
				<view class="info-item">
					<text class="info-label">真实姓名</text>
					<text class="info-value">{{ patientInfo.name || '未设置' }}</text>
				</view>
				
				<!-- 用户身份 -->
				<view class="info-item" v-if="patientInfo.specificRole">
					<text class="info-label">用户身份</text>
					<text class="info-value">{{ roleText }}</text>
				</view>
				
				<!-- 学号/工号 -->
				<view class="info-item" v-if="patientInfo.identityNumber">
					<text class="info-label">学号/工号</text>
					<text class="info-value">{{ patientInfo.identityNumber }}</text>
				</view>
			</view>
			
			<!-- 修改信息按钮 -->
			<view class="action-section">
				<button class="edit-btn" @click="handleEditInfo">
					修改信息
				</button>
			</view>
		</view>
	</view>
</template>

<script>
import { verifyIdentity } from '@/api/auth.js';
import { getPatientProfile } from '@/api/patient.js';
import { promptLogin } from '@/utils/auth.js';

export default {
	data() {
		return {
			isVerified: false, // 是否已认证
			patientInfo: {}, // 患者信息
			showVerifyForm: false, // 是否显示认证表单
			verifying: false, // 认证中
			verifyForm: {
				name: '', // 真实姓名
				identityNumber: '', // 学号/工号
				idCardNumber: '' // 身份证号
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
		},
		// 角色文本
		roleText() {
			const role = this.patientInfo.specificRole;
			if (role === 'student') return '学生';
			if (role === 'teacher') return '教师';
			if (role === 'outsider') return '外部人员';
			return '';
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
		// 页面加载时检查认证状态
		this.loadPatientInfo();
	},
	onShow() {
		// 页面显示时刷新信息
		if (this.isLoggedIn) {
			this.loadPatientInfo();
		}
	},
		methods: {
		// 加载患者信息
		async loadPatientInfo() {
			if (!this.isLoggedIn) return;
			
			try {
				const data = await getPatientProfile();
				if (data) {
					this.patientInfo = data;
					// 判断是否已认证：idStatus === 'verified' 或 '已认证'
					this.isVerified = data.idStatus === 'verified' || data.idStatus === '已认证';
					
					// 如果已认证，填充认证表单（只读显示用）
					if (this.isVerified) {
						this.verifyForm.name = data.name || '';
						this.verifyForm.identityNumber = data.identityNumber || '';
					}
				}
			} catch (error) {
				console.error('获取患者信息失败:', error);
				uni.showToast({
					title: '获取信息失败',
					icon: 'none'
				});
			}
		},
		// 跳转到编辑信息页面
		handleEditInfo() {
			uni.navigateTo({
				url: '/pages/edit-info/edit-info'
			});
		},
		// 处理真实姓名输入
		handleNameInput(e) {
			const value = e.detail.value;
			// 只允许汉字和英文字母
			const filteredValue = value.replace(/[^\u4e00-\u9fa5a-zA-Z]/g, '');
			if (value !== filteredValue) {
				this.verifyForm.name = filteredValue;
				uni.showToast({
					title: '真实姓名只能包含汉字或英文字母',
					icon: 'none',
					duration: 2000
				});
			}
		},
		
		// 提交认证
		async handleVerify() {
			const { name, identityNumber, idCardNumber } = this.verifyForm;
			
			// 必填项验证
			if (!name || !identityNumber || !idCardNumber) {
				uni.showToast({
					title: '请填写所有必填项',
					icon: 'none'
				});
				return;
			}

			// 真实姓名格式验证（只允许汉字或英文字母）
			if (!/^[\u4e00-\u9fa5a-zA-Z]+$/.test(name)) {
				uni.showToast({
					title: '真实姓名只能包含汉字或英文字母',
					icon: 'none'
				});
				return;
			}

			// 身份证号验证
			if (!/^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(idCardNumber)) {
				uni.showToast({
					title: '请输入有效的身份证号',
					icon: 'none'
				});
				return;
			}

			this.verifying = true;
			try {
				// 调用身份认证接口
				const data = await verifyIdentity({
					name: name,
					identityNumber: identityNumber,
					idCardNumber: idCardNumber
				});
				
				if (data) {
					uni.showToast({
						title: '身份认证成功',
						icon: 'success'
					});
					
					// 更新认证状态
					this.isVerified = true;
					this.patientInfo = data;
					this.showVerifyForm = false;
					
					// 刷新信息
					await this.loadPatientInfo();
				} else {
					uni.showToast({
						title: '认证失败，请重试',
						icon: 'none',
						duration: 3000
					});
				}
			} catch (error) {
				console.error('认证失败:', error);
				uni.showToast({
					title: (error && (error.msg || error.message)) || '认证失败，请重试',
					icon: 'none',
					duration: 3000
				});
			} finally {
				this.verifying = false;
			}
		}
	}
};
</script>

<style scoped>
.personal-info-page {
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

/* 信息区域 */
.info-section {
	padding: 30rpx;
}

/* 信息卡片 */
.info-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
	margin-bottom: 30rpx;
}
.card-title {
	font-size: 32rpx;
	color: #333;
	font-weight: 600;
	margin-bottom: 30rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}
.info-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 25rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}
.info-item:last-child {
	border-bottom: none;
}
.info-label {
	font-size: 28rpx;
	color: #666;
	font-weight: 500;
	min-width: 160rpx;
}
.info-value {
	font-size: 28rpx;
	color: #333;
	flex: 1;
	text-align: right;
}
.info-input {
	font-size: 28rpx;
	color: #333;
	flex: 1;
	text-align: right;
	padding: 10rpx 0;
}
.info-input.editable {
	color: #1976d2;
}

/* 状态徽章 */
.status-badge {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}
.status-badge.verified {
	background: #e8f5e9;
	color: #4caf50;
}
.status-badge.unverified {
	background: #fff3e0;
	color: #ff9800;
}
.status-icon {
	font-size: 24rpx;
}
.status-text {
	font-size: 24rpx;
	font-weight: 500;
}

/* 操作按钮区域 */
.action-section {
	margin-bottom: 30rpx;
}
.edit-btn {
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
.edit-btn::after {
	border: none;
}

/* 认证区域 */
.verify-section {
	margin-top: 30rpx;
}
.verify-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
	margin-bottom: 20rpx;
}
.verify-header {
	display: flex;
	align-items: center;
	gap: 15rpx;
	margin-bottom: 15rpx;
}
.verify-icon {
	font-size: 36rpx;
}
.verify-title {
	font-size: 32rpx;
	color: #333;
	font-weight: 600;
}
.verify-desc {
	font-size: 24rpx;
	color: #666;
	line-height: 1.6;
	margin-bottom: 25rpx;
	display: block;
}
.verify-btn {
	width: 100%;
	height: 80rpx;
	background: #fff;
	color: #1976d2;
	border: 2rpx solid #1976d2;
	border-radius: 12rpx;
	font-size: 28rpx;
	font-weight: 600;
}
.verify-btn::after {
	border: none;
}

/* 认证表单卡片 */
.verify-form-card {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}
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
.hint {
	font-size: 22rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
}
.submit-verify-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 15rpx;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
	margin-top: 20rpx;
}
.submit-verify-btn::after {
	border: none;
}
</style>
