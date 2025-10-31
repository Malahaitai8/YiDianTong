<template>
  <div class="doctor-patients">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>患者管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索患者姓名或电话"
              style="width: 250px; margin-right: 10px"
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
              style="width: 120px"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="待就诊" value="待就诊" />
              <el-option label="就诊中" value="就诊中" />
              <el-option label="已完成" value="已完成" />
              <el-option label="已取消" value="已取消" />
            </el-select>
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

// 模拟患者数据
const patientList = ref([
  {
    id: 1,
    name: '张三',
    phone: '138****1234',
    appointmentDate: '2024-01-15',
    appointmentTime: '09:00',
    department: '内科',
    status: '待就诊',
    symptoms: '头痛、发热',
    gender: '男',
    age: 35,
    idCard: '110101198901011234',
    address: '北京市朝阳区',
    doctor: '李医生',
    records: [
      {
        date: '2023-12-15',
        doctor: '李医生',
        diagnosis: '感冒',
        treatment: '多休息，多喝水'
      }
    ]
  },
  {
    id: 2,
    name: '李四',
    phone: '139****5678',
    appointmentDate: '2024-01-15',
    appointmentTime: '09:30',
    department: '内科',
    status: '就诊中',
    symptoms: '咳嗽、胸闷',
    gender: '女',
    age: 28,
    idCard: '110101199501011234',
    address: '北京市海淀区',
    doctor: '李医生',
    records: []
  },
  {
    id: 3,
    name: '王五',
    phone: '137****9012',
    appointmentDate: '2024-01-15',
    appointmentTime: '10:00',
    department: '内科',
    status: '已完成',
    symptoms: '胃痛',
    gender: '男',
    age: 42,
    idCard: '110101198201011234',
    address: '北京市西城区',
    doctor: '李医生',
    records: [
      {
        date: '2024-01-15',
        doctor: '李医生',
        diagnosis: '胃炎',
        treatment: '服用胃药，注意饮食'
      }
    ]
  }
])

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
  // TODO: 实现搜索逻辑
  console.log('搜索:', searchForm)
}

const handleSortChange = ({ column, prop, order }) => {
  // TODO: 实现排序逻辑
  console.log('排序:', { column, prop, order })
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  // TODO: 重新加载数据
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  // TODO: 重新加载数据
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
  pagination.total = patientList.value.length
  // TODO: 加载实际数据
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
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.patient-detail {
  margin-bottom: 20px;
}
</style>