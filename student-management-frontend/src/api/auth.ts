import request from '@/utils/request'
import type { ApiResponse, LoginRequest, LoginResponse, RegisterRequest, CaptchaResponse, User } from '@/types'

/** 获取验证码 */
export const getCaptcha = () => {
  return request.get<any, ApiResponse<CaptchaResponse>>('/auth/captcha')
}

/** 用户登录 */
export const login = (data: LoginRequest) => {
  return request.post<any, ApiResponse<LoginResponse>>('/auth/login', data)
}

/** 用户注册 */
export const register = (data: RegisterRequest) => {
  return request.post<any, ApiResponse<LoginResponse>>('/auth/register', data)
}

/** 刷新 Token */
export const refreshToken = (refreshToken: string) => {
  return request.post<any, ApiResponse<LoginResponse>>('/auth/refresh', { refreshToken })
}

/** 登出 */
export const logout = () => {
  return request.post<any, ApiResponse<null>>('/auth/logout')
}

/** 获取当前用户信息 */
export const getCurrentUser = () => {
  return request.get<any, ApiResponse<User>>('/auth/user')
}
