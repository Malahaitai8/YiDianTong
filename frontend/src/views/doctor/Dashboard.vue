<template>
  <div class="doctor-dashboard">
    <!-- 欢迎信息 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ doctorName || '医生' }}！</h2>
          <p>今天是 {{ formatDate(new Date(), 'YYYY年MM月DD日') }}，祝您工作愉快！</p>
        </div>
        <div class="welcome-icon">
          <el-icon size="60" color="#409EFF"><UserFilled /></el-icon>
        </div>
      </div>
    </el-card>

    <!-- 医生工作台 -->
    <div class="dashboard-content">
      <!-- 数据统计卡片 -->
      <el-row :gutter="20" class="stats-row" v-loading="dashboardLoading">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon today">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ todayStats.appointments }}</div>
                <div class="stat-label">今日预约</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon completed">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ todayStats.completed }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon waiting">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ todayStats.waiting }}</div>
                <div class="stat-label">待就诊</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ monthStats.total }}</div>
                <div class="stat-label">本月总计</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 今日排班和患者列表 -->
      <el-row :gutter="20" class="content-row">
        <!-- 今日排班 -->
        <el-col :span="12">
          <el-card class="schedule-card">
            <template #header>
              <div class="card-header">
                <span>今日排班</span>
                <el-button type="primary" size="small" @click="viewSchedule">
                  查看更多
                </el-button>
              </div>
            </template>
            
            <div v-if="todaySchedule.length === 0" class="empty-state">
              <el-empty description="今日无排班" />
            </div>
            
            <div v-else class="schedule-list">
              <div
                v-for="schedule in todaySchedule"
                :key="schedule.id"
                class="schedule-item"
              >
                <div class="schedule-time">{{ formatScheduleTime(schedule) }}</div>
                <div class="schedule-info">
                  <div class="schedule-clinic">{{ schedule.clinicName || schedule.clinic || '门诊' }}</div>
                  <div class="schedule-status" :class="getScheduleStatusClass(schedule)">
                    {{ getScheduleStatusText(schedule) }}
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 待就诊患者 -->
        <el-col :span="12">
          <el-card class="patient-card">
            <template #header>
              <div class="card-header">
                <span>待就诊患者</span>
                <el-button type="primary" size="small" @click="viewPatients">
                  查看全部
                </el-button>
              </div>
            </template>
            
            <div v-if="waitingPatients.length === 0" class="empty-state">
              <el-empty description="暂无待就诊患者" />
            </div>
            
            <div v-else class="patient-list">
              <div
                v-for="patient in waitingPatients"
                :key="patient.id"
                class="patient-item"
              >
                <div class="patient-info">
                  <div class="patient-name">{{ patient.patientName || patient.name }}</div>
                  <div class="patient-time">预约时间：{{ formatAppointmentTime(patient) }}</div>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  @click="startConsultation(patient)"
                >
                  开始就诊
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Calendar, 
  Check, 
  Clock, 
  DataAnalysis, 
  UserFilled,
  CloseBold
} from '@element-plus/icons-vue'
import { formatDate } from '@/utils'
import { ElMessage } from 'element-plus'
import { submitDoctorChangeRequest, getMyInfo, getDoctorDashboard, getTodayPatients, getMySchedules } from '@/api/doctor'
import { getClinicList } from '@/api/clinic'

const router = useRouter()
const userStore = useUserStore()

// 医生信息
const doctorName = ref('')
const doctorInfo = ref(null)

// 医生信息表单
const doctorFormRef = ref()
const doctorForm = reactive({
  name: '',
  title: '',
  specialization: '',
  bio: '',
  clinicId: ''
})

const doctorFormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ],
  specialization: [
    { required: true, message: '请输入专业科室', trigger: 'blur' }
  ],
  clinicId: [
    { required: true, message: '请选择所属门诊', trigger: 'change' }
  ],
  bio: [
    { required: true, message: '请输入个人简介', trigger: 'blur' },
    { min: 10, message: '个人简介至少10个字符', trigger: 'blur' }
  ]
}

const submitting = ref(false)
const clinicList = ref([])

// 统计数据
const todayStats = reactive({
  appointments: 0,
  completed: 0,
  waiting: 0
})

const monthStats = reactive({
  total: 0
})

const dashboardLoading = ref(false)

// 今日排班数据
const todaySchedule = ref([])

// 待就诊患者数据
const waitingPatients = ref([])

// 加载门诊列表
const loadClinicList = async () => {
  try {
    const response = await getClinicList()
    if (response.code === '200') {
      clinicList.value = response.data || []
    }
  } catch (error) {
    console.error('加载门诊列表失败:', error)
  }
}

