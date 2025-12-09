<template>
  <div class="whitelist-page">
    <el-card class="search-card">
      <el-row :gutter="12">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索身份证号/学号"
            clearable
            @input="handleKeywordFilter"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.roleType" placeholder="角色类型" clearable @change="handleRoleChange" style="width: 100%">
            <el-option label="全部角色" value="" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="外部人员" value="outsider" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 100%">
            <el-option label="全部状态" value="" />
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button style="width: 100%" @click="resetFilters">重置</el-button>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="openAddDialog" style="width: 100%">
            新增
          </el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="success" @click="openImportDialog" style="width: 100%">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 批量操作卡片 -->
    <el-card v-if="selectedItems.length > 0" class="batch-card">
      <div class="batch-operations">
        <span class="selected-info">已选择 {{ selectedItems.length }} 条记录</span>
        <div class="batch-buttons">
          <el-button
            type="danger"
            size="small"
            @click="handleBatchDelete"
            :disabled="selectedItems.length === 0"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>白名单列表</span>
          <el-tag type="info">共 {{ whitelist.length }} 条</el-tag>
        </div>
      </template>
      <el-table 
        :data="filteredWhitelist" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="whitelist-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="identityNumber" label="学工号/证件号" width="180" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            {{ roleTypeText(row.roleType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewWhitelist(row)">查看</el-button>
            <el-button type="warning" size="small" @click="editWhitelist(row)">编辑</el-button>
            <el-button 
              :type="row.status === 'active' ? 'info' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!filteredWhitelist.length && !loading" class="empty-state">
        <el-empty description="暂无白名单数据" />
      </div>
    </el-card>

    <el-dialog 
      v-model="formDialog.visible" 
      :title="formDialog.isEdit ? '编辑白名单' : '新增白名单'" 
      width="600px"
      :align-center="false"
      class="whitelist-dialog"
    >
      <div class="whitelist-form-wrap">
        <el-form :model="formModel" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="学号/工号" prop="identityNumber">
          <el-input v-model="formModel.identityNumber" placeholder="请输入学/工号或证件号" />
        </el-form-item>
        <el-form-item label="角色类型" prop="roleType">
          <el-select v-model="formModel.roleType" placeholder="请选择角色类型" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="外部人员" value="outsider" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formModel.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="formDialog.loading" @click="submitForm">
            {{ formDialog.isEdit ? '更新' : '提交' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" title="白名单详情" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ selectedItem?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学工号">{{ selectedItem?.identityNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleTypeText(selectedItem?.roleType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(selectedItem?.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(selectedItem?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(selectedItem?.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="editFromDetail">编辑</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="importDialog.visible"
      title="批量导入白名单"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="import-content">
        <!-- 使用说明卡片 -->
        <el-card class="usage-card" shadow="never">
          <template #header>
            <div class="card-header-title">
              <el-icon class="header-icon" color="#409EFF"><InfoFilled /></el-icon>
              <span>使用说明</span>
            </div>
          </template>
          
          <div class="usage-content">
            <div class="usage-item">
              <el-icon class="usage-icon" color="#409EFF"><Document /></el-icon>
              <span class="usage-text">下载模板文件，按照格式填写白名单信息</span>
            </div>
            <div class="usage-item">
              <el-icon class="usage-icon" color="#409EFF"><Document /></el-icon>
              <span class="usage-text">支持 Excel (.xlsx) 和 CSV (.csv) 格式</span>
            </div>
            <div class="usage-item">
              <el-icon class="usage-icon" color="#67C23A"><CircleCheck /></el-icon>
              <span class="usage-text">学工号/证件号为必填项，不能重复</span>
            </div>
            <div class="usage-item">
              <el-icon class="usage-icon" color="#409EFF"><User /></el-icon>
              <span class="usage-text">角色类型：student/学生、teacher/教师、outsider/外部人员（支持中英文）</span>
            </div>
            <div class="usage-item">
              <el-icon class="usage-icon" color="#409EFF"><Notebook /></el-icon>
              <span class="usage-text">状态：active/启用、inactive/停用（支持中英文，默认为启用）</span>
            </div>
          </div>
        </el-card>

        <!-- 模板下载按钮 -->
        <div class="template-actions">
          <el-button 
            type="primary" 
            size="large"
            @click="downloadTemplate('excel')"
            class="download-btn excel-btn"
          >
            <el-icon><Download /></el-icon>
            下载Excel模板
          </el-button>
          <el-button 
            type="success" 
            size="large"
            @click="downloadTemplate('csv')"
            class="download-btn csv-btn"
          >
            <el-icon><Download /></el-icon>
            下载CSV模板
          </el-button>
        </div>

        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls,.csv"
          :file-list="importDialog.fileList"
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .xlsx、.xls、.csv 格式，文件大小不超过 5MB
            </div>
          </template>
        </el-upload>

        <div v-if="importDialog.previewData.length > 0" class="preview-section">
          <el-divider>数据预览（前10条）</el-divider>
          <el-table :data="importDialog.previewData" border max-height="300">
            <el-table-column prop="identityNumber" label="学工号/证件号" width="150" />
            <el-table-column prop="roleType" label="角色类型" width="120">
              <template #default="{ row }">
                {{ roleTypeText(row.roleType) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="验证结果" min-width="150">
              <template #default="{ row }">
                <el-tag v-if="row.valid" type="success" size="small">有效</el-tag>
                <el-tag v-else type="danger" size="small">{{ row.error }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="preview-summary">
            <span>总计：{{ importDialog.totalCount }} 条</span>
            <span style="margin-left: 20px; color: #67c23a">有效：{{ importDialog.validCount }} 条</span>
            <span style="margin-left: 20px; color: #f56c6c">无效：{{ importDialog.invalidCount }} 条</span>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeImportDialog">取消</el-button>
          <el-button 
            type="primary" 
            :loading="importDialog.loading"
            :disabled="importDialog.validCount === 0"
            @click="handleImport"
          >
            确认导入（{{ importDialog.validCount }} 条）
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Upload, Download, InfoFilled, Document, CircleCheck, User, Notebook } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import {
  getWhitelistList,
  getWhitelistById,
  getWhitelistByRoleType,
  createWhitelist,
  updateWhitelist,
  deleteWhitelist,
  batchDeleteWhitelist,
  toggleWhitelistStatus
} from '@/api/whitelist'

const loading = ref(false)
const searchLoading = ref(false)
const whitelist = ref([])

const searchForm = reactive({
  keyword: '',
  roleType: '',
  status: ''
})

const filteredWhitelist = computed(() => {
  let result = whitelist.value
  if (searchForm.keyword) {
    const k = searchForm.keyword.trim().toLowerCase()
    result = result.filter((item) => item.identityNumber?.toLowerCase().includes(k))
  }
  if (searchForm.status) {
    result = result.filter((item) => item.status === searchForm.status)
  }
  return result
})

const formDialog = reactive({ visible: false, isEdit: false, loading: false })
const detailDialog = reactive({ visible: false })
const formRef = ref()
const formModel = reactive({
  id: null,
  identityNumber: '',
  roleType: '',
  status: 'active'
})
const selectedItem = ref(null)
const selectedItems = ref([])
const uploadRef = ref()

const importDialog = reactive({
  visible: false,
  loading: false,
  fileList: [],
  previewData: [],
  allData: [],
  totalCount: 0,
  validCount: 0,
  invalidCount: 0
})

const formRules = {
  identityNumber: [{ required: true, message: '请输入学工号', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadWhitelist = async (roleType = '') => {
  try {
    loading.value = true
    let resp
    if (roleType) {
      resp = await getWhitelistByRoleType(roleType)
    } else {
      resp = await getWhitelistList()
    }
    whitelist.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    whitelist.value = []
    ElMessage.error(error?.response?.data?.msg || '获取白名单失败')
  } finally {
    loading.value = false
  }
}

const handleKeywordFilter = () => {
  if (!searchForm.keyword && !searchForm.roleType) {
    loadWhitelist()
  }
}

const handleRoleChange = async () => {
  await loadWhitelist(searchForm.roleType || '')
}


const resetFilters = async () => {
  searchForm.keyword = ''
  searchForm.roleType = ''
  searchForm.status = ''
  await loadWhitelist()
}

const openAddDialog = () => {
  formModel.id = null
  formModel.identityNumber = ''
  formModel.roleType = ''
  formModel.status = 'active'
  formDialog.isEdit = false
  formDialog.visible = true
}

const editWhitelist = (row) => {
  formModel.id = row.id
  formModel.identityNumber = row.identityNumber
  formModel.roleType = row.roleType
  formModel.status = row.status
  formDialog.isEdit = true
  formDialog.visible = true
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    formDialog.loading = true
    const payload = {
      identityNumber: formModel.identityNumber,
      roleType: formModel.roleType,
      status: formModel.status
    }
    if (formDialog.isEdit) {
      await updateWhitelist(formModel.id, payload)
      ElMessage.success('白名单更新成功')
    } else {
      await createWhitelist(payload)
      ElMessage.success('白名单添加成功')
    }
    formDialog.visible = false
    await loadWhitelist(searchForm.roleType || '')
  } catch (error) {
    if (error?.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    } else if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    formDialog.loading = false
  }
}

const viewWhitelist = async (row) => {
  try {
    const resp = await getWhitelistById(row.id)
    selectedItem.value = resp?.data || row
    detailDialog.visible = true
  } catch (error) {
    selectedItem.value = row
    detailDialog.visible = true
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除白名单记录 ${row.identityNumber} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteWhitelist(row.id)
    ElMessage.success('删除成功')
    await loadWhitelist(searchForm.roleType || '')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.msg || '删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.value.length} 条记录吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const ids = selectedItems.value.map(item => item.id)
    await batchDeleteWhitelist(ids)
    ElMessage.success('批量删除成功')
    selectedItems.value = []
    await loadWhitelist(searchForm.roleType || '')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.msg || '批量删除失败')
    }
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 'active' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}白名单记录 ${row.identityNumber} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await toggleWhitelistStatus(row.id)
    ElMessage.success(`${action}成功`)
    await loadWhitelist(searchForm.roleType || '')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.msg || `${action}失败`)
    }
  }
}

const editFromDetail = () => {
  detailDialog.visible = false
  editWhitelist(selectedItem.value)
}

const openImportDialog = () => {
  importDialog.visible = true
  importDialog.fileList = []
  importDialog.previewData = []
  importDialog.allData = []
  importDialog.totalCount = 0
  importDialog.validCount = 0
  importDialog.invalidCount = 0
}

const closeImportDialog = () => {
  importDialog.visible = false
  importDialog.fileList = []
  importDialog.previewData = []
  importDialog.allData = []
}

const downloadTemplate = (type) => {
  // 创建模板数据
  const templateData = [
    { '学工号/证件号': '2021001001', '角色类型': 'student', '状态': 'active' },
    { '学工号/证件号': '2021001002', '角色类型': 'teacher', '状态': 'active' },
    { '学工号/证件号': '2021001003', '角色类型': 'outsider', '状态': 'inactive' },
    { '学工号/证件号': '示例：学生', '角色类型': '学生', '状态': '启用' },
    { '学工号/证件号': '示例：教师', '角色类型': '教师', '状态': '停用' }
  ]
  
  // 创建工作簿
  const ws = XLSX.utils.json_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '白名单模板')
  
  // 根据类型下载不同格式
  if (type === 'csv') {
    XLSX.writeFile(wb, '白名单导入模板.csv', { bookType: 'csv' })
    ElMessage.success('CSV模板下载成功')
  } else {
    XLSX.writeFile(wb, '白名单导入模板.xlsx')
    ElMessage.success('Excel模板下载成功')
  }
}

const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
      const jsonData = XLSX.utils.sheet_to_json(firstSheet)
      
      // 中英文转换映射
      const roleTypeMap = {
        'student': 'student',
        'teacher': 'teacher',
        'outsider': 'outsider',
        '学生': 'student',
        '教师': 'teacher',
        '外部人员': 'outsider'
      }
      
      const statusMap = {
        'active': 'active',
        'inactive': 'inactive',
        '启用': 'active',
        '停用': 'inactive'
      }
      
      // 验证和转换数据
      const processedData = jsonData.map((row, index) => {
        // 获取原始值
        let rawRoleType = row['角色类型'] || row['roleType'] || ''
        let rawStatus = row['状态'] || row['status'] || 'active'
        
        // 转换为英文（数据库存储格式）
        const convertedRoleType = roleTypeMap[rawRoleType] || rawRoleType
        const convertedStatus = statusMap[rawStatus] || rawStatus
        
        const item = {
          identityNumber: row['学工号/证件号'] || row['identityNumber'] || '',
          roleType: convertedRoleType,
          status: convertedStatus,
          valid: true,
          error: ''
        }
        
        // 验证必填项
        if (!item.identityNumber || item.identityNumber.includes('示例')) {
          item.valid = false
          item.error = '学工号不能为空'
        } else if (!rawRoleType) {
          item.valid = false
          item.error = '角色类型不能为空'
        } else if (!['student', 'teacher', 'outsider'].includes(item.roleType)) {
          item.valid = false
          item.error = '角色类型无效（应为：student/学生、teacher/教师、outsider/外部人员）'
        } else if (!['active', 'inactive'].includes(item.status)) {
          item.valid = false
          item.error = '状态无效（应为：active/启用、inactive/停用）'
        }
        
        return item
      })
      
      // 检查重复
      const identityNumbers = new Set()
      processedData.forEach(item => {
        if (item.valid && identityNumbers.has(item.identityNumber)) {
          item.valid = false
          item.error = '学工号重复'
        }
        identityNumbers.add(item.identityNumber)
      })
      
      importDialog.allData = processedData
      importDialog.previewData = processedData.slice(0, 10)
      importDialog.totalCount = processedData.length
      importDialog.validCount = processedData.filter(item => item.valid).length
      importDialog.invalidCount = processedData.filter(item => !item.valid).length
      
      ElMessage.success(`文件解析成功，共 ${importDialog.totalCount} 条数据`)
    } catch (error) {
      ElMessage.error('文件解析失败：' + error.message)
    }
  }
  reader.readAsArrayBuffer(file.raw)
}

const handleImport = async () => {
  try {
    importDialog.loading = true
    const validData = importDialog.allData.filter(item => item.valid)
    
    let successCount = 0
    let failCount = 0
    
    for (const item of validData) {
      try {
        await createWhitelist({
          identityNumber: item.identityNumber,
          roleType: item.roleType,
          status: item.status
        })
        successCount++
      } catch (error) {
        failCount++
        console.error(`导入失败: ${item.identityNumber}`, error)
      }
    }
    
    ElMessage.success(`导入完成！成功 ${successCount} 条，失败 ${failCount} 条`)
    closeImportDialog()
    await loadWhitelist()
  } catch (error) {
    ElMessage.error('导入失败：' + (error?.response?.data?.msg || error.message))
  } finally {
    importDialog.loading = false
  }
}

watch(
  () => searchForm.status,
  () => {
    // status 只做前端过滤，无需重新请求
  }
)

onMounted(() => {
  loadWhitelist()
})

const roleTypeText = (val) => {
  const map = { student: '学生', teacher: '教师', outsider: '外部人员' }
  return map[val] || val || '-'
}

const statusText = (val) => {
  const map = { active: '启用', inactive: '停用' }
  return map[val] || val || '-'
}

const formatDateTime = (val) => {
  if (!val) return '-'
  return String(val).replace('T', ' ').replace('Z', '')
}
</script>

<style scoped>
.whitelist-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  margin-bottom: 8px;
}

.batch-card {
  margin-bottom: 8px;
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.batch-operations {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-info {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

.batch-buttons {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  margin-top: 20px;
}

/* 表格样式 */
.whitelist-table {
  width: 100%;
}

.whitelist-table :deep(.el-table__inner-wrapper) {
  width: 100% !important;
}

.whitelist-table :deep(.el-table__header-wrapper) {
  overflow: visible !important;
}

.whitelist-table :deep(.el-table__body-wrapper) {
  overflow-x: hidden !important;
  overflow-y: auto !important;
}

.whitelist-table :deep(.el-table__header) {
  width: 100% !important;
  table-layout: fixed !important;
}

.whitelist-table :deep(.el-table__body) {
  width: 100% !important;
  table-layout: fixed !important;
}

/* 移除滚动条gutter */
.whitelist-table :deep(.el-table__body-wrapper) {
  scrollbar-gutter: auto !important;
}

.whitelist-table :deep(.el-scrollbar__wrap) {
  overflow-x: hidden !important;
}

/* 确保表头不偏移 */
.whitelist-table :deep(.el-table--scrollable-x .el-table__body-wrapper) {
  overflow-x: hidden !important;
}

.whitelist-table :deep(.el-table__header-wrapper .el-table__header) {
  margin-right: 0 !important;
}

.whitelist-table :deep(.el-table__body-wrapper .el-table__body) {
  margin-right: 0 !important;
}

/* 白名单表单整体左移 */
.whitelist-form-wrap {
  margin-left: -30px;
}

@media (max-width: 768px) {
  .whitelist-form-wrap {
    margin-left: -6px;
  }
}

/* 导入对话框样式 */
.import-content {
  padding: 10px 0;
}

/* 使用说明卡片样式 */
.usage-card {
  margin-bottom: 24px;
  border: 1px solid #e4e7ed;
}

.usage-card :deep(.el-card__header) {
  background-color: #f5f7fa;
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.header-icon {
  font-size: 18px;
}

.usage-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}

.usage-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.usage-icon {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.usage-text {
  flex: 1;
  text-align: left;
}

/* 模板下载按钮样式 */
.template-actions {
  margin-bottom: 30px;
  display: flex;
  gap: 16px;
}

.download-btn {
  flex: 1;
  height: 48px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
}

.excel-btn {
  background: #4a7afe;
  border-color: #4a7afe;
}

.excel-btn:hover {
  background: #3a6aee;
  border-color: #3a6aee;
}

.csv-btn {
  background: #22c55e;
  border-color: #22c55e;
}

.csv-btn:hover {
  background: #16a34a;
  border-color: #16a34a;
}

.upload-demo {
  margin: 20px 0;
}

.preview-section {
  margin-top: 20px;
}

.preview-summary {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
}
</style>

