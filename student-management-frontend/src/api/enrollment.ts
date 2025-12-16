import request from '@/utils/request'
import type { ApiResponse, PageResponse, StudentCourse } from '@/types'

/** 学生选课 */
export const enrollCourse = (courseId: number) => {
  return request.post<any, ApiResponse<StudentCourse>>('/student-course/enroll', { courseId })
}

/** 学生退课 */
export const dropCourse = (enrollmentId: number) => {
  return request.delete<any, ApiResponse<null>>(`/student-course/${enrollmentId}`)
}

/** 获取已选课程 */
export const getEnrolledCourses = (params?: {
  page?: number
  pageSize?: number
  status?: string
}) => {
  return request.get<any, ApiResponse<PageResponse<StudentCourse>>>('/student-course/enrolled', { params })
}

/** 获取可选课程 */
export const getAvailableCourses = (params?: {
  page?: number
  pageSize?: number
}) => {
  return request.get<any, ApiResponse<PageResponse<StudentCourse>>>('/student-course/available', { params })
}

/** 获取选课历史 */
export const getEnrollmentHistory = () => {
  return request.get<any, ApiResponse<StudentCourse[]>>('/student-course/history')
}

/** 获取活跃选课 */
export const getActiveEnrollments = () => {
  return request.get<any, ApiResponse<StudentCourse[]>>('/student-course/active')
}
