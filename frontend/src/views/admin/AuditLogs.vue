<template>
  <div class="audit-logs-page">
    <!-- 页面头部 -->
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">审计日志</h1>
        <p class="header-subtitle">系统操作记录与追踪</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="loadAuditLogs" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filters" label-width="80px" class="filter-form">
        <!-- 第一行：用户角色、操作人、操作类型、操作模块 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="用户角色">
              <el-select
                v-model="filters.userRole"
                placeholder="全部"
                clearable
                style="width: 100%"
              >
                <el-option label="患者" value="patient" />
                <el-option label="医生" value="doctor" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作人">
              <el-input
                v-model="filters.username"
                placeholder="请输入用户名"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作类型">
              <el-select
                v-model="filters.operationType"
                placeholder="全部"
                clearable
                style="width: 100%"
              >
                <el-option label="创建" value="CREATE" />
                <el-option label="更新" value="UPDATE" />
                <el-option label="删除" value="DELETE" />
                <el-option label="审批" value="APPROVE" />
                <el-option label="拒绝" value="REJECT" />
                <el-option label="查询" value="QUERY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作模块">
              <el-select
                v-model="filters.operationModule"
                placeholder="全部"
                clearable
                style="width: 100%"
              >
                <el-option label="排班" value="SCHEDULE" />
                <el-option label="预约" value="APPOINTMENT" />
                <el-option label="候补" value="WAITLIST" />
                <el-option label="审计" value="AUDIT" />
                <el-option label="用户" value="USER" />
                <el-option label="医生" value="DOCTOR" />
                <el-option label="科室" value="DEPARTMENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：时间范围、操作状态、按钮 -->
        <el-row :gutter="20" class="second-row">
          <el-col :span="12">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="操作状态">
              <el-select
                v-model="filters.status"
                placeholder="全部"
                clearable
                style="width: 100%"
              >
                <el-option label="成功" value="SUCCESS" />
                <el-option label="失败" value="FAILURE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label=" " class="button-form-item">
              <div class="button-group">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleReset">重置</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="auditLogs"
        v-loading="loading"
        stripe
        border
        style="width: 100%; table-layout: fixed;"
        :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }"
        @row-click="handleRowClick"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="createdAt" label="操作时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="userRole" label="用户角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getRoleTagType(row.userRole)">
              {{ getUserRoleName(row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="130">
          <template #default="{ row }">
            {{ row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)">
              {{ getOperationTypeName(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationModule" label="操作模块" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ getOperationModuleName(row.operationModule) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionTime" label="耗时" width="110" sortable="custom" align="center">
          <template #default="{ row }">
            <span :class="getExecutionTimeClass(row.executionTime)">
              {{ row.executionTime }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[20, 50, 100, 200]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="审计日志详情"
      width="900px"
      class="detail-dialog"
    >
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border label-class-name="detail-label">
          <el-descriptions-item label="日志ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatDateTime(detailData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="操作人">
            <div>
              <div>{{ detailData.username || '-' }}</div>
              <el-tag size="small" :type="getRoleTagType(detailData.userRole)">
                {{ getUserRoleName(detailData.userRole) }}
              </el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detailData.userId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTagType(detailData.operationType)">
              {{ getOperationTypeName(detailData.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作模块">
            <el-tag type="info">{{ getOperationModuleName(detailData.operationModule) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作描述" :span="2">
            {{ detailData.operationDesc || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag :type="getMethodTagType(detailData.requestMethod)">
              {{ detailData.requestMethod }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求URL" :span="1">
            <div class="url-text">{{ detailData.requestUrl || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2">
            <pre class="json-text">{{ formatJson(detailData.requestParams) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应状态码">
            <el-tag :type="detailData.responseCode === '200' ? 'success' : 'danger'">
              {{ detailData.responseCode }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应消息">
            {{ detailData.responseMsg || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作状态">
            <el-tag :type="detailData.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ detailData.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时(ms)">
            <span :class="getExecutionTimeClass(detailData.executionTime)">
              {{ detailData.executionTime }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ detailData.ipAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="User-Agent" :span="2">
            <div class="user-agent-text">{{ detailData.userAgent || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getAuditLogs, getAuditLogById } from '@/api/auditLog'

const loading = ref(false)
const auditLogs = ref([])
const detailVisible = ref(false)
const detailData = ref(null)
const timeRange = ref([])

// 排序状态
const sortConfig = reactive({
  prop: 'createdAt',
  order: 'descending' // descending: 降序(最新的在前), ascending: 升序(最旧的在前)
})

// 筛选条件
const filters = reactive({
  operationType: '',
  operationModule: '',
  username: '',
  userRole: '',
  status: '',
  targetType: '',
  targetId: '',
  startTime: '',
  endTime: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
  totalPages: 0
})

// 加载审计日志列表
const loadAuditLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filters
    }
    
    // 处理时间范围
    if (timeRange.value && timeRange.value.length === 2) {
      params.startTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }

    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const res = await getAuditLogs(params)
    const data = res?.data || {}
    let logs = data.list || []
    
    // 前端排序处理
    if (sortConfig.prop && sortConfig.order) {
      logs = sortLogs(logs, sortConfig.prop, sortConfig.order)
    }
    
    auditLogs.value = logs
    pagination.total = data.total || 0
    pagination.totalPages = data.totalPages || 0
  } catch (error) {
    console.error('加载审计日志失败:', error)
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const res = await getAuditLogById(row.id)
    detailData.value = res?.data || row
    detailVisible.value = true
  } catch (error) {
    console.error('加载日志详情失败:', error)
    ElMessage.error('加载日志详情失败')
  }
}

// 行点击事件
const handleRowClick = (row) => {
  viewDetail(row)
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadAuditLogs()
}

// 重置
const handleReset = () => {
  Object.keys(filters).forEach(key => {
    filters[key] = ''
  })
  timeRange.value = []
  pagination.page = 1
  loadAuditLogs()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  loadAuditLogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadAuditLogs()
}

// 排序变化处理
const handleSortChange = ({ prop, order }) => {
  sortConfig.prop = prop
  sortConfig.order = order
  loadAuditLogs()
}

// 排序函数
const sortLogs = (logs, prop, order) => {
  if (!order) return logs
  
  const sortedLogs = [...logs]
  sortedLogs.sort((a, b) => {
    let aValue = a[prop]
    let bValue = b[prop]
    
    // 处理时间字段
    if (prop === 'createdAt') {
      aValue = new Date(aValue).getTime()
      bValue = new Date(bValue).getTime()
    }
    
    // 处理数字字段
    if (prop === 'executionTime') {
      aValue = Number(aValue) || 0
      bValue = Number(bValue) || 0
    }
    
    if (order === 'ascending') {
      return aValue > bValue ? 1 : -1
    } else {
      return aValue < bValue ? 1 : -1
    }
  })
  
  return sortedLogs
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    const date = new Date(dateTimeStr)
    if (isNaN(date.getTime())) return dateTimeStr
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return dateTimeStr
  }
}

// 格式化JSON
const formatJson = (jsonStr) => {
  if (!jsonStr) return '-'
  try {
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 获取操作类型名称
const getOperationTypeName = (type) => {
  const map = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    APPROVE: '审批',
    REJECT: '拒绝',
    QUERY: '查询'
  }
  return map[type] || type
}

// 获取操作类型标签类型
const getOperationTypeTagType = (type) => {
  const map = {
    CREATE: 'success',
    UPDATE: 'primary',
    DELETE: 'danger',
    APPROVE: 'warning',
    REJECT: 'danger',
    QUERY: 'info'
  }
  return map[type] || ''
}

// 获取操作模块名称
const getOperationModuleName = (module) => {
  const map = {
    SCHEDULE: '排班',
    APPOINTMENT: '预约',
    WAITLIST: '候补',
    AUDIT: '审计',
    USER: '用户',
    DOCTOR: '医生',
    DEPARTMENT: '科室'
  }
  return map[module] || module
}

// 获取用户角色名称
const getUserRoleName = (role) => {
  const map = {
    patient: '患者',
    doctor: '医生',
    admin: '管理员'
  }
  return map[role] || role
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  const map = {
    patient: 'info',
    doctor: 'primary',
    admin: 'warning'
  }
  return map[role] || ''
}

// 获取请求方法标签类型
const getMethodTagType = (method) => {
  const map = {
    GET: 'info',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'primary'
  }
  return map[method] || ''
}

// 获取执行时间样式类
const getExecutionTimeClass = (time) => {
  if (!time) return ''
  if (time < 100) return 'execution-time-fast'
  if (time < 500) return 'execution-time-normal'
  return 'execution-time-slow'
}

// 初始化
onMounted(() => {
  loadAuditLogs()
})
</script>

<style scoped>
.audit-logs-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.header-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 600;
  color: #303133;
  text-align: left;
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #606266;
  text-align: left;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  padding: 10px 0;
}

.filter-form .el-row {
  margin-bottom: 0;
}

.filter-form .el-form-item {
  margin-bottom: 18px;
}

.filter-form .second-row {
  align-items: flex-start;
}

.button-form-item {
  width: 100%;
}

.button-form-item :deep(.el-form-item__content) {
  width: 100% !important;
}

.button-group {
  display: flex;
  gap: 10px;
  width: 100%;
}

.button-group .el-button {
  flex: 1;
  min-width: 0;
}

.table-card {
  margin-bottom: 20px;
}

.table-card :deep(.el-table) {
  table-layout: fixed !important;
}

.table-card :deep(.el-table__header-wrapper) {
  overflow: hidden;
}

.table-card :deep(.el-table__body-wrapper) {
  overflow-x: hidden;
  overflow-y: auto;
}

.table-card :deep(.el-table__header) {
  width: 100% !important;
}

.table-card :deep(.el-table__body) {
  width: 100% !important;
}

.table-card :deep(.el-table th),
.table-card :deep(.el-table td) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 修复表头偏移问题 */
.table-card :deep(.el-table__header-wrapper .el-table__header) {
  margin-right: 0 !important;
}

.table-card :deep(.el-table__body-wrapper .el-table__body) {
  margin-right: 0 !important;
}

/* 移除滚动条gutter */
.table-card :deep(.el-table__body-wrapper) {
  scrollbar-gutter: auto !important;
}

.table-card :deep(.el-table--scrollable-x .el-table__body-wrapper) {
  overflow-x: hidden !important;
}

.table-card :deep(.el-table--border::after),
.table-card :deep(.el-table--group::after),
.table-card :deep(.el-table::before) {
  background-color: #ebeef5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-content :deep(.detail-label) {
  white-space: nowrap;
  min-width: 120px;
  width: 120px;
}

.detail-content :deep(.el-descriptions__label) {
  white-space: nowrap !important;
}

.url-text {
  word-break: break-all;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
}

.json-text {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.user-agent-text {
  word-break: break-all;
  font-size: 12px;
  color: #606266;
}

.execution-time-fast {
  color: #67c23a;
  font-weight: 500;
}

.execution-time-normal {
  color: #e6a23c;
  font-weight: 500;
}

.execution-time-slow {
  color: #f56c6c;
  font-weight: 500;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>

