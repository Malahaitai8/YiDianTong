<template>
  <div class="schedule-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>排班管理</h2>
      <p>管理医生的排班信息，支持按科室和门诊分类查看</p>
    </div>

    <!-- 筛选和操作区域 -->
    <el-card class="filter-card">
      <!-- 视图切换 -->
      <div class="view-toggle">
        <el-radio-group v-model="viewMode" @change="handleViewModeChange">
          <el-radio-button label="card">卡片视图</el-radio-button>
          <el-radio-button label="table">表格视图</el-radio-button>
          <el-radio-button label="calendar">日历视图</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 筛选条件 -->
      <el-form :model="searchForm" :inline="true" class="filter-form">
        <el-form-item label="科室">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="请选择科室"
            clearable
            @change="handleDepartmentChange"
            style="width: 180px"
          >
            <el-option
              v-for="dept in departmentList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="门诊">
          <el-select
            v-model="searchForm.clinicId"
            placeholder="请选择门诊"
            clearable
            @change="handleClinicChange"
            style="width: 180px"
          >
            <el-option
              v-for="clinic in filteredClinicList"
              :key="clinic.id"
              :label="clinic.name"
              :value="clinic.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="医生">
          <el-select
            v-model="searchForm.doctorId"
            placeholder="请选择医生"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="doctor in filteredDoctorList"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        
        <el-form-item label="号别">
          <el-select
            v-model="searchForm.slotType"
            placeholder="请选择号别"
            clearable
            style="width: 120px"
          >
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
            <el-option label="特需号" value="vip" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="action-buttons">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增排班
        </el-button>
        <el-button type="success" @click="showBatchCreateDialog">
          <el-icon><DocumentAdd /></el-icon>
          批量创建
        </el-button>
        <el-button 
          type="danger" 
          @click="handleBatchDelete"
          :disabled="selectedRows.length === 0"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedRows.length }})
        </el-button>
      </div>
    </el-card>

    <!-- 卡片视图 -->
    <div v-if="viewMode === 'card'" class="card-view">
      <!-- 按科室分组显示 -->
      <div v-for="department in groupedSchedules" :key="department.id" class="department-section">
        <div class="department-header">
          <h3>{{ department.name }}</h3>
          <el-tag>{{ department.schedules.length }} 个排班</el-tag>
        </div>
        
        <!-- 按门诊分组 -->
        <div v-for="clinic in department.clinics" :key="clinic.id" class="clinic-section">
          <div class="clinic-header">
            <h4>{{ clinic.name }}</h4>
            <el-tag type="info" size="small">{{ clinic.schedules.length }} 个排班</el-tag>
          </div>
          
          <!-- 排班卡片 -->
          <div class="schedule-cards">
            <div 
              v-for="schedule in clinic.schedules" 
              :key="schedule.id" 
              class="schedule-card"
              :class="{ 'selected': selectedScheduleIds.includes(schedule.id) }"
              @click="toggleScheduleSelection(schedule)"
            >
              <div class="card-header">
                <div class="doctor-info">
                  <el-avatar :size="40" class="doctor-avatar">
                    {{ schedule.doctorName?.charAt(0) || 'D' }}
                  </el-avatar>
                  <div class="doctor-details">
                    <div class="doctor-name">{{ schedule.doctorName || '未知医生' }}</div>
                    <div class="schedule-date">{{ formatDate(schedule.scheduleDate) }}</div>
                  </div>
                </div>
                <div class="card-actions">
                  <el-dropdown @command="handleCardAction">
                    <el-button type="text" size="small">
                      <el-icon><MoreFilled /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item :command="{action: 'edit', data: schedule}">编辑</el-dropdown-item>
                        <el-dropdown-item :command="{action: 'delete', data: schedule}">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
              
              <div class="card-content">
                <div class="time-slot">
                  <el-tag :type="getTimeSlotType(schedule.timeSlot)" size="small">
                    {{ getTimeSlotText(schedule.timeSlot) }}
                  </el-tag>
                  <el-tag :type="getSlotTypeColor(schedule.slotType)" size="small">
                    {{ getSlotTypeText(schedule.slotType) }}
                  </el-tag>
                </div>
                
                <div class="slots-info">
                  <div class="slots-item">
                    <span class="label">总号源:</span>
                    <span class="value">{{ schedule.totalSlots || 0 }}</span>
                  </div>
                  <div class="slots-item">
                    <span class="label">剩余:</span>
                    <span class="value" :class="{ 'low-slots': schedule.availableSlots < 5 }">
                      {{ schedule.availableSlots || 0 }}
                    </span>
                  </div>
                  <div class="slots-item">
                    <span class="label">已约:</span>
                    <span class="value">{{ (schedule.totalSlots || 0) - (schedule.availableSlots || 0) }}</span>
                  </div>
                </div>
                
                <div class="progress-bar">
                  <el-progress 
                    :percentage="getBookingPercentage(schedule)" 
                    :color="getProgressColor(schedule)"
                    :show-text="false"
                    :stroke-width="6"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <el-empty v-if="groupedSchedules.length === 0" description="暂无排班数据" />
    </div>

    <!-- 日历视图 -->
    <div v-else-if="viewMode === 'calendar'" class="calendar-view">
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <div class="date-number">{{ data.day.split('-').pop() }}</div>
            <div class="schedules-in-date">
              <div 
                v-for="schedule in getSchedulesForDate(data.day)" 
                :key="schedule.id"
                class="mini-schedule"
                :title="`${schedule.doctorName} - ${getTimeSlotText(schedule.timeSlot)}`"
              >
                <el-tag size="small" :type="getTimeSlotType(schedule.timeSlot)">
                  {{ schedule.doctorName?.substring(0, 2) || 'Dr' }}
                </el-tag>
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </div>

    <!-- 表格视图 -->
    <el-card v-else class="table-card">
      <el-table
        v-loading="loading"
        :data="scheduleList"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="排班ID" width="80" />
        <el-table-column prop="doctorName" label="医生姓名" width="120" />
        <el-table-column prop="clinicName" label="门诊" width="120" />
        <el-table-column prop="scheduleDate" label="排班日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="100">
          <template #default="{ row }">
            <el-tag :type="getTimeSlotType(row.timeSlot)">
              {{ getTimeSlotText(row.timeSlot) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slotType" label="号别" width="100">
          <template #default="{ row }">
            <el-tag :type="getSlotTypeColor(row.slotType)">
              {{ getSlotTypeText(row.slotType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalSlots" label="总号源" width="80" />
        <el-table-column prop="availableSlots" label="剩余号源" width="80">
          <template #default="{ row }">
            <span :class="{ 'low-slots': row.availableSlots < 5 }">
              {{ row.availableSlots }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="dayOfWeek" label="星期" width="80" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑排班对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑排班' : '新增排班'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="医生" prop="doctorId">
          <el-select
            v-model="formData.doctorId"
            placeholder="请选择医生"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="doctor in doctorList"
              :key="doctor.id"
              :label="`${doctor.name} - ${doctor.clinicName}`"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="formData.scheduleDate"
            type="date"
            placeholder="请选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="时间段" prop="timeSlot">
          <el-select v-model="formData.timeSlot" placeholder="请选择时间段" style="width: 100%">
            <el-option label="上午" value="morning" />
            <el-option label="下午" value="afternoon" />
            <el-option label="晚上" value="evening" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="号别" prop="slotType">
          <el-select v-model="formData.slotType" placeholder="请选择号别" style="width: 100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
            <el-option label="特需号" value="vip" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="总号源数" prop="totalSlots">
          <el-input-number
            v-model="formData.totalSlots"
            :min="1"
            :max="100"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="可用号源" prop="availableSlots" v-if="isEdit">
          <el-input-number
            v-model="formData.availableSlots"
            :min="0"
            :max="formData.totalSlots"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量创建对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量创建排班"
      width="700px"
      @close="handleBatchDialogClose"
    >
      <el-form
        ref="batchFormRef"
        :model="batchFormData"
        :rules="batchFormRules"
        label-width="120px"
      >
        <el-form-item label="医生" prop="doctorId">
          <el-select
            v-model="batchFormData.doctorId"
            placeholder="请选择医生"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="doctor in doctorList"
              :key="doctor.id"
              :label="`${doctor.name} - ${doctor.clinicName}`"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="时间段" prop="timeSlots">
          <el-checkbox-group v-model="batchFormData.timeSlots">
            <el-checkbox label="morning">上午</el-checkbox>
            <el-checkbox label="afternoon">下午</el-checkbox>
            <el-checkbox label="evening">晚上</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="号别" prop="slotType">
          <el-select v-model="batchFormData.slotType" placeholder="请选择号别" style="width: 100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
            <el-option label="特需号" value="vip" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="总号源数" prop="totalSlots">
          <el-input-number
            v-model="batchFormData.totalSlots"
            :min="1"
            :max="100"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="跳过周末">
          <el-switch v-model="batchFormData.skipWeekends" />
        </el-form-item>
        
        <el-form-item label="排除日期">
          <el-date-picker
            v-model="batchFormData.excludeDates"
            type="dates"
            placeholder="选择要排除的日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="batchSubmitLoading">
          批量创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, onUnmounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, DocumentAdd, Delete, MoreFilled } from '@element-plus/icons-vue'
import { 
  getScheduleList, 
  createSchedule, 
  updateSchedule, 
  deleteSchedule, 
  batchCreateSchedule,
  batchDeleteSchedule 
} from '@/api/schedule'
import { getDoctorList } from '@/api/doctor'
import { getDepartmentList } from '@/api/department'
import { getClinicList } from '@/api/clinic'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const batchSubmitLoading = ref(false)
const scheduleList = ref([])
const doctorList = ref([])
const departmentList = ref([])
const clinicList = ref([])
const selectedRows = ref([])
const selectedScheduleIds = ref([])
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const batchFormRef = ref(null)

// 视图模式
const viewMode = ref('card') // card, table, calendar
const calendarDate = ref(new Date())

// 搜索表单
const searchForm = reactive({
  doctorId: '',
  departmentId: '',
  clinicId: '',
  slotType: ''
})

const dateRange = ref([])
const batchDateRange = ref([])

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  doctorId: '',
  scheduleDate: '',
  timeSlot: '',
  slotType: '',
  totalSlots: 20,
  availableSlots: 20
})

const batchFormData = reactive({
  doctorId: '',
  timeSlots: [],
  slotType: '',
  totalSlots: 20,
  skipWeekends: true,
  excludeDates: []
})

// 表单验证规则
const formRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  slotType: [{ required: true, message: '请选择号别', trigger: 'change' }],
  totalSlots: [{ required: true, message: '请输入总号源数', trigger: 'blur' }]
}

const batchFormRules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  timeSlots: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  slotType: [{ required: true, message: '请选择号别', trigger: 'change' }],
  totalSlots: [{ required: true, message: '请输入总号源数', trigger: 'blur' }]
}

// 计算属性
const searchParams = computed(() => {
  const params = {
    page: pagination.page,
    size: pagination.size,
    ...searchForm
  }
  
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  
  return params
})

// 监听批量日期范围变化
watch(batchDateRange, (newVal) => {
  if (newVal && newVal.length === 2) {
    batchFormData.startDate = newVal[0]
    batchFormData.endDate = newVal[1]
  }
})

// 方法
const loadScheduleList = async () => {
  try {
    loading.value = true
    const response = await getScheduleList(searchParams.value)
    
    // 确保数据是数组格式
    let data = []
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        data = response.data
      } else if (response.data.records && Array.isArray(response.data.records)) {
        data = response.data.records
      } else if (response.data.list && Array.isArray(response.data.list)) {
        data = response.data.list
      }
    }
    
    scheduleList.value = data
    pagination.total = response?.data?.total || data.length
    
    // 清空选中状态
    selectedRows.value = []
  } catch (error) {
    ElMessage.error('获取排班列表失败')
    console.error('获取排班列表失败:', error)
    scheduleList.value = []
    selectedRows.value = []
  } finally {
    loading.value = false
  }
}

