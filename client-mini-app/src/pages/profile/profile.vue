<template>
	<view class="page">
		<!-- 用户头部 -->
		<view class="header">
			<view class="user-card">
				<view class="user-avatar">{{ userInitial }}</view>
				<view class="user-info">
					<text class="user-name">{{ userInfo.username || '用户' }}</text>
					<text class="user-role">{{ roleText }}</text>
				</view>
			</view>
		</view>

		<!-- 身份认证卡片 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">身份认证</text>
				<text v-if="!isVerified" class="status-badge unverified">未认证</text>
				<text v-else class="status-badge verified">已认证</text>
			</view>
			
			<view class="form">
				<!-- 真实姓名 -->
				<view class="form-item">
					<text class="label">真实姓名 <text class="required">*</text></text>
					<input class="input" v-model="profile.name" placeholder="请输入真实姓名" />
				</view>
				
				<!-- 身份类型 -->
				<view class="form-item">
					<text class="label">身份类型 <text class="required">*</text></text>
					<view class="identity-selector">
						<view 
							class="identity-item" 
							:class="{ active: profile.identityType === 'student' }" 
							@click="selectIdentity('student')"
						>
							<text class="identity-text">🎓 学生</text>
						</view>
						<view 
							class="identity-item" 
							:class="{ active: profile.identityType === 'teacher' }" 
							@click="selectIdentity('teacher')"
						>
							<text class="identity-text">👨‍🏫 教师</text>
						</view>
					</view>
				</view>
				
				<!-- 学号/工号 -->
				<view class="form-item">
					<text class="label">{{ identityLabel }} <text class="required">*</text></text>
					<input 
						class="input" 
						v-model="profile.identityNumber" 
						:placeholder="`请输入${identityLabel}`" 
					/>
				</view>
				
				<!-- 身份证号 -->
				<view class="form-item">
					<text class="label">身份证号</text>
					<input 
						class="input" 
						v-model="profile.idCardNumber" 
						maxlength="18"
						placeholder="请输入身份证号（可选）" 
					/>
					<text class="hint-secure">用于实名认证，信息将加密存储</text>
				</view>
				
				<!-- 出生日期 -->
				<view class="form-item">
					<text class="label">出生日期</text>
					<picker mode="date" :value="profile.birthday" @change="onBirthdayChange">
						<view class="picker-view">
							<text v-if="profile.birthday" class="picker-text">{{ profile.birthday }}</text>
							<text v-else class="picker-placeholder">请选择出生日期</text>
						</view>
					</picker>
				</view>
				
				<!-- 联系方式 -->
				<view class="form-item">
					<text class="label">联系方式</text>
					<input 
						class="input" 
						v-model="profile.phoneNumber" 
						type="number"
						maxlength="11"
						placeholder="请输入手机号" 
					/>
				</view>
				
				<!-- 既往病史 -->
				<view class="form-item">
					<text class="label">既往病史 <text class="hint-text">（敏感信息，将加密存储）</text></text>
					<textarea 
						class="textarea" 
						v-model="profile.medicalHistory" 
						placeholder="请填写既往病史、过敏史等（选填）"
						maxlength="500"
					/>
					<text class="char-count">{{ profile.medicalHistory ? profile.medicalHistory.length : 0 }}/500</text>
				</view>
				
				<button class="btn-save" :loading="loading" @click="saveProfile">保存身份信息</button>
			</view>
		</view>

		<!-- 功能区域 -->
		<view class="section">
			<view class="section-title">快捷功能</view>
			<view class="func-grid">
				<view class="func-item" @click="goToAppointment">
					<text class="func-icon">📅</text>
					<text class="func-text">预约挂号</text>
				</view>
				<view class="func-item" @click="goToMyAppointments">
					<text class="func-icon">📋</text>
					<text class="func-text">我的预约</text>
				</view>
				<view class="func-item" @click="goToSubstitute">
					<text class="func-icon">⏰</text>
					<text class="func-text">候补队列</text>
				</view>
				<view class="func-item" @click="goToHistory">
					<text class="func-icon">📖</text>
					<text class="func-text">就诊记录</text>
				</view>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-card">
			<view class="tips-header">
				<text class="tips-icon">💡</text>
				<text class="tips-title">温馨提示</text>
			</view>
			<text class="tips-item">• 完成身份认证后可享受医保报销</text>
			<text class="tips-item">• 学生报销比例95%，教师90%</text>
			<text class="tips-item">• 就诊前会收到短信/微信提醒</text>
			<text class="tips-item">• 敏感信息将加密存储，保障隐私安全</text>
		</view>

		<button class="btn-logout" @click="handleLogout">退出登录</button>
	</view>
