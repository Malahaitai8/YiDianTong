<template>
	<view class="personal-info-page">
		<!-- 页面标题 -->
		<view class="page-title">
			<text class="title-text">身份认证</text>
			<text class="title-desc">完成认证后可享受医保报销服务</text>
		</view>

		<!-- 认证状态提示 -->
		<view class="status-card" v-if="isVerified">
			<view class="status-header">
				<text class="status-icon">✓</text>
				<text class="status-title">已认证</text>
			</view>
			<view class="status-info">
				<text class="info-item">姓名：{{ patientInfo.name }}</text>
				<text class="info-item" v-if="patientInfo.specificRole">
					身份：{{ roleText }}
				</text>
				<text class="info-item" v-if="patientInfo.identityNumber">
					学号/工号：{{ patientInfo.identityNumber }}
				</text>
			</view>
		</view>

		<!-- 表单区域 -->
		<view class="form-section" v-if="!isVerified">
			<!-- 真实姓名 -->
			<view class="form-item">
				<text class="label">真实姓名<text class="required">*</text></text>
				<input 
					class="input" 
					v-model="formData.name" 
					placeholder="请输入您的真实姓名"
					placeholder-class="placeholder"
				/>
			</view>

			<!-- 学号/工号 -->
			<view class="form-item">
				<text class="label">学号/工号<text class="required">*</text></text>
				<input 
					class="input" 
					v-model="formData.identityNumber" 
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
					v-model="formData.idCardNumber" 
					type="idcard"
					placeholder="请输入身份证号"
					placeholder-class="placeholder"
					maxlength="18"
				/>
				<text class="hint">身份证号将加密存储，仅用于身份核验</text>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-card" v-if="!isVerified">
			<view class="tips-header">
				<text class="tips-icon">💡</text>
				<text class="tips-title">温馨提示</text>
			</view>
			<text class="tips-text">完成身份认证后，您将享受校医院的医保报销服务：</text>
			<view class="reimbursement-info">
				<view class="reimbursement-item">
					<text class="reimb-icon">🎓</text>
					<text class="reimb-text">学生：95% 报销</text>
				</view>
				<view class="reimbursement-item">
					<text class="reimb-icon">👨‍🏫</text>
					<text class="reimb-text">教师：90% 报销</text>
				</view>
				<view class="reimbursement-item">
					<text class="reimb-icon">👤</text>
					<text class="reimb-text">外部人员：按标准报销</text>
				</view>
			</view>
			<text class="tips-text">请确保您的信息真实有效，系统将根据白名单自动识别您的身份类型。</text>
		</view>

		<!-- 提交按钮 -->
		<view class="submit-section" v-if="!isVerified">
			<button class="submit-btn" @click="handleSubmit" :loading="loading">
				{{ loading ? '认证中...' : '提交认证' }}
			</button>
		</view>
	</view>
</template>

<script>
import { verifyIdentity } from '@/api/auth.js';
import { getPatientProfile } from '@/api/patient.js';

export default {
	data() {
		return {
			formData: {
				name: '', // 真实姓名
				identityNumber: '', // 学号/工号（统一字段）
				idCardNumber: '' // 身份证号
			},
			loading: false,
			isVerified: false, // 是否已认证
			patientInfo: {} // 患者信息
		};
	},
	computed: {
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
		// 页面加载时检查认证状态
		this.checkVerifyStatus();
	},
	methods: {
		// 检查认证状态
		async checkVerifyStatus() {
			try {
				const data = await getPatientProfile();
				if (data) {
					this.patientInfo = data;
					// 判断是否已认证：idStatus === 'verified' 或 '已认证'
					this.isVerified = data.idStatus === 'verified' || data.idStatus === '已认证';
					
					// 如果已认证，填充表单数据（只读）
					if (this.isVerified) {
						this.formData.name = data.name || '';
						this.formData.identityNumber = data.identityNumber || '';
						this.formData.idCardNumber = data.idCardNumber || '';
					}
				}
			} catch (error) {
				console.error('获取认证状态失败:', error);
			}
		},
		async handleSubmit() {
			const { name, identityNumber, idCardNumber } = this.formData;
			
			// 必填项验证
			if (!name || !identityNumber || !idCardNumber) {
				uni.showToast({
					title: '请填写所有必填项',
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

			this.loading = true;
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
					
					// 延迟返回上一页
					setTimeout(() => {
						uni.navigateBack();
					}, 1500);
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
				this.loading = false;
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

/* 表单区域 */
.form-section {
	margin: 30rpx;
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

/* 认证状态卡片 */
.status-card {
	margin: 30rpx;
	background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	box-shadow: 0 4rpx 15rpx rgba(76, 175, 80, 0.2);
}
.status-header {
	display: flex;
	align-items: center;
	gap: 15rpx;
	margin-bottom: 25rpx;
}
.status-icon {
	font-size: 48rpx;
	color: #4caf50;
	font-weight: bold;
}
.status-title {
	font-size: 32rpx;
	color: #2e7d32;
	font-weight: 600;
}
.status-info {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}
.info-item {
	font-size: 26rpx;
	color: #333;
	line-height: 1.6;
}

/* 温馨提示 */
.tips-card {
	margin: 0 30rpx 30rpx;
	background: #fff3e0;
	border-radius: 20rpx;
	padding: 30rpx;
	border-left: 6rpx solid #ff9800;
}
.tips-header {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 15rpx;
}
.tips-icon {
	font-size: 28rpx;
}
.tips-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}
.tips-text {
	font-size: 24rpx;
	color: #666;
	line-height: 1.6;
	display: block;
	margin-bottom: 15rpx;
}
.reimbursement-info {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
	margin-bottom: 15rpx;
}
.reimbursement-item {
	flex: 1;
	background: #fff;
	border-radius: 12rpx;
	padding: 15rpx;
	display: flex;
	align-items: center;
	gap: 10rpx;
}
.reimb-icon {
	font-size: 32rpx;
}
.reimb-text {
	font-size: 24rpx;
	color: #333;
	font-weight: 600;
}

/* 提交按钮 */
.submit-section {
	padding: 0 30rpx;
}
.submit-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	color: #fff;
	border: none;
	border-radius: 15rpx;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(25, 118, 210, 0.3);
}
.submit-btn::after {
	border: none;
}
</style>



