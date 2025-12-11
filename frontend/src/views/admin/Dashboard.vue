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

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions-row">
      <el-col :span="24">
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="router.push('/admin/schedule')" class="action-btn">
              <el-icon><Calendar /></el-icon>
              <span>排班管理</span>
            </el-button>
            <el-button type="success" @click="router.push('/admin/schedule-rules')" class="action-btn">
              <el-icon><Setting /></el-icon>
              <span>排班规则</span>
            </el-button>
            <el-button type="warning" @click="router.push('/admin/appointments')" class="action-btn">
              <el-icon><Document /></el-icon>
              <span>预约管理</span>
            </el-button>
            <el-button type="info" @click="router.push('/admin/doctors')" class="action-btn">
              <el-icon><UserFilled /></el-icon>
              <span>医生管理</span>
            </el-button>
            <el-button type="primary" plain @click="router.push('/admin/patients')" class="action-btn">
              <el-icon><User /></el-icon>
              <span>患者管理</span>
            </el-button>
            <el-button type="success" plain @click="router.push('/admin/reports')" class="action-btn">
              <el-icon><DataAnalysis /></el-icon>
              <span>报表统计</span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日概况 -->
    <el-row :gutter="20" class="content-row">
      <!-- 待办事项 -->
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-tag type="danger" size="small">{{ todoList.length }}</el-tag>
            </div>
          </template>
          
          <div class="todo-list">
            <el-empty v-if="todoList.length === 0" description="暂无待办事项" :image-size="80" />
            <div
              v-for="todo in todoList"
              :key="todo.id"
              class="todo-item"
            >
              <div class="todo-icon">
                <el-icon :color="getTodoIconColor(todo.type)" :size="20">
                  <component :is="getTodoIcon(todo.type)" />
                </el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-desc">{{ todo.description }}</div>
              </div>
              <div class="todo-action">
                <el-button type="primary" size="small" text @click="handleTodo(todo)">
                  处理
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 当前排班 -->
      <el-col :span="12">
        <el-card class="schedule-card">
          <template #header>
            <div class="card-header">
              <span>当前排班</span>
              <el-tag type="success" size="small">{{ currentSchedules.length }}个</el-tag>
            </div>
          </template>
          
          <div class="schedule-list-container">
            <el-empty v-if="currentSchedules.length === 0" description="暂无进行中的排班" :image-size="80" />
            <div
              v-for="schedule in currentSchedules"
              :key="schedule.id"
              class="schedule-item-card"
            >
              <div class="schedule-doctor-info">
                <div class="doctor-avatar">
                  <el-icon><UserFilled /></el-icon>
                </div>
                <div class="doctor-details">
                  <div class="doctor-name-title">{{ schedule.doctorName }}</div>
                  <div class="doctor-dept">{{ schedule.departmentName }}</div>
                </div>
              </div>
              <div class="schedule-info">
                <div class="schedule-time">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTimeSlot(schedule.timeSlot) }}</span>
                </div>
                <div class="schedule-slots">
                  <el-tag :type="getSlotTagType(schedule)" size="small">
                    {{ schedule.availableSlots }}/{{ schedule.totalSlots }}
                  </el-tag>
                </div>
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
import { ElMessage } from 'element-plus'
import { getDoctorList } from '@/api/doctor'
import { getPatientList } from '@/api/patient'
import { getAppointmentStats, getRevenueStats } from '@/api/statistics'
import { getPendingApplicationRequests } from '@/api/applicationRequest'
import { getScheduleList } from '@/api/schedule'

const router = useRouter()

// 统计数据
const stats = reactive({
  // ...
  totalDoctors: 0,
  todayAppointments: 0,
  totalPatients: 0,
  monthlyRevenue: '0'
})

