<template>
  <div class="reports-page">
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="selectedDepartment"
            placeholder="选择科室"
            clearable
            @change="handleDepartmentChange"
          >
            <el-option label="全部科室" value="" />
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="儿科" value="儿科" />
            <el-option label="妇科" value="妇科" />
            <el-option label="骨科" value="骨科" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="reportType"
            placeholder="报表类型"
            @change="handleReportTypeChange"
          >
            <el-option label="预约统计" value="appointment" />
            <el-option label="医生工作量" value="doctor" />
            <el-option label="科室统计" value="department" />
            <el-option label="收入统计" value="revenue" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="generateReport">
            <el-icon><Search /></el-icon>
            生成报表
          </el-button>
          <el-button @click="exportReport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon appointments">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.totalAppointments }}</div>
              <div class="stat-label">总预约数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon completed">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.completedAppointments }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon cancelled">
              <el-icon><CloseBold /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.cancelledAppointments }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon noShow">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.noShowAppointments }}</div>
              <div class="stat-label">爽约</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 号源统计 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon slots">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.totalSlots }}</div>
              <div class="stat-label">总号源数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon available">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.availableSlots }}</div>
              <div class="stat-label">可用号源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon used">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ overview.usedSlots }}</div>
              <div class="stat-label">已用号源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rate">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ formatRate(overview.completionRate) }}%</div>
              <div class="stat-label">完成率</div>
              <div class="stat-sub-info">
                利用率: {{ formatRate(overview.utilization) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 预约趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>预约趋势</span>
              <el-select v-model="trendPeriod" size="small" style="width: 100px">
                <el-option label="日" value="day" />
                <el-option label="周" value="week" />
                <el-option label="月" value="month" />
              </el-select>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-placeholder">
              <el-icon size="60" color="#dcdfe6"><TrendCharts /></el-icon>
              <p>预约趋势图表</p>
              <div class="trend-data">
                <div class="trend-item" v-for="item in trendData" :key="item.date">
                  <span class="trend-date">{{ item.date }}</span>
                  <span class="trend-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 科室分布图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>科室预约分布</span>
          </template>
          <div class="chart-container">
            <div class="department-chart">
              <div
                v-for="dept in departmentData"
                :key="dept.name"
                class="dept-bar"
              >
                <div class="dept-info">
                  <span class="dept-name">{{ dept.name }}</span>
                  <span class="dept-count">{{ dept.count }}</span>
                </div>
                <div class="dept-progress">
                  <div
                    class="progress-bar"
                    :style="{ width: dept.percentage + '%', backgroundColor: dept.color }"
                  ></div>
                </div>
                <span class="dept-percentage">{{ dept.percentage }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>{{ getTableTitle() }}</span>
          <div>
            <el-button size="small" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button size="small" @click="exportTableData">
              <el-icon><Download /></el-icon>
              导出表格
            </el-button>
          </div>
        </div>
      </template>

      <!-- 预约统计表格 -->
      <el-table
        v-if="reportType === 'appointment'"
        :data="appointmentData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="doctor" label="医生" width="120" />
        <el-table-column prop="total" label="总预约" width="100" />
        <el-table-column prop="completed" label="已完成" width="100" />
        <el-table-column prop="cancelled" label="已取消" width="100" />
        <el-table-column prop="noShow" label="爽约" width="100" />
        <el-table-column label="完成率" width="100">
          <template #default="{ row }">
            <el-tag :type="getCompletionRateType(row.completionRate)">
              {{ row.completionRate }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="revenue" label="收入(元)" />
      </el-table>

      <!-- 医生工作量表格 -->
      <el-table
        v-if="reportType === 'doctor'"
        :data="doctorData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="name" label="医生姓名" width="120" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="workDays" label="工作天数" width="100" />
        <el-table-column prop="totalPatients" label="接诊患者" width="100" />
        <el-table-column prop="avgPatients" label="日均接诊" width="100" />
        <el-table-column prop="revenue" label="创收(元)" width="120" />
        <el-table-column label="患者满意度" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.satisfaction"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}分"
            />
          </template>
        </el-table-column>
        <el-table-column label="工作效率" width="100">
          <template #default="{ row }">
            <el-tag :type="getEfficiencyType(row.efficiency)">
              {{ row.efficiency }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 科室统计表格 -->
      <el-table
        v-if="reportType === 'department'"
        :data="departmentDetailData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="name" label="科室名称" width="120" />
        <el-table-column prop="doctorCount" label="医生数量" width="100" />
        <el-table-column prop="totalAppointments" label="总预约" width="100" />
        <el-table-column prop="completedAppointments" label="已完成" width="100" />
        <el-table-column prop="avgWaitTime" label="平均等待时间" width="120" />
        <el-table-column prop="revenue" label="科室收入(元)" width="120" />
        <el-table-column prop="patientSatisfaction" label="患者满意度" width="120" />
        <el-table-column label="科室效率" width="100">
          <template #default="{ row }">
            <el-progress
              :percentage="row.efficiency"
              :color="getEfficiencyColor(row.efficiency)"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 收入统计表格 -->
      <el-table
        v-if="reportType === 'revenue'"
        :data="revenueData"
        v-loading="loading"
        style="width: 100%"
        show-summary
        :summary-method="getRevenueSummary"
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="registrationFee" label="挂号费" width="100" />
        <el-table-column prop="consultationFee" label="诊疗费" width="100" />
        <el-table-column prop="medicationFee" label="药品费" width="100" />
        <el-table-column prop="examinationFee" label="检查费" width="100" />
        <el-table-column prop="totalRevenue" label="总收入" width="120" />
        <el-table-column label="收入占比" width="100">
          <template #default="{ row }">
            {{ row.revenuePercentage }}%
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  CircleCheckFilled,
  CloseBold,
  WarningFilled,
  Document,
  CircleCheck,
  UserFilled,
  TrendCharts,
  Search,
  Download,
  Refresh
} from '@element-plus/icons-vue'
import { getOverviewStats } from '@/api/statistics'

const loading = ref(false)
const dateRange = ref(['2024-01-01', '2024-01-31'])
const selectedDepartment = ref('')
const reportType = ref('appointment')
const trendPeriod = ref('day')

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 统计概览数据
const overview = reactive({
  totalAppointments: 0,
  completedAppointments: 0,
  cancelledAppointments: 0,
  noShowAppointments: 0,
  totalSlots: 0,
  availableSlots: 0,
  usedSlots: 0,
  completionRate: 0,
  utilization: 0
})

// 趋势数据
const trendData = ref([
  { date: '01-01', value: 45 },
  { date: '01-02', value: 52 },
  { date: '01-03', value: 38 },
  { date: '01-04', value: 67 },
  { date: '01-05', value: 59 },
  { date: '01-06', value: 73 },
  { date: '01-07', value: 81 }
])

// 科室分布数据
const departmentData = ref([
  { name: '内科', count: 356, percentage: 28, color: '#409eff' },
  { name: '外科', count: 298, percentage: 24, color: '#67c23a' },
  { name: '儿科', count: 245, percentage: 20, color: '#e6a23c' },
  { name: '妇科', count: 189, percentage: 15, color: '#f56c6c' },
  { name: '骨科', count: 168, percentage: 13, color: '#909399' }
])

// 预约统计数据
const appointmentData = ref([
  {
    date: '2024-01-15',
    department: '内科',
    doctor: '张医生',
    total: 20,
    completed: 18,
    cancelled: 1,
    noShow: 1,
    completionRate: 90,
    revenue: 1800
  },
  {
    date: '2024-01-15',
    department: '外科',
    doctor: '李医生',
    total: 15,
    completed: 14,
    cancelled: 0,
    noShow: 1,
    completionRate: 93,
    revenue: 2100
  }
])

// 医生工作量数据
const doctorData = ref([
  {
    name: '张医生',
    department: '内科',
    workDays: 22,
    totalPatients: 440,
    avgPatients: 20,
    revenue: 44000,
    satisfaction: 4.8,
    efficiency: '高效'
  },
  {
    name: '李医生',
    department: '外科',
    workDays: 20,
    totalPatients: 300,
    avgPatients: 15,
    revenue: 45000,
    satisfaction: 4.6,
    efficiency: '良好'
  }
])

// 科室详细数据
const departmentDetailData = ref([
  {
    name: '内科',
    doctorCount: 8,
    totalAppointments: 1200,
    completedAppointments: 1080,
    avgWaitTime: '15分钟',
    revenue: 120000,
    patientSatisfaction: '4.7分',
    efficiency: 85
  },
  {
    name: '外科',
    doctorCount: 6,
    totalAppointments: 800,
    completedAppointments: 750,
    avgWaitTime: '20分钟',
    revenue: 150000,
    patientSatisfaction: '4.5分',
    efficiency: 78
  }
])

// 收入统计数据
const revenueData = ref([
  {
    date: '2024-01-15',
    department: '内科',
    registrationFee: 500,
    consultationFee: 1200,
    medicationFee: 800,
    examinationFee: 600,
    totalRevenue: 3100,
    revenuePercentage: 25
  },
  {
    date: '2024-01-15',
    department: '外科',
    registrationFee: 400,
    consultationFee: 1500,
    medicationFee: 600,
    examinationFee: 1000,
    totalRevenue: 3500,
    revenuePercentage: 28
  }
])

const getTableTitle = () => {
  const titleMap = {
    'appointment': '预约统计详情',
    'doctor': '医生工作量统计',
    'department': '科室统计详情',
    'revenue': '收入统计详情'
  }
  return titleMap[reportType.value] || '统计详情'
}

const getCompletionRateType = (rate) => {
  if (rate >= 90) return 'success'
  if (rate >= 80) return 'warning'
  return 'danger'
}

const getEfficiencyType = (efficiency) => {
  const typeMap = {
    '高效': 'success',
    '良好': 'primary',
    '一般': 'warning',
    '较低': 'danger'
  }
  return typeMap[efficiency] || 'info'
}

const getEfficiencyColor = (efficiency) => {
  if (efficiency >= 80) return '#67c23a'
  if (efficiency >= 60) return '#e6a23c'
  return '#f56c6c'
}

const getRevenueSummary = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (index === 1) {
      sums[index] = ''
      return
    }
    
    const values = data.map(item => Number(item[column.property]))
    if (!values.every(value => isNaN(value))) {
      sums[index] = values.reduce((prev, curr) => {
        const value = Number(curr)
        if (!isNaN(value)) {
          return prev + curr
        } else {
          return prev
        }
      }, 0)
      if (column.property === 'revenuePercentage') {
        sums[index] = '100%'
      } else {
        sums[index] = `¥${sums[index]}`
      }
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const handleDateChange = () => {
  generateReport()
}

const handleDepartmentChange = () => {
  generateReport()
}

const handleReportTypeChange = () => {
  generateReport()
}

// 加载全局概览统计
const loadOverviewStats = async () => {
  try {
    const res = await getOverviewStats()
    const data = res?.data || {}
    overview.totalAppointments = data.totalAppointments || 0
    overview.completedAppointments = data.completedAppointments || 0
    overview.cancelledAppointments = data.cancelledAppointments || 0
    overview.noShowAppointments = data.noShowAppointments || 0
    overview.totalSlots = data.totalSlots || 0
    overview.availableSlots = data.availableSlots || 0
    overview.usedSlots = data.usedSlots || 0
    overview.completionRate = data.completionRate || 0
    overview.utilization = data.utilization || 0
  } catch (error) {
    console.error('加载概览统计失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 格式化百分比
const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '0.00'
  return (rate * 100).toFixed(2)
}

const generateReport = async () => {
  loading.value = true
  try {
    // TODO: 调用API生成报表
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据更新
    pagination.total = 50
    
    ElMessage.success('报表生成成功')
  } catch (error) {
    ElMessage.error('报表生成失败')
  } finally {
    loading.value = false
  }
}

const exportReport = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中')
}

const exportTableData = () => {
  // TODO: 实现表格导出功能
  ElMessage.info('表格导出功能开发中')
}

const resetFilters = () => {
  dateRange.value = ['2024-01-01', '2024-01-31']
  selectedDepartment.value = ''
  reportType.value = 'appointment'
  generateReport()
}

const refreshData = () => {
  loadOverviewStats()
  generateReport()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  generateReport()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  generateReport()
}

onMounted(() => {
  loadOverviewStats()
  generateReport()
})
</script>

<style scoped>
.reports-page {
  padding: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.overview-row {
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

.stat-icon.appointments {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.satisfaction {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.cancelled {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.noShow {
  background: linear-gradient(135deg, #ffd86f 0%, #fc6262 100%);
}

.stat-icon.slots {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.available {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.used {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.rate {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-sub-info {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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
  margin: 8px 0 5px 0;
}

.stat-change {
  font-size: 12px;
  display: flex;
  align-items: center;
}

.stat-change.positive {
  color: #67c23a;
}

.stat-change .el-icon {
  margin-right: 2px;
}

.charts-row {
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
  margin: 10px 0;
  font-size: 16px;
}

.trend-data {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.trend-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.trend-date {
  color: #909399;
}

.trend-value {
  color: #303133;
  font-weight: 600;
  margin-top: 2px;
}

.department-chart {
  width: 100%;
  padding: 20px;
}

.dept-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.dept-bar:last-child {
  margin-bottom: 0;
}

.dept-info {
  width: 80px;
  display: flex;
  flex-direction: column;
}

.dept-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.dept-count {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.dept-progress {
  flex: 1;
  height: 20px;
  background: #f0f0f0;
  border-radius: 10px;
  margin: 0 15px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.dept-percentage {
  width: 50px;
  text-align: right;
  font-size: 12px;
  color: #606266;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>