</template>

<script>
export default {
	data() {
		return {
			loading: false,
			isVerified: false,
			profile: {
				name: '',
				identityType: 'student',
				identityNumber: '',
				idCardNumber: '',
				birthday: '',
				phoneNumber: '',
				medicalHistory: ''
			}
		};
	},
	computed: {
		userInfo() {
			return this.$store.state.user.userInfo || {};
		},
		roleText() {
			const role = this.userInfo.role;
			const roleMap = {
				patient: '患者',
				doctor: '医生',
				admin: '管理员'
			};
			return roleMap[role] || '用户';
		},
		userInitial() {
			const username = this.userInfo.username || '用户';
			return username.substring(0, 1).toUpperCase();
		},
		identityLabel() {
			return this.profile.identityType === 'student' ? '学号' : '工号';
		}
	},
	methods: {
		selectIdentity(type) {
			this.profile.identityType = type;
		},
		onBirthdayChange(e) {
			this.profile.birthday = e.detail.value;
		},
		async saveProfile() {
			// 必填校验
			if (!this.profile.name) {
				uni.showToast({ title: '请输入真实姓名', icon: 'none' });
				return;
			}
			if (!this.profile.identityNumber) {
				uni.showToast({ title: `请输入${this.identityLabel}`, icon: 'none' });
				return;
			}
			
			// 可选字段校验
			if (this.profile.phoneNumber && !/^1[3-9]\d{9}$/.test(this.profile.phoneNumber)) {
				uni.showToast({ title: '手机号格式不正确', icon: 'none' });
				return;
			}
			if (this.profile.idCardNumber && !/^\d{17}[\dXx]$/.test(this.profile.idCardNumber)) {
				uni.showToast({ title: '身份证号格式不正确', icon: 'none' });
				return;
			}

			this.loading = true;
			try {
				// TODO: 调用后端API保存患者信息
				// 注意：需要后端提供 /patient/me 接口或通过userId查询patientId
				
				// 模拟保存成功
				uni.showToast({ title: '身份信息已保存', icon: 'success' });
				this.isVerified = true;
				
				// 实际开发中应该调用：
				// await updatePatientInfo(patientId, profileData);
				
			} catch (e) {
				console.error('保存失败:', e);
				uni.showToast({ title: '保存失败，请重试', icon: 'none' });
			} finally {
				this.loading = false;
			}
		},
		goToAppointment() {
			if (!this.isVerified) {
				uni.showModal({
					title: '提示',
					content: '请先完成身份认证',
					showCancel: false
				});
				return;
			}
			uni.showToast({ title: '预约挂号功能开发中', icon: 'none' });
		},
		goToMyAppointments() {
			uni.showToast({ title: '我的预约功能开发中', icon: 'none' });
		},
		goToSubstitute() {
			uni.navigateTo({ url: '/pkg-user/my-substitute/my-substitute' });
		},
		goToHistory() {
			uni.showToast({ title: '就诊记录功能开发中', icon: 'none' });
		},
		handleLogout() {
			uni.showModal({
				title: '提示',
				content: '确定要退出登录吗？',
				success: (res) => {
					if (res.confirm) {
						this.$store.dispatch('user/logout');
						uni.showToast({ title: '已退出', icon: 'success' });
						setTimeout(() => {
							uni.reLaunch({ url: '/pages/index/index' });
						}, 800);
					}
				}
			});
		}
	}
};
</script>

