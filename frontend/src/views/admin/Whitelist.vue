<template>
  <div class="whitelist-page">
    <el-card class="search-card">
      <el-row :gutter="16">
        <el-col :span="8">
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
        <el-col :span="6">
          <el-select v-model="searchForm.roleType" placeholder="角色类型" clearable @change="handleRoleChange" style="width: 100%">
            <el-option label="全部角色" value="" />
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="外部人员" value="outsider" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 100%">
            <el-option label="全部状态" value="" />
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button style="width: 100%" @click="resetFilters">重置</el-button>
        </el-col>
      </el-row>
      <el-row :gutter="16" style="margin-top: 12px">
        <el-col :span="8">
          <el-input
            v-model="searchForm.whitelistId"
            placeholder="输入白名单ID精确查询"
            clearable
            @keyup.enter="handleSearchById"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" :loading="searchLoading" @click="handleSearchById" style="width: 100%">
            ID 查询
          </el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="openAddDialog" style="width: 100%">
            新增白名单
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>白名单列表</span>
          <el-tag type="info">共 {{ whitelist.length }} 条</el-tag>
        </div>
      </template>
      <el-table :data="filteredWhitelist" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="identityNumber" label="学工号/证件号" min-width="160" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            {{ roleTypeText(row.roleType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewWhitelist(row)">查看</el-button>
            <el-button type="warning" size="small" @click="editWhitelist(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!filteredWhitelist.length && !loading" class="empty-state">
        <el-empty description="暂无白名单数据" />
      </div>
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑白名单' : '新增白名单'" width="500px">
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
        <el-descriptions-item label="创建时间">{{ selectedItem?.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ selectedItem?.updatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getWhitelistList,
  getWhitelistById,
  getWhitelistByRoleType,
  createWhitelist,
  updateWhitelist,
  deleteWhitelist
} from '@/api/whitelist'

const loading = ref(false)
const searchLoading = ref(false)
const whitelist = ref([])

const searchForm = reactive({
  keyword: '',
  roleType: '',
  status: '',
  whitelistId: ''
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

const handleSearchById = async () => {
  const id = String(searchForm.whitelistId || '').trim()
  if (!id) {
    await loadWhitelist(searchForm.roleType || '')
    return
  }
  if (!/^\d+$/.test(id)) {
    ElMessage.warning('白名单ID需为数字')
    return
  }
  try {
    searchLoading.value = true
    const resp = await getWhitelistById(id)
    if (resp?.data) {
      whitelist.value = [resp.data]
      ElMessage.success(`已找到白名单 ID ${id}`)
    } else {
      whitelist.value = []
      ElMessage.warning('未找到对应的白名单记录')
    }
  } catch (error) {
    whitelist.value = []
    ElMessage.error(error?.response?.data?.msg || '查询失败')
  } finally {
    searchLoading.value = false
  }
}

const resetFilters = async () => {
  searchForm.keyword = ''
  searchForm.roleType = ''
  searchForm.status = ''
  searchForm.whitelistId = ''
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  margin-top: 20px;
}
</style>

