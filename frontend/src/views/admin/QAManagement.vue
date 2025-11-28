<template>
  <div class="qa-management">
    <el-card class="page-header-card">
      <template #header>
        <div class="header-meta">
          <div>
            <h2>FAQ 知识库管理</h2>
            <p>维护问答助手的知识库、批量导入导出以及高频问题统计</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增 FAQ</el-button>
            <el-button :icon="Upload" @click="openImportDialog">批量导入</el-button>
            <el-button :icon="Download" @click="handleExport">导出 JSON</el-button>
          </div>
        </div>
      </template>
      <div class="metrics">
        <div class="metric-item">
          <span class="metric-label">FAQ 总数</span>
          <span class="metric-value">{{ faqList.length }}</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">检索结果</span>
          <span class="metric-value">{{ filteredFaqs.length }}</span>
        </div>
        <div class="metric-item">
          <span class="metric-label">统计条目</span>
          <span class="metric-value">{{ statsEntries.length }}</span>
        </div>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>FAQ 列表</span>
          <div class="card-actions">
            <el-input
              v-model="searchKeyword"
              prefix-icon="Search"
              placeholder="搜索问题或答案关键字"
              clearable
              class="search-input"
            />
          </div>
        </div>
      </template>

      <el-table
        :data="pagedFaqs"
        v-loading="loading"
        style="width: 100%"
        empty-text="暂无 FAQ"
      >
        <el-table-column type="index" width="70" label="#" :index="calcTableIndex" />
        <el-table-column label="问题" min-width="240">
          <template #default="{ row }">
            <div class="question-text">{{ row.question || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="答案" min-width="360">
          <template #default="{ row }">
            <div class="answer-text">{{ row.answer || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="filteredFaqs.length"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>高频问题统计 (qa.stats)</span>
          <div class="card-actions">
            <el-button text type="primary" @click="addStatsRow">新增统计项</el-button>
            <el-button text type="info" @click="handleStatsReset">清空列表</el-button>
            <el-button type="primary" @click="saveStats" :loading="statsSaving">保存统计</el-button>
          </div>
        </div>
      </template>

      <el-table :data="statsEntries" v-loading="statsLoading" style="width: 100%">
        <el-table-column label="问题" min-width="320">
          <template #default="{ row, $index }">
            <el-input
              v-model="row.question"
              placeholder="请输入问题"
              :maxlength="200"
              @change="markStatsDirty($index)"
            />
          </template>
        </el-table-column>
        <el-table-column label="计数" width="180">
          <template #default="{ row, $index }">
            <el-input-number
              v-model="row.count"
              :min="0"
              :max="9999"
              @change="markStatsDirty($index)"
            />
          </template>
        </el-table-column>
        <el-table-column width="120" label="操作">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeStatsRow($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="faqDialog.visible"
      :title="faqDialog.isEdit ? '编辑 FAQ' : '新增 FAQ'"
      width="540px"
      :close-on-click-modal="false"
    >
      <el-form :model="faqDialog.form" label-width="80px">
        <el-form-item label="问题">
          <el-input
            v-model="faqDialog.form.question"
            type="textarea"
            :rows="3"
            placeholder="请输入问题"
          />
        </el-form-item>
        <el-form-item label="答案">
          <el-input
            v-model="faqDialog.form.answer"
            type="textarea"
            :rows="5"
            placeholder="请输入答案"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="faqDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="faqDialog.loading" @click="submitFaq">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importDialog.visible"
      title="批量导入 FAQ"
      width="640px"
      :close-on-click-modal="false"
    >
      <p class="import-tip">
        请粘贴 JSON 数组，格式示例：
        <code>[{"question":"如何预约挂号？","answer":"..."}]</code>
      </p>
      <el-input
        v-model="importDialog.content"
        type="textarea"
        :rows="12"
        placeholder='[{"question":"...","answer":"..."}]'
      />
      <template #footer>
        <el-button @click="importDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="importDialog.loading" @click="handleImportSubmit">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download } from '@element-plus/icons-vue'
import {
  getFaqList,
  createFaq,
  updateFaq,
  deleteFaq,
  importFaqs,
  exportFaqs,
  getFaqStats,
  updateFaqStats
} from '@/api/qa'

const loading = ref(false)
const faqList = ref([])
const searchKeyword = ref('')
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const filteredFaqs = computed(() => {
  if (!searchKeyword.value.trim()) return faqList.value
  const keyword = searchKeyword.value.trim().toLowerCase()
  return faqList.value.filter((item) => {
    const q = item.question?.toLowerCase() || ''
    const a = item.answer?.toLowerCase() || ''
    return q.includes(keyword) || a.includes(keyword)
  })
})

const pagedFaqs = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredFaqs.value.slice(start, end)
})

const calcTableIndex = (index) => {
  return (pagination.currentPage - 1) * pagination.pageSize + index + 1
}

const faqDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false,
  index: null,
  form: {
    question: '',
    answer: ''
  }
})

const importDialog = reactive({
  visible: false,
  loading: false,
  content: ''
})

const statsEntries = ref([])
const statsLoading = ref(false)
const statsSaving = ref(false)

const loadFaqs = async () => {
  try {
    loading.value = true
    const resp = await getFaqList()
    const list = Array.isArray(resp?.data) ? resp.data : []
    faqList.value = list.map((item, idx) => ({
      ...item,
      _index: idx
    }))
  } catch (error) {
    console.error('获取 FAQ 失败', error)
    ElMessage.error(error?.response?.data?.msg || '获取 FAQ 列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    statsLoading.value = true
    const resp = await getFaqStats()
    const map = resp?.data || {}
    statsEntries.value = Object.keys(map).map((key) => ({
      question: key,
      count: Number(map[key]) || 0,
      _dirty: false
    }))
  } catch (error) {
    console.error('获取统计失败', error)
    statsEntries.value = []
  } finally {
    statsLoading.value = false
  }
}

const openCreateDialog = () => {
  faqDialog.isEdit = false
  faqDialog.index = null
  faqDialog.form.question = ''
  faqDialog.form.answer = ''
  faqDialog.visible = true
}

const openEditDialog = (row) => {
  faqDialog.isEdit = true
  faqDialog.index = row._index
  faqDialog.form.question = row.question
  faqDialog.form.answer = row.answer
  faqDialog.visible = true
}

const submitFaq = async () => {
  if (!faqDialog.form.question.trim() || !faqDialog.form.answer.trim()) {
    ElMessage.warning('问题和答案不能为空')
    return
  }
  try {
    faqDialog.loading = true
    const payload = {
      question: faqDialog.form.question.trim(),
      answer: faqDialog.form.answer.trim()
    }
    if (faqDialog.isEdit) {
      await updateFaq(faqDialog.index, payload)
      ElMessage.success('更新成功')
    } else {
      await createFaq(payload)
      ElMessage.success('新增成功')
    }
    faqDialog.visible = false
    await loadFaqs()
  } catch (error) {
    console.error('保存 FAQ 失败', error)
    ElMessage.error(error?.response?.data?.msg || '保存失败')
  } finally {
    faqDialog.loading = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除问题「${row.question}」吗？`, '提示', {
      type: 'warning'
    })
    await deleteFaq(row._index)
    ElMessage.success('删除成功')
    await loadFaqs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除 FAQ 失败', error)
      ElMessage.error(error?.response?.data?.msg || '删除失败')
    }
  }
}

const openImportDialog = () => {
  importDialog.visible = true
  importDialog.content = ''
}

const handleImportSubmit = async () => {
  if (!importDialog.content.trim()) {
    ElMessage.warning('请输入 JSON 内容')
    return
  }
  try {
    importDialog.loading = true
    const parsed = JSON.parse(importDialog.content)
    if (!Array.isArray(parsed)) {
      throw new Error('JSON 须为数组')
    }
    await importFaqs({ faqs: parsed })
    ElMessage.success('导入成功')
    importDialog.visible = false
    await loadFaqs()
  } catch (error) {
    console.error('导入失败', error)
    ElMessage.error(error?.message || error?.response?.data?.msg || '导入失败')
  } finally {
    importDialog.loading = false
  }
}

const handleExport = async () => {
  try {
    const resp = await exportFaqs()
    const faqs = resp?.data?.faqs || resp?.data || []
    const blob = new Blob([JSON.stringify(faqs, null, 2)], {
      type: 'application/json;charset=utf-8'
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `qa-faqs-${new Date().toISOString().slice(0, 10)}.json`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error(error?.response?.data?.msg || '导出失败')
  }
}

const addStatsRow = () => {
  statsEntries.value.push({
    question: '',
    count: 0,
    _dirty: true
  })
}

const removeStatsRow = (idx) => {
  statsEntries.value.splice(idx, 1)
}

const handleStatsReset = () => {
  statsEntries.value = []
}

const markStatsDirty = (idx) => {
  if (statsEntries.value[idx]) {
    statsEntries.value[idx]._dirty = true
  }
}

const saveStats = async () => {
  try {
    statsSaving.value = true
    const payload = {}
    statsEntries.value.forEach((item) => {
      if (item.question.trim()) {
        payload[item.question.trim()] = Number(item.count) || 0
      }
    })
    await updateFaqStats({ stats: payload })
    ElMessage.success('统计已更新')
    await loadStats()
  } catch (error) {
    console.error('保存统计失败', error)
    ElMessage.error(error?.response?.data?.msg || '保存失败')
  } finally {
    statsSaving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadFaqs(), loadStats()])
})
</script>

<style scoped>
.qa-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header-card h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #303133;
}

.page-header-card p {
  margin: 0;
  color: #909399;
}

.header-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.metrics {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.metric-item {
  flex: 1;
  background: #f5f7fb;
  border-radius: 8px;
  padding: 16px 18px;
}

.metric-label {
  display: block;
  color: #909399;
  font-size: 13px;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.question-text,
.answer-text {
  white-space: pre-line;
  line-height: 1.5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.import-tip {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.import-tip code {
  background: #f4f4f5;
  padding: 2px 4px;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .header-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .metrics {
    flex-direction: column;
  }

  .card-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-input {
    width: 100%;
  }
}
</style>