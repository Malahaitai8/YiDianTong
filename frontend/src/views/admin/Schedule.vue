<template>
  <div class="schedule-management">
    <!-- 操作栏 -->
    <el-card class="operation-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-date-picker
            v-model="selectedWeek"
            type="week"
            format="YYYY 第 ww 周"
            placeholder="选择周"
            @change="handleWeekChange"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="selectedDepartment"
            placeholder="选择科室"
            clearable
            @change="handleDepartmentChange"
          >
            <el-option label="全部科室" value="" />
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="儿科" value="儿科" />
            <el-option label="妇科" value="妇科" />
            <el-option label="骨科" value="骨科" />
          </el-select>
        </el-col>
        <el-col :span="10">
          <el-button type="primary" @click="openAddScheduleDialog">
            <el-icon><Plus /></el-icon>
            添加排班
          </el-button>
          <el-button @click="batchOperation">批量操作</el-button>
          <el-button @click="exportSchedule">导出排班表</el-button>
          <el-button @click="refreshSchedule">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 排班表格 -->
    <el-card class="schedule-card">
      <div class="schedule-header">
        <h3>{{ currentWeekText }} 排班表</h3>
        <div class="legend">
          <span class="legend-item">
            <span class="legend-color morning"></span>
            上午班
          </span>
          <span class="legend-item">
            <span class="legend-color afternoon"></span>
            下午班
          </span>
          <span class="legend-item">
            <span class="legend-color evening"></span>
            晚班
          </span>
          <span class="legend-item">
            <span class="legend-color full"></span>
            全天班
          </span>
        </div>
      </div>

      <div class="schedule-table">
        <table class="schedule-grid">
          <thead>
            <tr>
              <th class="doctor-column">医生/日期</th>
              <th v-for="day in weekDays" :key="day.date" class="day-column">
                <div class="day-header">
                  <div class="day-name">{{ day.name }}</div>
                  <div class="day-date">{{ day.date }}</div>
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="doctor in filteredDoctors" :key="doctor.id" class="doctor-row">
              <td class="doctor-info">
                <div class="doctor-name">{{ doctor.name }}</div>
                <div class="doctor-dept">{{ doctor.department }}</div>
              </td>
              <td v-for="day in weekDays" :key="`${doctor.id}-${day.date}`" class="schedule-cell">
                <div class="schedule-slots">
                  <div
                    v-for="schedule in getScheduleForDoctorAndDay(doctor.id, day.date)"
                    :key="schedule.id"
                    :class="['schedule-slot', schedule.period]"
                    @click="editSchedule(schedule)"
                  >
                    <div class="slot-time">{{ schedule.timeRange }}</div>
                    <div class="slot-room">{{ schedule.room }}</div>
                    <div class="slot-patients">{{ schedule.currentPatients }}/{{ schedule.maxPatients }}</div>
                    <div class="slot-actions">
                      <el-icon @click.stop="deleteSchedule(schedule)"><Delete /></el-icon>
                    </div>
                  </div>
                  <div
                    class="add-schedule-btn"
                    @click="addScheduleForDoctorAndDay(doctor.id, day.date)"
                  >
                    <el-icon><Plus /></el-icon>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <!-- 添加/编辑排班对话框 -->
    <el-dialog
      v-model="scheduleDialog.visible"
      :title="scheduleDialog.isEdit ? '编辑排班' : '添加排班'"
      width="500px"
      @close="resetScheduleForm"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleRules"
        label-width="100px"
      >
        <el-form-item label="医生" prop="doctorId">
          <el-select
            v-model="scheduleForm.doctorId"
            placeholder="请选择医生"
            style="width: 100%"
            :disabled="scheduleDialog.isEdit"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="`${doctor.name} - ${doctor.department}`"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="scheduleForm.date"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            :disabled="scheduleDialog.isEdit"
          />
        </el-form-item>

        <el-form-item label="时间段" prop="period">
          <el-select
            v-model="scheduleForm.period"
            placeholder="请选择时间段"
            style="width: 100%"
            @change="handlePeriodChange"
          >
            <el-option label="上午班 (08:00-12:00)" value="morning" />
            <el-option label="下午班 (14:00-18:00)" value="afternoon" />
            <el-option label="晚班 (18:00-22:00)" value="evening" />
            <el-option label="全天班 (08:00-18:00)" value="full" />
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="scheduleForm.startTime"
                placeholder="开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="scheduleForm.endTime"
                placeholder="结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="诊室" prop="room">
              <el-select v-model="scheduleForm.room" placeholder="请选择诊室">
                <el-option label="诊室1" value="诊室1" />
                <el-option label="诊室2" value="诊室2" />
                <el-option label="诊室3" value="诊室3" />
                <el-option label="诊室4" value="诊室4" />
                <el-option label="诊室5" value="诊室5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大患者数" prop="maxPatients">
              <el-input-number
                v-model="scheduleForm.maxPatients"
                :min="1"
                :max="50"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="scheduleForm.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scheduleDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveScheduleForm" :loading="scheduleDialog.loading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量操作对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      title="批量操作"
      width="600px"
    >
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchForm.operation">
            <el-radio label="copy">复制排班</el-radio>
            <el-radio label="delete">删除排班</el-radio>
            <el-radio label="modify">修改排班</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="batchForm.operation === 'copy'" label="源周">
          <el-date-picker
            v-model="batchForm.sourceWeek"
            type="week"
            placeholder="选择源周"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="batchForm.operation === 'copy'" label="目标周">
          <el-date-picker
            v-model="batchForm.targetWeek"
            type="week"
            placeholder="选择目标周"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="科室">
          <el-select
            v-model="batchForm.departments"
            multiple
            placeholder="选择科室"
            style="width: 100%"
          >
            <el-option label="内科" value="内科" />
            <el-option label="外科" value="外科" />
            <el-option label="儿科" value="儿科" />
            <el-option label="妇科" value="妇科" />
            <el-option label="骨科" value="骨科" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="executeBatchOperation">
            执行操作
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate, getWeekRange } from '@/utils'

