import * as XLSX from 'xlsx'

/**
 * 导出挂号数据统计报表
 * @param {Object} data - 报表数据
 * @param {Object} filters - 筛选条件
 */
export function exportRegistrationReport(data, filters) {
  const wb = XLSX.utils.book_new()
  
  const { startDate, endDate, departmentName } = filters
  const filterNote = departmentName !== '全部科室' ? `筛选条件：${departmentName}` : ''
  
  // ==================== Sheet 1: 统计概览 ====================
  createDashboardSummarySheet(wb, data.overview, { startDate, endDate, filterNote })
  
  // ==================== Sheet 2: 每日趋势明细 ====================
  createDailyTrendsSheet(wb, data.dailyTrends)
  
  // ==================== Sheet 3: 科室数据统计 ====================
  createDepartmentStatsSheet(wb, data.departmentStats)
  
  // ==================== Sheet 4: 医生绩效统计 ====================
  createDoctorStatsSheet(wb, data.doctorStats)
  
  // ==================== Sheet 5: 属性分布统计 ====================
  createDistributionStatsSheet(wb, data.distributions)
  
  // ==================== Sheet 6: 预约原始明细 ====================
  createRawDataSheet(wb, data.rawAppointments)
  
  // 生成文件名
  const filename = `挂号数据统计报表_${startDate}_${endDate}.xlsx`
  
  // 导出文件
  XLSX.writeFile(wb, filename)
  
  return filename
}

/**
 * Sheet 1: 统计概览
 */
function createDashboardSummarySheet(wb, overview, meta) {
  const data = [
    ['挂号数据统计报表 - 统计概览'],
    [],
    ['项目', '数值', '备注'],
    ['统计周期', `${meta.startDate} 至 ${meta.endDate}`, meta.filterNote],
    [],
    ['核心指标', '', ''],
    ['总预约数', overview.totalAppointments || 0, ''],
    ['已完成', overview.completedAppointments || 0, ''],
    ['已取消', overview.cancelledAppointments || 0, ''],
    ['爽约数', overview.noShowAppointments || 0, ''],
    ['完成率', '', `计算公式：已完成/总预约 = ${formatPercentage(overview.completionRate)}`],
    [],
    ['号源概况', '', ''],
    ['总号源数', overview.totalSlots || 0, ''],
    ['可用号源', overview.availableSlots || 0, ''],
    ['已用号源', overview.usedSlots || 0, ''],
    ['号源利用率', '', `${formatPercentage(overview.utilization)}`]
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(data)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 20 },  // 项目列
    { wch: 15 },  // 数值列
    { wch: 40 }   // 备注列
  ]
  
  // 合并单元格 - 标题
  ws['!merges'] = [
    { s: { r: 0, c: 0 }, e: { r: 0, c: 2 } }  // 标题行合并
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '统计概览')
}

/**
 * Sheet 2: 每日趋势明细
 */
function createDailyTrendsSheet(wb, dailyTrends) {
  const headers = ['日期', '总预约数', '已完成数', '已取消数', '爽约数', '总收入(元)', '环比增长(%)']
  const data = [headers]
  
  let previousRevenue = 0
  dailyTrends.forEach((item, index) => {
    const revenue = item.totalRevenue || 0
    const growth = index === 0 ? '-' : calculateGrowth(revenue, previousRevenue)
    
    data.push([
      item.date || '',
      item.totalCount || 0,
      item.completedCount || 0,
      item.cancelledCount || 0,
      item.noShowCount || 0,
      revenue,
      growth
    ])
    
    previousRevenue = revenue
  })
  
  // 添加合计行
  const totals = calculateTotals(dailyTrends)
  data.push([
    '合计',
    totals.totalAppointments,
    totals.completed,
    totals.cancelled,
    totals.noShow,
    totals.revenue,
    ''
  ])
  
  const ws = XLSX.utils.aoa_to_sheet(data)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 12 },  // 日期
    { wch: 12 },  // 总预约数
    { wch: 12 },  // 已完成数
    { wch: 12 },  // 已取消数
    { wch: 10 },  // 爽约数
    { wch: 15 },  // 总收入
    { wch: 15 }   // 环比增长
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '每日趋势明细')
}

/**
 * Sheet 3: 科室数据统计
 */
function createDepartmentStatsSheet(wb, departmentStats) {
  const headers = ['排名', '科室名称', '总预约人次', '占比(%)', '已完成', '已取消', '退号率(%)', '产生收入(元)']
  const data = [headers]
  
  departmentStats.forEach((item, index) => {
    data.push([
      index + 1,
      item.departmentName || '未知科室',
      item.totalAppointments || 0,
      formatPercentage(item.percentage),
      item.completedAppointments || 0,
      item.cancelledAppointments || 0,
      formatPercentage(item.cancellationRate * 100),
      item.totalRevenue || 0
    ])
  })
  
  const ws = XLSX.utils.aoa_to_sheet(data)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 8 },   // 排名
    { wch: 15 },  // 科室名称
    { wch: 14 },  // 总预约人次
    { wch: 12 },  // 占比
    { wch: 12 },  // 已完成
    { wch: 12 },  // 已取消
    { wch: 12 },  // 退号率
    { wch: 15 }   // 产生收入
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '科室数据统计')
}

/**
 * Sheet 4: 医生绩效统计
 */
