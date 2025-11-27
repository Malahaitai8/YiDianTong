<template>
  <div class="doctor-application-page">
    <el-card shadow="never" class="intro-card">
      <div class="intro-content">
        <div>
          <h2>统一申请管理</h2>
          <p>在这里可以提交调班申请和信息修改申请，并实时跟踪审批状态。</p>
        </div>
        <el-space wrap>
          <el-tag type="warning">调班申请</el-tag>
          <el-tag type="info">信息修改申请</el-tag>
          <el-tag type="success">审批进度实时更新</el-tag>
        </el-space>
      </div>
    </el-card>

    <el-row :gutter="16" class="form-section">
      <el-col :span="12" :xs="24">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header">
              <span>提交新申请</span>
              <el-button link type="primary" @click="loadMyData">刷新数据</el-button>
            </div>
          </template>

          <el-tabs v-model="activeTab" class="apply-tabs">
            <el-tab-pane label="调班申请" name="SCHEDULE_CHANGE">
              <el-form
                ref="scheduleFormRef"
                :model="scheduleForm"
                :rules="scheduleRules"
                label-width="110px"
                class="apply-form"
              >
                <el-form-item label="关联排班" prop="scheduleId">
                  <el-select
                    v-model="scheduleForm.scheduleId"
                    placeholder="请选择需要调整的排班"
                    filterable
                    style="width: 100%"
                    :disabled="scheduleOptions.length === 0"
                    @change="handleScheduleChange"
                  >
                    <el-option
                      v-for="item in scheduleOptions"
                      :key="item.id"
                      :label="formatScheduleOption(item)"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="变更类型" prop="changeType">
                  <el-radio-group v-model="scheduleForm.changeType">
                    <el-radio-button label="RESCHEDULE">改期</el-radio-button>
                    <el-radio-button label="CANCEL">取消排班</el-radio-button>
                    <el-radio-button label="SLOTS_ADJUST">号源调整</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="原排班信息">
                  <div class="info-readonly">
                    {{ scheduleForm.originalDate || '未选择' }} ·
                    {{ timeSlotText[scheduleForm.originalTimeSlot] || scheduleForm.originalTimeSlot || '-' }}
                  </div>
                </el-form-item>
                <el-form-item
                  label="新日期"
                  prop="newDate"
                  v-if="scheduleForm.changeType === 'RESCHEDULE'"
                >
                  <el-date-picker
                    v-model="scheduleForm.newDate"
                    type="date"
                    placeholder="选择新日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                  />
                </el-form-item>
                <el-form-item
                  label="新时段"
                  prop="newTimeSlot"
                  v-if="scheduleForm.changeType === 'RESCHEDULE'"
                >
                  <el-select v-model="scheduleForm.newTimeSlot" placeholder="选择新时段">
                    <el-option
                      v-for="item in timeSlotOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item
                  label="号源调整"
                  prop="slotsAdjustment"
                  v-if="scheduleForm.changeType === 'SLOTS_ADJUST'"
                >
                  <el-input-number
                    v-model="scheduleForm.slotsAdjustment"
                    :min="-20"
                    :max="50"
                    :step="1"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="申请原因">
                  <el-input
                    v-model="scheduleForm.reason"
                    type="textarea"
                    :rows="3"
                    maxlength="200"
                    show-word-limit
                    placeholder="选填，说明调班原因"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button @click="resetScheduleForm">重置</el-button>
                  <el-button
                    type="primary"
                    :loading="scheduleSubmitting"
                    @click="submitScheduleRequest"
                  >
                    提交申请
                  </el-button>
                </el-form-item>
              </el-form>
              <el-alert
                v-if="scheduleOptions.length === 0"
                type="warning"
                :closable="false"
                title="暂无可调整的排班，请先确认个人排班信息。"
              />
            </el-tab-pane>

            <el-tab-pane label="信息修改申请" name="INFO_UPDATE">
              <el-form
                ref="infoFormRef"
                :model="infoForm"
                :rules="infoRules"
                label-width="110px"
                class="apply-form"
              >
                <el-form-item label="字段" prop="fieldName">
                  <el-select v-model="infoForm.fieldName" placeholder="选择需要修改的字段">
                    <el-option label="姓名" value="name" />
                    <el-option label="职称" value="title" />
                    <el-option label="专长" value="specialty" />
                    <el-option label="个人简介" value="bio" />
                    <el-option label="科室" value="department" />
                  </el-select>
                </el-form-item>
                <el-form-item label="原值">
                  <el-input v-model="infoForm.oldValue" placeholder="原值（自动读取，可手动修改）" />
                </el-form-item>
                <el-form-item label="新值" prop="newValue">
                  <el-input v-model="infoForm.newValue" placeholder="请输入新的值" />
                </el-form-item>
                <el-form-item label="申请原因">
                  <el-input
                    v-model="infoForm.reason"
                    type="textarea"
                    :rows="3"
                    maxlength="200"
                    show-word-limit
                    placeholder="选填，说明修改原因"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button @click="resetInfoForm">重置</el-button>
                  <el-button
                    type="primary"
                    :loading="infoSubmitting"
                    @click="submitInfoRequest"
                  >
                    提交申请
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="12" :xs="24">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <span>我的申请记录</span>
              <el-space>
                <el-select
                  v-model="listFilters.type"
                  placeholder="全部类型"
                  clearable
                  style="width: 140px"
                >
                  <el-option label="调班申请" value="SCHEDULE_CHANGE" />
                  <el-option label="信息修改" value="INFO_UPDATE" />
                </el-select>
                <el-select
                  v-model="listFilters.status"
                  placeholder="全部状态"
                  clearable
                  style="width: 140px"
                >
                  <el-option label="待审核" value="PENDING" />
                  <el-option label="已批准" value="APPROVED" />
                  <el-option label="已拒绝" value="REJECTED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
                <el-button type="primary" link @click="loadMyRequests">刷新</el-button>
              </el-space>
            </div>
          </template>

          <el-table
            :data="filteredRequests"
            v-loading="listLoading"
            height="calc(100vh - 380px)"
            empty-text="暂无申请记录"
            row-key="id"
            border
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="类型" width="130">
              <template #default="{ row }">
                <el-tag :type="requestTypeTag[row.requestType] || 'info'">
                  {{ requestTypeText[row.requestType] || row.requestType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="摘要" min-width="220">
              <template #default="{ row }">
                <div v-if="row.requestType === 'SCHEDULE_CHANGE'">
                  排班 {{ row.scheduleId }} · {{ changeTypeText[row.changeType] || '未知' }}
                </div>
                <div v-else>
                  {{ fieldNameText[row.fieldName] || row.fieldName }} → {{ row.newValue }}
                </div>
                <div class="reason-text" v-if="row.reason">
                  <el-icon><ChatLineRound /></el-icon>
                  {{ row.reason }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType[row.status] || 'info'">
                  {{ statusText[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提交时间" width="170">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openDetail(row.id)">详情</el-button>
                <el-divider direction="vertical" />
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="danger"
                  link
                  @click="handleCancel(row.id)"
                >
                  取消
                </el-button>
                <span v-else class="status-static">{{ statusText[row.status] || row.status }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-drawer
      v-model="detailDrawer.visible"
      title="申请详情"
      size="500px"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <div v-if="detailDrawer.loading" class="drawer-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="detailDrawer.data" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请ID">{{ detailDrawer.data.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detailDrawer.data.requestTypeName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailDrawer.data.statusName }}</el-descriptions-item>
          <el-descriptions-item label="申请原因">
            {{ detailDrawer.data.reason || '未填写' }}
          </el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">业务信息</el-divider>
        <div v-if="detailDrawer.data.requestType === 'SCHEDULE_CHANGE'">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="排班ID">
              {{ detailDrawer.data.scheduleId }}
            </el-descriptions-item>
            <el-descriptions-item label="变更类型">
              {{ detailDrawer.data.changeTypeName }}
            </el-descriptions-item>
            <el-descriptions-item label="原排班">
              {{ detailDrawer.data.originalDate }} · {{ detailDrawer.data.originalTimeSlotName }}
            </el-descriptions-item>
            <el-descriptions-item label="新排班">
              <span v-if="detailDrawer.data.newDate">
                {{ detailDrawer.data.newDate }} · {{ detailDrawer.data.newTimeSlotName }}
              </span>
              <span v-else>无</span>
            </el-descriptions-item>
            <el-descriptions-item label="号源调整">
              {{ detailDrawer.data.slotsAdjustment ?? '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="字段">
              {{ detailDrawer.data.fieldNameChinese || detailDrawer.data.fieldName }}
            </el-descriptions-item>
            <el-descriptions-item label="旧值">
              {{ detailDrawer.data.oldValue || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="新值">
              {{ detailDrawer.data.newValue || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-divider content-position="left">审核信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="审核人">
            {{ detailDrawer.data.reviewerUsername || '未审核' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ detailDrawer.data.reviewedAt || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="拒绝原因">
            {{ detailDrawer.data.rejectionReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ detailDrawer.data.updatedAt || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="drawer-loading">
        <el-empty description="暂无数据" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  createApplicationRequest,
  getMyApplicationRequests,
  getApplicationRequestFullDetail,
  cancelApplicationRequest
} from '@/api/applicationRequest'
import { getMyInfo, getMySchedules } from '@/api/doctor'
import { ChatLineRound } from '@element-plus/icons-vue'

const userStore = useUserStore()
const doctorId = computed(() => userStore.user?.doctorId || userStore.user?.id)

const activeTab = ref('SCHEDULE_CHANGE')
const scheduleFormRef = ref()
const infoFormRef = ref()
const scheduleSubmitting = ref(false)
const infoSubmitting = ref(false)

const scheduleOptions = ref([])
const doctorProfile = ref(null)
const listLoading = ref(false)
const requestList = ref([])

const timeSlotText = {
  MORNING: '上午',
  AFTERNOON: '下午',
  EVENING: '夜间'
}

const timeSlotOptions = [
  { label: '上午', value: 'MORNING' },
  { label: '下午', value: 'AFTERNOON' },
  { label: '夜间', value: 'EVENING' }
]

const changeTypeText = {
  RESCHEDULE: '改期',
  CANCEL: '取消排班',
  SLOTS_ADJUST: '号源调整'
}

const fieldNameText = {
  name: '姓名',
  title: '职称',
  specialty: '专长',
  bio: '简介',
  department: '科室'
}

const statusText = {
  PENDING: '待审核',
  APPROVED: '已批准',
  REJECTED: '已拒绝',
  CANCELLED: '已取消'
}

const statusTagType = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'info'
}

const requestTypeText = {
  SCHEDULE_CHANGE: '调班申请',
  INFO_UPDATE: '信息修改申请'
}

const requestTypeTag = {
  SCHEDULE_CHANGE: 'warning',
  INFO_UPDATE: 'info'
}

const scheduleForm = reactive({
  scheduleId: '',
  changeType: 'RESCHEDULE',
  originalDate: '',
  originalTimeSlot: '',
  newDate: '',
  newTimeSlot: '',
  slotsAdjustment: 0,
  reason: ''
})

const validators = {
  rescheduleDate: (rule, value, callback) => {
    if (scheduleForm.changeType === 'RESCHEDULE' && !value) {
      callback(new Error('请选择新日期'))
    } else {
      callback()
    }
  },
  rescheduleSlot: (rule, value, callback) => {
    if (scheduleForm.changeType === 'RESCHEDULE' && !value) {
      callback(new Error('请选择新时段'))
    } else {
      callback()
    }
  },
  slotAdjust: (rule, value, callback) => {
    if (scheduleForm.changeType === 'SLOTS_ADJUST') {
      if (value === null || value === undefined) {
        callback(new Error('请输入调整数量'))
        return
      }
    }
    callback()
  }
}

const scheduleRules = {
  scheduleId: [{ required: true, message: '请选择排班', trigger: 'change' }],
  changeType: [{ required: true, message: '请选择变更类型', trigger: 'change' }],
  newDate: [{ validator: validators.rescheduleDate, trigger: 'change' }],
  newTimeSlot: [{ validator: validators.rescheduleSlot, trigger: 'change' }],
  slotsAdjustment: [{ validator: validators.slotAdjust, trigger: 'blur' }]
}

const infoForm = reactive({
  fieldName: '',
  oldValue: '',
  newValue: '',
  reason: ''
})

const infoRules = {
  fieldName: [{ required: true, message: '请选择字段', trigger: 'change' }],
  newValue: [{ required: true, message: '请输入新值', trigger: 'blur' }]
}

watch(
  () => infoForm.fieldName,
  (field) => {
    if (!field) {
      infoForm.oldValue = ''
      return
    }
    infoForm.oldValue = doctorProfile.value?.[field] || ''
  }
)

const listFilters = reactive({
  type: '',
  status: ''
})

const filteredRequests = computed(() => {
  return requestList.value
    .filter((item) => {
      if (listFilters.type && item.requestType !== listFilters.type) return false
      if (listFilters.status && item.status !== listFilters.status) return false
      return true
    })
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
})

const loadSchedules = async () => {
  try {
    const resp = await getMySchedules()
    scheduleOptions.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    scheduleOptions.value = []
    console.error('获取排班失败', error)
  }
}

const loadDoctorProfile = async () => {
  try {
    const resp = await getMyInfo()
    doctorProfile.value = resp?.data || null
  } catch (error) {
    doctorProfile.value = null
    console.error('获取医生信息失败', error)
  }
}

const loadMyRequests = async () => {
  try {
    listLoading.value = true
    const resp = await getMyApplicationRequests()
    requestList.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    console.error('获取申请记录失败', error)
    requestList.value = []
    ElMessage.error(error?.response?.data?.msg || '获取申请记录失败')
  } finally {
    listLoading.value = false
  }
}

const loadMyData = async () => {
  await Promise.all([loadSchedules(), loadDoctorProfile(), loadMyRequests()])
}

const formatScheduleOption = (item) => {
  const date = item.scheduleDate || item.date || '-'
  const slot = timeSlotText[item.timeSlot] || item.timeSlot || '-'
  const clinic = item.clinicName || item.department || ''
  return `${date} · ${slot}${clinic ? ` · ${clinic}` : ''} · 号源 ${item.availableSlots ?? '-'}`
}

const handleScheduleChange = (val) => {
  const selected = scheduleOptions.value.find((item) => item.id === val)
  if (selected) {
    scheduleForm.originalDate = selected.scheduleDate || selected.date || ''
    scheduleForm.originalTimeSlot = selected.timeSlot || ''
  } else {
    scheduleForm.originalDate = ''
    scheduleForm.originalTimeSlot = ''
  }
}

const resetScheduleForm = () => {
  scheduleForm.scheduleId = ''
  scheduleForm.changeType = 'RESCHEDULE'
  scheduleForm.originalDate = ''
  scheduleForm.originalTimeSlot = ''
  scheduleForm.newDate = ''
  scheduleForm.newTimeSlot = ''
  scheduleForm.slotsAdjustment = 0
  scheduleForm.reason = ''
}

const resetInfoForm = () => {
  infoForm.fieldName = ''
  infoForm.oldValue = ''
  infoForm.newValue = ''
  infoForm.reason = ''
}

const submitScheduleRequest = async () => {
  if (!scheduleFormRef.value) return
  if (!doctorId.value) {
    ElMessage.error('无法获取医生身份，请重新登录')
    return
  }
  try {
    const valid = await scheduleFormRef.value.validate()
    if (!valid) return
    scheduleSubmitting.value = true
    const payload = {
      requestType: 'SCHEDULE_CHANGE',
      scheduleId: Number(scheduleForm.scheduleId),
      changeType: scheduleForm.changeType,
      originalDate: scheduleForm.originalDate,
      originalTimeSlot: scheduleForm.originalTimeSlot,
      reason: scheduleForm.reason || undefined
    }
    if (scheduleForm.changeType === 'RESCHEDULE') {
      payload.newDate = scheduleForm.newDate
      payload.newTimeSlot = scheduleForm.newTimeSlot
    }
    if (scheduleForm.changeType === 'SLOTS_ADJUST') {
      payload.slotsAdjustment = scheduleForm.slotsAdjustment
    }
    await createApplicationRequest(payload)
    ElMessage.success('调班申请已提交')
    resetScheduleForm()
    await loadMyRequests()
  } catch (error) {
    if (error === false) return
    console.error('提交调班申请失败', error)
    ElMessage.error(error?.response?.data?.msg || '提交失败')
  } finally {
    scheduleSubmitting.value = false
  }
}

const submitInfoRequest = async () => {
  if (!infoFormRef.value) return
  if (!doctorId.value) {
    ElMessage.error('无法获取医生身份，请重新登录')
    return
  }
  try {
    const valid = await infoFormRef.value.validate()
    if (!valid) return
    infoSubmitting.value = true
    const payload = {
      requestType: 'INFO_UPDATE',
      doctorId: doctorId.value,
      fieldName: infoForm.fieldName,
      oldValue: infoForm.oldValue || undefined,
      newValue: infoForm.newValue,
      reason: infoForm.reason || undefined
    }
    await createApplicationRequest(payload)
    ElMessage.success('信息修改申请已提交')
    resetInfoForm()
    await loadMyRequests()
  } catch (error) {
    if (error === false) return
    console.error('提交信息修改申请失败', error)
    ElMessage.error(error?.response?.data?.msg || '提交失败')
  } finally {
    infoSubmitting.value = false
  }
}

const handleCancel = async (id) => {
  if (!id) return
  try {
    await ElMessageBox.confirm('确认取消该申请？', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '返回'
    })
    await cancelApplicationRequest(id)
    ElMessage.success('申请已取消')
    await loadMyRequests()
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('取消申请失败', error)
    ElMessage.error(error?.response?.data?.msg || '取消失败')
  }
}

const detailDrawer = reactive({
  visible: false,
  loading: false,
  data: null
})

const openDetail = async (id) => {
  if (!id) return
  detailDrawer.visible = true
  detailDrawer.loading = true
  detailDrawer.data = null
  try {
    const resp = await getApplicationRequestFullDetail(id)
    detailDrawer.data = resp?.data || null
  } catch (error) {
    console.error('查看详情失败', error)
    ElMessage.error(error?.response?.data?.msg || '获取详情失败')
  } finally {
    detailDrawer.loading = false
  }
}

const formatDate = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await loadMyData()
})
</script>

<style scoped>
.doctor-application-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.intro-card h2 {
  margin: 0 0 4px;
}

.intro-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.form-section {
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.apply-tabs :deep(.el-tabs__content) {
  padding-top: 12px;
}

.apply-form {
  padding-right: 8px;
}

.info-readonly {
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  color: #606266;
}

.reason-text {
  margin-top: 4px;
  color: #909399;
  display: flex;
  gap: 4px;
  align-items: center;
}

.status-static {
  color: #909399;
  font-size: 13px;
}

.drawer-loading {
  padding: 12px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 992px) {
  .apply-form {
    padding-right: 0;
  }

  .list-card :deep(.el-table) {
    font-size: 13px;
  }
}
</style>

