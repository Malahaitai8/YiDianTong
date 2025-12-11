<template>
  <div class="fee-settings-page">
    <!-- 顶部标题卡片 -->
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">费用设置</h1>
        <p class="header-subtitle">统一管理挂号费用和报销比例配置</p>
      </div>
    </div>

    <!-- 提示信息卡片 -->
    <el-card class="tips-card">
      <div class="tips-header">
        <el-icon color="#E6A23C" :size="20"><Warning /></el-icon>
        <span class="tips-title">提示</span>
      </div>
      <ul class="tips-list">
        <li>费用等级应为递增关系：普通号 < 专家号 < 特需号</li>
        <li>报销比例应在 0-100% 之间</li>
        <li>所有改动需要点击"保存设置"才会生效</li>
      </ul>
    </el-card>

    <!-- 挂号费用设置卡片 -->
    <el-card class="settings-card">
      <div class="card-title-wrapper">
        <h3 class="card-title">挂号费用设置</h3>
        <p class="card-subtitle">配置不同等级的挂号费用</p>
      </div>
      
      <el-row :gutter="32" class="fee-row">
        <el-col :span="8">
          <div class="fee-item">
            <div class="fee-label">普通号</div>
            <div class="fee-input-wrapper">
              <el-input-number
                v-model="feeConfig.normalFee"
                :min="0"
                :precision="2"
                :step="0.1"
                :controls="false"
                class="fee-input"
              />
              <span class="fee-unit">元</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="fee-item">
            <div class="fee-label">专家号</div>
            <div class="fee-input-wrapper">
              <el-input-number
                v-model="feeConfig.expertFee"
                :min="0"
                :precision="2"
                :step="0.1"
                :controls="false"
                class="fee-input"
              />
              <span class="fee-unit">元</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="fee-item">
            <div class="fee-label">特需号</div>
            <div class="fee-input-wrapper">
              <el-input-number
                v-model="feeConfig.vipFee"
                :min="0"
                :precision="2"
                :step="0.1"
                :controls="false"
                class="fee-input"
              />
              <span class="fee-unit">元</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 报销比例设置卡片 -->
    <el-card class="settings-card">
      <div class="card-title-wrapper">
        <h3 class="card-title">报销比例设置</h3>
        <p class="card-subtitle">配置不同身份的报销比例</p>
      </div>
      
      <el-row :gutter="32" class="reimbursement-row">
        <el-col :span="12">
          <div class="reimbursement-item">
            <div class="reimbursement-label">学生报销比例</div>
            <div class="reimbursement-input-wrapper">
              <el-input-number
                v-model="feeConfig.studentReimbursement"
                :min="0"
                :max="100"
                :precision="0"
                :step="1"
                :controls="false"
                class="reimbursement-input"
              />
              <span class="reimbursement-unit">%</span>
            </div>
            <div class="reimbursement-desc">在校学生享受的报销比例</div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="reimbursement-item">
            <div class="reimbursement-label">教师报销比例</div>
            <div class="reimbursement-input-wrapper">
              <el-input-number
                v-model="feeConfig.teacherReimbursement"
                :min="0"
                :max="100"
                :precision="0"
                :step="1"
                :controls="false"
                class="reimbursement-input"
              />
              <span class="reimbursement-unit">%</span>
            </div>
            <div class="reimbursement-desc">教职员工享受的报销比例</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 底部操作按钮 -->
    <div class="footer-actions">
      <el-button size="large" @click="resetConfig">
        <el-icon><RefreshLeft /></el-icon>
        取消更改
      </el-button>
      <el-button type="primary" size="large" @click="saveFeeConfig" :loading="feeSaving">
        <el-icon><Check /></el-icon>
        保存设置
      </el-button>
    </div>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft, Warning } from '@element-plus/icons-vue'
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

// 重置配置
const resetConfig = async () => {
  await loadFeeConfig()
  ElMessage.info('已恢复到上次保存的配置')
}

// 初始化
onMounted(async () => {
  await loadGlobalSettings()
  await loadFeeConfig()
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
.fee-settings-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0;
}

/* 顶部标题卡片 */
.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 设置卡片 */
.settings-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

:deep(.settings-card .el-card__body) {
  padding: 32px;
}

/* 卡片标题 */
.card-title-wrapper {
  margin-bottom: 32px;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

/* 挂号费用项 */
.fee-row {
  margin: 0;
}

.fee-item {
  text-align: left;
}

.fee-label {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}

.fee-input-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.fee-input {
  flex: 1;
}

:deep(.fee-input .el-input__inner) {
  height: 48px;
  font-size: 16px;
  text-align: left;
  padding-left: 16px;
}

.fee-unit {
  margin-left: 12px;
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.fee-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 报销比例项 */
.reimbursement-row {
  margin: 0;
}

.reimbursement-item {
  text-align: left;
}

.reimbursement-label {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}

.reimbursement-input-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.reimbursement-input {
  flex: 1;
}

:deep(.reimbursement-input .el-input__inner) {
  height: 48px;
  font-size: 16px;
  text-align: left;
  padding-left: 16px;
}

.reimbursement-unit {
  margin-left: 12px;
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.reimbursement-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 底部操作按钮 */
.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-bottom: 20px;
}

/* 提示卡片 */
.tips-card {
  border-radius: 8px;
  background: #FEF9E7;
  border: 1px solid #F9E79F;
}

:deep(.tips-card .el-card__body) {
  padding: 20px 24px;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.tips-title {
  font-size: 15px;
  font-weight: 600;
  color: #E6A23C;
}

.tips-list {
  margin: 0;
  padding-left: 28px;
  list-style: disc;
}

.tips-list li {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  text-align: left;
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

