import request from '@/utils/request'
import type { ApiResponse, PageResponse, Student, Course } from '@/types'

/** 搜索课程 */
export const searchCourses = (params: {
  keyword: string
  current?: number
  size?: number
}) => {
  return request.get<any, ApiResponse<PageResponse<Course>>>('/search/courses', { params })
}

/** 搜索学生 */
export const searchStudents = (params: {
  keyword: string
  current?: number
  size?: number
}) => {
  return request.get<any, ApiResponse<PageResponse<Student>>>('/search/students', { params })
}

/** 全局搜索 */
export const globalSearch = (keyword: string) => {
  return request.get<any, ApiResponse<{
    students: Student[]
    courses: Course[]
  }>>('/search/global', { params: { keyword } })
}

/** 热门搜索关键词 */
export const getPopularKeywords = (limit?: number) => {
  return request.get<any, ApiResponse<string[]>>('/search/popular-keywords', {
    params: { limit }
  })
}
