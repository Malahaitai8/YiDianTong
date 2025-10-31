<template>
  <div class="qa-management">
    <div class="page-header">
      <h2>QA管理</h2>
      <p>管理系统的问答内容，包括常见问题、智能客服回复等</p>
    </div>
    
    <div class="content-area">
      <div class="development-notice">
        <el-alert
          title="开发中"
          description="此页面正在开发中，敬请期待..."
          type="info"
          :closable="false"
          show-icon>
        </el-alert>
      </div>
      
      <!-- 预留的功能区域 -->
      <div class="feature-preview">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>常见问题管理</span>
                </div>
              </template>
              <p>管理患者常见问题和标准答案</p>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>智能客服配置</span>
                </div>
              </template>
              <p>配置智能客服的自动回复规则</p>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>问答统计</span>
                </div>
              </template>
              <p>查看问答使用情况和效果统计</p>
            </el-card>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>知识库管理</span>
                </div>
              </template>
              <p>维护医疗知识库和专业术语</p>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>反馈管理</span>
                </div>
              </template>
              <p>处理用户反馈和问题改进建议</p>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>质量评估</span>
                </div>
              </template>
              <p>评估问答质量和用户满意度</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 页面初始化
onMounted(() => {
  console.log('QA管理页面已加载')
})
</script>

<style scoped>
.qa-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.content-area {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.development-notice {
  margin-bottom: 30px;
}

.feature-preview {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid #ebeef5;
}
</style>