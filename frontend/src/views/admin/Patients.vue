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

    <!-- 批量操作卡片 -->
    <el-card v-if="selected.length > 0" class="batch-card">
      <div class="batch-operations">
        <span class="selected-info">已选择 {{ selected.length }} 个患者</span>
        <div class="batch-buttons">
          <el-button
            type="danger"
            size="small"
            @click="handleBatchDelete"
            :disabled="selected.length === 0"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>患者列表</span>
        </div>
      </template>
      <el-table :data="filteredPatients" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="患者ID" width="90" />
        <el-table-column label="患者信息" min-width="220">
          <template #default="{ row }">
            <div class="patient-info">
              <el-avatar :size="40">{{ (row.name || '患').charAt(0) }}</el-avatar>
              <div class="patient-details">
                <div class="patient-name">
                  {{ row.name || '-' }}
                  <el-tag v-if="!row.name" type="warning" size="small" style="margin-left: 8px">未填写</el-tag>
                </div>
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
        <el-table-column label="学号/工号" width="140">
          <template #default="{ row }">
            <span v-if="row.identityNumber">{{ row.identityNumber }}</span>
            <el-tag v-else type="warning" size="small">未填写</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="身份证号" min-width="180">
          <template #default="{ row }">
            <span v-if="row.idCardNumber">{{ row.idCardNumber }}</span>
            <el-tag v-else type="warning" size="small">未填写</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewPatient(row)">查看</el-button>
            <el-button type="warning" size="small" @click="editPatient(row)">编辑</el-button>
            <el-tooltip
              v-if="row.idStatus === 'pending' || row.idStatus === '待认证'"
              :content="hasRequiredFields(row) ? '审核患者认证' : '患者信息不完整，无法审核'"
              placement="top"
            >
              <el-button
                type="success"
                size="small"
                @click="handleApprovePatient(row)"
              >
                <el-icon><Check /></el-icon>
                审核
              </el-button>
            </el-tooltip>
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

    <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑患者' : '添加患者'" width="600px" class="add-patient-dialog" @close="resetForm">
      <div class="patient-form-wrap">
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
      </div>
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
          <div class="detail-item"><span class="detail-label">角色：</span><span class="detail-value">{{ specificRoleText(selectedPatient.specificRole) }}</span></div>
          <div class="detail-item"><span class="detail-label">身份状态：</span><span class="detail-value">{{ idStatusText(selectedPatient.idStatus) }}</span></div>
          <div class="detail-item"><span class="detail-label">联系电话：</span><span class="detail-value">{{ selectedPatient.phoneNumber }}</span></div>
          <div class="detail-item"><span class="detail-label">身份证号：</span><span class="detail-value">{{ selectedPatient.idCardNumber }}</span></div>
          <div class="detail-item"><span class="detail-label">用户账号：</span><span class="detail-value">{{ selectedPatient.user?.username || '-' }}</span></div>
          <div class="detail-item" v-if="selectedPatient.user?.status"><span class="detail-label">账户状态：</span><span class="detail-value">{{ userStatusText(selectedPatient.user.status) }}</span></div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialog.visible = false">关闭</el-button>
          <el-button type="warning" @click="editPatient(selectedPatient)">编辑</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核补充信息对话框 -->
    <el-dialog v-model="approveDialog.visible" title="补充患者认证信息" width="500px" @close="resetApproveForm">
      <el-alert
        title="提示"
        type="warning"
        description="该患者缺少必填的认证信息，请补充完整后再进行审核。"
        :closable="false"
        center
        style="margin-bottom: 20px;"
      />
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="100px">
        <el-form-item label="真实姓名" prop="name">
          <el-input v-model="approveForm.name" placeholder="请输入患者真实姓名" />
          <div class="form-hint">只能包含汉字或英文字母</div>
        </el-form-item>
        <el-form-item label="学号/工号" prop="identityNumber">
          <el-input v-model="approveForm.identityNumber" placeholder="请输入学号或工号" />
          <div class="form-hint">系统将根据白名单自动识别身份类型</div>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNumber">
          <el-input v-model="approveForm.idCardNumber" placeholder="请输入18位身份证号" maxlength="18" />
          <div class="form-hint">请输入有效的18位身份证号码</div>
        </el-form-item>
        <el-form-item label="角色" v-if="approveForm.specificRole">
          <el-tag :type="approveForm.specificRole === 'student' ? 'success' : approveForm.specificRole === 'teacher' ? 'warning' : 'info'" size="large">
            {{ specificRoleText(approveForm.specificRole) }}
          </el-tag>
          <div class="form-hint">系统已根据白名单自动设置角色</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="approveDialog.loading" @click="submitApprove">
            保存并审核
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Check } from '@element-plus/icons-vue'
import { getPatientList, getPatientById, createPatient, updatePatient, deletePatient, approvePatient } from '@/api/patient'
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

const approveDialog = reactive({ visible: false, loading: false })
const approveForm = reactive({
  patientId: null,
  name: '',
  identityNumber: '',
  idCardNumber: '',
  specificRole: ''
})

const approveRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度应在2-20个字符之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/, message: '真实姓名只能包含汉字或英文字母', trigger: 'blur' }
  ],
  identityNumber: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' }
  ],
  idCardNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入有效的身份证号', trigger: 'blur' }
  ]
}

const approveFormRef = ref()

const handleSelectionChange = (rows) => { selected.value = rows }

// 检查患者是否填写了所有必填字段
const hasRequiredFields = (patient) => {
  return patient.name && patient.name.trim() !== '' &&
         patient.identityNumber && patient.identityNumber.trim() !== '' &&
         patient.idCardNumber && patient.idCardNumber.trim() !== ''
}