const loadDoctorList = async () => {
  try {
    const response = await getDoctorList()
    doctorList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取医生列表失败')
    console.error('获取医生列表失败:', error)
  }
}

const loadDepartmentList = async () => {
  try {
    const response = await getDepartmentList()
    departmentList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取科室列表失败')
    console.error('获取科室列表失败:', error)
  }
}

const loadClinicList = async () => {
  try {
    const response = await getClinicList()
    clinicList.value = response.data || []
  } catch (error) {
    ElMessage.error('获取门诊列表失败')
    console.error('获取门诊列表失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadScheduleList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    doctorId: '',
    departmentId: '',
    clinicId: '',
    slotType: ''
  })
  dateRange.value = []
  pagination.page = 1
  loadScheduleList()
}

const handleSelectionChange = (selection) => {
  // 确保selection是数组
  if (Array.isArray(selection)) {
    selectedRows.value = selection
  } else {
    selectedRows.value = []
    console.warn('Selection is not an array:', selection)
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetFormData()
}

const showBatchCreateDialog = () => {
  batchDialogVisible.value = true
  resetBatchFormData()
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  Object.assign(formData, { ...row })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSchedule(row.id)
    ElMessage.success('删除成功')
    loadScheduleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除排班失败:', error)
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的排班')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个排班吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedRows.value.map(row => row.id)
    await batchDeleteSchedule(ids)
    ElMessage.success('批量删除成功')
    loadScheduleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error('批量删除排班失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await updateSchedule(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await createSchedule(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadScheduleList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      console.error('提交排班失败:', error)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleBatchSubmit = async () => {
  try {
    await batchFormRef.value.validate()
    
    if (!batchDateRange.value || batchDateRange.value.length !== 2) {
      ElMessage.error('请选择日期范围')
      return
    }
    
    batchSubmitLoading.value = true
    
    const data = {
      ...batchFormData,
      startDate: batchDateRange.value[0],
      endDate: batchDateRange.value[1]
    }
    
    const response = await batchCreateSchedule(data)
    ElMessage.success(`批量创建完成：成功 ${response.data.successCount} 个，跳过 ${response.data.skipCount} 个`)
    
    batchDialogVisible.value = false
    loadScheduleList()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('批量创建失败')
      console.error('批量创建排班失败:', error)
    }
  } finally {
    batchSubmitLoading.value = false
  }
}

const handleDialogClose = () => {
  resetFormData()
  formRef.value?.clearValidate()
}

const handleBatchDialogClose = () => {
  resetBatchFormData()
  batchFormRef.value?.clearValidate()
}

const resetFormData = () => {
  Object.assign(formData, {
    id: null,
    doctorId: '',
    scheduleDate: '',
    timeSlot: '',
    slotType: '',
    totalSlots: 20,
    availableSlots: 20
  })
}

const resetBatchFormData = () => {
  Object.assign(batchFormData, {
    doctorId: '',
    timeSlots: [],
    slotType: '',
    totalSlots: 20,
    skipWeekends: true,
    excludeDates: []
  })
  batchDateRange.value = []
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadScheduleList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadScheduleList()
}

// 辅助方法
const getTimeSlotType = (timeSlot) => {
  const typeMap = {
    morning: 'success',
    afternoon: 'warning',
    evening: 'info'
  }
  return typeMap[timeSlot] || ''
}

const getTimeSlotText = (timeSlot) => {
  const textMap = {
    morning: '上午',
    afternoon: '下午',
    evening: '晚上'
  }
  return textMap[timeSlot] || timeSlot
}

const getSlotTypeColor = (slotType) => {
  const colorMap = {
    normal: '',
    expert: 'warning',
    vip: 'danger'
  }
  return colorMap[slotType] || ''
}

const getSlotTypeText = (slotType) => {
  const textMap = {
    normal: '普通号',
    expert: '专家号',
    vip: '特需号'
  }
  return textMap[slotType] || slotType
}

// 新增的视图相关方法
const handleViewModeChange = (mode) => {
  viewMode.value = mode
}

const handleDepartmentChange = (departmentId) => {
  searchForm.clinicId = ''
  searchForm.doctorId = ''
  handleSearch()
}

const handleClinicChange = (clinicId) => {
  searchForm.doctorId = ''
  handleSearch()
}

const toggleScheduleSelection = (schedule) => {
  const index = selectedScheduleIds.value.indexOf(schedule.id)
  if (index > -1) {
    selectedScheduleIds.value.splice(index, 1)
  } else {
    selectedScheduleIds.value.push(schedule.id)
  }
}

const handleCardAction = ({ action, data }) => {
  if (action === 'edit') {
    handleEdit(data)
  } else if (action === 'delete') {
    handleDelete(data)
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const getBookingPercentage = (schedule) => {
  if (!schedule.totalSlots || schedule.totalSlots === 0) return 0
  const booked = schedule.totalSlots - (schedule.availableSlots || 0)
  return Math.round((booked / schedule.totalSlots) * 100)
}

const getProgressColor = (schedule) => {
  const percentage = getBookingPercentage(schedule)
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

const getSchedulesForDate = (date) => {
  return scheduleList.value.filter(schedule => schedule.scheduleDate === date)
}

// 计算属性
const filteredClinicList = computed(() => {
  if (!searchForm.departmentId) return clinicList.value
  return clinicList.value.filter(clinic => clinic.departmentId === searchForm.departmentId)
})

const filteredDoctorList = computed(() => {
  let filtered = doctorList.value
  if (searchForm.departmentId) {
    filtered = filtered.filter(doctor => doctor.departmentId === searchForm.departmentId)
  }
  if (searchForm.clinicId) {
    filtered = filtered.filter(doctor => doctor.clinicId === searchForm.clinicId)
  }
  return filtered
})

const groupedSchedules = computed(() => {
  const groups = {}
  
  scheduleList.value.forEach(schedule => {
    const deptId = schedule.departmentId || 'unknown'
    const clinicId = schedule.clinicId || 'unknown'
    
    if (!groups[deptId]) {
      groups[deptId] = {
        id: deptId,
        name: schedule.departmentName || '未知科室',
        schedules: [],
        clinics: {}
      }
    }
    
    if (!groups[deptId].clinics[clinicId]) {
      groups[deptId].clinics[clinicId] = {
        id: clinicId,
        name: schedule.clinicName || '未知门诊',
        schedules: []
      }
    }
    
    groups[deptId].schedules.push(schedule)
    groups[deptId].clinics[clinicId].schedules.push(schedule)
  })
  
  return Object.values(groups).map(dept => ({
    ...dept,
    clinics: Object.values(dept.clinics)
  }))
})

// 组件挂载时加载数据
onMounted(async () => {
  try {
    // 并行加载基础数据
    await Promise.all([
      loadDepartmentList(),
      loadClinicList(),
      loadDoctorList()
    ])
    // 加载排班列表
    await loadScheduleList()
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
})

// 组件卸载前清理
onBeforeUnmount(() => {
  // 清理数据状态
  scheduleList.value = []
  selectedRows.value = []
  doctorList.value = []
  loading.value = false
  
  // 重置表单
  Object.assign(searchForm, {
    doctorId: '',
    departmentId: '',
    clinicId: '',
    slotType: ''
  })
  dateRange.value = []
  
  // 重置分页
  Object.assign(pagination, {
    page: 1,
    size: 20,
    total: 0
  })
})
</script>

<style scoped>
.schedule-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.view-toggle {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.filter-form {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.table-card {
  background: white;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.low-slots {
  color: #f56c6c;
  font-weight: bold;
}

/* 卡片视图样式 */
.card-view {
  margin-top: 20px;
}

.department-section {
  margin-bottom: 32px;
}

.department-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.department-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.clinic-section {
  margin-bottom: 24px;
  margin-left: 20px;
}

.clinic-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.clinic-header h4 {
  margin: 0;
  color: #606266;
  font-size: 16px;
  font-weight: 500;
}

.schedule-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-left: 16px;
}

.schedule-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 2px solid transparent;
}

.schedule-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.schedule-card.selected {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doctor-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.doctor-details {
  flex: 1;
}

.doctor-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.schedule-date {
  font-size: 14px;
  color: #909399;
}

.card-actions {
  opacity: 0.7;
  transition: opacity 0.3s ease;
}

.schedule-card:hover .card-actions {
  opacity: 1;
}

.card-content {
  space-y: 12px;
}

.time-slot {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.slots-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.slots-item {
  text-align: center;
}

.slots-item .label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.slots-item .value {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.progress-bar {
  margin-top: 8px;
}

/* 日历视图样式 */
.calendar-view {
  margin-top: 20px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.calendar-cell {
  height: 100%;
  padding: 4px;
}

.date-number {
  font-weight: 600;
  margin-bottom: 4px;
}

.schedules-in-date {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.mini-schedule {
  font-size: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .schedule-cards {
    grid-template-columns: 1fr;
  }
  
  .filter-form {
    flex-direction: column;
  }
  
  .filter-form .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
  
  .department-header,
  .clinic-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 20px 20px 0;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-calendar-table .el-calendar-day) {
  height: 80px;
  padding: 4px;
}

:deep(.el-calendar__header) {
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
}
</style>