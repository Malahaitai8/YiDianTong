<template>
  <div class="reports-page">
    <!-- 页面头部 -->
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">统计报表</h1>
        <p class="header-subtitle">数据统计与分析—管理平台</p>
      </div>
      <div class="header-right">
        <el-button @click="exportReport" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
        <el-button @click="exportToPDF" :loading="exportingPDF" type="success">
          <el-icon><Document /></el-icon>
          导出PDF
        </el-button>
        <el-button type="primary" @click="refreshAllData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="selectedDepartment"
            placeholder="全部科室"
            clearable
            style="width: 100%"
            @change="handleDepartmentChange"
          >
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-col>
        <el-col :span="10">
          <div class="quick-buttons">
            <div class="quick-buttons-left">
              <el-button size="small" @click="setLastMonth">上月</el-button>
              <el-button size="small" @click="setLastWeek">上周</el-button>
            </div>
            <div class="quick-buttons-right">
              <el-button type="primary" @click="loadAllStats" :loading="loading">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 全局概览统计卡片 -->
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

    <!-- 号源统计卡片 -->
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
              <div class="chart-filter">
                <el-select
                  v-model="selectedDepartmentForTrend"
                  placeholder="全部科室"
                  clearable
                  style="width: 150px"
                  @change="handleTrendDepartmentChange"
                >
                  <el-option
                    v-for="dept in departmentList"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
              </div>
            </div>
          </template>
          <v-chart ref="appointmentTrendChartRef" class="chart" :option="appointmentTrendOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 收入趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收入趋势</span>
              <div class="chart-filter">
                <el-select
                  v-model="selectedDepartmentForTrend"
                  placeholder="全部科室"
                  clearable
                  style="width: 150px"
                  @change="handleTrendDepartmentChange"
                >
                  <el-option
                    v-for="dept in departmentList"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
              </div>
                </div>
          </template>
          <v-chart class="chart" :option="revenueTrendOption" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 科室预约统计 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>科室预约统计</span>
            </div>
          </template>
          <div class="department-stats-list" v-loading="loading">
            <div
              v-for="(item, index) in departmentStatsList"
              :key="index"
              class="department-stat-item"
            >
              <div class="department-info">
                <div class="department-name">{{ item.departmentName || '未知科室' }}</div>
                <div class="department-count">{{ item.totalAppointments || 0 }} 人次</div>
              </div>
              <div class="progress-bar-container">
                <div
                  class="progress-bar"
                  :style="{
                    width: item.percentage + '%',
                    backgroundColor: getProgressBarColor(index)
                  }"
                ></div>
              </div>
            </div>
            <div v-if="departmentStatsList.length === 0" class="empty-state">
              暂无数据
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 号别分布和时间段分布 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 号别分布饼图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>号别分布</span>
            </div>
          </template>
          <v-chart ref="slotTypeChartRef" class="chart" :option="slotTypeDistributionOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 时间段分布饼图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>时间段分布</span>
            </div>
          </template>
          <v-chart ref="timeSlotChartRef" class="chart" :option="timeSlotDistributionOption" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 退号率统计（按科室） -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>科室退号率</span>
            </div>
          </template>
          <v-chart ref="cancellationRateDeptChartRef" class="chart" :option="cancellationRateByDeptOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 退号率统计（按医生） -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>医生退号率</span>
              <div class="chart-filter">
                <el-select
                  v-model="selectedDepartmentForDoctor"
                  placeholder="全部科室"
                  clearable
                  style="width: 150px"
                  @change="handleDoctorDepartmentChange"
                >
                  <el-option
                    v-for="dept in departmentList"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
              </div>
            </div>
          </template>
          <v-chart ref="cancellationRateDoctorChartRef" class="chart" :option="cancellationRateByDoctorOption" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
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
  Refresh,
  Download
} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import {
  getOverviewStats,
  getDepartmentWorkloadStats,
  getDoctorWorkloadStats,
  getRevenueStats,
  getRevenueStatsByDepartment,
  getSlotTypeDistributionStats,
  getTimeSlotDistributionStats,
  getCancellationRateByDepartment,
  getCancellationRateByDoctor,
  getTrendStats
} from '@/api/statistics'
import { getDepartmentList } from '@/api/department'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

const loading = ref(false)
const exporting = ref(false)
const exportingPDF = ref(false)
const dateRange = ref([])

// 图表引用
const appointmentTrendChartRef = ref(null)
const revenueTrendChartRef = ref(null)
const slotTypeChartRef = ref(null)
const timeSlotChartRef = ref(null)
const cancellationRateDeptChartRef = ref(null)
const cancellationRateDoctorChartRef = ref(null)
const departmentList = ref([])
const selectedDepartment = ref(null) // 全局科室筛选
const selectedDepartmentForTrend = ref(null) // 趋势图科室筛选
const selectedDepartmentForDoctor = ref(null) // 医生退号率的科室筛选

// 初始化日期范围（默认最近30天）
const initDateRange = () => {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  dateRange.value = [
    formatDateStr(startDate),
    formatDateStr(endDate)
  ]
}

// 设置上月日期范围
const setLastMonth = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  
  // 上个月的第一天
  const firstDay = new Date(year, month - 1, 1)
  // 上个月的最后一天
  const lastDay = new Date(year, month, 0)
  
  dateRange.value = [
    formatDateStr(firstDay),
    formatDateStr(lastDay)
  ]
  loadAllStats()
}

// 设置上周日期范围（周一到周日）
const setLastWeek = () => {
  const now = new Date()
  const day = now.getDay() // 0=周日, 1=周一, ..., 6=周六
  
  // 计算本周周一
  // 如果今天是周日(0)，本周一应该是上周一，所以需要减去6天
  // 如果今天是周一(1)，本周一就是今天，所以需要减去0天
  // 如果今天是周二(2)，本周一是昨天，所以需要减去1天
  // 以此类推：daysToMonday = day === 0 ? 6 : day - 1
  const daysToMonday = day === 0 ? 6 : day - 1
  const thisMonday = new Date(now)
  thisMonday.setDate(now.getDate() - daysToMonday)
  thisMonday.setHours(0, 0, 0, 0) // 设置为当天的00:00:00
  
  // 上周周一（本周周一减去7天）
  const lastMonday = new Date(thisMonday)
  lastMonday.setDate(thisMonday.getDate() - 7)
  
  // 上周周日（上周周一加6天）
  const lastSunday = new Date(lastMonday)
  lastSunday.setDate(lastMonday.getDate() + 6)
  lastSunday.setHours(23, 59, 59, 999) // 设置为当天的23:59:59
  
  dateRange.value = [
    formatDateStr(lastMonday),
    formatDateStr(lastSunday)
  ]
  loadAllStats()
}

