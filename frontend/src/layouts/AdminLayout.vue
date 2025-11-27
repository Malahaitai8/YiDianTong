<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="logo">
        <h2>管理员信息</h2>
      </div>
      
      <nav class="nav-menu">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path" 
          class="nav-item"
          :class="{ active: isMenuActive(item) }"
        >
          <span class="nav-text">{{ item.name }}</span>
        </router-link>
      </nav>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="header">
        <div class="page-title">
          <h1>{{ $route.meta.title || '管理员后台' }}</h1>
        </div>
        
        <div class="user-info">
          <span class="user-name">管理员</span>
          <el-dropdown @command="handleCommand">
            <div class="user-menu">
              <span class="user-avatar">管</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="main-content">
        <router-view :key="$route.fullPath" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menuItems = ref([
  { path: '/admin/dashboard', name: '数据概览' },
  { path: '/admin/doctors', name: '医生管理' },
  { path: '/admin/doctor-change', name: '医生变更审核' },
  { path: '/admin/patients', name: '患者管理' },
  { path: '/admin/appointments', name: '预约管理' },
  { path: '/admin/admins', name: '管理员信息' },
  { path: '/admin/whitelist', name: '白名单管理' },
  { path: '/admin/departments', name: '科室管理' },
  { path: '/admin/schedule', name: '排班管理' },
  { path: '/admin/schedule-rules', name: '排班规则' },
  { path: '/admin/source-management', name: '号源管理' },
  { path: '/admin/qa-management', name: 'QA管理' },
  { path: '/admin/reports', name: '报表统计' }
])

// 判断菜单项是否应该高亮
const isMenuActive = (item) => {
  // 对于数据概览，/admin 和 /admin/dashboard 都应该高亮
  if (item.name === '数据概览') {
    return route.path === '/admin' || route.path === '/admin/dashboard'
  }
  // 其他菜单项使用精确匹配
  return route.path === item.path
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      
      // 执行退出登录
      const result = await userStore.logoutUser()
      
      if (result.success) {
        ElMessage.success(result.message || '退出登录成功')
        // 跳转到登录页面
        router.push('/login')
      } else {
        ElMessage.error(result.message || '退出登录失败')
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('退出登录失败')
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #f5f7fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  margin: 0;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
}

.sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #e1e8ed;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  padding: 0 25px;
  margin-bottom: 30px;
  border-bottom: 1px solid #e1e8ed;
  padding-bottom: 20px;
}

.logo h2 {
  color: #2c3e50;
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.nav-menu {
  flex: 1;
  padding: 0 15px;
}

.nav-item {
  display: block;
  padding: 12px 20px;
  margin-bottom: 5px;
  color: #5a6c7d;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: #f8f9fa;
  color: #2c3e50;
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.nav-item:hover:not(.active) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}

.nav-text {
  font-size: 0.95rem;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: white;
  padding: 20px 30px;
  border-bottom: 1px solid #e1e8ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-title h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.6rem;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #5a6c7d;
  font-weight: 500;
}

.user-menu {
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: #6c5ce7;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.main-content {
  flex: 1;
  padding: 25px;
  overflow-y: auto;
  background: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 60px;
    padding: 15px 0;
  }
  
  .logo h2 {
    display: none;
  }
  
  .nav-text {
    display: none;
  }
  
  .nav-item {
    text-align: center;
    padding: 12px 8px;
  }
  
  .header {
    padding: 15px 20px;
  }
  
  .main-content {
    padding: 20px;
  }
}
</style>