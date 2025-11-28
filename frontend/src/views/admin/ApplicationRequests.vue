<template>
  <div class="application-requests-page">
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in statCards" :key="card.key" :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-card__content">
            <div>
              <div class="stat-card__label">{{ card.label }}</div>
              <div class="stat-card__value">
                <span v-if="!statLoading">{{ stats[card.key] ?? 0 }}</span>
                <el-skeleton v-else :rows="1" animated />
              </div>
            </div>
            <el-tag :type="card.tagType" effect="dark">{{ card.badge }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="申请类型">
          <el-select
            v-model="filters.requestType"
            placeholder="全部类型"
            style="width: 180px"
            clearable
            @change="handleLocalFilterChange"
          >
            <el-option
              v-for="item in requestTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filters.status"
            placeholder="全部状态"
            style="width: 180px"
            clearable
            @change="handleStatusChange"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleLocalFilterChange"
          />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model.trim="filters.keyword"
            placeholder="支持申请人/理由/字段"
            clearable
            @clear="handleLocalFilterChange"
            @keyup.enter="handleLocalFilterChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLocalFilterChange">筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item class="quick-buttons">
          <el-button-group>
            <el-button
              v-for="item in quickFilters"
              :key="item.value"
              :type="quickStatus === item.value ? 'primary' : 'default'"
              @click="handleQuickStatus(item.value)"
            >
              {{ item.label }}
            </el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-header__left">
            <span>申请列表</span>
            <el-tag class="count-tag" type="info" effect="plain">
              共 {{ filteredRequests.length }} 条
            </el-tag>
          </div>
          <div class="table-header__actions">
            <el-button link type="primary" @click="loadStats">刷新统计</el-button>
            <el-divider direction="vertical" />
            <el-button link type="primary" @click="loadRequests">刷新列表</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredRequests"
        height="calc(100vh - 420px)"
        border
        v-loading="loading"
        :row-key="(row) => row.id"
        empty-text="暂无申请记录"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="140">
          <template #default="{ row }">
            <el-tag :type="requestTypeTagType[row.requestType] || 'info'">
              {{ requestTypeText[row.requestType] || row.requestType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请人" min-width="180">
          <template #default="{ row }">
            <div class="applicant-cell">
              <div class="applicant-name">
                {{ row.applicantUsername || `ID:${row.applicantId}` }}
              </div>
              <div class="applicant-role">{{ roleText[row.applicantRole] || row.applicantRole }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="申请内容" min-width="260">
          <template #default="{ row }">
            <div v-if="row.requestType === 'SCHEDULE_CHANGE'">
              排班ID：{{ row.scheduleId }} · {{ changeTypeText[row.changeType] || '未知' }}
            </div>
            <div v-else-if="row.requestType === 'INFO_UPDATE'">
              医生ID：{{ row.doctorId }} · {{ fieldNameText[row.fieldName] || row.fieldName }}
            </div>
            <div class="reason-text" v-if="row.reason">
              <el-icon class="reason-icon"><ChatLineRound /></el-icon>
              {{ row.reason }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType[row.status] || 'info'">
              {{ statusText[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="170">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row.id)">详情</el-button>
            <el-divider direction="vertical" />
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link @click="handleReview(row.id, 'APPROVED')">
                批准
              </el-button>
              <el-divider direction="vertical" />
              <el-button type="danger" link @click="handleReview(row.id, 'REJECTED')">
                拒绝
              </el-button>
            </template>
            <template v-else>
              <span class="status-static">{{ statusText[row.status] || row.status }}</span>
            </template>
            <el-divider direction="vertical" />
            <el-popconfirm
              title="确认删除该申请记录？"
              width="220"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer
      v-model="detailDrawer.visible"
      title="申请详情"
      size="520px"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <div v-if="detailDrawer.loading" class="drawer-loading">
        <el-skeleton :rows="8" animated />
      </div>
      <div v-else-if="detailDrawer.data" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请ID">
            {{ detailDrawer.data.id }}
          </el-descriptions-item>
          <el-descriptions-item label="申请类型">
            {{ detailDrawer.data.requestTypeName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ detailDrawer.data.statusName }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ detailDrawer.data.applicantUsername }}（{{ roleText[detailDrawer.data.applicantRole] || detailDrawer.data.applicantRole }}）
          </el-descriptions-item>
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
            <el-descriptions-item label="医生">
              {{ detailDrawer.data.doctorName || `ID:${detailDrawer.data.doctorId}` }}
            </el-descriptions-item>
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
          <el-descriptions-item label="审核状态">
            {{ detailDrawer.data.statusName }}
          </el-descriptions-item>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAllApplicationRequests,
  getApplicationRequestStatistics,
  getApplicationRequestFullDetail,
  getApplicationRequestsByStatus,
  getPendingApplicationRequests,
  reviewApplicationRequest,
  deleteApplicationRequest
} from '@/api/applicationRequest'
import { ChatLineRound } from '@element-plus/icons-vue'

const loading = ref(false)
const statLoading = ref(false)
const requestList = ref([])
const quickStatus = ref('ALL')
const skipStatusChange = ref(false)

const stats = reactive({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0,
  cancelled: 0,
  scheduleChange: 0,
  infoUpdate: 0
})

const statCards = [
  { key: 'total', label: '总申请', badge: 'ALL', tagType: 'primary' },
  { key: 'pending', label: '待审核', badge: '待处理', tagType: 'warning' },
  { key: 'approved', label: '已批准', badge: '已通过', tagType: 'success' },
  { key: 'rejected', label: '已拒绝', badge: '已驳回', tagType: 'danger' }
]

const quickFilters = [
  { label: '全部申请', value: 'ALL' },
  { label: '待审核申请', value: 'PENDING_ONLY' },
  { label: '最近更新', value: 'RECENT' }
]

const filters = reactive({
  requestType: '',
  status: '',
  dateRange: [],
  keyword: ''
})

const requestTypeOptions = [
  { label: '调班申请', value: 'SCHEDULE_CHANGE' },
  { label: '信息修改申请', value: 'INFO_UPDATE' }
]

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' }
]

const requestTypeText = {
  SCHEDULE_CHANGE: '调班申请',
  INFO_UPDATE: '信息修改申请'
}

const requestTypeTagType = {
  SCHEDULE_CHANGE: 'warning',
  INFO_UPDATE: 'info'
}

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

const roleText = {
  DOCTOR: '医生',
  ADMIN: '管理员'
}

const detailDrawer = reactive({
  visible: false,
  loading: false,
  data: null
})

const filteredRequests = computed(() => {
  return requestList.value
    .filter((item) => {
      if (filters.requestType && item.requestType !== filters.requestType) {
        return false
      }
      if (filters.keyword) {
        const keyword = filters.keyword.toLowerCase()
        const combined =
          `${item.applicantUsername || ''} ${item.reason || ''} ${item.fieldName || ''} ${
            item.changeType || ''
          }`.toLowerCase()
        if (!combined.includes(keyword)) return false
      }
      if (filters.dateRange?.length === 2) {
        const [start, end] = filters.dateRange
        const created = item.createdAt?.slice(0, 10)
        if (start && created < start) return false
        if (end && created > end) return false
      }
      return true
    })
    .sort((a, b) => {
      if (quickStatus.value === 'RECENT') {
        return new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0)
      }
      return new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
    })
})

const loadStats = async () => {
  try {
    statLoading.value = true
    const resp = await getApplicationRequestStatistics()
    Object.assign(stats, resp?.data || {})
  } catch (error) {
    console.error('获取申请统计失败', error)
    ElMessage.error(error?.response?.data?.msg || '获取统计失败')
  } finally {
    statLoading.value = false
  }
}

const loadRequests = async (mode = 'all', customStatus = '') => {
  try {
    loading.value = true
    let resp
    if (mode === 'pending') {
      resp = await getPendingApplicationRequests()
    } else if (mode === 'status') {
      const statusValue = customStatus || filters.status
      if (statusValue) {
        resp = await getApplicationRequestsByStatus(statusValue)
      } else {
        resp = await getAllApplicationRequests()
      }
    } else {
      resp = await getAllApplicationRequests()
    }
    requestList.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    console.error('获取申请列表失败', error)
    ElMessage.error(error?.response?.data?.msg || '获取申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuickStatus = async (value) => {
  if (quickStatus.value === value) return
  quickStatus.value = value
  if (value === 'PENDING_ONLY') {
    skipStatusChange.value = true
    filters.status = 'PENDING'
    await loadRequests('pending')
    skipStatusChange.value = false
  } else {
    skipStatusChange.value = true
    filters.status = ''
    await loadRequests('all')
    skipStatusChange.value = false
  }
}

const handleStatusChange = async (value) => {
  if (skipStatusChange.value) return
  if (value) {
    quickStatus.value = 'CUSTOM'
    await loadRequests('status', value)
  } else {
    quickStatus.value = 'ALL'
    await loadRequests('all')
  }
}

const handleLocalFilterChange = () => {
  // 仅依赖 computed 过滤，无需额外请求
}

const handleReset = () => {
  filters.requestType = ''
  skipStatusChange.value = true
  filters.status = ''
  filters.keyword = ''
  filters.dateRange = []
  quickStatus.value = 'ALL'
  skipStatusChange.value = false
  loadRequests('all')
}

const openDetail = async (id) => {
  if (!id) return
  detailDrawer.visible = true
  detailDrawer.loading = true
  detailDrawer.data = null
  try {
    const resp = await getApplicationRequestFullDetail(id)
    detailDrawer.data = resp?.data || null
  } catch (error) {
    console.error('获取申请详情失败', error)
    ElMessage.error(error?.response?.data?.msg || '获取详情失败')
  } finally {
    detailDrawer.loading = false
  }
}

const getCurrentLoadMode = () => {
  if (filters.status) return 'status'
  if (quickStatus.value === 'PENDING_ONLY') return 'pending'
  return 'all'
}

const handleReview = async (id, action) => {
  if (!id) return
  const payload = {
    requestId: id,
    action
  }
  try {
    if (action === 'APPROVED') {
      await ElMessageBox.confirm('确认批准该申请？', '审核确认', {
        type: 'warning',
        confirmButtonText: '批准',
        cancelButtonText: '取消'
      })
    } else {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
        confirmButtonText: '拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (val) => {
          if (!val || !val.trim()) return '拒绝原因不能为空'
          return true
        }
      })
      payload.rejectReason = value.trim()
    }

    await reviewApplicationRequest(payload)
    ElMessage.success(action === 'APPROVED' ? '已批准申请' : '已拒绝申请')
    const mode = getCurrentLoadMode()
    await Promise.all([loadRequests(mode), loadStats()])
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('审核申请失败', error)
    ElMessage.error(error?.response?.data?.msg || '审核失败')
  }
}

const handleDelete = async (id) => {
  if (!id) return
  try {
    await deleteApplicationRequest(id)
    ElMessage.success('删除成功')
    const mode = getCurrentLoadMode()
    await Promise.all([loadRequests(mode), loadStats()])
  } catch (error) {
    console.error('删除申请失败', error)
    ElMessage.error(error?.response?.data?.msg || '删除失败')
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
  await Promise.all([loadStats(), loadRequests()])
})
</script>

<style scoped>
.application-requests-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-row {
  margin-bottom: 4px;
}

.stat-card__content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card__label {
  font-size: 14px;
  color: #909399;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  align-items: center;
}

.quick-buttons {
  margin-left: auto;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header__left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.count-tag {
  font-weight: normal;
}

.applicant-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.applicant-name {
  font-weight: 600;
  color: #303133;
}

.applicant-role {
  color: #909399;
  font-size: 12px;
}

.reason-text {
  margin-top: 6px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.reason-icon {
  color: #909399;
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
</style>

