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
            </el-radio-group>
            
            <!-- 本周排班 -->
            <span class="time-range-label">本周排班</span>
            
            <!-- 日期选择器 -->
            <el-date-picker
              v-if="viewMode === 'week'"
              v-model="selectedWeek"
              type="week"
              placeholder="选择周"
              @change="handleWeekChange"
            />
            <el-date-picker
              v-else
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              @change="handleDateChange"
            />
          </div>
        </div>
      </template>

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
            v-for="timeSlot in timeSlots"
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
      <div v-else class="schedule-calendar">
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
            v-for="timeSlot in timeSlots"
            :key="timeSlot.time"
            class="time-row"
          >
            <div class="time-column">{{ timeSlot.label }}</div>
            <div
              v-for="day in weekDays"
              :key="`${day.date}-${timeSlot.time}`"
              class="schedule-cell"
              @click="handleCellClick(day.date, timeSlot.time)"
            >
              <div
                v-if="getScheduleForCell(day.date, timeSlot.time)"
                class="schedule-item"
                :class="getScheduleStatusClass(getScheduleForCell(day.date, timeSlot.time))"
              >
                <div class="schedule-title">
                  {{ formatTime(getScheduleForCell(day.date, timeSlot.time).timeSlot) }}
                </div>
                <div class="schedule-room">
                  {{ getScheduleForCell(day.date, timeSlot.time).slotType || '普通门诊' }}
                </div>
                <div class="schedule-patients">
                  {{ (getScheduleForCell(day.date, timeSlot.time).totalSlots - getScheduleForCell(day.date, timeSlot.time).availableSlots) || 0 }} / {{ getScheduleForCell(day.date, timeSlot.time).totalSlots || 0 }}
                </div>
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

// 视图模式
const viewMode = ref('week') // 'day' 或 'week'
const selectedWeek = ref(new Date())
const selectedDate = ref(new Date())
const scheduleDialogVisible = ref(false)
const selectedSchedule = ref(null)

// 获取当前用户信息
const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const doctorId = currentUser.id

// 时间段 - 只有上午和下午
const timeSlots = [
  { time: 'MORNING', label: '上午' },
  { time: 'AFTERNOON', label: '下午' }
]

// 排班数据
const schedules = ref([])
// 缓存所有排班数据
const allSchedulesCache = ref([])

// 计算本周日期
const weekDays = computed(() => {
  const days = []
  const startOfWeek = new Date(selectedWeek.value)
  const day = startOfWeek.getDay()
  const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1) // 调整为周一开始
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek.setDate(diff + i))
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

// 加载排班数据
const loadSchedules = async (forceReload = false) => {
  try {
    // 如果有缓存数据且不强制重新加载，直接使用缓存
    if (allSchedulesCache.value.length > 0 && !forceReload) {
      console.log('使用缓存数据...')
      const filteredSchedules = filterSchedulesByWeek(allSchedulesCache.value)
      schedules.value = filteredSchedules
      console.log('过滤后的排班数据:', schedules.value)
      return
    }
    
    console.log('开始加载排班数据...')
    const response = await request.get('/schedule/week')
    console.log('API响应:', response)
    
    if (response.data && response.data.length >= 0) {
      // 缓存所有排班数据
      allSchedulesCache.value = response.data || []
      console.log('所有排班数据已缓存:', allSchedulesCache.value)
      
      // 根据选择的周期过滤数据
      const filteredSchedules = filterSchedulesByWeek(allSchedulesCache.value)
      schedules.value = filteredSchedules
      console.log('过滤后的排班数据:', schedules.value)
    } else {
      console.log('响应格式异常:', response)
      ElMessage.error('加载排班数据失败')
    }
  } catch (error) {
    console.error('加载排班数据失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else {
      ElMessage.error('加载排班数据失败: ' + (error.message || '网络错误'))
    }
  }
}

// 根据选择的周期过滤排班数据
const filterSchedulesByWeek = (allSchedules) => {
  // 获取选择周的开始和结束日期
  const startOfWeek = new Date(selectedWeek.value)
  const day = startOfWeek.getDay()
  const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1) // 调整为周一开始
  startOfWeek.setDate(diff)
  startOfWeek.setHours(0, 0, 0, 0)
  
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 6)
  endOfWeek.setHours(23, 59, 59, 999)
  
  console.log('选择的周期:', formatDate(startOfWeek), '到', formatDate(endOfWeek))
  
  // 过滤在选择周期内的排班数据
  const filteredData = allSchedules.filter(schedule => {
    const scheduleDate = new Date(schedule.scheduleDate)
    const isInRange = scheduleDate >= startOfWeek && scheduleDate <= endOfWeek
    console.log('排班日期:', formatDate(scheduleDate), '是否在范围内:', isInRange)
    return isInRange
  })
  
  // 如果没有数据，显示提示信息
  if (filteredData.length === 0 && allSchedules.length > 0) {
    // 计算实际数据的日期范围
    const dates = allSchedules.map(s => new Date(s.scheduleDate))
    const minDate = new Date(Math.min(...dates))
    const maxDate = new Date(Math.max(...dates))
    
    ElMessage.info(`当前选择的周期 ${formatDate(startOfWeek)} 到 ${formatDate(endOfWeek)} 没有排班数据。\n实际数据范围：${formatDate(minDate)} 到 ${formatDate(maxDate)}`)
  }
  
  return filteredData
}





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
    return scheduleDate === dateStr && schedule.timeSlot === timeSlot
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
  if (timeSlot === 'MORNING') {
    return '上午';
  } else if (timeSlot === 'AFTERNOON') {
    return '下午';
  }
  return timeSlot;
}

const getScheduleForCell = (date, timeSlot) => {
  const fullDate = `2024-${date}` // date格式为 MM-DD
  
  return schedules.value.find(schedule => {
    const scheduleDate = formatDate(new Date(schedule.scheduleDate))
    return scheduleDate === fullDate && schedule.timeSlot === timeSlot
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
  loadSchedules()
}



const handleWeekChange = (date) => {
  selectedWeek.value = date
  // 使用缓存数据，不重新请求API
  loadSchedules(false)
}

const handleDateChange = (date) => {
  selectedDate.value = date
  loadSchedules()
}

const handleCellClick = (date, timeSlot) => {
  const schedule = getScheduleForCell(date, timeSlot)
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
</style>