const selectedWeek = ref(new Date())
const selectedDepartment = ref('')

// 医生数据
const doctors = ref([
  { id: 1, name: '张医生', department: '内科' },
  { id: 2, name: '李医生', department: '外科' },
  { id: 3, name: '王医生', department: '儿科' },
  { id: 4, name: '赵医生', department: '妇科' },
  { id: 5, name: '陈医生', department: '骨科' }
])

// 排班数据
const schedules = ref([
  {
    id: 1,
    doctorId: 1,
    date: '2024-01-15',
    period: 'morning',
    startTime: '08:00',
    endTime: '12:00',
    timeRange: '08:00-12:00',
    room: '诊室1',
    maxPatients: 20,
    currentPatients: 8,
    notes: ''
  },
  {
    id: 2,
    doctorId: 1,
    date: '2024-01-16',
    period: 'afternoon',
    startTime: '14:00',
    endTime: '18:00',
    timeRange: '14:00-18:00',
    room: '诊室1',
    maxPatients: 15,
    currentPatients: 5,
    notes: ''
  },
  {
    id: 3,
    doctorId: 2,
    date: '2024-01-15',
    period: 'full',
    startTime: '08:00',
    endTime: '18:00',
    timeRange: '08:00-18:00',
    room: '诊室2',
    maxPatients: 30,
    currentPatients: 12,
    notes: ''
  }
])

// 计算当前周的日期
const weekDays = computed(() => {
  const { start } = getWeekRange(selectedWeek.value)
  const days = []
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    days.push({
      name: dayNames[i],
      date: formatDate(date, 'YYYY-MM-DD')
    })
  }
  
  return days
})

const currentWeekText = computed(() => {
  const { start, end } = getWeekRange(selectedWeek.value)
  return `${formatDate(start, 'YYYY年MM月DD日')} - ${formatDate(end, 'YYYY年MM月DD日')}`
})

// 过滤医生
const filteredDoctors = computed(() => {
  if (!selectedDepartment.value) {
    return doctors.value
  }
  return doctors.value.filter(doctor => doctor.department === selectedDepartment.value)
})

