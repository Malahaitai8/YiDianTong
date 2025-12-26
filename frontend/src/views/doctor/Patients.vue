<template>
  <div class="doctor-patients">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>患者查看</span>
          <div class="header-actions">
            <el-radio-group v-model="viewMode" size="small" @change="reload">
              <el-radio-button label="today">今日预约</el-radio-button>
              <el-radio-button label="all">全部预约</el-radio-button>
            </el-radio-group>
            <template v-if="viewMode === 'today'">
              <el-select v-model="timeSlotFilter" placeholder="时间段" size="small" style="width: 120px; margin-left: 10px" @change="reload">
                <el-option label="全部" value="" />
                <el-option label="上午" value="MORNING" />
                <el-option label="下午" value="AFTERNOON" />
              </el-select>
              <el-select v-model="todayStatusFilter" placeholder="状态筛选" size="small" style="width: 120px; margin-left: 10px" clearable @change="reload">
                <el-option label="全部" value="" />
                <el-option label="待就诊" value="待就诊" />
                <el-option label="已完成" value="已完成" />
                <el-option label="已取消" value="已取消" />
                <el-option label="未到诊" value="未到诊" />
              </el-select>
              <div class="today-stats">
                <el-tag type="warning" effect="light">上午：{{ morningCount }}</el-tag>
                <el-tag type="success" effect="light">下午：{{ afternoonCount }}</el-tag>
                <el-tag type="primary" effect="light">合计：{{ pagination.total }}</el-tag>
              </div>
            </template>
            <template v-else>
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索患者姓名或电话"
                style="width: 250px; margin-left: 10px"
                clearable
                @input="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="searchForm.status"
                placeholder="状态筛选"
                style="width: 120px; margin-left: 10px"
                clearable
                @change="handleSearch"
              >
                <el-option label="全部" value="" />
                <el-option label="待就诊" value="待就诊" />
                <el-option label="已完成" value="已完成" />
                <el-option label="已取消" value="已取消" />
                <el-option label="未到诊" value="未到诊" />
              </el-select>
            </template>
          </div>
        </div>
      </template>

      <!-- 患者列表 -->
      <el-table
        v-loading="loading"
        :data="patientList"
        style="width: 100%"
        @sort-change="handleSortChange"
        @row-click="viewPatientDetail"
        :row-style="{ cursor: 'pointer' }"
      >
        <el-table-column prop="name" label="患者姓名" min-width="150" />
        <el-table-column prop="phone" label="联系电话" min-width="150" />
        <el-table-column v-if="viewMode !== 'today'" prop="appointmentDate" label="预约日期" min-width="150" sortable="custom">
          <template #default="scope">
            {{ formatDateOnly(scope.row.appointmentDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时段" min-width="120">
          <template #default="scope">
            {{ scope.row.timeSlotName || formatTimeSlot(scope.row.timeSlot) || scope.row.appointmentTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.statusName || scope.row.status)" size="small">
              {{ getAppointmentStatusText(scope.row.statusName || scope.row.status) }}
            </el-tag>
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
          :prev-text="'上一页'"
          :next-text="'下一页'"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <template #total="{ total }">
            共 {{ total }} 条
          </template>
        </el-pagination>
      </div>
    </el-card>

    <!-- 患者详情对话框 -->
    <el-dialog
      v-model="patientDialogVisible"
      title="患者详情"
      width="900px"
    >
      <div v-if="selectedPatient" class="patient-detail">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="患者姓名">
                {{ selectedPatient.name }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ selectedPatient.phoneNumber }}
              </el-descriptions-item>
              <el-descriptions-item label="患者身份">
                {{ getSpecificRoleText(selectedPatient.specificRole) }}
              </el-descriptions-item>
              <el-descriptions-item label="是否曾就诊">
                <el-tag :type="selectedPatient.hasVisited ? 'success' : 'info'" size="small">
                  {{ selectedPatient.hasVisited ? '是' : '否' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 就诊历史 -->
          <el-tab-pane label="就诊历史" name="history">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="总预约次数">
                <el-tag type="primary">{{ selectedPatient.totalAppointments || 0 }} 次</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="已完成就诊">
                <el-tag type="success">{{ selectedPatient.completedAppointments || 0 }} 次</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="最近就诊日期" :span="2">
                {{ selectedPatient.lastVisitDate ? formatDateTime(selectedPatient.lastVisitDate) : '暂无记录' }}
              </el-descriptions-item>
              <el-descriptions-item label="最近就诊状态" :span="2">
                <el-tag v-if="selectedPatient.lastStatusName" :type="getStatusType(selectedPatient.lastStatusName)" size="small">
                  {{ selectedPatient.lastStatusName }}
                </el-tag>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 即将到诊 -->
          <el-tab-pane label="即将到诊" name="upcoming" v-if="selectedPatient.nextAppointmentId">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="预约ID">
                {{ selectedPatient.nextAppointmentId }}
              </el-descriptions-item>
              <el-descriptions-item label="预约日期">
                {{ formatDateTime(selectedPatient.nextAppointmentDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="预约时段" :span="2">
                {{ formatTimeSlot(selectedPatient.nextAppointmentTimeSlot) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="即将到诊" name="upcoming" v-else>
            <el-empty description="暂无即将到诊的预约" />
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="patientDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMyPatients, getTodayPatients } from '@/api/doctor'
import { getDoctorPatients } from '@/api/appointment'
import { formatDate } from '@/utils'

const viewMode = ref('today')
const timeSlotFilter = ref('')
const todayStatusFilter = ref('')
const morningCount = ref(0)
const afternoonCount = ref(0)
const eveningCount = ref(0)

const loading = ref(false)
const patientDialogVisible = ref(false)
const selectedPatient = ref(null)
const activeTab = ref('basic')

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 实际患者数据
const patientList = ref([])

// 获取预约状态文本（将英文状态转换为中文）
const getAppointmentStatusText = (status) => {
  if (!status) return ''
  
  // 如果已经是中文，直接返回
  const chineseStatuses = ['待就诊', '已完成', '已取消', '未到诊', '就诊中', '已确认']
  if (chineseStatuses.includes(status)) {
    return status
  }
  
  // 将英文状态转换为中文（根据数据库定义：scheduled/cancelled/completed/no_show）
  const statusMap = {
    'SCHEDULED': '待就诊',
    'scheduled': '待就诊',
    'COMPLETED': '已完成',
    'completed': '已完成',
    'CANCELLED': '已取消',
    'cancelled': '已取消',
    'NO_SHOW': '未到诊',
    'no_show': '未到诊'
  }
  
  // 转换为大写进行匹配（不区分大小写）
  const upperStatus = String(status).toUpperCase()
  return statusMap[upperStatus] || status
}

// 格式化日期（只显示日期部分，不显示时间）
const formatDateOnly = (dateStr) => {
  if (!dateStr) return ''
  
  // 如果是 ISO 格式的日期时间字符串，提取日期部分
  if (typeof dateStr === 'string' && dateStr.includes('T')) {
    return dateStr.split('T')[0]
  }
  
  // 如果已经是日期格式（YYYY-MM-DD），直接返回
  if (typeof dateStr === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(dateStr)) {
    return dateStr
  }
  
  // 如果是 Date 对象或其他格式，使用 formatDate 格式化
  try {
    return formatDate(new Date(dateStr))
  } catch (e) {
    return dateStr
  }
}

// 加载全部患者列表
const loadPatients = async () => {
  try {
    loading.value = true
    // 将中文状态映射为后端状态码（根据数据库定义：scheduled/cancelled/completed/no_show）
    const statusMap = {
      '待就诊': 'scheduled',
      '已完成': 'completed',
      '已取消': 'cancelled',
      '未到诊': 'no_show'
    }
    
    const params = {
      patientName: searchForm.keyword || undefined,
      status: searchForm.status ? statusMap[searchForm.status] : undefined
    }
    const resp = await getMyPatients(params)
    const rawList = Array.isArray(resp?.data?.patients)
      ? resp.data.patients
      : Array.isArray(resp?.data)
        ? resp.data
        : []
    const total = typeof resp?.data?.total === 'number' ? resp.data.total : rawList.length

    // 映射时间段
    const mapTimeSlotToCN = (t) => (t === 'MORNING' ? '上午' : t === 'AFTERNOON' ? '下午' : t || '')

    patientList.value = rawList.map(item => {
      // 处理预约日期：从 appointmentTime 或 scheduleDate 中提取日期部分
      let appointmentDate = item.scheduleDate || ''
      if (!appointmentDate && item.appointmentTime) {
        const dateTimeStr = String(item.appointmentTime)
        appointmentDate = dateTimeStr.includes('T') ? dateTimeStr.split('T')[0] : dateTimeStr.split(' ')[0]
      }
      
      return {
        // 保留所有后端返回的原始字段
        appointmentId: item.appointmentId,
        patientId: item.patientId,
        patientName: item.patientName,
        phoneNumber: item.phoneNumber,
        idCardNumber: item.idCardNumber,
        specificRole: item.specificRole,
        appointmentTime: item.appointmentTime,
        status: item.status,
        statusName: item.statusName,
        fee: item.fee,
        actualFee: item.actualFee,
        sourceType: item.sourceType,
        createdAt: item.createdAt,
        scheduleId: item.scheduleId,
        scheduleDate: item.scheduleDate,
        timeSlot: item.timeSlot,
        timeSlotName: item.timeSlotName,
        // 兼容字段（用于显示）
        id: item.patientId || item.appointmentId || item.id,
        name: item.patientName || item.name,
        phone: item.patientPhone || item.phoneNumber,
        appointmentDate: formatDateOnly(appointmentDate),
        appointmentTime: item.timeSlotName || formatTimeSlot(item.timeSlot) || mapTimeSlotToCN(item.timeSlot)
      }
    })
    pagination.total = total
  } catch (error) {
    console.error('加载患者列表失败:', error)
    ElMessage.error('加载患者列表失败')
  } finally {
    loading.value = false
  }
}

const mapTimeSlotToCN = (t) => {
  const m = { MORNING: '上午', AFTERNOON: '下午', EVENING: '晚上', morning: '上午', afternoon: '下午', evening: '晚上' }
  return m[t] || ''
}

const loadToday = async () => {
  try {
    loading.value = true
    const params = { timeSlot: timeSlotFilter.value || undefined }
    const resp = await getTodayPatients(params)
    let rawList = Array.isArray(resp?.data?.patients) ? resp.data.patients : []
    
    // 统计上午下午人数（从全部数据中统计）
    morningCount.value = rawList.filter(item => {
      const slot = String(item.timeSlot || '').toUpperCase()
      return slot === 'MORNING'
    }).length
    afternoonCount.value = rawList.filter(item => {
      const slot = String(item.timeSlot || '').toUpperCase()
      return slot === 'AFTERNOON'
    }).length
    
    // 根据状态筛选过滤
    if (todayStatusFilter.value) {
      const statusMap = {
        '待就诊': ['SCHEDULED', 'scheduled', '待就诊'],
        '已完成': ['COMPLETED', 'completed', '已完成'],
        '已取消': ['CANCELLED', 'cancelled', '已取消'],
        '未到诊': ['NO_SHOW', 'no_show', '未到诊']
      }
      const targetStatuses = statusMap[todayStatusFilter.value] || []
      rawList = rawList.filter(item => targetStatuses.includes(item.status || item.statusName))
    }
    
    pagination.total = rawList.length
    patientList.value = rawList.map(item => {
      // 处理预约日期：从 scheduleDate 或 appointmentTime 中提取日期部分
      let appointmentDate = item.scheduleDate || ''
      if (!appointmentDate && item.appointmentTime) {
        const dateTimeStr = String(item.appointmentTime)
        appointmentDate = dateTimeStr.includes('T') ? dateTimeStr.split('T')[0] : dateTimeStr.split(' ')[0]
      }
      
      return {
        // 保留所有后端返回的原始字段
        appointmentId: item.appointmentId,
        patientId: item.patientId,
        patientName: item.patientName,
        phoneNumber: item.phoneNumber,
        idCardNumber: item.idCardNumber,
        specificRole: item.specificRole,
        appointmentTime: item.appointmentTime,
        status: item.status,
        statusName: item.statusName,
        fee: item.fee,
        actualFee: item.actualFee,
        sourceType: item.sourceType,
        createdAt: item.createdAt,
        scheduleId: item.scheduleId,
        scheduleDate: item.scheduleDate,
        timeSlot: item.timeSlot,
        timeSlotName: item.timeSlotName,
        // 兼容字段（用于显示）
        id: item.patientId,
        name: item.patientName,
        phone: item.phoneNumber,
        appointmentDate: formatDateOnly(appointmentDate),
        appointmentTime: item.timeSlotName || formatTimeSlot(item.timeSlot) || mapTimeSlotToCN(item.timeSlot)
      }
    })
  } catch (error) {
    console.error('加载今日预约失败:', error)
    ElMessage.error('加载今日预约失败')
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pagination.currentPage = 1
  if (viewMode.value === 'today') {
    loadToday()
  } else {
    loadPatients()
  }
}

// 身份证号脱敏处理
const maskIdCard = (idCard) => {
  if (!idCard || idCard === '-') return '-'
  const str = String(idCard)
  // 如果已经是脱敏格式（包含*），直接返回
  if (str.includes('*')) return str
  // 保留前4位和后4位，中间用*代替
  if (str.length >= 8) {
    return str.substring(0, 4) + '********' + str.substring(str.length - 4)
  }
  // 如果长度不够，全部用*代替
  return '********'
}

const getStatusType = (status) => {
  if (!status) return 'info'
  
  // 先转换为中文状态
  const chineseStatus = getAppointmentStatusText(status)
  
  // 根据中文状态返回对应的颜色类型
  const statusMap = {
    '待就诊': 'warning',      // 橙色/黄色
    '已完成': 'success',      // 绿色
    '已取消': 'danger',       // 红色
    '未到诊': 'info'          // 灰色
  }
  
  return statusMap[chineseStatus] || 'info'
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadPatients()
}

const handleSortChange = ({ column, prop, order }) => {
  // 可按需要实现后端/前端排序，这里暂不处理
  loadPatients()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadPatients()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadPatients()
}

// 格式化金额
const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期时间（显示日期和时间）
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  // 如果是 ISO 格式或数据库格式，直接显示
  if (typeof dateTimeStr === 'string') {
    // 如果包含时间部分，直接返回
    if (dateTimeStr.includes(' ') || dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }
    return dateTimeStr
  }
  return dateTimeStr
}

// 格式化时间段
const formatTimeSlot = (timeSlot) => {
  if (!timeSlot) return ''
  const slotMap = {
    'MORNING': '上午',
    'morning': '上午',
    'AFTERNOON': '下午',
    'afternoon': '下午',
    'EVENING': '晚上',
    'evening': '晚上'
  }
  return slotMap[timeSlot] || timeSlot
}

// 获取具体角色文本
const getSpecificRoleText = (role) => {
  if (!role) return '-'
  const roleMap = {
    'student': '学生',
    'teacher': '教师',
    'staff': '职工',
    'external': '校外人员'
  }
  return roleMap[role] || role
}

// 获取来源类型文本（将英文转换为中文）
const getSourceTypeText = (sourceType) => {
  if (!sourceType) return '-'
  
  // 如果已经是中文，直接返回
  const chineseSources = ['线上预约', '线下预约', '电话预约', '候补预约']
  if (chineseSources.includes(sourceType)) {
    return sourceType
  }
  
  // 将英文来源类型转换为中文（不区分大小写）
  const sourceMap = {
    'ONLINE': '线上预约',
    'OFFLINE': '线下预约',
    'PHONE': '电话预约',
    'WAITLIST': '候补预约'
  }
  
  // 转换为大写进行匹配
  const upperSource = String(sourceType).toUpperCase()
  return sourceMap[upperSource] || sourceType
}

const viewPatientDetail = async (patient) => {
  try {
    loading.value = true
    // 调用新接口获取患者详细信息（包含就诊历史）
    const resp = await getDoctorPatients()
    const patientList = Array.isArray(resp?.data) ? resp.data : []
    
    // 根据 patientId 查找对应的患者详情
    const patientDetail = patientList.find(p => p.patientId === patient.patientId)
    
    if (patientDetail) {
      selectedPatient.value = patientDetail
    } else {
      // 如果没找到，使用列表中的基本数据
      selectedPatient.value = {
        ...patient,
        hasVisited: false,
        totalAppointments: 0,
        completedAppointments: 0
      }
    }
    
    activeTab.value = 'basic'
    patientDialogVisible.value = true
  } catch (error) {
    console.error('获取患者详情失败:', error)
    ElMessage.error('获取患者详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.doctor-patients {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.today-stats { display: flex; align-items: center; gap: 6px; }

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.patient-detail {
  margin-bottom: 20px;
}
</style>