import type { RouteRecordRaw } from 'vue-router'

// 公共路由（无需登录）
export const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

// 需要认证的路由
export const authRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' }
      },
      // 学生管理 - 使用绝对路径避免嵌套问题
      {
        path: 'student/list',
        name: 'StudentList',
        component: () => import('@/views/student/list.vue'),
        meta: { title: '学生列表', icon: 'User', roles: ['admin', 'teacher'], parentTitle: '学生管理' }
      },
      // 课程管理
      {
        path: 'course/list',
        name: 'CourseList',
        component: () => import('@/views/course/list.vue'),
        meta: { title: '课程列表', icon: 'Reading', roles: ['admin', 'teacher'], parentTitle: '课程管理' }
      },
      {
        path: 'course/detail/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/detail.vue'),
        meta: { title: '课程详情', icon: 'Reading', hidden: true, parentTitle: '课程管理' }
      },
      // 选课中心
      {
        path: 'enrollment/market',
        name: 'EnrollmentMarket',
        component: () => import('@/views/enrollment/market.vue'),
        meta: { title: '选课大厅', icon: 'Collection', roles: ['student'], parentTitle: '选课中心' }
      },
      {
        path: 'enrollment/mine',
        name: 'MyEnrollment',
        component: () => import('@/views/enrollment/mine.vue'),
        meta: { title: '我的课程', icon: 'Collection', roles: ['student'], parentTitle: '选课中心' }
      },
      // 消息通知
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/notification/index.vue'),
        meta: { title: '消息通知', icon: 'Bell' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      }
    ]
  }
]

// 所有路由
export const routes: RouteRecordRaw[] = [
  ...publicRoutes,
  ...authRoutes,
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]
