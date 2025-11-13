<template>
	<view class="appointment-page">
		<view class="page-header">
			<text class="header-title">线上挂号</text>
			<text class="header-desc">选择科室和医生进行预约</text>
		</view>

		<view class="content">
			<view class="entry-card" @click="goToDepartments">
				<view class="entry-left">
					<text class="entry-icon">🏥</text>
					<view class="entry-texts">
						<text class="entry-title">按科室选择就诊</text>
						<text class="entry-sub">热门科室、全部科室一览</text>
					</view>
				</view>
				<text class="entry-arrow">›</text>
			</view>

			<view class="entry-card" @click="goToDoctors">
				<view class="entry-left">
					<text class="entry-icon">👨‍⚕️</text>
					<view class="entry-texts">
						<text class="entry-title">按医生预约挂号</text>
						<text class="entry-sub">查看医生出诊与可约时段</text>
					</view>
				</view>
				<text class="entry-arrow">›</text>
			</view>

			<view class="tip">
				<text class="tip-text">小贴士：从这里进入，底部菜单会保留；进入后可通过左上角返回。</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {};
	},
	methods: {
		goToDepartments() {
			uni.navigateTo({
				url: '/pages/department-list/department-list'
			});
		},
		goToDoctors() {
			uni.navigateTo({
				url: '/pages/doctor-list/doctor-list'
			});
		}
	}
};
</script>

<style scoped>
.appointment-page {
	min-height: 100vh;
	background: #f5f7fa;
}
.page-header {
	background: linear-gradient(135deg, #1976d2 0%, #2196f3 100%);
	padding: 40rpx 30rpx;
	color: #fff;
}
.header-title {
	font-size: 36rpx;
	font-weight: bold;
	display: block;
	margin-bottom: 10rpx;
}
.header-desc {
	font-size: 24rpx;
	opacity: 0.9;
}
.content {
	padding: 60rpx 30rpx;
}
.entry-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 28rpx 24rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.06);
	margin-bottom: 24rpx;
}
.entry-left {
	display: flex;
	align-items: center;
	gap: 20rpx;
}
.entry-icon {
	font-size: 56rpx;
}
.entry-texts {
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}
.entry-title {
	font-size: 30rpx;
	color: #333;
	font-weight: 600;
}
.entry-sub {
	font-size: 24rpx;
	color: #888;
}
.entry-arrow {
	font-size: 54rpx;
	color: #bbb;
	font-weight: 300;
}
.tip {
	margin-top: 16rpx;
}
.tip-text {
	font-size: 22rpx;
	color: #9e9e9e;
}
</style>