<style scoped>
.page {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 40rpx;
}
.header {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	padding: 50rpx 30rpx 40rpx;
}
.user-card {
	display: flex;
	align-items: center;
	gap: 25rpx;
}
.user-avatar {
	width: 100rpx;
	height: 100rpx;
	background: rgba(255, 255, 255, 0.3);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 40rpx;
	color: #fff;
	font-weight: bold;
	border: 3rpx solid rgba(255, 255, 255, 0.5);
}
.user-info {
	flex: 1;
}
.user-name {
	display: block;
	font-size: 32rpx;
	color: #fff;
	font-weight: bold;
	margin-bottom: 8rpx;
}
.user-role {
	display: inline-block;
	font-size: 22rpx;
	color: #fff;
	background: rgba(255, 255, 255, 0.25);
	padding: 4rpx 16rpx;
	border-radius: 20rpx;
}
.section {
	background: #fff;
	margin: 30rpx;
	border-radius: 20rpx;
	padding: 35rpx 30rpx;
}
.section-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 30rpx;
}
.section-title {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
	padding-left: 15rpx;
	border-left: 4rpx solid #1976d2;
}
.status-badge {
	font-size: 22rpx;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
}
.unverified {
	color: #ff9800;
	background: #fff3e0;
}
.verified {
	color: #4caf50;
	background: #e8f5e9;
}
.form {
	
}
.form-item {
	margin-bottom: 30rpx;
}
.label {
	display: block;
	font-size: 28rpx;
	color: #333;
	margin-bottom: 15rpx;
	font-weight: 500;
}
.required {
	color: #f44336;
}
.hint-text {
	font-size: 24rpx;
	color: #999;
	font-weight: normal;
}
.identity-selector {
	display: flex;
	gap: 20rpx;
}
.identity-item {
	flex: 1;
	height: 88rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 2rpx solid transparent;
}
.identity-item.active {
	background: #e3f2fd;
	border-color: #1e88e5;
}
.identity-text {
	font-size: 26rpx;
	color: #666;
}
.identity-item.active .identity-text {
	color: #1e88e5;
	font-weight: 600;
}
.input {
	width: 100%;
	height: 88rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	padding: 0 30rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}
.picker-view {
	height: 88rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	padding: 0 30rpx;
	display: flex;
	align-items: center;
}
.picker-text {
	font-size: 28rpx;
	color: #333;
}
.picker-placeholder {
	font-size: 28rpx;
	color: #999;
}
.hint-secure {
	display: block;
	font-size: 24rpx;
	color: #f57c00;
	margin-top: 10rpx;
}
.textarea {
	width: 100%;
	min-height: 200rpx;
	background: #f1f7ff;
	border-radius: 10rpx;
	padding: 20rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}
.char-count {
	display: block;
	text-align: right;
	font-size: 24rpx;
	color: #999;
	margin-top: 10rpx;
}
.btn-save {
	width: 100%;
	height: 90rpx;
	background: #1e88e5;
	color: #fff;
	border-radius: 10rpx;
	font-size: 30rpx;
	font-weight: 600;
	border: none;
	margin-top: 20rpx;
}
.btn-save::after {
	border: none;
}
.func-grid {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 20rpx;
}
.func-item {
	height: 120rpx;
	background: #f1f7ff;
	border-radius: 15rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
}
.func-icon {
	font-size: 44rpx;
}
.func-text {
	font-size: 24rpx;
	color: #333;
}
.tips-card {
	background: #fff;
	margin: 30rpx;
	border-radius: 20rpx;
	padding: 30rpx;
	border: 2rpx solid #e3f2fd;
}
.tips-header {
	display: flex;
	align-items: center;
	gap: 10rpx;
	margin-bottom: 20rpx;
}
.tips-icon {
	font-size: 28rpx;
}
.tips-title {
	font-size: 28rpx;
	color: #1976d2;
	font-weight: 600;
}
.tips-item {
	display: block;
	font-size: 24rpx;
	color: #666;
	line-height: 1.8;
	margin-bottom: 8rpx;
}
.btn-logout {
	width: calc(100% - 60rpx);
	height: 88rpx;
	background: #fff;
	color: #f44336;
	border: 2rpx solid #f44336;
	border-radius: 15rpx;
	font-size: 28rpx;
	margin: 0 30rpx;
}
.btn-logout::after {
	border: none;
}
</style>
