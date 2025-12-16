import request from '@/utils/request'
import type { ApiResponse, Menu, Role, Permission } from '@/types'

/** 获取当前用户角色 */
export const getMyRoles = () => {
  return request.get<any, ApiResponse<string[]>>('/permission/my-roles')
}

/** 获取当前用户权限 */
export const getMyPermissions = () => {
  return request.get<any, ApiResponse<string[]>>('/permission/my-permissions')
}

/** 获取用户菜单 */
export const getMenus = () => {
  return request.get<any, ApiResponse<Menu[]>>('/permission/menus')
}

/** 获取所有角色（管理员） */
export const getAllRoles = () => {
  return request.get<any, ApiResponse<Role[]>>('/permission/roles')
}

/** 获取所有权限（管理员） */
export const getAllPermissions = () => {
  return request.get<any, ApiResponse<Permission[]>>('/permission/all')
}

/** 给用户添加角色（管理员） */
export const addUserRole = (userId: number, roleId: number) => {
  return request.post<any, ApiResponse<null>>(`/permission/users/${userId}/roles/${roleId}`)
}

/** 移除用户角色（管理员） */
export const removeUserRole = (userId: number, roleId: number) => {
  return request.delete<any, ApiResponse<null>>(`/permission/users/${userId}/roles/${roleId}`)
}
