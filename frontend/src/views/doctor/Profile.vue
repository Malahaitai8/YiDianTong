<template>
  <div class="doctor-profile">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="refreshProfile">刷新信息</el-button>
            <el-button type="warning" size="small" @click="openApplyDialog">修改信息</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else-if="doctorInfo" class="profile-content">
        <div class="profile-header">
          <div class="avatar-section">
            <el-avatar :size="100" class="doctor-avatar">
              {{ doctorInfo.name ? doctorInfo.name.charAt(0) : '医' }}
            </el-avatar>
          </div>
          <div class="basic-info">
            <h2 class="doctor-name">{{ doctorInfo.name || '未设置姓名' }}</h2>
            <p class="doctor-title">{{ doctorInfo.title || '未设置职称' }}</p>
            <p class="doctor-department">{{ doctorInfo.department || '未设置科室' }}</p>
          </div>
        </div>

        <el-divider />

        <div class="profile-details">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label class="info-label">姓名：</label>
                <span class="info-value">{{ doctorInfo.name || '未设置' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label class="info-label">职称：</label>
                <span class="info-value">{{ doctorInfo.title || '未设置' }}</span>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label class="info-label">科室：</label>
                <span class="info-value">{{ doctorInfo.department || '未设置' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label class="info-label">医生ID：</label>
                <span class="info-value">{{ doctorInfo.id || '未知' }}</span>
              </div>
            </el-col>
          </el-row>

          <div class="info-item full-width">
            <label class="info-label">擅长领域：</label>
            <div class="info-value">
              {{ doctorInfo.specialty || '未设置擅长领域' }}
            </div>
          </div>

          <div class="info-item full-width">
            <label class="info-label">个人简介：</label>
            <div class="info-value bio-content">
              {{ doctorInfo.bio || '暂无个人简介' }}
            </div>
          </div>
        </div>
      </div>

      <div v-else class="error-state">
        <el-empty description="无法获取医生信息">
          <el-button type="primary" @click="loadDoctorProfile">重新加载</el-button>
        </el-empty>
      </div>
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="提交信息修改申请" width="600px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="字段" prop="fieldName">
              <el-select v-model="applyForm.fieldName" placeholder="请选择要修改的字段" style="width: 100%">
                <el-option label="姓名" value="name" />
                <el-option label="职称" value="title" />
                <el-option label="擅长领域" value="specialty" />
                <el-option label="个人简介" value="bio" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新值" prop="newValue">
              <el-input v-model="applyForm.newValue" placeholder="请输入新的值" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="申请原因" prop="reason">
              <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="可选，填写申请原因" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="closeApplyDialog">取消</el-button>
        <el-button type="primary" :loading="applySubmitting" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 我的申请记录 -->
    <el-card class="applications-card" shadow="never">
      <template #header>
        <span>我的信息修改申请</span>
      </template>
      <el-table :data="applications" style="width: 100%">
        <el-table-column prop="fieldName" label="字段" width="120" />
        <el-table-column prop="oldValue" label="原值" min-width="150" />
        <el-table-column prop="newValue" label="新值" min-width="150" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" />
        <el-table-column prop="createdAt" label="提交时间" width="180" />
      </el-table>
      <div v-if="applications.length === 0" class="empty-state">
        <el-empty description="暂无申请记录" />
      </div>
    </el-card>

    
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { getMyInfo, applyDoctorInfoUpdate, getMyInfoApplications } from '@/api/doctor'

const userStore = useUserStore()
const loading = ref(false)
const doctorInfo = ref(null)

// 获取医生详细信息
const loadDoctorProfile = async () => {
  try {
    loading.value = true
    const res = await getMyInfo()
    const info = res?.data || null
    if (info) {
      await loadDepartmentInfo(info)
      doctorInfo.value = info
    } else {
      ElMessage.warning('未找到对应的医生信息，请联系管理员')
      doctorInfo.value = null
    }
  } catch (error) {
    console.error('获取医生信息失败:', error)
    ElMessage.error('获取医生信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取科室信息
const loadDepartmentInfo = async (doctor) => {
  try {
    if (!doctor.clinicId) {
      doctor.department = '未分配科室'
      return
    }

    // 1. 根据clinicId查询clinic信息
    const clinicResponse = await request.get(`/clinic/selectById/${doctor.clinicId}`)
    const clinic = clinicResponse.data
    
    if (!clinic || !clinic.departmentId) {
      doctor.department = '科室信息不完整'
      return
    }

    // 2. 根据departmentId查询department信息
    const departmentResponse = await request.get(`/department/selectById/${clinic.departmentId}`)
    const department = departmentResponse.data
    
    if (department && department.name) {
      doctor.department = department.name
    } else {
      doctor.department = '未知科室'
    }
  } catch (error) {
    console.error('获取科室信息失败:', error)
    doctor.department = '获取科室信息失败'
  }
}

// 刷新个人信息
const refreshProfile = () => {
  loadDoctorProfile()
  loadMyApplications()
}


onMounted(() => {
  loadDoctorProfile()
  loadMyApplications()
})

// 申请表单
const applyFormRef = ref()
const applySubmitting = ref(false)
const applyForm = reactive({ fieldName: '', newValue: '', reason: '' })
const applyRules = {
  fieldName: [{ required: true, message: '请选择字段', trigger: 'change' }],
  newValue: [{ required: true, message: '请输入新值', trigger: 'blur' }]
}
const resetApplyForm = () => {
  applyForm.fieldName = ''
  applyForm.newValue = ''
  applyForm.reason = ''
}
const submitApply = async () => {
  if (!applyFormRef.value) return
  try {
    const valid = await applyFormRef.value.validate()
    if (!valid) return
    applySubmitting.value = true
    await applyDoctorInfoUpdate({
      fieldName: applyForm.fieldName,
      newValue: applyForm.newValue,
      reason: applyForm.reason || undefined
    })
    ElMessage.success('申请已提交，请等待管理员审核')
    resetApplyForm()
    await loadMyApplications()
    closeApplyDialog()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '提交申请失败')
  } finally {
    applySubmitting.value = false
  }
}

// 我的申请列表
const applications = ref([])
const loadMyApplications = async () => {
  try {
    const res = await getMyInfoApplications()
    const list = Array.isArray(res?.data) ? res.data : []
    applications.value = list
  } catch (e) {
    applications.value = []
  }
}

const statusText = (s) => {
  const m = { PENDING: '待审核', APPROVED: '已批准', REJECTED: '已拒绝', CANCELLED: '已取消' }
  return m[s] || s
}
const statusTagType = (s) => {
  const m = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
  return m[s] || 'info'
}

const applyDialogVisible = ref(false)
const openApplyDialog = () => {
  resetApplyForm()
  applyDialogVisible.value = true
}
const closeApplyDialog = () => {
  applyDialogVisible.value = false
}
</script>

<style scoped>
.doctor-profile {
  padding: 0;
}
.applications-card { margin-bottom: 20px; }

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-container {
  padding: 20px;
}

.profile-content {
  padding: 0;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-section {
  margin-right: 30px;
}

.doctor-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 36px;
  font-weight: 600;
}

.basic-info {
  flex: 1;
}

.doctor-name {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.doctor-title {
  margin: 0 0 5px 0;
  color: #409EFF;
  font-size: 16px;
  font-weight: 500;
}

.doctor-department {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.profile-details {
  padding: 0;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  min-height: 24px;
}

.info-item.full-width {
  flex-direction: column;
  align-items: stretch;
}

.info-label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  margin-right: 10px;
  font-size: 14px;
}

.full-width .info-label {
  margin-bottom: 8px;
}

.info-value {
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
  flex: 1;
}

.bio-content {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
  min-height: 60px;
  white-space: pre-wrap;
}

.error-state {
  padding: 40px 0;
  text-align: center;
}

 

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-section {
    margin-right: 0;
    margin-bottom: 20px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .info-label {
    margin-bottom: 5px;
    margin-right: 0;
  }
}
</style>