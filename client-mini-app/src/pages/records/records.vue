<template>
	<view class="records-page">
		<view class="page-header">
			<text class="header-title">就诊记录</text>
			<text class="header-desc">查看您的就诊历史</text>
		</view>

		<view class="content">
			<view class="empty-state">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无就诊记录</text>
				<text class="empty-desc">您还没有就诊记录</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {};
	}
};
</script>

<style scoped>
.records-page {
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
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 120rpx 0;
}
.empty-icon {
	font-size: 120rpx;
	margin-bottom: 30rpx;
	opacity: 0.3;
}
.empty-text {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 15rpx;
}
.empty-desc {
	font-size: 24rpx;
	color: #999;
}
</style>