// 加载统计数据
const loadStats = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const now = new Date()
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
    const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0]
    
    // 并行加载所有统计数据
    const [doctorsRes, patientsRes, todayAppointmentsRes, monthlyRevenueRes] = await Promise.all([
      getDoctorList().catch(() => ({ data: [] })),
      getPatientList().catch(() => ({ data: [] })),
      getAppointmentStats({ startDate: today, endDate: today }).catch(() => ({ data: {} })),
      getRevenueStats({ startDate: monthStart, endDate: monthEnd }).catch(() => ({ data: {} }))
    ])
    
    // 统计医生数量
    const doctorList = Array.isArray(doctorsRes?.data) ? doctorsRes.data : (doctorsRes?.data?.list || [])
    stats.totalDoctors = doctorList.length
    
    // 统计患者数量
    const patientList = Array.isArray(patientsRes?.data) ? patientsRes.data : (patientsRes?.data?.list || [])
    stats.totalPatients = patientList.length
    
    // 今日预约数量（从统计接口获取，返回的是列表，需要汇总）
    const todayAppointmentsList = Array.isArray(todayAppointmentsRes?.data) ? todayAppointmentsRes.data : []
    stats.todayAppointments = todayAppointmentsList.reduce((sum, item) => sum + (item.totalAppointments || 0), 0)
    
    // 本月收入（从统计接口获取，返回的是列表，需要汇总）
    const monthlyRevenueList = Array.isArray(monthlyRevenueRes?.data) ? monthlyRevenueRes.data : []
    const monthlyRevenue = monthlyRevenueList.reduce((sum, item) => {
      const revenue = item.totalRevenue || item.actualFee || 0
      return sum + Number(revenue)
    }, 0)
    
    if (monthlyRevenue >= 10000) {
      stats.monthlyRevenue = (monthlyRevenue / 10000).toFixed(1) + '万'
    } else {
      stats.monthlyRevenue = monthlyRevenue.toFixed(2)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 待办事项
const todoList = ref([])

// 加载待办事项（待审核的申请）
const loadTodoList = async () => {
  try {
    const response = await getPendingApplicationRequests()
    const pendingRequests = Array.isArray(response?.data) ? response.data : []
    
    // 将待审核申请转换为待办事项格式
    todoList.value = pendingRequests.map(request => {
      let title = ''
      let description = ''
      let type = 'pending'
      
      // 根据申请类型设置标题和描述
      if (request.requestType === 'SCHEDULE_CHANGE') {
        title = '调班申请'
        type = 'urgent'
        
        // 调班类型说明
        const changeTypeMap = {
          'CANCEL': '取消排班',
          'RESCHEDULE': '调整时间',
          'ADJUST_SLOTS': '调整号源'
        }
        const changeTypeText = changeTypeMap[request.changeType] || '调班'
        description = `${request.applicantName || '医生'} - ${changeTypeText}`
        
      } else if (request.requestType === 'INFO_UPDATE') {
        title = '信息修改申请'
        type = 'review'
        
        // 字段名映射
        const fieldNameMap = {
          'name': '姓名',
          'title': '职称',
          'specialty': '专长',
          'bio': '简介',
          'phone': '电话',
          'email': '邮箱'
        }
        const fieldText = fieldNameMap[request.fieldName] || request.fieldName
        description = `${request.applicantName || '医生'} - 修改${fieldText}`
        
      } else {
        title = '待审核申请'
        type = 'pending'
        description = `${request.applicantName || '医生'} - ${request.reason || '待审核'}`
      }
      
      return {
        id: request.id,
        requestId: request.id,
        title: title,
        description: description,
        type: type,
        rawData: request
      }
    })
  } catch (error) {
    console.error('加载待办事项失败:', error)
  }
}

// 当前排班
const currentSchedules = ref([])

// 加载当前正在进行的排班
const loadCurrentSchedules = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const response = await getScheduleList({
      startDate: today,
      endDate: today
    })
    
    console.log('排班API返回数据:', response)
    
    const scheduleList = Array.isArray(response?.data) ? response.data : (response?.data?.list || [])
    
    console.log('解析后的排班列表:', scheduleList)
    
    // 打印第一个排班的所有字段，看看数据结构
    if (scheduleList.length > 0) {
      console.log('第一个排班的数据:', scheduleList[0])
      console.log('timeSlot字段值:', scheduleList[0].timeSlot)
    }
    
    // 筛选出当前正在进行的排班（根据时间段判断）
    const now = new Date()
    const currentHour = now.getHours()
    
    console.log('当前小时:', currentHour)
    
    let currentTimeSlot = ''
    if (currentHour >= 8 && currentHour < 12) {
      currentTimeSlot = 'morning'
    } else if (currentHour >= 14 && currentHour < 18) {
      currentTimeSlot = 'afternoon'
    } else if (currentHour >= 18 && currentHour < 21) {
      currentTimeSlot = 'evening'
    }
    
    console.log('当前时间段:', currentTimeSlot)
    
    // 如果不在工作时间段，显示所有今天的排班
    if (!currentTimeSlot) {
      console.log('当前不在工作时间，显示今天所有排班')
      currentSchedules.value = scheduleList
    } else {
      // 筛选当前时间段的排班
      const filtered = scheduleList.filter(schedule => {
        console.log(`比较: schedule.timeSlot="${schedule.timeSlot}" vs currentTimeSlot="${currentTimeSlot}"`)
        return schedule.timeSlot === currentTimeSlot
      })
      
      console.log('筛选后的排班:', filtered)
      
      // 如果当前时间段没有排班，显示今天所有排班
      if (filtered.length === 0) {
        console.log('当前时间段无排班，显示今天所有排班')
        currentSchedules.value = scheduleList
      } else {
        currentSchedules.value = filtered
      }
    }
  } catch (error) {
    console.error('加载当前排班失败:', error)
  }
}

