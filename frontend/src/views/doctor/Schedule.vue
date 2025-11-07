<template>
  <div class="doctor-schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的排班</span>
          <div class="header-actions">
            <!-- 视图切换 -->
            <el-radio-group v-model="viewMode" @change="handleViewModeChange" class="view-toggle">
              <el-radio-button label="day">日视图</el-radio-button>
              <el-radio-button label="week">周视图</el-radio-button>
              <el-radio-button label="month">月视图</el-radio-button>
            </el-radio-group>
            
            <!-- 时间段筛选（与下方筛选栏保持一致） -->
            <el-select v-model="filterTimeSlot" placeholder="时间段" style="width: 140px" @change="loadSchedules">
              <el-option label="全部时间段" value="ALL" />
              <el-option label="上午" value="MORNING" />
              <el-option label="下午" value="AFTERNOON" />
            </el-select>
            <!-- 管理员逻辑：单选作为快捷键，不在头部显示日期选择器 -->
          </div>
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
          <el-form-item>
            <el-tag type="info">医生ID：{{ myDoctorInfo?.id ?? '未知' }}</el-tag>
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
                <div class="schedule-room">
                  {{ getDayScheduleForSlot(timeSlot.time).slotType || '普通门诊' }}
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
      <div v-else-if="viewMode === 'week'" class="schedule-calendar">
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
                <div class="schedule-room">
                  {{ getScheduleForCell(day.fullDate, timeSlot.time).slotType || '普通门诊' }}
                </div>
                <div class="schedule-patients">
                  {{ (getScheduleForCell(day.fullDate, timeSlot.time).totalSlots - getScheduleForCell(day.fullDate, timeSlot.time).availableSlots) || 0 }} / {{ getScheduleForCell(day.fullDate, timeSlot.time).totalSlots || 0 }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 月视图（参考管理员端：纯7列日历） -->
      <div v-else-if="viewMode === 'month'" class="month-view">
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
                <el-tag
                  v-for="ts in displayTimeSlots"
                  :key="ts.time"
                  v-if="getScheduleForCell(day.fullDate, ts.time)"
                  size="small"
                  type="success"
                  effect="plain"
                  class="slot-chip"
                >{{ ts.label }}</el-tag>
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
            {{ selectedSchedule.slotType || '普通门诊' }}
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

        <!-- 患者列表 -->
        <div class="patients-section">
          <h4>预约患者列表</h4>
          <el-table
            :data="selectedSchedule.patients || []"
            style="width: 100%"
            max-height="300"
          >
            <el-table-column prop="name" label="患者姓名" width="120" />
            <el-table-column prop="phone" label="联系电话" width="120" />
            <el-table-column prop="appointmentTime" label="预约时间" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag
                  :type="getPatientStatusType(scope.row.status)"
                  size="small"
                >
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === '待就诊'"
                  type="primary"
                  size="small"
                  @click="startConsultation(scope.row)"
                >
                  开始就诊
                </el-button>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { formatDate } from '@/utils'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { getMySchedules, getMyInfo } from '@/api/doctor'

// 视图模式
const viewMode = ref('week') // 'day' 或 'week'
const selectedWeek = ref(new Date())
const selectedDate = ref(new Date())
const selectedMonth = ref(new Date())
const scheduleDialogVisible = ref(false)
const selectedSchedule = ref(null)
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

// 排班数据
const schedules = ref([])

// 计算本周日期（避免在循环中修改同一 Date 实例）
const weekDays = computed(() => {
  const days = []
  const base = new Date(selectedWeek.value)
  const day = base.getDay()
  const monday = new Date(base)
  monday.setDate(base.getDate() - day + (day === 0 ? -6 : 1))
  monday.setHours(0, 0, 0, 0)

  for (let i = 0; i < 7; i++) {
    const date = new Date(monday)
    date.setDate(monday.getDate() + i)
    const today = new Date()

    days.push({
      name: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i],
      date: formatDate(date, 'MM-DD'),
      fullDate: formatDate(date),
      isToday: formatDate(date) === formatDate(today)
    })
  }

  return days
})

// 计算当月的6行7列网格（以周一为一周开始）
const monthGrid = computed(() => {
  const base = new Date(selectedMonth.value)
  const year = base.getFullYear()
  const month = base.getMonth()
  const firstOfMonth = new Date(year, month, 1)
  const firstDay = firstOfMonth.getDay()
  // 以周一为第一天，计算网格开始的周一
  const offsetToMonday = firstDay === 0 ? -6 : 1 - firstDay
  const gridStart = new Date(firstOfMonth)
  gridStart.setDate(firstOfMonth.getDate() + offsetToMonday)
  gridStart.setHours(0,0,0,0)

  const weeks = []
  for (let w = 0; w < 6; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const cellDate = new Date(gridStart)
      cellDate.setDate(gridStart.getDate() + w*7 + d)
      const today = new Date()
      week.push({
        date: formatDate(cellDate, 'MM-DD'),
        fullDate: formatDate(cellDate),
        inMonth: cellDate.getMonth() === month,
        isToday: formatDate(cellDate) === formatDate(today)
      })
    }
    weeks.push(week)
  }
  return weeks
})
// 统一时间段大小写
const normalizeSlot = (slot) => (slot ?? '').toString().trim().toUpperCase()

