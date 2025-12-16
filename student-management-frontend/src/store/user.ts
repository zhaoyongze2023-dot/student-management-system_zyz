import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { login as loginApi, logout as logoutApi, getCurrentUser, register as registerApi } from '@/api/auth'
import { getMyRoles, getMyPermissions } from '@/api/permission'
import type { User, LoginRequest, RegisterRequest } from '@/types'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string | null>(storage.getToken())
  const refreshToken = ref<string | null>(storage.getRefreshToken())
  const user = ref<User | null>(storage.getUser())
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('admin'))
  const isTeacher = computed(() => roles.value.includes('teacher'))
  const isStudent = computed(() => roles.value.includes('student'))

  // Actions
  const login = async (data: LoginRequest) => {
    const res = await loginApi(data)
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    user.value = res.data.user
    
    storage.setToken(res.data.token)
    storage.setRefreshToken(res.data.refreshToken)
    storage.setUser(res.data.user)
    
    // 获取角色和权限
    await fetchRolesAndPermissions()
    
    return res
  }

  const register = async (data: RegisterRequest) => {
    const res = await registerApi(data)
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    user.value = res.data.user
    
    storage.setToken(res.data.token)
    storage.setRefreshToken(res.data.refreshToken)
    storage.setUser(res.data.user)
    
    await fetchRolesAndPermissions()
    
    return res
  }

  const logout = async () => {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch (e) {
      // 忽略登出错误
    } finally {
      token.value = null
      refreshToken.value = null
      user.value = null
      roles.value = []
      permissions.value = []
      storage.clearAuth()
    }
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    const res = await getCurrentUser()
    user.value = res.data
    storage.setUser(res.data)
  }

  const fetchRolesAndPermissions = async () => {
    if (!token.value) return
    try {
      const [rolesRes, permissionsRes] = await Promise.all([
        getMyRoles(),
        getMyPermissions()
      ])
      roles.value = rolesRes.data || []
      permissions.value = permissionsRes.data || []
    } catch (e) {
      console.error('获取角色权限失败', e)
    }
  }

  const hasPermission = (permission: string) => {
    return permissions.value.includes(permission)
  }

  const hasRole = (role: string) => {
    return roles.value.includes(role)
  }

  const hasAnyRole = (roleList: string[]) => {
    return roleList.some(role => roles.value.includes(role))
  }

  // 初始化时获取角色权限
  if (token.value) {
    fetchRolesAndPermissions()
  }

  return {
    token,
    refreshToken,
    user,
    roles,
    permissions,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    login,
    register,
    logout,
    fetchUserInfo,
    fetchRolesAndPermissions,
    hasPermission,
    hasRole,
    hasAnyRole
  }
})
