<template>
  <div class="doctor-profile">
    <!-- 个人信息卡片 -->
    <el-card class="profile-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" size="small" @click="refreshProfile">
            刷新信息
          </el-button>
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
                <label class="info-label">用户ID：</label>
                <span class="info-value">{{ doctorInfo.userId || '未知' }}</span>
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

    <!-- 统计信息卡片 -->
    <el-card class="stats-card" shadow="never">
      <template #header>
        <span>工作统计</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ workStats.totalPatients }}</div>
            <div class="stat-label">累计患者</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ workStats.monthlyPatients }}</div>
            <div class="stat-label">本月患者</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item">
            <div class="stat-number">{{ workStats.todayPatients }}</div>
            <div class="stat-label">今日患者</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const userStore = useUserStore()
const loading = ref(false)
const doctorInfo = ref(null)

// 工作统计数据（模拟数据，实际应该从API获取）
const workStats = reactive({
  totalPatients: 0,
  monthlyPatients: 0,
  todayPatients: 0
})

// 获取医生详细信息
const loadDoctorProfile = async () => {
  try {
    loading.value = true
    
    // 获取当前登录用户的userId
    const currentUserId = userStore.user?.userId
    if (!currentUserId) {
      ElMessage.error('无法获取用户信息，请重新登录')
      return
    }

    // 调用获取所有医生信息的API
    const response = await request.get('/doctor/selectAll')
    
    // request实例已经处理了响应结构，直接使用response.data
    const allDoctors = response.data
    
    // 在前端过滤出当前用户的医生信息
    const currentDoctor = allDoctors.find(doctor => doctor.userId === currentUserId)
    
    if (currentDoctor) {
      // 获取科室信息：doctor -> clinic -> department
      await loadDepartmentInfo(currentDoctor)
      doctorInfo.value = currentDoctor
      ElMessage.success('医生信息加载成功')
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
}

// 加载工作统计（模拟数据）
const loadWorkStats = () => {
  // 这里应该调用实际的API获取统计数据
  workStats.totalPatients = 156
  workStats.monthlyPatients = 28
  workStats.todayPatients = 5
}

onMounted(() => {
  loadDoctorProfile()
  loadWorkStats()
})
</script>

<style scoped>
.doctor-profile {
  padding: 0;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: #409EFF;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
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