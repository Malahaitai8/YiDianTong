<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/* 全局样式 - 手机尺寸适配 */
	page {
		background-color: #f5f5f5;
	}
	
	/* H5模式下限制最大宽度为手机尺寸 */
	#app {
		max-width: 750rpx;
		margin: 0 auto;
		background: #fff;
	}
	
	/* 重置样式 */
	* {
		box-sizing: border-box;
	}
</style>