// 全局概览数据
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

// 图表选项
const appointmentTrendOption = ref({})
const revenueTrendOption = ref({})
const slotTypeDistributionOption = ref({})
const timeSlotDistributionOption = ref({})
const departmentDistributionOption = ref({})
const cancellationRateByDeptOption = ref({})
const cancellationRateByDoctorOption = ref({})

// 科室预约统计数据
const departmentStatsList = ref([])

// 趋势数据
const trendData = ref({
  appointmentTrend: [],
  revenueTrend: [],
  cancellationTrend: []
})

// 原始统计数据（用于导出）
const rawStatsData = ref({
  slotTypeDistribution: [],
  timeSlotDistribution: [],
  cancellationRateByDept: [],
  cancellationRateByDoctor: []
})


// 格式化日期字符串
const formatDateStr = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 生成日期范围内的所有日期
const generateDateRange = (startDate, endDate) => {
  const dates = []
  const start = new Date(startDate)
  const end = new Date(endDate)
  
  const currentDate = new Date(start)
  while (currentDate <= end) {
    dates.push(formatDateStr(currentDate))
    currentDate.setDate(currentDate.getDate() + 1)
  }
  
  return dates
}

// 填充缺失的日期数据，确保日期范围内每一天都有数据
const fillMissingDates = (data, dateRange, getValueFn) => {
  if (!dateRange || dateRange.length !== 2) {
    // 如果没有日期范围，返回原始数据但确保按日期排序
    return [...data].sort((a, b) => {
      if (!a.date || !b.date) return 0
      return a.date.localeCompare(b.date)
    })
  }
  
  const allDates = generateDateRange(dateRange[0], dateRange[1])
  const dataMap = new Map()
  
  // 将现有数据转换为Map，以日期为key（标准化日期格式）
  data.forEach(item => {
    if (item.date) {
      // 确保日期格式统一为 YYYY-MM-DD
      const normalizedDate = item.date.split('T')[0] // 处理可能带时间的日期格式
      dataMap.set(normalizedDate, { ...item, date: normalizedDate })
    }
  })
  
  // 为每个日期创建数据项，缺失的用0填充
  return allDates.map(date => {
    if (dataMap.has(date)) {
      return dataMap.get(date)
    } else {
      // 创建默认数据项
      const defaultItem = { date }
      if (getValueFn) {
        Object.assign(defaultItem, getValueFn())
      }
      return defaultItem
    }
  })
}

// 格式化百分比
const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '0.00'
  return (rate * 100).toFixed(2)
}

// 格式化金额
const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

// 获取科室ID，兼容后端可能的字段名
const extractDeptId = (item) =>
  item?.departmentId ||
  item?.deptId ||
  item?.department_id ||
  item?.dept_id ||
  item?.department?.id

// 根据科室ID获取科室名称
const getDeptNameById = (id) => {
  if (!id) return null
  const found = departmentList.value.find((d) => String(d.id) === String(id))
  return found?.name || null
}

// 获取查询参数
const getQueryParams = () => {
  const params = {}
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  if (selectedDepartment.value) {
    params.departmentId = selectedDepartment.value
    params.deptId = selectedDepartment.value // 兼容可能的参数名
    const deptName = getDeptNameById(selectedDepartment.value)
    if (deptName) {
      params.departmentName = deptName // 某些接口可能按名称筛选
    }
  }
  return params
}

// 加载全局概览统计
const loadOverviewStats = async () => {
  try {
    const params = getQueryParams()
    const res = await getOverviewStats(params)
    const data = res?.data || {}
    Object.assign(overview, {
      totalAppointments: data.totalAppointments || 0,
      completedAppointments: data.completedAppointments || 0,
      cancelledAppointments: data.cancelledAppointments || 0,
      noShowAppointments: data.noShowAppointments || 0,
      totalSlots: data.totalSlots || 0,
      availableSlots: data.availableSlots || 0,
      usedSlots: data.usedSlots || 0,
      completionRate: data.completionRate || 0,
      utilization: data.utilization || 0
    })
  } catch (error) {
    console.error('加载概览统计失败:', error)
    ElMessage.error('加载概览统计失败')
  }
}

