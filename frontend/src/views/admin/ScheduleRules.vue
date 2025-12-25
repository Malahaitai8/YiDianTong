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
        <el-option label="全部科室" :value="''" />
        <el-option v-for="d in departmentList" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="filterDoctorId" placeholder="医生" style="width: 100%" clearable>
        <el-option label="全部医生" :value="''" />
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
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column label="规则名称" width="180">
        <template #default="scope">
          <el-link type="primary" @click="viewDetail(scope.row)">{{ scope.row.ruleName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="规则类型" width="110">
        <template #default="scope">
          <el-tag>{{ mapRuleType(scope.row.ruleType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="医生/科室" width="150">
        <template #default="scope">
          <div>
            <span>{{ scope.row.doctorName || scope.row.doctor?.name || scope.row.departmentName || scope.row.department?.name || '-' }}</span>
            <span v-if="(scope.row.doctorName||scope.row.doctor?.name) && (scope.row.departmentName||scope.row.department?.name)" style="color:#909399">（{{ scope.row.departmentName || scope.row.department?.name }}）</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ scope.row.statusName || scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column label="操作" min-width="420">
        <template #default="scope">
          <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          <el-button size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除该规则？" width="260" popper-class="popconfirm-wide" @confirm="doDelete(scope.row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
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
    <el-drawer v-model="formVisible" :title="formMode==='create'?'创建排班规则':'编辑排班规则'" size="50%" destroy-on-close class="rule-form-drawer">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="140px">
        <div class="form-section">
          <div class="section-title">基本信息 <span class="required-legend">* 为必填项</span></div>
          <div class="kv">
            <div class="kv-row">
              <div class="kv-label">规则名称<span class="required-mark">*</span></div>
              <div class="kv-value">
                <el-input 
                  v-model="form.ruleName" 
                  placeholder="请输入规则名称" 
                  maxlength="30"
                  show-word-limit
                  clearable
                  @input="validateRuleName"
                />
                <div v-if="ruleNameError" class="error-message">{{ ruleNameError }}</div>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">规则类型<span class="required-mark">*</span></div>
              <div class="kv-value">
                <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width:100%">
                  <el-option label="固定周排班" value="FIXED_WEEKLY" />
                  <el-option label="轮班制" value="ROTATION" />
                  <el-option label="自定义" value="CUSTOM" />
                </el-select>
                <div v-if="ruleTypeError" class="error-message">{{ ruleTypeError }}</div>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">规则描述</div>
              <div class="kv-value">
                <el-input 
                  v-model="form.description" 
                  placeholder="可填写规则说明" 
                  type="textarea"
                  maxlength="200"
                  show-word-limit
                  :rows="3"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">关联信息</div>
          <div class="kv">
            <div class="kv-row">
              <div class="kv-label">科室</div>
              <div class="kv-value">
                <el-select v-model.number="form.departmentId" placeholder="请选择科室（可选）" clearable @change="handleDepartmentChange" style="width:100%">
                  <el-option v-for="d in departmentList" :key="d.id" :label="d.name" :value="d.id" />
                </el-select>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">门诊</div>
              <div class="kv-value">
                <el-select v-model.number="form.clinicId" placeholder="请选择门诊（可选）" clearable style="width:100%">
                  <el-option v-for="c in formClinics" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">医生</div>
              <div class="kv-value">
                <el-select 
                  v-model.number="form.doctorId" 
                  placeholder="请选择医生（可选）" 
                  clearable 
                  @change="handleDoctorSelected" 
                  style="width:100%"
                >
                  <el-option v-for="doc in formDoctors" :key="doc.id" :label="doc.name" :value="doc.id" />
                </el-select>
              </div>
            </div>
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">时间配置</div>
          <div class="kv">
            <div class="kv-row">
              <div class="kv-label">星期<span class="required-mark">*</span></div>
              <div class="kv-value">
                <el-checkbox-group v-model="weekDays" @change="validateWeekDays">
                  <div class="weekday-rows">
                    <div class="weekday-row">
                      <el-checkbox v-for="n in [1,2,3,4,5]" :key="n" :label="n" class="weekday-checkbox">
                        {{ weekOptions[n] }}
                      </el-checkbox>
                    </div>
                    <div class="weekday-row">
                      <el-checkbox v-for="n in [6,7]" :key="n" :label="n" class="weekday-checkbox">
                        {{ weekOptions[n] }}
                      </el-checkbox>
                    </div>
                  </div>
                </el-checkbox-group>
                <div v-if="weekDaysError" class="error-message">{{ weekDaysError }}</div>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">时段<span class="required-mark">*</span></div>
              <div class="kv-value">
                <el-checkbox-group v-model="timeSlots" @change="validateTimeSlots">
                  <el-checkbox label="morning">上午</el-checkbox>
                  <el-checkbox label="afternoon">下午</el-checkbox>
                  <el-checkbox label="evening">晚间</el-checkbox>
                </el-checkbox-group>
                <div v-if="timeSlotsError" class="error-message">{{ timeSlotsError }}</div>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">生效日期<span class="required-mark">*</span></div>
              <div class="kv-value">
                <div style="display:flex; align-items:center; gap:8px;">
                  <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" :disabled-date="disableBeforeToday" @change="validateStartDate" />
                  <span>至</span>
                  <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期（可选）" :disabled-date="disableEndBeforeStart" @change="validateStartDate" />
                </div>
                <div v-if="startDateError" class="error-message">{{ startDateError }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">号源配置</div>
          <div class="kv">
            <div class="kv-row">
              <div class="kv-label">号别类型<span class="required-mark">*</span></div>
              <div class="kv-value">
                <el-radio-group v-model="form.slotType" @change="validateSlotType">
                  <el-radio label="normal">普通号</el-radio>
                  <el-radio label="expert">专家号</el-radio>
                  <el-radio label="vip">特需号</el-radio>
                </el-radio-group>
                <div v-if="slotTypeError" class="error-message">{{ slotTypeError }}</div>
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label">总号源数<span class="required-mark">*</span></div>
              <div class="kv-value">
                <el-input-number 
                  v-model.number="form.totalSlots" 
                  :min="1" 
                  :max="100" 
                  :precision="0"
                  @change="validateTotalSlots"
                />
                <div class="hint-text">号源不超过100个</div>
                <div v-if="totalSlotsError" class="error-message">{{ totalSlotsError }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-section">
          <div class="section-title">高级规则</div>
          <div class="kv">
            <div class="kv-row">
              <div class="kv-label single-line">每天最多排班次数</div>
              <div class="kv-value">
                <el-input-number v-model.number="form.maxDailySchedules" :min="1" :max="10" :precision="0" />
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label single-line">最多连续出诊天数</div>
              <div class="kv-value">
                <el-input-number v-model.number="form.maxContinuousDays" :min="1" :max="30" :precision="0" />
              </div>
            </div>
            <div class="kv-row">
              <div class="kv-label single-line">跳过周末</div>
              <div class="kv-value"><el-switch v-model="form.skipWeekends" /></div>
            </div>
            <div class="kv-row">
              <div class="kv-label single-line">跳过节假日</div>
              <div class="kv-value"><el-switch v-model="form.skipHolidays" /></div>
            </div>
            <div class="kv-row">
              <div class="kv-label single-line">优先级</div>
              <div class="kv-value">
                <el-input-number v-model.number="form.priority" :min="0" :max="100" :precision="0" />
              </div>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="detail-footer">
          <el-button @click="formVisible=false">取消</el-button>
          <el-button 
            v-if="formMode==='create'" 
            type="success" 
            @click="submitAndEnable"
            :loading="formSubmitting"
          >
            保存并启用
          </el-button>
          <el-button 
            v-else 
            type="primary" 
            @click="submitForm"
            :loading="formSubmitting"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 应用规则对话框 -->
    <el-dialog v-model="applyVisible" title="应用规则生成排班" width="640px" class="apply-dialog" :close-on-click-modal="false">
      <div class="apply-intro">即将应用规则：{{ currentRuleData?.ruleName || '-' }}</div>
      <el-card shadow="never" class="apply-card">
        <div class="apply-form-container">
          <div class="form-row">
            <div class="form-label">应用日期范围：</div>
            <div class="form-value">
              <div class="date-range">
                <el-date-picker v-model="applyForm.applyStartDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期（可选）" clearable :disabled-date="disableApplyStartDate" />
                <span class="date-separator">至</span>
                <el-date-picker v-model="applyForm.applyEndDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期（可选）" clearable :disabled-date="disableApplyEndDate" />
              </div>
              <div class="hint-text" style="margin-top: 4px;">不填则使用规则自身的日期范围。可填写部分日期来缩小应用范围。</div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-label">是否覆盖已有排班：</div>
            <div class="form-value">
              <el-radio-group v-model="applyForm.overwriteExisting">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-label">排除日期（可选）：</div>
            <div class="form-value">
              <div class="exclude-date-input">
                <el-date-picker v-model="applyExcludeInput" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" :disabled-date="disableExcludeDate" />
                <el-button size="small" @click="addExcludeDate" class="add-date-btn">+ 添加日期</el-button>
              </div>
              <div class="exclude-date-tags">
                <el-tag v-for="d in applyForm.excludeDates" :key="d" closable @close="removeExcludeDate(d)" class="exclude-date-tag">
                  × {{ d }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-label">预计生成排班数：</div>
            <div class="form-value">
              <el-tag type="info" class="estimate-tag">约 {{ estimatedApplyCount }} 条</el-tag>
              <span class="estimate-desc">（根据规则和日期范围自动计算）</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <div class="apply-tips">
        <div class="tips-title">⚠️ 提示：</div>
        <div class="tips-content">
          <div> 生成的排班将立即生效</div>
          <div> 如果选择覆盖，将删除该时间范围内的现有排班</div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span> 建议先检测冲突：</span>
            <el-button size="small" @click="checkConflicts(currentRuleData)" class="conflict-btn" :loading="conflictChecking">检测冲突</el-button>
            <span v-if="conflictCheckResult === 'success'" style="color: #67c23a; font-size: 14px;">✔ 无冲突</span>
            <el-popover v-else-if="conflictCheckResult === 'conflict'" placement="top" :width="300" trigger="hover">
              <template #reference>
                <span style="color: #f56c6c; font-size: 14px; cursor: pointer;">⚠️ 存在 {{ conflicts.length }} 个冲突</span>
              </template>
              <div style="max-height: 300px; overflow-y: auto;">
                <div v-for="(c, idx) in conflicts" :key="idx" style="margin-bottom: 8px; padding: 8px; background: #fef0f0; border-radius: 4px;">
                  <div style="font-weight: 600; color: #f56c6c;">{{ c.conflictRuleName }}</div>
                  <div style="font-size: 12px; color: #909399; margin-top: 4px;">{{ c.reason }}</div>
                </div>
              </div>
            </el-popover>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applyVisible=false">取消</el-button>
          <el-button type="primary" @click="submitApply">确认生成</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 规则详情抽屉 -->
    <el-drawer v-model="detailVisible" title="规则详情" size="600px" direction="rtl">
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
            <div class="kv-row"><div class="kv-label">创建人</div><div class="kv-value">{{ detailData.createdByName || detailData.createdBy || '-' }}</div></div>
            <div class="kv-row"><div class="kv-label">创建时间</div><div class="kv-value">{{ detailData.createdAt || '-' }}</div></div>
            <div class="kv-row" v-if="detailData.updatedAt && detailData.updatedAt !== detailData.createdAt"><div class="kv-label">更新时间</div><div class="kv-value">{{ detailData.updatedAt }}</div></div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="detail-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="openEdit(detailData)">编辑</el-button>
          <el-button v-if="detailData.status!=='ACTIVE'" type="success" @click="doEnable(detailData)">启用</el-button>
          <el-popconfirm title="确认删除该规则？" width="260" popper-class="popconfirm-wide" @confirm="doDelete(detailData)">
            <template #reference>
              <el-button type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </template>
    </el-drawer>

    <!-- 应用结果提示 -->
    <el-dialog v-model="applyResultVisible" title="排班生成结果" width="560px" class="result-dialog" :close-on-click-modal="false">
      <div class="result-content-new">
        <!-- 成功图标和标题 -->
        <div class="result-header-new">
          <div class="success-icon-wrapper">
            <el-icon class="success-icon-new"><CircleCheck /></el-icon>
          </div>
          <div class="result-title-new">排班生成成功</div>
          <div class="result-subtitle">所有数据已成功处理</div>
        </div>
        
        <!-- 统计卡片 -->
        <div class="result-stats-cards">
          <div class="stat-card stat-success">
            <div class="stat-card-label">成功生成</div>
            <div class="stat-card-value">{{ applyResultData.successCount || 0 }}<span class="stat-unit">条</span></div>
          </div>
          <div class="stat-card stat-skip">
            <div class="stat-card-label">跳过</div>
            <div class="stat-card-value">{{ applyResultData.skipCount || 0 }}<span class="stat-unit">条</span></div>
            <div class="stat-card-note">已存在</div>
          </div>
          <div class="stat-card stat-error">
            <div class="stat-card-label">失败</div>
            <div class="stat-card-value">{{ applyResultData.errorCount || 0 }}<span class="stat-unit">条</span></div>
          </div>
        </div>
        
        <!-- 按钮 -->
        <div class="result-actions">
          <el-button type="primary" size="large" @click="goToAdminSchedule" class="view-schedule-btn">查看生成的排班</el-button>
          <el-button size="large" @click="applyResultVisible=false" class="close-btn">关闭</el-button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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
const formSubmitting = ref(false)
const form = ref({
  ruleName: '',
  ruleType: 'FIXED_WEEKLY',
  doctorId: null,
  departmentId: null,
  clinicId: null,
  slotType: 'normal',
  totalSlots: 20,
  maxDailySchedules: null,
  maxContinuousDays: null,
  skipWeekends: false,
  skipHolidays: false,
  startDate: new Date().toISOString().slice(0,10),
  endDate: null,
  priority: 0,
  description: ''
})
const formRef = ref()
const formRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 30, message: '规则名称为2–30个字符', trigger: 'blur' }
  ],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  weekDays: [{ validator: (_,_val,cb)=>{ const rt = normalizeRuleType(form.value.ruleType); if (rt==='FIXED_WEEKLY' && !weekDays.value.length) return cb(new Error('请至少选择一天进行排班')); cb() }, trigger: 'change' }],
  totalSlots: [
    { type: 'number', required: true, message: '请输入总号源数', trigger: 'change' },
    { validator: (_,_val,cb)=>{ const v=form.value.totalSlots; if (!Number.isInteger(v) || v<1||v>100) return cb(new Error('号源个数为1–100的整数')); cb() }, trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' },
    { validator: (_,_val,cb)=>{ const s=form.value.startDate; const today = new Date().toISOString().slice(0,10); if (s && s < today) return cb(new Error('开始日期不能早于今天')); cb() }, trigger: 'change' }
  ],
  endDate: [{ validator: (_,_val,cb)=>{ const s=form.value.startDate; const e=form.value.endDate; if (e && s && e<=s) return cb(new Error('结束日期必须晚于开始日期')); cb() }, trigger: 'change' }],
  maxDailySchedules: [{ validator: (_,_val,cb)=>{ const v=form.value.maxDailySchedules; if (v==null||v==='') return cb(); if (!Number.isInteger(v) || v<1||v>10) return cb(new Error('每天最多排班次数需为1-10的整数')); cb() }, trigger: 'change' }],
  maxContinuousDays: [{ validator: (_,_val,cb)=>{ const v=form.value.maxContinuousDays; if (v==null||v==='') return cb(); if (!Number.isInteger(v) || v<1||v>30) return cb(new Error('最多连续出诊天数需为1-30的整数')); cb() }, trigger: 'change' }],
  priority: [{ validator: (_,_val,cb)=>{ const v=form.value.priority; if (!Number.isInteger(v) || v<0||v>100) return cb(new Error('优先级需为0-100的整数')); cb() }, trigger: 'change' }],
  description: [{ max: 200, message: '描述不超过200字符', trigger: 'blur' }]
}

const weekDays = ref([])
const timeSlots = ref([])
const weekOptions = {1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日'}

const applyVisible = ref(false)
const applyForm = ref({ applyStartDate: '', applyEndDate: '', overwriteExisting: false, excludeDates: [] })
const applyExcludeInput = ref('')
const currentRuleId = ref(null)

const conflictVisible = ref(false)
const conflicts = ref([])
const conflictChecking = ref(false)
const conflictCheckResult = ref('') // 'success' | 'conflict' | ''

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
const handleDepartmentChange = () => { 
  // 切换科室时，如果当前选择的医生/门诊不属于新科室，则清空
  if (form.value.doctorId) {
    const doctor = doctorList.value.find(d => d.id === form.value.doctorId)
    if (doctor && form.value.departmentId && Number(doctor.clinic?.departmentId || doctor.departmentId) !== Number(form.value.departmentId)) {
      form.value.doctorId = null
    }
  }
  if (form.value.clinicId) {
    const clinic = clinicList.value.find(c => c.id === form.value.clinicId)
    if (clinic && form.value.departmentId && Number(clinic.departmentId) !== Number(form.value.departmentId)) {
      form.value.clinicId = null
    }
  }
}
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
  // 重置表单为初始值
  form.value = {
    ruleName: '',
    ruleType: 'FIXED_WEEKLY',
    doctorId: null,
    departmentId: null,
    clinicId: null,
    slotType: 'normal',
    totalSlots: 20,
    maxDailySchedules: null,
    maxContinuousDays: null,
    skipWeekends: false,
    skipHolidays: false,
    startDate: new Date().toISOString().slice(0,10),
    endDate: null,
    priority: 0,
    description: ''
  }
  weekDays.value = []
  timeSlots.value = []
  currentRuleId.value = null
  // 重置验证错误
  ruleNameError.value = ''
  ruleTypeError.value = ''
  weekDaysError.value = ''
  timeSlotsError.value = ''
  slotTypeError.value = ''
  totalSlotsError.value = ''
  startDateError.value = ''
}

const openEdit = async (row) => {
  try {
    const res = await getScheduleRuleById(row.id)
    const data = res?.data || row
    formMode.value = 'edit'
    formVisible.value = true
    form.value = {
      ruleName: data.ruleName,
      ruleType: normalizeRuleType(data.ruleType),
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
    // 重置验证错误
    ruleNameError.value = ''
    ruleTypeError.value = ''
    weekDaysError.value = ''
    timeSlotsError.value = ''
    slotTypeError.value = ''
    totalSlotsError.value = ''
    startDateError.value = ''
  } catch (e) {
    ElMessage.error('加载规则详情失败')
  }
}

const submitForm = async () => {
  // 先执行自定义验证
  validateRuleName()
  validateRuleType()
  validateWeekDays()
  validateTimeSlots()
  validateSlotType()
  validateTotalSlots()
  validateStartDate()
  
  // 检查是否有错误
  const isValid = ((typeof isFormValid !== 'undefined') && (isFormValid?.value === true)) || (typeof checkFormValid === 'function' && checkFormValid())
  if (!isValid) {
    ElMessage.error(buildInvalidReasonsText())
    return
  }
  
  if (formRef.value) {
    const ok = await formRef.value.validate().catch(()=>false)
    if (!ok) return
  }
  const payload = { ...form.value, weekDays: weekDays.value, timeSlots: timeSlots.value }
  try {
    formSubmitting.value = true
    ElMessage.info('正在保存...')
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
  } finally { formSubmitting.value = false }
}

// 日期选择禁用逻辑与联动
const disableBeforeToday = (date) => { const t = new Date(); t.setHours(0,0,0,0); return date.getTime() < t.getTime() }
const disableEndBeforeStart = (date) => { const s = form.value.startDate; if (!s) return false; const sd = new Date(s); sd.setHours(0,0,0,0); return date.getTime() <= sd.getTime() }
watch(() => form.value.startDate, (s) => { const e = form.value.endDate; if (e && new Date(e).getTime() <= new Date(s).getTime()) { form.value.endDate = null } })

// 应用规则时的日期限制：必须在规则的日期范围内
const disableApplyStartDate = (date) => {
  if (!currentRuleData.value) return false
  const ruleStart = currentRuleData.value.startDate
  const ruleEnd = currentRuleData.value.endDate
  
  if (ruleStart) {
    const start = new Date(ruleStart)
    start.setHours(0, 0, 0, 0)
    if (date.getTime() < start.getTime()) return true
  }
  
  if (ruleEnd) {
    const end = new Date(ruleEnd)
    end.setHours(0, 0, 0, 0)
    if (date.getTime() > end.getTime()) return true
  }
  
  return false
}

const disableApplyEndDate = (date) => {
  if (!currentRuleData.value) return false
  const ruleStart = currentRuleData.value.startDate
  const ruleEnd = currentRuleData.value.endDate
  const applyStart = applyForm.value.applyStartDate
  
  // 必须在规则的日期范围内
  if (ruleStart) {
    const start = new Date(ruleStart)
    start.setHours(0, 0, 0, 0)
    if (date.getTime() < start.getTime()) return true
  }
  
  if (ruleEnd) {
    const end = new Date(ruleEnd)
    end.setHours(0, 0, 0, 0)
    if (date.getTime() > end.getTime()) return true
  }
  
  // 必须晚于开始日期
  if (applyStart) {
    const start = new Date(applyStart)
    start.setHours(0, 0, 0, 0)
    if (date.getTime() <= start.getTime()) return true
  }
  
  return false
}

// 排除日期限制：必须在应用日期范围内
const disableExcludeDate = (date) => {
  if (!currentRuleData.value) return false
  
  // 获取应用日期范围（如果没填则使用规则的日期范围）
  const applyStart = applyForm.value.applyStartDate || currentRuleData.value.startDate
  const applyEnd = applyForm.value.applyEndDate || currentRuleData.value.endDate
  
  if (applyStart) {
    const start = new Date(applyStart)
    start.setHours(0, 0, 0, 0)
    if (date.getTime() < start.getTime()) return true
  }
  
  if (applyEnd) {
    const end = new Date(applyEnd)
    end.setHours(0, 0, 0, 0)
    if (date.getTime() > end.getTime()) return true
  }
  
  return false
}

const submitAndEnable = async () => {
  // 先执行自定义验证
  validateRuleName()
  validateRuleType()
  validateWeekDays()
  validateTimeSlots()
  validateSlotType()
  validateTotalSlots()
  validateStartDate()
  
  // 检查是否有错误
  const isValid2 = ((typeof isFormValid !== 'undefined') && (isFormValid?.value === true)) || (typeof checkFormValid === 'function' && checkFormValid())
  if (!isValid2) {
    ElMessage.error(buildInvalidReasonsText())
    return
  }
  
  if (formRef.value) {
    const ok = await formRef.value.validate().catch(()=>false)
    if (!ok) return
  }
  const payload = { ...form.value, weekDays: weekDays.value, timeSlots: timeSlots.value }
  try {
    formSubmitting.value = true
    ElMessage.info('正在保存并启用...')
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
  } catch (e) { ElMessage.error('保存并启用失败') } finally { formSubmitting.value = false }
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
const openApply = (row) => { 
  currentRuleId.value = row.id
  currentRuleData.value = row
  conflictCheckResult.value = '' // 重置冲突检测结果
  applyVisible.value = true 
}
const estimatedApplyCount = computed(() => {
  if (!currentRuleData.value) return 0
  
  // 使用用户填写的日期，如果没填则使用规则的日期
  let s = applyForm.value.applyStartDate || currentRuleData.value.startDate
  let e = applyForm.value.applyEndDate || currentRuleData.value.endDate
  
  if (!s) return 0
  
  // 如果没有结束日期，默认计算30天
  if (!e) {
    const tempEnd = new Date(s)
    tempEnd.setDate(tempEnd.getDate() + 30)
    e = tempEnd.toISOString().slice(0, 10)
  }
  
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
    conflictChecking.value = true
    conflictCheckResult.value = ''
    const res = await detectScheduleRuleConflicts(row.id)
    conflicts.value = res?.data?.conflicts || res?.data || []
    
    if (conflicts.value.length > 0) {
      conflictCheckResult.value = 'conflict'
    } else {
      conflictCheckResult.value = 'success'
    }
  } catch (e) {
    ElMessage.error('检测失败')
    conflictCheckResult.value = ''
  } finally {
    conflictChecking.value = false
  }
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

const goToAdminSchedule = () => {
  // 构建查询参数
  const query = {}
  
  // 获取应用日期范围（如果没填则使用规则的日期范围）
  const startDate = applyForm.value.applyStartDate || currentRuleData.value?.startDate
  const endDate = applyForm.value.applyEndDate || currentRuleData.value?.endDate
  
  if (startDate) query.startDate = startDate
  if (endDate) query.endDate = endDate
  
  // 获取时间段
  if (currentRuleData.value?.timeSlots) {
    query.timeSlots = currentRuleData.value.timeSlots
  }
  
  // 获取医生ID
  if (currentRuleData.value?.doctorId) {
    query.doctorId = currentRuleData.value.doctorId
  }
  
  // 获取科室ID
  if (currentRuleData.value?.departmentId) {
    query.departmentId = currentRuleData.value.departmentId
  }
  
  // 获取门诊ID
  if (currentRuleData.value?.clinicId) {
    query.clinicId = currentRuleData.value.clinicId
  }
  
  // 关闭结果对话框
  applyResultVisible.value = false
  
  // 跳转到排班管理页面，带上查询参数
  router.push({ 
    path: '/admin/schedule', 
    query 
  })
}

const showErrorDetails = () => {
  if (applyResultData.value.errors && applyResultData.value.errors.length > 0) {
    const errorMessages = applyResultData.value.errors.map(err => err.message || err).join('\n')
    ElMessageBox.alert(errorMessages, '失败详情', {
      confirmButtonText: '确定',
      customClass: 'error-details-dialog'
    })
  }
}

// 表单验证状态
const ruleNameError = ref('')
const ruleTypeError = ref('')
const weekDaysError = ref('')
const timeSlotsError = ref('')
const slotTypeError = ref('')
const totalSlotsError = ref('')
const startDateError = ref('')

// 验证规则名称
const validateRuleName = () => {
  const name = form.value.ruleName?.trim() || ''
  if (!name) {
    ruleNameError.value = '请输入规则名称'
    return false
  }
  if (name.length < 2) {
    ruleNameError.value = '规则名称至少2个字符'
    return false
  }
  if (name.length > 30) {
    ruleNameError.value = '规则名称最多30个字'
    return false
  }
  ruleNameError.value = ''
  return true
}

// 验证规则类型
const validateRuleType = () => {
  if (!form.value.ruleType) {
    ruleTypeError.value = '请选择规则类型'
    return false
  }
  ruleTypeError.value = ''
  return true
}

// 验证星期选择
const validateWeekDays = () => {
  const rt = normalizeRuleType(form.value.ruleType)
  if (rt === 'FIXED_WEEKLY' && (!weekDays.value || weekDays.value.length === 0)) {
    weekDaysError.value = '请至少选择一天进行排班'
    return false
  }
  weekDaysError.value = ''
  return true
}

// 验证时段选择
const validateTimeSlots = () => {
  if (!timeSlots.value || timeSlots.value.length === 0) {
    timeSlotsError.value = '请至少选择一个时段'
    return false
  }
  timeSlotsError.value = ''
  return true
}

// 验证号别类型
const validateSlotType = () => {
  if (!form.value.slotType) {
    slotTypeError.value = '请选择号别类型'
    return false
  }
  slotTypeError.value = ''
  return true
}

// 验证总号源数
const validateTotalSlots = () => {
  const slots = form.value.totalSlots
  if (!slots || slots < 1 || slots > 100) {
    totalSlotsError.value = '号源个数为1–100的整数'
    return false
  }
  totalSlotsError.value = ''
  return true
}

// 验证开始日期和日期范围
const validateStartDate = () => {
  if (!form.value.startDate) {
    startDateError.value = '请选择开始日期'
    return false
  }
  
  // 如果填写了结束日期，检查日期范围是否至少一周
  if (form.value.endDate) {
    const start = new Date(form.value.startDate)
    const end = new Date(form.value.endDate)
    const diffDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    
    if (diffDays < 7) {
      startDateError.value = '日期范围至少需要7天，以确保排班规则完整生效'
      return false
    }
  }
  
  startDateError.value = ''
  return true
}

// 验证规则描述长度
const validateDescription = () => {
  const desc = form.value.description || ''
  if (desc.length > 200) {
    form.value.description = desc.substring(0, 200)
  }
}

// 表单整体验证
const isFormValid = computed(() => {
  const nameLen = (form.value.ruleName || '').trim().length
  const ruleNameValid = nameLen >= 2 && nameLen <= 30
  const ruleTypeValid = !!form.value.ruleType
  const rt = normalizeRuleType(form.value.ruleType)
  const weekDaysValid = rt === 'FIXED_WEEKLY' ? (weekDays.value && weekDays.value.length > 0) : true
  const timeSlotsValid = timeSlots.value && timeSlots.value.length > 0
  const slotTypeValid = !!form.value.slotType
  const totalSlotsValid = form.value.totalSlots >= 1 && form.value.totalSlots <= 100
  const startValid = !!form.value.startDate
  let dateRangeValid = true
  if (form.value.startDate && form.value.endDate) {
    const start = new Date(form.value.startDate)
    const end = new Date(form.value.endDate)
    const diffDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    dateRangeValid = diffDays >= 7
  }
  return ruleNameValid && ruleTypeValid && weekDaysValid && timeSlotsValid && slotTypeValid && totalSlotsValid && startValid && dateRangeValid
})

const invalidReasons = computed(() => {
  const reasons = []
  const nameLen = (form.value.ruleName || '').trim().length
  if (nameLen < 2 || nameLen > 30) reasons.push('规则名称需为2–30个字符')
  if (!form.value.ruleType) reasons.push('请选择规则类型')
  const rt = normalizeRuleType(form.value.ruleType)
  if (rt === 'FIXED_WEEKLY' && (!weekDays.value || weekDays.value.length === 0)) reasons.push('固定周排班需至少选择一天')
  if (!timeSlots.value || timeSlots.value.length === 0) reasons.push('请至少选择一个时段')
  if (!form.value.slotType) reasons.push('请选择号别类型')
  const slots = form.value.totalSlots
  if (!(slots >= 1 && slots <= 100)) reasons.push('总号源数需为1–100的整数')
  if (!form.value.startDate) reasons.push('请选择开始日期')
  if (form.value.startDate && form.value.endDate) {
    const start = new Date(form.value.startDate)
    const end = new Date(form.value.endDate)
    const diffDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    if (diffDays < 7) reasons.push('日期范围至少需要7天')
  }
  return reasons
})
const invalidReasonsText = computed(() => {
  const arr = invalidReasons.value || []
  return arr.length ? arr.join('；') : '请完善必填项'
})

// 运行时校验函数，避免因计算属性不可用导致点击报错
function checkFormValid() {
  const nameLen = (form.value.ruleName || '').trim().length
  if (nameLen < 2 || nameLen > 30) return false
  if (!form.value.ruleType) return false
  const rt = normalizeRuleType(form.value.ruleType)
  if (rt === 'FIXED_WEEKLY' && (!weekDays.value || weekDays.value.length === 0)) return false
  if (!timeSlots.value || timeSlots.value.length === 0) return false
  if (!form.value.slotType) return false
  const slots = form.value.totalSlots
  if (!(slots >= 1 && slots <= 100)) return false
  const s = form.value.startDate
  if (!s) return false
  const e = form.value.endDate
  if (s && e) {
    const start = new Date(s)
    const end = new Date(e)
    const diffDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    if (diffDays < 7) return false
  }
  return true
}

// 构建错误提示文本（不依赖计算属性）
function buildInvalidReasonsText() {
  const reasons = []
  const nameLen = (form.value.ruleName || '').trim().length
  if (nameLen < 2 || nameLen > 30) reasons.push('规则名称需为2–30个字符')
  if (!form.value.ruleType) reasons.push('请选择规则类型')
  const rt = normalizeRuleType(form.value.ruleType)
  if (rt === 'FIXED_WEEKLY' && (!weekDays.value || weekDays.value.length === 0)) reasons.push('固定周排班需至少选择一天')
  if (!timeSlots.value || timeSlots.value.length === 0) reasons.push('请至少选择一个时段')
  if (!form.value.slotType) reasons.push('请选择号别类型')
  const slots = form.value.totalSlots
  if (!(slots >= 1 && slots <= 100)) reasons.push('总号源数需为1–100的整数')
  const s = form.value.startDate
  if (!s) reasons.push('请选择开始日期')
  const e = form.value.endDate
  if (s && e) {
    const start = new Date(s)
    const end = new Date(e)
    const diffDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    if (diffDays < 7) reasons.push('日期范围至少需要7天')
  }
  return reasons.length ? reasons.join('；') : '请完善必填项'
}

// 监听表单变化，实时验证
watch(() => form.value.ruleName, validateRuleName)
watch(() => form.value.ruleType, validateRuleType)
watch(weekDays, validateWeekDays)
watch(() => form.value.totalSlots, validateTotalSlots)
watch(() => form.value.description, validateDescription)
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
.stat-content { display:flex; align-items:center; height:100%; padding:0 20px; }
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
.kv-label { width: 140px; color: #606266; text-align: left; white-space: nowrap; flex-shrink: 0; }
.kv-value { flex: 1; color: #303133; text-align: left; }
.detail-footer { text-align: right; }
.apply-dialog :deep(.el-dialog__body) { padding-top: 6px; }
.apply-intro { text-align: left; margin-bottom: 8px; color: #303133; }
.apply-card { margin-bottom: 8px; }
.apply-tips { text-align: left; color: #606266; margin-top: 4px; }
.popconfirm-wide { min-width: 280px; }
.popconfirm-wide .el-popconfirm__main { white-space: nowrap; }
.form-section { background: #fff; border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; margin-bottom: 12px; }
.form-section .section-title { padding: 12px 16px; font-weight: 600; border-bottom: 1px solid #ebeef5; text-align: left; }
.form-section .kv { padding: 12px 16px; }
.rule-type-tips { margin-top: 8px; color: #909399; font-size: 12px; }

/* 应用规则对话框样式 */
.apply-dialog :deep(.el-dialog__body) { padding-top: 6px; }
.apply-intro { text-align: left; margin-bottom: 16px; color: #303133; font-size: 16px; font-weight: 500; }
.apply-card { margin-bottom: 16px; border: 1px solid #ebeef5; }
.apply-card :deep(.el-card__body) { padding: 16px; }
.apply-form-container { padding: 0; }
.form-row { display: flex; align-items: flex-start; margin-bottom: 16px; gap: 4px !important; }
.form-label { min-width: fit-content; color: #606266; font-size: 14px; line-height: 32px; text-align: left; white-space: nowrap; }
.form-value { flex: 1; }
.date-range { display: flex; align-items: center; gap: 8px; }
.date-separator { color: #909399; font-size: 14px; }
.required-mark { color: #f56c6c; font-size: 12px; margin-left: 8px; }
.overwrite-checkbox { color: #e6a23c; font-weight: 500; }
.exclude-date-input { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.add-date-btn { background-color: #409eff; color: white; border: none; }
.add-date-btn:hover { background-color: #66b1ff; }
.exclude-date-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.exclude-date-tag { background-color: #f0f9ff; border-color: #b3d8ff; color: #409eff; }
.estimate-tag { margin-right: 8px; }
.estimate-desc { color: #909399; font-size: 12px; }
.apply-tips { background-color: #fef0f0; border: 1px solid #fbc4c4; border-radius: 4px; padding: 12px; margin-top: 16px; }
.tips-title { color: #f56c6c; font-weight: 600; margin-bottom: 8px; }
.tips-content { color: #606266; font-size: 14px; line-height: 1.6; }
.tips-content div { margin-bottom: 4px; }
.conflict-btn { margin-left: 8px; background-color: #e6a23c; color: white; border: none; }
.conflict-btn:hover { background-color: #eebe77; }
.dialog-footer { text-align: right; }

/* 结果对话框样式 - 新版 */
.result-dialog :deep(.el-dialog__body) { padding: 32px 24px; }
.result-dialog :deep(.el-dialog__header) { border-bottom: 1px solid #f0f0f0; padding-bottom: 16px; }
.result-content-new { text-align: center; }

.result-header-new { margin-bottom: 32px; padding-top: 8px; }
.success-icon-wrapper { 
  display: inline-flex; 
  align-items: center; 
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f0f9ff;
  margin-bottom: 16px;
}
.success-icon-new { color: #67c23a; font-size: 32px; }
.result-title-new { color: #303133; font-size: 20px; font-weight: 600; margin-bottom: 8px; }
.result-subtitle { color: #909399; font-size: 14px; }

.result-stats-cards { 
  display: grid; 
  grid-template-columns: repeat(3, 1fr); 
  gap: 16px; 
  margin-bottom: 32px; 
}
.stat-card { 
  background: #f5f7fa; 
  border-radius: 8px; 
  padding: 20px 16px; 
  text-align: center;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-card.stat-success { background: #f0f9ff; border: 1px solid #d0e8ff; }
.stat-card.stat-skip { background: #fff7e6; border: 1px solid #ffe7ba; }
.stat-card.stat-error { background: #fef0f0; border: 1px solid #fde2e2; }
.stat-card-label { color: #606266; font-size: 14px; margin-bottom: 8px; }
.stat-card-value { 
  color: #303133; 
  font-size: 32px; 
  font-weight: 600; 
  line-height: 1;
}
.stat-card.stat-success .stat-card-value { color: #67c23a; }
.stat-card.stat-skip .stat-card-value { color: #e6a23c; }
.stat-card.stat-error .stat-card-value { color: #f56c6c; }
.stat-unit { font-size: 14px; font-weight: 400; margin-left: 4px; }
.stat-card-note { color: #909399; font-size: 12px; margin-top: 4px; }

.result-actions { 
  display: flex; 
  gap: 12px; 
  justify-content: center;
}
.view-schedule-btn { 
  flex: 1;
  max-width: 200px;
  background: #67c23a !important;
  border-color: #67c23a !important;
}
.view-schedule-btn:hover { 
  background: #85ce61 !important;
  border-color: #85ce61 !important;
}
.close-btn { 
  flex: 1;
  max-width: 120px;
}

/* 规则表单样式 */
.rule-form-drawer :deep(.el-drawer__header) {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 0;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #ebeef5;
}

.required-legend {
  color: #909399;
  font-size: 12px;
  font-weight: normal;
  margin-left: 8px;
}

.required-mark {
  color: #f56c6c;
  margin-left: 4px;
}

.kv-label.single-line {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 120px;
  flex-shrink: 0;
}

/* 高级规则特定样式 */
.form-section .kv-row {
  align-items: center;
}

.form-section .kv-label {
  flex-shrink: 0;
  padding-right: 8px;
  min-width: 140px;
  max-width: 180px;
}

/* 确保高级规则标签不换行 */
.form-section:nth-child(5) .kv-label {
  white-space: nowrap;
  overflow: visible;
  text-overflow: clip;
}

/* 提示文字样式 */
.hint-text {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.4;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
  line-height: 1.4;
}

.weekday-rows {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.weekday-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.weekday-checkbox {
  margin-right: 0;
  min-width: 60px;
}

.weekday-checkbox :deep(.el-checkbox__label) {
  font-size: 14px;
  color: #606266;
}

/* 增强表单字段样式 */
.form-section :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.2s;
}

.form-section :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.form-section :deep(.el-input-number) {
  width: 120px;
}

.form-section :deep(.el-input-number__increase),
.form-section :deep(.el-input-number__decrease) {
  border-radius: 0;
}

/* 错误状态样式 */
.form-section :deep(.el-input__inner.error),
.form-section :deep(.el-textarea__inner.error) {
  border-color: #f56c6c;
}

</style>
