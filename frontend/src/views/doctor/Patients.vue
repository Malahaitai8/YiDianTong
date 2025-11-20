<template>
  <div class="doctor-patients">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>患者管理</span>
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
                <el-option label="晚上" value="EVENING" />
              </el-select>
              <div class="today-stats">
                <el-tag type="warning" effect="light">上午：{{ morningCount }}</el-tag>
                <el-tag type="success" effect="light">下午：{{ afternoonCount }}</el-tag>
                <el-tag type="info" effect="light">晚上：{{ eveningCount }}</el-tag>
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
                <el-option label="就诊中" value="就诊中" />
                <el-option label="已完成" value="已完成" />
                <el-option label="已取消" value="已取消" />
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
      >
        <el-table-column prop="name" label="患者姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" sortable="custom" />
        <el-table-column prop="appointmentTime" label="预约时间" width="100" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="symptoms" label="症状描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === '待就诊'"
              type="primary"
              size="small"
              @click="startConsultation(scope.row)"
            >
              开始就诊
            </el-button>
            <el-button
              v-if="scope.row.status === '就诊中'"
              type="success"
              size="small"
              @click="completeConsultation(scope.row)"
            >
              完成就诊
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewPatientDetail(scope.row)"
            >
              查看详情
            </el-button>
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

    <!-- 患者详情对话框 -->
    <el-dialog
      v-model="patientDialogVisible"
      title="患者详情"
      width="800px"
    >
      <div v-if="selectedPatient" class="patient-detail">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名">
                {{ selectedPatient.name }}
              </el-descriptions-item>
              <el-descriptions-item label="性别">
                {{ selectedPatient.gender }}
              </el-descriptions-item>
              <el-descriptions-item label="年龄">
                {{ selectedPatient.age }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ selectedPatient.phone }}
              </el-descriptions-item>
              <el-descriptions-item label="身份证号">
                {{ selectedPatient.idCard }}
              </el-descriptions-item>
              <el-descriptions-item label="地址">
                {{ selectedPatient.address }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 预约信息 -->
          <el-tab-pane label="预约信息" name="appointment">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="预约日期">
                {{ selectedPatient.appointmentDate }}
              </el-descriptions-item>
              <el-descriptions-item label="预约时间">
                {{ selectedPatient.appointmentTime }}
              </el-descriptions-item>
              <el-descriptions-item label="科室">
                {{ selectedPatient.department }}
              </el-descriptions-item>
              <el-descriptions-item label="医生">
                {{ selectedPatient.doctor }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(selectedPatient.status)">
                  {{ selectedPatient.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="症状描述" :span="2">
                {{ selectedPatient.symptoms }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 就诊记录 -->
          <el-tab-pane label="就诊记录" name="records">
            <el-table :data="selectedPatient.records || []" style="width: 100%">
              <el-table-column prop="date" label="就诊日期" width="120" />
              <el-table-column prop="doctor" label="医生" width="100" />
              <el-table-column prop="diagnosis" label="诊断" min-width="150" />
              <el-table-column prop="treatment" label="治疗方案" min-width="150" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="patientDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 就诊对话框 -->
    <el-dialog
      v-model="consultationDialogVisible"
      title="患者就诊"
      width="600px"
    >
      <el-form
        ref="consultationFormRef"
        :model="consultationForm"
        :rules="consultationRules"
        label-width="80px"
      >
        <el-form-item label="患者姓名">
          <el-input v-model="consultationForm.patientName" disabled />
        </el-form-item>
        <el-form-item label="诊断结果" prop="diagnosis">
          <el-input
            v-model="consultationForm.diagnosis"
            type="textarea"
            :rows="3"
            placeholder="请输入诊断结果"
          />
        </el-form-item>
        <el-form-item label="治疗方案" prop="treatment">
          <el-input
            v-model="consultationForm.treatment"
            type="textarea"
            :rows="3"
            placeholder="请输入治疗方案"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="consultationForm.notes"
            type="textarea"
            :rows="2"
            placeholder="其他备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="consultationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConsultation">完成就诊</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyPatients, getTodayPatients } from '@/api/doctor'

const viewMode = ref('today')
const timeSlotFilter = ref('')
const morningCount = ref(0)
const afternoonCount = ref(0)
const eveningCount = ref(0)

const loading = ref(false)
const patientDialogVisible = ref(false)
const consultationDialogVisible = ref(false)
const selectedPatient = ref(null)
const activeTab = ref('basic')
const consultationFormRef = ref()

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

// 就诊表单
const consultationForm = reactive({
  patientId: '',
  patientName: '',
  diagnosis: '',
  treatment: '',
  notes: ''
})

const consultationRules = {
  diagnosis: [
    { required: true, message: '请输入诊断结果', trigger: 'blur' }
  ],
  treatment: [
    { required: true, message: '请输入治疗方案', trigger: 'blur' }
  ]
}

// 实际患者数据
const patientList = ref([])

// 加载全部患者列表
const loadPatients = async () => {
  try {
    loading.value = true
    // 将中文状态映射为后端状态码，并使用 patientName 作为搜索参数
    const statusMap = {
      '待就诊': 'PENDING',
      '就诊中': 'CONFIRMED',
      '已完成': 'COMPLETED',
      '已取消': 'CANCELLED'
    }
    const params = {
      patientName: searchForm.keyword || undefined,
      status: statusMap[searchForm.status] || undefined
    }
    const resp = await getMyPatients(params)
    const rawList = Array.isArray(resp?.data?.patients)
      ? resp.data.patients
      : Array.isArray(resp?.data)
        ? resp.data
        : []
    const total = typeof resp?.data?.total === 'number' ? resp.data.total : rawList.length

    // 映射为前端展示所需字段
    const mapStatusToCN = (s) => {
      const m = {
        'PENDING': '待就诊',
        'CONFIRMED': '待就诊',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return m[s] || s
    }
    const mapTimeSlotToCN = (t) => (t === 'MORNING' ? '上午' : t === 'AFTERNOON' ? '下午' : t || '')

    patientList.value = rawList.map(item => ({
      id: item.patientId || item.appointmentId || item.id,
      name: item.patientName || item.name,
      phone: item.patientPhone || item.phoneNumber,
      appointmentDate: item.scheduleDate || (item.appointmentTime ? String(item.appointmentTime).split(' ')[0] : ''),
      appointmentTime: mapTimeSlotToCN(item.timeSlot),
      department: item.slotType || '普通门诊',
      status: mapStatusToCN(item.status),
      symptoms: ''
    }))
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
    const rawList = Array.isArray(resp?.data?.patients) ? resp.data.patients : []
    morningCount.value = Number(resp?.data?.morningCount || 0)
    afternoonCount.value = Number(resp?.data?.afternoonCount || 0)
    eveningCount.value = Number(resp?.data?.eveningCount || 0)
    pagination.total = Number(resp?.data?.total || rawList.length || 0)
    patientList.value = rawList.map(item => ({
      id: item.patientId,
      name: item.patientName,
      phone: item.phoneNumber,
      appointmentDate: item.scheduleDate,
      appointmentTime: mapTimeSlotToCN(item.timeSlot),
      department: item.timeSlotName,
      status: item.statusName || item.status,
      symptoms: ''
    }))
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

const getStatusType = (status) => {
  const statusMap = {
    '待就诊': 'warning',
    '就诊中': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
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

const startConsultation = (patient) => {
  consultationForm.patientId = patient.id
  consultationForm.patientName = patient.name
  consultationForm.diagnosis = ''
  consultationForm.treatment = ''
  consultationForm.notes = ''
  consultationDialogVisible.value = true
}

const completeConsultation = async (patient) => {
  try {
    await ElMessageBox.confirm('确定完成该患者的就诊吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用API完成就诊
    patient.status = '已完成'
    ElMessage.success('就诊完成')
  } catch (error) {
    // 用户取消操作
  }
}

const viewPatientDetail = (patient) => {
  selectedPatient.value = patient
  activeTab.value = 'basic'
  patientDialogVisible.value = true
}

const submitConsultation = async () => {
  if (!consultationFormRef.value) return
  
  try {
    const valid = await consultationFormRef.value.validate()
    if (!valid) return
    
    // TODO: 调用API提交就诊记录
    const patient = patientList.value.find(p => p.id === consultationForm.patientId)
    if (patient) {
      patient.status = '已完成'
      if (!patient.records) patient.records = []
      patient.records.push({
        date: new Date().toISOString().split('T')[0],
        doctor: '李医生',
        diagnosis: consultationForm.diagnosis,
        treatment: consultationForm.treatment
      })
    }
    
    consultationDialogVisible.value = false
    ElMessage.success('就诊记录已保存')
  } catch (error) {
    console.error('提交就诊记录失败:', error)
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