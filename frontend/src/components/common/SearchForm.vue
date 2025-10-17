<template>
  <el-card class="search-form">
    <el-form
      ref="formRef"
      :model="formData"
      :inline="inline"
      :label-width="labelWidth"
      @submit.prevent="handleSearch"
    >
      <el-row :gutter="gutter">
        <el-col
          v-for="field in fields"
          :key="field.prop"
          :span="field.span || defaultSpan"
        >
          <el-form-item :label="field.label" :prop="field.prop">
            <!-- 输入框 -->
            <el-input
              v-if="field.type === 'input'"
              v-model="formData[field.prop]"
              :placeholder="field.placeholder"
              :clearable="field.clearable !== false"
              @input="handleFieldChange(field.prop, $event)"
            >
              <template v-if="field.prefix" #prefix>
                <el-icon>
                  <component :is="field.prefix" />
                </el-icon>
              </template>
            </el-input>

            <!-- 选择器 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="formData[field.prop]"
              :placeholder="field.placeholder"
              :clearable="field.clearable !== false"
              :multiple="field.multiple"
              @change="handleFieldChange(field.prop, $event)"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>

            <!-- 日期选择器 -->
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="formData[field.prop]"
              :type="field.dateType || 'date'"
              :placeholder="field.placeholder"
              :format="field.format"
              :value-format="field.valueFormat"
              :clearable="field.clearable !== false"
              @change="handleFieldChange(field.prop, $event)"
            />

            <!-- 日期范围选择器 -->
            <el-date-picker
              v-else-if="field.type === 'daterange'"
              v-model="formData[field.prop]"
              type="daterange"
              :start-placeholder="field.startPlaceholder || '开始日期'"
              :end-placeholder="field.endPlaceholder || '结束日期'"
              :format="field.format"
              :value-format="field.valueFormat"
              :clearable="field.clearable !== false"
              @change="handleFieldChange(field.prop, $event)"
            />

            <!-- 数字输入框 -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="formData[field.prop]"
              :placeholder="field.placeholder"
              :min="field.min"
              :max="field.max"
              :step="field.step"
              :precision="field.precision"
              @change="handleFieldChange(field.prop, $event)"
            />

            <!-- 开关 -->
            <el-switch
              v-else-if="field.type === 'switch'"
              v-model="formData[field.prop]"
              :active-text="field.activeText"
              :inactive-text="field.inactiveText"
              @change="handleFieldChange(field.prop, $event)"
            />

            <!-- 单选框组 -->
            <el-radio-group
              v-else-if="field.type === 'radio'"
              v-model="formData[field.prop]"
              @change="handleFieldChange(field.prop, $event)"
            >
              <el-radio
                v-for="option in field.options"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>

            <!-- 复选框组 -->
            <el-checkbox-group
              v-else-if="field.type === 'checkbox'"
              v-model="formData[field.prop]"
              @change="handleFieldChange(field.prop, $event)"
            >
              <el-checkbox
                v-for="option in field.options"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-col>

        <!-- 操作按钮 -->
        <el-col :span="actionSpan">
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleSearch"
            >
              <el-icon><Search /></el-icon>
              {{ searchText }}
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              {{ resetText }}
            </el-button>
            <slot name="actions"></slot>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  fields: {
    type: Array,
    required: true
  },
  modelValue: {
    type: Object,
    default: () => ({})
  },
  inline: {
    type: Boolean,
    default: false
  },
  labelWidth: {
    type: String,
    default: '100px'
  },
  gutter: {
    type: Number,
    default: 20
  },
  defaultSpan: {
    type: Number,
    default: 6
  },
  actionSpan: {
    type: Number,
    default: 6
  },
  loading: {
    type: Boolean,
    default: false
  },
  searchText: {
    type: String,
    default: '搜索'
  },
  resetText: {
    type: String,
    default: '重置'
  },
  autoSearch: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset', 'field-change'])

const formRef = ref()
const formData = reactive({})

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
    select: '',
    date: null,
    daterange: [],
    number: null,
    switch: false,
    radio: '',
    checkbox: []
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
  
  if (props.autoSearch) {
    handleSearch()
  }
}

// 搜索处理
const handleSearch = () => {
  emit('search', { ...formData })
}

// 重置处理
const handleReset = () => {
  formRef.value?.resetFields()
  
  // 重置为默认值
  props.fields.forEach(field => {
    formData[field.prop] = field.defaultValue || getDefaultValue(field.type)
  })
  
  emit('reset', { ...formData })
  
  if (props.autoSearch) {
    handleSearch()
  }
}

// 暴露方法
defineExpose({
  resetFields: () => formRef.value?.resetFields(),
  validate: () => formRef.value?.validate(),
  clearValidate: () => formRef.value?.clearValidate()
})

// 初始化
initFormData()
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__content) {
  width: 100%;
}

:deep(.el-input),
:deep(.el-select),
:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-radio-group),
:deep(.el-checkbox-group) {
  width: 100%;
}
</style>