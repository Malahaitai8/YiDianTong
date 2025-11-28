<template>
  <div class="admin-users-page">
    <el-card class="search-card">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索管理员姓名/角色"
            clearable
            @input="handleKeywordChange"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchForm.adminId"
            placeholder="输入管理员ID精确查询"
            clearable
            @keyup.enter="handleSearchById"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" :loading="searchLoading" @click="handleSearchById" style="width: 100%">
            ID 查询
          </el-button>
        </el-col>
        <el-col :span="3">
          <el-button style="width: 100%" @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <span>管理员列表</span>
            <el-tag type="info" style="margin-left: 10px;">共 {{ admins.length }} 人</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="filteredAdmins" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="管理员ID" width="120" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="name" label="姓名" min-width="160" />
        <el-table-column prop="adminRole" label="管理员角色" min-width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewAdmin(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!filteredAdmins.length && !loading" class="empty-state">
        <el-empty description="暂无管理员数据" />
      </div>
    </el-card>

    <el-dialog v-model="viewDialog.visible" title="管理员详情" width="460px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="管理员ID">{{ selectedAdmin?.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联用户ID">{{ selectedAdmin?.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ selectedAdmin?.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管理员角色">{{ selectedAdmin?.adminRole || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialog.visible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdminList, getAdminById } from '@/api/adminUser'

const loading = ref(false)
const searchLoading = ref(false)
const admins = ref([])

const searchForm = reactive({
  keyword: '',
  adminId: ''
})

const filteredAdmins = computed(() => {
  if (!searchForm.keyword) return admins.value
  const k = searchForm.keyword.trim().toLowerCase()
  return admins.value.filter((admin) => {
    const name = admin.name?.toLowerCase() || ''
    const role = admin.adminRole?.toLowerCase() || ''
    const userId = String(admin.userId || '').toLowerCase()
    return name.includes(k) || role.includes(k) || userId.includes(k)
  })
})

const viewDialog = reactive({ visible: false })
const selectedAdmin = ref(null)

const loadAdmins = async () => {
  try {
    loading.value = true
    const resp = await getAdminList()
    admins.value = Array.isArray(resp?.data) ? resp.data : []
  } catch (error) {
    admins.value = []
    ElMessage.error(error?.response?.data?.msg || '获取管理员列表失败')
  } finally {
    loading.value = false
  }
}

const handleKeywordChange = () => {}

const handleSearchById = async () => {
  const id = String(searchForm.adminId || '').trim()
  if (!id) {
    await loadAdmins()
    return
  }
  if (!/^\d+$/.test(id)) {
    ElMessage.warning('管理员ID需为数字')
    return
  }
  try {
    searchLoading.value = true
    const resp = await getAdminById(id)
    if (resp?.data) {
      admins.value = [resp.data]
      ElMessage.success(`已获取管理员 ID ${id}`)
    } else {
      admins.value = []
      ElMessage.warning('未找到对应的管理员')
    }
  } catch (error) {
    admins.value = []
    ElMessage.error(error?.response?.data?.msg || '查询失败')
  } finally {
    searchLoading.value = false
  }
}

const resetSearch = async () => {
  searchForm.keyword = ''
  searchForm.adminId = ''
  await loadAdmins()
}

const viewAdmin = async (row) => {
  try {
    const resp = await getAdminById(row.id)
    selectedAdmin.value = resp?.data || row
    viewDialog.visible = true
  } catch (error) {
    selectedAdmin.value = row
    viewDialog.visible = true
  }
}

onMounted(loadAdmins)
</script>

<style scoped>
.admin-users-page {
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
  margin-top: 16px;
}
</style>

