<template>
  <el-card class="stat-card" :class="cardClass" :shadow="shadow">
    <div class="stat-content">
      <div class="stat-icon" :style="iconStyle">
        <el-icon :size="iconSize">
          <component :is="icon" />
        </el-icon>
      </div>
      
      <div class="stat-info">
        <div class="stat-title">{{ title }}</div>
        <div class="stat-value" :style="valueStyle">
          <span v-if="prefix" class="stat-prefix">{{ prefix }}</span>
          <span class="stat-number">{{ formattedValue }}</span>
          <span v-if="suffix" class="stat-suffix">{{ suffix }}</span>
        </div>
        
        <div v-if="showTrend" class="stat-trend" :class="trendClass">
          <el-icon :size="12">
            <ArrowUp v-if="trend > 0" />
            <ArrowDown v-if="trend < 0" />
            <Minus v-if="trend === 0" />
          </el-icon>
          <span class="trend-text">{{ trendText }}</span>
        </div>
        
        <div v-if="description" class="stat-description">
          {{ description }}
        </div>
      </div>
      
      <div v-if="extra" class="stat-extra">
        <slot name="extra">{{ extra }}</slot>
      </div>
    </div>
    
    <div v-if="$slots.footer" class="stat-footer">
      <slot name="footer"></slot>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

const props = defineProps({
  // 标题
  title: {
    type: String,
    required: true
  },
  // 数值
  value: {
    type: [Number, String],
    required: true
  },
  // 图标
  icon: {
    type: [String, Object],
    default: ''
  },
  // 图标大小
  iconSize: {
    type: Number,
    default: 24
  },
  // 图标颜色
  iconColor: {
    type: String,
    default: '#409eff'
  },
  // 图标背景色
  iconBgColor: {
    type: String,
    default: '#ecf5ff'
  },
  // 前缀
  prefix: {
    type: String,
    default: ''
  },
  // 后缀
  suffix: {
    type: String,
    default: ''
  },
  // 数值颜色
  valueColor: {
    type: String,
    default: '#303133'
  },
  // 趋势值
  trend: {
    type: Number,
    default: 0
  },
  // 趋势文本
  trendText: {
    type: String,
    default: ''
  },
  // 是否显示趋势
  showTrend: {
    type: Boolean,
    default: false
  },
  // 描述文本
  description: {
    type: String,
    default: ''
  },
  // 额外内容
  extra: {
    type: String,
    default: ''
  },
  // 卡片阴影
  shadow: {
    type: String,
    default: 'hover'
  },
  // 主题色
  theme: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'primary', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  // 数值格式化
  formatter: {
    type: Function,
    default: null
  },
  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  }
})

// 主题色配置
const themeColors = {
  default: {
    iconColor: '#409eff',
    iconBgColor: '#ecf5ff',
    valueColor: '#303133'
  },
  primary: {
    iconColor: '#409eff',
    iconBgColor: '#ecf5ff',
    valueColor: '#409eff'
  },
  success: {
    iconColor: '#67c23a',
    iconBgColor: '#f0f9ff',
    valueColor: '#67c23a'
  },
  warning: {
    iconColor: '#e6a23c',
    iconBgColor: '#fdf6ec',
    valueColor: '#e6a23c'
  },
  danger: {
    iconColor: '#f56c6c',
    iconBgColor: '#fef0f0',
    valueColor: '#f56c6c'
  },
  info: {
    iconColor: '#909399',
    iconBgColor: '#f4f4f5',
    valueColor: '#909399'
  }
}

// 卡片样式类
const cardClass = computed(() => {
  const classes = [`stat-card--${props.theme}`]
  if (props.loading) {
    classes.push('stat-card--loading')
  }
  return classes
})

// 图标样式
const iconStyle = computed(() => {
  const theme = themeColors[props.theme]
  return {
    color: props.iconColor || theme.iconColor,
    backgroundColor: props.iconBgColor || theme.iconBgColor
  }
})

// 数值样式
const valueStyle = computed(() => {
  const theme = themeColors[props.theme]
  return {
    color: props.valueColor || theme.valueColor
  }
})

// 格式化数值
const formattedValue = computed(() => {
  if (props.formatter) {
    return props.formatter(props.value)
  }
  
  if (typeof props.value === 'number') {
    // 数字格式化，添加千分位分隔符
    return props.value.toLocaleString()
  }
  
  return props.value
})

// 趋势样式类
const trendClass = computed(() => {
  if (props.trend > 0) return 'trend-up'
  if (props.trend < 0) return 'trend-down'
  return 'trend-flat'
})
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-card--loading {
  opacity: 0.6;
  pointer-events: none;
}

.stat-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  line-height: 1.4;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-prefix,
.stat-suffix {
  font-size: 16px;
  font-weight: 400;
  opacity: 0.8;
}

.stat-number {
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-bottom: 4px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-flat {
  color: #909399;
}

.trend-text {
  font-weight: 500;
}

.stat-description {
  font-size: 12px;
  color: #c0c4cc;
  line-height: 1.4;
}

.stat-extra {
  margin-left: auto;
  flex-shrink: 0;
}

.stat-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 12px;
  color: #909399;
}

/* 主题样式 */
.stat-card--primary {
  border-left: 4px solid #409eff;
}

.stat-card--success {
  border-left: 4px solid #67c23a;
}

.stat-card--warning {
  border-left: 4px solid #e6a23c;
}

.stat-card--danger {
  border-left: 4px solid #f56c6c;
}

.stat-card--info {
  border-left: 4px solid #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-content {
    gap: 12px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .stat-prefix,
  .stat-suffix {
    font-size: 14px;
  }
}

/* 加载动画 */
.stat-card--loading .stat-value {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .stat-card {
    background-color: #2d2d2d;
    border-color: #404040;
  }
  
  .stat-title {
    color: #b0b0b0;
  }
  
  .stat-description {
    color: #808080;
  }
  
  .stat-footer {
    border-top-color: #404040;
    color: #b0b0b0;
  }
}
</style>