// 排班对话框
const scheduleDialog = reactive({
  visible: false,
  isEdit: false,
  loading: false
})

const scheduleFormRef = ref()
const scheduleForm = reactive({
  id: null,
  doctorId: null,
  date: null,
  period: '',
  startTime: null,
  endTime: null,
  room: '',
  maxPatients: 20,
  notes: ''
})

const scheduleRules = {
  doctorId: [
    { required: true, message: '请选择医生', trigger: 'change' }
  ],
  date: [
    { required: true, message: '请选择日期', trigger: 'change' }
  ],
  period: [
    { required: true, message: '请选择时间段', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  room: [
    { required: true, message: '请选择诊室', trigger: 'change' }
  ],
  maxPatients: [
    { required: true, message: '请输入最大患者数', trigger: 'blur' }
  ]
}

// 批量操作对话框
const batchDialog = reactive({
  visible: false
})

const batchForm = reactive({
  operation: 'copy',
  sourceWeek: null,
  targetWeek: null,
  departments: []
})

const getScheduleForDoctorAndDay = (doctorId, date) => {
  return schedules.value.filter(schedule => 
    schedule.doctorId === doctorId && schedule.date === date
  )
}

const handleWeekChange = () => {
  // 重新加载排班数据
  loadScheduleData()
}

const handleDepartmentChange = () => {
  // 科室筛选已通过计算属性实现
}

const openAddScheduleDialog = () => {
  scheduleDialog.isEdit = false
  scheduleDialog.visible = true
}

const addScheduleForDoctorAndDay = (doctorId, date) => {
  scheduleDialog.isEdit = false
  scheduleDialog.visible = true
  scheduleForm.doctorId = doctorId
  scheduleForm.date = new Date(date)
}

const editSchedule = (schedule) => {
  scheduleDialog.isEdit = true
  scheduleDialog.visible = true
  
  Object.assign(scheduleForm, {
    id: schedule.id,
    doctorId: schedule.doctorId,
    date: new Date(schedule.date),
    period: schedule.period,
    startTime: new Date(`2000-01-01 ${schedule.startTime}`),
    endTime: new Date(`2000-01-01 ${schedule.endTime}`),
    room: schedule.room,
    maxPatients: schedule.maxPatients,
    notes: schedule.notes || ''
  })
}

const deleteSchedule = async (schedule) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个排班吗？',
      '确认删除',
      { type: 'warning' }
    )
    
    const index = schedules.value.findIndex(s => s.id === schedule.id)
    if (index !== -1) {
      schedules.value.splice(index, 1)
      ElMessage.success('排班删除成功')
    }
  } catch {
    // 用户取消
  }
}

const handlePeriodChange = (period) => {
  const timeMap = {
    'morning': { start: '08:00', end: '12:00' },
    'afternoon': { start: '14:00', end: '18:00' },
    'evening': { start: '18:00', end: '22:00' },
    'full': { start: '08:00', end: '18:00' }
  }
  
  if (timeMap[period]) {
    scheduleForm.startTime = new Date(`2000-01-01 ${timeMap[period].start}`)
    scheduleForm.endTime = new Date(`2000-01-01 ${timeMap[period].end}`)
  }
}

const resetScheduleForm = () => {
  scheduleFormRef.value?.resetFields()
  Object.assign(scheduleForm, {
    id: null,
    doctorId: null,
    date: null,
    period: '',
    startTime: null,
    endTime: null,
    room: '',
    maxPatients: 20,
    notes: ''
  })
}

