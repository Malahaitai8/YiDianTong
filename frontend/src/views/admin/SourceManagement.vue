<template>
  <div class="source-management">
    <div class="page-header">
      <h2>号源管理</h2>
      <p>管理挂号费配置和号别设置</p>
    </div>

    <!-- 挂号费配置 -->
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>挂号费配置</span>
          <el-button type="primary" @click="saveFeeConfig" :loading="saving">
            保存配置
          </el-button>
        </div>
      </template>
      
      <el-form :model="feeConfig" label-width="120px" class="fee-config-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="普通号挂号费">
              <el-input-number
                v-model="feeConfig.normalFee"
                :min="0"
                :precision="2"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="unit">元</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专家号挂号费">
              <el-input-number
                v-model="feeConfig.expertFee"
                :min="0"
                :precision="2"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="unit">元</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="特需号挂号费">
              <el-input-number
                v-model="feeConfig.vipFee"
                :min="0"
                :precision="2"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="unit">元</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学生报销比例">
              <el-input-number
                v-model="feeConfig.studentReimbursement"
                :min="0"
                :max="100"
                :precision="0"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="unit">%</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教师报销比例">
              <el-input-number
                v-model="feeConfig.teacherReimbursement"
                :min="0"
                :max="100"
                :precision="0"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="unit">%</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 号别管理 -->
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>号别管理</span>
          <el-button type="primary" @click="showAddSlotTypeDialog">
            添加号别
          </el-button>
        </div>
      </template>
      
      <el-table :data="slotTypes" style="width: 100%" v-loading="slotTypesLoading">
        <el-table-column prop="type" label="号别类型" width="150">
          <template #default="scope">
            <el-tag :type="getSlotTypeTagType(scope.row.type)">
              {{ getSlotTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="号别名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="fee" label="挂号费" width="100">
          <template #default="scope">
            ¥{{ scope.row.fee }}
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isActive"
              @change="toggleSlotTypeStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editSlotType(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteSlotType(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 号别添加/编辑对话框 -->
    <el-dialog
      :title="slotTypeDialogTitle"
      v-model="slotTypeDialogVisible"
      width="500px"
      @close="resetSlotTypeForm"
    >
      <el-form
        :model="slotTypeForm"
        :rules="slotTypeRules"
        ref="slotTypeFormRef"
        label-width="100px"
      >
        <el-form-item label="号别类型" prop="type">
          <el-select v-model="slotTypeForm.type" placeholder="请选择号别类型" style="width: 100%">
            <el-option label="普通号" value="normal" />
            <el-option label="专家号" value="expert" />
            <el-option label="特需号" value="vip" />
          </el-select>
        </el-form-item>
        <el-form-item label="号别名称" prop="name">
          <el-input v-model="slotTypeForm.name" placeholder="请输入号别名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="slotTypeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入号别描述"
          />
        </el-form-item>
        <el-form-item label="挂号费" prop="fee">
          <el-input-number
            v-model="slotTypeForm.fee"
            :min="0"
            :precision="2"
            :step="0.1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-switch v-model="slotTypeForm.isActive" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="slotTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSlotType" :loading="slotTypeSaving">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 统计信息 -->
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>号源统计</span>
          <el-button type="primary" @click="refreshStats">
            刷新统计
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ stats.totalSlots }}</div>
            <div class="stat-label">总号源数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ stats.bookedSlots }}</div>
            <div class="stat-label">已预约号源</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ stats.availableSlots }}</div>
            <div class="stat-label">可用号源</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-number">{{ stats.utilizationRate }}%</div>
            <div class="stat-label">利用率</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { systemConfigApi } from '@/api/systemConfig'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'SourceManagement',
  data() {
    return {
      // 挂号费配置
      feeConfig: {
        normalFee: 0,
        expertFee: 0,
        vipFee: 0,
        studentReimbursement: 0,
        teacherReimbursement: 0
      },
      saving: false,
      
      // 号别管理
      slotTypes: [],
      slotTypesLoading: false,
      slotTypeDialogVisible: false,
      slotTypeDialogTitle: '添加号别',
      slotTypeSaving: false,
      slotTypeForm: {
        id: null,
        type: '',
        name: '',
        description: '',
        fee: 0,
        isActive: true
      },
      slotTypeRules: {
        type: [
          { required: true, message: '请选择号别类型', trigger: 'change' }
        ],
        name: [
          { required: true, message: '请输入号别名称', trigger: 'blur' }
        ],
        fee: [
          { required: true, message: '请输入挂号费', trigger: 'blur' }
        ]
      },
      
      // 统计信息
      stats: {
        totalSlots: 0,
        bookedSlots: 0,
        availableSlots: 0,
        utilizationRate: 0
      }
    }
  },
  
  mounted() {
    this.loadFeeConfig()
    this.loadSlotTypes()
    this.loadStats()
  },
  
  methods: {
    // 加载挂号费配置
    async loadFeeConfig() {
      try {
        const config = await systemConfigApi.getFeeConfig()
        this.feeConfig = {
          normalFee: parseFloat(config.normalFee || 0),
          expertFee: parseFloat(config.expertFee || 0),
          vipFee: parseFloat(config.vipFee || 0),
          studentReimbursement: parseInt(config.studentReimbursement || 0),
          teacherReimbursement: parseInt(config.teacherReimbursement || 0)
        }
      } catch (error) {
        console.error('加载挂号费配置失败:', error)
        ElMessage.error('加载挂号费配置失败')
      }
    },
    
    // 保存挂号费配置
    async saveFeeConfig() {
      this.saving = true
      try {
        await systemConfigApi.updateFeeConfig(this.feeConfig)
        ElMessage.success('挂号费配置保存成功')
      } catch (error) {
        console.error('保存挂号费配置失败:', error)
        ElMessage.error('保存挂号费配置失败')
      } finally {
        this.saving = false
      }
    },
    
    // 加载号别列表
    async loadSlotTypes() {
      this.slotTypesLoading = true
      try {
        // 模拟数据，实际应该从后端获取
        this.slotTypes = [
          {
            id: 1,
            type: 'normal',
            name: '普通号',
            description: '普通门诊号源',
            fee: this.feeConfig.normalFee,
            isActive: true
          },
          {
            id: 2,
            type: 'expert',
            name: '专家号',
            description: '专家门诊号源',
            fee: this.feeConfig.expertFee,
            isActive: true
          },
          {
            id: 3,
            type: 'vip',
            name: '特需号',
            description: '特需门诊号源',
            fee: this.feeConfig.vipFee,
            isActive: true
          }
        ]
      } catch (error) {
        console.error('加载号别列表失败:', error)
        ElMessage.error('加载号别列表失败')
      } finally {
        this.slotTypesLoading = false
      }
    },
    
    // 显示添加号别对话框
    showAddSlotTypeDialog() {
      this.slotTypeDialogTitle = '添加号别'
      this.slotTypeDialogVisible = true
      this.resetSlotTypeForm()
    },
    
    // 编辑号别
    editSlotType(row) {
      this.slotTypeDialogTitle = '编辑号别'
      this.slotTypeDialogVisible = true
      this.slotTypeForm = { ...row }
    },
    
    // 保存号别
    async saveSlotType() {
      try {
        await this.$refs.slotTypeFormRef.validate()
        this.slotTypeSaving = true
        
        // 这里应该调用后端API保存号别
        // 目前只是模拟操作
        if (this.slotTypeForm.id) {
          // 更新
          const index = this.slotTypes.findIndex(item => item.id === this.slotTypeForm.id)
          if (index !== -1) {
            this.slotTypes[index] = { ...this.slotTypeForm }
          }
          ElMessage.success('号别更新成功')
        } else {
          // 新增
          this.slotTypeForm.id = Date.now()
          this.slotTypes.push({ ...this.slotTypeForm })
          ElMessage.success('号别添加成功')
        }
        
        this.slotTypeDialogVisible = false
      } catch (error) {
        console.error('保存号别失败:', error)
      } finally {
        this.slotTypeSaving = false
      }
    },
    
    // 切换号别状态
    async toggleSlotTypeStatus(row) {
      try {
        // 这里应该调用后端API更新状态
        ElMessage.success(`号别${row.isActive ? '启用' : '禁用'}成功`)
      } catch (error) {
        console.error('更新号别状态失败:', error)
        ElMessage.error('更新号别状态失败')
        // 回滚状态
        row.isActive = !row.isActive
      }
    },
    
    // 删除号别
    async deleteSlotType(row) {
      try {
        await ElMessageBox.confirm(
          `确定要删除号别"${row.name}"吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // 这里应该调用后端API删除
        const index = this.slotTypes.findIndex(item => item.id === row.id)
        if (index !== -1) {
          this.slotTypes.splice(index, 1)
        }
        ElMessage.success('号别删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除号别失败:', error)
          ElMessage.error('删除号别失败')
        }
      }
    },
    
    // 重置号别表单
    resetSlotTypeForm() {
      this.slotTypeForm = {
        id: null,
        type: '',
        name: '',
        description: '',
        fee: 0,
        isActive: true
      }
      if (this.$refs.slotTypeFormRef) {
        this.$refs.slotTypeFormRef.clearValidate()
      }
    },
    
    // 加载统计信息
    async loadStats() {
      try {
        // 这里应该调用后端API获取统计信息
        // 目前使用模拟数据
        this.stats = {
          totalSlots: 1200,
          bookedSlots: 850,
          availableSlots: 350,
          utilizationRate: 71
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
        ElMessage.error('加载统计信息失败')
      }
    },
    
    // 刷新统计
    refreshStats() {
      this.loadStats()
      ElMessage.success('统计信息已刷新')
    },
    
    // 获取号别类型标签样式
    getSlotTypeTagType(type) {
      const typeMap = {
        normal: '',
        expert: 'warning',
        vip: 'danger'
      }
      return typeMap[type] || ''
    },
    
    // 获取号别类型名称
    getSlotTypeName(type) {
      const nameMap = {
        normal: '普通号',
        expert: '专家号',
        vip: '特需号'
      }
      return nameMap[type] || type
    }
  }
}
</script>

<style scoped>
.source-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fee-config-form {
  padding: 20px 0;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>