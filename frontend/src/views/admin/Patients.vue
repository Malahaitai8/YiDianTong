<template>
  <div class="patients-management">
    <el-card class="search-card">
      <el-row :gutter="16" class="filter-row" type="flex">
        <el-col :span="5">
          <el-input
            v-model="searchForm.patientId"
            placeholder="输入患者ID精确查询"
            clearable
            @input="handleSearchById"
            @clear="handleSearchById"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchForm.keyword" placeholder="搜索患者姓名或手机号" clearable @input="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.idStatus" placeholder="身份状态" clearable @change="handleSearch" style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="已认证" value="已认证" />
            <el-option label="待认证" value="待认证" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.role" placeholder="角色" clearable @change="handleSearch" style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="学生" value="学生" />
            <el-option label="教师" value="教师" />
            <el-option label="外来人员" value="外来人员" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-button @click="resetSearch" style="width: 100%">重置</el-button>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="openAddDialog" style="width: 100%">
            <el-icon><Plus /></el-icon>
            添加患者
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="filteredPatients" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="患者ID" width="90" />
        <el-table-column label="患者信息" min-width="220">
          <template #default="{ row }">
            <div class="patient-info">
              <el-avatar :size="40">{{ (row.name || '患').charAt(0) }}</el-avatar>
              <div class="patient-details">
                <div class="patient-name">{{ row.name || '-' }}</div>
                <div class="patient-phone">{{ row.phoneNumber || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="账号" width="140">
          <template #default="{ row }">
            <span v-if="row.user">{{ row.user.username }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            {{ specificRoleText(row.specificRole) }}
          </template>
        </el-table-column>
        <el-table-column label="身份状态" width="120">
          <template #default="{ row }">
            {{ idStatusText(row.idStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="idCardNumber" label="身份证号" min-width="200" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewPatient(row)">查看</el-button>
            <el-button type="warning" size="small" @click="editPatient(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="filteredPatients.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无患者数据" />
      </div>
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.currentPage"
          @current-change="(p)=>{ pagination.currentPage=p }"
        />
      </div>
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑患者' : '添加患者'" width="600px" @close="resetForm">
      <el-form ref="patientFormRef" :model="patientForm" :rules="patientRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username" v-if="!formDialog.isEdit">
              <el-input v-model="patientForm.username" placeholder="用于登录的用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="!formDialog.isEdit">
              <el-input v-model="patientForm.password" type="password" show-password placeholder="初始登录密码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="patientForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phoneNumber">
              <el-input v-model="patientForm.phoneNumber" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="specificRole">
              <el-select v-model="patientForm.specificRole" placeholder="请选择角色" style="width: 100%">
                <el-option label="学生" value="student" />
                <el-option label="教师" value="teacher" />
                <el-option label="外来人员" value="outsider" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份状态" prop="idStatus">
              <el-select v-model="patientForm.idStatus" placeholder="请选择身份状态" style="width: 100%">
                <el-option label="待认证" value="待认证" />
                <el-option label="已认证" value="已认证" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCardNumber">
              <el-input v-model="patientForm.idCardNumber" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="formDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="formDialog.loading" @click="savePatient">{{ formDialog.isEdit ? '更新' : '添加' }}</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialog.visible" title="患者详情" width="600px">
      <div class="patient-detail">
        <div class="detail-grid">
          <div class="detail-item"><span class="detail-label">患者ID：</span><span class="detail-value">{{ selectedPatient.id }}</span></div>
          <div class="detail-item"><span class="detail-label">姓名：</span><span class="detail-value">{{ selectedPatient.name }}</span></div>
          <div class="detail-item"><span class="detail-label">角色：</span><span class="detail-value">{{ selectedPatient.specificRole }}</span></div>
          <div class="detail-item"><span class="detail-label">身份状态：</span><span class="detail-value">{{ idStatusText(selectedPatient.idStatus) }}</span></div>
          <div class="detail-item"><span class="detail-label">联系电话：</span><span class="detail-value">{{ selectedPatient.phoneNumber }}</span></div>
          <div class="detail-item"><span class="detail-label">身份证号：</span><span class="detail-value">{{ selectedPatient.idCardNumber }}</span></div>
          <div class="detail-item"><span class="detail-label">用户账号：</span><span class="detail-value">{{ selectedPatient.user?.username || '-' }}</span></div>
          <div class="detail-item" v-if="selectedPatient.user?.status"><span class="detail-label">账户状态：</span><span class="detail-value">{{ selectedPatient.user.status }}</span></div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialog.visible = false">关闭</el-button>
          <el-button type="warning" @click="editPatient(selectedPatient)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  </template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getPatientList, getPatientById, createPatient, updatePatient, deletePatient } from '@/api/patient'
import { registerPatient } from '@/api/auth'

const loading = ref(false)
const patients = ref([])
const selected = ref([])

const searchForm = reactive({
  keyword: '',
  idStatus: '',
  role: '',
  patientId: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const filteredPatients = computed(() => {
  let result = patients.value
  if (searchForm.keyword) {
    const k = searchForm.keyword.trim()
    result = result.filter(p =>
      (p.name && p.name.includes(k)) || (p.phoneNumber && p.phoneNumber.includes(k))
    )
  }
  if (searchForm.idStatus) {
    result = result.filter(p => idStatusText(p.idStatus) === searchForm.idStatus)
  }
  if (searchForm.role) {
    result = result.filter(p => specificRoleText(p.specificRole) === searchForm.role)
  }
  pagination.total = result.length
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return result.slice(start, end)
})

const formDialog = reactive({ visible: false, isEdit: false, loading: false })
const patientFormRef = ref()
const patientForm = reactive({
  id: null,
  username: '',
  password: '123456',
  name: '',
  specificRole: '',
  idStatus: '待认证',
  phoneNumber: '',
  idCardNumber: ''
})

const patientRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  specificRole: [{ required: true, message: '请选择角色', trigger: 'change' }],
  idStatus: [{ required: true, message: '请选择身份状态', trigger: 'change' }],
  phoneNumber: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  idCardNumber: [{ required: true, message: '请输入身份证号', trigger: 'blur' }]
}

const viewDialog = reactive({ visible: false })
const selectedPatient = ref({})

const handleSelectionChange = (rows) => { selected.value = rows }
const handleSearch = () => { pagination.currentPage = 1 }
const resetSearch = async () => {
  searchForm.keyword = ''
  searchForm.idStatus = ''
  searchForm.role = ''
  searchForm.patientId = ''
  pagination.currentPage = 1
  await loadPatients()
}

const loadPatients = async () => {
  try {
    loading.value = true
    const resp = await getPatientList()
    const list = Array.isArray(resp?.data) ? resp.data : []
    patients.value = list
  } catch (e) {
    ElMessage.error('获取患者列表失败')
    patients.value = []
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  patientForm.id = null
  patientForm.username = ''
  patientForm.password = '123456'
  patientForm.name = ''
  patientForm.specificRole = ''
  patientForm.idStatus = '待认证'
  patientForm.phoneNumber = ''
  patientForm.idCardNumber = ''
  formDialog.isEdit = false
  formDialog.visible = true
}

const editPatient = (row) => {
  patientForm.id = row.id
  patientForm.username = row.user?.username || ''
  patientForm.password = ''
  patientForm.name = row.name
  patientForm.specificRole = row.specificRole
  patientForm.idStatus = row.idStatus
  patientForm.phoneNumber = row.phoneNumber
  patientForm.idCardNumber = row.idCardNumber
  formDialog.isEdit = true
  formDialog.visible = true
}

const savePatient = async () => {
  if (!patientFormRef.value) return
  try {
    // 根据模式设置校验项：新增需要校验用户名/密码，编辑不校验
    const valid = await patientFormRef.value.validate()
    if (!valid) return
    formDialog.loading = true

    if (formDialog.isEdit && patientForm.id) {
      const payload = {
        name: patientForm.name,
        specificRole: patientForm.specificRole,
        idStatus: toIdStatusCode(patientForm.idStatus),
        phoneNumber: patientForm.phoneNumber,
        idCardNumber: patientForm.idCardNumber
      }
      await updatePatient(patientForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      // 1) 先注册患者用户（自动创建用户 + 患者）
      await registerPatient({
        username: patientForm.username,
        password: patientForm.password,
        phoneNumber: patientForm.phoneNumber,
        role: 'patient'
      })

      // 2) 查找新创建的患者ID（通过用户名匹配）
      const listResp = await getPatientList()
      const list = Array.isArray(listResp?.data) ? listResp.data : []
      const created = list.find(p => p.user?.username === patientForm.username)

      if (!created) {
        throw new Error('找不到刚创建的患者记录')
      }

      // 3) 更新患者详细信息
      const payload = {
        name: patientForm.name,
        specificRole: patientForm.specificRole,
        idStatus: toIdStatusCode(patientForm.idStatus),
        phoneNumber: patientForm.phoneNumber,
        idCardNumber: patientForm.idCardNumber
      }
      await updatePatient(created.id, payload)
      ElMessage.success('添加成功')
    }

    formDialog.visible = false
    await loadPatients()
  } catch (error) {
    let msg = '保存失败'
    if (error?.response?.data?.msg) msg = error.response.data.msg
    else if (error?.message) msg = error.message
    ElMessage.error(msg)
  } finally {
    formDialog.loading = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该患者吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deletePatient(row.id)
    ElMessage.success('删除成功')
    await loadPatients()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const viewPatient = async (row) => {
  try {
    const resp = await getPatientById(row.id)
    selectedPatient.value = resp?.data || row
    viewDialog.visible = true
  } catch (e) {
    selectedPatient.value = row
    viewDialog.visible = true
  }
}

const handleSearchById = async () => {
  const id = String(searchForm.patientId || '').trim()
  if (!id) {
    await loadPatients()
    return
  }
  if (!/^\d+$/.test(id)) {
    ElMessage.warning('患者ID需为数字')
    return
  }
  try {
    loading.value = true
    const resp = await getPatientById(id)
    if (resp?.data) {
      patients.value = [resp.data]
      pagination.currentPage = 1
      pagination.total = 1
      ElMessage.success(`已找到患者 ID ${id}`)
    } else {
      patients.value = []
      pagination.total = 0
      ElMessage.warning('未找到该患者')
    }
  } catch (error) {
    const msg = error?.response?.data?.msg || '查询失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (!patientFormRef.value) return
  patientFormRef.value.resetFields()
}

onMounted(loadPatients)

// 显示身份状态中文映射（仅前端显示）
const idStatusText = (v) => {
  const m = { verified: '已认证', pending: '待认证', 已认证: '已认证', 待认证: '待认证' }
  return m[v] || v || '-'
}

// 后端身份状态编码转换（提交前使用）
const toIdStatusCode = (v) => {
  const m = { '已认证': 'verified', '待认证': 'pending', verified: 'verified', pending: 'pending' }
  return m[v] || 'pending'
}

// 显示角色中文映射（仅前端显示）
const specificRoleText = (v) => {
  const m = { student: '学生', teacher: '教师', outsider: '外来人员', 学生: '学生', 教师: '教师', 外来人员: '外来人员' }
  return m[v] || v || '-'
}
</script>

<style scoped>
.patients-management {
  padding: 10px;
}
.filter-row {
  flex-wrap: nowrap;
  align-items: center;
}
.search-card { margin-bottom: 16px; }
.table-card { margin-top: 8px; }
.patient-info { display: flex; align-items: center; gap: 12px; }
.patient-details { display: flex; flex-direction: column; }
.patient-name { font-weight: 600; color: #2c3e50; }
.patient-phone { font-size: 12px; color: #8c99a6; }
.empty-state { margin-top: 20px; }
.pagination-container { display: flex; justify-content: flex-end; padding: 12px 0; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.detail-item { display: flex; gap: 8px; }
.detail-label { color: #7a8a99; }
.detail-value { color: #2c3e50; }
</style>