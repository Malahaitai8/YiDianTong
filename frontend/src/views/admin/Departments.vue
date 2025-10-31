<template>
  <div class="departments-container">
    <!-- 统计数据卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon departments">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ departmentStats.totalDepartments }}</div>
              <div class="stat-label">科室总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon clinics">
              <el-icon><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ departmentStats.totalClinics }}</div>
              <div class="stat-label">门诊总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容卡片 -->
    <el-card class="main-card">
      <!-- 搜索和操作栏 -->
      <div class="search-bar">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索科室名称"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
              style="width: 300px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="openBatchAddDialog">
              <el-icon><Plus /></el-icon>
              批量添加科室
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 科室列表 -->
      <div class="table-section">
        <div class="table-header">
          <h3>科室列表</h3>
          <div class="table-actions">
            <el-button 
              v-if="selectedDepartments.length > 0" 
              type="danger" 
              size="small"
              @click="batchDelete"
            >
              批量删除 ({{ selectedDepartments.length }})
            </el-button>
          </div>
        </div>
        
        <el-table
          :data="filteredDepartments"
          v-loading="loading"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          class="modern-table"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="科室ID" width="80" />
          <el-table-column prop="name" label="科室名称" min-width="150">
            <template #default="{ row }">
              <div class="department-name">
                <el-icon class="dept-icon"><OfficeBuilding /></el-icon>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="科室描述" min-width="200">
            <template #default="{ row }">
              <span class="description-text">{{ row.description || '暂无描述' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="门诊数量" width="100">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ getDepartmentClinicCount(row.id) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="{ row }">
              <el-button-group>
                <el-button type="primary" size="small" @click="viewDepartment(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button type="warning" size="small" @click="editDepartment(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteDepartment(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </el-button-group>
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
      </div>
    </el-card>

    <!-- 添加/编辑科室对话框 -->
    <el-dialog
      v-model="departmentDialog.visible"
      :title="departmentDialog.isEdit ? '编辑科室' : '添加科室'"
      width="500px"
      @close="resetDepartmentForm"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="departmentRules"
        label-width="100px"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入科室名称" />
        </el-form-item>
        
        <el-form-item label="科室描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入科室描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="departmentDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveDepartment" :loading="departmentDialog.loading">
            {{ departmentDialog.isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看科室详情对话框 -->
    <el-dialog
      v-model="viewDialog.visible"
      title="科室详情"
      width="800px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="科室ID">
          {{ selectedDepartment.id }}
        </el-descriptions-item>
        <el-descriptions-item label="科室名称">
          {{ selectedDepartment.name }}
        </el-descriptions-item>
        <el-descriptions-item label="科室描述">
          {{ selectedDepartment.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 门诊列表 -->
      <div class="clinic-section" style="margin-top: 20px;">
        <div class="section-header">
          <h3>科室门诊</h3>
          <el-button type="primary" size="small" @click="openAddClinicDialog">
            <el-icon><Plus /></el-icon>
            添加门诊
          </el-button>
        </div>
        
        <el-table
          :data="departmentClinics"
          v-loading="clinicsLoading"
          style="width: 100%; margin-top: 10px;"
          size="small"
        >
          <el-table-column prop="id" label="门诊ID" width="80" />
          <el-table-column prop="name" label="门诊名称" width="150" />
          <el-table-column prop="description" label="门诊描述" min-width="200">
            <template #default="{ row }">
              <span>{{ row.description || '暂无描述' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="warning" size="small" @click="editClinic(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDeleteClinic(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="departmentClinics.length === 0 && !clinicsLoading" class="empty-state">
          <el-empty description="该科室暂无门诊" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="editDepartment(selectedDepartment)">
            编辑科室
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑门诊对话框 -->
    <el-dialog
      v-model="clinicDialog.visible"
      :title="clinicDialog.isEdit ? '编辑门诊' : '添加门诊'"
      width="500px"
      @close="resetClinicForm"
    >
      <el-form
        ref="clinicFormRef"
        :model="clinicForm"
        :rules="clinicRules"
        label-width="100px"
      >
        <el-form-item label="门诊名称" prop="name">
          <el-input v-model="clinicForm.name" placeholder="请输入门诊名称" />
        </el-form-item>
        
        <el-form-item label="门诊描述" prop="description">
          <el-input
            v-model="clinicForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入门诊描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="clinicDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveClinic" :loading="clinicDialog.loading">
            {{ clinicDialog.isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量添加科室对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      title="批量添加科室"
      width="800px"
      @close="resetBatchForm"
    >
      <el-tabs v-model="batchDialog.activeTab" class="batch-tabs">
        <!-- 手动添加选项卡 -->
        <el-tab-pane label="手动添加" name="manual">
          <div class="batch-form-container">
            <!-- 数量设置区域 -->
            <div class="quantity-section">
              <el-card shadow="never" class="quantity-card">
                <div class="quantity-header">
                  <span class="quantity-title">快速设置</span>
                </div>
                <div class="quantity-content">
                   <el-row :gutter="20" align="middle">
                     <el-col :span="6">
                       <div class="quantity-input-wrapper">
                         <span class="quantity-label">添加数量：</span>
                         <el-input-number
                           v-model="batchQuantity"
                           :min="1"
                           :max="20"
                           size="small"
                           placeholder="输入数量"
                           class="quantity-input"
                         />
                       </div>
                     </el-col>
                     <el-col :span="6">
                       <el-button 
                         type="success" 
                         size="small" 
                         @click="setBatchQuantity"
                         :disabled="!batchQuantity || batchQuantity < 1"
                       >
                         <el-icon><Setting /></el-icon>
                         生成表单
                       </el-button>
                     </el-col>
                     <el-col :span="6">
                       <el-text type="info" size="small">
                         当前：{{ batchForm.departments.length }} 个科室
                       </el-text>
                     </el-col>
                   </el-row>
                 </div>
              </el-card>
            </div>
            
            <div class="batch-header">
              <span>请填写要添加的科室信息：</span>
              <el-button type="primary" size="small" @click="addDepartmentItem">
                <el-icon><Plus /></el-icon>
                添加科室
              </el-button>
            </div>
            
            <div class="batch-form-list">
              <div 
                v-for="(item, index) in batchForm.departments" 
                :key="index"
                class="batch-form-item"
              >
                <div class="item-header">
                  <span class="item-title">科室 {{ index + 1 }}</span>
                  <el-button 
                    v-if="batchForm.departments.length > 1"
                    type="danger" 
                    size="small" 
                    text
                    @click="removeDepartmentItem(index)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
                
                <el-form
                  :model="item"
                  :rules="batchDepartmentRules"
                  label-width="100px"
                  class="batch-item-form"
                >
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="科室名称" :prop="`name`">
                        <el-input 
                          v-model="item.name" 
                          placeholder="请输入科室名称"
                          @blur="validateDepartmentName(item, index)"
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="科室描述" :prop="`description`">
                        <el-input 
                          v-model="item.description" 
                          placeholder="请输入科室描述"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </div>
            </div>
            
            <div class="batch-summary">
              <el-alert
                :title="`共 ${batchForm.departments.length} 个科室待添加`"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 导入表格选项卡 -->
        <el-tab-pane label="导入表格" name="import">
          <div class="import-container">
            <!-- 模板下载区域 -->
            <div class="template-section">
              <el-alert
                type="info"
                :closable="false"
                show-icon
                class="usage-alert"
              >
                <template #default>
                  <div class="usage-tips">
                    <div class="tip-item">
                      <el-icon class="tip-icon"><DocumentAdd /></el-icon>
                      <span>下载模板文件，按照格式填写科室信息</span>
                    </div>
                    <div class="tip-item">
                      <el-icon class="tip-icon"><Document /></el-icon>
                      <span>支持 Excel (.xlsx) 和 CSV (.csv) 格式</span>
                    </div>
                    <div class="tip-item">
                      <el-icon class="tip-icon"><Check /></el-icon>
                      <span>科室名称为必填项，长度2-50个字符</span>
                    </div>
                    <div class="tip-item">
                      <el-icon class="tip-icon"><Edit /></el-icon>
                      <span>科室描述为可选项</span>
                    </div>
                  </div>
                </template>
              </el-alert>
              
              <div class="template-actions">
                <el-button type="success" @click="downloadTemplate('excel')">
                  <el-icon><Download /></el-icon>
                  下载Excel模板
                </el-button>
                <el-button type="success" @click="downloadTemplate('csv')">
                  <el-icon><Download /></el-icon>
                  下载CSV模板
                </el-button>
              </div>
            </div>

            <!-- 文件上传区域 -->
            <div class="upload-section">
              <el-upload
                ref="uploadRef"
                class="upload-demo"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                :before-upload="beforeUpload"
                accept=".xlsx,.xls,.csv"
                :limit="1"
                :on-exceed="handleExceed"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 .xlsx、.xls、.csv 格式，文件大小不超过 5MB
                  </div>
                </template>
              </el-upload>
            </div>

            <!-- 数据预览区域 -->
            <div v-if="importData.length > 0" class="preview-section">
              <div class="preview-header">
                <h4>数据预览</h4>
                <div class="preview-actions">
                  <el-button size="small" @click="clearImportData">清空数据</el-button>
                  <span class="data-count">共 {{ importData.length }} 条数据</span>
                </div>
              </div>
              
              <el-table
                :data="importData"
                style="width: 100%"
                max-height="300"
                border
              >
                <el-table-column type="index" label="序号" width="60" />
                <el-table-column prop="name" label="科室名称" min-width="150">
                  <template #default="{ row, $index }">
                    <div class="cell-content">
                      <span :class="{ 'error-text': row.errors?.name }">{{ row.name }}</span>
                      <el-icon v-if="row.errors?.name" class="error-icon" color="#f56c6c">
                        <WarningFilled />
                      </el-icon>
                    </div>
                    <div v-if="row.errors?.name" class="error-message">
                      {{ row.errors.name }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="科室描述" min-width="200">
                  <template #default="{ row }">
                    <span>{{ row.description || '无' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag v-if="!row.errors" type="success" size="small">有效</el-tag>
                    <el-tag v-else type="danger" size="small">错误</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="import-summary">
                <el-alert
                  :title="`有效数据: ${validImportCount} 条，错误数据: ${errorImportCount} 条`"
                  :type="errorImportCount > 0 ? 'warning' : 'success'"
                  :closable="false"
                  show-icon
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialog.visible = false">取消</el-button>
          <el-button 
            v-if="batchDialog.activeTab === 'manual'"
            type="primary" 
            @click="saveBatchDepartments" 
            :loading="batchDialog.loading"
            :disabled="!isValidBatchForm"
          >
            批量添加 ({{ batchForm.departments.length }})
          </el-button>
          <el-button 
            v-if="batchDialog.activeTab === 'import'"
            type="primary" 
            @click="saveImportDepartments" 
            :loading="batchDialog.loading"
            :disabled="validImportCount === 0"
          >
            导入数据 ({{ validImportCount }})
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
  import { 
    Search, 
    Plus, 
    OfficeBuilding, 
    House, 
    CircleCheckFilled, 
    DataAnalysis,
    View,
    Edit,
    Delete,
    Download,
    UploadFilled,
    WarningFilled,
    Setting,
    DocumentAdd,
    Document,
    Check
  } from '@element-plus/icons-vue'
  import { 
    getDepartmentList, 
    getDepartmentById, 
    createDepartment, 
    updateDepartment, 
    deleteDepartment,
    batchCreateDepartments
  } from '@/api/department'
  import {
    getClinicList,
    getClinicsByDepartmentId,
    createClinic,
    updateClinic,
    deleteClinic
  } from '@/api/clinic'
  import * as XLSX from 'xlsx'

// 响应式数据
const loading = ref(false)
const departments = ref([])
const selectedDepartments = ref([])

// 门诊相关数据
const departmentClinics = ref([])
const clinicsLoading = ref(false)

// 统计数据
const departmentStats = reactive({
  totalDepartments: 0,
  totalClinics: 0,
  activeDepartments: 0,
  averageClinics: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 科室对话框
const departmentDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false
})

// 查看对话框
const viewDialog = reactive({
  visible: false,
  department: {}
})

// 门诊对话框
const clinicDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false
})

// 批量添加对话框
const batchDialog = reactive({
  visible: false,
  loading: false,
  activeTab: 'manual'
})

// 导入数据相关
const importData = ref([])
const uploadRef = ref()

// 科室表单
const departmentForm = reactive({
  id: null,
  name: '',
  description: ''
})

// 门诊表单
const clinicForm = reactive({
  id: null,
  name: '',
  description: '',
  departmentId: null
})

// 批量添加表单
const batchForm = reactive({
  departments: [
    { name: '', description: '' }
  ]
})

// 批量数量
const batchQuantity = ref(1)

// 选中的科室
const selectedDepartment = ref({})

// 表单引用
const departmentFormRef = ref()
const clinicFormRef = ref()

// 表单验证规则
const departmentRules = {
  name: [
    { required: true, message: '请输入科室名称', trigger: 'blur' },
    { min: 2, max: 50, message: '科室名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const clinicRules = {
  name: [
    { required: true, message: '请输入门诊名称', trigger: 'blur' },
    { min: 2, max: 50, message: '门诊名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const batchDepartmentRules = {
  name: [
    { required: true, message: '请输入科室名称', trigger: 'blur' },
    { min: 2, max: 50, message: '科室名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 计算属性 - 过滤后的科室列表
const filteredDepartments = computed(() => {
  let result = departments.value
  
  if (searchForm.keyword) {
    result = result.filter(dept => 
      dept.name.toLowerCase().includes(searchForm.keyword.toLowerCase()) ||
      (dept.description && dept.description.toLowerCase().includes(searchForm.keyword.toLowerCase()))
    )
  }
  
  // 更新分页总数
  pagination.total = result.length
  
  // 分页处理
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return result.slice(start, end)
})

// 方法
// 加载科室列表
const loadDepartments = async () => {
  try {
    loading.value = true
    const response = await getDepartmentList()
    departments.value = response.data || []
    pagination.total = departments.value.length
    
    // 计算统计数据
    await calculateStats()
  } catch (error) {
    ElMessage.error('获取科室列表失败')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStats = async () => {
  try {
    // 获取所有门诊数据来计算统计
    const clinicsResponse = await getClinicList()
    const allClinics = clinicsResponse.data || []
    
    // 更新统计数据
    departmentStats.totalDepartments = departments.value.length
    departmentStats.totalClinics = allClinics.length
    departmentStats.activeDepartments = departments.value.filter(dept => 
      allClinics.some(clinic => clinic.departmentId === dept.id)
    ).length
    departmentStats.averageClinics = departments.value.length > 0 
      ? Math.round((allClinics.length / departments.value.length) * 10) / 10 
      : 0
    
    // 同时更新门诊数量缓存
    const countMap = new Map()
    allClinics.forEach(clinic => {
      const deptId = clinic.departmentId
      countMap.set(deptId, (countMap.get(deptId) || 0) + 1)
    })
    clinicCountCache.value = countMap
  } catch (error) {
    console.error('计算统计数据失败:', error)
  }
}

// 门诊数量缓存
const clinicCountCache = ref(new Map())

// 获取科室门诊数量
const getDepartmentClinicCount = (departmentId) => {
  return clinicCountCache.value.get(departmentId) || 0
}



// 获取科室列表（搜索和分页）
const fetchDepartments = async () => {
  loading.value = true
  try {
    const response = await getDepartmentList()
    let filteredData = response.data || []
    
    // 应用搜索过滤
      if (searchForm.name) {
        filteredData = filteredData.filter(dept => 
          dept.name.includes(searchForm.name) || 
          dept.description.includes(searchForm.name)
        )
      }
    
    // 分页处理
    const start = (pagination.currentPage - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    departments.value = filteredData.slice(start, end)
    pagination.total = filteredData.length
    
  } catch (error) {
    ElMessage.error('获取科室列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchDepartments()
}

const resetSearch = () => {
  searchForm.keyword = ''
  pagination.currentPage = 1
  fetchDepartments()
}

const handleSelectionChange = (selection) => {
  selectedDepartments.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchDepartments()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchDepartments()
}

const viewDepartment = async (department) => {
    try {
      const response = await getDepartmentById(department.id)
      selectedDepartment.value = response.data || department
      viewDialog.department = response.data || department
      viewDialog.visible = true
      
      // 加载该科室下的门诊列表
      await loadDepartmentClinics(department.id)
    } catch (error) {
      ElMessage.error('获取科室详情失败')
    }
  }

const editDepartment = (department) => {
  departmentDialog.visible = true
  departmentDialog.isEdit = true
  departmentForm.id = department.id
  departmentForm.name = department.name
  departmentForm.description = department.description || ''
  viewDialog.visible = false
}

const handleDeleteDepartment = async (department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除科室"${department.name}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await deleteDepartment(department.id)
    ElMessage.success('删除成功')
    await fetchDepartments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除科室失败:', error)
      ElMessage.error('删除失败')
    }
  } finally {
    loading.value = false
  }
}

const saveDepartment = async () => {
  if (!departmentFormRef.value) return
  
  try {
    await departmentFormRef.value.validate()
    
    departmentDialog.loading = true
    
    const data = {
      name: departmentForm.name,
      description: departmentForm.description
    }
    
    if (departmentDialog.isEdit) {
      await updateDepartment(departmentForm.id, data)
    } else {
      await createDepartment(data)
    }
    
    ElMessage.success(departmentDialog.isEdit ? '更新成功' : '添加成功')
      departmentDialog.visible = false
      await fetchDepartments()
  } catch (error) {
    console.error('保存科室失败:', error)
    ElMessage.error(departmentDialog.isEdit ? '更新失败' : '添加失败')
  } finally {
    departmentDialog.loading = false
  }
}

const resetDepartmentForm = () => {
  if (departmentFormRef.value) {
    departmentFormRef.value.resetFields()
  }
  departmentForm.id = null
  departmentForm.name = ''
  departmentForm.description = ''
}

// 批量添加相关方法
const openBatchAddDialog = () => {
  batchDialog.visible = true
  resetBatchForm()
}

const resetBatchForm = () => {
  batchForm.departments = [
    { name: '', description: '' }
  ]
  batchDialog.activeTab = 'manual'
  importData.value = []
  batchQuantity.value = 1
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 设置批量数量
const setBatchQuantity = () => {
  if (!batchQuantity.value || batchQuantity.value < 1) {
    ElMessage.warning('请输入有效的数量')
    return
  }
  
  const newDepartments = []
  for (let i = 0; i < batchQuantity.value; i++) {
    newDepartments.push({ name: '', description: '' })
  }
  
  batchForm.departments = newDepartments
  ElMessage.success(`已生成 ${batchQuantity.value} 个科室表单`)
}

const addDepartmentItem = () => {
  batchForm.departments.push({ name: '', description: '' })
}

const removeDepartmentItem = (index) => {
  if (batchForm.departments.length > 1) {
    batchForm.departments.splice(index, 1)
  }
}

const validateDepartmentName = (item, index) => {
  if (!item.name.trim()) return
  
  // 检查当前批次中是否有重复名称
  const duplicateInBatch = batchForm.departments.some((dept, i) => 
    i !== index && dept.name.trim() === item.name.trim()
  )
  
  if (duplicateInBatch) {
    ElMessage.warning(`科室名称"${item.name}"在当前批次中重复`)
    return
  }
  
  // 检查是否与现有科室重复
  const duplicateInExisting = departments.value.some(dept => 
    dept.name === item.name.trim()
  )
  
  if (duplicateInExisting) {
    ElMessage.warning(`科室名称"${item.name}"已存在`)
  }
}

const saveBatchDepartments = async () => {
  try {
    // 验证表单
    const validDepartments = batchForm.departments.filter(dept => 
      dept.name.trim() && dept.name.trim().length >= 2
    )
    
    if (validDepartments.length === 0) {
      ElMessage.error('请至少填写一个有效的科室信息')
      return
    }
    
    // 检查重复名称
    const names = validDepartments.map(dept => dept.name.trim())
    const uniqueNames = [...new Set(names)]
    if (names.length !== uniqueNames.length) {
      ElMessage.error('批次中存在重复的科室名称')
      return
    }
    
    batchDialog.loading = true
    
    // 调用批量创建API
    await batchCreateDepartments(validDepartments)
    
    ElMessage.success(`成功添加 ${validDepartments.length} 个科室`)
    batchDialog.visible = false
    await fetchDepartments()
    await calculateStats()
    
  } catch (error) {
    console.error('批量添加科室失败:', error)
    ElMessage.error('批量添加失败，请检查网络连接或联系管理员')
  } finally {
    batchDialog.loading = false
  }
}

// 计算属性 - 验证批量表单是否有效
const isValidBatchForm = computed(() => {
  return batchForm.departments.some(dept => 
    dept.name.trim() && dept.name.trim().length >= 2
  )
})

// 计算属性 - 有效导入数据数量
const validImportCount = computed(() => {
  return importData.value.filter(item => !item.errors).length
})

// 计算属性 - 错误导入数据数量
const errorImportCount = computed(() => {
  return importData.value.filter(item => item.errors).length
})

// 加载科室下的门诊列表
const loadDepartmentClinics = async (departmentId) => {
  try {
    clinicsLoading.value = true
    const response = await getClinicsByDepartmentId(departmentId)
    departmentClinics.value = response.data || []
  } catch (error) {
    ElMessage.error('获取门诊列表失败')
    departmentClinics.value = []
  } finally {
    clinicsLoading.value = false
  }
}

// 打开添加门诊对话框
const openAddClinicDialog = () => {
  clinicForm.id = null
  clinicForm.name = ''
  clinicForm.description = ''
  clinicForm.departmentId = selectedDepartment.value.id
  clinicDialog.isEdit = false
  clinicDialog.visible = true
}

// 编辑门诊
const editClinic = (clinic) => {
  clinicForm.id = clinic.id
  clinicForm.name = clinic.name
  clinicForm.description = clinic.description
  clinicForm.departmentId = clinic.departmentId
  clinicDialog.isEdit = true
  clinicDialog.visible = true
}

// 保存门诊
const saveClinic = async () => {
  if (!clinicFormRef.value) return
  
  try {
    await clinicFormRef.value.validate()
    clinicDialog.loading = true
    
    const data = {
      name: clinicForm.name,
      description: clinicForm.description,
      departmentId: clinicForm.departmentId
    }
    
    if (clinicDialog.isEdit) {
      await updateClinic(clinicForm.id, data)
      ElMessage.success('更新成功')
    } else {
      await createClinic(data)
      ElMessage.success('添加成功')
    }
    
    clinicDialog.visible = false
    await loadDepartmentClinics(selectedDepartment.value.id)
    // 重新计算统计数据和门诊数量缓存
    await calculateStats()
  } catch (error) {
    console.error('保存门诊失败:', error)
    ElMessage.error(clinicDialog.isEdit ? '更新失败' : '添加失败')
  } finally {
    clinicDialog.loading = false
  }
}

// 删除门诊
const handleDeleteClinic = async (clinic) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除门诊"${clinic.name}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteClinic(clinic.id)
    ElMessage.success('删除成功')
    await loadDepartmentClinics(selectedDepartment.value.id)
    // 重新计算统计数据和门诊数量缓存
    await calculateStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除门诊失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 重置门诊表单
const resetClinicForm = () => {
  if (clinicFormRef.value) {
    clinicFormRef.value.resetFields()
  }
  clinicForm.id = null
  clinicForm.name = ''
  clinicForm.description = ''
  clinicForm.departmentId = null
}

// 批量删除科室
const batchDelete = async () => {
  if (selectedDepartments.value.length === 0) {
    ElMessage.warning('请选择要删除的科室')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedDepartments.value.length} 个科室吗？此操作不可恢复！`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除
    const deletePromises = selectedDepartments.value.map(dept => deleteDepartment(dept.id))
    await Promise.all(deletePromises)
    
    ElMessage.success(`成功删除 ${selectedDepartments.value.length} 个科室`)
    selectedDepartments.value = []
    await loadDepartments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 导入相关方法
const downloadTemplate = (type) => {
  const headers = ['科室名称', '科室描述']
  const sampleData = [
    ['内科', '负责内科疾病的诊断和治疗'],
    ['外科', '负责外科手术和治疗'],
    ['儿科', '专门为儿童提供医疗服务']
  ]
  
  if (type === 'excel') {
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    const wsData = [headers, ...sampleData]
    const ws = XLSX.utils.aoa_to_sheet(wsData)
    
    // 设置列宽
    ws['!cols'] = [
      { width: 20 }, // 科室名称
      { width: 40 }  // 科室描述
    ]
    
    XLSX.utils.book_append_sheet(wb, ws, '科室模板')
    XLSX.writeFile(wb, '科室导入模板.xlsx')
  } else if (type === 'csv') {
    // 生成CSV内容
    const csvContent = [headers, ...sampleData]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n')
    
    // 创建下载链接
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '科室导入模板.csv'
    link.click()
    URL.revokeObjectURL(link.href)
  }
}

const handleFileChange = (file) => {
  const fileType = file.name.split('.').pop().toLowerCase()
  if (!['xlsx', 'xls', 'csv'].includes(fileType)) {
    ElMessage.error('只支持 Excel 和 CSV 格式文件')
    return false
  }
  
  // 检查文件大小 (5MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  
  // 解析文件
  parseFile(file.raw)
}

// 解析文件内容
const parseFile = (file) => {
  const reader = new FileReader()
  
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      let workbook
      
      if (file.name.endsWith('.csv')) {
        // 解析CSV
        const text = new TextDecoder('utf-8').decode(data)
        const lines = text.split('\n').filter(line => line.trim())
        const parsedData = lines.slice(1).map(line => {
          const columns = line.split(',').map(col => col.replace(/"/g, '').trim())
          return {
            name: columns[0] || '',
            description: columns[1] || ''
          }
        })
        validateAndSetImportData(parsedData)
      } else {
        // 解析Excel
        workbook = XLSX.read(data, { type: 'array' })
        const firstSheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[firstSheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
        
        if (jsonData.length < 2) {
          ElMessage.error('文件内容为空或格式不正确')
          return
        }
        
        // 跳过标题行，解析数据
        const parsedData = jsonData.slice(1).map(row => ({
          name: (row[0] || '').toString().trim(),
          description: (row[1] || '').toString().trim()
        })).filter(item => item.name || item.description) // 过滤空行
        
        validateAndSetImportData(parsedData)
      }
    } catch (error) {
      console.error('文件解析失败:', error)
      ElMessage.error('文件解析失败，请检查文件格式')
    }
  }
  
  reader.readAsArrayBuffer(file)
}

// 验证并设置导入数据
const validateAndSetImportData = (data) => {
  const validatedData = data.map((item, index) => {
    const errors = {}
    
    // 验证科室名称
    if (!item.name) {
      errors.name = '科室名称不能为空'
    } else if (item.name.length < 2 || item.name.length > 50) {
      errors.name = '科室名称长度应在2-50个字符之间'
    } else {
      // 检查是否与现有科室重复
      const existingDept = departments.value.find(dept => dept.name === item.name)
      if (existingDept) {
        errors.name = '科室名称已存在'
      }
      
      // 检查批次内是否重复
      const duplicateIndex = data.findIndex((d, i) => i !== index && d.name === item.name)
      if (duplicateIndex !== -1) {
        errors.name = `与第${duplicateIndex + 1}行重复`
      }
    }
    
    return {
      ...item,
      errors: Object.keys(errors).length > 0 ? errors : null
    }
  })
  
  importData.value = validatedData
  ElMessage.success(`成功解析 ${data.length} 条数据`)
}

const beforeUpload = () => {
  return false // 阻止自动上传
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const clearImportData = () => {
  importData.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const saveImportDepartments = async () => {
  const validData = importData.value.filter(item => !item.errors)
  if (validData.length === 0) {
    ElMessage.error('没有有效的数据可以导入')
    return
  }
  
  try {
    batchDialog.loading = true
    await batchCreateDepartments(validData)
    ElMessage.success(`成功导入 ${validData.length} 个科室`)
    batchDialog.visible = false
    await loadDepartments()
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败')
  } finally {
    batchDialog.loading = false
  }
}

// 生命周期
  onMounted(() => {
    loadDepartments()
  })
</script>

<style scoped>
.departments-container {
  padding: 0;
}

/* 统计卡片样式 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 28px;
  color: white;
}

.stat-icon.departments {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.clinics {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.active {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.average {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

/* 主卡片样式 */
.main-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 搜索栏样式 */
.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}

.search-form .el-form-item {
  margin-right: 16px;
  margin-bottom: 0;
}

.search-form .el-form-item:last-child {
  margin-left: auto;
  margin-right: 0;
}

.search-form .el-form-item .el-button {
  margin-right: 8px;
}

.search-form .el-form-item .el-button:last-child {
  margin-right: 0;
}

/* 表格区域样式 */
.table-section {
  padding: 0 20px 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.table-actions {
  display: flex;
  gap: 10px;
}

/* 现代化表格样式 */
.modern-table {
  border-radius: 8px;
  overflow: hidden;
}

.modern-table :deep(.el-table__header) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
  border-bottom: 2px solid #e4e7ed;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f0f9ff;
}

.department-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dept-icon {
  color: #409eff;
}

.description-text {
  color: #606266;
  line-height: 1.4;
}

/* 按钮组样式 */
.el-button-group {
  display: flex;
  gap: 4px;
}

.el-button-group .el-button {
  margin: 0;
  border-radius: 6px;
}

/* 分页样式 */
.pagination-container {
  margin-top: 20px;
  text-align: right;
  padding: 20px 0;
  border-top: 1px solid #e4e7ed;
}

/* 对话框样式 */
.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 10px;
}

/* 门诊列表样式 */
.clinic-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 10px;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .table-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
}

/* 动画效果 */
.el-card {
  transition: all 0.3s ease;
}

.el-button {
  transition: all 0.2s ease;
}

.el-button:hover {
  transform: translateY(-1px);
}

/* 批量添加对话框样式 */
.batch-form-container {
  max-height: 500px;
  overflow-y: auto;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.batch-header span {
  font-weight: 600;
  color: #303133;
}

.batch-form-list {
  margin-bottom: 20px;
}

.batch-form-item {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.batch-form-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.item-title {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.batch-item-form {
  margin: 0;
}

.batch-summary {
  margin-top: 20px;
}

/* 批量表单响应式设计 */
@media (max-width: 768px) {
  .batch-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .item-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .batch-form-item .el-row .el-col {
    margin-bottom: 10px;
  }
}

/* 导入相关样式 */
.batch-tabs {
  margin-top: 20px;
}

/* 数量设置区域样式 */
.quantity-section {
  margin-bottom: 20px;
}

.quantity-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.quantity-header {
  margin-bottom: 15px;
}

.quantity-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.quantity-content {
  padding: 0;
}

.quantity-content .el-form-item {
  margin-bottom: 0;
}

.quantity-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-label {
  white-space: nowrap;
  color: #606266;
  font-size: 14px;
  min-width: 70px;
}

.quantity-input {
  flex: 1;
  min-width: 100px;
}

.import-container {
  padding: 20px 0;
}

.template-section {
  margin-bottom: 30px;
}

.usage-alert {
  text-align: left;
}

.usage-tips {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.tip-icon {
  color: #409eff;
  font-size: 16px;
  flex-shrink: 0;
}

.template-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
}

.upload-section {
  margin-bottom: 30px;
}

.preview-section {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.preview-header h4 {
  margin: 0;
  color: #303133;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.data-count {
  color: #909399;
  font-size: 14px;
}

.cell-content {
  display: flex;
  align-items: center;
  gap: 5px;
}

.error-text {
  color: #f56c6c;
}

.error-icon {
  font-size: 16px;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 2px;
}

.import-summary {
  margin-top: 15px;
}
</style>