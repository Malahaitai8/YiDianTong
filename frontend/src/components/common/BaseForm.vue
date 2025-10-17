<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    :label-width="labelWidth"
    :label-position="labelPosition"
    :inline="inline"
    :size="size"
    :disabled="disabled"
    :validate-on-rule-change="validateOnRuleChange"
    :hide-required-asterisk="hideRequiredAsterisk"
    :show-message="showMessage"
    :inline-message="inlineMessage"
    :status-icon="statusIcon"
    @validate="handleValidate"
  >
    <el-row :gutter="gutter">
      <el-col
        v-for="field in fields"
        :key="field.prop"
        :span="field.span || defaultSpan"
        :xs="field.xs"
        :sm="field.sm"
        :md="field.md"
        :lg="field.lg"
        :xl="field.xl"
      >
        <el-form-item
          :prop="field.prop"
          :label="field.label"
          :label-width="field.labelWidth"
          :required="field.required"
          :rules="field.rules"
          :error="field.error"
          :show-message="field.showMessage"
          :inline-message="field.inlineMessage"
          :size="field.size"
        >
          <!-- 输入框 -->
          <el-input
            v-if="field.type === 'input'"
            v-model="formData[field.prop]"
            :type="field.inputType || 'text'"
            :placeholder="field.placeholder"
            :clearable="field.clearable !== false"
            :show-password="field.showPassword"
            :disabled="field.disabled"
            :readonly="field.readonly"
            :maxlength="field.maxlength"
            :minlength="field.minlength"
            :show-word-limit="field.showWordLimit"
            :prefix-icon="field.prefixIcon"
            :suffix-icon="field.suffixIcon"
            :rows="field.rows"
            :autosize="field.autosize"
            :resize="field.resize"
            @input="handleFieldChange(field.prop, $event)"
            @change="handleFieldChange(field.prop, $event)"
            @blur="handleFieldBlur(field.prop, $event)"
            @focus="handleFieldFocus(field.prop, $event)"
          >
            <template v-if="field.prepend" #prepend>
              {{ field.prepend }}
            </template>
            <template v-if="field.append" #append>
              {{ field.append }}
            </template>
          </el-input>

          <!-- 文本域 -->
          <el-input
            v-else-if="field.type === 'textarea'"
            v-model="formData[field.prop]"
            type="textarea"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :readonly="field.readonly"
            :maxlength="field.maxlength"
            :minlength="field.minlength"
            :show-word-limit="field.showWordLimit"
            :rows="field.rows || 4"
            :autosize="field.autosize"
            :resize="field.resize"
            @input="handleFieldChange(field.prop, $event)"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 数字输入框 -->
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="formData[field.prop]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            :precision="field.precision"
            :controls="field.controls !== false"
            :controls-position="field.controlsPosition"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 选择器 -->
          <el-select
            v-else-if="field.type === 'select'"
            v-model="formData[field.prop]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :multiple="field.multiple"
            :multiple-limit="field.multipleLimit"
            :collapse-tags="field.collapseTags"
            :collapse-tags-tooltip="field.collapseTagsTooltip"
            :filterable="field.filterable"
            :allow-create="field.allowCreate"
            :remote="field.remote"
            :remote-method="field.remoteMethod"
            :loading="field.loading"
            :loading-text="field.loadingText"
            :no-match-text="field.noMatchText"
            :no-data-text="field.noDataText"
            @change="handleFieldChange(field.prop, $event)"
            @visible-change="handleSelectVisibleChange(field, $event)"
          >
            <el-option
              v-for="option in field.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
              :disabled="option.disabled"
            />
          </el-select>

          <!-- 级联选择器 -->
          <el-cascader
            v-else-if="field.type === 'cascader'"
            v-model="formData[field.prop]"
            :options="field.options"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :show-all-levels="field.showAllLevels !== false"
            :collapse-tags="field.collapseTags"
            :separator="field.separator"
            :filterable="field.filterable"
            :props="field.props"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 日期选择器 -->
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="formData[field.prop]"
            :type="field.dateType || 'date'"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :format="field.format"
            :value-format="field.valueFormat"
            :disabled-date="field.disabledDate"
            :shortcuts="field.shortcuts"
            :default-value="field.defaultValue"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 日期范围选择器 -->
          <el-date-picker
            v-else-if="field.type === 'daterange'"
            v-model="formData[field.prop]"
            type="daterange"
            :start-placeholder="field.startPlaceholder || '开始日期'"
            :end-placeholder="field.endPlaceholder || '结束日期'"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :format="field.format"
            :value-format="field.valueFormat"
            :disabled-date="field.disabledDate"
            :shortcuts="field.shortcuts"
            :default-value="field.defaultValue"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 时间选择器 -->
          <el-time-picker
            v-else-if="field.type === 'time'"
            v-model="formData[field.prop]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :clearable="field.clearable !== false"
            :format="field.format"
            :value-format="field.valueFormat"
            :disabled-hours="field.disabledHours"
            :disabled-minutes="field.disabledMinutes"
            :disabled-seconds="field.disabledSeconds"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 开关 -->
          <el-switch
            v-else-if="field.type === 'switch'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :width="field.width"
            :active-text="field.activeText"
            :inactive-text="field.inactiveText"
            :active-value="field.activeValue"
            :inactive-value="field.inactiveValue"
            :active-color="field.activeColor"
            :inactive-color="field.inactiveColor"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 单选框组 -->
          <el-radio-group
            v-else-if="field.type === 'radio'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :size="field.size"
            @change="handleFieldChange(field.prop, $event)"
          >
            <el-radio
              v-for="option in field.options"
              :key="option.value"
              :label="option.value"
              :disabled="option.disabled"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>

          <!-- 复选框组 -->
          <el-checkbox-group
            v-else-if="field.type === 'checkbox'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :min="field.min"
            :max="field.max"
            @change="handleFieldChange(field.prop, $event)"
          >
            <el-checkbox
              v-for="option in field.options"
              :key="option.value"
              :label="option.value"
              :disabled="option.disabled"
            >
              {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>

          <!-- 评分 -->
          <el-rate
            v-else-if="field.type === 'rate'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :max="field.max"
            :allow-half="field.allowHalf"
            :low-threshold="field.lowThreshold"
            :high-threshold="field.highThreshold"
            :colors="field.colors"
            :void-color="field.voidColor"
            :disabled-void-color="field.disabledVoidColor"
            :icon-classes="field.iconClasses"
            :void-icon-class="field.voidIconClass"
            :disabled-void-icon-class="field.disabledVoidIconClass"
            :show-text="field.showText"
            :show-score="field.showScore"
            :text-color="field.textColor"
            :texts="field.texts"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 滑块 -->
          <el-slider
            v-else-if="field.type === 'slider'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            :show-input="field.showInput"
            :show-input-controls="field.showInputControls"
            :show-stops="field.showStops"
            :show-tooltip="field.showTooltip"
            :format-tooltip="field.formatTooltip"
            :range="field.range"
            :vertical="field.vertical"
            :height="field.height"
            :marks="field.marks"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 颜色选择器 -->
          <el-color-picker
            v-else-if="field.type === 'color'"
            v-model="formData[field.prop]"
            :disabled="field.disabled"
            :size="field.size"
            :show-alpha="field.showAlpha"
            :color-format="field.colorFormat"
            :predefine="field.predefine"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 自定义组件 -->
          <component
            v-else-if="field.component"
            :is="field.component"
            v-model="formData[field.prop]"
            v-bind="field.componentProps"
            @change="handleFieldChange(field.prop, $event)"
          />

          <!-- 自定义插槽 -->
          <slot
            v-else-if="field.slot"
            :name="field.slot"
            :field="field"
            :value="formData[field.prop]"
            :form-data="formData"
          />

          <!-- 帮助文本 -->
          <div v-if="field.help" class="field-help">
            {{ field.help }}
          </div>
        </el-form-item>
      </el-col>
    </el-row>

    <!-- 表单操作按钮 -->
    <el-form-item v-if="showActions" class="form-actions">
      <slot name="actions">
        <el-button
          v-if="showResetButton"
          :size="actionSize"
          @click="handleReset"
        >
          {{ resetButtonText }}
        </el-button>
        <el-button
          v-if="showSubmitButton"
          type="primary"
          :size="actionSize"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          {{ submitButtonText }}
        </el-button>
      </slot>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  // 表单字段配置
  fields: {
    type: Array,
    required: true
  },
  // 表单数据
  modelValue: {
    type: Object,
    default: () => ({})
  },
  // 表单验证规则
  rules: {
    type: Object,
    default: () => ({})
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: '100px'
  },
  // 标签位置
  labelPosition: {
    type: String,
    default: 'right'
  },
  // 是否行内表单
  inline: {
    type: Boolean,
    default: false
  },
  // 表单尺寸
  size: {
    type: String,
    default: 'default'
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否在 rules 属性改变后立即触发一次验证
  validateOnRuleChange: {
    type: Boolean,
    default: true
  },
  // 是否隐藏必填字段的标签旁边的红色星号
  hideRequiredAsterisk: {
    type: Boolean,
    default: false
  },
  // 是否显示校验错误信息
  showMessage: {
    type: Boolean,
    default: true
  },
  // 是否以行内形式展示校验信息
  inlineMessage: {
    type: Boolean,
    default: false
  },
  // 是否在输入框中显示校验结果反馈图标
  statusIcon: {
    type: Boolean,
    default: false
  },
  // 栅格间隔
  gutter: {
    type: Number,
    default: 20
  },
  // 默认栅格占据的列数
  defaultSpan: {
    type: Number,
    default: 24
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 是否显示重置按钮
  showResetButton: {
    type: Boolean,
    default: true
  },
  // 是否显示提交按钮
  showSubmitButton: {
    type: Boolean,
    default: true
  },
  // 重置按钮文本
  resetButtonText: {
    type: String,
    default: '重置'
  },
  // 提交按钮文本
  submitButtonText: {
    type: String,
    default: '提交'
  },
  // 操作按钮尺寸
  actionSize: {
    type: String,
    default: 'default'
  },
  // 提交按钮加载状态
  submitLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'submit',
  'reset',
  'validate',
  'field-change',
  'field-blur',
  'field-focus'
])

const formRef = ref()
const formData = reactive({})

// 合并表单验证规则
const formRules = computed(() => {
  const rules = { ...props.rules }
  
  // 从字段配置中提取验证规则
  props.fields.forEach(field => {
    if (field.rules) {
      rules[field.prop] = field.rules
    }
  })
  
  return rules
})

// 初始化表单数据
const initFormData = () => {
  props.fields.forEach(field => {
    if (props.modelValue[field.prop] !== undefined) {
      formData[field.prop] = props.modelValue[field.prop]
    } else {
      formData[field.prop] = field.defaultValue || getDefaultValue(field.type)
    }
  })
}

// 获取字段类型的默认值
const getDefaultValue = (type) => {
  const defaultValues = {
    input: '',
    textarea: '',
    number: null,
    select: '',
    cascader: [],
    date: null,
    daterange: [],
    time: null,
    switch: false,
    radio: '',
    checkbox: [],
    rate: 0,
    slider: 0,
    color: ''
  }
  return defaultValues[type] || ''
}

// 监听外部数据变化
watch(
  () => props.modelValue,
  (newValue) => {
    Object.assign(formData, newValue)
  },
  { deep: true, immediate: true }
)

// 监听表单数据变化
watch(
  formData,
  (newValue) => {
    emit('update:modelValue', { ...newValue })
  },
  { deep: true }
)

// 字段值变化处理
const handleFieldChange = (prop, value) => {
  emit('field-change', prop, value, formData)
}

// 字段失焦处理
const handleFieldBlur = (prop, event) => {
  emit('field-blur', prop, event, formData)
}

// 字段聚焦处理
const handleFieldFocus = (prop, event) => {
  emit('field-focus', prop, event, formData)
}

// 选择器显示状态变化
const handleSelectVisibleChange = (field, visible) => {
  if (visible && field.remote && field.remoteMethod) {
    field.remoteMethod('')
  }
}

// 表单验证
const handleValidate = (prop, isValid, message) => {
  emit('validate', prop, isValid, message)
}

// 提交处理
const handleSubmit = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (valid) {
      emit('submit', { ...formData })
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 重置处理
const handleReset = () => {
  formRef.value?.resetFields()
  
  // 重置为默认值
  props.fields.forEach(field => {
    formData[field.prop] = field.defaultValue || getDefaultValue(field.type)
  })
  
  emit('reset', { ...formData })
}

// 暴露方法
defineExpose({
  validate: () => formRef.value?.validate(),
  validateField: (props) => formRef.value?.validateField(props),
  resetFields: () => formRef.value?.resetFields(),
  clearValidate: (props) => formRef.value?.clearValidate(props),
  scrollToField: (prop) => formRef.value?.scrollToField(prop)
})

// 初始化
initFormData()
</script>

<style scoped>
.form-actions {
  text-align: center;
  margin-top: 20px;
}

.form-actions :deep(.el-form-item__content) {
  justify-content: center;
}

.field-help {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
}

:deep(.el-form-item__content) {
  width: 100%;
}

:deep(.el-input),
:deep(.el-select),
:deep(.el-cascader),
:deep(.el-date-editor),
:deep(.el-time-picker) {
  width: 100%;
}

:deep(.el-textarea) {
  width: 100%;
}

:deep(.el-radio-group),
:deep(.el-checkbox-group) {
  width: 100%;
}

:deep(.el-rate) {
  height: 32px;
  line-height: 32px;
}

:deep(.el-slider) {
  margin: 12px 0;
}

:deep(.el-color-picker) {
  display: block;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    text-align: left !important;
    padding-right: 0 !important;
  }
  
  .form-actions {
    text-align: left;
  }
  
  .form-actions :deep(.el-form-item__content) {
    justify-content: flex-start;
  }
}
</style>