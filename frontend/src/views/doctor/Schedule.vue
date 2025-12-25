<template>
  <div class="doctor-schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的排班</span>
          <el-radio-group v-model="viewMode" class="view-toggle">
            <el-radio-button label="day">日视图</el-radio-button>
            <el-radio-button label="week">周视图</el-radio-button>
            <el-radio-button label="month">月视图</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 顶部筛选栏：开始日期 / 结束日期 / 时间段 / 重置 -->
      <div class="filters-bar">
        <el-form inline>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="filterStartDate"
              type="date"
              placeholder="选择开始日期"
              @change="handleFilterDateChange"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="filterEndDate"
              type="date"
              placeholder="选择结束日期"
              @change="handleFilterDateChange"
            />
          </el-form-item>
          <el-form-item label="时间段">
            <el-select v-model="filterTimeSlot" placeholder="全部时间段" style="width: 160px" @change="loadSchedules">
              <el-option label="全部时间段" value="ALL" />
              <el-option label="上午" value="MORNING" />
              <el-option label="下午" value="AFTERNOON" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="resetFilters">重置筛选</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日视图 -->
      <div v-if="viewMode === 'day'" class="day-view">
        <div class="day-header">
          <h3>{{ formatSelectedDate() }}</h3>
          <div class="day-navigation">
            <el-button @click="previousDay" icon="ArrowLeft" circle />
            <el-button @click="nextDay" icon="ArrowRight" circle />
          </div>
        </div>
        
        <div class="day-schedule">
          <div
            v-for="timeSlot in displayTimeSlots"
            :key="timeSlot.time"
            class="day-time-slot"
            @click="handleDaySlotClick(selectedDate, timeSlot.time)"
          >
            <div class="time-label">{{ timeSlot.label }}</div>
            <div class="slot-content">
              <div
                v-if="getDayScheduleForSlot(timeSlot.time)"
                class="schedule-item"
                :class="getScheduleStatusClass(getDayScheduleForSlot(timeSlot.time))"
              >
                <div class="schedule-title">
                  {{ formatTime(getDayScheduleForSlot(timeSlot.time).timeSlot) }}
                </div>
                <div class="schedule-clinic">
                  {{ clinicName }}
                </div>
                <div class="schedule-room">
                  {{ formatSlotType(getDayScheduleForSlot(timeSlot.time).slotType) }}
                </div>
                <div class="schedule-patients">
                  {{ (getDayScheduleForSlot(timeSlot.time).totalSlots - getDayScheduleForSlot(timeSlot.time).availableSlots) || 0 }} / {{ getDayScheduleForSlot(timeSlot.time).totalSlots || 0 }}
                </div>
              </div>
              <div v-else class="empty-slot">
                <span>无排班</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 周视图 -->
      <div v-else-if="viewMode === 'week'" class="week-view">
        <div class="week-header">
          <h3>{{ formatWeekRange() }}</h3>
          <div class="week-navigation">
            <el-button @click="previousWeek" icon="ArrowLeft" circle />
            <el-button @click="nextWeek" icon="ArrowRight" circle />
          </div>
        </div>
        <div class="schedule-calendar">
          <div class="calendar-header">
          <div class="time-column">时间</div>
          <div
            v-for="day in weekDays"
            :key="day.date"
            class="day-column"
            :class="{ today: day.isToday }"
          >
            <div class="day-name">{{ day.name }}</div>
            <div class="day-date">{{ day.date }}</div>
          </div>
        </div>

        <div class="calendar-body">
          <div
            v-for="timeSlot in displayTimeSlots"
            :key="timeSlot.time"
            class="time-row"
          >
            <div class="time-column">{{ timeSlot.label }}</div>
            <div
              v-for="day in weekDays"
              :key="`${day.fullDate}-${timeSlot.time}`"
              class="schedule-cell"
              @click="handleCellClick(day.fullDate, timeSlot.time)"
            >
              <div
                v-if="getScheduleForCell(day.fullDate, timeSlot.time)"
                class="schedule-item"
                :class="getScheduleStatusClass(getScheduleForCell(day.fullDate, timeSlot.time))"
              >
                <div class="schedule-title">
                  {{ formatTime(getScheduleForCell(day.fullDate, timeSlot.time).timeSlot) }}
                </div>
                <div class="schedule-clinic">
                  {{ clinicName }}
                </div>
                <div class="schedule-room">
                  {{ formatSlotType(getScheduleForCell(day.fullDate, timeSlot.time).slotType) }}
                </div>
                <div class="schedule-patients">
                  {{ (getScheduleForCell(day.fullDate, timeSlot.time).totalSlots - getScheduleForCell(day.fullDate, timeSlot.time).availableSlots) || 0 }} / {{ getScheduleForCell(day.fullDate, timeSlot.time).totalSlots || 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>
        </div>
        <!-- 颜色图例说明 -->
        <div class="legend-container">
          <div class="legend-item">
            <div class="legend-box legend-normal"></div>
            <span class="legend-text">号源充足</span>
          </div>
          <div class="legend-item">
            <div class="legend-box legend-low"></div>
            <span class="legend-text">号源紧张</span>
          </div>
          <div class="legend-item">
            <div class="legend-box legend-full"></div>
            <span class="legend-text">已满</span>
          </div>
        </div>
      </div>

      <!-- 月视图（参考管理员端：纯7列日历） -->
      <div v-else-if="viewMode === 'month'" class="month-view">
        <div class="month-header">
          <h3>{{ formatMonthRange() }}</h3>
          <div class="month-navigation">
            <el-button @click="previousMonth" icon="ArrowLeft" circle />
            <el-button @click="nextMonth" icon="ArrowRight" circle />
          </div>
        </div>
        <div class="calendar-header">
          <div
            v-for="dayName in ['周一','周二','周三','周四','周五','周六','周日']"
            :key="dayName"
            class="day-column"
          >
            <div class="day-name">{{ dayName }}</div>
          </div>
        </div>

        <div class="calendar-body">
          <div
            v-for="(week, rIdx) in monthGrid"
            :key="rIdx"
            class="time-row"
          >
            <div
              v-for="day in week"
              :key="day.fullDate"
              class="schedule-cell"
              :class="{ today: day.isToday, outside: !day.inMonth, inRange: isInFilterRange(day.fullDate) }"
              @click="handleMonthCellClick(day.fullDate)"
            >
              <div class="month-day">
                <span class="day-date">{{ day.date }}</span>
              </div>
              <div class="cell-schedules">
                <template v-for="ts in displayTimeSlots" :key="ts.time">
                  <div
                    v-if="getScheduleForCell(day.fullDate, ts.time)"
                    class="schedule-item"
                    :class="getScheduleStatusClass(getScheduleForCell(day.fullDate, ts.time))"
                    @click.stop="handleCellClick(day.fullDate, ts.time)"
                  >
                    <div class="schedule-title">
                      {{ formatTime(getScheduleForCell(day.fullDate, ts.time).timeSlot) }}
                    </div>
                    <div class="schedule-clinic">
                      {{ clinicName }}
                    </div>
                    <div class="schedule-room">
                      {{ formatSlotType(getScheduleForCell(day.fullDate, ts.time).slotType) }}
                    </div>
                    <div class="schedule-patients">
                      {{ (getScheduleForCell(day.fullDate, ts.time).totalSlots - getScheduleForCell(day.fullDate, ts.time).availableSlots) || 0 }} / {{ getScheduleForCell(day.fullDate, ts.time).totalSlots || 0 }}
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 排班详情对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="排班详情"
      width="600px"
    >
      <div v-if="selectedSchedule" class="schedule-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日期">
            {{ formatDate(new Date(selectedSchedule.scheduleDate)) }}
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ formatTime(selectedSchedule.timeSlot) }}
          </el-descriptions-item>
          <el-descriptions-item label="门诊类型">
            {{ formatSlotType(selectedSchedule.slotType) }}
          </el-descriptions-item>
          <el-descriptions-item label="总号源">
            {{ selectedSchedule.totalSlots || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="可用号源">
            {{ selectedSchedule.availableSlots || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="已预约">
            {{ (selectedSchedule.totalSlots - selectedSchedule.availableSlots) || 0 }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 患者列表 - 仅当天的排班显示今日预约 -->
        <div v-if="isTodaySchedule" class="patients-section">
          <div class="patients-header">
            <h4>今日预约</h4>
          </div>
          <el-table
            :data="selectedSchedule.patients || []"
            style="width: 100%"
            max-height="300"
            empty-text="无预约"
          >
            <el-table-column prop="name" label="患者姓名" />
            <el-table-column prop="status" label="状态" align="center">
              <template #default="scope">
                <el-tag
                  :type="getPatientStatusType(scope.row.status)"
                  size="small"
                >
                  {{ getAppointmentStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="scheduleDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick, shallowRef } from 'vue'
import { formatDate, getTodayString } from '@/utils'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { getMySchedules, getMyInfo, getTodayPatients } from '@/api/doctor'

// 视图模式
const viewMode = ref('week') // 'day' 或 'week'
const selectedWeek = ref(new Date())
const selectedDate = ref(new Date())
const selectedMonth = ref(new Date())
const scheduleDialogVisible = ref(false)
const selectedSchedule = ref(null)
const dialogTimeSlotFilter = ref('')
const dialogMorningCount = ref(0)
const dialogAfternoonCount = ref(0)
const dialogEveningCount = ref(0)
// 顶部筛选栏模型
const filterStartDate = ref(null) // Date
const filterEndDate = ref(null)   // Date
const filterTimeSlot = ref('ALL') // ALL/MORNING/AFTERNOON

// 获取当前医生信息（通过后端接口校验映射关系）
const myDoctorInfo = ref(null)
const loadMyInfo = async () => {
  try {
    const res = await getMyInfo()
    myDoctorInfo.value = res.data || null
  } catch (e) {
    console.error('获取医生信息失败', e)
  }
}

// 医生所属门诊名称（使用现有接口返回的 myDoctorInfo）
const clinicName = computed(() => {
  const info = myDoctorInfo.value
  // 兼容不同字段结构：优先 clinic.name，其次 clinicName
  return (info?.clinic?.name) || (info?.clinicName) || '未设置门诊'
})

// 判断选中的排班是否是今天的
const isTodaySchedule = computed(() => {
  if (!selectedSchedule.value) return false
  const scheduleDate = typeof selectedSchedule.value.scheduleDate === 'string' 
    ? selectedSchedule.value.scheduleDate 
    : formatDate(new Date(selectedSchedule.value.scheduleDate))
  return scheduleDate === getTodayString()
})

// 所有时间段
const allTimeSlots = [
  { time: 'MORNING', label: '上午' },
  { time: 'AFTERNOON', label: '下午' }
]

// 根据筛选展示时间段
const displayTimeSlots = computed(() => {
  if (filterTimeSlot.value === 'ALL') return allTimeSlots
  return allTimeSlots.filter(s => s.time === filterTimeSlot.value)
})

// 排班数据（使用 shallowRef 避免深度响应导致的性能问题)
const schedules = shallowRef([])
const isLoadingSchedules = ref(false)

// 计算本周日期（避免在循环中修改同一 Date 实例）
const weekDays = computed(() => {
  const days = []
  const base = new Date(selectedWeek.value)
  const day = base.getDay()
  const monday = new Date(base)
  monday.setDate(base.getDate() - day + (day === 0 ? -6 : 1))
  monday.setHours(0, 0, 0, 0)
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  for (let i = 0; i < 7; i++) {
    const current = new Date(monday)
    current.setDate(monday.getDate() + i)
    const dateStr = formatDate(current)
    days.push({
      name: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i],
      date: current.getDate(),
      fullDate: dateStr,
      isToday: current.getTime() === today.getTime()
    })
  }
  return days
})

// 计算月视图网格
const monthGrid = computed(() => {
  const base = new Date(selectedMonth.value)
  const year = base.getFullYear()
  const month = base.getMonth()
  const firstOfMonth = new Date(year, month, 1)
  const firstDay = firstOfMonth.getDay()
  const offsetToMonday = firstDay === 0 ? -6 : 1 - firstDay
  const gridStart = new Date(firstOfMonth)
  gridStart.setDate(firstOfMonth.getDate() + offsetToMonday)
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const weeks = []
  for (let w = 0; w < 6; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const current = new Date(gridStart)
      current.setDate(gridStart.getDate() + w * 7 + d)
      const dateStr = formatDate(current)
      week.push({
        date: current.getDate(),
        fullDate: dateStr,
        isToday: current.getTime() === today.getTime(),
        inMonth: current.getMonth() === month
      })
    }
    weeks.push(week)
  }
  return weeks
})

// 统一时间段大小写
const normalizeSlot = (slot) => (slot ?? '').toString().trim().toUpperCase()

// 加载排班数据（优先使用视图模式对应的日期，其次使用筛选栏日期）
const loadSchedules = async () => {
  // 防止重复加载
  if (isLoadingSchedules.value) {
    return
  }
  
  isLoadingSchedules.value = true
  
  try {
    let startDate, endDate
    // 优先使用视图模式对应的日期
    if (viewMode.value === 'day') {
      const d = new Date(selectedDate.value)
      d.setHours(0, 0, 0, 0)
      startDate = formatDate(d)
      endDate = formatDate(d)
    } else if (viewMode.value === 'week') {
      const base = new Date(selectedWeek.value)
      const day = base.getDay()
      const monday = new Date(base)
      monday.setDate(base.getDate() - day + (day === 0 ? -6 : 1))
      monday.setHours(0, 0, 0, 0)
      const sunday = new Date(monday)
      sunday.setDate(monday.getDate() + 6)
      sunday.setHours(23, 59, 59, 999)
      startDate = formatDate(monday)
      endDate = formatDate(sunday)
    } else if (viewMode.value === 'month') {
      // 以周一开头、周日结尾的完整月网格范围
      const base = new Date(selectedMonth.value)
      const year = base.getFullYear()
      const month = base.getMonth()
      const firstOfMonth = new Date(year, month, 1)
      const firstDay = firstOfMonth.getDay()
      const offsetToMonday = firstDay === 0 ? -6 : 1 - firstDay
      const gridStart = new Date(firstOfMonth)
      gridStart.setDate(firstOfMonth.getDate() + offsetToMonday)

      const gridEnd = new Date(gridStart)
      gridEnd.setDate(gridStart.getDate() + 6*7 - 1)
      startDate = formatDate(gridStart)
      endDate = formatDate(gridEnd)
    } else if (filterStartDate.value && filterEndDate.value) {
      // 如果不在特定视图模式下，且用户手动设置了筛选栏日期，使用筛选栏日期
      startDate = formatDate(new Date(filterStartDate.value))
      endDate = formatDate(new Date(filterEndDate.value))
    }

    const params = { startDate, endDate }
    if (filterTimeSlot.value !== 'ALL') {
      params.timeSlot = filterTimeSlot.value
    }

    const resp = await getMySchedules(params)

    let list = []
    if (Array.isArray(resp?.data?.schedules)) {
      list = resp.data.schedules
    } else if (Array.isArray(resp?.data)) {
      list = resp.data
    } else {
      list = []
    }
    
    // 关键修复：确保数据完整加载后再一次性更新，避免中间状态导致闪现
    schedules.value = list
  } catch (error) {
    console.error('加载排班数据失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error('加载排班数据失败: ' + (error.message || '网络错误'))
    }
  } finally {
    isLoadingSchedules.value = false
  }
}

// 已移除前端二次过滤，直接使用后端筛选结果

// 获取日视图指定时间段的排班
const getDayScheduleForSlot = (timeSlot) => {
  // 避免在渲染过程中重复计算日期
  const dateStr = formatDate(selectedDate.value)
  
  // 使用 find 而不是 filter，提高性能
  return schedules.value.find(schedule => {
    if (!schedule) return false
    // 后端返回的 scheduleDate 已经是 yyyy-MM-dd 格式的字符串，直接比较即可
    // 如果后端返回的是 Date 对象，则使用 formatDate 格式化
    const scheduleDate = typeof schedule.scheduleDate === 'string' 
      ? schedule.scheduleDate 
      : formatDate(new Date(schedule.scheduleDate))
    return scheduleDate === dateStr && normalizeSlot(schedule.timeSlot) === normalizeSlot(timeSlot)
  })
}

// 获取周视图/月视图指定单元格的排班
const getScheduleForCell = (fullDate, timeSlot) => {
  // 添加数据有效性检查，避免在数据加载过程中访问空数组
  if (!schedules.value || schedules.value.length === 0) {
    return null
  }
  
  return schedules.value.find(schedule => {
    if (!schedule) return false
    // 后端返回的 scheduleDate 已经是 yyyy-MM-dd 格式的字符串，直接比较即可
    // 如果后端返回的是 Date 对象，则使用 formatDate 格式化
    const scheduleDate = typeof schedule.scheduleDate === 'string' 
      ? schedule.scheduleDate 
      : formatDate(new Date(schedule.scheduleDate))
    return scheduleDate === fullDate && normalizeSlot(schedule.timeSlot) === normalizeSlot(timeSlot)
  })
}

// 格式化时间段显示
const formatTime = (timeSlot) => {
  const slot = normalizeSlot(timeSlot)
  if (slot === 'MORNING') return '上午'
  if (slot === 'AFTERNOON') return '下午'
  if (slot === 'EVENING') return '晚上'
  return timeSlot
}

// 格式化门诊类型
const formatSlotType = (slotType) => {
  if (!slotType) return '普通门诊'
  const type = String(slotType).toUpperCase()
  if (type === 'NORMAL') return '普通门诊'
  if (type === 'EXPERT') return '专家门诊'
  if (type === 'SPECIAL') return '特需门诊'
  return '普通门诊'
}

// 获取排班状态样式类
const getScheduleStatusClass = (schedule) => {
  if (!schedule) return ''
  const available = schedule.availableSlots || 0
  const total = schedule.totalSlots || 0
  if (available === 0) return 'full'
  if (available < total * 0.3) return 'low'
  return 'normal'
}

// 格式化选中日期显示
const formatSelectedDate = () => {
  const date = new Date(selectedDate.value)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
  return `${year}年${month}月${day}日 ${weekDay}`
}

// 格式化周范围显示
const formatWeekRange = () => {
  if (weekDays.value.length === 0) return ''
  const first = weekDays.value[0].fullDate
  const last = weekDays.value[6].fullDate
  return `${first} ~ ${last}`
}

// 格式化月份显示
const formatMonthRange = () => {
  const date = new Date(selectedMonth.value)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  return `${year}年${month}月`
}

// 日视图导航
const previousDay = () => {
  const date = new Date(selectedDate.value)
  date.setDate(date.getDate() - 1)
  date.setHours(0, 0, 0, 0)
  selectedDate.value = date
  filterStartDate.value = new Date(date)
  filterEndDate.value = new Date(date)
  loadSchedules()
}

const nextDay = () => {
  const date = new Date(selectedDate.value)
  date.setDate(date.getDate() + 1)
  date.setHours(0, 0, 0, 0)
  selectedDate.value = date
  filterStartDate.value = new Date(date)
  filterEndDate.value = new Date(date)
  loadSchedules()
}

// 日视图时间段点击
const handleDaySlotClick = (date, timeSlot) => {
  const schedule = getDayScheduleForSlot(timeSlot)
  if (schedule) {
    selectedSchedule.value = schedule
    scheduleDialogVisible.value = true
    dialogTimeSlotFilter.value = normalizeSlot(schedule.timeSlot)
    loadPatientsForSchedule(schedule.id)
  }
}

// 加载排班的患者列表
const loadPatientsForSchedule = async (scheduleId) => {
  if (!scheduleId) return
  try {
    const resp = await getTodayPatients({ scheduleId })
    if (selectedSchedule.value) {
      selectedSchedule.value.patients = resp.data || []
    }
  } catch (error) {
    console.error('加载患者列表失败:', error)
  }
}

// 获取预约状态文本
const getAppointmentStatusText = (status) => {
  if (!status) return '未知'
  const statusMap = {
    'PENDING': '待就诊',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'NO_SHOW': '未到诊',
    'IN_PROGRESS': '就诊中'
  }
  return statusMap[status] || status
}

// 获取患者状态标签类型
const getPatientStatusType = (status) => {
  if (!status) return 'info'
  
  // 支持中文状态
  const chineseStatusMap = {
    '待就诊': 'warning',
    '已完成': 'success',
    '已取消': 'danger',
    '未到诊': 'info',
    '就诊中': 'primary',
    '已确认': 'success'
  }
  
  // 如果已经是中文，直接返回
  if (chineseStatusMap[status]) {
    return chineseStatusMap[status]
  }
  
  // 如果是英文状态，先转换为中文再判断
  const chineseStatus = getAppointmentStatusText(status)
  return chineseStatusMap[chineseStatus] || 'info'
}

// 视图切换处理方法
const handleViewModeChange = (mode) => {
  const today = new Date()
  today.setHours(0,0,0,0)

  if (mode === 'day') {
    // 快捷键：设为当天
    selectedDate.value = new Date(today)
    filterStartDate.value = new Date(today)
    filterEndDate.value = new Date(today)
  } else if (mode === 'week') {
    // 快捷键：设为本周周一到周日
    const day = today.getDay()
    const monday = new Date(today)
    monday.setDate(today.getDate() - day + (day === 0 ? -6 : 1))
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    selectedWeek.value = new Date(today)
    filterStartDate.value = new Date(monday)
    filterEndDate.value = new Date(sunday)
  } else if (mode === 'month') {
    // 快捷键：设为当月 1 日 至 当月末
    const year = today.getFullYear()
    const month = today.getMonth()
    const first = new Date(year, month, 1)
    selectedMonth.value = new Date(first)
    // 不清空筛选日期：保持用户选择的范围用于月视图黄标
  }
  
  // 移除 nextTick，直接同步调用避免时序问题
  loadSchedules()
}

// 监听视图模式变化（排除初始化时的触发）
watch(viewMode, (newMode, oldMode) => {
  if (oldMode !== undefined && newMode !== oldMode) {
    handleViewModeChange(newMode)
  }
})

const handleWeekChange = (date) => {
  selectedWeek.value = date
  loadSchedules()
}

const handleDateChange = (date) => {
  selectedDate.value = date
  loadSchedules()
}

const handleMonthChange = (date) => {
  selectedMonth.value = date
  loadSchedules()
}

// 周视图导航方法
const previousWeek = () => {
  const date = new Date(selectedWeek.value)
  date.setDate(date.getDate() - 7)
  date.setHours(0, 0, 0, 0)
  selectedWeek.value = date
  // 同步更新筛选栏日期
  const day = date.getDay()
  const monday = new Date(date)
  monday.setDate(date.getDate() - day + (day === 0 ? -6 : 1))
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  filterStartDate.value = new Date(monday)
  filterEndDate.value = new Date(sunday)
  loadSchedules()
}

const nextWeek = () => {
  const date = new Date(selectedWeek.value)
  date.setDate(date.getDate() + 7)
  date.setHours(0, 0, 0, 0)
  selectedWeek.value = date
  // 同步更新筛选栏日期
  const day = date.getDay()
  const monday = new Date(date)
  monday.setDate(date.getDate() - day + (day === 0 ? -6 : 1))
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  filterStartDate.value = new Date(monday)
  filterEndDate.value = new Date(sunday)
  loadSchedules()
}

// 月视图导航方法
const previousMonth = () => {
  const date = new Date(selectedMonth.value)
  date.setMonth(date.getMonth() - 1)
  date.setDate(1)
  date.setHours(0, 0, 0, 0)
  selectedMonth.value = date
  loadSchedules()
}

const nextMonth = () => {
  const date = new Date(selectedMonth.value)
  date.setMonth(date.getMonth() + 1)
  date.setDate(1)
  date.setHours(0, 0, 0, 0)
  selectedMonth.value = date
  loadSchedules()
}

// 月视图单元格点击：优先打开对应时间段的排班详情（若存在）
const handleMonthCellClick = (fullDate) => {
  let preferred = null
  const slots = filterTimeSlot.value === 'ALL' ? ['MORNING','AFTERNOON'] : [filterTimeSlot.value]
  for (const ts of slots) {
    const found = getScheduleForCell(fullDate, ts)
    if (found) { preferred = found; break }
  }
  if (preferred) {
    selectedSchedule.value = preferred
    scheduleDialogVisible.value = true
    // 设置时间段过滤器为当前排班的时间段
    dialogTimeSlotFilter.value = normalizeSlot(preferred.timeSlot)
    loadPatientsForSchedule(preferred.id)
  }
}

// 输入日期段：自动跳转到月视图并定位月份
const handleFilterDateChange = () => {
  if (filterStartDate.value && filterEndDate.value) {
    viewMode.value = 'month'
    selectedMonth.value = new Date(filterStartDate.value)
  }
  loadSchedules()
}

// 判断给定日期是否在筛选范围内（用于月视图淡黄色高亮）
const isInFilterRange = (fullDateStr) => {
  if (!filterStartDate.value || !filterEndDate.value) return false
  // 解析为本地日期，避免 YYYY-MM-DD 被当作 UTC 导致时区偏移
  const [yy, mm, dd] = String(fullDateStr).split('-').map(n => Number(n))
  const d = new Date(yy, mm - 1, dd)
  const s = new Date(filterStartDate.value)
  const e = new Date(filterEndDate.value)
  d.setHours(0,0,0,0); s.setHours(0,0,0,0); e.setHours(0,0,0,0)
  return d.getTime() >= s.getTime() && d.getTime() <= e.getTime()
}

// 重置筛选栏
const resetFilters = () => {
  filterStartDate.value = null
  filterEndDate.value = null
  filterTimeSlot.value = 'ALL'
  loadSchedules()
}

const handleCellClick = (fullDate, timeSlot) => {
  const schedule = getScheduleForCell(fullDate, timeSlot)
  if (schedule) {
    selectedSchedule.value = schedule
    scheduleDialogVisible.value = true
    // 设置时间段过滤器为当前排班的时间段
    dialogTimeSlotFilter.value = normalizeSlot(schedule.timeSlot)
    loadPatientsForSchedule(schedule.id)
  }
}


onMounted(() => {
  loadMyInfo()
  loadSchedules()
})
</script>

<style scoped>
.doctor-schedule {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.view-toggle {
  margin-left: auto;
}

.filters-bar {
  padding: 10px 0 20px 0;
}

.date-range-select {
  width: 120px;
}

/* 日视图样式 */
.day-view {
  padding: 20px 0;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.day-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.day-navigation {
  display: flex;
  gap: 10px;
}

/* 周视图样式 */
.week-view {
  padding: 20px 0;
}

/* 周视图头部样式 */
.week-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.week-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.week-navigation {
  display: flex;
  gap: 10px;
}

/* 月视图头部样式 */
.month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.month-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.month-navigation {
  display: flex;
  gap: 10px;
}

.day-schedule {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.day-time-slot {
  display: flex;
  align-items: center;
  min-height: 80px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.day-time-slot:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.time-label {
  width: 100px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #e4e7ed;
  padding: 20px 10px;
}

.slot-content {
  flex: 1;
  padding: 15px 20px;
}

.empty-slot {
  text-align: center;
  color: #c0c4cc;
  font-style: italic;
}

.empty-slot span {
  font-size: 14px;
}

/* 周视图样式保持不变 */
.schedule-calendar {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.calendar-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.time-column {
  width: 80px;
  padding: 15px 10px;
  text-align: center;
  font-weight: 600;
  border-right: 1px solid #e4e7ed;
}

.day-column {
  flex: 1;
  padding: 15px 10px;
  text-align: center;
  border-right: 1px solid #e4e7ed;
}

.day-column:last-child {
  border-right: none;
}

.day-column.today {
  background-color: #ecf5ff;
  color: #409eff;
}

.day-name {
  font-weight: 600;
  margin-bottom: 5px;
}

.day-date {
  font-size: 12px;
  color: #909399;
}

.calendar-body {
  background-color: #fff;
}

.time-row {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
}

.time-row:last-child {
  border-bottom: none;
}

.schedule-cell {
  flex: 1;
  min-height: 60px;
  padding: 5px;
  border-right: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.3s;
}

.schedule-cell:hover {
  background-color: #f5f7fa;
}

.schedule-cell:last-child {
  border-right: none;
}

.schedule-item {
  height: 100%;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.2;
}

.schedule-item.normal {
  background-color: #e1f3d8;
  border: 1px solid #b3d8a4;
  color: #529b2e;
}

.schedule-item.low {
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  color: #d48806;
}

.schedule-item.full {
  background-color: #ffe6e6;
  border: 1px solid #ffb3b3;
  color: #d32f2f;
}

.schedule-item.cancelled {
  background-color: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #c45656;
}

.schedule-title {
  font-weight: 600;
  margin-bottom: 2px;
}

.schedule-room {
  margin-bottom: 2px;
}

.schedule-patients {
  font-size: 11px;
}

.schedule-detail {
  margin-bottom: 20px;
}

.patients-section {
  margin-top: 20px;
}

.patients-header {
  margin-bottom: 10px;
}

.patients-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
/* 门诊信息样式：插入在时间与号别之间 */
.schedule-clinic {
  font-size: 12px;
  color: #67c23a; /* 与成功标签色系一致 */
  margin: 2px 0;
}

/* 颜色图例样式 */
.legend-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-box {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid;
}

.legend-box.legend-normal {
  background-color: #e1f3d8;
  border-color: #b3d8a4;
}

.legend-box.legend-low {
  background-color: #fff7e6;
  border-color: #ffd591;
}

.legend-box.legend-full {
  background-color: #ffe6e6;
  border-color: #ffb3b3;
}

.legend-text {
  font-size: 14px;
  color: #606266;
}

/* 月视图：跨月淡化、筛选范围淡黄色、日期布局与标签间距 */
.month-view .schedule-cell.outside { background-color: #fafafa; color: #c0c4cc; }
.month-view .schedule-cell.inRange { background-color: #fff7e6; box-shadow: inset 0 0 0 2px #f5d78e; }
.month-day { display: flex; justify-content: space-between; }
.month-view .cell-schedules { display: flex; flex-direction: column; gap: 6px; }
.month-view .schedule-cell.inRange .schedule-item { background-color: #fff9ed; border-color: #f0c78a; }
.slot-chip { margin: 2px; }
</style>