// 批量删除患者
const handleBatchDelete = async () => {
  if (selected.value.length === 0) {
    ElMessage.warning('请先选择要删除的患者')
    return
  }

  try {
    const patientNames = selected.value.map(patient => patient.name || `ID:${patient.id}`).join('、')
    await ElMessageBox.confirm(
      `确定要删除以下 ${selected.value.length} 个患者吗？\n${patientNames}\n\n此操作不可恢复！`,
      '确认批量删除',
      { 
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    // 批量删除
    const deletePromises = selected.value.map(patient => deletePatient(patient.id))
    await Promise.all(deletePromises)
    
    ElMessage.success(`成功删除 ${selected.value.length} 个患者`)
    selected.value = []
    await loadPatients() // 重新加载数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error.message || '未知错误'))
    }
  }
}
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
  patientForm.idStatus = idStatusText(row.idStatus)
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
    if (error?.response?.data?.msg) {
      msg = error.response.data.msg
    } else if (error?.response?.data?.message) {
      msg = error.response.data.message
    } else if (error?.message) {
      msg = error.message
    }
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

// 审核患者
const handleApprovePatient = async (patient) => {
  // 前端验证必填字段
  const missingFields = []
  if (!patient.name || patient.name.trim() === '') {
    missingFields.push('姓名')
  }
  if (!patient.identityNumber || patient.identityNumber.trim() === '') {
    missingFields.push('学号/工号')
  }
  if (!patient.idCardNumber || patient.idCardNumber.trim() === '') {
    missingFields.push('身份证号')
  }
  
  // 如果有缺失字段，打开补充信息对话框
  if (missingFields.length > 0) {
    approveForm.patientId = patient.id
    approveForm.name = patient.name || ''
    approveForm.identityNumber = patient.identityNumber || ''
    approveForm.idCardNumber = patient.idCardNumber || ''
    approveDialog.visible = true
    return
  }
  
  // 信息完整，直接审核
  try {
    await ElMessageBox.confirm(
      `确定要审核患者 ${patient.name} 的身份认证吗？\n\n患者信息：\n• 姓名：${patient.name}\n• 学号/工号：${patient.identityNumber}\n• 身份证号：${patient.idCardNumber}`,
      '确认操作',
      { type: 'warning' }
    )
    
    await approvePatient(patient.id)
    ElMessage.success('审核成功')
    await loadPatients()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      const errorMsg = error?.response?.data?.msg || error?.message || '未知错误'
      ElMessage.error('审核失败: ' + errorMsg)
    }
  }
}

// 提交审核（补充信息后）
const submitApprove = async () => {
  if (!approveFormRef.value) return
  
  try {
    const valid = await approveFormRef.value.validate()
    if (!valid) return
    
    approveDialog.loading = true
    
    // 先更新患者信息
    console.log('开始更新患者信息...', {
      patientId: approveForm.patientId,
      name: approveForm.name,
      identityNumber: approveForm.identityNumber,
      idCardNumber: approveForm.idCardNumber
    })
    
    await updatePatient(approveForm.patientId, {
      name: approveForm.name,
      identityNumber: approveForm.identityNumber,
      idCardNumber: approveForm.idCardNumber
    })
    
    console.log('患者信息更新成功，开始获取更新后的数据...')
    
    // 获取更新后的患者信息（包含后端自动设置的角色）
    const updatedPatient = await getPatientById(approveForm.patientId)
    console.log('获取到更新后的患者信息:', updatedPatient)
    console.log('患者数据对象:', updatedPatient?.data)
    console.log('specificRole 字段:', updatedPatient?.data?.specificRole)
    
    // 检查角色字段
    const role = updatedPatient?.data?.specificRole
    if (role && role.trim() !== '') {
      approveForm.specificRole = role
      const roleText = specificRoleText(role)
      console.log('角色已自动设置:', roleText, '原始值:', role)
      ElMessage.success(`信息已更新，角色已自动设置为：${roleText}`)
    } else {
      console.error('未获取到角色信息！')
      console.error('完整响应:', JSON.stringify(updatedPatient, null, 2))
      ElMessage.warning('信息已更新，但未能获取角色信息，请刷新页面查看')
    }
    
    // 再提交审核
    console.log('开始提交审核...')
    await approvePatient(approveForm.patientId)
    
    ElMessage.success('审核成功')
    approveDialog.visible = false
    await loadPatients()
  } catch (error) {
    console.error('审核失败:', error)
    const errorMsg = error?.response?.data?.msg || error?.message || '未知错误'
    ElMessage.error('操作失败: ' + errorMsg)
  } finally {
    approveDialog.loading = false
  }
}

// 重置审核表单
const resetApproveForm = () => {
  if (approveFormRef.value) {
    approveFormRef.value.resetFields()
  }
  approveForm.patientId = null
  approveForm.name = ''
  approveForm.identityNumber = ''
  approveForm.idCardNumber = ''
  approveForm.specificRole = ''
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

// 账户状态中文映射（仅前端显示）
const userStatusText = (v) => {
  const m = {
    active: '已激活',
    disabled: '已停用',
    inactive: '已禁用',
    pending: '待审核',
    pending_approval: '待审核',
    locked: '已锁定'
  }
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
.table-card { margin-top: 8px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
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

/* 添加/编辑患者对话框表单整体左移一点 */
.patient-form-wrap { margin-left: -35px; }
@media (max-width: 768px) { .patient-form-wrap { margin-left: -6px; } }
.patient-detail { padding-left: 15px; }
@media (max-width: 768px) { .patient-detail { padding-left: 10px; } }

/* 表单提示文字样式 */
.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>