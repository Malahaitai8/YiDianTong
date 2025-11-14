<template>
  <div class="schedule-rules-page">
    <div class="header-card">
      <div class="header-left">
        <h1 class="header-title">排班规则管理</h1>
        <p class="header-subtitle">规则模板制定与批量生成排班</p>
      </div>
      <div class="header-right">
        <el-button type="success" @click="openCreate">新建规则</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon total">
              <el-icon><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ totalRulesCount }}</div>
              <div class="stat-label">总规则数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon week">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ activeRulesCount }}</div>
              <div class="stat-label">启用规则</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon today">
              <el-icon><CloseBold /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ inactiveRulesCount }}</div>
              <div class="stat-label">禁用规则</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon expired">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ expiredRulesCount }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="toolbar">
      <el-select v-model="filterDepartmentId" placeholder="科室" style="width: 100%" clearable>
        <el-option label="全部科室" :value="null" />
        <el-option v-for="d in departmentList" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="filterDoctorId" placeholder="医生" style="width: 100%" clearable>
        <el-option label="全部医生" :value="null" />
        <el-option v-for="doc in filteredDoctors" :key="doc.id" :label="doc.name" :value="doc.id" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" style="width: 100%" clearable>
        <el-option label="全部" value="" />
        <el-option label="启用(ACTIVE)" value="ACTIVE" />
        <el-option label="禁用(INACTIVE)" value="INACTIVE" />
        <el-option label="过期(EXPIRED)" value="EXPIRED" />
      </el-select>
      <el-select v-model="filterRuleType" placeholder="规则类型" style="width: 100%" clearable>
        <el-option label="全部" value="" />
        <el-option label="固定周排班" value="FIXED_WEEKLY" />
        <el-option label="轮班制" value="ROTATION" />
        <el-option label="自定义" value="CUSTOM" />
      </el-select>
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table :data="pagedRules" v-loading="loading" border style="width: 100%">
      <el-table-column type="index" label="序号" width="80" />
      <el-table-column label="规则名称" min-width="200">
        <template #default="scope">
          <el-link type="primary" @click="viewDetail(scope.row)">{{ scope.row.ruleName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="规则类型" width="140">
        <template #default="scope">
          <el-tag>{{ mapRuleType(scope.row.ruleType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="医生/科室" min-width="220">
        <template #default="scope">
          <div>
            <span>{{ scope.row.doctorName || scope.row.doctor?.name || scope.row.departmentName || scope.row.department?.name || '-' }}</span>
            <span v-if="(scope.row.doctorName||scope.row.doctor?.name) && (scope.row.departmentName||scope.row.department?.name)" style="color:#909399">（{{ scope.row.departmentName || scope.row.department?.name }}）</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ scope.row.statusName || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="110" />
      <el-table-column label="操作" width="420" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          <el-button size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="openDeleteConfirm(scope.row)">删除</el-button>
          <el-button size="small" type="success" v-if="scope.row.status!=='ACTIVE'" @click="doEnable(scope.row)">启用</el-button>
          <el-button size="small" type="warning" v-if="scope.row.status==='ACTIVE'" @click="doDisable(scope.row)">禁用</el-button>
          <el-button size="small" type="success" :disabled="scope.row.status!=='ACTIVE'" @click="openApply(scope.row)">应用生成排班</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display:flex; justify-content:flex-end; margin-top:12px;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10,20,50,100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="displayRules.length"
      />
    </div>

    <!-- 规则表单：新建/编辑 -->
    <el-drawer v-model="formVisible" :title="formMode==='create'?'新建规则':'编辑规则'" size="50%" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="form.ruleName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.ruleType" placeholder="选择类型">
            <el-option label="固定周排班" value="FIXED_WEEKLY" />
            <el-option label="轮班制" value="ROTATION" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model.number="form.departmentId" placeholder="请选择科室" clearable @change="handleDepartmentChange">
            <el-option v-for="d in departmentList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="门诊">
          <el-select v-model.number="form.clinicId" placeholder="请选择门诊" clearable>
            <el-option v-for="c in formClinics" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model.number="form.doctorId" placeholder="请选择医生" clearable @change="handleDoctorSelected">
            <el-option v-for="doc in formDoctors" :key="doc.id" :label="doc.name" :value="doc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="周几出诊">
          <el-checkbox-group v-model="weekDays">
            <el-checkbox v-for="(label,n) in weekOptions" :key="n" :label="Number(n)">{{ label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="时段">
          <el-checkbox-group v-model="timeSlots">
            <el-checkbox label="morning">上午</el-checkbox>
            <el-checkbox label="afternoon">下午</el-checkbox>
            <el-checkbox label="evening">晚间</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="号别">
          <el-radio-group v-model="form.slotType">
            <el-radio label="normal">普通号</el-radio>
            <el-radio label="expert">专家号</el-radio>
            <el-radio label="vip">特需号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="每次号源">
          <el-input-number v-model.number="form.totalSlots" :min="1" />
        </el-form-item>
        <el-form-item label="最多日排班数">
          <el-input-number v-model.number="form.maxDailySchedules" :min="1" />
        </el-form-item>
        <el-form-item label="最多连续天数">
          <el-input-number v-model.number="form.maxContinuousDays" :min="1" />
        </el-form-item>
        <el-form-item label="跳过周末">
          <el-switch v-model="form.skipWeekends" />
        </el-form-item>
        <el-form-item label="跳过节假日">
          <el-switch v-model="form.skipHolidays" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model.number="form.priority" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right">
          <el-button @click="formVisible=false">取消</el-button>
          <el-button type="primary" @click="submitForm">{{ formMode==='create'?'创建':'保存' }}</el-button>
          <el-button type="success" @click="submitAndEnable">保存并启用</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 应用规则对话框 -->
    <el-dialog v-model="applyVisible" title="应用规则生成排班" width="640px" class="apply-dialog">
      <div class="apply-intro">即将应用规则：{{ currentRuleData?.ruleName || '-' }}</div>
      <el-card shadow="never" class="apply-card">
        <div class="kv">
          <div class="kv-row">
            <div class="kv-label">应用日期范围</div>
            <div class="kv-value">
              <el-date-picker v-model="applyForm.applyStartDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
              <span style="margin:0 8px;">至</span>
              <el-date-picker v-model="applyForm.applyEndDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
              <span style="color:#909399; margin-left:8px;">必填</span>
            </div>
          </div>
          <div class="kv-row">
            <div class="kv-label">覆盖已有排班</div>
            <div class="kv-value"><el-checkbox v-model="applyForm.overwriteExisting">覆盖已有排班（谨慎使用）</el-checkbox></div>
          </div>
          <div class="kv-row">
            <div class="kv-label">排除日期（可选）</div>
            <div class="kv-value">
              <div style="display:flex; gap:8px; align-items:center; margin-bottom:8px;">
                <el-date-picker v-model="applyExcludeInput" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
                <el-button size="small" @click="addExcludeDate">添加日期</el-button>
              </div>
              <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <el-tag v-for="d in applyForm.excludeDates" :key="d" closable @close="removeExcludeDate(d)">{{ d }}</el-tag>
              </div>
            </div>
          </div>
          <div class="kv-row">
            <div class="kv-label">预计生成排班数</div>
            <div class="kv-value"><el-tag type="info">约 {{ estimatedApplyCount }} 条</el-tag></div>
          </div>
        </div>
      </el-card>
      <div class="apply-tips">
        <div style="margin-bottom:6px;">提示：</div>
        <div>- 生成的排班将立即生效</div>
        <div>- 如果选择覆盖，将删除该时间范围内的现有排班</div>
        <div>- 建议先检测冲突：<el-button size="small" @click="checkConflicts(currentRuleData)">检测冲突</el-button></div>
      </div>
      <template #footer>
        <div class="detail-footer">
          <el-button @click="applyVisible=false">取消</el-button>
          <el-button type="primary" @click="submitApply">确认生成</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 规则详情对话框 -->
    <el-dialog v-model="detailVisible" title="规则详情" width="720px" class="detail-dialog">
      <div class="detail-grid">
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <div class="kv">
            <div class="kv-row"><div class="kv-label">规则名称</div><div class="kv-value">{{ detailData.ruleName }}</div></div>
            <div class="kv-row"><div class="kv-label">规则类型</div><div class="kv-value">{{ mapRuleType(detailData.ruleType) }}</div></div>
            <div class="kv-row"><div class="kv-label">规则状态</div><div class="kv-value"><el-tag :type="statusTagType(detailData.status)">{{ detailData.statusName || detailData.status }}</el-tag></div></div>
            <div class="kv-row"><div class="kv-label">优先级</div><div class="kv-value">{{ detailData.priority }}</div></div>
            <div class="kv-row"><div class="kv-label">规则描述</div><div class="kv-value">{{ detailData.description || '-' }}</div></div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title">关联信息</div>
          <div class="kv">
            <div class="kv-row"><div class="kv-label">医生</div><div class="kv-value">{{ detailData.doctorName || '-' }}{{ detailData.doctorTitle ? `（${detailData.doctorTitle}）` : '' }}</div></div>
            <div class="kv-row"><div class="kv-label">科室</div><div class="kv-value">{{ detailData.departmentName || '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">门诊</div><div class="kv-value">{{ detailData.clinicName || '-' }}</div></div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title">时间配置</div>
          <div class="kv">
            <div class="kv-row"><div class="kv-label">星期</div><div class="kv-value">{{ detailData.weekDaysDisplay || getWeekDaysDisplay(detailData.weekDays) || '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">时段</div><div class="kv-value">{{ detailData.timeSlotsDisplay || getTimeSlotsDisplay(detailData.timeSlots) || '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">生效期间</div><div class="kv-value">{{ formatDate(detailData.startDate) || '-' }} 至 {{ formatDate(detailData.endDate) || '未设置' }}</div></div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title">号源配置</div>
          <div class="kv">
            <div class="kv-row"><div class="kv-label">号别</div><div class="kv-value">{{ detailData.slotTypeName || mapSlotType(detailData.slotType) }}</div></div>
            <div class="kv-row"><div class="kv-label">总号源数</div><div class="kv-value">{{ detailData.totalSlots }}</div></div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title">高级规则</div>
          <div class="kv">
            <div class="kv-row"><div class="kv-label">每天最多排班次数</div><div class="kv-value">{{ detailData.maxDailySchedules ?? '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">最多连续出诊天数</div><div class="kv-value">{{ detailData.maxContinuousDays ?? '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">跳过周末</div><div class="kv-value">{{ detailData.skipWeekends ? '是' : '否' }}</div></div>
            <div class="kv-row"><div class="kv-label">跳过节假日</div><div class="kv-value">{{ detailData.skipHolidays ? '是' : '否' }}</div></div>
          </div>
        </div>
        <div class="detail-section">
          <div class="section-title">审计信息</div>
          <div class="kv">
            <div class="kv-row"><div class="kv-label">创建人</div><div class="kv-value">{{ detailData.createdBy || '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">创建时间</div><div class="kv-value">{{ detailData.createdAt || '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">更新时间</div><div class="kv-value">{{ detailData.updatedAt || '-' }}</div></div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="detail-footer">
          <el-button @click="openEdit(detailData)">编辑</el-button>
          <el-button type="success" :disabled="detailData.status!=='ACTIVE'" @click="openApply(detailData)">应用规则</el-button>
          <el-button v-if="detailData.status!=='ACTIVE'" type="success" @click="doEnable(detailData)">启用</el-button>
          <el-button v-else type="warning" @click="doDisable(detailData)">禁用</el-button>
          <el-popconfirm title="确认删除该规则？" @confirm="doDelete(detailData)">
            <template #reference>
              <el-button type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </template>
    </el-dialog>

    <!-- 应用结果提示 -->
    <el-dialog v-model="applyResultVisible" title="排班生成结果" width="520px">
      <div style="display:flex; flex-direction:column; gap:8px; text-align:left;">
        <div style="font-size:16px;">✅ 排班生成完成</div>
        <div>成功生成：{{ applyResultData.successCount || 0 }} 条</div>
        <div>跳过：{{ applyResultData.skipCount || 0 }} 条（已存在）</div>
        <div>失败：{{ applyResultData.errorCount || 0 }} 条</div>
        <div v-if="(applyResultData.errors||[]).length" style="color:#909399;">可在日志查看失败详情</div>
      </div>
      <template #footer>
        <div class="detail-footer">
          <el-button type="primary" @click="goToAdminSchedule">查看生成的排班</el-button>
          <el-button @click="applyResultVisible=false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 冲突信息 -->
    <el-drawer v-model="conflictVisible" title="规则冲突" size="40%" destroy-on-close>
      <div v-if="conflicts?.length">
        <el-alert type="warning" :closable="false" title="存在冲突的规则如下" />
        <el-timeline>
          <el-timeline-item v-for="(c,idx) in conflicts" :key="idx" :timestamp="c.conflictRuleId">
            <p>{{ c.conflictRuleName }}：{{ c.reason }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else>
        <el-result icon="success" title="无冲突" sub-title="该规则与其他启用规则不存在冲突" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, CircleCheck, CloseBold, Timer } from '@element-plus/icons-vue'
import {
  listScheduleRules,
  getScheduleRuleById,
  getScheduleRuleDetail,
  createScheduleRule,
  updateScheduleRule,
  deleteScheduleRule,
  enableScheduleRule,
  disableScheduleRule,
  applyScheduleRule,
  detectScheduleRuleConflicts
} from '@/api/scheduleRule'
import { getDoctorList, getDoctorById } from '@/api/doctor'
import router from '@/router'
import { getDepartmentList } from '@/api/department'
import { getClinicList } from '@/api/clinic'

const loading = ref(false)
const rules = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const filterRuleType = ref('')
const filterDoctorId = ref(null)
const filterDepartmentId = ref(null)
const doctorList = ref([])
const departmentList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)

const formVisible = ref(false)
const formMode = ref('create') // create | edit
const form = ref({
  ruleName: '',
  ruleType: 'FIXED_WEEKLY',
  doctorId: null,
  departmentId: null,
  clinicId: null,
  slotType: 'normal',
  totalSlots: 20,
  maxDailySchedules: 2,
  maxContinuousDays: 5,
  skipWeekends: false,
  skipHolidays: false,
  startDate: new Date().toISOString().slice(0,10),
  endDate: '',
  priority: 0,
  description: ''
})
const formRef = ref()
const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }, { min: 1, max: 100, message: '长度不超过100字符', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  timeSlots: [{ validator: (_,_val,cb)=>{ if (!timeSlots.value.length) return cb(new Error('至少选择一个时段')); cb() }, trigger: 'change' }],
  totalSlots: [{ type: 'number', required: true, message: '请输入总号源数', trigger: 'change' }, { validator: (_,_val,cb)=>{ const v=form.value.totalSlots; if (v<1||v>100) return cb(new Error('总号源数需在1-100之间')); cb() }, trigger: 'change' }],
  endDate: [{ validator: (_,_val,cb)=>{ const s=form.value.startDate; const e=form.value.endDate; if (e && s && e<=s) return cb(new Error('结束日期必须晚于开始日期')); cb() }, trigger: 'change' }]
}

const weekDays = ref([])
const timeSlots = ref(['morning','afternoon'])
const weekOptions = {1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日'}

const applyVisible = ref(false)
const applyForm = ref({ applyStartDate: '', applyEndDate: '', overwriteExisting: false, excludeDates: [] })
const applyExcludeInput = ref('')
const currentRuleId = ref(null)

const conflictVisible = ref(false)
const conflicts = ref([])

const totalRulesCount = computed(() => Array.isArray(rules.value) ? rules.value.length : 0)
const activeRulesCount = computed(() => (rules.value||[]).filter(r => r.status === 'ACTIVE').length)
const inactiveRulesCount = computed(() => (rules.value||[]).filter(r => r.status === 'INACTIVE').length)
const expiredRulesCount = computed(() => (rules.value||[]).filter(r => (r.status === 'EXPIRED') || isExpiredByDate(r)).length)
const isExpiredByDate = (r) => {
  const e = formatDate(r.endDate)
  if (!e) return false
  const today = formatDate(new Date())
  return e < today
}

const displayRules = computed(() => {
  let list = rules.value
  if (filterStatus.value) {
    list = list.filter(r => r.status === filterStatus.value)
  }
  if (filterRuleType.value) {
    list = list.filter(r => normalizeRuleType(r.ruleType) === String(filterRuleType.value).toUpperCase())
  }
  if (filterDepartmentId.value) {
    const did = Number(filterDepartmentId.value)
    list = list.filter(r => Number(r.departmentId) === did || Number(r.department?.id) === did)
  }
  if (filterDoctorId.value) {
    const docId = Number(filterDoctorId.value)
    list = list.filter(r => Number(r.doctorId) === docId || Number(r.doctor?.id) === docId)
  }
  if (searchKeyword.value) {
    const k = searchKeyword.value.toLowerCase()
    list = list.filter(r => (r.ruleName||'').toLowerCase().includes(k) || (r.doctor?.name||'').toLowerCase().includes(k))
  }
  list = [...list].sort((a,b) => (Number(b.priority||0) - Number(a.priority||0)))
  return list
})

const pagedRules = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return displayRules.value.slice(start, start + pageSize.value)
})

const reload = async () => {
  loading.value = true
  try {
    const res = await listScheduleRules()
    rules.value = Array.isArray(res?.data) ? res.data : (res?.data || [])
  } catch (e) {
    ElMessage.error('加载规则失败')
  } finally { loading.value = false }
}

onMounted(async () => {
  await reload()
  try {
    const [docsRes, depsRes, clinicsRes] = await Promise.all([getDoctorList(), getDepartmentList(), getClinicList()])
    doctorList.value = Array.isArray(docsRes?.data) ? docsRes.data : (docsRes?.data?.list || [])
    departmentList.value = Array.isArray(depsRes?.data) ? depsRes.data : (depsRes?.data?.list || [])
    clinicList.value = Array.isArray(clinicsRes?.data) ? clinicsRes.data : (clinicsRes?.data?.list || [])
  } catch {}
})

const filteredDoctors = computed(() => {
  if (!filterDepartmentId.value) return doctorList.value
  const did = Number(filterDepartmentId.value)
  return doctorList.value.filter(d => Number(d.clinic?.departmentId || d.departmentId) === did)
})
const clinicList = ref([])
const formDoctors = computed(() => {
  if (!form.value.departmentId) return doctorList.value
  const did = Number(form.value.departmentId)
  return doctorList.value.filter(d => Number(d.clinic?.departmentId || d.departmentId) === did)
})
const formClinics = computed(() => {
  if (!form.value.departmentId) return clinicList.value
  const did = Number(form.value.departmentId)
  return clinicList.value.filter(c => Number(c.departmentId) === did)
})
const handleDepartmentChange = () => { form.value.doctorId = null; form.value.clinicId = null }
const handleDoctorSelected = async (val) => {
  try {
    const res = await getDoctorById(val)
    const d = res?.data || {}
    const deptId = Number(d?.clinic?.departmentId || d?.departmentId)
    const clinicId = Number(d?.clinic?.id || d?.clinicId)
    if (deptId) form.value.departmentId = deptId
    if (clinicId) form.value.clinicId = clinicId
  } catch {}
}

const doSearch = () => { currentPage.value = 1 }
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterRuleType.value = ''
  filterDoctorId.value = null
  filterDepartmentId.value = null
  currentPage.value = 1
}

const openCreate = () => {
  formMode.value = 'create'
  formVisible.value = true
  weekDays.value = []
  timeSlots.value = ['morning','afternoon']
}

const openEdit = async (row) => {
  try {
    const res = await getScheduleRuleById(row.id)
    const data = res?.data || row
    formMode.value = 'edit'
    formVisible.value = true
    form.value = {
      ruleName: data.ruleName,
      ruleType: data.ruleType,
      doctorId: data.doctorId,
      departmentId: data.departmentId,
      clinicId: data.clinicId,
      slotType: data.slotType,
      totalSlots: data.totalSlots,
      maxDailySchedules: data.maxDailySchedules,
      maxContinuousDays: data.maxContinuousDays,
      skipWeekends: !!data.skipWeekends,
      skipHolidays: !!data.skipHolidays,
      startDate: formatDate(data.startDate),
      endDate: formatDate(data.endDate),
      priority: data.priority,
      description: data.description
    }
    weekDays.value = normalizeWeekDays(data.weekDays)
    timeSlots.value = normalizeTimeSlots(data.timeSlots)
    currentRuleId.value = data.id
  } catch (e) {
    ElMessage.error('加载规则详情失败')
  }
}

const submitForm = async () => {
  if (formRef.value) {
    const ok = await formRef.value.validate().catch(()=>false)
    if (!ok) return
  }
  const payload = { ...form.value, weekDays: weekDays.value, timeSlots: timeSlots.value }
  try {
    if (formMode.value === 'create') {
      await createScheduleRule(payload)
      ElMessage.success('创建成功')
    } else {
      await updateScheduleRule(currentRuleId.value, payload)
      ElMessage.success('保存成功')
    }
    formVisible.value = false
    reload()
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

const submitAndEnable = async () => {
  if (formRef.value) {
    const ok = await formRef.value.validate().catch(()=>false)
    if (!ok) return
  }
  const payload = { ...form.value, weekDays: weekDays.value, timeSlots: timeSlots.value }
  try {
    let id = currentRuleId.value
    if (formMode.value === 'create') {
      const res = await createScheduleRule(payload)
      id = res?.data?.id || res?.id || id
    } else {
      await updateScheduleRule(currentRuleId.value, payload)
      id = currentRuleId.value
    }
    if (id) await enableScheduleRule(id)
    ElMessage.success('保存并启用成功')
    formVisible.value = false
    reload()
  } catch (e) { ElMessage.error('保存并启用失败') }
}

const doDelete = async (row) => {
  try {
    await deleteScheduleRule(row.id)
    ElMessage.success('删除成功')
    reload()
  } catch (e) { ElMessage.error('删除失败') }
}

const doEnable = async (row) => {
  try { await enableScheduleRule(row.id); ElMessage.success('已启用'); reload() } catch (e) { ElMessage.error('启用失败') }
}
const doDisable = async (row) => {
  try { await disableScheduleRule(row.id); ElMessage.success('已禁用'); reload() } catch (e) { ElMessage.error('禁用失败') }
}

const currentRuleData = ref(null)
const openApply = (row) => { currentRuleId.value = row.id; currentRuleData.value = row; applyVisible.value = true }
const estimatedApplyCount = computed(() => {
  const s = applyForm.value.applyStartDate
  const e = applyForm.value.applyEndDate
  if (!s || !e || !currentRuleData.value) return 0
  const days = []
  const start = new Date(s)
  const end = new Date(e)
  for (let d = new Date(start); d <= end; d.setDate(d.getDate()+1)) {
    const ds = new Date(d)
    const dow = ds.getDay() === 0 ? 7 : ds.getDay()
    const wd = normalizeWeekDays(currentRuleData.value.weekDays)
    const skipWeekend = !!currentRuleData.value.skipWeekends
    if (skipWeekend && (dow===6 || dow===7)) continue
    if (wd.length && !wd.includes(dow)) continue
    const exclude = Array.isArray(applyForm.value.excludeDates) ? applyForm.value.excludeDates : []
    const dateStr = `${ds.getFullYear()}-${String(ds.getMonth()+1).padStart(2,'0')}-${String(ds.getDate()).padStart(2,'0')}`
    if (exclude.includes(dateStr)) continue
    days.push(dateStr)
  }
  const ts = normalizeTimeSlots(currentRuleData.value.timeSlots)
  return days.length * (ts.length || 1)
})
const applyResultVisible = ref(false)
const applyResultData = ref({ successCount: 0, skipCount: 0, errorCount: 0, errors: [], message: '' })
const submitApply = async () => {
  try {
    const payload = { ...applyForm.value }
    if (!Array.isArray(payload.excludeDates) || payload.excludeDates.length === 0) {
      delete payload.excludeDates
    }
    const res = await applyScheduleRule(currentRuleId.value, payload)
    applyResultData.value = res?.data || {}
    applyVisible.value = false
    applyResultVisible.value = true
  } catch (e) { ElMessage.error('应用失败') }
}
const addExcludeDate = () => {
  const d = applyExcludeInput.value
  if (!d) return
  const list = applyForm.value.excludeDates || []
  if (!list.includes(d)) list.push(d)
  applyForm.value.excludeDates = list
  applyExcludeInput.value = ''
}
const removeExcludeDate = (d) => {
  applyForm.value.excludeDates = (applyForm.value.excludeDates || []).filter(x => x !== d)
}

const checkConflicts = async (row) => {
  try {
    const res = await detectScheduleRuleConflicts(row.id)
    conflicts.value = res?.data?.conflicts || res?.data || []
    conflictVisible.value = true
  } catch (e) { ElMessage.error('检测失败') }
}

const detailVisible = ref(false)
const detailData = ref({})
const viewDetail = async (row) => {
  try {
    const res = await getScheduleRuleById(row.id)
    detailData.value = res?.data || row
    detailVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

// 辅助显示
const statusTagType = (s) => s==='ACTIVE'?'success':(s==='INACTIVE'?'warning':'info')
const formatDate = (d) => {
  if (!d) return ''
  const dt = typeof d === 'string' ? d.split(' ')[0] : new Date(d)
  if (typeof dt === 'string') return dt
  const y = dt.getFullYear(), m = String(dt.getMonth()+1).padStart(2,'0'), da = String(dt.getDate()).padStart(2,'0')
  return `${y}-${m}-${da}`
}
const mapTimeSlot = (s) => ({ morning: '上午', afternoon: '下午', evening: '晚间' }[String(s).toLowerCase()] || s)
const mapSlotType = (s) => ({ normal:'普通', expert:'专家', vip:'特需' }[s] || s)
const normalizeRuleType = (t) => {
  const v = String(t||'').toUpperCase()
  if (v === 'FIXED_WEEKLY' || v === 'WEEKLY' || v === 'FIXED') return 'FIXED_WEEKLY'
  if (v === 'ROTATION' || v === 'ROTATING') return 'ROTATION'
  if (v === 'CUSTOM' || v === 'FLEXIBLE') return 'CUSTOM'
  return v
}
const mapRuleType = (t) => ({ FIXED_WEEKLY: '固定周排班', ROTATION: '轮班制', CUSTOM: '自定义' }[normalizeRuleType(t)] || t)
const normalizeWeekDays = (val) => Array.isArray(val) ? val : String(val||'').split(',').map(n=>parseInt(n)).filter(Boolean)
const normalizeTimeSlots = (val) => Array.isArray(val) ? val : String(val||'').split(',').filter(Boolean)
const getWeekDaysDisplay = (val) => normalizeWeekDays(val).map(n=>weekOptions[n]).join('、')
const getTimeSlotsDisplay = (val) => normalizeTimeSlots(val).map(s=>mapTimeSlot(s)).join('、')
</script>

<style scoped>
.schedule-rules-page { display: flex; flex-direction: column; gap: 16px; }
.header-card { display:flex; align-items:center; justify-content:space-between; padding:24px; background:#fff; border-radius:8px; box-shadow:0 2px 4px rgba(0,0,0,0.1); }
.header-left { display:flex; flex-direction:column; text-align:left; }
.header-title { margin:0 0 8px 0; font-size:24px; line-height:1.2; font-weight:600; color:#303133; text-align:left; }
.header-subtitle { margin:0; font-size:14px; color:#606266; text-align:left; }
.toolbar { display: grid; grid-template-columns: repeat(4, 1fr) auto auto; gap: 12px; align-items: center; }
.toolbar :deep(.el-select) { width: 100%; }
.toolbar :deep(.el-input) { width: 100%; }
.toolbar :deep(.el-button) { white-space: nowrap; }
.slot-chip { margin-right: 6px; }
.stats-row { margin-bottom: 8px; }
.stat-card { height: 110px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); transition: all .3s ease; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 20px rgba(0,0,0,0.15); }
.stat-content { display:flex; align-items:center; height:100%; padding:20px; }
.stat-icon { width:60px; height:60px; border-radius:50%; display:flex; align-items:center; justify-content:center; margin-right:16px; font-size:24px; color:#fff; }
.stat-icon.total { background: linear-gradient(135deg,#667eea 0%,#764ba2 100%); }
.stat-icon.week { background: linear-gradient(135deg,#4facfe 0%,#00f2fe 100%); }
.stat-icon.today { background: linear-gradient(135deg,#f093fb 0%,#f5576c 100%); }
.stat-icon.expired { background: linear-gradient(135deg,#ffd86f 0%,#fc6262 100%); }
.stat-info { flex:1; }
.stat-number { font-size:28px; font-weight:600; color:#303133; line-height:1; }
.stat-label { font-size:14px; color:#909399; margin-top:6px; }
.detail-dialog :deep(.el-dialog__body) { padding-top: 6px; }
.detail-grid { display: grid; grid-template-columns: 1fr; gap: 12px; }
.detail-section { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.section-title { padding: 12px 16px; font-weight: 600; border-bottom: 1px solid #ebeef5; text-align: left; }
.kv { padding: 12px 16px; }
.kv-row { display: flex; align-items: flex-start; gap: 12px; margin: 6px 0; }
.kv-label { width: 120px; color: #606266; text-align: left; }
.kv-value { flex: 1; color: #303133; text-align: left; }
.detail-footer { text-align: right; }
.apply-dialog :deep(.el-dialog__body) { padding-top: 6px; }
.apply-intro { text-align: left; margin-bottom: 8px; color: #303133; }
.apply-card { margin-bottom: 8px; }
.apply-tips { text-align: left; color: #606266; margin-top: 4px; }
</style>
const goToAdminSchedule = () => { router.push('/admin/schedule') }
const openDeleteConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该规则？', '提示', { type: 'warning', center: true, confirmButtonText: '删除', cancelButtonText: '取消' })
    await doDelete(row)
  } catch {}
}
