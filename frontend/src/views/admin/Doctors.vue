<template>
  <div class="doctors-management">
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索医生姓名"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="5">
          <el-select
            v-model="searchForm.department"
            placeholder="选择科室"
            clearable
            @change="handleSearch"
            style="width: 100%"
          >
            <el-option label="全部科室" value="" />
            <el-option 
              v-for="department in departments" 
              :key="department.id" 
              :label="department.name" 
              :value="department.id" 
            />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select
            v-model="searchForm.status"
            placeholder="选择状态"
            clearable
            @change="handleSearch"
            style="width: 100%"
          >
            <el-option label="全部状态" value="" />
            <el-option label="已激活" value="active" />
            <el-option label="已停用" value="disabled" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button @click="resetSearch" style="width: 100%">重置</el-button>
        </el-col>
        <el-col :span="5">
          <el-button type="primary" @click="openAddDialog" style="width: 100%">
            <el-icon><Plus /></el-icon>
            添加医生
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 医生列表 -->
    <el-card class="table-card">
      <el-table
        :data="filteredDoctors"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="医生ID" width="100" />
        
        <el-table-column label="医生信息" width="200">
          <template #default="{ row }">
            <div class="doctor-info">
              <el-avatar :size="40">
                {{ row.name.charAt(0) }}
              </el-avatar>
              <div class="doctor-details">
                <div class="doctor-name">{{ row.name }}</div>
                <div class="doctor-title">{{ row.title }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="科室" width="120">
          <template #default="{ row }">
            <span v-if="row.clinic">
              {{ row.clinic?.department?.name || '未知科室' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="门诊" width="130">
          <template #default="{ row }">
            <span v-if="row.clinic">{{ row.clinic.name }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="用户账号" width="120">
          <template #default="{ row }">
            <span v-if="row.user">{{ row.user.username }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="专长" width="200">
          <template #default="{ row }">
            <el-tag size="small" v-if="row.specialty">
              {{ row.specialty }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.user?.status)" v-if="row.user">
              {{ getStatusText(row.user.status) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="个人简介" width="200">
          <template #default="{ row }">
            <span v-if="row.bio">{{ row.bio }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <!-- 待审核状态的操作按钮 -->
            <template v-if="row.user?.status === 'pending_approval'">
              <el-button-group>
                <el-button
                  type="success"
                  size="small"
                  @click="approveDoctor(row)"
                >
                  <el-icon><Check /></el-icon>
                  通过
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="rejectDoctor(row)"
                >
                  <el-icon><Close /></el-icon>
                  拒绝
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="viewPendingDetail(row)"
                >
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
              </el-button-group>
            </template>
            
            <!-- 其他状态的操作按钮 -->
            <template v-else>
              <el-button-group>
                <el-button
                  type="primary"
                  size="small"
                  @click="viewDoctor(row)"
                >
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  @click="editDoctor(row)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>

                <el-button
                  type="danger"
                  size="small"
                  @click="handleDisableDoctor(row)"
                  v-if="row.user?.status === 'active'"
                >
                  <el-icon><CircleClose /></el-icon>
                  停用
                </el-button>
              </el-button-group>
            </template>
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

      <!-- 批量操作栏 -->
      <div class="batch-operations" v-if="selectedDoctors.length > 0">
        <span class="selected-info">已选择 {{ selectedDoctors.length }} 个医生</span>
        <div class="batch-buttons">
          <el-button
            type="warning"
            size="small"
            @click="handleBatchResetPassword"
            :disabled="selectedDoctors.length === 0"
          >
            <el-icon><Key /></el-icon>
            批量重置密码
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleBatchDelete"
            :disabled="selectedDoctors.length === 0"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑医生对话框 -->
    <el-dialog
      v-model="doctorDialog.visible"
      :title="doctorDialog.isEdit ? '编辑医生' : '添加医生'"
      width="600px"
      @close="resetDoctorForm"
    >
      <!-- 添加医生时的提示信息 -->
      <el-alert
        type="info"
        :closable="false"
        class="doctor-create-alert"
      >
        <template #default>
          <div class="alert-content">
            <div class="alert-item">
              <el-icon class="alert-icon"><Key /></el-icon>
              <span>系统将自动创建医生的登录账号，默认密码为：<strong>123456</strong></span>
            </div>
            <div class="alert-item">
              <el-icon class="alert-icon"><CircleCheck /></el-icon>
              <span>医生账号创建后状态为已激活，可以直接登录使用。</span>
            </div>
          </div>
        </template>
      </el-alert>

      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="doctorRules"
        label-width="100px"
      >
        <el-row :gutter="20" v-if="!doctorDialog.isEdit">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="doctorForm.username" placeholder="请输入登录用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="doctorForm.password" 
                placeholder="默认密码：123456" 
                type="password"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="doctorForm.name" placeholder="请输入医生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="门诊" prop="clinicId">
              <el-select v-model="doctorForm.clinicId" placeholder="请选择门诊" style="width: 100%">
                <el-option 
                  v-for="clinic in clinics" 
                  :key="clinic.id" 
                  :label="clinic.name" 
                  :value="clinic.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="doctorForm.title" placeholder="请选择职称" style="width: 100%">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="住院医师" value="住院医师" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专长" prop="specialty">
              <el-input v-model="doctorForm.specialty" placeholder="请输入专长领域" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="doctorForm.bio"
            type="textarea"
            :rows="3"
            placeholder="请输入医生个人简介"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="doctorDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveDoctorForm" :loading="doctorDialog.loading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 医生详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="医生详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedDoctor" class="doctor-detail">
        <!-- 医生基本信息卡片 -->
        <el-card class="doctor-header-card" shadow="never">
          <el-row :gutter="30">
            <el-col :span="6">
              <div class="doctor-avatar">
                <el-avatar :size="120" class="doctor-avatar-img">
                  {{ selectedDoctor.name ? selectedDoctor.name.charAt(0) : '医' }}
                </el-avatar>
              </div>
            </el-col>
            <el-col :span="18">
              <div class="doctor-basic-info">
                <h3>{{ selectedDoctor.name || '未设置姓名' }}</h3>
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">医生ID：</span>
                    <span class="info-value">{{ selectedDoctor.id }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">科室：</span>
                    <span class="info-value">{{ selectedDoctor.clinic?.department?.name || '未知科室' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">门诊：</span>
                    <span class="info-value">{{ selectedDoctor.clinic?.name || '未设置门诊' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">职称：</span>
                    <span class="info-value">{{ selectedDoctor.title || '未设置职称' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">用户账号：</span>
                    <span class="info-value">{{ selectedDoctor.user?.username || '未关联账号' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">账户状态：</span>
                    <span class="info-value">
                      <el-tag :type="getStatusType(selectedDoctor.user?.status)" v-if="selectedDoctor.user">
                        {{ getStatusText(selectedDoctor.user.status) }}
                      </el-tag>
                      <span v-else>未关联账号</span>
                    </span>
                  </div>
                  <div class="info-item" v-if="selectedDoctor.user?.createdAt">
                    <span class="info-label">创建时间：</span>
                    <span class="info-value">{{ formatDate(selectedDoctor.user.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 专长领域卡片 -->
        <el-card class="doctor-section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="el-icon-star-on"></i>
              <span>专长领域</span>
            </div>
          </template>
          <div class="doctor-specialty-content">
            <el-tag v-if="selectedDoctor.specialty" class="specialty-tag">
              {{ selectedDoctor.specialty }}
            </el-tag>
            <span v-else class="empty-text">未设置专长</span>
          </div>
        </el-card>

        <!-- 医生简介卡片 -->
        <el-card class="doctor-section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="el-icon-document"></i>
              <span>医生简介</span>
            </div>
          </template>
          <div class="doctor-bio-content">
            <p>{{ selectedDoctor.bio || '暂无简介' }}</p>
          </div>
        </el-card>

        <!-- 其他信息卡片 -->
        <el-card class="doctor-section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <i class="el-icon-info"></i>
              <span>其他信息</span>
            </div>
          </template>
          <div class="doctor-additional-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="additional-info-item">
                  <span class="info-label">用户ID：</span>
                  <span class="info-value">{{ selectedDoctor.userId || '未关联' }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="additional-info-item">
                  <span class="info-label">门诊ID：</span>
                  <span class="info-value">{{ selectedDoctor.clinicId || '未设置' }}</span>
                </div>
              </el-col>
            </el-row>
            <div class="additional-info-item" v-if="selectedDoctor.clinic?.description">
              <span class="info-label">门诊描述：</span>
              <span class="info-value">{{ selectedDoctor.clinic.description }}</span>
            </div>
            <div class="additional-info-item" v-if="selectedDoctor.clinic?.department?.description">
              <span class="info-label">科室描述：</span>
              <span class="info-value">{{ selectedDoctor.clinic.department.description }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown, View, Edit, Delete, Check, Close, Key, CircleClose, CircleCheck } from '@element-plus/icons-vue'
import { 
  getDoctorList, 
  getDoctorById, 
  createDoctor, 
  updateDoctor, 
  deleteDoctor,
  createDoctorAccount,
  resetDoctorPassword,
  disableDoctor
} from '@/api/doctor'
import { getNextUserId, createUserAndDoctor } from '@/api/auth'
import { getDepartmentList } from '@/api/department'
import { getClinicList } from '@/api/clinic'

const loading = ref(false)
const selectedDoctors = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  department: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 医生列表数据
const doctors = ref([])

// 科室和门诊数据
const departments = ref([])
const clinics = ref([])

// 过滤后的医生列表
const filteredDoctors = computed(() => {
  let result = doctors.value

  if (searchForm.keyword) {
    result = result.filter(doctor =>
      doctor.name.includes(searchForm.keyword)
    )
  }

  if (searchForm.department) {
    result = result.filter(doctor => 
      doctor.clinic && 
      doctor.clinic.departmentId == searchForm.department
    )
  }

  if (searchForm.status) {
    result = result.filter(doctor => 
      doctor.user && doctor.user.status === searchForm.status
    )
  }

  pagination.total = result.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return result.slice(start, end)
})



// 医生对话框
const doctorDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false
})

const doctorFormRef = ref()
const doctorForm = reactive({
  id: null,
  username: '',
  password: '123456',
  clinicId: null,
  name: '',
  title: '',
  specialty: '',
  bio: ''
})

const doctorRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  clinicId: [
    { required: true, message: '请选择所属门诊', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ],
  specialty: [
    { required: true, message: '请输入专长', trigger: 'blur' }
  ]
}

// 详情对话框
const detailDialog = reactive({
  visible: false
})
const selectedDoctor = ref(null)

const getStatusType = (status) => {
  const statusMap = {
    'active': 'success',
    'inactive': 'danger',
    'disabled': 'danger',
    'pending_approval': 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'active': '已激活',
    'inactive': '已禁用',
    'disabled': '已停用',
    'pending_approval': '待审核'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return '日期格式错误'
  }
}

// 加载医生数据
const loadDoctors = async () => {
  try {
    loading.value = true
    const response = await getDoctorList()
    doctors.value = response.data || []
  } catch (error) {
    console.error('加载医生数据失败:', error)
    ElMessage.error('加载医生数据失败')
  } finally {
    loading.value = false
  }
}

// 加载科室数据
const loadDepartments = async () => {
  try {
    const response = await getDepartmentList()
    departments.value = response.data || []
  } catch (error) {
    console.error('加载科室数据失败:', error)
  }
}

// 加载门诊数据
const loadClinics = async () => {
  try {
    const response = await getClinicList()
    clinics.value = response.data || []
  } catch (error) {
    console.error('加载门诊数据失败:', error)
  }
}

// 根据科室ID获取科室名称
const getDepartmentName = (departmentId) => {
  if (!departmentId || !departments.value.length) {
    return '未知科室'
  }
  // 确保数据类型匹配，可能后端返回的是字符串或数字
  const department = departments.value.find(d => d.id == departmentId)
  return department ? department.name : '未知科室'
}

const handleSearch = () => {
  pagination.currentPage = 1
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.department = ''
  searchForm.status = ''
  pagination.currentPage = 1
}

const handleSelectionChange = (selection) => {
  selectedDoctors.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
}

const openAddDialog = () => {
  doctorDialog.isEdit = false
  doctorDialog.visible = true
}

const viewDoctor = (doctor) => {
  selectedDoctor.value = doctor
  detailDialog.visible = true
}

const editDoctor = (doctor) => {
  doctorDialog.isEdit = true
  doctorDialog.visible = true
  
  // 填充表单数据
  Object.assign(doctorForm, {
    id: doctor.id,
    userId: doctor.userId,
    clinicId: doctor.clinicId,
    name: doctor.name,
    title: doctor.title,
    specialty: doctor.specialty || '',
    bio: doctor.bio || ''
  })
}

const resetDoctorForm = () => {
  doctorFormRef.value?.resetFields()
  Object.assign(doctorForm, {
    id: null,
    username: '',
    password: '123456',
    clinicId: null,
    name: '',
    title: '',
    specialty: '',
    bio: ''
  })
}

const saveDoctorForm = async () => {
  try {
    await doctorFormRef.value.validate()
    doctorDialog.loading = true

    if (doctorDialog.isEdit) {
      // 编辑模式
      await updateDoctor(doctorForm.id, {
        name: doctorForm.name,
        title: doctorForm.title,
        specialty: doctorForm.specialty,
        bio: doctorForm.bio
      })
      ElMessage.success('医生信息更新成功')
    } else {
      // 添加模式 - 使用新的创建医生账号API
      await createDoctorAccount({
        username: doctorForm.username,
        password: doctorForm.password,
        clinicId: doctorForm.clinicId,
        name: doctorForm.name,
        title: doctorForm.title,
        specialty: doctorForm.specialty,
        bio: doctorForm.bio
      })
      ElMessage.success('医生账号创建成功，账号已激活，可以直接登录')
    }

    doctorDialog.visible = false
    await loadDoctors() // 重新加载数据
  } catch (error) {
    console.error('保存失败:', error)
    
    // 根据错误类型提供更友好的提示
    let errorMessage = '保存失败'
    
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 403:
          errorMessage = '权限不足，请检查登录状态或联系管理员'
          break
        case 404:
          errorMessage = '接口不存在，请检查系统配置'
          break
        case 400:
          errorMessage = data?.msg || '请求参数错误，请检查输入信息'
          break
        case 500:
          errorMessage = '服务器内部错误，请稍后重试'
          break
        default:
          errorMessage = data?.msg || `请求失败 (${status})`
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    doctorDialog.loading = false
  }
}

const handleCommand = async (command) => {
  const [action, id] = command.split('-')
  const doctorId = parseInt(id)
  const doctor = doctors.value.find(d => d.id === doctorId)

  if (!doctor) return

  switch (action) {
    case 'schedule':
      // TODO: 跳转到排班管理页面
      ElMessage.info('跳转到排班管理页面')
      break

    case 'reset':
      try {
        await ElMessageBox.confirm(
          `确定要重置 ${doctor.name} 的密码吗？`,
          '确认操作',
          { type: 'warning' }
        )
        // TODO: 调用重置密码API
        ElMessage.success('密码重置成功')
      } catch {
        // 用户取消
      }
      break

    case 'toggle':
      try {
        const currentStatus = doctor.user?.status
        const newStatus = currentStatus === 1 ? 0 : 1
        const statusText = newStatus === 1 ? '启用' : '禁用'
        
        await ElMessageBox.confirm(
          `确定要将 ${doctor.name} 设为${statusText}状态吗？`,
          '确认操作',
          { type: 'warning' }
        )
        
        // TODO: 调用更新用户状态的API
        if (doctor.user) {
          doctor.user.status = newStatus
        }
        ElMessage.success(`已将 ${doctor.name} 设为${statusText}状态`)
      } catch {
        // 用户取消
      }
      break

    case 'delete':
      try {
        await ElMessageBox.confirm(
          `确定要删除医生 ${doctor.name} 吗？此操作不可恢复！`,
          '确认删除',
          { type: 'error' }
        )
        
        await deleteDoctor(doctorId)
        ElMessage.success('医生删除成功')
        await loadDoctors() // 重新加载数据
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }
      break
  }
}

// 删除医生
const handleDeleteDoctor = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除医生 ${doctor.name} 吗？此操作不可恢复！`,
      '确认删除',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await deleteDoctor(doctor.id)
    ElMessage.success('医生删除成功')
    await loadDoctors() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 批量删除医生
const handleBatchDelete = async () => {
  if (selectedDoctors.value.length === 0) {
    ElMessage.warning('请先选择要删除的医生')
    return
  }

  try {
    const doctorNames = selectedDoctors.value.map(doctor => doctor.name).join('、')
    await ElMessageBox.confirm(
      `确定要删除以下 ${selectedDoctors.value.length} 个医生吗？\n${doctorNames}\n\n此操作不可恢复！`,
      '确认批量删除',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    // 批量删除
    const deletePromises = selectedDoctors.value.map(doctor => deleteDoctor(doctor.id))
    await Promise.all(deletePromises)
    
    ElMessage.success(`成功删除 ${selectedDoctors.value.length} 个医生`)
    selectedDoctors.value = [] // 清空选择
    await loadDoctors() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 批量重置密码
const handleBatchResetPassword = async () => {
  if (selectedDoctors.value.length === 0) {
    ElMessage.warning('请先选择要重置密码的医生')
    return
  }

  try {
    const doctorNames = selectedDoctors.value.map(doctor => doctor.name).join('、')
    await ElMessageBox.confirm(
      `确定要将以下 ${selectedDoctors.value.length} 个医生的密码重置为 123456 吗？\n${doctorNames}`,
      '确认批量重置密码',
      { 
        type: 'warning',
        confirmButtonText: '确定重置',
        cancelButtonText: '取消'
      }
    )
    
    // 批量重置密码
    const resetPromises = selectedDoctors.value.map(doctor => 
      resetDoctorPassword(doctor.user.id, { newPassword: '123456' })
    )
    await Promise.all(resetPromises)
    
    ElMessage.success(`成功重置 ${selectedDoctors.value.length} 个医生的密码`)
    selectedDoctors.value = [] // 清空选择
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量重置密码失败:', error)
      ElMessage.error('批量重置密码失败: ' + (error.message || '未知错误'))
    }
  }
}

// 重置密码处理函数
const handleResetPassword = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要将医生 ${doctor.name} 的密码重置为 123456 吗？`,
      '重置密码',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await resetDoctorPassword(doctor.user.id, { newPassword: '123456' })
    ElMessage.success(`医生 ${doctor.name} 的密码已重置为 123456`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败: ' + (error.message || '未知错误'))
    }
  }
}

// 停用医生处理函数
const handleDisableDoctor = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要停用医生 ${doctor.name} 的账号吗？停用后该医生将无法登录系统。`,
      '停用医生账号',
      {
        confirmButtonText: '确定停用',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await disableDoctor(doctor.user.id)
    ElMessage.success(`医生 ${doctor.name} 的账号已停用`)
    await loadDoctors() // 重新加载医生列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用医生失败:', error)
      ElMessage.error('停用医生失败: ' + (error.message || '未知错误'))
    }
  }
}

// 审核相关方法
const viewPendingDetail = async (doctor) => {
  try {
    const response = await getPendingDoctorDetail(doctor.user.id)
    selectedDoctor.value = response.data
    detailDialog.visible = true
  } catch (error) {
    console.error('获取待审核医生详情失败:', error)
    ElMessage.error('获取医生详情失败')
  }
}

const approveDoctor = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过医生 ${doctor.name} 的注册申请吗？`,
      '确认审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await approveDoctorRegistration({
      userId: doctor.user.id,
      doctorId: doctor.id
    })
    
    ElMessage.success('审核通过成功')
    await loadDoctors() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败: ' + (error.message || '未知错误'))
    }
  }
}

const rejectDoctor = async (doctor) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入拒绝医生 ${doctor.name} 注册申请的原因：`,
      '拒绝审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '请输入拒绝原因'
          }
          return true
        }
      }
    )
    
    await rejectDoctorRegistration({
      userId: doctor.user.id,
      doctorId: doctor.id,
      reason: reason.trim()
    })
    
    ElMessage.success('审核拒绝成功')
    await loadDoctors() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核拒绝失败:', error)
      ElMessage.error('审核拒绝失败: ' + (error.message || '未知错误'))
    }
  }
}



onMounted(async () => {
  await Promise.all([
    loadDoctors(),
    loadDepartments(),
    loadClinics()
  ])
})
</script>

<style scoped>
.doctors-management {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.doctor-info {
  display: flex;
  align-items: center;
}

.doctor-details {
  margin-left: 12px;
}

.doctor-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.doctor-title {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 医生详情对话框样式 */
.doctor-detail {
  padding: 0;
  line-height: 1.6;
}

/* 医生头像样式 */
.doctor-avatar {
  text-align: center;
  margin-bottom: 20px;
}

.doctor-avatar-img {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 3px solid #f0f0f0;
}

/* 基本信息卡片样式 */
.doctor-header-card {
  margin-bottom: 18px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.doctor-basic-info h3 {
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 18px;
  color: #2c3e50;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
  display: inline-block;
  text-align: left;
}

/* 信息网格布局 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 24px;
  margin-top: 18px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  min-height: 30px;
  line-height: 1.5;
}

.info-label {
  display: inline-block;
  width: 90px;
  color: #666;
  font-weight: 500;
  flex-shrink: 0;
  text-align: left;
  padding-top: 2px;
}

.info-value {
  color: #333;
  font-weight: 400;
  flex: 1;
  text-align: left;
  padding-top: 2px;
}

/* 卡片样式 */
.doctor-section-card {
  margin-bottom: 18px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.doctor-section-card:last-child {
  margin-bottom: 0;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  text-align: left;
}

.card-header i {
  margin-right: 8px;
  color: #409eff;
  font-size: 16px;
}

/* 专长内容样式 */
.doctor-specialty-content {
  padding: 16px 0;
  text-align: left;
}

.specialty-tag {
  background: #409eff;
  color: white;
  border: none;
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 4px;
  font-weight: 500;
  display: inline-block;
}

/* 简介内容样式 */
.doctor-bio-content {
  padding: 16px 0;
  text-align: left;
}

.doctor-bio-content p {
  line-height: 1.6;
  color: #555;
  text-align: left;
  margin: 0;
  font-size: 14px;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

/* 其他信息样式 */
.doctor-additional-content {
  padding: 16px 0;
}

.additional-info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14px;
  min-height: 30px;
  line-height: 1.5;
}

.additional-info-item:last-child {
  margin-bottom: 0;
}

.additional-info-item .info-label {
  width: 80px;
  color: #666;
  font-weight: 500;
  flex-shrink: 0;
  text-align: left;
  padding-top: 2px;
}

/* 批量操作栏样式 */
.batch-operations {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-top: 16px;
}

.selected-info {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.batch-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 医生创建提示信息样式 */
.doctor-create-alert {
  margin-bottom: 20px;
  width: 100%;
  margin-left: 0;
  margin-right: 0;
}

.alert-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.5;
  text-align: left;
}

.alert-icon {
  color: #409eff;
  font-size: 16px;
  flex-shrink: 0;
}

.alert-item span {
  color: #606266;
  font-size: 14px;
}

.alert-item strong {
  color: #e6a23c;
  font-weight: 600;
  font-size: 14px;
  padding: 2px 6px;
  background-color: #fdf6ec;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.additional-info-item .info-value {
  color: #333;
  flex: 1;
  text-align: left;
  padding-top: 2px;
}

/* 空文本样式 */
.empty-text {
  color: #999;
  font-style: italic;
  text-align: left;
}

/* 状态标签样式 */
.el-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .doctor-basic-info h3 {
    font-size: 24px;
  }
}
</style>