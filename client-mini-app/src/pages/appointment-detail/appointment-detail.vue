<template>
	<view class="appointment-detail-page">
		<!-- 状态卡片 -->
		<view
			class="status-card"
			:class="{
				'status-pending': appointmentDetail.status === 'PENDING' || appointmentDetail.status === 'pending',
				'status-confirmed': appointmentDetail.status === 'CONFIRMED' || appointmentDetail.status === 'confirmed' || appointmentDetail.status === 'SCHEDULED' || appointmentDetail.status === 'scheduled',
				'status-rescheduled': appointmentDetail.status === 'RESCHEDULED' || appointmentDetail.status === 'RESCHEDULE',
				'status-completed': appointmentDetail.status === 'COMPLETED' || appointmentDetail.status === 'completed',
				'status-cancelled': appointmentDetail.status === 'CANCELLED' || appointmentDetail.status === 'cancelled'
			}"
		>
			<view class="status-icon">{{ getStatusIcon(appointmentDetail.status) }}</view>
			<text class="status-text">{{ getDisplayStatusTextDetail() }}</text>
			<text v-if="appointmentDetail.rescheduleWindowExpires && (new Date(appointmentDetail.rescheduleWindowExpires)).getTime() > nowTs && (String(appointmentDetail.sourceType || '').toUpperCase() !== 'RESELECTED')" style="margin-top:8rpx; font-size:24rpx; color:#fff; opacity:0.95;">
				重新选择剩余：{{ getReselectRemainingTextDetail() }}
			</text>
		</view>

		<!-- 就诊信息卡片 -->
		<view class="info-card">
			<view class="card-title">
				<text class="title-icon">🏥</text>
				<text class="title-text">就诊信息</text>
			</view>
			
			<view class="info-row">
				<text class="info-label">就诊医生</text>
				<text class="info-value">{{ displayValue(appointmentDetail.doctorName) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">医生职称</text>
				<text class="info-value">{{ displayValue(appointmentDetail.doctorTitle) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">科室/门诊</text>
				<text class="info-value">{{ displayValue(appointmentDetail.clinicName) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊日期</text>
				<text class="info-value">{{ displayValue(appointmentDetail.appointmentDate) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">就诊时间</text>
				<text class="info-value">{{ displayValue(appointmentDetail.appointmentTime) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">时间段</text>
				<text class="info-value">{{ displayValue(appointmentDetail.timeSlotDisplay) }}</text>
			</view>
		</view>

		<!-- 费用信息卡片 -->
		<view class="info-card">
			<view class="card-title">
				<text class="title-icon">💰</text>
				<text class="title-text">费用信息</text>
			</view>
			
			<view class="info-row">
				<text class="info-label">挂号费（原价）</text>
				<text class="info-value original-price">¥{{ formatMoney(appointmentDetail.originalFee) }}</text>
			</view>
			<view class="info-row" v-if="appointmentDetail.reimbursementRate">
				<text class="info-label">报销比例</text>
				<text class="info-value">{{ formatPercent(appointmentDetail.reimbursementRate) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">实付金额</text>
				<text class="info-value actual-price">¥{{ formatMoney(appointmentDetail.actualFee) }}</text>
			</view>
		</view>

		<!-- 预约信息卡片 -->
		<view class="info-card">
			<view class="card-title">
				<text class="title-icon">📋</text>
				<text class="title-text">预约信息</text>
			</view>
			
			<view class="info-row">
				<text class="info-label">预约编号</text>
				<text class="info-value">{{ displayValue(appointmentDetail.appointmentNo) }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">预约时间</text>
				<text class="info-value">{{ displayValue(appointmentDetail.createdAt) }}</text>
			</view>
			<view class="info-row" v-if="appointmentDetail.reminderTime">
				<text class="info-label">提醒时间</text>
				<text class="info-value">{{ displayValue(appointmentDetail.reminderTime) }}</text>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view class="tips-card">
			<view class="tips-title">💡 温馨提示</view>
			<view class="tips-item">• 请于就诊前2小时内勿取消，以免影响信用</view>
			<view class="tips-item">• 到诊时请携带身份证件和预约凭证</view>
			<view class="tips-item">• 如需改约，请在就诊前24小时操作</view>
			<view class="tips-item">• 退号后费用将在3-5个工作日内原路退回</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<!-- 待就诊/已确认状态：仅显示退号按钮；已重新安排时显示重新选择 -->
			<template v-if="canCancel">
				<button class="action-btn cancel-btn" @click="goToCancel">
					<text class="btn-icon">❌</text>
					<text class="btn-text">退号</text>
				</button>
			</template>
			<template v-if="canReselect()">
				<button class="action-btn waitlist-btn" @click="showReselectOptions">
					<text class="btn-icon">🔁</text>
					<text class="btn-text">重新选择</text>
				</button>
			</template>
			<!-- 其他状态：只显示返回按钮 -->
			<button v-else class="action-btn back-btn" @click="goBack">
				<text class="btn-text">返回</text>
			</button>
		</view>
		<!-- 自定义重新选择模态框（放在根 view 内） -->
		<view v-if="reselectModalVisible" class="custom-modal-wrap">
			<view class="custom-modal-overlay" @click="closeReselectModal"></view>
			<view class="custom-modal">
				<view class="modal-header">
					<text class="modal-title">选择新的就诊时段</text>
					<button class="modal-close" @click="closeReselectModal">关闭</button>
					<!-- 调试按钮：确认点击是否能触发（仅调试用，可删除） -->
					<button class="modal-test" @click.stop="onConfirmClick" style="margin-left:12rpx;">测试确认</button>
				</view>
				<!-- 筛选与排序控件 -->
				<view class="modal-controls">
					<view class="filter-group">
						<text class="filter-label">号别：</text>
						<button :class="{'chip':true, active: reselectFilterSlotType==''}" @click="reselectFilterSlotType = ''">全部</button>
						<button :class="{'chip':true, active: reselectFilterSlotType=='NORMAL'}" @click="reselectFilterSlotType = 'NORMAL'">普通号</button>
						<button :class="{'chip':true, active: reselectFilterSlotType=='EXPERT'}" @click="reselectFilterSlotType = 'EXPERT'">专家号</button>
						<button :class="{'chip':true, active: reselectFilterSlotType=='VIP'}" @click="reselectFilterSlotType = 'VIP'">特需号</button>
					</view>
					<view class="sort-group">
						<text class="filter-label">排序：</text>
						<button :class="{'chip':true, active: reselectSortBy=='date'}" @click="reselectSortBy='date'">按日期</button>
						<button :class="{'chip':true, active: reselectSortBy=='slotType'}" @click="reselectSortBy='slotType'">按号别</button>
						<button :class="{'chip':true, active: reselectSortBy=='available'}" @click="reselectSortBy='available'">按剩余</button>
					</view>
				</view>
				<view class="modal-body">
					<view class="option-card" v-for="(opt, idx) in reselectVisibleOptions" :key="opt.scheduleId" :class="{selected: String(opt.scheduleId) === selectedReselectScheduleId}" @click.stop="selectReselectOption(idx)">
						<view class="card-left">
							<text class="doctor-name">{{ opt.doctorName }}<text v-if="opt.doctorTitle">（{{ opt.doctorTitle }}）</text></text>
							<text class="department">{{ opt.departmentName }}</text>
							<text class="date-time">{{ (new Date(opt.date)).getFullYear() }}年{{ String(new Date(opt.date).getMonth()+1).padStart(2,'0') }}月{{ String(new Date(opt.date).getDate()).padStart(2,'0') }}日 {{ getTimeSlotDisplay(opt.timeSlot) }}</text>
							<text class="slot-meta">{{ getSlotTypeDisplay(opt.slotType) }} {{ opt.fee ? ('¥' + opt.fee) : '' }} {{ opt.availableSlots ? ('剩余' + opt.availableSlots + '个') : '' }}</text>
						</view>
					<view class="card-right">
						<button class="select-btn" @click.stop="selectReselectOption(idx)" @tap.stop="selectReselectOption(idx)">选此时段</button>
						<text class="checkmark" v-if="selectedReselectScheduleId === String(opt.scheduleId)">✔</text>
					</view>
					</view>
				</view>
				<view class="modal-footer">
					<view class="confirm-btn" :class="{disabled: !selectedReselectScheduleId}" @click.stop="onConfirmClick">确认重新选择</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getMyAppointments, cancelAppointment } from '@/api/appointment.js'
import request from '@/utils/request.js'

export default {
	name: 'AppointmentDetail',
	data() {
		return {
			appointmentId: null,
			// 挂号详情数据
			appointmentDetail: {
				id: null,
				appointmentNo: '',
				doctorId: null,
				doctorName: '',
				doctorTitle: '',
				clinicName: '',
				appointmentDate: '',
				appointmentTime: '',
				timeSlot: '',
				timeSlotDisplay: '',
				status: 'pending', // Changed from 'PENDING' to 'pending' to match backend
				originalFee: '0.00',
				actualFee: '0.00',
				reimbursementRate: '',
				createdAt: '',
				reminderTime: ''
				,
				// nowTs and reselectInterval moved to root level for reactivity
			}
			,
			// 顶层时间与模态相关状态
			nowTs: Date.now(),
			reselectInterval: null,
			reselectModalVisible: false,
			reselectOptions: [],
			// 已选择的选项（对象），以及在可见列表中的索引
			selectedReselectIndex: null,
			selectedReselectScheduleId: null,
			selectedReselectOption: null,
			// 筛选/排序
			reselectFilterSlotType: '', // '', 'NORMAL','EXPERT','VIP'
			reselectSortBy: 'date' // 'date'|'slotType'|'available'
		};
	},
	computed: {
		// 是否可以取消（待就诊或已确认状态）
		canCancel() {
			const status = this.appointmentDetail.status;
			return status === 'PENDING' || status === 'pending' || 
				   status === 'CONFIRMED' || status === 'confirmed' ||
				   status === 'SCHEDULED' || status === 'scheduled' ||
				   status === 'RESCHEDULED' || status === 'RESCHEDULE' || status === 'RESELECTED';
		}
		,
		// 可见的重新选择选项（应用筛选与排序）
		reselectVisibleOptions() {
			let list = Array.isArray(this.reselectOptions) ? this.reselectOptions.slice() : [];
			// 过滤号别
			if (this.reselectFilterSlotType) {
				list = list.filter(opt => (opt.slotType || '').toString().toUpperCase() === this.reselectFilterSlotType);
			}
			// 排序
			if (this.reselectSortBy === 'date') {
				list.sort((a,b) => new Date(a.date) - new Date(b.date));
			} else if (this.reselectSortBy === 'slotType') {
				list.sort((a,b) => ((a.slotType||'').localeCompare(b.slotType||'')));
			} else if (this.reselectSortBy === 'available') {
				list.sort((a,b) => (Number(b.availableSlots||0) - Number(a.availableSlots||0)));
			}
			return list;
		}
	},
	onLoad(options) {
		console.log('挂号详情页面参数:', options);
		
		if (options.id) {
			this.appointmentId = parseInt(options.id);
			// 调用API获取挂号详情
			this.loadAppointmentDetail();
		}
		
		// 如果有传入的数据，可以合并
		if (options.status) {
			this.appointmentDetail.status = options.status;
		}
	},
	onShow() {
		// 启动倒计时更新
		if (!this.reselectInterval) {
			this.reselectInterval = setInterval(() => {
				this.nowTs = Date.now();
			}, 60 * 1000);
		}
	},
	onUnload() {
		if (this.reselectInterval) {
			clearInterval(this.reselectInterval);
			this.reselectInterval = null;
		}
	},
	methods: {
		displayValue(value, fallback = '--') {
			return value === undefined || value === null || value === '' ? fallback : value;
		},
		// 前端内部使用：timeSlot 序号（上午=1, 下午=2, 晚上=3）
		_timeSlotOrder(slot) {
			if (!slot) return 0;
			const s = (slot || '').toString().toLowerCase();
			if (s === 'morning' || s === '上午') return 1;
			if (s === 'afternoon' || s === '下午') return 2;
			if (s === 'evening' || s === '晚上') return 3;
			return 0;
		},
		_timeSlotOrderFromNow() {
			const d = new Date();
			const h = d.getHours();
			if (h < 12) return 1;
			if (h < 18) return 2;
			return 3;
		},
		// 时间段中文显示
		getTimeSlotDisplay(timeSlot) {
			if (!timeSlot) return '';
			const map = {
				'morning': '上午',
				'afternoon': '下午',
				'evening': '晚上',
				'上午': '上午',
				'下午': '下午',
				'晚上': '晚上'
			};
			return map[(timeSlot || '').toString().toLowerCase()] || timeSlot;
		},
		formatMoney(value, fallback = '--') {
			const num = Number(value);
			if (!Number.isFinite(num)) {
				return fallback;
			}
			return num.toFixed(2);
		},
		formatPercent(value, fallback = '--') {
			if (value === undefined || value === null || value === '') {
				return fallback;
			}
			const str = String(value).trim();
			if (str.endsWith('%')) {
				return str;
			}
			const num = Number(str);
			return Number.isFinite(num) ? `${num}%` : str || fallback;
		},
		// 格式化日期 YYYY-MM-DD
		formatDate(dateStr) {
			if (!dateStr) return '--';
			try {
				const date = new Date(dateStr);
				if (isNaN(date.getTime())) {
					// 如果是字符串格式，尝试直接提取日期部分
					if (typeof dateStr === 'string' && dateStr.length >= 10) {
						return dateStr.substring(0, 10);
					}
					return '--';
				}
				const year = date.getFullYear();
				const month = String(date.getMonth() + 1).padStart(2, '0');
				const day = String(date.getDate()).padStart(2, '0');
				return `${year}-${month}-${day}`;
			} catch (e) {
				console.error('日期格式化失败:', dateStr, e);
				return '--';
			}
		},
		// 格式化预约时间（根据日期和时间段）
		formatAppointmentTime(scheduleDate, timeSlot) {
			if (!scheduleDate) return '--';
			try {
				const date = new Date(scheduleDate);
				if (isNaN(date.getTime())) return '--';
				
				// 根据时间段设置默认时间
				const timeSlotMap = {
					'morning': '08:00-12:00',
					'afternoon': '14:00-18:00',
					'evening': '18:00-22:00'
				};
				
				const timeRange = timeSlotMap[timeSlot?.toLowerCase()] || '--';
				return timeRange;
			} catch (e) {
				console.error('预约时间格式化失败:', scheduleDate, timeSlot, e);
				return '--';
			}
		},
		// 格式化日期时间 YYYY-MM-DD HH:mm:ss
		formatDateTime(dateStr) {
			if (!dateStr) return '--';
			try {
				const date = new Date(dateStr);
				if (isNaN(date.getTime())) return '--';
				
				const year = date.getFullYear();
				const month = String(date.getMonth() + 1).padStart(2, '0');
				const day = String(date.getDate()).padStart(2, '0');
				const hours = String(date.getHours()).padStart(2, '0');
				const minutes = String(date.getMinutes()).padStart(2, '0');
				const seconds = String(date.getSeconds()).padStart(2, '0');
				
				return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
			} catch (e) {
				console.error('日期时间格式化失败:', dateStr, e);
				return '--';
			}
		},
		// 生成预约编号
		formatAppointmentNo(id) {
			if (!id) return '--';
			// 格式：APT + 8位数字（不足补0）
			return 'APT' + String(id).padStart(8, '0');
		},
		// 加载挂号详情
		async loadAppointmentDetail() {
			try {
				// 获取我的所有预约
				const data = await getMyAppointments();
				const appointments = Array.isArray(data) ? data : (data.list || []);
				
				// 查找当前预约
				const appointment = appointments.find(item => item.id === this.appointmentId);
				if (appointment) {
					// 处理时间段显示
					const timeSlotMap = {
						'morning': '上午',
						'afternoon': '下午',
						'evening': '晚上'
					};
					
					// [修复] 正确映射字段
					this.appointmentDetail = {
						...this.appointmentDetail,
						id: appointment.id,
						appointmentNo: this.formatAppointmentNo(appointment.id), // 生成预约编号
						doctorId: appointment.doctorId,
						doctorName: appointment.doctorName || '--',
						doctorTitle: appointment.doctorTitle || '--',
						clinicName: appointment.clinicName || appointment.departmentName || '--',
						// [修复] 就诊日期应该使用scheduleDate，而不是appointmentTime
						appointmentDate: this.formatDate(appointment.scheduleDate || appointment.appointmentTime),
						// [修复] 就诊时间根据scheduleDate和timeSlot计算
						appointmentTime: this.formatAppointmentTime(appointment.scheduleDate, appointment.timeSlot),
						timeSlot: appointment.timeSlot,
						slotType: appointment.slotType || appointment.slot_type || '',
						timeSlotDisplay: timeSlotMap[appointment.timeSlot?.toLowerCase()] || appointment.timeSlot || '--',
						status: appointment.status,
						originalFee: appointment.fee || 0,
						actualFee: appointment.actualFee || 0,
						// [修复] 预约时间使用createdAt
						createdAt: this.formatDateTime(appointment.createdAt),
						// 支持后端返回的自动分配与重新选择窗口
						rescheduleWindowExpires: appointment.rescheduleWindowExpires || appointment.reschedule_window_expires || null,
						autoAssigned: appointment.autoAssigned || appointment.auto_assigned || false,
						sourceType: appointment.sourceType
					};
					
					console.log('处理后的预约详情:', this.appointmentDetail);
				} else {
					uni.showToast({ title: '未找到预约信息', icon: 'none' });
				}
			} catch (e) {
				console.error('加载挂号详情失败:', e);
				uni.showToast({ title: e.msg || '加载失败', icon: 'none' });
			}
		},

		// 获取状态文本
		getStatusText(status) {
			// 返回“基础状态”文本（不包含“已重新安排/已重新选择”后缀）
			const statusMap = {
				'PENDING': '待就诊',
				'pending': '待就诊',
				'CONFIRMED': '已确认',
				'confirmed': '已确认',
				'SCHEDULED': '已确认',
				'scheduled': '已确认',
				// 对于重新安排/重新选择，我们仍然把基础状态显示为“待就诊”，
				// 复合提示由 getDisplayStatusTextDetail 追加后缀
				'RESCHEDULED': '待就诊',
				'RESCHEDULE': '待就诊',
				'RESELECTED': '待就诊',
				'COMPLETED': '已完成',
				'completed': '已完成',
				'CANCELLED': '已取消',
				'cancelled': '已取消'
			};
			return statusMap[status] || '未知状态';
		},

		// 获取状态图标
		getStatusIcon(status) {
			const iconMap = {
				'PENDING': '⏳',
				'pending': '⏳',
				'CONFIRMED': '✅',
				'confirmed': '✅',
				'SCHEDULED': '✅',
				'scheduled': '✅',
				'RESCHEDULED': '🔁',
				'RE SCHEDULED': '🔁',
				'RE SCHEDULE': '🔁',
				'COMPLETED': '✔️',
				'completed': '✔️',
				'CANCELLED': '❌',
				'cancelled': '❌'
			};
			return iconMap[status] || '❓';
		},

		// 获取状态样式类
		getStatusClass(status) {
			const classMap = {
				'PENDING': 'status-pending',
				'pending': 'status-pending',
				'CONFIRMED': 'status-confirmed',
				'confirmed': 'status-confirmed',
				'SCHEDULED': 'status-confirmed',
				'scheduled': 'status-confirmed',
				'RESCHEDULED': 'status-rescheduled',
				'RE SCHEDULED': 'status-rescheduled',
				'RE SCHEDULE': 'status-rescheduled',
				'COMPLETED': 'status-completed',
				'completed': 'status-completed',
				'CANCELLED': 'status-cancelled',
				'cancelled': 'status-cancelled'
			};
			return classMap[status] || '';
		},
		// 详情页专用：复合显示状态（例如“待就诊（已重新安排）”）
		getDisplayStatusTextDetail() {
			try {
				const s = this.appointmentDetail.status || '';
				const base = this.getStatusText(s);
				const src = this.appointmentDetail.sourceType || '';
				const srcUpper = String(src).toUpperCase();
				// 患者主动重新选择
				if (srcUpper === 'RESELECTED') {
					return `${base}（已重新选择）`;
				}
				// 系统自动重新安排或来源标记为 RESCHEDULED
				if (srcUpper === 'RESCHEDULED' || srcUpper === 'RESCHEDULE' || this.appointmentDetail.autoAssigned) {
					return `${base}（已重新安排）`;
				}
				return base;
			} catch (e) {
				return this.getStatusText(this.appointmentDetail.status);
			}
		},
		// 计算详情页重新选择剩余时间
		getReselectRemainingTextDetail() {
			try {
				const expires = this.appointmentDetail.rescheduleWindowExpires || this.appointmentDetail.reschedule_window_expires;
				if (!expires) return '';
				const expTs = (new Date(expires)).getTime();
				const diff = expTs - (this.nowTs || Date.now());
				if (diff <= 0) return '已过期';
				const hours = Math.floor(diff / (1000 * 60 * 60));
				const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
				if (hours > 0) return `${hours}小时${minutes}分`;
				return `${minutes}分`;
			} catch (e) {
				return '';
			}
		},

		// 跳转到退号页面
		goToCancel() {
			uni.navigateTo({
				url: `/pages/cancel-appointment/cancel-appointment?id=${this.appointmentId || this.appointmentDetail.id}`
			});
		},

		// 跳转到候补页面
		goToWaitlist() {
			uni.navigateTo({
				url: `/pages/waitlist/waitlist?scheduleId=${this.appointmentDetail.scheduleId}&doctorId=${this.appointmentDetail.doctorId}&scheduleDate=${this.appointmentDetail.appointmentDate}&timeSlot=${this.appointmentDetail.timeSlot}`
			});
		},
		// 判断是否可重新选择：仅当来源为系统重新安排 RESCHEDULED（且在窗口期内）允许一次性重新选择
		canReselect() {
			try {
				const src = (this.appointmentDetail.sourceType || '').toString().toUpperCase();
				if (src !== 'RESCHEDULED') return false;
				const expires = this.appointmentDetail.rescheduleWindowExpires;
				if (!expires) return true; // 未提供截止，默认允许
				const expTs = (new Date(expires)).getTime();
				return Date.now() <= expTs;
			} catch (e) {
				return false;
			}
		},
		// 获取号别类型的中文显示
		getSlotTypeDisplay(slotType) {
			if (!slotType) return '';
			const type = slotType.toUpperCase();
			switch (type) {
				case 'NORMAL':
					return '普通号';
				case 'EXPERT':
					return '专家号';
				case 'VIP':
					return '特需号';
				default:
					return slotType;
			}
		},
		// 显示重新选择选项
		async showReselectOptions() {
			if (!this.appointmentId) return;
			try {
				uni.showLoading({ title: '加载可选时段...' });
				const resp = await request({ url: `/appointment/${this.appointmentId}/reselect-options`, method: 'GET', silent: true });
				uni.hideLoading();
				const list = Array.isArray(resp) ? resp : (resp && resp.list ? resp.list : []);
				if (!list || list.length === 0) {
					uni.showModal({ title: '无可用选项', content: '当前没有可重新选择的时段，是否接受当前安排？', confirmText: '接受安排', cancelText: '稍后再选' });
					return;
				}
				const optionList = list.map(opt => {
					// 日期格式化为中文：2025年12月29日
					let dateStr = '';
					if (opt.date) {
						const d = new Date(opt.date);
						const y = d.getFullYear();
						const m = String(d.getMonth() + 1).padStart(2, '0');
						const day = String(d.getDate()).padStart(2, '0');
						dateStr = `${y}年${m}月${day}日`;
					}
					const timeSlot = this.getTimeSlotDisplay(opt.timeSlot) || '';
					const doctorTitle = opt.doctorTitle || '';
					const department = opt.departmentName || '';
					const slotType = this.getSlotTypeDisplay(opt.slotType);
					const fee = opt.fee ? `¥${opt.fee}` : '';
					const available = opt.availableSlots ? `剩余${opt.availableSlots}个` : '';

					// 构建中文且美观的显示文本
					let displayText = `${opt.doctorName}`;
					if (doctorTitle) displayText += `（${doctorTitle}）`;
					if (department) displayText += ` - ${department}`;
					displayText += `\n${dateStr} ${timeSlot}`;
					if (slotType || fee || available) {
						const details = [slotType, fee, available].filter(Boolean).join('  ');
						displayText += `\n${details}`;
					}

					// 决定前端是否可选：需与当前 appointment 的 slotType 相同，且为未来时段，且有剩余
					let selectable = true;
					let reason = '';
					try {
						const origSlotType = (this.appointmentDetail.slotType || '').toString().trim().toUpperCase();
						const optSlotType = (opt.slotType || '').toString().trim().toUpperCase();
						if (origSlotType && optSlotType && origSlotType !== optSlotType) {
							selectable = false;
							reason = '号别不符';
						}
						// 非未来日期或过去时段不可选
						const now = new Date();
						const optDate = opt.date ? new Date(opt.date) : null;
						if (optDate) {
							// 如果 optDate before today -> 不选
							const startOfOptDay = new Date(optDate.getFullYear(), optDate.getMonth(), optDate.getDate());
							const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
							if (startOfOptDay < startOfToday) {
								selectable = false;
								reason = '过去日期';
							} else if (startOfOptDay.getTime() === startOfToday.getTime()) {
								// same day: check time slot already passed
								const optOrder = this._timeSlotOrder(opt.timeSlot);
								const nowOrder = this._timeSlotOrderFromNow();
								if (optOrder > 0 && optOrder < nowOrder) {
									selectable = false;
									reason = '时段已过';
								}
							}
						}
						// 剩余数为0也不可选（防御）
						if (!opt.availableSlots || Number(opt.availableSlots) <= 0) {
							selectable = false;
							reason = '无可用号源';
						}
					} catch (e) {
						// ignore and leave selectable true
					}

					return {
						_text: displayText,
						...opt,
						selectable: selectable,
						unselectReason: reason
					};
				});

				// 只保留可选项进行显示（前端不展示不可选项）
				this.reselectOptions = optionList.filter(o => o.selectable);
				this.selectedReselectIndex = null;
				this.reselectModalVisible = true;
			} catch (e) {
				uni.hideLoading();
				console.error('加载重新选择选项失败', e);
				uni.showToast({ title: '加载失败', icon: 'none' });
			}
		},

		// 关闭重新选择模态框
		closeReselectModal() {
			this.reselectModalVisible = false;
			this.reselectOptions = [];
			this.selectedReselectIndex = null;
			this.selectedReselectOption = null;
		},

		// 选择某个选项（高亮）
		selectReselectOption(index) {
			console.debug('selectReselectOption called, index=', index, 'visibleOptionsCount=', (this.reselectVisibleOptions || []).length);
			this.selectedReselectIndex = index;
			this.selectedReselectOption = this.reselectVisibleOptions[index] || null;
			this.selectedReselectScheduleId = this.selectedReselectOption ? String(this.selectedReselectOption.scheduleId) : null;
			console.debug('selectedReselectScheduleId=', this.selectedReselectScheduleId, 'selectedReselectOption=', this.selectedReselectOption);
		},

		// 通过自定义模态确认重新选择
		async confirmReselectFromModal(index) {
			// resolve selected option by scheduleId if index not provided
			let selected = null;
			if (index != null) {
				selected = this.reselectVisibleOptions[index];
			} else if (this.selectedReselectScheduleId) {
				// scheduleId in options may be number, selectedReselectScheduleId may be string -> compare as strings
				selected = (this.reselectVisibleOptions || []).find(o => String(o.scheduleId) === String(this.selectedReselectScheduleId));
			} else {
				selected = this.selectedReselectOption;
			}
			if (!selected) {
				uni.showToast({ title: '请先选择时段', icon: 'none' });
				return;
			}
			console.debug('confirmReselectFromModal selected=', selected);
			try {
				uni.showLoading({ title: '正在重新选择...' });
				console.debug('about to send reselect request', { appointmentId: this.appointmentId, newScheduleId: selected.scheduleId });
				const resp = await request({
					url: `/appointment/${this.appointmentId}/reselect`,
					method: 'POST',
					data: { newScheduleId: selected.scheduleId }
				});
				console.debug('reselect request response:', resp);
				uni.hideLoading();
				uni.showToast({ title: '重新选择成功', icon: 'success' });
				this.closeReselectModal();
				this.loadAppointmentDetail();
			} catch (err) {
				console.error('reselect request failed:', err);
				uni.hideLoading();
				uni.showToast({ title: err?.msg || err?.message || '重新选择失败', icon: 'none' });
			}
		},
		// 点击确认（包裹一层用于调试与兼容）
		onConfirmClick() {
			console.debug('onConfirmClick called, selectedReselectScheduleId=', this.selectedReselectScheduleId);
			if (!this.selectedReselectScheduleId) {
				uni.showToast({ title: '请先选择时段', icon: 'none' });
				return;
			}
			// 立即可见的反馈（便于确认点击事件已触发）
			uni.showToast({ title: '已点击确认', icon: 'none', duration: 600 });
			// 调用确认逻辑
			this.confirmReselectFromModal();
		},

		// 返回
		goBack() {
			try {
				// 首先尝试后退一步
				uni.navigateBack({
					delta: 1,
					complete: (res) => {
						// 如果后退无效（仍然在同一页），在短延时后兜底跳转到记录页
						setTimeout(() => {
							// 兜底：切换到记录页（若 records 是 tab 页），否则重定向
							try {
								uni.switchTab({ url: '/pages/records/records' });
							} catch (e) {
								uni.redirectTo({ url: '/pages/records/records' });
							}
						}, 300);
					}
				});
			} catch (e) {
				console.error('goBack error, fallback to records', e);
				try {
					uni.switchTab({ url: '/pages/records/records' });
				} catch (err) {
					uni.redirectTo({ url: '/pages/records/records' });
				}
			}
		}
	}
};
</script>

<style scoped>
.appointment-detail-page {
	min-height: 100vh;
	background: #f5f7fa;
	padding-bottom: 160rpx;
}

/* 状态卡片 */
.status-card {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
	margin: 30rpx;
	border-radius: 20rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	color: #fff;
}

.status-card.status-pending {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.status-card.status-confirmed {
	background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
}

.status-card.status-rescheduled {
	background: linear-gradient(135deg, #8e24aa 0%, #ba68c8 100%);
}

.status-card.status-completed {
	background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
}

.status-card.status-cancelled {
	background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
}

.status-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
}

.status-text {
	font-size: 36rpx;
	font-weight: 600;
}

/* 信息卡片 */
.info-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
}

.card-title {
	display: flex;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #f5f5f5;
}

.title-icon {
	font-size: 32rpx;
	margin-right: 12rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}

.info-row:last-child {
	border-bottom: none;
}

.info-label {
	font-size: 28rpx;
	color: #666;
}

.info-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.original-price {
	color: #999;
	text-decoration: line-through;
}

.actual-price {
	color: #ff5722;
	font-weight: 600;
	font-size: 32rpx;
}

/* 温馨提示卡片 */
.tips-card {
	background: #fff;
	margin: 0 30rpx 20rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	background: linear-gradient(135deg, #fff9e6 0%, #fffbf0 100%);
	border: 2rpx solid #ffe082;
}

.tips-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #f57c00;
	margin-bottom: 20rpx;
}

.tips-item {
	font-size: 26rpx;
	color: #666;
	line-height: 1.8;
	margin-bottom: 12rpx;
}

.tips-item:last-child {
	margin-bottom: 0;
}

/* 底部操作栏 */
.bottom-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
	display: flex;
	gap: 20rpx;
	z-index: 100;
}

.action-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	font-weight: 600;
	border: none;
}

.waitlist-btn {
	background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
	color: #fff;
}

.cancel-btn {
	background: linear-gradient(135deg, #f44336 0%, #e57373 100%);
	color: #fff;
}

.back-btn {
	background: linear-gradient(135deg, #9e9e9e 0%, #bdbdbd 100%);
	color: #fff;
}

.btn-icon {
	margin-right: 8rpx;
	font-size: 32rpx;
}

.btn-text {
	font-size: 30rpx;
}

/* 自定义模态样式 */
.custom-modal-wrap {
	position: fixed;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	z-index: 200;
}
.custom-modal-overlay {
	position: absolute;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
	background: rgba(0,0,0,0.4);
	z-index: 200;
}
.custom-modal {
	position: fixed;
	left: 50%;
	top: 50%;
	transform: translate(-50%, -50%);
	width: 88%;
	max-height: 80%;
	background: #fff;
	border-radius: 16rpx;
	overflow: hidden;
	display: flex;
	flex-direction: column;
	z-index: 201;
	box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.12);
}
.modal-header {
	padding: 20rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-bottom: 1rpx solid #f5f5f5;
}
.modal-title {
	font-size: 32rpx;
	font-weight: 700;
}
.modal-close {
	background: transparent;
	border: none;
	color: #666;
}
.modal-test {
	background: #1976d2;
	color: #fff;
	border: none;
	padding: 6rpx 10rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
}
.modal-body {
	padding: 12rpx 16rpx;
	flex: 1;
	overflow: auto;
	-webkit-overflow-scrolling: touch;
}
.option-card {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 14rpx;
	border-bottom: 1rpx solid #f0f0f0;
}
.option-card.selected {
	background: #f5f9ff;
}
.doctor-name {
	font-size: 30rpx;
	font-weight: 600;
}
.department, .date-time, .slot-meta {
	font-size: 24rpx;
	color: #666;
	margin-top: 6rpx;
}
.card-left {
	flex: 1;
}
.card-right {
	width: 120rpx;
	display: flex;
	justify-content: center;
	align-items: center;
}
.select-btn {
	background: linear-gradient(90deg,#1976d2,#42a5f5);
	color: #fff;
	padding: 8rpx 12rpx;
	border-radius: 10rpx;
	border: none;
}
.checkmark {
	font-size: 28rpx;
	color: #4caf50;
	margin-left: 8rpx;
}
.confirm-btn.disabled {
	opacity: 0.6;
	pointer-events: none;
}
.modal-footer {
	padding: 16rpx;
	border-top: 1rpx solid #f5f5f5;
	display: flex;
	justify-content: center;
}
.confirm-btn {
	background: #4caf50;
	color: #fff;
	padding: 14rpx 40rpx;
	border-radius: 12rpx;
	border: none;
}

/* modal controls */
.modal-controls {
	display:flex;
	justify-content:space-between;
	align-items:center;
	padding: 8rpx 12rpx;
	border-bottom:1rpx solid #f5f5f5;
	background:#fff;
}
.filter-group, .sort-group {
	display:flex;
	align-items:center;
	gap:8rpx;
}
.filter-label {
	font-size:24rpx;
	color:#666;
	margin-right:6rpx;
}
.chip {
	background:#f3f6ff;
	border-radius:12rpx;
	padding:6rpx 12rpx;
	font-size:22rpx;
	color:#333;
	border:none;
}
.chip.active {
	background:#e6f0ff;
	border:1rpx solid #cde0ff;
	color:#1976d2;
}
</style>