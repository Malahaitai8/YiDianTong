<template>
  <div class="reports-page">
    <!-- 页面头部 -->
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">统计报表</h1>
        <p class="header-subtitle">数据统计与分析—管理平台</p>
      </div>
      <div class="header-right">
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
        <el-col :span="4">
          <el-select
            v-model="selectedDepartment"
            placeholder="选择科室"
            clearable
            @change="handleDepartmentChange"
            style="width: 100%"
          >
            <el-option label="全部科室" value="" />
            <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="selectedDoctor"
            placeholder="选择医生"
            clearable
            @change="handleDoctorChange"
            style="width: 100%"
            :disabled="!selectedDepartment"
          >
            <el-option label="全部医生" value="" />
            <el-option v-for="doc in filteredDoctors" :key="doc.id" :label="doc.name" :value="doc.id" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadAllStats" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
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
            </div>
          </template>
          <v-chart class="chart" :option="appointmentTrendOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 收入趋势图 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收入趋势</span>
                </div>
          </template>
          <v-chart class="chart" :option="revenueTrendOption" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 号别分布饼图 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>号别分布</span>
              </div>
          </template>
          <v-chart class="chart" :option="slotTypeDistributionOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 时间段分布饼图 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>时间段分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="timeSlotDistributionOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 科室预约分布饼图 -->
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>科室预约分布</span>
          </div>
          </template>
          <v-chart class="chart" :option="departmentDistributionOption" v-loading="loading" />
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
          <v-chart class="chart" :option="cancellationRateByDeptOption" v-loading="loading" />
        </el-card>
      </el-col>

      <!-- 退号率统计（按医生） -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>医生退号率</span>
                </div>
          </template>
          <v-chart class="chart" :option="cancellationRateByDoctorOption" v-loading="loading" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>详细数据</span>
          <div>
            <el-button size="small" @click="exportTableData">
              <el-icon><Download /></el-icon>
              导出表格
            </el-button>
          </div>
        </div>
      </template>

      <!-- 预约统计表格 -->
      <el-table
        :data="appointmentTableData"
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="totalAppointments" label="总预约" width="100" />
        <el-table-column prop="completedAppointments" label="已完成" width="100" />
        <el-table-column prop="cancelledAppointments" label="已取消" width="100" />
        <el-table-column prop="noShowAppointments" label="爽约" width="100" />
        <el-table-column label="完成率" width="100">
          <template #default="{ row }">
            <el-tag :type="getCompletionRateType(row.completionRate)">
              {{ formatRate(row.completionRate) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalRevenue" label="收入(元)" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.totalRevenue) }}
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
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
  Download,
  Refresh
} from '@element-plus/icons-vue'
import {
  getOverviewStats,
  getAppointmentStats,
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
import { getDoctorList } from '@/api/doctor'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const loading = ref(false)
const dateRange = ref([])
const selectedDepartment = ref('')
const selectedDoctor = ref('')
const departmentList = ref([])
const doctorList = ref([])

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

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

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

// 表格数据
const appointmentTableData = ref([])

// 图表选项
const appointmentTrendOption = ref({})
const revenueTrendOption = ref({})
const slotTypeDistributionOption = ref({})
const timeSlotDistributionOption = ref({})
const departmentDistributionOption = ref({})
const cancellationRateByDeptOption = ref({})
const cancellationRateByDoctorOption = ref({})

// 趋势数据
const trendData = ref({
  appointmentTrend: [],
  revenueTrend: [],
  cancellationTrend: []
})

// 计算属性
const filteredDoctors = computed(() => {
  if (!selectedDepartment.value) return doctorList.value
  const deptId = Number(selectedDepartment.value)
  return doctorList.value.filter(doc => {
    const docDeptId = doc.clinic?.departmentId ? Number(doc.clinic.departmentId) : null
    return docDeptId === deptId
  })
})

// 格式化日期字符串
const formatDateStr = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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

// 获取查询参数
const getQueryParams = () => {
  const params = {}
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  if (selectedDoctor.value) {
    params.doctorId = selectedDoctor.value
  }
  return params
}

// 加载全局概览统计
const loadOverviewStats = async () => {
  try {
    const res = await getOverviewStats()
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
    const res = await getTrendStats(params)
    const data = res?.data || {}
    trendData.value = {
      appointmentTrend: data.appointmentTrend || [],
      revenueTrend: data.revenueTrend || [],
      cancellationTrend: data.cancellationTrend || []
    }
    updateTrendCharts()
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 更新预约趋势图
const updateAppointmentTrendChart = () => {
  const data = trendData.value.appointmentTrend || []
  const dates = data.map(item => item.date)
  const totalCounts = data.map(item => item.totalCount || 0)
  const completedCounts = data.map(item => item.completedCount || 0)
  const cancelledCounts = data.map(item => item.cancelledCount || 0)

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
      bottom: '3%',
      containLabel: true
    },
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
  const data = trendData.value.revenueTrend || []
  const dates = data.map(item => item.date)
  const revenues = data.map(item => item.totalRevenue || 0)

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
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => `¥${(value / 1000).toFixed(0)}k`
      }
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
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}\n{c} ({d}%)'
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
          data: data.map(item => ({
            value: item.appointmentCount || 0,
            name: getTimeSlotName(item.timeSlot)
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
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
    
    departmentDistributionOption.value = {
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
          name: '科室分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}\n{c}'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: data.map(item => ({
            value: item.totalAppointments || 0,
            name: item.departmentName || '未知科室'
          }))
        }
      ]
    }
  } catch (error) {
    console.error('加载科室分布失败:', error)
  }
}

// 加载退号率统计（按科室）
const loadCancellationRateByDept = async () => {
  try {
    const params = getQueryParams()
    const res = await getCancellationRateByDepartment(params)
    const data = res?.data || []
    
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
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.departmentName || '未知'),
        axisLabel: {
          rotate: 45
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

// 加载退号率统计（按医生）
const loadCancellationRateByDoctor = async () => {
  try {
    const params = getQueryParams()
    const res = await getCancellationRateByDoctor(params)
    const data = res?.data || []
    
    // 只显示前10名医生
    const topData = data.slice(0, 10)
    
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
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: topData.map(item => item.doctorName || '未知'),
        axisLabel: {
          rotate: 45
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
          data: topData.map(item => item.cancellationRate || 0),
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

// 加载预约统计数据
const loadAppointmentStats = async () => {
  try {
    const params = getQueryParams()
    const res = await getAppointmentStats(params)
    const data = res?.data || []
    appointmentTableData.value = data
    pagination.total = data.length
  } catch (error) {
    console.error('加载预约统计失败:', error)
    ElMessage.error('加载预约统计失败')
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
      loadCancellationRateByDoctor(),
      loadAppointmentStats()
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

const getCompletionRateType = (rate) => {
  const rateNum = typeof rate === 'number' ? rate : parseFloat(rate)
  if (rateNum >= 0.9) return 'success'
  if (rateNum >= 0.8) return 'warning'
  return 'danger'
}

// 事件处理
const handleDateChange = () => {
  loadAllStats()
}

const handleDepartmentChange = () => {
  selectedDoctor.value = ''
  loadAllStats()
}

const handleDoctorChange = () => {
  loadAllStats()
}

const resetFilters = () => {
  initDateRange()
  selectedDepartment.value = ''
  selectedDoctor.value = ''
  loadAllStats()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
}

const exportTableData = () => {
  ElMessage.info('导出功能开发中')
}

// 初始化
onMounted(async () => {
  initDateRange()
  try {
    const [deptRes, docRes] = await Promise.all([
      getDepartmentList(),
      getDoctorList()
    ])
    departmentList.value = deptRes?.data || []
    doctorList.value = docRes?.data || []
  } catch (error) {
    console.error('加载基础数据失败:', error)
  }
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

.overview-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
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

.chart {
  height: 400px;
  width: 100%;
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
