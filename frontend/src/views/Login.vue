<template>
  <div class="login-container">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="decoration">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="logo-title">医点通</h1>
          <p class="logo-subtitle">智慧医疗 · 便民服务</p>
        </div>
        
        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">📅</div>
            <div class="feature-text">
              <h3>智能排班</h3>
              <p>高效的医生排班管理系统</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">👨‍⚕️</div>
            <div class="feature-text">
              <h3>医生管理</h3>
              <p>完善的医生信息管理平台</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">
              <h3>数据统计</h3>
              <p>实时的医院运营数据分析</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录区域 -->
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号信息</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              class="login-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              class="login-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '立即登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-tips">
          <div class="tips-header">
            <span>💡 测试账号</span>
          </div>
          <div class="tips-content">
            <div class="tip-item">
              <span class="tip-label">医生端：</span>
              <span class="tip-value">doctor / 123456</span>
            </div>
            <div class="tip-item">
              <span class="tip-label">管理端：</span>
              <span class="tip-value">admin / 123456</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    const result = await userStore.loginUser({
      username: loginForm.username,
      password: loginForm.password
    })
    
    console.log('登录结果:', result)
    
    if (result.success) {
      ElMessage.success('登录成功')
      
      // 确保状态已更新
      console.log('用户信息:', userStore.user)
      console.log('用户角色:', userStore.user?.role)
      console.log('是否已登录:', userStore.isLoggedIn)
      
      // 使用nextTick确保状态更新完成后再跳转
      await nextTick()
      
      // 根据用户角色和状态跳转到对应页面
      const userRole = userStore.user?.role
      const userStatus = userStore.user?.status
      
      if (userRole === 'doctor') {
        // 医生角色需要检查状态
        if (userStatus === 'pending_approval') {
          // 待审核状态，跳转到信息提交页面
          router.push('/doctor/pending-approval')
        } else if (userStatus === 'active') {
          // 已激活状态，跳转到医生工作台
          router.push('/doctor')
        } else if (userStatus === 'inactive') {
          // 已拒绝状态，显示提示信息
          ElMessage.error('您的账号审核未通过，请联系管理员')
          return
        } else {
          // 其他状态
          router.push('/doctor')
        }
      } else if (userRole === 'admin') {
        router.push('/admin')
      } else if (userRole === 'patient') {
        router.push('/patient')
      } else {
        console.error('未知用户角色:', userRole)
        ElMessage.error('用户角色异常，请联系管理员')
      }
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  margin: 0;
  padding: 0;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

/* 左侧装饰区域 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.login-left::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.05)"/><circle cx="20" cy="80" r="0.5" fill="rgba(255,255,255,0.05)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.decoration {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 400px;
  padding: 40px;
}

.logo-section {
  margin-bottom: 60px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.logo-icon svg {
  width: 40px;
  height: 40px;
  color: white;
}

.logo-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 10px 0;
  background: linear-gradient(45deg, #fff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.feature-item {
  display: flex;
  align-items: center;
  text-align: left;
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 15px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-icon {
  font-size: 32px;
  margin-right: 20px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

.feature-text h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
}

.feature-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
  line-height: 1.4;
}

/* 右侧登录区域 */
.login-right {
  flex: 1;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  background: white;
  border-radius: 20px;
  padding: 50px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  color: #1a202c;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.login-header p {
  color: #718096;
  font-size: 16px;
  margin: 0;
}

.login-form {
  margin-bottom: 30px;
}

.login-input {
  margin-bottom: 20px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  box-shadow: none;
  padding: 12px 16px;
  transition: all 0.3s ease;
}

.login-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
}

.login-tips {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.tips-header {
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.tip-label {
  color: #718096;
  font-weight: 500;
}

.tip-value {
  color: #2d3748;
  font-weight: 600;
  font-family: 'Courier New', monospace;
  background: #fff;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-left {
    flex: none;
    height: 40vh;
  }
  
  .decoration {
    padding: 20px;
  }
  
  .feature-list {
    display: none;
  }
  
  .logo-title {
    font-size: 36px;
  }
  
  .login-right {
    flex: 1;
    padding: 20px;
  }
  
  .login-box {
    padding: 30px;
  }
}
</style>