// 加载趋势数据
const loadTrendStats = async () => {
  try {
    const params = getQueryParams()
    // 如果图表有自己的筛选，优先使用图表的筛选；否则使用全局筛选
    const deptId = selectedDepartmentForTrend.value || selectedDepartment.value
    if (deptId) {
      params.departmentId = deptId
      params.deptId = deptId // 兼容可能的参数名
      const deptName = getDeptNameById(deptId)
      if (deptName) {
        params.departmentName = deptName // 某些接口可能按名称筛
      }
    }
    const res = await getTrendStats(params)
    const data = res?.data || {}
    const filterTrend = (list = []) => {
      const targetDeptId = selectedDepartmentForTrend.value || selectedDepartment.value
      if (!targetDeptId) return list
      const targetDeptName = getDeptNameById(targetDeptId)
      const hasDeptInfo = list.some(
        (item) =>
          extractDeptId(item) ||
          item?.departmentName ||
          item?.deptName ||
          item?.department?.name
      )
      // 如果数据里没有科室字段，直接返回原数据以避免被过滤成空
      if (!hasDeptInfo) return list
      return list.filter((item) => {
        const deptId = extractDeptId(item)
        if (deptId && String(deptId) === String(targetDeptId)) {
          return true
        }
        const deptName = item?.departmentName || item?.deptName || item?.department?.name
        if (deptName && targetDeptName) {
          return String(deptName) === String(targetDeptName)
        }
        return false
      })
    }
    trendData.value = {
      appointmentTrend: filterTrend(data.appointmentTrend),
      revenueTrend: filterTrend(data.revenueTrend),
      cancellationTrend: data.cancellationTrend || []
    }
    updateTrendCharts()
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 更新预约趋势图
const updateAppointmentTrendChart = () => {
  let data = trendData.value.appointmentTrend || []
  
  // 填充缺失的日期，确保日期范围内每一天都有数据
  data = fillMissingDates(data, dateRange.value, () => ({
    totalCount: 0,
    completedCount: 0,
    cancelledCount: 0
  }))
  
  const dates = data.map(item => item.date)
  const totalCounts = data.map(item => item.totalCount || 0)
  const completedCounts = data.map(item => item.completedCount || 0)
  const cancelledCounts = data.map(item => item.cancelledCount || 0)

  // 计算数据范围，用于 dataZoom
  const dataLength = dates.length
  const showDataZoom = dataLength > 7 // 超过7个数据点时显示滚动条

  appointmentTrendOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['总预约', '已完成', '已取消']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: showDataZoom ? '15%' : '3%',
      containLabel: true
    },
    dataZoom: showDataZoom ? [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: Math.max(0, 100 - (7 / dataLength * 100)), // 默认显示最后7个数据点
        end: 100,
        height: 20,
        bottom: 10,
        handleIcon: 'path://M30.9,53.2C16.8,53.2,5.3,41.7,5.3,27.6S16.8,2,30.9,2C45,2,56.4,13.5,56.4,27.6S45,53.2,30.9,53.2z M30.9,3.5C17.6,3.5,6.8,14.4,6.8,27.6c0,13.3,10.8,24.1,24.1,24.1C44.2,51.7,55,40.9,55,27.6C54.9,14.4,44.1,3.5,30.9,3.5z M36.9,35.8c0,0.6-0.4,1-1,1H26c-0.6,0-1-0.4-1-1V19.5c0-0.6,0.4-1,1-1h9.9c0.6,0,1,0.4,1,1V35.8z',
        handleSize: '80%',
        handleStyle: {
          color: '#fff',
          shadowBlur: 3,
          shadowColor: 'rgba(0, 0, 0, 0.6)',
          shadowOffsetX: 2,
          shadowOffsetY: 2
        }
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: Math.max(0, 100 - (7 / dataLength * 100)),
        end: 100
      }
    ] : [],
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总预约',
        type: 'line',
        data: totalCounts,
        smooth: true,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '已完成',
        type: 'line',
        data: completedCounts,
        smooth: true,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '已取消',
        type: 'line',
        data: cancelledCounts,
        smooth: true,
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
}

// 更新收入趋势图
const updateRevenueTrendChart = () => {
  let data = trendData.value.revenueTrend || []
  
  // 填充缺失的日期，确保日期范围内每一天都有数据
  data = fillMissingDates(data, dateRange.value, () => ({
    totalRevenue: 0,
    revenue: 0,
    amount: 0
  }))
  
  const dates = data.map(item => item.date)
  const revenues = data.map(item => {
    // 确保收入值是数字类型，尝试多个可能的字段名
    const revenue = item.totalRevenue !== undefined ? item.totalRevenue : 
                    (item.revenue !== undefined ? item.revenue : 
                    (item.amount !== undefined ? item.amount : 0))
    const numValue = typeof revenue === 'number' ? revenue : parseFloat(revenue)
    return isNaN(numValue) ? 0 : numValue
  })

  // 计算数据范围
  const maxRevenue = revenues.length > 0 ? Math.max(...revenues) : 0
  const minRevenue = revenues.length > 0 ? Math.min(...revenues) : 0
  const dataLength = dates.length
  const showDataZoom = dataLength > 7 // 超过7个数据点时显示滚动条

  // 调试日志（开发时使用）
  if (process.env.NODE_ENV === 'development') {
    console.log('收入趋势数据:', {
      dataLength,
      revenues,
      maxRevenue,
      minRevenue,
      sampleData: data.slice(0, 3)
    })
  }

  // 根据数据范围决定 yAxis 的 formatter
  const getYAxisFormatter = (value) => {
    if (maxRevenue === 0) {
      return '¥0'
    }
    if (maxRevenue >= 1000) {
      return `¥${(value / 1000).toFixed(value >= 10000 ? 0 : 1)}k`
    } else if (maxRevenue >= 100) {
      return `¥${value.toFixed(0)}`
    } else {
      return `¥${value.toFixed(2)}`
    }
  }

  revenueTrendOption.value = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ¥${formatMoney(param.value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: showDataZoom ? '15%' : '3%',
      containLabel: true
    },
    dataZoom: showDataZoom ? [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: Math.max(0, 100 - (7 / dataLength * 100)), // 默认显示最后7个数据点
        end: 100,
        height: 20,
        bottom: 10,
        handleIcon: 'path://M30.9,53.2C16.8,53.2,5.3,41.7,5.3,27.6S16.8,2,30.9,2C45,2,56.4,13.5,56.4,27.6S45,53.2,30.9,53.2z M30.9,3.5C17.6,3.5,6.8,14.4,6.8,27.6c0,13.3,10.8,24.1,24.1,24.1C44.2,51.7,55,40.9,55,27.6C54.9,14.4,44.1,3.5,30.9,3.5z M36.9,35.8c0,0.6-0.4,1-1,1H26c-0.6,0-1-0.4-1-1V19.5c0-0.6,0.4-1,1-1h9.9c0.6,0,1,0.4,1,1V35.8z',
        handleSize: '80%',
        handleStyle: {
          color: '#fff',
          shadowBlur: 3,
          shadowColor: 'rgba(0, 0, 0, 0.6)',
          shadowOffsetX: 2,
          shadowOffsetY: 2
        }
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: Math.max(0, 100 - (7 / dataLength * 100)),
        end: 100
      }
    ] : [],
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      min: minRevenue >= 0 ? 0 : undefined, // 如果最小值大于等于0，则从0开始
      // 如果最大值是0，设置一个小的范围以便显示
      max: maxRevenue === 0 ? 10 : undefined,
      axisLabel: {
        formatter: getYAxisFormatter
      },
      // 如果数据都是0或很小，不使用scale模式，确保y轴能正确显示
      scale: maxRevenue === 0 ? false : (maxRevenue > 0 && maxRevenue < 10 && minRevenue >= 0)
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: revenues,
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        },
        itemStyle: { color: '#409eff' }
      }
    ]
  }
}

