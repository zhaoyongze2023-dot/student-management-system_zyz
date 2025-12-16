import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/** 上传头像 */
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResponse<{ url: string }>>('/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/** 上传通用文件 */
export const uploadFile = (file: File, directory?: string) => {
  const formData = new FormData()
  formData.append('file', file)
  if (directory) {
    formData.append('directory', directory)
  }
  return request.post<any, ApiResponse<{ url: string }>>('/upload/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/** 更新学生头像 */
export const uploadStudentAvatar = (id: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResponse<{ url: string }>>(`/upload/student/${id}/avatar`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
