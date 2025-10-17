<template>
  <div class="page-header">
    <div class="header-content">
      <div class="header-left">
        <h1 class="page-title">{{ title }}</h1>
        <p v-if="description" class="page-description">{{ description }}</p>
        <el-breadcrumb v-if="breadcrumb && breadcrumb.length" separator="/">
          <el-breadcrumb-item
            v-for="item in breadcrumb"
            :key="item.path"
            :to="item.path ? { path: item.path } : undefined"
          >
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="header-right">
        <slot name="extra"></slot>
      </div>
    </div>
    
    <div v-if="$slots.tabs" class="header-tabs">
      <slot name="tabs"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  breadcrumb: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.page-header {
  background: white;
  padding: 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.page-description {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.header-right {
  margin-left: 24px;
}

.header-tabs {
  margin-top: 24px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

:deep(.el-breadcrumb) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item) {
  color: #909399;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #303133;
  font-weight: 500;
}
</style>