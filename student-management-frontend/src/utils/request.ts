import axios, { type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/user'
import type { ApiResponse } from '@/types'

// 创建 axios 实例
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
instance.interceptors.response.use(
  (response: AxiosResponse): any => {
    const res = response.data as ApiResponse
    // 业务错误处理
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401 未授权
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error: AxiosError<ApiResponse>) => {
    let message = '请求失败'
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = data?.message || '登录已过期，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          message = data?.message || '权限不足'
          break
        case 404:
          message = data?.message || '资源不存在'
          break
        case 429:
          message = data?.message || '请求过于频繁，请稍后再试'
          break
        case 500:
          message = data?.message || '服务器错误'
          break
        default:
          message = data?.message || `请求失败(${status})`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时，请稍后再试'
    } else if (error.message.includes('Network Error')) {
      message = '网络错误，请检查网络连接'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default instance
