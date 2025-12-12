<template>
  <div class="appointments-management">
    <!-- 搜索和筛选栏 -->
    <el-card class="search-card">
      <el-row :gutter="16">
        <el-col :span="5">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索预约ID、患者姓名或医生姓名"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.status"
            placeholder="预约状态"
            clearable
            @change="handleSearch"
            style="width: 100%"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待就诊" value="scheduled" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.sourceType"
            placeholder="来源类型"
            clearable
            @change="handleSearch"
            style="width: 100%"
          >
            <el-option label="全部来源" value="" />
            <el-option label="线上预约" value="ONLINE" />
            <el-option label="线下预约" value="OFFLINE" />
            <el-option label="电话预约" value="PHONE" />
            <el-option label="候补预约" value="WAITLIST" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="searchForm.department"
            placeholder="科室"
            clearable
            @change="handleSearch"
            style="width: 100%"
          >
            <el-option label="全部科室" value="" />
            <el-option 
              v-for="dept in departmentList" 
              :key="dept.id" 
              :label="dept.name" 
              :value="dept.id" 
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button @click="resetSearch" style="width: 100%">重置</el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadAppointments" style="width: 100%">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 批量操作卡片 -->
    <el-card v-if="selectedAppointments.length > 0" class="batch-card">
      <div class="batch-operations">
        <span class="selected-info">已选择 {{ selectedAppointments.length }} 个预约</span>
        <div class="batch-buttons">
          <el-button
            type="danger"
            size="small"
            @click="handleBatchDelete"
            :disabled="selectedAppointments.length === 0"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 预约列表 -->
    <el-card class="table-card">
      <el-table
        :data="filteredAppointments"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="预约ID" width="80" />
        <el-table-column label="患者" width="100">
          <template #default="{ row }">
            {{ getPatientName(row.patientId) }}
          </template>
        </el-table-column>
        <el-table-column label="科室" width="80">
          <template #default="{ row }">
            {{ getDepartmentName(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="门诊" width="130">
          <template #default="{ row }">
            {{ getClinicName(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="医生" width="100">
          <template #default="{ row }">
            {{ getDoctorNameOnly(row.doctorId) }}
          </template>
        </el-table-column>
        <el-table-column label="排班日期" width="120">
          <template #default="{ row }">
            {{ getScheduleDateShort(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="时间段" width="80" align="center">
          <template #default="{ row }">
            {{ getTimeSlotText(row.scheduleId) }}
          </template>
        </el-table-column>
        <el-table-column label="号源类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getSlotTypeTagType(row.scheduleId)" size="small">
              {{ getSlotTypeText(row.scheduleId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="来源类型" width="100" align="center">
          <template #default="{ row }">
            {{ getSourceTypeText(row.sourceType) }}
          </template>
        </el-table-column>
        <el-table-column label="预约状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="挂号费" width="90" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.fee) }}
          </template>
        </el-table-column>
        <el-table-column label="实际费用" width="90" align="right">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600;">
              ¥{{ formatMoney(row.actualFee) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewAppointment(row)">
              查看详情
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="filteredAppointments.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无预约数据" />
      </div>
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          :current-page="pagination.currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预约详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="800px"
    >
      <div v-if="selectedAppointment" class="appointment-detail">
        <el-tabs v-model="activeTab">
          <!-- 患者信息 -->
          <el-tab-pane label="患者信息" name="patient">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="患者姓名">
                {{ selectedAppointment.patientInfo?.name || getPatientName(selectedAppointment.patientId) }}
              </el-descriptions-item>
              <el-descriptions-item label="患者ID">
                {{ selectedAppointment.patientId }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ selectedAppointment.patientInfo?.phoneNumber || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="身份证号">
                {{ maskIdCard(selectedAppointment.patientInfo?.idCardNumber) }}
              </el-descriptions-item>
              <el-descriptions-item label="具体角色">
                {{ getSpecificRoleText(selectedAppointment.patientInfo?.specificRole) }}
              </el-descriptions-item>
              <el-descriptions-item label="身份状态">
                {{ getIdStatusText(selectedAppointment.patientInfo?.idStatus) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 预约信息 -->
          <el-tab-pane label="预约信息" name="appointment">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="预约ID">
                {{ selectedAppointment.id }}
              </el-descriptions-item>
              <el-descriptions-item label="预约状态">
                <el-tag :type="getStatusType(selectedAppointment.status)" size="small">
                  {{ getStatusText(selectedAppointment.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="医生">
                {{ getDoctorName(selectedAppointment.doctorId) }}
              </el-descriptions-item>
              <el-descriptions-item label="医生职称">
                {{ selectedAppointment.doctorInfo?.title || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="科室">
                {{ getDepartmentName(selectedAppointment.scheduleId) }}
              </el-descriptions-item>
              <el-descriptions-item label="门诊">
                {{ getClinicName(selectedAppointment.scheduleId) }}
              </el-descriptions-item>
              <el-descriptions-item label="排班日期">
                {{ getScheduleDate(selectedAppointment.scheduleId) }}
              </el-descriptions-item>
              <el-descriptions-item label="时间段">
                {{ getTimeSlotText(selectedAppointment.scheduleId) }}
              </el-descriptions-item>
              <el-descriptions-item label="号源类型">
                {{ getSlotTypeText(selectedAppointment.scheduleId) }}
              </el-descriptions-item>
              <el-descriptions-item label="预约时间">
                {{ formatDateTime(selectedAppointment.appointmentTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(selectedAppointment.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="挂号费用">
                ¥{{ formatMoney(selectedAppointment.fee) }}
              </el-descriptions-item>
              <el-descriptions-item label="实际费用">
                <span style="color: #67c23a; font-weight: 600;">
                  ¥{{ formatMoney(selectedAppointment.actualFee) }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="来源类型">
                {{ getSourceTypeText(selectedAppointment.sourceType) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { getAppointmentList, getAppointmentById, deleteAppointment } from '@/api/appointment'
import { getPatientById } from '@/api/patient'
import { getDoctorById } from '@/api/doctor'
import { getScheduleById, getScheduleDetailsById } from '@/api/schedule'
import { getDepartmentList } from '@/api/department'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  sourceType: '',
  department: ''
})

// 科室列表
const departmentList = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 数据
const loading = ref(false)
const appointments = ref([])
const selectedAppointments = ref([])
const detailDialogVisible = ref(false)
const selectedAppointment = ref(null)
const activeTab = ref('patient')

// 缓存映射：用于存储患者、医生、排班信息
const patientMap = ref(new Map())
const doctorMap = ref(new Map())
const scheduleMap = ref(new Map())

// 筛选后的预约列表
const filteredAppointments = computed(() => {
  let result = [...appointments.value] // 创建副本，避免修改原数组
  
  console.log('开始筛选，原始数据量:', result.length)
  console.log('筛选条件:', {
    keyword: searchForm.keyword,
    status: searchForm.status,
    sourceType: searchForm.sourceType,
    department: searchForm.department
  })

  // 关键词搜索
  if (searchForm.keyword && searchForm.keyword.trim()) {
    const keyword = searchForm.keyword.trim()
    const keywordLower = keyword.toLowerCase()
    const beforeCount = result.length
    
    // 判断是否为纯数字（用于精确匹配预约ID）
    const isNumeric = /^\d+$/.test(keyword)
    
    result = result.filter(item => {
      // 如果是纯数字，只匹配预约ID（精确匹配或包含匹配）
      if (isNumeric) {
        const itemId = String(item.id)
        // 精确匹配ID
        if (itemId === keyword) {
          return true
        }
        // 也支持包含匹配（如搜索"1"可以匹配ID 1、10、11、21等）
        if (itemId.includes(keyword)) {
          return true
        }
        // 纯数字输入时，不匹配其他字段（患者ID、医生ID、姓名等）
        return false
      } else {
        // 非数字关键词，匹配所有字段
        const itemId = String(item.id)
        if (itemId.includes(keyword)) {
          return true
        }
        
        // 匹配患者姓名、医生姓名、患者ID、医生ID
        const patientName = getPatientName(item.patientId).toLowerCase()
        const doctorName = getDoctorNameOnly(item.doctorId).toLowerCase()
        const patientIdStr = String(item.patientId)
        const doctorIdStr = String(item.doctorId)
        
        return patientName.includes(keywordLower) ||
               doctorName.includes(keywordLower) ||
               patientIdStr.includes(keyword) ||
               doctorIdStr.includes(keyword)
      }
    })
    console.log(`关键词筛选: ${beforeCount} -> ${result.length}`, {
      keyword,
      isNumeric,
      matchedIds: result.map(r => r.id).slice(0, 10)
    })
  }

  // 状态筛选（前端筛选，支持各种格式）
  if (searchForm.status && searchForm.status !== '') {
    const beforeCount = result.length
    const filterValue = String(searchForm.status).toLowerCase().trim()
    
    // 状态值映射：根据数据库实际值（scheduled/cancelled/completed）
    const statusMap = {
      'scheduled': ['scheduled', 'SCHEDULED', '待就诊'],
      'completed': ['completed', 'COMPLETED', '已完成'],
      'cancelled': ['cancelled', 'CANCELLED', '已取消']
    }
    
    // 获取筛选条件对应的所有可能的状态值
    const possibleStatuses = statusMap[filterValue] || [filterValue]
    console.log('状态筛选 - 筛选值:', filterValue, '可能的状态值:', possibleStatuses)
    
    result = result.filter(item => {
      if (!item || !item.status) {
        console.log('状态筛选 - 跳过无状态项:', item?.id)
        return false
      }
      const itemStatus = String(item.status).trim()
      const itemStatusLower = itemStatus.toLowerCase()
      
      // 检查是否匹配任何可能的状态值（不区分大小写）
      const matches = possibleStatuses.some(status => 
        itemStatusLower === status.toLowerCase()
      )
      
      if (beforeCount <= 5) { // 只在数据量少时输出详细日志
        console.log('状态筛选 - 检查项:', {
          id: item.id,
          itemStatus,
          itemStatusLower,
          matches
        })
      }
      
      return matches
    })
    console.log(`状态筛选: ${beforeCount} -> ${result.length}`)
  }

  // 来源类型筛选
  if (searchForm.sourceType && searchForm.sourceType !== '') {
    const beforeCount = result.length
    const sourceFilter = String(searchForm.sourceType).toUpperCase().trim()
    result = result.filter(item => {
      if (!item.sourceType) return false
      const itemSource = String(item.sourceType).toUpperCase().trim()
      return itemSource === sourceFilter
    })
    console.log(`来源类型筛选: ${beforeCount} -> ${result.length}`)
  }

  // 科室筛选
  if (searchForm.department && searchForm.department !== '') {
    const beforeCount = result.length
    result = result.filter(item => {
      if (!item.scheduleId) return false
      const schedule = scheduleMap.value.get(item.scheduleId)
      if (!schedule) return false
      // 支持数字和字符串比较
      const scheduleDeptId = schedule.departmentId
      const filterDeptId = searchForm.department
      return scheduleDeptId == filterDeptId // 使用 == 支持类型转换
    })
    console.log(`科室筛选: ${beforeCount} -> ${result.length}`)
  }

  // 分页
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  pagination.total = result.length
  const paginatedResult = result.slice(start, end)
  console.log('最终筛选结果:', {
    total: result.length,
    page: pagination.currentPage,
    pageSize: pagination.pageSize,
    showing: paginatedResult.length
  })
  return paginatedResult
})

// 批量加载患者信息
const loadPatientInfo = async (patientIds) => {
  const uniqueIds = [...new Set(patientIds.filter(id => id && !patientMap.value.has(id)))]
  if (uniqueIds.length === 0) return

  const promises = uniqueIds.map(async (id) => {
    try {
      const resp = await getPatientById(id)
      if (resp?.data) {
        patientMap.value.set(id, resp.data)
      }
    } catch (error) {
      console.warn(`获取患者${id}信息失败:`, error)
    }
  })
  await Promise.all(promises)
}

// 批量加载医生信息
const loadDoctorInfo = async (doctorIds) => {
  const uniqueIds = [...new Set(doctorIds.filter(id => id && !doctorMap.value.has(id)))]
  if (uniqueIds.length === 0) return

  const promises = uniqueIds.map(async (id) => {
    try {
      const resp = await getDoctorById(id)
      if (resp?.data) {
        doctorMap.value.set(id, resp.data)
      }
    } catch (error) {
      console.warn(`获取医生${id}信息失败:`, error)
    }
  })
  await Promise.all(promises)
}

// 批量加载排班信息
const loadScheduleInfo = async (scheduleIds) => {
  const uniqueIds = [...new Set(scheduleIds.filter(id => id && !scheduleMap.value.has(id)))]
  if (uniqueIds.length === 0) return

  const promises = uniqueIds.map(async (id) => {
    try {
      // 优先使用详细信息接口
      let resp = await getScheduleDetailsById(id)
      if (!resp?.data) {
        // 如果详细信息接口失败，尝试普通接口
        resp = await getScheduleById(id)
      }
      if (resp?.data) {
        scheduleMap.value.set(id, resp.data)
      }
    } catch (error) {
      console.warn(`获取排班${id}信息失败:`, error)
    }
  })
  await Promise.all(promises)
}

// 加载预约列表
const loadAppointments = async () => {
  try {
    loading.value = true
    const resp = await getAppointmentList()
    const list = Array.isArray(resp?.data) ? resp.data : []
    
    // 调试：检查状态值
    if (list.length > 0) {
      const uniqueStatuses = [...new Set(list.map(item => item.status).filter(Boolean))]
      console.log('预约列表中的状态值:', uniqueStatuses)
    }
    
    appointments.value = list
    // 注意：pagination.total 会在 computed 中自动计算，这里不需要设置
    pagination.currentPage = 1

    // 批量加载关联信息
    const patientIds = list.map(item => item.patientId).filter(Boolean)
    const doctorIds = list.map(item => item.doctorId).filter(Boolean)
    const scheduleIds = list.map(item => item.scheduleId).filter(Boolean)

    await Promise.all([
      loadPatientInfo(patientIds),
      loadDoctorInfo(doctorIds),
      loadScheduleInfo(scheduleIds)
    ])
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化金额
const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  if (typeof dateTimeStr === 'string') {
    return dateTimeStr.replace('T', ' ').split('.')[0]
  }
  return dateTimeStr
}

// 获取状态文本（将英文状态转换为中文）
const getStatusText = (status) => {
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
  
  // 转换为小写进行匹配（不区分大小写）
  const lowerStatus = String(status).toLowerCase()
  return statusMap[status] || statusMap[lowerStatus] || status
}

// 获取状态类型（用于标签颜色，使用和医生端一样的颜色方案）
const getStatusType = (status) => {
  if (!status) return 'info'
  
  // 先转换为中文状态
  const chineseStatus = getStatusText(status)
  
  // 根据中文状态返回对应的颜色类型
  const statusMap = {
    '待就诊': 'warning',      // 橙色/黄色
    '已完成': 'success',      // 绿色
    '已取消': 'danger',       // 红色
    '未到诊': 'info'          // 灰色
  }
  
  return statusMap[chineseStatus] || 'info'
}

// 获取患者姓名
const getPatientName = (patientId) => {
  if (!patientId) return '-'
  const patient = patientMap.value.get(patientId)
  return patient?.name || `患者${patientId}`
}

// 获取医生姓名（带ID）
const getDoctorName = (doctorId) => {
  if (!doctorId) return '-'
  const doctor = doctorMap.value.get(doctorId)
  return doctor ? `${doctorId} - ${doctor.name}` : `医生${doctorId}`
}

// 获取医生姓名（仅姓名）
const getDoctorNameOnly = (doctorId) => {
  if (!doctorId) return '-'
  const doctor = doctorMap.value.get(doctorId)
  return doctor?.name || '-'
}

// 获取科室名称
const getDepartmentName = (scheduleId) => {
  if (!scheduleId) return '-'
  const schedule = scheduleMap.value.get(scheduleId)
  return schedule?.departmentName || '-'
}

// 获取门诊名称
const getClinicName = (scheduleId) => {
  if (!scheduleId) return '-'
  const schedule = scheduleMap.value.get(scheduleId)
  return schedule?.clinicName || '-'
}

// 获取来源类型文本
const getSourceTypeText = (sourceType) => {
  if (!sourceType) return '-'
  const sourceMap = {
    'ONLINE': '线上预约',
    'OFFLINE': '线下预约',
    'PHONE': '电话预约',
    'WAITLIST': '候补预约'
  }
  return sourceMap[sourceType] || sourceType
}

// 获取排班日期
const getScheduleDate = (scheduleId) => {
  if (!scheduleId) return '-'
  const schedule = scheduleMap.value.get(scheduleId)
  if (!schedule?.scheduleDate) return '-'
  
  // 格式化日期
  const date = new Date(schedule.scheduleDate)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdays[date.getDay()]
  
  return `${year}-${month}-${day} (${weekday})`
}

// 获取时间段文本
const getTimeSlotText = (scheduleId) => {
  if (!scheduleId) return '-'
  const schedule = scheduleMap.value.get(scheduleId)
  if (!schedule?.timeSlot) return '-'
  
  const timeSlot = String(schedule.timeSlot).toUpperCase()
  const timeSlotMap = {
    'MORNING': '上午',
    'AFTERNOON': '下午',
    'EVENING': '晚上'
  }
  return timeSlotMap[timeSlot] || schedule.timeSlot
}

// 获取号源类型文本
const getSlotTypeText = (scheduleId) => {
  if (!scheduleId) return '-'
  const schedule = scheduleMap.value.get(scheduleId)
  if (!schedule?.slotType) return '-'
  
  const slotType = String(schedule.slotType).toUpperCase()
  const slotTypeMap = {
    'EXPERT': '专家号',
    'REGULAR': '普通号',
    'EMERGENCY': '急诊号',
    'SPECIAL': '特需号'
  }
  return slotTypeMap[slotType] || schedule.slotType
}

// 获取排班日期（短格式，用于表格）
const getScheduleDateShort = (scheduleId) => {
  if (!scheduleId) return '-'
  const schedule = scheduleMap.value.get(scheduleId)
  if (!schedule?.scheduleDate) return '-'
  
  const date = new Date(schedule.scheduleDate)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  return `${month}-${day}`
}

// 获取号源类型标签颜色
const getSlotTypeTagType = (scheduleId) => {
  if (!scheduleId) return ''
  const schedule = scheduleMap.value.get(scheduleId)
  if (!schedule?.slotType) return ''
  
  const slotType = String(schedule.slotType).toUpperCase()
  const typeMap = {
    'EXPERT': 'danger',    // 专家号 - 红色
    'REGULAR': '',         // 普通号 - 默认
    'EMERGENCY': 'warning', // 急诊号 - 橙色
    'SPECIAL': 'success'   // 特需号 - 绿色
  }
  return typeMap[slotType] || ''
}

// 搜索处理
const handleSearch = () => {
  console.log('handleSearch 被调用，当前筛选条件:', {
    status: searchForm.status,
    keyword: searchForm.keyword,
    sourceType: searchForm.sourceType,
    department: searchForm.department
  })
  pagination.currentPage = 1
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.sourceType = ''
  searchForm.department = ''
  pagination.currentPage = 1
}

// 加载科室列表
const loadDepartments = async () => {
  try {
    const resp = await getDepartmentList()
    departmentList.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    console.error('加载科室列表失败:', error)
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedAppointments.value = selection
}

// 批量删除预约
const handleBatchDelete = async () => {
  if (selectedAppointments.value.length === 0) {
    ElMessage.warning('请先选择要删除的预约')
    return
  }

  try {
    const appointmentIds = selectedAppointments.value.map(apt => `ID:${apt.id}`).join('、')
    await ElMessageBox.confirm(
      `确定要删除以下 ${selectedAppointments.value.length} 个预约吗？\n${appointmentIds}\n\n此操作不可恢复！`,
      '确认批量删除',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    // 批量删除
    const deletePromises = selectedAppointments.value.map(apt => deleteAppointment(apt.id))
    await Promise.all(deletePromises)
    
    ElMessage.success(`成功删除 ${selectedAppointments.value.length} 个预约`)
    selectedAppointments.value = []
    await loadAppointments() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
    }
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

// 获取身份状态文本
const getIdStatusText = (status) => {
  if (!status) return '-'
  const statusMap = {
    'pending': '待认证',
    'verified': '已认证'
  }
  return statusMap[status] || status
}

// 查看详情
const viewAppointment = async (appointment) => {
  try {
    loading.value = true
    activeTab.value = 'patient'
    
    // 获取预约详情
    const resp = await getAppointmentById(appointment.id)
    const appointmentData = resp?.data || appointment
    
    // 获取患者信息
    let patientInfo = null
    if (appointmentData.patientId) {
      try {
        if (patientMap.value.has(appointmentData.patientId)) {
          patientInfo = patientMap.value.get(appointmentData.patientId)
        } else {
          const patientResp = await getPatientById(appointmentData.patientId)
          patientInfo = patientResp?.data
          if (patientInfo) {
            patientMap.value.set(appointmentData.patientId, patientInfo)
          }
        }
      } catch (error) {
        console.warn('获取患者信息失败:', error)
      }
    }
    
    // 获取医生信息
    let doctorInfo = null
    if (appointmentData.doctorId) {
      try {
        if (doctorMap.value.has(appointmentData.doctorId)) {
          doctorInfo = doctorMap.value.get(appointmentData.doctorId)
        } else {
          const doctorResp = await getDoctorById(appointmentData.doctorId)
          doctorInfo = doctorResp?.data
          if (doctorInfo) {
            doctorMap.value.set(appointmentData.doctorId, doctorInfo)
          }
        }
      } catch (error) {
        console.warn('获取医生信息失败:', error)
      }
    }
    
    // 获取排班信息
    let scheduleInfo = null
    if (appointmentData.scheduleId) {
      try {
        if (scheduleMap.value.has(appointmentData.scheduleId)) {
          scheduleInfo = scheduleMap.value.get(appointmentData.scheduleId)
        } else {
          // 优先使用详细信息接口
          let scheduleResp = await getScheduleDetailsById(appointmentData.scheduleId)
          if (!scheduleResp?.data) {
            scheduleResp = await getScheduleById(appointmentData.scheduleId)
          }
          scheduleInfo = scheduleResp?.data
          if (scheduleInfo) {
            scheduleMap.value.set(appointmentData.scheduleId, scheduleInfo)
          }
        }
      } catch (error) {
        console.warn('获取排班信息失败:', error)
      }
    }
    
    selectedAppointment.value = {
      ...appointmentData,
      patientInfo,
      doctorInfo,
      scheduleInfo
    }
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
    // 如果获取详情失败，使用列表中的数据
    selectedAppointment.value = {
      ...appointment,
      patientInfo: patientMap.value.get(appointment.patientId) || null,
      doctorInfo: doctorMap.value.get(appointment.doctorId) || null,
      scheduleInfo: scheduleMap.value.get(appointment.scheduleId) || null
    }
    detailDialogVisible.value = true
  } finally {
    loading.value = false
  }
}

// 删除预约
const handleDelete = async (appointment) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除预约ID为 ${appointment.id} 的预约记录吗？删除后将尝试候补填充，若无候补则归还号源。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    await deleteAppointment(appointment.id)
    ElMessage.success('删除成功')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除预约失败:', error)
      ElMessage.error('删除预约失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDepartments()
  loadAppointments()
})
</script>

<style scoped>
.appointments-management {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.batch-card {
  margin-bottom: 16px;
  background: #fff7e6;
  border: 1px solid #ffd666;
}

.batch-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-info {
  font-size: 14px;
  color: #faad14;
  font-weight: 500;
}

.batch-buttons {
  display: flex;
  gap: 12px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.appointment-detail {
  margin-bottom: 20px;
}
</style>

