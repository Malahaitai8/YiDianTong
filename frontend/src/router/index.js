import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 导入布局组件
import DoctorLayout from '@/layouts/DoctorLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

// 导入页面组件
import Login from '@/views/Login.vue'
import DoctorDashboard from '@/views/doctor/Dashboard.vue'
import DoctorSchedule from '@/views/doctor/Schedule.vue'
import DoctorPatients from '@/views/doctor/Patients.vue'
import DoctorProfile from '@/views/doctor/Profile.vue'

import AdminDashboard from '@/views/admin/Dashboard.vue'
import AdminDoctors from '@/views/admin/Doctors.vue'
import AdminDepartments from '@/views/admin/Departments.vue'
import AdminSchedule from '@/views/admin/Schedule.vue'
import AdminSourceManagement from '@/views/admin/SourceManagement.vue'
import AdminQAManagement from '@/views/admin/QAManagement.vue'
import AdminReports from '@/views/admin/Reports.vue'
import AdminScheduleRules from '@/views/admin/ScheduleRules.vue'
import AdminScheduleSettings from '@/views/admin/ScheduleSettings.vue'
import AdminPatients from '@/views/admin/Patients.vue'
import AdminAppointments from '@/views/admin/Appointments.vue'
import AdminUsers from '@/views/admin/AdminUsers.vue'
import AdminWhitelist from '@/views/admin/Whitelist.vue'
import AdminApplicationRequests from '@/views/admin/ApplicationRequests.vue'
import DoctorApplicationRequests from '@/views/doctor/ApplicationRequests.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },

  {
    path: '/doctor',
    component: DoctorLayout,
    meta: { requiresAuth: true, role: 'doctor' },
    children: [
      {
        path: '',
        redirect: '/doctor/dashboard'
      },
      {
        path: 'dashboard',
        name: 'DoctorDashboard',
        component: DoctorDashboard,
        meta: { title: '工作台' }
      },
      {
        path: 'schedule',
        name: 'DoctorSchedule',
        component: DoctorSchedule,
        meta: { title: '我的排班' }
      },
      {
        path: 'patients',
        name: 'DoctorPatients',
        component: DoctorPatients,
        meta: { title: '患者查看' }
      },
      {
        path: 'profile',
        name: 'DoctorProfile',
        component: DoctorProfile,
        meta: { title: '个人信息' }
      },
      {
        path: 'applications',
        name: 'DoctorApplicationRequests',
        component: DoctorApplicationRequests,
        meta: { title: '我的申请' }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { title: '数据概览' }
      },
      {
        path: 'doctors',
        name: 'AdminDoctors',
        component: AdminDoctors,
        meta: { title: '医生管理' }
      },
      {
        path: 'patients',
        name: 'AdminPatients',
        component: AdminPatients,
        meta: { title: '患者管理' }
      },
      {
        path: 'appointments',
        name: 'AdminAppointments',
        component: AdminAppointments,
        meta: { title: '预约管理' }
      },
      {
        path: 'admins',
        name: 'AdminUsers',
        component: AdminUsers,
        meta: { title: '管理员信息' }
      },
      {
        path: 'whitelist',
        name: 'AdminWhitelist',
        component: AdminWhitelist,
        meta: { title: '白名单管理' }
      },
      {
        path: 'doctor-change',
        name: 'AdminDoctorChangeRequests',
        component: AdminApplicationRequests,
        meta: { title: '申请管理' }
      },
      {
        path: 'departments',
        name: 'AdminDepartments',
        component: AdminDepartments,
        meta: { title: '科室管理' }
      },
      {
        path: 'schedule',
        name: 'AdminSchedule',
        component: AdminSchedule,
        meta: { title: '排班管理' }
      },
      {
        path: 'schedule-rules',
        name: 'AdminScheduleRules',
        component: AdminScheduleRules,
        meta: { title: '排班规则' }
      },
      {
        path: 'schedule-settings',
        name: 'AdminScheduleSettings',
        component: AdminScheduleSettings,
        meta: { title: '号源管理' }
      },
      {
        path: 'qa-management',
        name: 'AdminQAManagement',
        component: AdminQAManagement,
        meta: { title: 'QA管理' }
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: AdminReports,
        meta: { title: '统计报表' }
      },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  const user = userStore.user
  const userRole = user?.role

  console.log('路由守卫执行:', {
    to: to.path,
    from: from.path,
    token: !!token,
    user: user,
    userRole: userRole
  })

  // 1. 检查是否需要认证
  if (to.meta.requiresAuth && !token) {
    console.log('需要认证但无token，跳转到登录页')
    next('/login')
    return
  }

  // 2. 已登录用户访问登录页或根路径，根据角色重定向到对应首页
  if ((to.path === '/login' || to.path === '/') && token && userRole) {
    console.log('已登录用户访问登录页或根路径，准备重定向')
    
    if (userRole === 'doctor') {
      console.log('医生，重定向到工作台')
      next('/doctor')
    } else if (userRole === 'admin') {
      console.log('管理员，重定向到管理后台')
      next('/admin')
    } else if (userRole === 'patient') {
      console.log('患者，重定向到患者端')
      next('/patient')
    } else {
      // 未知角色，清除认证信息并跳转到登录页
      console.log('未知角色，清除认证信息')
      userStore.clearAuth()
      next('/login')
    }
    return
  }

  // 3. 检查角色权限
  if (to.meta.role && userRole !== to.meta.role) {
    console.log('角色权限不匹配:', { required: to.meta.role, actual: userRole })
    // 角色不匹配，重定向到对应角色的首页或未授权页面
    if (userRole === 'doctor') {
      next('/doctor')
    } else if (userRole === 'admin') {
      next('/admin')
    } else if (userRole === 'patient') {
      next('/patient')
    } else {
      // 未知角色或无权限，跳转到登录页
      console.log('未知角色或无权限，跳转到登录页')
      next('/login')
    }
    return
  }

  // 4. 检查医生状态权限
  if (userRole === 'doctor') {
    const userStatus = user?.status
    
    // 被禁用的医生不能访问任何医生页面
    if (userStatus === 'inactive') {
      console.log('被禁用的医生尝试访问医生页面，跳转到登录页')
      userStore.clearAuth()
      next('/login')
      return
    }
  }

  // 5. 通过所有检查，允许访问
  console.log('通过所有检查，允许访问')
  next()
})

export default router