// 更新趋势图表
const updateTrendCharts = () => {
  updateAppointmentTrendChart()
  updateRevenueTrendChart()
}

// 加载号别分布统计
const loadSlotTypeDistribution = async () => {
  try {
    const params = getQueryParams()
    const res = await getSlotTypeDistributionStats(params)
    const data = res?.data || []
    
    // 保存原始数据用于导出
    rawStatsData.value.slotTypeDistribution = data
    
    slotTypeDistributionOption.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '号别分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n{d}%',
            fontSize: 14,
            fontWeight: 'bold',
            color: '#303133'
          },
          labelLine: {
            show: true,
            length: 15,
            length2: 10,
            lineStyle: {
              color: '#909399',
              width: 1
            }
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: data.map(item => ({
            value: item.appointmentCount || 0,
            name: getSlotTypeName(item.slotType)
          }))
        }
      ]
    }
  } catch (error) {
    console.error('加载号别分布失败:', error)
  }
}

// 加载时间段分布统计
const loadTimeSlotDistribution = async () => {
  try {
    const params = getQueryParams()
    const res = await getTimeSlotDistributionStats(params)
    const data = res?.data || []
    
    // 保存原始数据用于导出
    rawStatsData.value.timeSlotDistribution = data
    
    timeSlotDistributionOption.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '时间段分布',
          type: 'pie',
          radius: '60%',
          avoidLabelOverlap: true,
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n{d}%',
            fontSize: 14,
            fontWeight: 'bold',
            color: '#303133'
          },
          labelLine: {
            show: true,
            length: 15,
            length2: 10,
            lineStyle: {
              color: '#909399',
              width: 1
            }
          },
          data: data.map(item => ({
            value: item.appointmentCount || 0,
            name: getTimeSlotName(item.timeSlot)
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            },
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          }
        }
      ]
    }
  } catch (error) {
    console.error('加载时间段分布失败:', error)
  }
}

// 加载科室分布统计
const loadDepartmentDistribution = async () => {
  try {
    const params = getQueryParams()
    const res = await getDepartmentWorkloadStats(params)
    const data = res?.data || []
    
    // 按预约数排序（从高到低）
    const sortedData = [...data].sort((a, b) => (b.totalAppointments || 0) - (a.totalAppointments || 0))
    
    // 计算最大值，用于计算百分比
    const maxCount = sortedData.length > 0 ? sortedData[0].totalAppointments || 0 : 1
    
    // 处理数据，添加百分比
    departmentStatsList.value = sortedData.map(item => ({
      departmentName: item.departmentName || '未知科室',
      totalAppointments: item.totalAppointments || 0,
      percentage: maxCount > 0 ? ((item.totalAppointments || 0) / maxCount * 100) : 0
    }))
  } catch (error) {
    console.error('加载科室分布失败:', error)
    departmentStatsList.value = []
  }
}

// 获取进度条颜色
const getProgressBarColor = (index) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  return colors[index % colors.length]
}

// 加载退号率统计（按科室）
const loadCancellationRateByDept = async () => {
  try {
    const params = getQueryParams()
    const res = await getCancellationRateByDepartment(params)
    const data = res?.data || []
    
    // 保存原始数据用于导出
    rawStatsData.value.cancellationRateByDept = data
    
    // 判断是否需要显示滚动条（当科室数量超过8个时显示）
    const showDataZoom = data.length > 8
    
    cancellationRateByDeptOption.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params) => {
          const param = params[0]
          return `${param.name}<br/>退号率: ${param.value}%`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: showDataZoom ? '15%' : '3%',
        containLabel: true
      },
      dataZoom: showDataZoom ? [
        {
          type: 'slider',
          show: true,
          xAxisIndex: [0],
          start: 0,
          end: Math.min(100, (8 / data.length * 100)), // 默认显示前8个科室
          height: 20,
          bottom: 10,
          handleIcon: 'path://M30.9,53.2C16.8,53.2,5.3,41.7,5.3,27.6S16.8,2,30.9,2C45,2,56.4,13.5,56.4,27.6S45,53.2,30.9,53.2z M30.9,3.5C17.6,3.5,6.8,14.4,6.8,27.6c0,13.3,10.8,24.1,24.1,24.1C44.2,51.7,55,40.9,55,27.6C54.9,14.4,44.1,3.5,30.9,3.5z M36.9,35.8c0,0.6-0.4,1-1,1H26c-0.6,0-1-0.4-1-1V19.5c0-0.6,0.4-1,1-1h9.9c0.6,0,1,0.4,1,1V35.8z',
          handleSize: '80%',
          handleStyle: {
            color: '#fff',
            shadowBlur: 3,
            shadowColor: 'rgba(0, 0, 0, 0.6)',
            shadowOffsetX: 2,
            shadowOffsetY: 2
          }
        },
        {
          type: 'inside',
          xAxisIndex: [0],
          start: 0,
          end: Math.min(100, (8 / data.length * 100))
        }
      ] : [],
      xAxis: {
        type: 'category',
        data: data.map(item => item.departmentName || '未知'),
        axisLabel: {
          rotate: 45,
          interval: 0 // 显示所有标签
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '退号率',
          type: 'bar',
          data: data.map(item => item.cancellationRate || 0),
          itemStyle: {
            color: (params) => {
              const rate = params.value
              if (rate >= 10) return '#f56c6c'
              if (rate >= 5) return '#e6a23c'
              return '#67c23a'
            }
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}%'
          }
        }
      ]
    }
  } catch (error) {
    console.error('加载科室退号率失败:', error)
  }
}