function createDoctorStatsSheet(wb, doctorStats) {
  const headers = ['医生姓名', '所属科室', '职称/号别', '总预约数', '已完成', '退号数', '退号率(%)']
  const data = [headers]
  
  doctorStats.forEach(item => {
    data.push([
      item.doctorName || '未知医生',
      item.departmentName || '未知科室',
      item.title || item.slotType || '普通',
      item.totalAppointments || 0,
      item.completedAppointments || 0,
      item.cancelledAppointments || 0,
      formatPercentage(item.cancellationRate * 100)
    ])
  })
  
  const ws = XLSX.utils.aoa_to_sheet(data)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 12 },  // 医生姓名
    { wch: 15 },  // 所属科室
    { wch: 15 },  // 职称/号别
    { wch: 12 },  // 总预约数
    { wch: 10 },  // 已完成
    { wch: 10 },  // 退号数
    { wch: 12 }   // 退号率
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '医生绩效统计')
}

/**
 * Sheet 5: 属性分布统计
 */
function createDistributionStatsSheet(wb, distributions) {
  const data = [
    ['号别分布'],
    ['号别类型', '预约人次', '占比'],
  ]
  
  // 号别分布数据
  const slotTypeTotal = distributions.slotType.reduce((sum, item) => sum + (item.appointmentCount || 0), 0)
  distributions.slotType.forEach(item => {
    const count = item.appointmentCount || 0
    const percentage = slotTypeTotal > 0 ? ((count / slotTypeTotal) * 100).toFixed(2) : '0.00'
    data.push([
      getSlotTypeName(item.slotType),
      count,
      `${percentage}%`
    ])
  })
  
  // 空行分隔
  data.push([])
  data.push([])
  
  // 时段分布
  data.push(['时段分布'])
  data.push(['时间段', '预约人次', '占比'])
  
  const timeSlotTotal = distributions.timeSlot.reduce((sum, item) => sum + (item.appointmentCount || 0), 0)
  distributions.timeSlot.forEach(item => {
    const count = item.appointmentCount || 0
    const percentage = timeSlotTotal > 0 ? ((count / timeSlotTotal) * 100).toFixed(2) : '0.00'
    data.push([
      getTimeSlotName(item.timeSlot),
      count,
      `${percentage}%`
    ])
  })
  
  const ws = XLSX.utils.aoa_to_sheet(data)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 15 },  // 类型/时间段
    { wch: 12 },  // 预约人次
    { wch: 12 }   // 占比
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '属性分布统计')
}

/**
 * Sheet 6: 预约原始明细
 */
function createRawDataSheet(wb, rawAppointments) {
  const headers = [
    '订单号', '患者姓名', '手机号', '预约日期', '就诊时段', 
    '科室', '医生', '号别', '挂号费', '状态', '创建时间'
  ]
  const data = [headers]
  
  rawAppointments.forEach(item => {
    data.push([
      item.id || '',
      item.patientName || '未知',
      maskPhone(item.phoneNumber),
      item.appointmentDate || '',
      getTimeSlotName(item.timeSlot),
      item.departmentName || '',
      item.doctorName || '',
      getSlotTypeName(item.slotType),
      item.fee || 0,
      getStatusName(item.status),
      item.createdAt || ''
    ])
  })
  
  const ws = XLSX.utils.aoa_to_sheet(data)
  
  // 设置列宽
  ws['!cols'] = [
    { wch: 12 },  // 订单号
    { wch: 12 },  // 患者姓名
    { wch: 15 },  // 手机号
    { wch: 12 },  // 预约日期
    { wch: 10 },  // 就诊时段
    { wch: 12 },  // 科室
    { wch: 12 },  // 医生
    { wch: 10 },  // 号别
    { wch: 10 },  // 挂号费
    { wch: 10 },  // 状态
    { wch: 18 }   // 创建时间
  ]
  
  XLSX.utils.book_append_sheet(wb, ws, '预约原始明细')
}

// ==================== 辅助函数 ====================

function formatPercentage(value) {
  if (value === null || value === undefined || isNaN(value)) return '0.00%'
  return `${Number(value).toFixed(2)}%`
}

function calculateGrowth(current, previous) {
  if (!previous || previous === 0) return '-'
  const growth = ((current - previous) / previous) * 100
  return growth >= 0 ? `+${growth.toFixed(2)}%` : `${growth.toFixed(2)}%`
}

function calculateTotals(dailyTrends) {
  return dailyTrends.reduce((acc, item) => ({
    totalAppointments: acc.totalAppointments + (item.totalCount || 0),
    completed: acc.completed + (item.completedCount || 0),
    cancelled: acc.cancelled + (item.cancelledCount || 0),
    noShow: acc.noShow + (item.noShowCount || 0),
    revenue: acc.revenue + (item.totalRevenue || 0)
  }), { totalAppointments: 0, completed: 0, cancelled: 0, noShow: 0, revenue: 0 })
}

function getSlotTypeName(slotType) {
  const map = {
    'normal': '普通号',
    'expert': '专家号',
    'vip': 'VIP号'
  }
  return map[slotType] || slotType || '普通号'
}

function getTimeSlotName(timeSlot) {
  const map = {
    'morning': '上午',
    'afternoon': '下午',
    'evening': '晚上'
  }
  return map[timeSlot] || timeSlot || ''
}

function getStatusName(status) {
  const map = {
    'scheduled': '待就诊',
    'completed': '已完成',
    'cancelled': '已取消',
    'no_show': '未到诊',
    'NO_SHOW': '未到诊'
  }
  return map[status] || status || ''
}

function maskPhone(phone) {
  if (!phone) return '-'
  const str = String(phone)
  if (str.length === 11) {
    return str.substring(0, 3) + '****' + str.substring(7)
  }
  return str
}
