<template>
  <div class="doctor-schedule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的排班</span>
          <div class="header-actions">
            <el-date-picker
              v-model="selectedWeek"
              type="week"
              placeholder="选择周"
              @change="handleWeekChange"
            />
          </div>
        </div>
      </template>

      <!-- 周视图 -->
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
            v-for="timeSlot in timeSlots"
            :key="timeSlot.time"
            class="time-row"
          >
            <div class="time-column">{{ timeSlot.time }}</div>
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
                  {{ getScheduleForCell(day.date, timeSlot.time).department }}
                </div>
                <div class="schedule-room">
                  {{ getScheduleForCell(day.date, timeSlot.time).room }}
                </div>
                <div class="schedule-patients">
                  {{ getScheduleForCell(day.date, timeSlot.time).patientCount || 0 }} 人
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
            {{ selectedSchedule.date }}
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ selectedSchedule.startTime }} - {{ selectedSchedule.endTime }}
          </el-descriptions-item>
          <el-descriptions-item label="科室">
            {{ selectedSchedule.department }}
          </el-descriptions-item>
          <el-descriptions-item label="诊室">
            {{ selectedSchedule.room }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getScheduleStatusType(selectedSchedule.status)">
              {{ selectedSchedule.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预约人数">
            {{ selectedSchedule.patientCount || 0 }} / {{ selectedSchedule.maxPatients || 20 }}
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

const selectedWeek = ref(new Date())
const scheduleDialogVisible = ref(false)
const selectedSchedule = ref(null)

// 时间段
const timeSlots = [
  { time: '08:00' },
  { time: '09:00' },
  { time: '10:00' },
  { time: '11:00' },
  { time: '14:00' },
  { time: '15:00' },
  { time: '16:00' },
  { time: '17:00' }
]

// 模拟排班数据
const schedules = ref([
  {
    id: 1,
    date: '2024-01-15',
    startTime: '08:00',
    endTime: '12:00',
    department: '内科',
    room: '诊室1',
    status: '正常',
    patientCount: 8,
    maxPatients: 20,
    patients: [
      { id: 1, name: '张三', phone: '138****1234', appointmentTime: '08:30', status: '已完成' },
      { id: 2, name: '李四', phone: '139****5678', appointmentTime: '09:00', status: '待就诊' },
      { id: 3, name: '王五', phone: '137****9012', appointmentTime: '09:30', status: '待就诊' }
    ]
  },
  {
    id: 2,
    date: '2024-01-15',
    startTime: '14:00',
    endTime: '18:00',
    department: '内科',
    room: '诊室1',
    status: '正常',
    patientCount: 5,
    maxPatients: 20,
    patients: []
  }
])

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

const getScheduleForCell = (date, time) => {
  const fullDate = `2024-${date}`
  return schedules.value.find(schedule => {
    const scheduleDate = schedule.date
    const startHour = parseInt(schedule.startTime.split(':')[0])
    const endHour = parseInt(schedule.endTime.split(':')[0])
    const cellHour = parseInt(time.split(':')[0])
    
    return scheduleDate === fullDate && cellHour >= startHour && cellHour < endHour
  })
}

const getScheduleStatusClass = (schedule) => {
  const statusMap = {
    '正常': 'normal',
    '已满': 'full',
    '取消': 'cancelled'
  }
  return statusMap[schedule.status] || 'normal'
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

const handleWeekChange = (date) => {
  // TODO: 加载新周的排班数据
  console.log('切换到周:', date)
}

const handleCellClick = (date, time) => {
  const schedule = getScheduleForCell(date, time)
  if (schedule) {
    selectedSchedule.value = schedule
    scheduleDialogVisible.value = true
  }
}

const startConsultation = (patient) => {
  ElMessage.success(`开始为患者 ${patient.name} 就诊`)
  // TODO: 实现开始就诊逻辑
}

onMounted(() => {
  // TODO: 加载排班数据
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