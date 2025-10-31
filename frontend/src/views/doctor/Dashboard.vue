<template>
  <div class="doctor-dashboard">
    <!-- 欢迎信息 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.user?.username || '医生' }}！</h2>
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
      <el-row :gutter="20" class="stats-row">
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
                <div class="schedule-time">{{ schedule.time }}</div>
                <div class="schedule-info">
                  <div class="schedule-clinic">{{ schedule.clinic }}</div>
                  <div class="schedule-status" :class="schedule.status">
                    {{ getStatusText(schedule.status) }}
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
                  <div class="patient-name">{{ patient.name }}</div>
                  <div class="patient-time">预约时间：{{ patient.appointmentTime }}</div>
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
import { submitDoctorChangeRequest } from '@/api/doctor'
import { getClinicList } from '@/api/clinic'

const router = useRouter()
const userStore = useUserStore()

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
  appointments: 8,
  completed: 5,
  waiting: 3
})

const monthStats = reactive({
  total: 156
})

// 今日排班数据
const todaySchedule = ref([
  {
    id: 1,
    time: '08:00-12:00',
    clinic: '内科门诊',
    status: 'active'
  },
  {
    id: 2,
    time: '14:00-18:00',
    clinic: '专家门诊',
    status: 'upcoming'
  }
])

// 待就诊患者数据
const waitingPatients = ref([
  {
    id: 1,
    name: '张三',
    appointmentTime: '09:30'
  },
  {
    id: 2,
    name: '李四',
    appointmentTime: '10:00'
  },
  {
    id: 3,
    name: '王五',
    appointmentTime: '10:30'
  }
])

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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    active: '进行中',
    upcoming: '即将开始',
    completed: '已完成'
  }
  return statusMap[status] || status
}

// 查看排班
const viewSchedule = () => {
  router.push('/doctor/schedule')
}

// 查看患者
const viewPatients = () => {
  router.push('/doctor/patients')
}

// 开始就诊
const startConsultation = (patient) => {
  ElMessage.success(`开始为患者 ${patient.name} 就诊`)
  // TODO: 跳转到就诊页面
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
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