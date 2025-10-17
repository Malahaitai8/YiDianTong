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
              <div class="schedule-time">
                {{ schedule.startTime }} - {{ schedule.endTime }}
              </div>
              <div class="schedule-info">
                <div class="schedule-department">{{ schedule.department }}</div>
                <div class="schedule-room">{{ schedule.room }}</div>
              </div>
              <div class="schedule-status">
                <el-tag :type="getScheduleStatusType(schedule.status)">
                  {{ schedule.status }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 待就诊患者 -->
      <el-col :span="12">
        <el-card class="patients-card">
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
          
          <div v-else class="patients-list">
            <div
              v-for="patient in waitingPatients"
              :key="patient.id"
              class="patient-item"
            >
              <div class="patient-info">
                <div class="patient-name">{{ patient.name }}</div>
                <div class="patient-time">预约时间：{{ patient.appointmentTime }}</div>
              </div>
              <div class="patient-actions">
                <el-button type="primary" size="small" @click="startConsultation(patient)">
                  开始就诊
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const todayStats = reactive({
  appointments: 12,
  completed: 8,
  waiting: 4,
})

const monthStats = reactive({
  total: 156
})

// 今日排班
const todaySchedule = ref([
  {
    id: 1,
    startTime: '08:00',
    endTime: '12:00',
    department: '内科',
    room: '诊室1',
    status: '进行中'
  },
  {
    id: 2,
    startTime: '14:00',
    endTime: '18:00',
    department: '内科',
    room: '诊室1',
    status: '未开始'
  }
])

// 待就诊患者
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

const getScheduleStatusType = (status) => {
  const statusMap = {
    '进行中': 'success',
    '未开始': 'info',
    '已结束': 'info'
  }
  return statusMap[status] || 'info'
}

const viewSchedule = () => {
  router.push('/doctor/schedule')
}

const viewPatients = () => {
  router.push('/doctor/patients')
}

const startConsultation = (patient) => {
  ElMessage.success(`开始为患者 ${patient.name} 就诊`)
  // TODO: 实现开始就诊逻辑
}

onMounted(() => {
  // TODO: 加载实际数据
})
</script>

<style scoped>
.doctor-dashboard {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
}

.welcome-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
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
  margin-top: 5px;
}

.content-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.schedule-list,
.patients-list {
  max-height: 300px;
  overflow-y: auto;
}

.schedule-item,
.patient-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.schedule-item:last-child,
.patient-item:last-child {
  border-bottom: none;
}

.schedule-time {
  font-weight: 600;
  color: #303133;
  min-width: 120px;
}

.schedule-info {
  flex: 1;
  margin-left: 15px;
}

.schedule-department {
  font-size: 14px;
  color: #303133;
}

.schedule-room {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.patient-info {
  flex: 1;
}

.patient-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.patient-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.empty-state {
  padding: 40px 0;
}
</style>