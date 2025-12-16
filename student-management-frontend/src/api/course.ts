import request from '@/utils/request'
import type { ApiResponse, PageResponse, Course, CourseFormData, CourseSchedule, CourseAttachment } from '@/types'

/** 获取课程列表 */
export const getCourseList = (params: {
  current?: number
  size?: number
  keyword?: string
  status?: string
  semester?: string
}) => {
  return request.get<any, ApiResponse<PageResponse<Course>>>('/course/list', { params })
}

/** 获取课程详情 */
export const getCourse = (id: number) => {
  return request.get<any, ApiResponse<Course>>(`/course/${id}`)
}

/** 创建课程 */
export const createCourse = (data: CourseFormData) => {
  return request.post<any, ApiResponse<Course>>('/course/create', data)
}

/** 更新课程 */
export const updateCourse = (id: number, data: Partial<CourseFormData>) => {
  return request.post<any, ApiResponse<Course>>(`/course/${id}`, data)
}

/** 删除课程 */
export const deleteCourse = (id: number) => {
  return request.delete<any, ApiResponse<null>>(`/course/${id}`)
}

/** 添加课程日程 */
export const addCourseSchedule = (courseId: number, data: CourseSchedule) => {
  return request.post<any, ApiResponse<CourseSchedule>>(`/course/${courseId}/schedules`, data)
}

/** 删除课程日程 */
export const deleteCourseSchedule = (scheduleId: number) => {
  return request.delete<any, ApiResponse<null>>(`/course/schedules/${scheduleId}`)
}

/** 添加课程附件 */
export const addCourseAttachment = (courseId: number, data: CourseAttachment) => {
  return request.post<any, ApiResponse<CourseAttachment>>(`/course/${courseId}/attachments`, data)
}
