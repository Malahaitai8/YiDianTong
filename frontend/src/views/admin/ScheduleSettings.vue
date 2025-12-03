<template>
  <div class="schedule-settings-page">
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">号源管理</h1>
        <p class="header-subtitle">统一管理号源上限、挂号费配置、号别设置和统计信息</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="settings-tabs">
      <!-- 号源上限管理 -->
      <el-tab-pane label="号源上限管理" name="limits">
        <el-tabs v-model="limitSubTab" type="card" class="sub-tabs">
          <!-- 全局配置 -->
          <el-tab-pane label="全局配置" name="global">
        <div class="tab-content">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>全局上限配置</span>
                <el-button type="primary" @click="openEditGlobal">编辑配置</el-button>
              </div>
            </template>
            <div v-loading="globalLoading" class="settings-display">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="允许的号别">
                  <el-tag v-for="type in globalSettings.allowedSlotTypes" :key="type" class="slot-type-tag">
                    {{ mapSlotType(type) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="默认总号源">
                  {{ globalSettings.defaultTotalSlots || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="单条排班最大号源">
                  {{ globalSettings.maxSlotsPerSchedule || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="单医生单日预约上限">
                  {{ globalSettings.maxAppointmentsPerDayPerDoctor || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="单患者单日预约上限">
                  {{ globalSettings.maxAppointmentsPerDayPerPatient || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="单医生单日VIP号上限">
                  {{ globalSettings.vipDailyLimitPerDoctor || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="周末启用严格限制">
                  {{ globalSettings.enforceWeekendLimits ? '是' : '否' }}
                </el-descriptions-item>
                <el-descriptions-item label="覆盖策略">
                  {{ mapOverrideStrategy(globalSettings.overrideStrategy) }}
                </el-descriptions-item>
                <el-descriptions-item label="最晚取消时间（小时）" :span="2">
                  {{ globalSettings.cancelPolicy?.latestCancelHours || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="逾期取消惩罚" :span="2">
                  {{ globalSettings.cancelPolicy?.penaltyEnabled ? '启用' : '禁用' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </div>
          </el-tab-pane>

          <!-- 医生级配置 -->
          <el-tab-pane label="医生级配置" name="doctor">
        <div class="tab-content">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>医生级上限配置</span>
                <div class="header-actions">
                  <el-select
                    v-model="selectedDoctorId"
                    placeholder="请选择医生"
                    filterable
                    clearable
                    style="width: 300px; margin-right: 12px;"
                    @change="loadDoctorSettings"
                  >
                    <el-option
                      v-for="doctor in doctorList"
                      :key="doctor.id"
                      :label="`${doctor.name} (${doctor.title || ''})`"
                      :value="doctor.id"
                    />
                  </el-select>
                  <el-button
                    type="primary"
                    :disabled="!selectedDoctorId"
                    @click="openEditDoctor"
                  >
                    编辑配置
                  </el-button>
                </div>
              </div>
            </template>
            <div v-loading="doctorLoading" class="settings-display">
              <div v-if="!selectedDoctorId" class="empty-state">
                <el-empty description="请先选择医生" />
              </div>
              <el-descriptions v-else :column="2" border>
                <el-descriptions-item label="医生ID">
                  {{ doctorSettings.doctorId || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="覆盖策略">
                  {{ mapOverrideStrategy(doctorSettings.overrideStrategy) }}
                </el-descriptions-item>
                <el-descriptions-item label="允许的号别">
                  <el-tag v-for="type in doctorSettings.allowedSlotTypes" :key="type" class="slot-type-tag">
                    {{ mapSlotType(type) }}
                  </el-tag>
                  <span v-if="!doctorSettings.allowedSlotTypes || doctorSettings.allowedSlotTypes.length === 0">继承全局</span>
                </el-descriptions-item>
                <el-descriptions-item label="默认总号源">
                  {{ doctorSettings.defaultTotalSlots || '继承全局' }}
                </el-descriptions-item>
                <el-descriptions-item label="单条排班最大号源">
                  {{ doctorSettings.maxSlotsPerSchedule || '继承全局' }}
                </el-descriptions-item>
                <el-descriptions-item label="单医生单日VIP号上限">
                  {{ doctorSettings.vipDailyLimitPerDoctor || '继承全局' }}
                </el-descriptions-item>
                <el-descriptions-item label="生效开始日期" :span="2">
                  {{ formatDate(doctorSettings.effectiveStartDate) || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="生效结束日期" :span="2">
                  {{ formatDate(doctorSettings.effectiveEndDate) || '无限制' }}
                </el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">
                  {{ doctorSettings.description || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </div>
          </el-tab-pane>

          <!-- 门诊级配置 -->
          <el-tab-pane label="门诊级配置" name="clinic">
        <div class="tab-content">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>门诊级上限配置</span>
                <div class="header-actions">
                  <el-select
                    v-model="selectedClinicId"
                    placeholder="请选择门诊"
                    filterable
                    clearable
                    style="width: 300px; margin-right: 12px;"
                    @change="loadClinicSettings"
                  >
                    <el-option
                      v-for="clinic in clinicList"
                      :key="clinic.id"
                      :label="clinic.name"
                      :value="clinic.id"
                    />
                  </el-select>
                  <el-button
                    type="primary"
                    :disabled="!selectedClinicId"
                    @click="openEditClinic"
                  >
                    编辑配置
                  </el-button>
                </div>
              </div>
            </template>
            <div v-loading="clinicLoading" class="settings-display">
              <div v-if="!selectedClinicId" class="empty-state">
                <el-empty description="请先选择门诊" />
              </div>
              <el-descriptions v-else :column="2" border>
                <el-descriptions-item label="门诊ID">
                  {{ clinicSettings.clinicId || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="覆盖策略">
                  {{ mapOverrideStrategy(clinicSettings.overrideStrategy) }}
                </el-descriptions-item>
                <el-descriptions-item label="允许的号别">
                  <el-tag v-for="type in clinicSettings.allowedSlotTypes" :key="type" class="slot-type-tag">
                    {{ mapSlotType(type) }}
                  </el-tag>
                  <span v-if="!clinicSettings.allowedSlotTypes || clinicSettings.allowedSlotTypes.length === 0">继承全局</span>
                </el-descriptions-item>
                <el-descriptions-item label="默认总号源">
                  {{ clinicSettings.defaultTotalSlots || '继承全局' }}
                </el-descriptions-item>
                <el-descriptions-item label="单条排班最大号源">
                  {{ clinicSettings.maxSlotsPerSchedule || '继承全局' }}
                </el-descriptions-item>
                <el-descriptions-item label="生效开始日期" :span="2">
                  {{ formatDate(clinicSettings.effectiveStartDate) || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="生效结束日期" :span="2">
                  {{ formatDate(clinicSettings.effectiveEndDate) || '无限制' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </div>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>

      <!-- 挂号费配置 -->
      <el-tab-pane label="挂号费配置" name="fees">
        <div class="tab-content">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>挂号费配置</span>
                <el-button type="primary" @click="saveFeeConfig" :loading="feeSaving">
                  保存配置
                </el-button>
              </div>
            </template>
            <el-form :model="feeConfig" label-width="150px" class="fee-config-form">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="普通号挂号费">
                    <el-input-number
                      v-model="feeConfig.normalFee"
                      :min="0"
                      :precision="2"
                      :step="0.1"
                      controls-position="right"
                      style="width: 200px;"
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
                      style="width: 200px;"
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
                      style="width: 200px;"
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
                      style="width: 200px;"
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
                      style="width: 200px;"
                    />
                    <span class="unit">%</span>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 号别管理 -->
      <el-tab-pane label="号别管理" name="slotTypes">
        <div class="tab-content">
          <el-card class="settings-card">
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
                    {{ mapSlotType(scope.row.type) }}
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
        </div>
      </el-tab-pane>

      <!-- 号源统计 -->
      <el-tab-pane label="号源统计" name="stats">
        <div class="tab-content">
          <el-card class="settings-card">
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
                  <div class="stat-number">{{ slotStats.totalSlots }}</div>
                  <div class="stat-label">总号源数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-number">{{ slotStats.bookedSlots }}</div>
                  <div class="stat-label">已预约号源</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-number">{{ slotStats.availableSlots }}</div>
                  <div class="stat-label">可用号源</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-number">{{ slotStats.utilizationRate }}%</div>
                  <div class="stat-label">利用率</div>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 全局配置编辑对话框 -->
    <el-dialog
      v-model="globalEditVisible"
      title="编辑全局上限配置"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="globalFormRef"
        :model="globalForm"
        :rules="globalFormRules"
        label-width="200px"
      >
        <el-form-item label="允许的号别" prop="allowedSlotTypes">
          <el-checkbox-group v-model="globalForm.allowedSlotTypes">
            <el-checkbox label="normal">普通号</el-checkbox>
            <el-checkbox label="expert">专家号</el-checkbox>
            <el-checkbox label="vip">特需号</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="默认总号源" prop="defaultTotalSlots">
          <el-input-number
            v-model="globalForm.defaultTotalSlots"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单条排班最大号源" prop="maxSlotsPerSchedule">
          <el-input-number
            v-model="globalForm.maxSlotsPerSchedule"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单医生单日预约上限" prop="maxAppointmentsPerDayPerDoctor">
          <el-input-number
            v-model="globalForm.maxAppointmentsPerDayPerDoctor"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单患者单日预约上限" prop="maxAppointmentsPerDayPerPatient">
          <el-input-number
            v-model="globalForm.maxAppointmentsPerDayPerPatient"
            :min="1"
            :max="10"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单医生单日VIP号上限" prop="vipDailyLimitPerDoctor">
          <el-input-number
            v-model="globalForm.vipDailyLimitPerDoctor"
            :min="1"
            :max="100"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="周末启用严格限制" prop="enforceWeekendLimits">
          <el-switch v-model="globalForm.enforceWeekendLimits" />
        </el-form-item>
        <el-form-item label="覆盖策略" prop="overrideStrategy">
          <el-radio-group v-model="globalForm.overrideStrategy">
            <el-radio label="INHERIT">继承（下级未配置字段从全局继承）</el-radio>
            <el-radio label="OVERRIDE">覆盖（下级未配置字段不继承全局）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最晚取消时间（小时）" prop="cancelPolicy.latestCancelHours">
          <el-input-number
            v-model="globalForm.cancelPolicy.latestCancelHours"
            :min="0"
            :max="48"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="逾期取消惩罚" prop="cancelPolicy.penaltyEnabled">
          <el-switch v-model="globalForm.cancelPolicy.penaltyEnabled" />
        </el-form-item>
        <el-form-item label="生效开始日期" prop="effectiveStartDate">
          <el-date-picker
            v-model="globalForm.effectiveStartDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期（不选则立即生效）"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="生效结束日期" prop="effectiveEndDate">
          <el-date-picker
            v-model="globalForm.effectiveEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期（不选则无限制）"
            style="width: 200px;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="globalEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGlobalSettings" :loading="globalSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 医生级配置编辑对话框 -->
    <el-dialog
      v-model="doctorEditVisible"
      title="编辑医生级上限配置"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="doctorFormRules"
        label-width="200px"
      >
        <el-form-item label="医生">
          <el-input :value="selectedDoctorName" disabled style="width: 200px;" />
        </el-form-item>
        <el-form-item label="覆盖策略" prop="overrideStrategy">
          <el-radio-group v-model="doctorForm.overrideStrategy">
            <el-radio label="INHERIT">继承（未配置字段从全局继承）</el-radio>
            <el-radio label="OVERRIDE">覆盖（未配置字段不继承全局）</el-radio>
          </el-radio-group>
          <div class="form-hint">当选择覆盖时，建议显式配置所有字段</div>
        </el-form-item>
        <el-form-item label="允许的号别" prop="allowedSlotTypes">
          <el-checkbox-group v-model="doctorForm.allowedSlotTypes">
            <el-checkbox label="normal">普通号</el-checkbox>
            <el-checkbox label="expert">专家号</el-checkbox>
            <el-checkbox label="vip">特需号</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="默认总号源" prop="defaultTotalSlots">
          <el-input-number
            v-model="doctorForm.defaultTotalSlots"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单条排班最大号源" prop="maxSlotsPerSchedule">
          <el-input-number
            v-model="doctorForm.maxSlotsPerSchedule"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单医生单日VIP号上限" prop="vipDailyLimitPerDoctor">
          <el-input-number
            v-model="doctorForm.vipDailyLimitPerDoctor"
            :min="1"
            :max="100"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="生效开始日期" prop="effectiveStartDate">
          <el-date-picker
            v-model="doctorForm.effectiveStartDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期（不选则立即生效）"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="生效结束日期" prop="effectiveEndDate">
          <el-date-picker
            v-model="doctorForm.effectiveEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期（不选则无限制）"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="doctorForm.description"
            type="textarea"
            :rows="3"
            placeholder="可填写配置说明"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doctorEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDoctorSettings" :loading="doctorSaving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 门诊级配置编辑对话框 -->
    <el-dialog
      v-model="clinicEditVisible"
      title="编辑门诊级上限配置"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="clinicFormRef"
        :model="clinicForm"
        :rules="clinicFormRules"
        label-width="200px"
      >
        <el-form-item label="门诊">
          <el-input :value="selectedClinicName" disabled style="width: 200px;" />
        </el-form-item>
        <el-form-item label="覆盖策略" prop="overrideStrategy">
          <el-radio-group v-model="clinicForm.overrideStrategy">
            <el-radio label="INHERIT">继承（未配置字段从全局继承）</el-radio>
            <el-radio label="OVERRIDE">覆盖（未配置字段不继承全局）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="允许的号别" prop="allowedSlotTypes">
          <el-checkbox-group v-model="clinicForm.allowedSlotTypes">
            <el-checkbox label="normal">普通号</el-checkbox>
            <el-checkbox label="expert">专家号</el-checkbox>
            <el-checkbox label="vip">特需号</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="默认总号源" prop="defaultTotalSlots">
          <el-input-number
            v-model="clinicForm.defaultTotalSlots"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="单条排班最大号源" prop="maxSlotsPerSchedule">
          <el-input-number
            v-model="clinicForm.maxSlotsPerSchedule"
            :min="1"
            :max="1000"
            :precision="0"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="生效开始日期" prop="effectiveStartDate">
          <el-date-picker
            v-model="clinicForm.effectiveStartDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期（不选则立即生效）"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="生效结束日期" prop="effectiveEndDate">
          <el-date-picker
            v-model="clinicForm.effectiveEndDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期（不选则无限制）"
            style="width: 200px;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clinicEditVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClinicSettings" :loading="clinicSaving">保存</el-button>
      </template>
    </el-dialog>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getGlobalScheduleSettings,
  updateGlobalScheduleSettings,
  getDoctorScheduleSettings,
  updateDoctorScheduleSettings,
  getClinicScheduleSettings,
  updateClinicScheduleSettings
} from '@/api/scheduleSettings'
import { getDoctorList } from '@/api/doctor'
import { getClinicList } from '@/api/clinic'
import { systemConfigApi } from '@/api/systemConfig'

const activeTab = ref('limits')
const limitSubTab = ref('global')

// 全局配置
const globalLoading = ref(false)
const globalSettings = ref({})
const globalEditVisible = ref(false)
const globalSaving = ref(false)
const globalFormRef = ref()
const globalForm = ref({
  allowedSlotTypes: [],
  defaultTotalSlots: 20,
  maxSlotsPerSchedule: 50,
  maxAppointmentsPerDayPerDoctor: 60,
  maxAppointmentsPerDayPerPatient: 3,
  vipDailyLimitPerDoctor: 10,
  enforceWeekendLimits: true,
  cancelPolicy: {
    latestCancelHours: 2,
    penaltyEnabled: false
  },
  overrideStrategy: 'INHERIT',
  effectiveStartDate: null,
  effectiveEndDate: null
})

const globalFormRules = {
  allowedSlotTypes: [
    { required: true, message: '请至少选择一个号别类型', trigger: 'change' }
  ],
  defaultTotalSlots: [
    { required: true, message: '请输入默认总号源', trigger: 'blur' },
    { type: 'number', min: 1, message: '默认总号源必须大于0', trigger: 'blur' }
  ],
  maxSlotsPerSchedule: [
    { required: true, message: '请输入单条排班最大号源', trigger: 'blur' },
    { type: 'number', min: 1, message: '单条排班最大号源必须大于0', trigger: 'blur' }
  ],
  maxAppointmentsPerDayPerDoctor: [
    { required: true, message: '请输入单医生单日预约上限', trigger: 'blur' },
    { type: 'number', min: 1, message: '单医生单日预约上限必须大于0', trigger: 'blur' }
  ],
  maxAppointmentsPerDayPerPatient: [
    { required: true, message: '请输入单患者单日预约上限', trigger: 'blur' },
    { type: 'number', min: 1, message: '单患者单日预约上限必须大于0', trigger: 'blur' }
  ],
  vipDailyLimitPerDoctor: [
    { required: true, message: '请输入单医生单日VIP号上限', trigger: 'blur' },
    { type: 'number', min: 1, message: '单医生单日VIP号上限必须大于0', trigger: 'blur' }
  ]
}

// 医生级配置
const doctorLoading = ref(false)
const doctorSettings = ref({})
const selectedDoctorId = ref(null)
const doctorList = ref([])
const doctorEditVisible = ref(false)
const doctorSaving = ref(false)
const doctorFormRef = ref()
const doctorForm = ref({
  allowedSlotTypes: [],
  defaultTotalSlots: null,
  maxSlotsPerSchedule: null,
  vipDailyLimitPerDoctor: null,
  overrideStrategy: 'INHERIT',
  effectiveStartDate: null,
  effectiveEndDate: null,
  description: ''
})

const doctorFormRules = {
  overrideStrategy: [
    { required: true, message: '请选择覆盖策略', trigger: 'change' }
  ]
}

const selectedDoctorName = computed(() => {
  const doctor = doctorList.value.find(d => d.id === selectedDoctorId.value)
  return doctor ? `${doctor.name} (${doctor.title || ''})` : ''
})

// 门诊级配置
const clinicLoading = ref(false)
const clinicSettings = ref({})
const selectedClinicId = ref(null)
const clinicList = ref([])
const clinicEditVisible = ref(false)
const clinicSaving = ref(false)
const clinicFormRef = ref()
const clinicForm = ref({
  allowedSlotTypes: [],
  defaultTotalSlots: null,
  maxSlotsPerSchedule: null,
  overrideStrategy: 'INHERIT',
  effectiveStartDate: null,
  effectiveEndDate: null
})

const clinicFormRules = {
  overrideStrategy: [
    { required: true, message: '请选择覆盖策略', trigger: 'change' }
  ]
}

const selectedClinicName = computed(() => {
  const clinic = clinicList.value.find(c => c.id === selectedClinicId.value)
  return clinic ? clinic.name : ''
})

// 挂号费配置
const feeSaving = ref(false)
const feeConfig = ref({
  normalFee: 0,
  expertFee: 0,
  vipFee: 0,
  studentReimbursement: 0,
  teacherReimbursement: 0
})

// 号别管理
const slotTypesLoading = ref(false)
const slotTypes = ref([])
const slotTypeDialogVisible = ref(false)
const slotTypeDialogTitle = ref('添加号别')
const slotTypeSaving = ref(false)
const slotTypeFormRef = ref()
const slotTypeForm = ref({
  id: null,
  type: '',
  name: '',
  description: '',
  fee: 0,
  isActive: true
})
const slotTypeRules = {
  type: [
    { required: true, message: '请选择号别类型', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入号别名称', trigger: 'blur' }
  ],
  fee: [
    { required: true, message: '请输入挂号费', trigger: 'blur' }
  ]
}

// 号源统计
const slotStats = ref({
  totalSlots: 0,
  bookedSlots: 0,
  availableSlots: 0,
  utilizationRate: 0
})

// 加载全局配置
const loadGlobalSettings = async () => {
  globalLoading.value = true
  try {
    const res = await getGlobalScheduleSettings()
    globalSettings.value = res?.data || {}
  } catch (e) {
    ElMessage.error('加载全局配置失败')
  } finally {
    globalLoading.value = false
  }
}

// 加载医生级配置
const loadDoctorSettings = async () => {
  if (!selectedDoctorId.value) {
    doctorSettings.value = {}
    return
  }
  doctorLoading.value = true
  try {
    const res = await getDoctorScheduleSettings(selectedDoctorId.value)
    doctorSettings.value = res?.data || {}
  } catch (e) {
    ElMessage.error('加载医生配置失败')
    doctorSettings.value = {}
  } finally {
    doctorLoading.value = false
  }
}

// 加载门诊级配置
const loadClinicSettings = async () => {
  if (!selectedClinicId.value) {
    clinicSettings.value = {}
    return
  }
  clinicLoading.value = true
  try {
    const res = await getClinicScheduleSettings(selectedClinicId.value)
    clinicSettings.value = res?.data || {}
  } catch (e) {
    ElMessage.error('加载门诊配置失败')
    clinicSettings.value = {}
  } finally {
    clinicLoading.value = false
  }
}

// 打开编辑全局配置
const openEditGlobal = () => {
  globalForm.value = {
    allowedSlotTypes: globalSettings.value.allowedSlotTypes || [],
    defaultTotalSlots: globalSettings.value.defaultTotalSlots || 20,
    maxSlotsPerSchedule: globalSettings.value.maxSlotsPerSchedule || 50,
    maxAppointmentsPerDayPerDoctor: globalSettings.value.maxAppointmentsPerDayPerDoctor || 60,
    maxAppointmentsPerDayPerPatient: globalSettings.value.maxAppointmentsPerDayPerPatient || 3,
    vipDailyLimitPerDoctor: globalSettings.value.vipDailyLimitPerDoctor || 10,
    enforceWeekendLimits: globalSettings.value.enforceWeekendLimits !== false,
    cancelPolicy: {
      latestCancelHours: globalSettings.value.cancelPolicy?.latestCancelHours || 2,
      penaltyEnabled: globalSettings.value.cancelPolicy?.penaltyEnabled || false
    },
    overrideStrategy: globalSettings.value.overrideStrategy || 'INHERIT',
    effectiveStartDate: globalSettings.value.effectiveStartDate || null,
    effectiveEndDate: globalSettings.value.effectiveEndDate || null
  }
  globalEditVisible.value = true
}

// 保存全局配置
const saveGlobalSettings = async () => {
  if (!globalFormRef.value) return
  const valid = await globalFormRef.value.validate().catch(() => false)
  if (!valid) return

  globalSaving.value = true
  try {
    await updateGlobalScheduleSettings(globalForm.value)
    ElMessage.success('保存成功')
    globalEditVisible.value = false
    await loadGlobalSettings()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    globalSaving.value = false
  }
}

// 打开编辑医生配置
const openEditDoctor = () => {
  if (!selectedDoctorId.value) return
  doctorForm.value = {
    allowedSlotTypes: doctorSettings.value.allowedSlotTypes || [],
    defaultTotalSlots: doctorSettings.value.defaultTotalSlots || null,
    maxSlotsPerSchedule: doctorSettings.value.maxSlotsPerSchedule || null,
    vipDailyLimitPerDoctor: doctorSettings.value.vipDailyLimitPerDoctor || null,
    overrideStrategy: doctorSettings.value.overrideStrategy || 'INHERIT',
    effectiveStartDate: doctorSettings.value.effectiveStartDate || null,
    effectiveEndDate: doctorSettings.value.effectiveEndDate || null,
    description: doctorSettings.value.description || ''
  }
  doctorEditVisible.value = true
}

// 保存医生配置
const saveDoctorSettings = async () => {
  if (!doctorFormRef.value || !selectedDoctorId.value) return
  const valid = await doctorFormRef.value.validate().catch(() => false)
  if (!valid) return

  doctorSaving.value = true
  try {
    await updateDoctorScheduleSettings(selectedDoctorId.value, doctorForm.value)
    ElMessage.success('保存成功')
    doctorEditVisible.value = false
    await loadDoctorSettings()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    doctorSaving.value = false
  }
}

// 打开编辑门诊配置
const openEditClinic = () => {
  if (!selectedClinicId.value) return
  clinicForm.value = {
    allowedSlotTypes: clinicSettings.value.allowedSlotTypes || [],
    defaultTotalSlots: clinicSettings.value.defaultTotalSlots || null,
    maxSlotsPerSchedule: clinicSettings.value.maxSlotsPerSchedule || null,
    overrideStrategy: clinicSettings.value.overrideStrategy || 'INHERIT',
    effectiveStartDate: clinicSettings.value.effectiveStartDate || null,
    effectiveEndDate: clinicSettings.value.effectiveEndDate || null
  }
  clinicEditVisible.value = true
}

// 保存门诊配置
const saveClinicSettings = async () => {
  if (!clinicFormRef.value || !selectedClinicId.value) return
  const valid = await clinicFormRef.value.validate().catch(() => false)
  if (!valid) return

  clinicSaving.value = true
  try {
    await updateClinicScheduleSettings(selectedClinicId.value, clinicForm.value)
    ElMessage.success('保存成功')
    clinicEditVisible.value = false
    await loadClinicSettings()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    clinicSaving.value = false
  }
}

// 工具函数
const mapSlotType = (type) => {
  const map = {
    normal: '普通号',
    expert: '专家号',
    vip: '特需号'
  }
  return map[type] || type
}

const mapOverrideStrategy = (strategy) => {
  const map = {
    INHERIT: '继承',
    OVERRIDE: '覆盖'
  }
  return map[strategy] || strategy
}

const formatDate = (date) => {
  if (!date) return ''
  if (typeof date === 'string') {
    return date.split(' ')[0]
  }
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载挂号费配置
const loadFeeConfig = async () => {
  try {
    const config = await systemConfigApi.getFeeConfig()
    feeConfig.value = {
      normalFee: config.normalFee || 0,
      expertFee: config.expertFee || 0,
      vipFee: config.vipFee || 0,
      studentReimbursement: config.studentReimbursement || 0,
      teacherReimbursement: config.teacherReimbursement || 0
    }
  } catch (error) {
    console.error('加载挂号费配置失败:', error)
    ElMessage.error('加载挂号费配置失败')
  }
}

// 保存挂号费配置
const saveFeeConfig = async () => {
  feeSaving.value = true
  try {
    await systemConfigApi.updateFeeConfig(feeConfig.value)
    ElMessage.success('挂号费配置保存成功')
  } catch (error) {
    console.error('保存挂号费配置失败:', error)
    ElMessage.error('保存挂号费配置失败')
  } finally {
    feeSaving.value = false
  }
}

// 加载号别列表
const loadSlotTypes = async () => {
  slotTypesLoading.value = true
  try {
    // 模拟数据，实际应该从后端获取
    slotTypes.value = [
      {
        id: 1,
        type: 'normal',
        name: '普通号',
        description: '普通门诊号源',
        fee: feeConfig.value.normalFee,
        isActive: true
      },
      {
        id: 2,
        type: 'expert',
        name: '专家号',
        description: '专家门诊号源',
        fee: feeConfig.value.expertFee,
        isActive: true
      },
      {
        id: 3,
        type: 'vip',
        name: '特需号',
        description: '特需门诊号源',
        fee: feeConfig.value.vipFee,
        isActive: true
      }
    ]
  } catch (error) {
    console.error('加载号别列表失败:', error)
    ElMessage.error('加载号别列表失败')
  } finally {
    slotTypesLoading.value = false
  }
}

// 显示添加号别对话框
const showAddSlotTypeDialog = () => {
  slotTypeDialogTitle.value = '添加号别'
  slotTypeDialogVisible.value = true
  resetSlotTypeForm()
}

// 编辑号别
const editSlotType = (row) => {
  slotTypeDialogTitle.value = '编辑号别'
  slotTypeDialogVisible.value = true
  slotTypeForm.value = { ...row }
}

// 保存号别
const saveSlotType = async () => {
  try {
    await slotTypeFormRef.value.validate()
    slotTypeSaving.value = true
    
    // 这里应该调用后端API保存号别
    // 目前只是模拟操作
    if (slotTypeForm.value.id) {
      // 更新
      const index = slotTypes.value.findIndex(item => item.id === slotTypeForm.value.id)
      if (index !== -1) {
        slotTypes.value[index] = { ...slotTypeForm.value }
      }
      ElMessage.success('号别更新成功')
    } else {
      // 新增
      slotTypeForm.value.id = Date.now()
      slotTypes.value.push({ ...slotTypeForm.value })
      ElMessage.success('号别添加成功')
    }
    
    slotTypeDialogVisible.value = false
  } catch (error) {
    console.error('保存号别失败:', error)
  } finally {
    slotTypeSaving.value = false
  }
}

// 切换号别状态
const toggleSlotTypeStatus = async (row) => {
  try {
    // 这里应该调用后端API更新状态
    ElMessage.success(`号别${row.isActive ? '启用' : '禁用'}成功`)
  } catch (error) {
    console.error('更新号别状态失败:', error)
    ElMessage.error('更新号别状态失败')
    // 回滚状态
    row.isActive = !row.isActive
  }
}

// 删除号别
const deleteSlotType = async (row) => {
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
    const index = slotTypes.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      slotTypes.value.splice(index, 1)
    }
    ElMessage.success('号别删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除号别失败:', error)
      ElMessage.error('删除号别失败')
    }
  }
}

// 重置号别表单
const resetSlotTypeForm = () => {
  slotTypeForm.value = {
    id: null,
    type: '',
    name: '',
    description: '',
    fee: 0,
    isActive: true
  }
  if (slotTypeFormRef.value) {
    slotTypeFormRef.value.clearValidate()
  }
}

// 获取号别类型标签样式
const getSlotTypeTagType = (type) => {
  const typeMap = {
    normal: '',
    expert: 'warning',
    vip: 'danger'
  }
  return typeMap[type] || ''
}

// 加载统计信息
const loadStats = async () => {
  try {
    // 这里应该调用后端API获取统计信息
    // 目前使用模拟数据
    slotStats.value = {
      totalSlots: 1200,
      bookedSlots: 850,
      availableSlots: 350,
      utilizationRate: 71
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    ElMessage.error('加载统计信息失败')
  }
}

// 刷新统计
const refreshStats = () => {
  loadStats()
  ElMessage.success('统计信息已刷新')
}

// 初始化
onMounted(async () => {
  await loadGlobalSettings()
  await loadFeeConfig()
  await loadSlotTypes()
  await loadStats()
  try {
    const [docsRes, clinicsRes] = await Promise.all([getDoctorList(), getClinicList()])
    doctorList.value = Array.isArray(docsRes?.data) ? docsRes.data : (docsRes?.data?.list || [])
    clinicList.value = Array.isArray(clinicsRes?.data) ? clinicsRes.data : (clinicsRes?.data?.list || [])
  } catch (e) {
    console.error('加载医生或门诊列表失败', e)
  }
})
</script>

<style scoped>
.schedule-settings-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

.settings-tabs {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tab-content {
  padding: 20px;
}

.settings-card {
  border: none;
  box-shadow: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.settings-display {
  min-height: 200px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.slot-type-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.form-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #606266;
}

.sub-tabs {
  margin-top: 0;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.fee-config-form {
  padding: 20px 0;
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

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>

