import request from '@/utils/request'
import type { ApiResponse, PageResponse, Student, StudentFormData } from '@/types'

/** 获取学生列表 */
export const getStudentList = (params: {
  current?: number
  size?: number
  keyword?: string
  classId?: number
  status?: string
}) => {
  return request.get<any, ApiResponse<PageResponse<Student>>>('/student/list', { params })
}

/** 获取学生详情 */
export const getStudent = (id: number) => {
  return request.get<any, ApiResponse<Student>>(`/student/${id}`)
}

/** 创建学生 */
export const createStudent = (data: StudentFormData) => {
  return request.post<any, ApiResponse<Student>>('/student', data)
}

/** 更新学生 */
export const updateStudent = (id: number, data: Partial<StudentFormData>) => {
  return request.put<any, ApiResponse<Student>>(`/student/${id}`, data)
}

/** 删除学生 */
export const deleteStudent = (id: number) => {
  return request.delete<any, ApiResponse<null>>(`/student/${id}`)
}

/** 批量删除学生 */
export const batchDeleteStudents = (ids: number[]) => {
  return request.post<any, ApiResponse<null>>('/student/batch-delete', { ids })
}
