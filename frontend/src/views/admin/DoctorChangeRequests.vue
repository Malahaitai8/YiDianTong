<template>
  <div class="doctor-change-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="医生ID">
          <el-input
            v-model="filters.doctorId"
            placeholder="输入医生ID"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态筛选">
          <el-select
            v-model="filters.status"
            placeholder="全部状态"
            style="width: 180px"
            @change="handleStatusChange"
          >
            <el-option label="全部" value="" />
            <el-option
              v-for="opt in statusOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <span>变更申请列表</span>
            <el-tag type="info" class="count-tag">共 {{ requestList.length }} 条</el-tag>
          </div>
          <el-button link type="primary" @click="loadRequests">刷新</el-button>
        </div>
      </template>

      <el-table
        :data="requestList"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无申请"
      >
        <el-table-column prop="id" label="申请ID" width="180" />
        <el-table-column prop="doctorId" label="医生ID" width="100" />
        <el-table-column label="医生姓名" min-width="140">
          <template #default="{ row }">
            <div class="doctor-name">
              <span>{{ row.name || '-' }}</span>
            </div>
            <div class="doctor-title">{{ row.title || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="所属门诊" min-width="140">
          <template #default="{ row }">
            {{ getClinicName(row.clinicId) }}
          </template>
        </el-table-column>
        <el-table-column prop="specialty" label="专长" min-width="150" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status] || 'info'">
              {{ statusText[row.status] || row.status || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">
              查看详情
            </el-button>
            <el-divider direction="vertical" />
            <template v-if="row.status === 'PENDING'">
              <el-button
                type="success"
                link
                @click="handleReview(row, 'APPROVE')"
              >
                通过
              </el-button>
              <el-divider direction="vertical" />
              <el-button
                type="danger"
                link
                @click="handleReview(row, 'REJECT')"
              >
                拒绝
              </el-button>
            </template>
            <template v-else>
              <span class="review-result">
                {{ row.status === 'APPROVED' ? '已通过' : '已处理' }}
              </span>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="detailDialog.visible"
      title="变更申请详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="detailDialog.data" :column="1" border>
        <el-descriptions-item label="申请ID">
          {{ detailDialog.data.id }}
        </el-descriptions-item>
        <el-descriptions-item label="医生ID">
          {{ detailDialog.data.doctorId }}
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          {{ detailDialog.data.name || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="职称">
          {{ detailDialog.data.title || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="专长">
          {{ detailDialog.data.specialty || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="所属门诊">
          {{ getClinicName(detailDialog.data.clinicId) }}
        </el-descriptions-item>
        <el-descriptions-item label="简介">
          <span class="bio-text">{{ detailDialog.data.bio || '暂无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusTagType[detailDialog.data.status] || 'info'">
            {{ statusText[detailDialog.data.status] || detailDialog.data.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDate(detailDialog.data.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="审批结果">
          {{ detailDialog.data.approvedBy ? `${detailDialog.data.approvedBy} / ${formatDate(detailDialog.data.approvedAt)}` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核备注">
          {{ detailDialog.data.reason || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClinicList } from '@/api/clinic'
import {
  getDoctorChangeByDoctor,
  getAdminDoctorChangeList,
  reviewDoctorChange
} from '@/api/doctorChange'

const loading = ref(false)
const requestList = ref([])
const clinics = ref([])

const filters = reactive({
  doctorId: '',
  status: ''
})

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const statusText = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

const statusTagType = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger'
}

const detailDialog = reactive({
  visible: false,
  data: null
})

const loadClinics = async () => {
  try {
    const resp = await getClinicList()
    clinics.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    console.error('加载门诊列表失败', error)
  }
}

const getClinicName = (clinicId) => {
  if (!clinicId) return '未指定'
  const clinic = clinics.value.find((item) => item.id === clinicId)
  return clinic?.name || `ID: ${clinicId}`
}

const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  try {
    return new Date(timestamp).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return '-'
  }
}

const loadRequests = async () => {
  try {
    loading.value = true
    const trimmedDoctorId = filters.doctorId.trim()
    let resp

    if (trimmedDoctorId) {
      if (!/^\d+$/.test(trimmedDoctorId)) {
        ElMessage.warning('医生ID需为数字')
        return
      }
      resp = await getDoctorChangeByDoctor(trimmedDoctorId)
    } else {
      resp = await getAdminDoctorChangeList(filters.status || undefined)
    }

    requestList.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    console.error('获取变更申请失败', error)
    ElMessage.error(error?.response?.data?.msg || '获取变更申请失败')
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  if (!filters.doctorId.trim()) {
    loadRequests()
  }
}

const handleSearch = () => {
  loadRequests()
}

const resetFilters = () => {
  filters.doctorId = ''
  filters.status = ''
  loadRequests()
}

const openDetail = (row) => {
  detailDialog.data = row
  detailDialog.visible = true
}

const handleReview = async (row, action) => {
  if (!row?.id) return
  try {
    let reason = ''
    if (action === 'APPROVE') {
      await ElMessageBox.confirm(
        `确定要通过医生 ${row.name || row.doctorId} 的变更申请吗？`,
        '审核确认',
        {
          confirmButtonText: '通过',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } else {
      const { value } = await ElMessageBox.prompt(
        `请输入拒绝医生 ${row.name || row.doctorId} 申请的原因`,
        '拒绝申请',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入拒绝原因',
          inputValidator: (val) => {
            if (!val || !val.trim()) return '拒绝原因不能为空'
            return true
          }
        }
      )
      reason = value.trim()
    }

    await reviewDoctorChange({
      id: row.id,
      action,
      reason: reason || undefined
    })
    ElMessage.success(action === 'APPROVE' ? '已通过申请' : '已拒绝申请')
    await loadRequests()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核变更申请失败', error)
      ElMessage.error(error?.response?.data?.msg || '操作失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadClinics(), loadRequests()])
})
</script>

<style scoped>
.doctor-change-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  margin-bottom: 0;
}

.filter-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count-tag {
  margin-left: 10px;
}

.doctor-title {
  color: #909399;
  font-size: 12px;
}

.review-result {
  color: #909399;
}

.bio-text {
  white-space: pre-wrap;
}
</style>

