<template>
  <div class="schedule-rules-page">
    <div class="page-header">
      <h1>排班规则管理</h1>
      <p>制定、启用及应用排班规则模板</p>
    </div>

  <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="按规则名/医生筛选" style="width: 220px" clearable />
      <el-select v-model="filterDepartmentId" placeholder="科室" style="width: 180px" clearable>
        <el-option label="全部科室" :value="null" />
        <el-option v-for="d in departmentList" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="filterDoctorId" placeholder="医生" style="width: 180px" clearable>
        <el-option label="全部医生" :value="null" />
        <el-option v-for="doc in filteredDoctors" :key="doc.id" :label="doc.name" :value="doc.id" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" style="width: 140px" clearable>
        <el-option label="全部" value="" />
        <el-option label="启用(ACTIVE)" value="ACTIVE" />
        <el-option label="禁用(INACTIVE)" value="INACTIVE" />
        <el-option label="过期(EXPIRED)" value="EXPIRED" />
      </el-select>
      <el-select v-model="filterRuleType" placeholder="规则类型" style="width: 160px" clearable>
        <el-option label="全部" value="" />
        <el-option label="固定周排班" value="FIXED_WEEKLY" />
        <el-option label="轮班制" value="ROTATION" />
        <el-option label="自定义" value="CUSTOM" />
      </el-select>
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
      <el-button type="success" @click="openCreate">新建规则</el-button>
      <el-button @click="reload">刷新</el-button>
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
          <el-popconfirm title="确认删除该规则？" @confirm="doDelete(scope.row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
          <el-button size="small" type="success" v-if="scope.row.status!=='ACTIVE'" @click="doEnable(scope.row)">启用</el-button>
          <el-button size="small" type="warning" v-if="scope.row.status==='ACTIVE'" @click="doDisable(scope.row)">禁用</el-button>
          <el-dropdown>
            <el-button size="small">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :disabled="scope.row.status!=='ACTIVE'" @click="openApply(scope.row)">应用生成排班</el-dropdown-item>
                <el-dropdown-item @click="checkConflicts(scope.row)">检测冲突</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
            <el-option label="固定周排班(FIXED_WEEKLY)" value="FIXED_WEEKLY" />
            <el-option label="轮班制(ROTATION)" value="ROTATION" />
            <el-option label="自定义(CUSTOM)" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model.number="form.departmentId" placeholder="请选择科室" clearable @change="handleDepartmentChange">
            <el-option v-for="d in departmentList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model.number="form.doctorId" placeholder="请选择医生" :disabled="!form.departmentId" clearable>
            <el-option v-for="doc in formDoctors" :key="doc.id" :label="doc.name" :value="doc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="门诊">
          <el-select v-model.number="form.clinicId" placeholder="请选择门诊" clearable>
            <el-option v-for="c in formClinics" :key="c.id" :label="c.name" :value="c.id" />
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
    <el-dialog v-model="applyVisible" title="应用规则生成排班" width="560px">
      <el-form :model="applyForm" label-width="120px">
        <el-form-item label="开始日期">
          <el-date-picker v-model="applyForm.applyStartDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="applyForm.applyEndDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="覆盖已存在">
          <el-switch v-model="applyForm.overwriteExisting" />
        </el-form-item>
        <el-form-item label="排除日期">
          <el-date-picker v-model="applyForm.excludeDates" type="dates" value-format="YYYY-MM-DD" placeholder="选择多个日期" />
        </el-form-item>
        <el-form-item label="预计生成数">
          <el-tag type="info">约 {{ estimatedApplyCount }} 条</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="text-align:right">
          <el-button @click="applyVisible=false">取消</el-button>
          <el-button type="primary" @click="submitApply">应用</el-button>
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
import { getDoctorList } from '@/api/doctor'
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
const currentRuleId = ref(null)

const conflictVisible = ref(false)
const conflicts = ref([])

const displayRules = computed(() => {
  let list = rules.value
  if (filterStatus.value) {
    list = list.filter(r => r.status === filterStatus.value)
  }
  if (filterRuleType.value) {
    list = list.filter(r => String(r.ruleType).toUpperCase() === String(filterRuleType.value).toUpperCase())
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
  if (!form.value.departmentId) return []
  const did = Number(form.value.departmentId)
  return doctorList.value.filter(d => Number(d.clinic?.departmentId || d.departmentId) === did)
})
const formClinics = computed(() => {
  if (!form.value.departmentId) return clinicList.value
  const did = Number(form.value.departmentId)
  return clinicList.value.filter(c => Number(c.departmentId) === did)
})
const handleDepartmentChange = () => { form.value.doctorId = null; form.value.clinicId = null }

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
const submitApply = async () => {
  try {
    const payload = { ...applyForm.value }
    if (!Array.isArray(payload.excludeDates) || payload.excludeDates.length === 0) {
      delete payload.excludeDates
    }
    const res = await applyScheduleRule(currentRuleId.value, payload)
    const msg = res?.data?.message || '应用完成'
    ElMessage.success(msg)
    applyVisible.value = false
  } catch (e) { ElMessage.error('应用失败') }
}

const checkConflicts = async (row) => {
  try {
    const res = await detectScheduleRuleConflicts(row.id)
    conflicts.value = res?.data?.conflicts || res?.data || []
    conflictVisible.value = true
  } catch (e) { ElMessage.error('检测失败') }
}

const viewDetail = async (row) => {
  try {
    const res = await getScheduleRuleDetail(row.id)
    const d = res?.data || {}
    const detailText = `规则：${d.ruleName || row.ruleName}
类型：${d.ruleTypeName || mapRuleType(d.ruleType || row.ruleType)}
医生：${d.doctorName || row.doctor?.name || '-'}（${d.doctorTitle || ''}）
科室：${d.departmentName || row.department?.name || '-'}
门诊：${d.clinicName || '-'}
周几：${d.weekDaysDisplay || getWeekDaysDisplay(d.weekDays)}
时段：${d.timeSlotsDisplay || getTimeSlotsDisplay(d.timeSlots)}
号别：${d.slotTypeName || mapSlotType(d.slotType)}
号源：${d.totalSlots}
开始：${d.startDate || formatDate(row.startDate)}
结束：${d.endDate || formatDate(row.endDate) || '未设置'}
状态：${d.statusName || d.status || row.status}
优先级：${d.priority}
描述：${d.description || ''}`
    await ElMessageBox.alert(detailText, '规则详情', { confirmButtonText: '知道了' })
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
const mapRuleType = (t) => ({ FIXED_WEEKLY: '固定周排班', ROTATION: '轮班制', CUSTOM: '自定义' }[String(t).toUpperCase()] || t)
const normalizeWeekDays = (val) => Array.isArray(val) ? val : String(val||'').split(',').map(n=>parseInt(n)).filter(Boolean)
const normalizeTimeSlots = (val) => Array.isArray(val) ? val : String(val||'').split(',').filter(Boolean)
const getWeekDaysDisplay = (val) => normalizeWeekDays(val).map(n=>weekOptions[n]).join('、')
const getTimeSlotsDisplay = (val) => normalizeTimeSlots(val).map(s=>mapTimeSlot(s)).join('、')
</script>

<style scoped>
.schedule-rules-page { display: flex; flex-direction: column; gap: 16px; }
.page-header h1 { margin: 0; font-size: 22px; }
.page-header p { margin: 4px 0 0; color: #606266; }
.toolbar { display: flex; gap: 12px; align-items: center; }
.slot-chip { margin-right: 6px; }
</style>
