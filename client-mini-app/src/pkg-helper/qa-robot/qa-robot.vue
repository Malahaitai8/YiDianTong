<template>
	<view class="qa-page">
		<view class="top-suggest" v-if="topQuestions.length">
			<view class="section-title">猜你想问</view>
			<view class="chips">
				<view
					class="chip"
					v-for="(q, idx) in topQuestions"
					:key="idx"
					@click="useTopQuestion(q)"
				>
					<text>{{ q }}</text>
				</view>
			</view>
		</view>

		<view class="chat-body">
			<scroll-view scroll-y class="chat-scroll" :scroll-into-view="lastMsgId">
				<view
					v-for="m in messages"
					:key="m.id"
					:id="'msg-' + m.id"
					class="msg-row"
					:class="m.role === 'user' ? 'from-user' : 'from-bot'"
				>
					<view class="avatar" v-if="m.role === 'bot'">问</view>
					<view class="bubble">{{ m.content }}</view>
					<view class="avatar user" v-if="m.role === 'user'">我</view>
				</view>
				<view :id="lastMsgId" />
			</scroll-view>
		</view>

		<view class="chat-input-bar">
			<input
				class="chat-input"
				v-model="query"
				placeholder="请输入问题..."
				confirm-type="send"
				@confirm="onSend"
				maxlength="200"
			/>
			<button class="send-btn" type="primary" @click="onSend" :disabled="sending">{{ sending ? '发送中' : '发送' }}</button>
		</view>
	</view>
</template>

<script>
export default {
	name: 'QaRobot',
	data() {
		return {
			query: '',
			sending: false,
			topQuestions: [
				'如何预约挂号？',
				'可以当天挂急诊吗？',
				'怎么退号与退款？',
				'医保怎么结算？',
				'化验报告在哪里看？'
			],
			faqs: [
				{
					question: '如何预约挂号？',
					keywords: ['预约', '挂号', '就诊'],
					answer: '您可在首页选择“预约挂号”，按科室与医生筛选后，选择可预约时段提交即可。支持代预约与就诊人管理。'
				},
				{
					question: '可以当天挂急诊吗？',
					keywords: ['当天', '急诊', '加号'],
					answer: '急诊支持当天挂号，建议优先到院急诊分诊台取号；在线仅开放部分急诊科室并视号源情况而定。'
				},
				{
					question: '怎么退号与退款？',
					keywords: ['退号', '退款', '取消'],
					answer: '就诊前2小时可在“我的-预约记录”中自助退号，费用将原路退回，到账以支付渠道为准。就诊后或已取号不支持退号。'
				},
				{
					question: '医保怎么结算？',
					keywords: ['医保', '报销', '结算'],
					answer: '门诊支持医保在线结算，请在个人信息中完善参保信息，就诊时出示电子凭证或绑定社保卡后按提示完成结算。'
				},
				{
					question: '化验报告在哪里看？',
					keywords: ['化验', '报告', '检验', '检查', '结果'],
					answer: '在“我的-报告查询”中可查看检验与检查报告，通常在出结果后30分钟内同步，支持PDF下载与分享。'
				}
			],
			messages: [
				{ id: 1, role: 'bot', content: '您好，我是智能助手，请描述您的问题或选择上方猜你想问～' }
			]
		};
	},
	methods: {
		onSend() {
			const text = (this.query || '').trim()
			if (!text || this.sending) return
			this.appendUser(text)
			this.query = ''
			this.answerByKeywords(text)
		},
		useTopQuestion(q) {
			this.appendUser(q)
			this.answerByKeywords(q)
		},
		appendUser(text) {
			this.messages.push({
				id: Date.now() + Math.random(),
				role: 'user',
				content: text
			})
			this.$nextTick(this.scrollToBottom)
		},
		appendBot(text) {
			this.messages.push({
				id: Date.now() + Math.random(),
				role: 'bot',
				content: text
			})
			this.$nextTick(this.scrollToBottom)
		},
		answerByKeywords(text) {
			this.sending = true
			const lower = text.toLowerCase()
			const match = this.faqs.find(f =>
				f.keywords.some(k => lower.includes(k.toLowerCase())) ||
				lower.includes(f.question.toLowerCase())
			)
			const answer = match
				? match.answer
				: '抱歉，未找到相关答案。您可以尝试更换关键词，或联系人工服务。'
			setTimeout(() => {
				this.appendBot(answer)
				this.sending = false
			}, 200)
		},
		scrollToBottom() {
			// 通过 scroll-into-view 锚定到最后一条
			// lastMsgId 为计算属性
		}
	},
	computed: {
		lastMsgId() {
			const last = this.messages[this.messages.length - 1]
			return last ? ('msg-' + last.id) : ''
		}
	}
};
</script>

<style scoped>
.qa-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f5f6f8;
	box-sizing: border-box;
	padding-bottom: env(safe-area-inset-bottom);
}

.top-suggest {
	padding: 24rpx 24rpx 0 24rpx;
	background: #f5f6f8;
}

.section-title {
	font-size: 28rpx;
	color: #666;
	margin-bottom: 16rpx;
}

.chips {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin-bottom: 8rpx;
}

.chip {
	background: #f5f7fa;
	border: 1rpx solid #e5eaf3;
	border-radius: 999rpx;
	padding: 12rpx 20rpx;
	font-size: 26rpx;
	color: #333;
}

.chat-body {
	flex: 1;
	display: flex;
	min-height: 0;
}

.chat-scroll {
	flex: 1;
	padding: 16rpx 24rpx;
}

.msg-row {
	display: flex;
	align-items: flex-end;
	margin: 16rpx 0;
}

.from-bot {
	justify-content: flex-start;
}

.from-user {
	justify-content: flex-end;
}

.avatar {
	width: 48rpx;
	height: 48rpx;
	border-radius: 50%;
	background: #e6f0ff;
	color: #2a6ae9;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 26rpx;
	margin-right: 12rpx;
}

.avatar.user {
	background: #e8f7e6;
	color: #18a058;
	margin-left: 12rpx;
	margin-right: 0;
}

.bubble {
	max-width: 70%;
	background: #fff;
	border: 1rpx solid #f0f0f0;
	border-radius: 16rpx;
	padding: 16rpx 20rpx;
	font-size: 28rpx;
	line-height: 1.6;
	white-space: pre-wrap;
}

.from-user .bubble {
	background: #2a6ae9;
	color: #fff;
	border-color: #2a6ae9;
}

.chat-input-bar {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 12rpx 16rpx calc(12rpx + env(safe-area-inset-bottom)) 16rpx;
	background: #fff;
	border-top: 1rpx solid #eee;
}

.chat-input {
	flex: 1;
	height: 72rpx;
	padding: 0 20rpx;
	background: #f7f8fa;
	border: 1rpx solid #eee;
	border-radius: 12rpx;
	font-size: 28rpx;
}

.send-btn {
	min-width: 152rpx;
	font-size: 28rpx;
}
</style>
