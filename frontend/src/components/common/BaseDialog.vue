<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :top="top"
    :modal="modal"
    :modal-class="modalClass"
    :append-to-body="appendToBody"
    :lock-scroll="lockScroll"
    :custom-class="customClass"
    :open-delay="openDelay"
    :close-delay="closeDelay"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :center="center"
    :align-center="alignCenter"
    :destroy-on-close="destroyOnClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 自定义标题 -->
    <template v-if="$slots.title" #title>
      <slot name="title"></slot>
    </template>

    <!-- 对话框内容 -->
    <div v-loading="loading" class="dialog-content">
      <slot></slot>
    </div>

    <!-- 底部操作按钮 -->
    <template v-if="showFooter" #footer>
      <slot name="footer">
        <div class="dialog-footer">
          <el-button
            v-if="showCancelButton"
            :size="buttonSize"
            @click="handleCancel"
          >
            {{ cancelButtonText }}
          </el-button>
          <el-button
            v-if="showConfirmButton"
            type="primary"
            :size="buttonSize"
            :loading="confirmLoading"
            @click="handleConfirm"
          >
            {{ confirmButtonText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 是否显示对话框
  modelValue: {
    type: Boolean,
    default: false
  },
  // 对话框标题
  title: {
    type: String,
    default: ''
  },
  // 对话框宽度
  width: {
    type: [String, Number],
    default: '50%'
  },
  // 是否全屏
  fullscreen: {
    type: Boolean,
    default: false
  },
  // 距离顶部的距离
  top: {
    type: String,
    default: '15vh'
  },
  // 是否显示遮罩层
  modal: {
    type: Boolean,
    default: true
  },
  // 遮罩层类名
  modalClass: {
    type: String,
    default: ''
  },
  // 是否插入到 body 元素上
  appendToBody: {
    type: Boolean,
    default: false
  },
  // 是否在对话框出现时将 body 滚动锁定
  lockScroll: {
    type: Boolean,
    default: true
  },
  // 自定义类名
  customClass: {
    type: String,
    default: ''
  },
  // 打开延时
  openDelay: {
    type: Number,
    default: 0
  },
  // 关闭延时
  closeDelay: {
    type: Number,
    default: 0
  },
  // 是否可以通过点击遮罩层关闭对话框
  closeOnClickModal: {
    type: Boolean,
    default: true
  },
  // 是否可以通过按下 ESC 关闭对话框
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },
  // 是否显示关闭按钮
  showClose: {
    type: Boolean,
    default: true
  },
  // 关闭前的回调
  beforeClose: {
    type: Function,
    default: undefined
  },
  // 是否对头部和底部采用居中布局
  center: {
    type: Boolean,
    default: false
  },
  // 是否水平垂直对齐对话框
  alignCenter: {
    type: Boolean,
    default: false
  },
  // 关闭时销毁子元素
  destroyOnClose: {
    type: Boolean,
    default: false
  },
  // 是否显示底部
  showFooter: {
    type: Boolean,
    default: true
  },
  // 是否显示取消按钮
  showCancelButton: {
    type: Boolean,
    default: true
  },
  // 是否显示确认按钮
  showConfirmButton: {
    type: Boolean,
    default: true
  },
  // 取消按钮文本
  cancelButtonText: {
    type: String,
    default: '取消'
  },
  // 确认按钮文本
  confirmButtonText: {
    type: String,
    default: '确定'
  },
  // 按钮尺寸
  buttonSize: {
    type: String,
    default: 'default'
  },
  // 内容加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 确认按钮加载状态
  confirmLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'open',
  'opened',
  'close',
  'closed',
  'confirm',
  'cancel'
])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 关闭前处理
const handleBeforeClose = (done) => {
  if (props.beforeClose) {
    props.beforeClose(done)
  } else {
    done()
  }
}

// 打开事件
const handleOpen = () => {
  emit('open')
}

// 打开完成事件
const handleOpened = () => {
  emit('opened')
}

// 关闭事件
const handleClose = () => {
  emit('close')
}

// 关闭完成事件
const handleClosed = () => {
  emit('closed')
}

// 确认处理
const handleConfirm = () => {
  emit('confirm')
}

// 取消处理
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

// 暴露方法
defineExpose({
  close: () => {
    visible.value = false
  },
  open: () => {
    visible.value = true
  }
})
</script>

<style scoped>
.dialog-content {
  min-height: 100px;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button + .el-button {
  margin-left: 12px;
}

:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
  color: #606266;
  line-height: 1.6;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
}

:deep(.el-dialog__close) {
  font-size: 16px;
  color: #909399;
}

:deep(.el-dialog__close:hover) {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
  
  :deep(.el-dialog__header) {
    padding: 16px 20px 12px;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 12px 20px 16px;
  }
}

/* 全屏模式 */
:deep(.el-dialog.is-fullscreen) {
  border-radius: 0;
}

:deep(.el-dialog.is-fullscreen .el-dialog__header) {
  border-radius: 0;
}

/* 加载状态 */
:deep(.el-loading-mask) {
  border-radius: 4px;
}
</style>