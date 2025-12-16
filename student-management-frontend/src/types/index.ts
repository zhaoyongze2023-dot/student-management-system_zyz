// ==================== 基础类型 ====================

/** API 统一响应格式 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/** 分页响应 */
export interface PageResponse<T> {
  total: number
  current: number
  size: number
  records: T[]
}

/** 分页请求参数 */
export interface PageRequest {
  current?: number
  size?: number
  sortBy?: string
  keyword?: string
}

// ==================== 用户相关 ====================

/** 用户信息 */
export interface User {
  id: number
  username: string
  email: string
  phone?: string
  avatar?: string
  roles: string[]
  createdAt: string
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
  captchaKey?: string
  captcha?: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  refreshToken: string
  user: User
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  password: string
  email: string
  phone?: string
}

/** 验证码响应 */
export interface CaptchaResponse {
  key: string
  image: string
}

// ==================== 学生相关 ====================

/** 学生信息 */
export interface Student {
  id: number
  studentId: string
  name: string
  classId?: number
  className?: string
  gender: string
  age?: number
  phone?: string
  email?: string
  major?: string
  admissionYear?: number
  status: string
  avatarUrl?: string
  createdAt?: string
  updatedAt?: string
}

/** 学生表单数据 */
export interface StudentFormData {
  studentId: string
  name: string
  classId?: number
  gender: string
  age?: number
  phone?: string
  email?: string
  major?: string
  admissionYear?: number
  status?: string
  avatarUrl?: string
}

// ==================== 课程相关 ====================

/** 课程信息 */
export interface Course {
  id: number
  name: string
  code?: string
  description?: string
  teacherId?: number
  teacherName?: string
  category?: string
  capacity: number
  enrolled?: number
  status: string
  startDate?: string
  endDate?: string
  credits?: number
  location?: string
  syllabus?: string
  requirements?: string
  schedules?: CourseSchedule[]
  attachments?: CourseAttachment[]
  createdAt?: string
  updatedAt?: string
}

/** 课程日程 */
export interface CourseSchedule {
  id?: number
  dayOfWeek: number
  startTime: string
  endTime: string
  location?: string
}

/** 课程附件 */
export interface CourseAttachment {
  id?: number
  fileName: string
  fileUrl: string
  fileType?: string
  fileSize?: number
}

/** 课程表单数据 */
export interface CourseFormData {
  name: string
  code?: string
  description?: string
  teacherId?: number
  category?: string
  capacity: number
  status?: string
  startDate?: string
  endDate?: string
  credits?: number
  location?: string
  syllabus?: string
  requirements?: string
}

// ==================== 选课相关 ====================

/** 选课记录 */
export interface StudentCourse {
  id: number
  studentId: number
  studentName?: string
  courseId: number
  courseName?: string
  courseCode?: string
  teacherName?: string
  credits?: number
  enrolledAt: string
  status: string
  grade?: number
  location?: string
  schedules?: CourseSchedule[]
  createdAt?: string
  updatedAt?: string
}

// ==================== 消息相关 ====================

/** 消息 */
export interface Message {
  id: number
  senderId: number
  senderName?: string
  receiverId: number
  content: string
  isRead: boolean
  type?: string
  createdAt: string
}

// ==================== 权限相关 ====================

/** 角色 */
export interface Role {
  id: number
  code: string
  name: string
  description?: string
}

/** 权限 */
export interface Permission {
  id: number
  code: string
  name: string
  description?: string
}

/** 菜单 */
export interface Menu {
  id: number
  name: string
  path: string
  icon?: string
  sort?: number
  children?: Menu[]
}

// ==================== 数据字典 ====================

/** 班级 */
export interface ClassInfo {
  id: number
  name: string
}

/** 字典项 */
export interface DictItem {
  value: string
  label: string
}

// ==================== 常量 ====================

/** 角色代码 */
export const RoleCodes = {
  STUDENT: 'student',
  TEACHER: 'teacher',
  ADMIN: 'admin'
} as const

/** 权限代码 */
export const PermissionCodes = {
  STUDENT_VIEW: 'student:view',
  STUDENT_EDIT: 'student:edit',
  STUDENT_DELETE: 'student:delete',
  COURSE_VIEW: 'course:view',
  COURSE_CREATE: 'course:create',
  COURSE_EDIT: 'course:edit',
  COURSE_DELETE: 'course:delete',
  ENROLLMENT_VIEW: 'enrollment:view',
  ENROLLMENT_MANAGE: 'enrollment:manage',
  GRADE_VIEW: 'grade:view',
  GRADE_EDIT: 'grade:edit',
  DATA_EXPORT: 'data:export'
} as const

/** 学生状态映射 */
export const STUDENT_STATUS_MAP: Record<string, { label: string; color: string }> = {
  active: { label: '正常', color: 'success' },
  inactive: { label: '休学', color: 'warning' },
  graduated: { label: '毕业', color: 'info' }
}

/** 课程状态映射 */
export const COURSE_STATUS_MAP: Record<string, { label: string; color: string }> = {
  open: { label: '开放选课', color: 'success' },
  closed: { label: '已关闭', color: 'danger' },
  full: { label: '已满员', color: 'warning' },
  archived: { label: '已归档', color: 'info' }
}

/** 性别映射 */
export const GENDER_MAP: Record<string, string> = {
  M: '男',
  F: '女'
}

/** 星期映射 */
export const DAY_OF_WEEK_MAP: Record<number, string> = {
  1: '周一',
  2: '周二',
  3: '周三',
  4: '周四',
  5: '周五',
  6: '周六',
  7: '周日'
}
