<template>
  <div class="admin-dashboard">
    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon doctors">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalDoctors }}</div>
              <div class="stat-label">在职医生</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon appointments">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.todayAppointments }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon patients">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalPatients }}</div>
              <div class="stat-label">注册患者</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.monthlyRevenue }}</div>
              <div class="stat-label">本月收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表和列表 -->
    <el-row :gutter="20" class="content-row">
      <!-- 预约趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>预约趋势</span>
              <el-select v-model="chartPeriod" size="small" style="width: 100px">
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
                <el-option label="本年" value="year" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon size="60" color="#dcdfe6"><TrendCharts /></el-icon>
              <p>预约趋势图表</p>
              <p class="chart-note">（此处可集成 ECharts 或其他图表库）</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 科室统计 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>科室预约统计</span>
          </template>
          <div class="department-stats">
            <div
              v-for="dept in departmentStats"
              :key="dept.name"
              class="dept-item"
            >
              <div class="dept-info">
                <div class="dept-name">{{ dept.name }}</div>
                <div class="dept-count">{{ dept.appointments }} 人次</div>
              </div>
              <div class="dept-progress">
                <el-progress
                  :percentage="dept.percentage"
                  :color="dept.color"
                  :show-text="false"
                />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日概况 -->
    <el-row :gutter="20" class="content-row">
      <!-- 今日排班 -->
      <el-col :span="12">
        <el-card class="schedule-card">
          <template #header>
            <div class="card-header">
              <span>今日排班</span>
              <el-button type="primary" size="small" @click="manageSchedule">
                管理排班
              </el-button>
            </div>
          </template>
          
          <div class="schedule-list">
            <div
              v-for="schedule in todaySchedules"
              :key="schedule.id"
              class="schedule-item"
            >
              <div class="schedule-doctor">
                <div class="doctor-name">{{ schedule.doctorName }}</div>
                <div class="doctor-dept">{{ schedule.department }}</div>
              </div>
              <div class="schedule-time">
                {{ schedule.startTime }} - {{ schedule.endTime }}
              </div>
              <div class="schedule-room">{{ schedule.room }}</div>
              <div class="schedule-patients">
                {{ schedule.currentPatients }}/{{ schedule.maxPatients }}
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

      <!-- 系统通知 -->
      <el-col :span="12">
        <el-card class="notice-card">
          <template #header>
            <div class="card-header">
              <span>系统通知</span>
              <el-button type="primary" size="small" @click="manageNotices">
                管理通知
              </el-button>
            </div>
          </template>
          
          <div class="notice-list">
            <div
              v-for="notice in systemNotices"
              :key="notice.id"
              class="notice-item"
            >
              <div class="notice-icon">
                <el-icon :color="getNoticeIconColor(notice.type)">
                  <component :is="getNoticeIcon(notice.type)" />
                </el-icon>
              </div>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
              <div class="notice-status">
                <el-tag v-if="notice.isNew" type="danger" size="small">新</el-tag>
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

const router = useRouter()
const chartPeriod = ref('week')

// 统计数据
const stats = reactive({
  totalDoctors: 25,
  todayAppointments: 156,
  totalPatients: 1248,
  monthlyRevenue: '12.5万'
})

// 科室统计
const departmentStats = ref([
  { name: '内科', appointments: 45, percentage: 80, color: '#409eff' },
  { name: '外科', appointments: 32, percentage: 60, color: '#67c23a' },
  { name: '儿科', appointments: 28, percentage: 50, color: '#e6a23c' },
  { name: '妇科', appointments: 25, percentage: 45, color: '#f56c6c' },
  { name: '骨科', appointments: 20, percentage: 35, color: '#909399' }
])

// 今日排班
const todaySchedules = ref([
  {
    id: 1,
    doctorName: '张医生',
    department: '内科',
    startTime: '08:00',
    endTime: '12:00',
    room: '诊室1',
    currentPatients: 8,
    maxPatients: 20,
    status: '进行中'
  },
  {
    id: 2,
    doctorName: '李医生',
    department: '外科',
    startTime: '09:00',
    endTime: '17:00',
    room: '诊室2',
    currentPatients: 5,
    maxPatients: 15,
    status: '正常'
  },
  {
    id: 3,
    doctorName: '王医生',
    department: '儿科',
    startTime: '14:00',
    endTime: '18:00',
    room: '诊室3',
    currentPatients: 12,
    maxPatients: 12,
    status: '已满'
  }
])

// 系统通知
const systemNotices = ref([
  {
    id: 1,
    type: 'warning',
    title: '系统维护通知：明日凌晨2:00-4:00进行系统维护',
    time: '2小时前',
    isNew: true
  },
  {
    id: 2,
    type: 'info',
    title: '新增医生：李医生已加入外科团队',
    time: '1天前',
    isNew: false
  },
  {
    id: 3,
    type: 'success',
    title: '系统更新：预约系统已升级到v2.1版本',
    time: '3天前',
    isNew: false
  }
])

const getScheduleStatusType = (status) => {
  const statusMap = {
    '进行中': 'success',
    '正常': 'primary',
    '已满': 'warning',
    '已结束': 'info'
  }
  return statusMap[status] || 'info'
}

const getNoticeIcon = (type) => {
  const iconMap = {
    'warning': 'Warning',
    'info': 'InfoFilled',
    'success': 'SuccessFilled',
    'error': 'CircleCloseFilled'
  }
  return iconMap[type] || 'InfoFilled'
}

const getNoticeIconColor = (type) => {
  const colorMap = {
    'warning': '#e6a23c',
    'info': '#409eff',
    'success': '#67c23a',
    'error': '#f56c6c'
  }
  return colorMap[type] || '#409eff'
}

const manageSchedule = () => {
  router.push('/admin/schedule')
}

const manageNotices = () => {
  // TODO: 打开通知管理页面
}

onMounted(() => {
  // TODO: 加载实际数据
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
  color: white;
}

.stat-icon.doctors {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.appointments {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.patients {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.content-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-placeholder p {
  margin: 10px 0 5px 0;
  font-size: 16px;
}

.chart-note {
  font-size: 12px !important;
  color: #c0c4cc !important;
}

.department-stats {
  padding: 10px 0;
}

.dept-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.dept-item:last-child {
  margin-bottom: 0;
}

.dept-info {
  width: 120px;
}

.dept-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.dept-count {
  font-size: 12px;
  color: #909399;
}

.dept-progress {
  flex: 1;
  margin-left: 20px;
}

.schedule-list,
.notice-list {
  max-height: 320px;
  overflow-y: auto;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-doctor {
  width: 100px;
}

.doctor-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.doctor-dept {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.schedule-time {
  width: 120px;
  font-size: 13px;
  color: #606266;
}

.schedule-room {
  width: 80px;
  font-size: 13px;
  color: #606266;
}

.schedule-patients {
  width: 60px;
  font-size: 13px;
  color: #606266;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-icon {
  width: 40px;
  display: flex;
  justify-content: center;
}

.notice-content {
  flex: 1;
  margin-left: 10px;
}

.notice-title {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 5px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.notice-status {
  width: 40px;
  display: flex;
  justify-content: center;
}
</style>