// 根据号源情况返回标签类型
const getSlotTagType = (schedule) => {
  const ratio = schedule.availableSlots / schedule.totalSlots
  if (ratio > 0.5) return 'success'
  if (ratio > 0.2) return 'warning'
  return 'danger'
}

// 将英文时间段转换为中文
const formatTimeSlot = (timeSlot) => {
  const timeSlotMap = {
    'morning': '上午',
    'afternoon': '下午',
    'evening': '晚上'
  }
  return timeSlotMap[timeSlot] || timeSlot
}

const getTodoIcon = (type) => {
  const iconMap = {
    'pending': 'Clock',
    'urgent': 'Warning',
    'review': 'Document',
    'approval': 'CircleCheck'
  }
  return iconMap[type] || 'InfoFilled'
}

const getTodoIconColor = (type) => {
  const colorMap = {
    'pending': '#409eff',
    'urgent': '#f56c6c',
    'review': '#e6a23c',
    'approval': '#67c23a'
  }
  return colorMap[type] || '#909399'
}

const handleTodo = (todo) => {
  // 跳转到申请管理页面，并自动筛选为待审核状态
  router.push({
    path: '/admin/doctor-change',
    query: { status: 'PENDING' }
  })
}

onMounted(() => {
  loadStats()
  loadTodoList()
  loadCurrentSchedules()
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

/* 快捷操作样式 */
.quick-actions-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 0;
}

.action-btn {
  flex: 1;
  min-width: 140px;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
}

.action-btn .el-icon {
  font-size: 24px;
}

/* 待办事项样式 */
.todo-card,
.schedule-card {
  height: 450px;
}

.todo-card :deep(.el-card__body),
.schedule-card :deep(.el-card__body) {
  height: calc(100% - 56px);
  overflow-y: auto;
}

.todo-list {
  padding: 10px 0;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.todo-item:hover {
  background: #e8edf3;
  transform: translateX(4px);
}

.todo-icon {
  margin-right: 12px;
  margin-top: 2px;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.todo-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.todo-action {
  margin-left: 12px;
}

/* 当前排班样式 */
.schedule-list-container {
  padding: 10px 0;
}

.schedule-item-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.schedule-item-card:hover {
  background: #e8edf3;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.schedule-doctor-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.doctor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  margin-right: 12px;
}

.doctor-details {
  flex: 1;
}

.doctor-name-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.schedule-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.schedule-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.schedule-time .el-icon {
  font-size: 16px;
  color: #909399;
}

.schedule-slots {
  min-width: 60px;
  text-align: right;
}
</style>