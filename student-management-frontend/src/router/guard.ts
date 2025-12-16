import type { Router } from 'vue-router'
import { useUserStore } from '@/store/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// 白名单路由
const whiteList = ['/login', '/register', '/403', '/404']

export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    NProgress.start()

    const userStore = useUserStore()
    const token = userStore.token

    // 设置页面标题
    document.title = `${to.meta.title || '学生管理系统'} - 学生管理系统`

    if (token) {
      if (to.path === '/login') {
        // 已登录，跳转到首页
        next({ path: '/' })
        NProgress.done()
      } else {
        // 检查角色权限
        const roles = userStore.roles
        if (roles.length === 0) {
          // 获取用户角色
          await userStore.fetchRolesAndPermissions()
        }
        
        // 检查路由角色要求
        const requiredRoles = to.meta.roles as string[] | undefined
        if (requiredRoles && requiredRoles.length > 0) {
          const hasRole = userStore.hasAnyRole(requiredRoles)
          if (hasRole) {
            next()
          } else {
            next({ path: '/403' })
          }
        } else {
          next()
        }
      }
    } else {
      // 未登录
      if (whiteList.includes(to.path)) {
        next()
      } else {
        next(`/login?redirect=${to.path}`)
        NProgress.done()
      }
    }
  })

  router.afterEach(() => {
    NProgress.done()
  })
}
