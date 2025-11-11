<template>
	<view class="personal-info-page">
		<!-- 页面标题 -->
		<view class="page-title">
			<text class="title-text">完善身份信息</text>
			<text class="title-desc">完成认证后可享受医保报销服务</text>
		</view>

		<!-- 表单区域 -->
		<view class="form-section">
			<!-- 真实姓名 -->
			<view class="form-item">
				<text class="label">真实姓名<text class="required">*</text></text>
				<input 
					class="input" 
					v-model="formData.realName" 
					placeholder="请输入您的真实姓名"
					placeholder-class="placeholder"
				/>
			</view>

			<!-- 身份类型 -->
			<view class="form-item">
				<text class="label">身份类型<text class="required">*</text></text>
				<view class="identity-selector">
					<view 
						class="identity-option" 
						:class="{ active: formData.identityType === 'student' }" 
						@click="selectIdentity('student')"
					>
						<text class="option-icon">🎓</text>
						<text class="option-text">学生</text>
					</view>
					<view 
						class="identity-option" 
						:class="{ active: formData.identityType === 'teacher' }" 
						@click="selectIdentity('teacher')"
					>
						<text class="option-icon">👨‍🏫</text>
						<text class="option-text">教师</text>
					</view>
				</view>
			</view>

			<!-- 学号/工号 -->
			<view class="form-item">
				<text class="label">{{ identityLabel }}<text class="required">*</text></text>
				<input 
					class="input" 
					v-model="formData.identityNumber" 
					:placeholder="`请输入您的${identityLabel}`"
					placeholder-class="placeholder"
				/>
			</view>

			<!-- 身份证号 -->
			<view class="form-item">
				<text class="label">身份证号</text>
				<input 
					class="input" 
					v-model="formData.idCard" 
					type="idcard"
					placeholder="请输入身份证号（选填）"
					placeholder-class="placeholder"
					maxlength="18"
				/>
				<text class="hint">身份证号将加密存储，仅用于身份核验</text>
			</view>

			<!-- 出生日期 -->
			<view class="form-item">
				<text class="label">出生日期</text>
				<picker mode="date" :value="formData.birthDate" @change="onDateChange" :end="todayDate">
					<view class="picker-input">
						{{ formData.birthDate || '请选择出生日期' }}
					</view>
				</picker>
			</view>

			<!-- 联系电话 -->
			<view class="form-item">
				<text class="label">联系电话<text class="required">*</text></text>
				<input 
					class="input" 
					v-model="formData.phoneNumber" 
					type="number"
					placeholder="请输入手机号"
					placeholder-class="placeholder"
					maxlength="11"
				/>
			</view>

			<!-- 既往病史 -->
			<view class="form-item">
				<text class="label">既往病史</text>
				<textarea 
					class="textarea" 
					v-model="formData.medicalHistory" 
					placeholder="请填写您的既往病史（选填，500字以内）"
					placeholder-class="placeholder"
					maxlength="500"
				></textarea>
				<view class="textarea-footer">
					<text class="hint">敏感信息将加密存储，仅供医生参考</text>
					<text class="word-count">{{ formData.medicalHistory.length }}/500</text>
				</view>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-card">
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
			</view>
			<text class="tips-text">请确保您的信息真实有效，以便医生更好地为您服务。</text>
		</view>

		<!-- 保存按钮 -->
		<view class="submit-section">
			<button class="submit-btn" @click="handleSubmit" :loading="loading">
				{{ loading ? '保存中...' : '保存信息' }}
			</button>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			formData: {
				realName: '',
				identityType: '', // 'student' or 'teacher'
				identityNumber: '', // 学号或工号
				idCard: '',
				birthDate: '',
				phoneNumber: '',
				medicalHistory: ''
			},
			loading: false
		};
	},
	computed: {
		identityLabel() {
			return this.formData.identityType === 'student' ? '学号' :
			       this.formData.identityType === 'teacher' ? '工号' : '身份标识';
		},
		todayDate() {
			const today = new Date();
			return today.toISOString().split('T')[0];
		}
	},
	methods: {
		selectIdentity(type) {
			this.formData.identityType = type;
		},
		onDateChange(e) {
			this.formData.birthDate = e.detail.value;
		},
		async handleSubmit() {
			const { realName, identityType, identityNumber, phoneNumber } = this.formData;
			
			// 必填项验证
			if (!realName || !identityType || !identityNumber || !phoneNumber) {
				uni.showToast({
					title: '请填写所有带*的必填项',
					icon: 'none'
				});
				return;
			}

			// 手机号验证
			if (!/^1[3-9]\d{9}$/.test(phoneNumber)) {
				uni.showToast({
					title: '请输入有效的手机号',
					icon: 'none'
				});
				return;
			}

			// 身份证号验证（如果填写了）
			if (this.formData.idCard && !/^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(this.formData.idCard)) {
				uni.showToast({
					title: '请输入有效的身份证号',
					icon: 'none'
				});
				return;
			}

			this.loading = true;
			try {
				// TODO: 调用后端API保存患者信息
				// await updatePatientInfo(this.$store.state.user.userInfo.userId, this.formData);
				
				// 模拟保存
				await new Promise(resolve => setTimeout(resolve, 1000));
				
				uni.showToast({
					title: '身份信息保存成功',
					icon: 'success'
				});
				
				setTimeout(() => {
					uni.navigateBack();
				}, 1500);
			} catch (error) {
				console.error('保存失败:', error);
				uni.showToast({
					title: error.msg || '保存失败，请重试',
					icon: 'none'
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

/* 身份选择器 */
.identity-selector {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}
.identity-option {
	height: 100rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	border: 2rpx solid transparent;
	transition: all 0.3s;
}
.identity-option.active {
	background: #e3f2fd;
	border-color: #1976d2;
}
.option-icon {
	font-size: 40rpx;
}
.option-text {
	font-size: 26rpx;
	color: #333;
}

/* 日期选择器 */
.picker-input {
	height: 80rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	color: #333;
	display: flex;
	align-items: center;
	border: 2rpx solid transparent;
}

/* 文本域 */
.textarea {
	width: 100%;
	min-height: 180rpx;
	background: #f5f7fa;
	border-radius: 12rpx;
	padding: 20rpx;
	font-size: 28rpx;
	color: #333;
	box-sizing: border-box;
	border: 2rpx solid transparent;
}
.textarea:focus {
	background: #fff;
	border-color: #1976d2;
}
.textarea-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 10rpx;
}
.word-count {
	font-size: 22rpx;
	color: #999;
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



