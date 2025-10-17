<template>
  <div class="doctors-management">
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索医生姓名或工号"
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
            v-model="searchForm.department"
            placeholder="选择科室"
            clearable
            @change="handleSearch"
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
            v-model="searchForm.status"
            placeholder="选择状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部状态" value="" />
            <el-option label="在职" value="active" />
            <el-option label="休假" value="leave" />
            <el-option label="离职" value="inactive" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加医生
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
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
        
        <el-table-column prop="employeeId" label="工号" width="100" />
        
        <el-table-column label="医生信息" width="200">
          <template #default="{ row }">
            <div class="doctor-info">
              <el-avatar :size="40" :src="row.avatar">
                {{ row.name.charAt(0) }}
              </el-avatar>
              <div class="doctor-details">
                <div class="doctor-name">{{ row.name }}</div>
                <div class="doctor-title">{{ row.title }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="department" label="科室" width="120" />
        
        <el-table-column prop="phone" label="联系电话" width="130" />
        
        <el-table-column prop="email" label="邮箱" width="180" />
        
        <el-table-column label="专长" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="specialty in row.specialties"
              :key="specialty"
              size="small"
              style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ specialty }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="joinDate" label="入职日期" width="120" />
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDoctor(row)"
            >
              查看
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="editDoctor(row)"
            >
              编辑
            </el-button>
            <el-dropdown @command="handleCommand">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="`schedule-${row.id}`">
                    排班管理
                  </el-dropdown-item>
                  <el-dropdown-item :command="`reset-password-${row.id}`">
                    重置密码
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="`toggle-status-${row.id}`"
                    :divided="true"
                  >
                    {{ row.status === 'active' ? '设为休假' : '设为在职' }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="`delete-${row.id}`"
                    style="color: #f56c6c;"
                  >
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 添加/编辑医生对话框 -->
    <el-dialog
      v-model="doctorDialog.visible"
      :title="doctorDialog.isEdit ? '编辑医生' : '添加医生'"
      width="600px"
      @close="resetDoctorForm"
    >
      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="doctorRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="employeeId">
              <el-input v-model="doctorForm.employeeId" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="doctorForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="科室" prop="department">
              <el-select v-model="doctorForm.department" placeholder="请选择科室">
                <el-option label="内科" value="内科" />
                <el-option label="外科" value="外科" />
                <el-option label="儿科" value="儿科" />
                <el-option label="妇科" value="妇科" />
                <el-option label="骨科" value="骨科" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="doctorForm.title" placeholder="请选择职称">
                <el-option label="主任医师" value="主任医师" />
                <el-option label="副主任医师" value="副主任医师" />
                <el-option label="主治医师" value="主治医师" />
                <el-option label="住院医师" value="住院医师" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="doctorForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="doctorForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="专长" prop="specialties">
          <el-select
            v-model="doctorForm.specialties"
            multiple
            placeholder="请选择专长"
            style="width: 100%"
          >
            <el-option label="心血管疾病" value="心血管疾病" />
            <el-option label="呼吸系统疾病" value="呼吸系统疾病" />
            <el-option label="消化系统疾病" value="消化系统疾病" />
            <el-option label="神经系统疾病" value="神经系统疾病" />
            <el-option label="内分泌疾病" value="内分泌疾病" />
            <el-option label="普通外科" value="普通外科" />
            <el-option label="骨科手术" value="骨科手术" />
            <el-option label="儿童保健" value="儿童保健" />
            <el-option label="妇科疾病" value="妇科疾病" />
          </el-select>
        </el-form-item>

        <el-form-item label="简介">
          <el-input
            v-model="doctorForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入医生简介"
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
      width="800px"
    >
      <div v-if="selectedDoctor" class="doctor-detail">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="doctor-avatar">
              <el-avatar :size="120" :src="selectedDoctor.avatar">
                {{ selectedDoctor.name.charAt(0) }}
              </el-avatar>
            </div>
          </el-col>
          <el-col :span="18">
            <div class="doctor-basic-info">
              <h3>{{ selectedDoctor.name }}</h3>
              <p><strong>工号：</strong>{{ selectedDoctor.employeeId }}</p>
              <p><strong>科室：</strong>{{ selectedDoctor.department }}</p>
              <p><strong>职称：</strong>{{ selectedDoctor.title }}</p>
              <p><strong>联系电话：</strong>{{ selectedDoctor.phone }}</p>
              <p><strong>邮箱：</strong>{{ selectedDoctor.email }}</p>
              <p><strong>入职日期：</strong>{{ selectedDoctor.joinDate }}</p>
              <p><strong>状态：</strong>
                <el-tag :type="getStatusType(selectedDoctor.status)">
                  {{ getStatusText(selectedDoctor.status) }}
                </el-tag>
              </p>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <div class="doctor-specialties">
          <h4>专长领域</h4>
          <el-tag
            v-for="specialty in selectedDoctor.specialties"
            :key="specialty"
            style="margin-right: 10px; margin-bottom: 10px;"
          >
            {{ specialty }}
          </el-tag>
        </div>

        <el-divider />

        <div class="doctor-description">
          <h4>医生简介</h4>
          <p>{{ selectedDoctor.description || '暂无简介' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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
const doctors = ref([
  {
    id: 1,
    employeeId: 'D001',
    name: '张医生',
    title: '主任医师',
    department: '内科',
    phone: '13800138001',
    email: 'zhang@hospital.com',
    specialties: ['心血管疾病', '高血压'],
    status: 'active',
    joinDate: '2020-01-15',
    avatar: '',
    description: '从事内科临床工作20年，擅长心血管疾病的诊断和治疗。'
  },
  {
    id: 2,
    employeeId: 'D002',
    name: '李医生',
    title: '副主任医师',
    department: '外科',
    phone: '13800138002',
    email: 'li@hospital.com',
    specialties: ['普通外科', '微创手术'],
    status: 'active',
    joinDate: '2019-03-20',
    avatar: '',
    description: '专注于普通外科和微创手术，具有丰富的临床经验。'
  },
  {
    id: 3,
    employeeId: 'D003',
    name: '王医生',
    title: '主治医师',
    department: '儿科',
    phone: '13800138003',
    email: 'wang@hospital.com',
    specialties: ['儿童保健', '小儿呼吸'],
    status: 'leave',
    joinDate: '2021-06-10',
    avatar: '',
    description: '专业从事儿科临床工作，对儿童常见病有丰富经验。'
  }
])

// 过滤后的医生列表
const filteredDoctors = computed(() => {
  let result = doctors.value

  if (searchForm.keyword) {
    result = result.filter(doctor =>
      doctor.name.includes(searchForm.keyword) ||
      doctor.employeeId.includes(searchForm.keyword)
    )
  }

  if (searchForm.department) {
    result = result.filter(doctor => doctor.department === searchForm.department)
  }

  if (searchForm.status) {
    result = result.filter(doctor => doctor.status === searchForm.status)
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
  employeeId: '',
  name: '',
  title: '',
  department: '',
  phone: '',
  email: '',
  specialties: [],
  description: ''
})

const doctorRules = {
  employeeId: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择科室', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
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
    'leave': 'warning',
    'inactive': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'active': '在职',
    'leave': '休假',
    'inactive': '离职'
  }
  return statusMap[status] || '未知'
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
    employeeId: doctor.employeeId,
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    phone: doctor.phone,
    email: doctor.email,
    specialties: [...doctor.specialties],
    description: doctor.description || ''
  })
}

const resetDoctorForm = () => {
  doctorFormRef.value?.resetFields()
  Object.assign(doctorForm, {
    id: null,
    employeeId: '',
    name: '',
    title: '',
    department: '',
    phone: '',
    email: '',
    specialties: [],
    description: ''
  })
}

const saveDoctorForm = async () => {
  try {
    await doctorFormRef.value.validate()
    doctorDialog.loading = true

    // TODO: 调用API保存数据
    await new Promise(resolve => setTimeout(resolve, 1000))

    if (doctorDialog.isEdit) {
      // 编辑模式
      const index = doctors.value.findIndex(d => d.id === doctorForm.id)
      if (index !== -1) {
        Object.assign(doctors.value[index], {
          ...doctorForm,
          specialties: [...doctorForm.specialties]
        })
      }
      ElMessage.success('医生信息更新成功')
    } else {
      // 添加模式
      const newDoctor = {
        ...doctorForm,
        id: Date.now(),
        status: 'active',
        joinDate: new Date().toISOString().split('T')[0],
        avatar: '',
        specialties: [...doctorForm.specialties]
      }
      doctors.value.unshift(newDoctor)
      ElMessage.success('医生添加成功')
    }

    doctorDialog.visible = false
  } catch (error) {
    console.error('保存失败:', error)
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
        const newStatus = doctor.status === 'active' ? 'leave' : 'active'
        const statusText = newStatus === 'active' ? '在职' : '休假'
        
        await ElMessageBox.confirm(
          `确定要将 ${doctor.name} 设为${statusText}状态吗？`,
          '确认操作',
          { type: 'warning' }
        )
        
        doctor.status = newStatus
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
        
        const index = doctors.value.findIndex(d => d.id === doctorId)
        if (index !== -1) {
          doctors.value.splice(index, 1)
          ElMessage.success('医生删除成功')
        }
      } catch {
        // 用户取消
      }
      break
  }
}

onMounted(() => {
  // TODO: 加载医生数据
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

.doctor-detail {
  padding: 20px 0;
}

.doctor-avatar {
  text-align: center;
}

.doctor-basic-info h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.doctor-basic-info p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.5;
}

.doctor-specialties h4,
.doctor-description h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}

.doctor-description p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}
</style>