// 加载科室列表
const loadDepartmentList = async () => {
  try {
    const res = await getDepartmentList()
    
    // 调试信息
    console.log('科室API返回数据:', res)
    
    // 处理不同的数据格式
    let data = []
    if (Array.isArray(res)) {
      data = res
    } else if (Array.isArray(res?.data)) {
      data = res.data
    } else if (Array.isArray(res?.data?.list)) {
      data = res.data.list
    } else if (res?.data && typeof res.data === 'object' && !Array.isArray(res.data)) {
      // 如果是对象，尝试转换为数组
      data = Object.values(res.data)
    }
    
    console.log('处理后的科室数据:', data)
    
    // 映射数据，确保字段名正确
    departmentList.value = data.map(dept => {
      // 尝试多种可能的字段名
      const id = dept.id || dept.departmentId || dept.deptId
      const name = dept.name || dept.departmentName || dept.deptName || dept.title
      return { id, name }
    }).filter(dept => dept.id && dept.name) // 过滤掉无效数据
    
    console.log('最终科室列表:', departmentList.value)
    
    if (departmentList.value.length === 0) {
      console.warn('科室列表为空，请检查API返回的数据格式')
    }
  } catch (error) {
    console.error('加载科室列表失败:', error)
    ElMessage.error('加载科室列表失败: ' + (error?.message || '未知错误'))
    departmentList.value = []
  }
}

// 加载退号率统计（按医生）
const loadCancellationRateByDoctor = async () => {
  try {
    const params = getQueryParams()
    // 如果图表有自己的筛选，优先使用图表的筛选；否则使用全局筛选
    const deptId = selectedDepartmentForDoctor.value || selectedDepartment.value
    if (deptId) {
      params.departmentId = deptId
    }
    const res = await getCancellationRateByDoctor(params)
    const data = res?.data || []

    // 保存原始数据用于导出
    rawStatsData.value.cancellationRateByDoctor = data

    // 先在前端按科室过滤，防止接口未实现科室筛选
    const targetDeptId = selectedDepartmentForDoctor.value || selectedDepartment.value
    const filteredData = targetDeptId
      ? data.filter(item => {
          const itemDeptId =
            item.departmentId ||
            item.deptId ||
            item.department_id ||
            item.dept_id ||
            item.department?.id
          return itemDeptId && String(itemDeptId) === String(targetDeptId)
        })
      : data
    
    // 显示所有医生数据，不限制数量
    const displayData = filteredData
    
    // 判断是否需要显示滚动条（当医生数量超过8个时显示）
    const showDataZoom = displayData.length > 8
    
    cancellationRateByDoctorOption.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params) => {
          const param = params[0]
          return `${param.name}<br/>退号率: ${param.value}%`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: showDataZoom ? '15%' : '3%',
        containLabel: true
      },
      dataZoom: showDataZoom ? [
        {
          type: 'slider',
          show: true,
          xAxisIndex: [0],
          start: 0,
          end: Math.min(100, (8 / displayData.length * 100)), // 默认显示前8个医生
          height: 20,
          bottom: 10,
          handleIcon: 'path://M30.9,53.2C16.8,53.2,5.3,41.7,5.3,27.6S16.8,2,30.9,2C45,2,56.4,13.5,56.4,27.6S45,53.2,30.9,53.2z M30.9,3.5C17.6,3.5,6.8,14.4,6.8,27.6c0,13.3,10.8,24.1,24.1,24.1C44.2,51.7,55,40.9,55,27.6C54.9,14.4,44.1,3.5,30.9,3.5z M36.9,35.8c0,0.6-0.4,1-1,1H26c-0.6,0-1-0.4-1-1V19.5c0-0.6,0.4-1,1-1h9.9c0.6,0,1,0.4,1,1V35.8z',
          handleSize: '80%',
          handleStyle: {
            color: '#fff',
            shadowBlur: 3,
            shadowColor: 'rgba(0, 0, 0, 0.6)',
            shadowOffsetX: 2,
            shadowOffsetY: 2
          }
        },
        {
          type: 'inside',
          xAxisIndex: [0],
          start: 0,
          end: Math.min(100, (8 / displayData.length * 100))
        }
      ] : [],
      xAxis: {
        type: 'category',
        data: displayData.map(item => item.doctorName || '未知'),
        axisLabel: {
          rotate: 45,
          interval: 0 // 显示所有标签
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '退号率',
          type: 'bar',
          data: displayData.map(item => item.cancellationRate || 0),
          itemStyle: {
            color: (params) => {
              const rate = params.value
              if (rate >= 10) return '#f56c6c'
              if (rate >= 5) return '#e6a23c'
              return '#67c23a'
            }
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}%'
          }
        }
      ]
    }
  } catch (error) {
    console.error('加载医生退号率失败:', error)
  }
}

// 加载所有统计数据
const loadAllStats = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverviewStats(),
      loadTrendStats(),
      loadSlotTypeDistribution(),
      loadTimeSlotDistribution(),
      loadDepartmentDistribution(),
      loadCancellationRateByDept(),
      loadCancellationRateByDoctor()
    ])
  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新所有数据
const refreshAllData = () => {
  loadAllStats()
}

// 辅助函数
const getSlotTypeName = (type) => {
  const map = {
    normal: '普通号',
    expert: '专家号',
    vip: '特需号'
  }
  return map[type] || type
}

const getTimeSlotName = (slot) => {
  const map = {
    morning: '上午',
    afternoon: '下午',
    evening: '晚间'
  }
  return map[slot] || slot
}

// 事件处理
const handleDateChange = () => {
  loadAllStats()
}

// 处理全局科室筛选变化
const handleDepartmentChange = (value) => {
  // 同步更新所有图表的科室筛选
  selectedDepartmentForTrend.value = value
  selectedDepartmentForDoctor.value = value
  // 重新加载所有统计数据
  loadAllStats()
}

// 处理趋势图科室筛选变化
const handleTrendDepartmentChange = (value) => {
  // 如果趋势图选择了科室，不同步到全局筛选，只更新趋势图
  loadTrendStats()
}

// 处理医生退号率科室筛选变化
const handleDoctorDepartmentChange = (value) => {
  // 如果医生退号率图表选择了科室，不同步到全局筛选，只更新该图表
  loadCancellationRateByDoctor()
}

const resetFilters = () => {
  initDateRange()
  selectedDepartment.value = null
  selectedDepartmentForTrend.value = null
  selectedDepartmentForDoctor.value = null
  loadAllStats()
}