// 提交医生信息
const submitDoctorInfo = async () => {
  try {
    const valid = await doctorFormRef.value.validate()
    if (!valid) return
    
    submitting.value = true
    
    // 准备提交数据
    const submitData = {
      name: doctorForm.name,
      title: doctorForm.title,
      specialty: doctorForm.specialization,
      bio: doctorForm.bio,
      clinicId: doctorForm.clinicId
    }
    
    // 调用医生信息变更申请接口
    const response = await submitDoctorChangeRequest(submitData)
    
    if (response.code === '200') {
      ElMessage.success('信息提交成功，请等待管理员审核')
      // 更新用户状态为审核中
      userStore.user.status = 'under_review'
    } else {
      ElMessage.error(response.msg || '提交失败，请重试')
    }
  } catch (error) {
    console.error('提交医生信息失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  doctorFormRef.value?.resetFields()
}

// 重新申请
const resubmitApplication = () => {
  // 清空表单
  resetForm()
}

// 格式化排班时间显示
const formatScheduleTime = (schedule) => {
  if (!schedule?.timeSlot) return '-'
  const slot = schedule.timeSlot.toUpperCase()
  const labelMap = {
    'MORNING': '上午',
    '上午': '上午',
    'AFTERNOON': '下午',
    '下午': '下午',
    'EVENING': '晚上',
    '晚间': '晚上',
    '晚上': '晚上'
  }
  if (labelMap[slot]) return labelMap[slot]
  return labelMap[schedule.timeSlot] || schedule.timeSlot || '-'
}

// 获取排班状态类名
const getScheduleStatusClass = (schedule) => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  
  // 判断时间段是否已开始或结束
  const timeSlot = schedule.timeSlot?.toUpperCase() || ''
  const hour = now.getHours()
  
  if (timeSlot.includes('MORNING') || timeSlot === '上午') {
    if (hour >= 8 && hour < 12) return 'active'
    if (hour < 8) return 'upcoming'
    return 'completed'
  } else if (timeSlot.includes('AFTERNOON') || timeSlot === '下午') {
    if (hour >= 14 && hour < 18) return 'active'
    if (hour < 14) return 'upcoming'
    return 'completed'
  } else if (timeSlot.includes('EVENING') || timeSlot === '晚间' || timeSlot === '晚上') {
    if (hour >= 18 && hour < 22) return 'active'
    if (hour < 18) return 'upcoming'
    return 'completed'
  }
  
  return 'upcoming'
}

// 获取排班状态文本
const getScheduleStatusText = (schedule) => {
  const statusClass = getScheduleStatusClass(schedule)
  const statusMap = {
    active: '进行中',
    upcoming: '即将开始',
    completed: '已完成'
  }
  return statusMap[statusClass] || '即将开始'
}

// 查看排班
const viewSchedule = () => {
  router.push('/doctor/schedule')
}

// 查看患者
const viewPatients = () => {
  router.push('/doctor/patients')
}

// 格式化预约时间显示
const formatAppointmentTime = (patient) => {
  if (!patient) return '-'
  const timeStr = patient.appointmentTime
  const scheduleDate = patient.scheduleDate
  if (timeStr) {
    // 完整时间戳，保留日期和到分钟
    if (timeStr.includes(' ')) {
      return timeStr.slice(0, 16)
    }
    if (scheduleDate) {
      return `${scheduleDate} ${timeStr}`
    }
    return timeStr
  }
  return scheduleDate || '-'
}

// 开始就诊
const startConsultation = (patient) => {
  const patientName = patient.patientName || patient.name
  ElMessage.success(`开始为患者 ${patientName} 就诊`)
  // TODO: 跳转到就诊页面
}

// 加载医生信息
const loadDoctorInfo = async () => {
  try {
    const resp = await getMyInfo()
    doctorInfo.value = resp?.data || null
    doctorName.value = doctorInfo.value?.name || userStore.user?.username || '医生'
  } catch (error) {
    console.error('获取医生信息失败:', error)
    doctorName.value = userStore.user?.username || '医生'
  }
}

// 加载今日排班
const loadTodaySchedule = async () => {
  try {
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    
    const resp = await getMySchedules({
      startDate: todayStr,
      endDate: todayStr
    })
    
    const schedules = resp?.data?.schedules || resp?.data || []
    
    // 获取医生信息中的门诊名称
    const clinicName = doctorInfo.value?.clinic?.name || doctorInfo.value?.clinicName || '门诊'
    
    // 格式化今日排班数据
    todaySchedule.value = schedules.map(schedule => ({
      id: schedule.id,
      scheduleDate: schedule.scheduleDate,
      timeSlot: schedule.timeSlot,
      slotType: schedule.slotType,
      totalSlots: schedule.totalSlots,
      availableSlots: schedule.availableSlots,
      clinicName: clinicName
    }))
    
    // 按时间段排序
    todaySchedule.value.sort((a, b) => {
      const orderMap = { 'MORNING': 1, 'morning': 1, '上午': 1, 'AFTERNOON': 2, 'afternoon': 2, '下午': 2, 'EVENING': 3, 'evening': 3, '晚间': 3, '晚上': 3 }
      const orderA = orderMap[a.timeSlot] || 99
      const orderB = orderMap[b.timeSlot] || 99
      return orderA - orderB
    })
  } catch (error) {
    console.error('加载今日排班失败:', error)
    todaySchedule.value = []
  }
}

// 加载待就诊患者
const loadWaitingPatients = async () => {
  try {
    // 获取今日患者列表，筛选出待就诊的患者（状态为 scheduled）
    const todayPatientsResp = await getTodayPatients()
    const todayPatientsData = todayPatientsResp?.data || {}
    const todayPatientsList = todayPatientsData.patients || todayPatientsData.data || []
    
    // 筛选待就诊患者（状态为 scheduled 或 SCHEDULED）
    const waitingList = Array.isArray(todayPatientsList) 
      ? todayPatientsList.filter(p => 
          p.status === 'scheduled' || p.status === 'SCHEDULED'
        )
      : []
    
    // 格式化待就诊患者数据
    waitingPatients.value = waitingList.map(patient => ({
      id: patient.appointmentId || patient.id,
      patientId: patient.patientId,
      patientName: patient.patientName || patient.name,
      appointmentTime: patient.appointmentTime,
      scheduleDate: patient.scheduleDate,
      status: patient.status
    }))
    
    // 按预约时间排序（最早在前）
    waitingPatients.value.sort((a, b) => {
      const parseTime = (item) => {
        if (!item) return 0
        const raw = item.appointmentTime
        const datePart = item.scheduleDate
        let dateTimeStr = ''
        if (raw) {
          dateTimeStr = raw.includes(' ') ? raw : (datePart ? `${datePart} ${raw}` : raw)
        } else if (datePart) {
          dateTimeStr = `${datePart} 00:00`
        } else {
          return 0
        }
        const timestamp = Date.parse(dateTimeStr.replace(/-/g, '/'))
        return Number.isNaN(timestamp) ? 0 : timestamp
      }
      return parseTime(a) - parseTime(b)
    })
  } catch (error) {
    console.error('加载待就诊患者失败:', error)
    waitingPatients.value = []
  }
}

// 加载Dashboard数据
const loadDashboardData = async () => {
  dashboardLoading.value = true
  try {
    // 获取今日患者列表，用于统计今日预约、已完成、待就诊
    const todayPatientsResp = await getTodayPatients()
    const todayPatientsData = todayPatientsResp?.data || {}
    const todayPatientsList = todayPatientsData.patients || todayPatientsData.data || []
    
    // 统计今日预约数据
    if (Array.isArray(todayPatientsList)) {
      todayStats.appointments = todayPatientsList.length
      todayStats.completed = todayPatientsList.filter(p => 
        p.status === 'completed' || p.status === 'COMPLETED'
      ).length
      todayStats.waiting = todayPatientsList.filter(p => 
        p.status === 'scheduled' || p.status === 'SCHEDULED'
      ).length
    } else {
      // 如果数据格式不对，使用默认值
      todayStats.appointments = 0
      todayStats.completed = 0
      todayStats.waiting = 0
    }
    
    // 获取Dashboard统计数据（本月总计）
    const dashboardResp = await getDoctorDashboard()
    const dashboardData = dashboardResp?.data || {}
    monthStats.total = dashboardData.monthlyTotal || 0
    
    // 加载待就诊患者
    await loadWaitingPatients()
  } catch (error) {
    console.error('加载Dashboard数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    dashboardLoading.value = false
  }
}

onMounted(async () => {
  // 先加载医生信息
  await loadDoctorInfo()
  // 然后加载Dashboard数据和今日排班
  await loadDashboardData()
  // 确保医生信息加载后再加载排班（因为需要门诊名称）
  if (doctorInfo.value) {
    await loadTodaySchedule()
  }
})
</script>

<style scoped>
.doctor-dashboard {
  padding: 20px;
}

/* 欢迎卡片样式 */
.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}



/* 统计卡片样式 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.today {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.waiting {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.total {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 内容行样式 */
.content-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 排班卡片样式 */
.schedule-list {
  max-height: 300px;
  overflow-y: auto;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-time {
  font-weight: 600;
  color: #409EFF;
  margin-right: 15px;
  min-width: 100px;
}

.schedule-info {
  flex: 1;
}

.schedule-clinic {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.schedule-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
}

.schedule-status.active {
  background: #e1f3d8;
  color: #67c23a;
}

.schedule-status.upcoming {
  background: #fdf6ec;
  color: #e6a23c;
}

.schedule-status.completed {
  background: #f0f9ff;
  color: #409eff;
}

/* 患者列表样式 */
.patient-list {
  max-height: 300px;
  overflow-y: auto;
}

.patient-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.patient-info {
  flex: 1;
  text-align: left;
}

.patient-item:last-child {
  border-bottom: none;
}

.patient-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.patient-time {
  font-size: 12px;
  color: #909399;
}

/* 空状态样式 */
.empty-state {
  padding: 40px 0;
}
</style>