// 加载排班数据（优先使用筛选栏日期范围，其次按视图模式）
const loadSchedules = async () => {
  try {
    let startDate, endDate
    if (filterStartDate.value && filterEndDate.value) {
      startDate = formatDate(new Date(filterStartDate.value))
      endDate = formatDate(new Date(filterEndDate.value))
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
    } else {
      const d = new Date(selectedDate.value)
      d.setHours(0, 0, 0, 0)
      startDate = formatDate(d)
      endDate = formatDate(d)
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
    schedules.value = list
  } catch (error) {
    console.error('加载排班数据失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error('加载排班数据失败: ' + (error.message || '网络错误'))
    }
  }
}

// 已移除前端二次过滤，直接使用后端筛选结果





// 格式化选中日期
const formatSelectedDate = () => {
  const date = new Date(selectedDate.value)
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
  return `${formatDate(date)} ${weekDay}`
}

// 日视图相关方法
const previousDay = () => {
  const date = new Date(selectedDate.value)
  date.setDate(date.getDate() - 1)
  selectedDate.value = date
  loadSchedules()
}

const nextDay = () => {
  const date = new Date(selectedDate.value)
  date.setDate(date.getDate() + 1)
  selectedDate.value = date
  loadSchedules()
}

const getDayScheduleForSlot = (timeSlot) => {
  const dateStr = formatDate(selectedDate.value)
  
  return schedules.value.find(schedule => {
    const scheduleDate = formatDate(new Date(schedule.scheduleDate))
    return scheduleDate === dateStr && normalizeSlot(schedule.timeSlot) === normalizeSlot(timeSlot)
  })
}

const handleDaySlotClick = (date, timeSlot) => {
  const schedule = getDayScheduleForSlot(timeSlot)
  if (schedule) {
    selectedSchedule.value = schedule
    scheduleDialogVisible.value = true
    // 加载患者列表
    loadPatientsForSchedule(schedule.id)
  }
}

// 加载指定排班的患者列表
const loadPatientsForSchedule = async (scheduleId) => {
  try {
    // 注意：这里需要一个新的API接口来获取指定排班的患者列表
    // 建议的API: GET /appointment/schedule/{scheduleId}
    // 返回该排班下的所有预约患者信息
    
    // 暂时使用模拟数据
    const mockPatients = [
      { 
        id: 1, 
        name: '张三', 
        phone: '138****1234', 
        appointmentTime: '08:30', 
        status: '已完成',
        patientId: 101
      },
      { 
        id: 2, 
        name: '李四', 
        phone: '139****5678', 
        appointmentTime: '09:00', 
        status: '待就诊',
        patientId: 102
      },
      { 
        id: 3, 
        name: '王五', 
        phone: '137****9012', 
        appointmentTime: '09:30', 
        status: '待就诊',
        patientId: 103
      }
    ]
    
    if (selectedSchedule.value) {
      selectedSchedule.value.patients = mockPatients
    }
    
    // TODO: 实际的API调用应该是这样的：
    // const response = await request.get(`/appointment/schedule/${scheduleId}`)
    // if (response.data) {
    //   selectedSchedule.value.patients = response.data.map(appointment => ({
    //     id: appointment.id,
    //     name: appointment.patientName,
    //     phone: appointment.patientPhone,
    //     appointmentTime: formatTime(appointment.appointmentTime),
    //     status: getAppointmentStatusText(appointment.status),
    //     patientId: appointment.patientId
    //   }))
    // }
    
  } catch (error) {
    console.error('加载患者列表失败:', error)
    ElMessage.error('加载患者列表失败')
  }
}

// 获取预约状态文本
const getAppointmentStatusText = (status) => {
  const statusMap = {
    'PENDING': '待就诊',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'NO_SHOW': '未到诊'
  }
  return statusMap[status] || status
}

// 格式化时间
const formatTime = (timeSlot) => {
  if (!timeSlot) return '';
  // 根据timeSlot显示上午/下午
  const s = normalizeSlot(timeSlot)
  if (s === 'MORNING') {
    return '上午';
  } else if (s === 'AFTERNOON') {
    return '下午';
  }
  return timeSlot;
}

const getScheduleForCell = (fullDate, timeSlot) => {
  return schedules.value.find(schedule => {
    const scheduleDate = formatDate(new Date(schedule.scheduleDate))
    return scheduleDate === fullDate && normalizeSlot(schedule.timeSlot) === normalizeSlot(timeSlot)
  })
}

const getScheduleStatusClass = (schedule) => {
  if (!schedule) return 'normal'
  
  if (schedule.availableSlots === 0) {
    return 'full'
  } else if (schedule.availableSlots > 0) {
    return 'normal'
  }
  
  return 'normal'
}

const getScheduleStatusType = (status) => {
  const statusMap = {
    '正常': 'success',
    '已满': 'warning',
    '取消': 'danger'
  }
  return statusMap[status] || 'info'
}

const getPatientStatusType = (status) => {
  const statusMap = {
    '待就诊': 'warning',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
}

// 事件处理方法
const handleViewModeChange = (mode) => {
  viewMode.value = mode
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
    const last = new Date(year, month + 1, 0)
    selectedMonth.value = new Date(first)
    filterStartDate.value = new Date(first)
    filterEndDate.value = new Date(last)
  }
  loadSchedules()
}



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
  const d = new Date(fullDateStr)
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
    loadPatientsForSchedule(schedule.id)
  }
}

const startConsultation = (patient) => {
  ElMessage.success(`开始为患者 ${patient.name} 就诊`)
  // TODO: 实现开始就诊逻辑
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.filters-bar {
  padding: 10px 0 20px 0;
}

.view-toggle {
  margin-right: 10px;
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

.schedule-item.full {
  background-color: #fdf6ec;
  border: 1px solid #f5dab1;
  color: #b88230;
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

.patients-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
/* 月视图：跨月淡化、筛选范围淡黄色、日期布局与标签间距 */
.month-view .schedule-cell.outside { background-color: #fafafa; color: #c0c4cc; }
.month-view .schedule-cell.inRange { background-color: #fff7e6; }
.month-day { display: flex; justify-content: space-between; }
.slot-chip { margin: 2px; }
</style>