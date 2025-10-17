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
import AdminDashboard from '@/views/admin/Dashboard.vue'
import AdminDoctors from '@/views/admin/Doctors.vue'
import AdminSchedule from '@/views/admin/Schedule.vue'
import AdminReports from '@/views/admin/Reports.vue'

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
        meta: { title: '患者管理' }
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
        meta: { title: '管理后台' }
      },
      {
        path: 'doctors',
        name: 'AdminDoctors',
        component: AdminDoctors,
        meta: { title: '医生管理' }
      },
      {
        path: 'schedule',
        name: 'AdminSchedule',
        component: AdminSchedule,
        meta: { title: '排班管理' }
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: AdminReports,
        meta: { title: '统计报表' }
      }
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
  const userRole = userStore.user?.role

  // 如果需要认证但没有token，跳转到登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 如果已登录访问登录页，根据角色跳转到对应页面
  if (to.path === '/login' && token) {
    if (userRole === 'doctor') {
      next('/doctor')
    } else if (userRole === 'admin') {
      next('/admin')
    } else {
      next()
    }
    return
  }

  // 检查角色权限
  if (to.meta.role && userRole !== to.meta.role) {
    // 角色不匹配，跳转到对应角色的首页
    if (userRole === 'doctor') {
      next('/doctor')
    } else if (userRole === 'admin') {
      next('/admin')
    } else {
      next('/login')
    }
    return
  }

  next()
})

export default router