const saveScheduleForm = async () => {
  try {
    await scheduleFormRef.value.validate()
    scheduleDialog.loading = true

    const startTime = scheduleForm.startTime.toTimeString().slice(0, 5)
    const endTime = scheduleForm.endTime.toTimeString().slice(0, 5)
    const timeRange = `${startTime}-${endTime}`

    const scheduleData = {
      doctorId: scheduleForm.doctorId,
      date: formatDate(scheduleForm.date, 'YYYY-MM-DD'),
      period: scheduleForm.period,
      startTime,
      endTime,
      timeRange,
      room: scheduleForm.room,
      maxPatients: scheduleForm.maxPatients,
      currentPatients: 0,
      notes: scheduleForm.notes
    }

    // TODO: 调用API保存数据
    await new Promise(resolve => setTimeout(resolve, 1000))

    if (scheduleDialog.isEdit) {
      // 编辑模式
      const index = schedules.value.findIndex(s => s.id === scheduleForm.id)
      if (index !== -1) {
        Object.assign(schedules.value[index], scheduleData)
      }
      ElMessage.success('排班更新成功')
    } else {
      // 添加模式
      const newSchedule = {
        ...scheduleData,
        id: Date.now()
      }
      schedules.value.push(newSchedule)
      ElMessage.success('排班添加成功')
    }

    scheduleDialog.visible = false
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    scheduleDialog.loading = false
  }
}

const batchOperation = () => {
  batchDialog.visible = true
}

const executeBatchOperation = async () => {
  try {
    // TODO: 实现批量操作逻辑
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('批量操作执行成功')
    batchDialog.visible = false
  } catch (error) {
    ElMessage.error('批量操作失败')
  }
}

const exportSchedule = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中')
}

const refreshSchedule = () => {
  loadScheduleData()
  ElMessage.success('数据已刷新')
}

const loadScheduleData = () => {
  // TODO: 根据选中的周加载排班数据
}

onMounted(() => {
  loadScheduleData()
})
</script>

<style scoped>
.schedule-management {
  padding: 0;
}

.operation-card {
  margin-bottom: 20px;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.schedule-header h3 {
  margin: 0;
  color: #303133;
}

.legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #606266;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 5px;
}

.legend-color.morning {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.legend-color.afternoon {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.legend-color.evening {
  background-color: #fff2e8;
  border: 1px solid #ffbb96;
}

.legend-color.full {
  background-color: #f9f0ff;
  border: 1px solid #d3adf7;
}

.schedule-table {
  overflow-x: auto;
}

.schedule-grid {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.schedule-grid th,
.schedule-grid td {
  border: 1px solid #e8e8e8;
  padding: 0;
  vertical-align: top;
}

.schedule-grid th {
  background-color: #fafafa;
  height: 60px;
}

.doctor-column {
  width: 120px;
  min-width: 120px;
}

.day-column {
  width: calc((100% - 120px) / 7);
  min-width: 100px;
}

.day-header {
  text-align: center;
  padding: 10px;
}

.day-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.day-date {
  font-size: 12px;
  color: #909399;
}

.doctor-row {
  height: 120px;
}

.doctor-info {
  padding: 15px 10px;
  background-color: #fafafa;
  text-align: center;
}

.doctor-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.doctor-dept {
  font-size: 12px;
  color: #909399;
}

.schedule-cell {
  padding: 5px;
  position: relative;
  height: 120px;
}

.schedule-slots {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.schedule-slot {
  flex: 1;
  padding: 5px;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  font-size: 11px;
  line-height: 1.2;
  transition: all 0.2s;
}

.schedule-slot:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.schedule-slot.morning {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
}

.schedule-slot.afternoon {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.schedule-slot.evening {
  background-color: #fff2e8;
  border: 1px solid #ffbb96;
  color: #fa8c16;
}

.schedule-slot.full {
  background-color: #f9f0ff;
  border: 1px solid #d3adf7;
  color: #722ed1;
}

.slot-time {
  font-weight: 600;
  margin-bottom: 2px;
}

.slot-room {
  margin-bottom: 2px;
}

.slot-patients {
  font-size: 10px;
  opacity: 0.8;
}

.slot-actions {
  position: absolute;
  top: 2px;
  right: 2px;
  opacity: 0;
  transition: opacity 0.2s;
}

.schedule-slot:hover .slot-actions {
  opacity: 1;
}

.slot-actions .el-icon {
  cursor: pointer;
  color: #f56c6c;
  font-size: 12px;
}

.add-schedule-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  color: #999;
  transition: all 0.2s;
}

.add-schedule-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.add-schedule-btn .el-icon {
  font-size: 14px;
}
</style>