// 导出报表
const exportReport = async () => {
  try {
    exporting.value = true
    
    // 检查是否有数据
    if (!dateRange.value || dateRange.value.length !== 2) {
      ElMessage.warning('请先选择日期范围并查询数据')
      return
    }
    
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    
    // 获取筛选条件信息
    const startDate = dateRange.value[0]
    const endDate = dateRange.value[1]
    const deptName = selectedDepartment.value
      ? (departmentList.value.find(d => d.id === selectedDepartment.value)?.name || '全部科室')
      : '全部科室'
    const filterNote = selectedDepartment.value ? `筛选条件：${deptName}` : '筛选条件：全部科室'
    
    // 1. 概览统计工作表
    const overviewData = [
      ['统计报表 - 概览统计'],
      ['导出时间', new Date().toLocaleString('zh-CN')],
      ['日期范围', dateRange.value && dateRange.value.length === 2 
        ? `${dateRange.value[0]} 至 ${dateRange.value[1]}` 
        : '全部'],
      ['筛选科室', selectedDepartment.value 
        ? (departmentList.value.find(d => d.id === selectedDepartment.value)?.name || '全部')
        : '全部'],
      [],
      ['指标', '数值'],
      ['总预约数', overview.totalAppointments],
      ['已完成', overview.completedAppointments],
      ['已取消', overview.cancelledAppointments],
      ['爽约', overview.noShowAppointments],
      ['总号源数', overview.totalSlots],
      ['可用号源', overview.availableSlots],
      ['已用号源', overview.usedSlots],
      ['完成率', `${formatRate(overview.completionRate)}%`],
      ['利用率', `${formatRate(overview.utilization)}%`]
    ]
    const overviewWs = XLSX.utils.aoa_to_sheet(overviewData)
    overviewWs['!cols'] = [{ wch: 15 }, { wch: 20 }]
    XLSX.utils.book_append_sheet(wb, overviewWs, '概览统计')
    
    // 2. 预约趋势工作表
    const appointmentTrendData = [
      ['日期', '总预约', '已完成', '已取消']
    ]
    const appointmentTrend = trendData.value.appointmentTrend || []
    appointmentTrend.forEach(item => {
      appointmentTrendData.push([
        item.date || '',
        item.totalCount || 0,
        item.completedCount || 0,
        item.cancelledCount || 0
      ])
    })
    const appointmentTrendWs = XLSX.utils.aoa_to_sheet(appointmentTrendData)
    appointmentTrendWs['!cols'] = [{ wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }]
    XLSX.utils.book_append_sheet(wb, appointmentTrendWs, '预约趋势')
    
    // 3. 收入趋势工作表
    const revenueTrendData = [
      ['日期', '收入(元)']
    ]
    const revenueTrend = trendData.value.revenueTrend || []
    revenueTrend.forEach(item => {
      const revenue = item.totalRevenue !== undefined ? item.totalRevenue : 
                     (item.revenue !== undefined ? item.revenue : 
                     (item.amount !== undefined ? item.amount : 0))
      revenueTrendData.push([
        item.date || '',
        typeof revenue === 'number' ? revenue : parseFloat(revenue) || 0
      ])
    })
    const revenueTrendWs = XLSX.utils.aoa_to_sheet(revenueTrendData)
    revenueTrendWs['!cols'] = [{ wch: 12 }, { wch: 15 }]
    XLSX.utils.book_append_sheet(wb, revenueTrendWs, '收入趋势')
    
    // 4. 科室预约统计工作表
    const departmentStatsData = [
      ['科室名称', '预约人次', '占比(%)']
    ]
    departmentStatsList.value.forEach(item => {
      departmentStatsData.push([
        item.departmentName || '未知科室',
        item.totalAppointments || 0,
        item.percentage ? item.percentage.toFixed(2) : 0
      ])
    })
    const departmentStatsWs = XLSX.utils.aoa_to_sheet(departmentStatsData)
    departmentStatsWs['!cols'] = [{ wch: 20 }, { wch: 12 }, { wch: 12 }]
    XLSX.utils.book_append_sheet(wb, departmentStatsWs, '科室预约统计')
    
    // 5. 号别分布工作表
    const slotTypeData = [
      ['号别类型', '预约数量']
    ]
    rawStatsData.value.slotTypeDistribution.forEach(item => {
      slotTypeData.push([
        getSlotTypeName(item.slotType),
        item.appointmentCount || 0
      ])
    })
    const slotTypeWs = XLSX.utils.aoa_to_sheet(slotTypeData)
    slotTypeWs['!cols'] = [{ wch: 15 }, { wch: 12 }]
    XLSX.utils.book_append_sheet(wb, slotTypeWs, '号别分布')
    
    // 6. 时间段分布工作表
    const timeSlotData = [
      ['时间段', '预约数量']
    ]
    rawStatsData.value.timeSlotDistribution.forEach(item => {
      timeSlotData.push([
        getTimeSlotName(item.timeSlot),
        item.appointmentCount || 0
      ])
    })
    const timeSlotWs = XLSX.utils.aoa_to_sheet(timeSlotData)
    timeSlotWs['!cols'] = [{ wch: 15 }, { wch: 12 }]
    XLSX.utils.book_append_sheet(wb, timeSlotWs, '时间段分布')
    
    // 7. 科室退号率工作表
    const cancellationRateDeptData = [
      ['科室名称', '退号率(%)']
    ]
    rawStatsData.value.cancellationRateByDept.forEach(item => {
      cancellationRateDeptData.push([
        item.departmentName || '未知',
        item.cancellationRate ? (item.cancellationRate * 100).toFixed(2) : 0
      ])
    })
    const cancellationRateDeptWs = XLSX.utils.aoa_to_sheet(cancellationRateDeptData)
    cancellationRateDeptWs['!cols'] = [{ wch: 20 }, { wch: 12 }]
    XLSX.utils.book_append_sheet(wb, cancellationRateDeptWs, '科室退号率')
    
    // 8. 医生退号率工作表
    const cancellationRateDoctorData = [
      ['医生姓名', '退号率(%)']
    ]
    // 使用过滤后的数据（与图表显示一致）
    const targetDeptId = selectedDepartmentForDoctor.value || selectedDepartment.value
    const filteredDoctorData = targetDeptId
      ? rawStatsData.value.cancellationRateByDoctor.filter(item => {
          const itemDeptId = item.departmentId || item.deptId || item.department_id || item.dept_id || item.department?.id
          return itemDeptId && String(itemDeptId) === String(targetDeptId)
        })
      : rawStatsData.value.cancellationRateByDoctor
    filteredDoctorData.forEach(item => {
      cancellationRateDoctorData.push([
        item.doctorName || '未知',
        item.cancellationRate ? (item.cancellationRate * 100).toFixed(2) : 0
      ])
    })
    const cancellationRateDoctorWs = XLSX.utils.aoa_to_sheet(cancellationRateDoctorData)
    cancellationRateDoctorWs['!cols'] = [{ wch: 20 }, { wch: 12 }]
    XLSX.utils.book_append_sheet(wb, cancellationRateDoctorWs, '医生退号率')
    
    // 生成文件名
    const dateStr = dateRange.value && dateRange.value.length === 2
      ? `${dateRange.value[0]}_${dateRange.value[1]}`
      : '全部'
    const deptStr = selectedDepartment.value
      ? (departmentList.value.find(d => d.id === selectedDepartment.value)?.name || '')
      : '全部科室'
    const filename = `统计报表_${dateStr}_${deptStr}.xlsx`
    
    // 导出文件
    XLSX.writeFile(wb, filename)
    ElMessage.success('报表导出成功')
  } catch (error) {
    console.error('导出报表失败:', error)
    ElMessage.error('导出报表失败: ' + (error?.message || '未知错误'))
  } finally {
    exporting.value = false
  }
}

// 导出为PDF（包含图表）
const exportToPDF = async () => {
  try {
    exportingPDF.value = true
    
    // 检查是否有数据
    if (!dateRange.value || dateRange.value.length !== 2) {
      ElMessage.warning('请先选择日期范围并查询数据')
      return
    }
    
    // 创建PDF实例（A4纸张，横向）
    const pdf = new jsPDF('landscape', 'mm', 'a4')
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const margin = 10
    const contentWidth = pageWidth - 2 * margin
    const contentHeight = pageHeight - 2 * margin
    
    let yPos = margin
    
    // 辅助函数：添加新页面
    const addNewPage = () => {
      pdf.addPage()
      yPos = margin
    }
    
    // 辅助函数：检查是否需要新页面
    const checkNewPage = (requiredHeight) => {
      if (yPos + requiredHeight > pageHeight - margin) {
        addNewPage()
        return true
      }
      return false
    }
    
    // 1. 添加标题和基本信息
    pdf.setFontSize(20)
    pdf.setFont('helvetica', 'bold')
    pdf.text('统计报表', margin, yPos)
    yPos += 10
    
    pdf.setFontSize(12)
    pdf.setFont('helvetica', 'normal')
    const dateStr = dateRange.value && dateRange.value.length === 2
      ? `${dateRange.value[0]} 至 ${dateRange.value[1]}`
      : '全部'
    const deptStr = selectedDepartment.value
      ? (departmentList.value.find(d => d.id === selectedDepartment.value)?.name || '全部')
      : '全部科室'
    
    pdf.text(`日期范围: ${dateStr}`, margin, yPos)
    yPos += 6
    pdf.text(`筛选科室: ${deptStr}`, margin, yPos)
    yPos += 6
    pdf.text(`导出时间: ${new Date().toLocaleString('zh-CN')}`, margin, yPos)
    yPos += 10
    
    // 2. 添加概览统计表格
    pdf.setFontSize(14)
    pdf.setFont('helvetica', 'bold')
    pdf.text('概览统计', margin, yPos)
    yPos += 8
    
    pdf.setFontSize(10)
    pdf.setFont('helvetica', 'normal')
    
    // 创建概览统计表格数据
    const overviewTableData = [
      ['指标', '数值'],
      ['总预约数', overview.totalAppointments],
      ['已完成', overview.completedAppointments],
      ['已取消', overview.cancelledAppointments],
      ['爽约', overview.noShowAppointments],
      ['总号源数', overview.totalSlots],
      ['可用号源', overview.availableSlots],
      ['已用号源', overview.usedSlots],
      ['完成率', `${formatRate(overview.completionRate)}%`],
      ['利用率', `${formatRate(overview.utilization)}%`]
    ]
    
    // 绘制表格
    const cellHeight = 6
    const colWidth = contentWidth / 2
    overviewTableData.forEach((row, rowIndex) => {
      checkNewPage(cellHeight)
      const isHeader = rowIndex === 0
      pdf.setFont('helvetica', isHeader ? 'bold' : 'normal')
      
      // 绘制单元格
      pdf.rect(margin, yPos - 4, colWidth, cellHeight)
      pdf.rect(margin + colWidth, yPos - 4, colWidth, cellHeight)
      
      // 添加文本
      pdf.text(row[0], margin + 2, yPos)
      pdf.text(String(row[1]), margin + colWidth + 2, yPos)
      yPos += cellHeight
    })
    
    yPos += 5
    
    // 3. 获取并添加图表
    const chartWidth = (contentWidth - 10) / 2 // 两个图表并排，留10mm间距
    const chartHeight = 60 // 图表高度
    
    // 等待图表渲染完成
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 获取图表图片
    const getChartImage = async (chartRef) => {
      if (!chartRef?.value?.chart) return null
      try {
        const chartInstance = chartRef.value.chart
        return chartInstance.getDataURL({
          type: 'png',
          pixelRatio: 2,
          backgroundColor: '#fff'
        })
      } catch (error) {
        console.error('获取图表图片失败:', error)
        return null
      }
    }
    
    // 添加图片到PDF的辅助函数
    const addImageToPDF = async (imageData, title, width, height) => {
      if (!imageData) return false
      
      checkNewPage(height + 15)
      
      // 添加标题
      pdf.setFontSize(12)
      pdf.setFont('helvetica', 'bold')
      pdf.text(title, margin, yPos)
      yPos += 8
      
      // 添加图片
      try {
        pdf.addImage(imageData, 'PNG', margin, yPos, width, height)
        yPos += height + 5
        return true
      } catch (error) {
        console.error('添加图片失败:', error)
        return false
      }
    }
    
    // 添加预约趋势图
    const appointmentTrendImg = await getChartImage(appointmentTrendChartRef)
    if (appointmentTrendImg) {
      await addImageToPDF(appointmentTrendImg, '预约趋势', chartWidth, chartHeight)
    }
    
    // 添加收入趋势图
    const revenueTrendImg = await getChartImage(revenueTrendChartRef)
    if (revenueTrendImg) {
      await addImageToPDF(revenueTrendImg, '收入趋势', chartWidth, chartHeight)
    }
    
    // 添加号别分布图
    const slotTypeImg = await getChartImage(slotTypeChartRef)
    if (slotTypeImg) {
      await addImageToPDF(slotTypeImg, '号别分布', chartWidth, chartHeight)
    }
    
    // 添加时间段分布图
    const timeSlotImg = await getChartImage(timeSlotChartRef)
    if (timeSlotImg) {
      await addImageToPDF(timeSlotImg, '时间段分布', chartWidth, chartHeight)
    }
    
    // 添加科室退号率图
    const cancellationRateDeptImg = await getChartImage(cancellationRateDeptChartRef)
    if (cancellationRateDeptImg) {
      await addImageToPDF(cancellationRateDeptImg, '科室退号率', chartWidth, chartHeight)
    }
    
    // 添加医生退号率图
    const cancellationRateDoctorImg = await getChartImage(cancellationRateDoctorChartRef)
    if (cancellationRateDoctorImg) {
      await addImageToPDF(cancellationRateDoctorImg, '医生退号率', chartWidth, chartHeight)
    }
    
    // 4. 添加详细数据表格
    addNewPage()
    yPos = margin
    
    // 预约趋势数据表
    pdf.setFontSize(14)
    pdf.setFont('helvetica', 'bold')
    pdf.text('预约趋势数据', margin, yPos)
    yPos += 8
    
    pdf.setFontSize(9)
    pdf.setFont('helvetica', 'normal')
    const trendTableCols = ['日期', '总预约', '已完成', '已取消']
    const trendColWidth = contentWidth / 4
    
    // 表头
    checkNewPage(cellHeight)
    pdf.setFont('helvetica', 'bold')
    trendTableCols.forEach((col, index) => {
      pdf.rect(margin + index * trendColWidth, yPos - 4, trendColWidth, cellHeight)
      pdf.text(col, margin + index * trendColWidth + 2, yPos)
    })
    yPos += cellHeight
    
    // 数据行（最多显示20行）
    pdf.setFont('helvetica', 'normal')
    const appointmentTrend = trendData.value.appointmentTrend || []
    const maxTrendRows = Math.min(20, appointmentTrend.length)
    for (let i = 0; i < maxTrendRows; i++) {
      checkNewPage(cellHeight)
      const item = appointmentTrend[i]
      const rowData = [
        item.date || '',
        item.totalCount || 0,
        item.completedCount || 0,
        item.cancelledCount || 0
      ]
      rowData.forEach((cell, index) => {
        pdf.rect(margin + index * trendColWidth, yPos - 4, trendColWidth, cellHeight)
        pdf.text(String(cell), margin + index * trendColWidth + 2, yPos)
      })
      yPos += cellHeight
    }
    
    yPos += 10
    
    // 科室预约统计表
    pdf.setFontSize(14)
    pdf.setFont('helvetica', 'bold')
    pdf.text('科室预约统计', margin, yPos)
    yPos += 8
    
    pdf.setFontSize(9)
    pdf.setFont('helvetica', 'normal')
    const deptTableCols = ['科室名称', '预约人次', '占比(%)']
    const deptColWidth = contentWidth / 3
    
    // 表头
    checkNewPage(cellHeight)
    pdf.setFont('helvetica', 'bold')
    deptTableCols.forEach((col, index) => {
      pdf.rect(margin + index * deptColWidth, yPos - 4, deptColWidth, cellHeight)
      pdf.text(col, margin + index * deptColWidth + 2, yPos)
    })
    yPos += cellHeight
    
    // 数据行
    pdf.setFont('helvetica', 'normal')
    departmentStatsList.value.forEach(item => {
      checkNewPage(cellHeight)
      const rowData = [
        item.departmentName || '未知科室',
        item.totalAppointments || 0,
        item.percentage ? item.percentage.toFixed(2) : 0
      ]
      rowData.forEach((cell, index) => {
        pdf.rect(margin + index * deptColWidth, yPos - 4, deptColWidth, cellHeight)
        pdf.text(String(cell), margin + index * deptColWidth + 2, yPos)
      })
      yPos += cellHeight
    })
    
    // 生成文件名并保存
    const dateStrForFile = dateRange.value && dateRange.value.length === 2
      ? `${dateRange.value[0]}_${dateRange.value[1]}`
      : '全部'
    const filename = `统计报表_${dateStrForFile}_${deptStr}.pdf`
    
    pdf.save(filename)
    ElMessage.success('PDF报表导出成功')
  } catch (error) {
    console.error('导出PDF失败:', error)
    ElMessage.error('导出PDF失败: ' + (error?.message || '未知错误'))
  } finally {
    exportingPDF.value = false
  }
}

// 初始化
onMounted(async () => {
  initDateRange()
  await loadDepartmentList() // 先加载科室列表
  await loadAllStats()
})
</script>

<style scoped>
.reports-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.header-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 600;
  color: #303133;
  text-align: left;
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #606266;
  text-align: left;
}

.filter-card {
  margin-bottom: 20px;
}

.quick-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.quick-buttons-left {
  display: flex;
  gap: 10px;
  align-items: center;
}

.quick-buttons-right {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-left: auto;
}

.overview-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 140px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card :deep(.el-card__body) {
  height: 100%;
  padding: 18px 20px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
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

.stat-sub-info {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.chart-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart {
  height: 400px;
  width: 100%;
}

.department-stats-list {
  min-height: 400px;
  padding: 10px 0;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.department-stat-item {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  width: 100%;
  max-width: 800px;
}

.department-info {
  flex: 0 0 160px;
  display: flex;
  flex-direction: column;
  text-align: left;
}

.department-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.department-count {
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

.progress-bar-container {
  flex: 1;
  height: 24px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  font-size: 14px;
}
</style>
