# 前端 API 对接完整文档

> 后端接口已全部测试通过，本文档为前端开发人员提供详细的接口对接指南

---

## 📋 目录

1. [基础配置](#1-基础配置)
2. [统一响应格式](#2-统一响应格式)
3. [认证模块 API](#3-认证模块-api)
4. [学生管理 API](#4-学生管理-api)
5. [课程管理 API](#5-课程管理-api)
6. [选课管理 API](#6-选课管理-api)
7. [权限管理 API](#7-权限管理-api)
8. [文件上传 API](#8-文件上传-api)
9. [搜索 API](#9-搜索-api)
10. [消息通知 API](#10-消息通知-api)
11. [数据字典 API](#11-数据字典-api)
12. [错误码说明](#12-错误码说明)
13. [TypeScript 类型定义](#13-typescript-类型定义)
14. [Axios 配置示例](#14-axios-配置示例)

---

## 1. 基础配置

### 1.1 服务地址

| 环境 | 地址 |
|------|------|
| 开发环境 | `http://localhost:8080/api` |
| 生产环境 | `https://your-domain.com/api` |

### 1.2 请求头配置

```http
Content-Type: application/json
Authorization: Bearer <token>
```

### 1.3 认证方式

- 使用 **JWT Token** 认证
- Token 放在请求头 `Authorization` 中
- 格式：`Bearer <token>`（注意 Bearer 后有空格）

---

## 2. 统一响应格式

### 2.1 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 2.2 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "current": 1,
    "size": 10,
    "records": [ ... ]
  }
}
```

### 2.3 错误响应

```json
{
  "code": 400,
  "message": "错误描述信息",
  "data": null
}
```

---

## 3. 认证模块 API

### 3.1 获取验证码

获取图形验证码用于登录。

```
GET /api/auth/captcha
```

**请求参数**：无

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "key": "captcha_1702540800000_abc123",
    "image": "data:image/png;base64,iVBORw0KGgoAAAANSUhE..."
  }
}
```

**前端使用**：
```typescript
// 将 image 直接赋值给 img 标签的 src
<img :src="captchaData.image" @click="refreshCaptcha" />
```

---

### 3.2 用户登录

```
POST /api/auth/login
```

**请求体**：
```json
{
  "username": "admin",
  "password": "123456",
  "captchaKey": "captcha_1702540800000_abc123",
  "captcha": "a1b2"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | ✅ | 用户名，3-50字符 |
| password | string | ✅ | 密码，6-100字符 |
| captchaKey | string | ⚠️ | 验证码key（开发环境可选） |
| captcha | string | ⚠️ | 验证码（开发环境可选） |

**成功响应**：
```json
{
  "code": 200,
  "message": "login success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "phone": "13800138000",
      "avatar": null,
      "roles": ["admin"],
      "permissions": ["student:view", "student:edit", "course:view", "course:create"],
      "createdAt": "2024-01-01T00:00:00Z"
    }
  }
}
```

**错误响应**：
```json
// 401 - 用户名或密码错误
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}

// 401 - 验证码错误
{
  "code": 401,
  "message": "invalid captcha",
  "data": null
}

// 429 - 登录次数过多被锁定
{
  "code": 429,
  "message": "账户已被锁定，请15分钟后重试",
  "data": null
}
```

---

### 3.3 用户注册

```
POST /api/auth/register
```

**请求体**：
```json
{
  "username": "newuser",
  "password": "123456",
  "email": "newuser@example.com",
  "phone": "13900139000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | ✅ | 用户名，3-50字符，唯一 |
| password | string | ✅ | 密码，6-100字符 |
| email | string | ✅ | 邮箱地址 |
| phone | string | ⬚ | 手机号 |

**成功响应**：
```json
{
  "code": 200,
  "message": "register success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 5,
      "username": "newuser",
      "email": "newuser@example.com",
      "phone": "13900139000",
      "roles": ["student"],
      "permissions": [],
      "createdAt": "2024-12-14T10:30:00Z"
    }
  }
}
```

---

### 3.4 刷新 Token

```
POST /api/auth/refresh
```

**请求体**：
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**成功响应**：
```json
{
  "code": 200,
  "message": "token refreshed",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 3.5 登出

```
POST /api/auth/logout
```

**请求头**：需要 Authorization

**响应**：
```json
{
  "code": 200,
  "message": "logout success",
  "data": null
}
```

---

### 3.6 获取当前用户信息

```
GET /api/auth/user
```

**请求头**：需要 Authorization

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "/uploads/avatars/admin.jpg",
    "roles": ["admin"],
    "permissions": ["student:view", "student:edit", "course:view"],
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

---

## 4. 学生管理 API

### 4.1 获取学生列表

```
GET /api/student/list
```

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | int | ⬚ | 1 | 当前页码（从1开始） |
| size | int | ⬚ | 10 | 每页条数 |
| keyword | string | ⬚ | - | 搜索关键词（姓名/学号） |
| classId | long | ⬚ | - | 班级ID筛选 |
| status | string | ⬚ | - | 状态筛选：active/inactive/graduated |

**请求示例**：
```
GET /api/student/list?current=1&size=10&keyword=张&status=active
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "studentId": "S202401001",
        "name": "张三",
        "classId": 1,
        "className": "计算机科学与技术1班",
        "gender": "M",
        "age": 20,
        "phone": "13800138001",
        "email": "zhangsan@example.com",
        "major": "计算机科学与技术",
        "admissionYear": 2024,
        "status": "active",
        "avatarUrl": "/uploads/avatars/student1.jpg",
        "createdAt": "2024-09-01T00:00:00Z",
        "updatedAt": "2024-12-01T10:30:00Z"
      }
    ]
  }
}
```

---

### 4.2 获取学生详情

```
GET /api/student/{id}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 学生ID |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "studentId": "S202401001",
    "name": "张三",
    "classId": 1,
    "className": "计算机科学与技术1班",
    "gender": "M",
    "age": 20,
    "phone": "13800138001",
    "email": "zhangsan@example.com",
    "major": "计算机科学与技术",
    "admissionYear": 2024,
    "status": "active",
    "avatarUrl": "/uploads/avatars/student1.jpg",
    "createdAt": "2024-09-01T00:00:00Z",
    "updatedAt": "2024-12-01T10:30:00Z"
  }
}
```

---

### 4.3 创建学生

> ⚠️ 需要 **ADMIN** 角色

```
POST /api/student
```

**请求体**：
```json
{
  "studentId": "S202401010",
  "name": "李四",
  "classId": 1,
  "gender": "M",
  "age": 19,
  "phone": "13800138010",
  "email": "lisi@example.com",
  "major": "软件工程",
  "admissionYear": 2024,
  "status": "active"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentId | string | ✅ | 学号，唯一 |
| name | string | ✅ | 姓名 |
| classId | long | ✅ | 班级ID |
| gender | string | ✅ | 性别：M(男)/F(女) |
| age | int | ⬚ | 年龄 |
| phone | string | ⬚ | 手机号 |
| email | string | ⬚ | 邮箱 |
| major | string | ⬚ | 专业 |
| admissionYear | int | ⬚ | 入学年份 |
| status | string | ⬚ | 状态：active/inactive/graduated |

**成功响应**：
```json
{
  "code": 200,
  "message": "create success",
  "data": {
    "id": 10,
    "studentId": "S202401010",
    "name": "李四",
    ...
  }
}
```

---

### 4.4 更新学生

> ⚠️ 需要 **ADMIN** 角色

```
PUT /api/student/{id}
```

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 学生ID |

**请求体**：（同创建，字段可选）
```json
{
  "name": "李四（改名）",
  "phone": "13900139000"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "update success",
  "data": { ... }
}
```

---

### 4.5 删除学生

> ⚠️ 需要 **ADMIN** 角色

```
DELETE /api/student/{id}
```

**响应**：
```json
{
  "code": 200,
  "message": "delete success",
  "data": null
}
```

---

### 4.6 批量删除学生

> ⚠️ 需要 **ADMIN** 角色

```
POST /api/student/batch-delete
```

**请求体**：
```json
{
  "ids": [1, 2, 3]
}
```

**响应**：
```json
{
  "code": 200,
  "message": "batch delete success",
  "data": null
}
```

---

## 5. 课程管理 API

### 5.1 获取课程列表

```
GET /api/course/list
```

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | int | ⬚ | 1 | 当前页码 |
| size | int | ⬚ | 10 | 每页条数 |
| keyword | string | ⬚ | - | 搜索关键词 |
| status | string | ⬚ | - | 状态：open/closed/full/archived |
| semester | string | ⬚ | - | 学期筛选 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 1,
        "name": "数据库原理",
        "code": "CS301",
        "description": "本课程介绍数据库基础理论...",
        "teacherId": 2,
        "category": "专业必修",
        "capacity": 50,
        "enrolled": 35,
        "status": "open",
        "startDate": "2024-09-01",
        "endDate": "2025-01-15",
        "credits": 3,
        "location": "教学楼A301",
        "syllabus": "课程大纲内容...",
        "requirements": "预修课程：程序设计基础",
        "schedules": [
          {
            "id": 1,
            "dayOfWeek": 1,
            "startTime": "08:00",
            "endTime": "09:40",
            "location": "教学楼A301"
          }
        ],
        "attachments": [],
        "createdAt": "2024-08-01T00:00:00Z",
        "updatedAt": "2024-09-01T00:00:00Z"
      }
    ]
  }
}
```

---

### 5.2 获取课程详情

```
GET /api/course/{id}
```

**响应**：（同列表单条记录格式）

---

### 5.3 创建课程

> ⚠️ 需要 **TEACHER** 或 **ADMIN** 角色

```
POST /api/course/create
```

**请求体**：
```json
{
  "name": "人工智能导论",
  "code": "CS401",
  "description": "介绍人工智能基础概念和应用",
  "teacherId": 2,
  "category": "专业选修",
  "capacity": 40,
  "status": "open",
  "startDate": "2024-09-01",
  "endDate": "2025-01-15",
  "credits": 2,
  "location": "教学楼B202",
  "syllabus": "第一周：绪论...",
  "requirements": "建议先修：数据结构"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | ✅ | 课程名称 |
| code | string | ⬚ | 课程代码 |
| description | string | ⬚ | 课程描述 |
| teacherId | long | ⬚ | 教师ID |
| category | string | ⬚ | 课程分类 |
| capacity | int | ✅ | 课程容量 |
| status | string | ⬚ | 状态：open/closed/full/archived |
| startDate | string | ⬚ | 开始日期（yyyy-MM-dd） |
| endDate | string | ⬚ | 结束日期（yyyy-MM-dd） |
| credits | int | ⬚ | 学分 |
| location | string | ⬚ | 上课地点 |
| syllabus | string | ⬚ | 课程大纲 |
| requirements | string | ⬚ | 先修要求 |

**响应**：
```json
{
  "code": 200,
  "message": "课程创建成功",
  "data": { ... }
}
```

---

### 5.4 更新课程

> ⚠️ 需要 **TEACHER** 或 **ADMIN** 角色

```
POST /api/course/{id}
```

**请求体**：（同创建，字段可选）

---

### 5.5 删除课程

> ⚠️ 需要 **ADMIN** 角色

```
DELETE /api/course/{id}
```

---

### 5.6 添加课程日程

> ⚠️ 需要 **TEACHER** 或 **ADMIN** 角色

```
POST /api/course/{courseId}/schedules
```

**请求体**：
```json
{
  "dayOfWeek": 1,
  "startTime": "08:00",
  "endTime": "09:40",
  "location": "教学楼A301"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dayOfWeek | int | ✅ | 星期几：1-7（1=周一） |
| startTime | string | ✅ | 开始时间（HH:mm） |
| endTime | string | ✅ | 结束时间（HH:mm） |
| location | string | ⬚ | 上课地点 |

---

### 5.7 删除课程日程

> ⚠️ 需要 **TEACHER** 或 **ADMIN** 角色

```
DELETE /api/course/schedules/{scheduleId}
```

---

### 5.8 添加课程附件

```
POST /api/course/{courseId}/attachments
```

**请求体**：
```json
{
  "fileName": "课件第一章.pdf",
  "fileUrl": "/uploads/files/chapter1.pdf",
  "fileType": "application/pdf",
  "fileSize": 1024000
}
```

---

## 6. 选课管理 API

### 6.1 学生选课

> ⚠️ 需要 **STUDENT** 角色

```
POST /api/student-course/enroll
```

**请求体**：
```json
{
  "courseId": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "选课成功",
  "data": {
    "id": 100,
    "studentId": 1,
    "courseId": 1,
    "courseName": "数据库原理",
    "courseCode": "CS301",
    "teacherName": "王老师",
    "credits": 3,
    "capacity": 50,
    "enrolled": 36,
    "location": "教学楼A301",
    "status": "active",
    "grade": null,
    "enrollDate": "2024-12-14T10:30:00Z",
    "schedules": [...],
    "createdAt": "2024-12-14T10:30:00Z"
  }
}
```

**错误响应**：
```json
// 课程已满
{
  "code": 400,
  "message": "课程已满，无法选课",
  "data": null
}

// 已经选过这门课
{
  "code": 400,
  "message": "您已经选过这门课程",
  "data": null
}
```

---

### 6.2 学生退课

> ⚠️ 需要 **STUDENT** 角色

**方式一：通过选课记录ID**
```
DELETE /api/student-course/{enrollmentId}
```

**方式二：通过课程ID（兼容）**
```
POST /api/student-course/drop?studentId=1&courseId=1
```

**响应**：
```json
{
  "code": 200,
  "message": "退课成功",
  "data": null
}
```

---

### 6.3 获取已选课程列表

```
GET /api/student-course/enrolled
```

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page 或 current | int | ⬚ | 1 | 页码 |
| pageSize 或 size | int | ⬚ | 10 | 每页条数 |
| status | string | ⬚ | active | 状态筛选 |
| studentId | long | ⬚ | - | 学生ID（从Token自动获取） |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "current": 1,
    "size": 10,
    "records": [
      {
        "id": 100,
        "studentId": 1,
        "courseId": 1,
        "courseName": "数据库原理",
        "courseCode": "CS301",
        "teacherName": "王老师",
        "credits": 3,
        "capacity": 50,
        "enrolled": 36,
        "location": "教学楼A301",
        "status": "active",
        "grade": null,
        "enrollDate": "2024-09-05T10:30:00Z",
        "schedules": [
          {
            "dayOfWeek": 1,
            "startTime": "08:00",
            "endTime": "09:40",
            "location": "教学楼A301"
          }
        ]
      }
    ]
  }
}
```

---

### 6.4 获取可选课程列表

```
GET /api/student-course/available
```

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | ⬚ | 1 | 页码 |
| pageSize | int | ⬚ | 10 | 每页条数 |

**响应**：（格式同已选课程，返回未选且未满的课程）

---

### 6.5 获取选课历史

```
GET /api/student-course/history
```

返回所有选课记录，包括已退课的。

---

### 6.6 获取活跃选课

```
GET /api/student-course/active
```

返回当前有效的选课记录（未退课）。

---

## 7. 权限管理 API

### 7.1 获取当前用户角色

```
GET /api/permission/my-roles
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": ["student"]
}
```

角色代码说明：
- `student` - 学生
- `teacher` - 教师
- `admin` - 管理员

---

### 7.2 获取当前用户权限

```
GET /api/permission/my-permissions
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    "course:view",
    "enrollment:view",
    "enrollment:manage"
  ]
}
```

权限代码说明：

| 权限代码 | 说明 |
|----------|------|
| student:view | 查看学生 |
| student:edit | 编辑学生 |
| student:delete | 删除学生 |
| course:view | 查看课程 |
| course:create | 创建课程 |
| course:edit | 编辑课程 |
| course:delete | 删除课程 |
| enrollment:view | 查看选课 |
| enrollment:manage | 管理选课 |
| grade:view | 查看成绩 |
| grade:edit | 编辑成绩 |
| permission:manage | 管理权限 |
| data:export | 导出数据 |

---

### 7.3 获取用户菜单

```
GET /api/permission/menus
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "仪表板",
      "path": "/dashboard",
      "icon": "dashboard",
      "sort": 1,
      "children": []
    },
    {
      "id": 2,
      "name": "学生管理",
      "path": "/student",
      "icon": "user",
      "sort": 2,
      "children": []
    }
  ]
}
```

---

### 7.4 获取所有角色（管理员）

> ⚠️ 需要 **ADMIN** 角色

```
GET /api/permission/roles
```

---

### 7.5 获取所有权限（管理员）

> ⚠️ 需要 **ADMIN** 角色

```
GET /api/permission/all
```

---

### 7.6 给用户添加角色（管理员）

> ⚠️ 需要 **ADMIN** 角色

```
POST /api/permission/users/{userId}/roles/{roleId}
```

---

### 7.7 移除用户角色（管理员）

> ⚠️ 需要 **ADMIN** 角色

```
DELETE /api/permission/users/{userId}/roles/{roleId}
```

---

## 8. 文件上传 API

### 8.1 上传头像

```
POST /api/upload/avatar
Content-Type: multipart/form-data
```

**表单字段**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | ✅ | 图片文件（jpg/png/gif，最大2MB） |

**响应**：
```json
{
  "code": 200,
  "message": "upload success",
  "data": {
    "url": "/uploads/avatars/1702540800000_abc123.jpg"
  }
}
```

**前端示例**：
```typescript
const uploadAvatar = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await axios.post('/api/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  
  return response.data.data.url
}
```

---

### 8.2 上传通用文件

```
POST /api/upload/file
Content-Type: multipart/form-data
```

**表单字段**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | ✅ | 文件 |
| directory | string | ⬚ | 目录名，默认 files |

**响应**：
```json
{
  "code": 200,
  "message": "upload success",
  "data": {
    "url": "/uploads/files/document.pdf"
  }
}
```

---

### 8.3 更新学生头像

```
POST /api/upload/student/{id}/avatar
Content-Type: multipart/form-data
```

上传并自动更新学生的头像URL。

---

## 9. 搜索 API

### 9.1 搜索课程

```
GET /api/search/courses
```

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | ✅ | 搜索关键词 |
| current | int | ⬚ | 页码，默认1 |
| size | int | ⬚ | 每页条数，默认10 |

**响应**：
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "total": 5,
    "current": 1,
    "size": 10,
    "records": [ ... ]
  }
}
```

---

### 9.2 搜索学生

```
GET /api/search/students
```

参数同搜索课程。

---

### 9.3 全局搜索

```
GET /api/search/global
```

**响应**：
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "courses": [...],
    "students": [...],
    "totalCourses": 3,
    "totalStudents": 5
  }
}
```

---

### 9.4 热门搜索关键词

```
GET /api/search/popular-keywords?limit=10
```

---

## 10. 消息通知 API

### 10.1 发送消息

```
POST /api/notification/send
```

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| senderId | long | ✅ | 发送者ID |
| receiverId | long | ✅ | 接收者ID |
| content | string | ✅ | 消息内容 |

---

### 10.2 获取消息列表

```
GET /api/notification/messages
```

**查询参数**：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 20 | 每页条数 |

**说明**：接收者ID从Token自动获取

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "current": 1,
    "size": 20,
    "records": [
      {
        "id": 1,
        "senderId": 2,
        "senderName": "系统管理员",
        "receiverId": 1,
        "receiverName": "张三",
        "content": "您的选课申请已通过",
        "status": "unread",
        "readAt": null,
        "createdAt": "2024-12-14T10:30:00Z"
      }
    ]
  }
}
```

---

### 10.3 标记消息已读

```
POST /api/notification/messages/{messageId}/read
```

---

### 10.4 获取未读消息数

```
GET /api/notification/unread-count
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 5
  }
}
```

---

## 11. 数据字典 API

### 11.1 获取班级列表

```
GET /api/dict/classes
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "name": "计算机科学与技术1班", "grade": "2024" },
    { "id": 2, "name": "软件工程1班", "grade": "2024" }
  ]
}
```

---

### 11.2 获取状态字典

```
GET /api/dict/status
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "label": "正常", "value": "active" },
    { "label": "休学", "value": "inactive" },
    { "label": "毕业", "value": "graduated" }
  ]
}s
```

---

### 11.3 获取性别字典

```
GET /api/dict/gender
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "label": "男", "value": "M" },
    { "label": "女", "value": "F" }
  ]
}
```

---

## 12. 错误码说明

| 错误码 | 含义 | 处理方式 |
|--------|------|----------|
| 200 | 成功 | 正常处理 |
| 400 | 请求参数错误 | 检查请求参数 |
| 401 | 未授权/Token无效 | 跳转登录页 |
| 403 | 权限不足 | 显示无权限提示 |
| 404 | 资源不存在 | 显示不存在提示 |
| 429 | 请求过于频繁 | 显示锁定提示 |
| 500 | 服务器错误 | 显示错误提示 |

---

## 13. TypeScript 类型定义

```typescript
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
  page?: number
  pageSize?: number
  keyword?: string
}

// ==================== 用户相关 ====================

/** 用户信息 */
export interface User {
  id: number
  username: string
  email: string
  phone: string
  avatar?: string
  roles: string[]
  permissions: string[]
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
  classId: number
  className?: string
  gender: 'M' | 'F'
  age?: number
  phone?: string
  email?: string
  major?: string
  admissionYear?: number
  status: 'active' | 'inactive' | 'graduated'
  avatarUrl?: string
  createdAt: string
  updatedAt?: string
}

/** 学生表单数据 */
export interface StudentFormData {
  studentId: string
  name: string
  classId: number
  gender: 'M' | 'F'
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
  category?: string
  capacity: number
  enrolled: number
  status: 'open' | 'closed' | 'full' | 'archived'
  startDate?: string
  endDate?: string
  credits?: number
  location?: string
  syllabus?: string
  requirements?: string
  schedules?: CourseSchedule[]
  attachments?: CourseAttachment[]
  createdAt: string
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
  courseId: number
  courseName: string
  courseCode?: string
  teacherName?: string
  credits?: number
  capacity?: number
  enrolled?: number
  location?: string
  status: string
  grade?: string
  enrollDate: string
  schedules?: CourseSchedule[]
  attachments?: CourseAttachment[]
  createdAt: string
  updatedAt?: string
}

// ==================== 消息相关 ====================

/** 消息 */
export interface Message {
  id: number
  senderId: number
  senderName: string
  receiverId: number
  receiverName: string
  content: string
  status: 'read' | 'unread'
  readAt?: string
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
  sort: number
  children?: Menu[]
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
  PERMISSION_MANAGE: 'permission:manage',
  DATA_EXPORT: 'data:export'
} as const
```

---

## 14. Axios 配置示例

### 14.1 创建 Axios 实例

```typescript
// src/api/index.ts
import axios, { AxiosInstance, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 30000
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      // ✅ 关键：格式必须是 "Bearer <token>"
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
instance.interceptors.response.use(
  // ✅ 直接返回 response.data，简化调用
  (response) => response.data,
  (error: AxiosError<any>) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '请求失败'
    
    switch (status) {
      case 401:
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
        break
      case 403:
        ElMessage.error('权限不足: ' + message)
        break
      case 404:
        ElMessage.error('资源不存在')
        break
      case 429:
        ElMessage.error('请求过于频繁，请稍后再试')
        break
      case 500:
        ElMessage.error('服务器错误: ' + message)
        break
      default:
        ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

export default instance
```

### 14.2 API 调用示例

```typescript
// src/api/auth.ts
import instance from './index'
import type { ApiResponse, LoginRequest, LoginResponse, CaptchaResponse } from '@/types'

/** 获取验证码 */
export const getCaptcha = () => {
  return instance.get<any, ApiResponse<CaptchaResponse>>('/auth/captcha')
}

/** 用户登录 */
export const login = (data: LoginRequest) => {
  return instance.post<any, ApiResponse<LoginResponse>>('/auth/login', data)
}

/** 刷新 Token */
export const refreshToken = (refreshToken: string) => {
  return instance.post<any, ApiResponse<LoginResponse>>('/auth/refresh', { refreshToken })
}

/** 登出 */
export const logout = () => {
  return instance.post('/auth/logout')
}

/** 获取当前用户角色 */
export const getMyRoles = () => {
  return instance.get<any, ApiResponse<string[]>>('/permission/my-roles')
}

/** 获取当前用户权限 */
export const getMyPermissions = () => {
  return instance.get<any, ApiResponse<string[]>>('/permission/my-permissions')
}
```

```typescript
// src/api/student.ts
import instance from './index'
import type { ApiResponse, PageResponse, Student, StudentFormData } from '@/types'

/** 获取学生列表 */
export const getStudentList = (params: {
  current?: number
  size?: number
  keyword?: string
  classId?: number
  status?: string
}) => {
  return instance.get<any, ApiResponse<PageResponse<Student>>>('/student/list', { params })
}

/** 获取学生详情 */
export const getStudent = (id: number) => {
  return instance.get<any, ApiResponse<Student>>(`/student/${id}`)
}

/** 创建学生 */
export const createStudent = (data: StudentFormData) => {
  return instance.post<any, ApiResponse<Student>>('/student', data)
}

/** 更新学生 */
export const updateStudent = (id: number, data: Partial<StudentFormData>) => {
  return instance.put<any, ApiResponse<Student>>(`/student/${id}`, data)
}

/** 删除学生 */
export const deleteStudent = (id: number) => {
  return instance.delete<any, ApiResponse<null>>(`/student/${id}`)
}

/** 批量删除学生 */
export const batchDeleteStudents = (ids: number[]) => {
  return instance.post<any, ApiResponse<null>>('/student/batch-delete', { ids })
}
```

```typescript
// src/api/course.ts
import instance from './index'
import type { ApiResponse, PageResponse, Course, CourseFormData } from '@/types'

/** 获取课程列表 */
export const getCourseList = (params: {
  current?: number
  size?: number
  keyword?: string
  status?: string
}) => {
  return instance.get<any, ApiResponse<PageResponse<Course>>>('/course/list', { params })
}

/** 获取课程详情 */
export const getCourse = (id: number) => {
  return instance.get<any, ApiResponse<Course>>(`/course/${id}`)
}

/** 创建课程 */
export const createCourse = (data: CourseFormData) => {
  return instance.post<any, ApiResponse<Course>>('/course/create', data)
}

/** 更新课程 */
export const updateCourse = (id: number, data: Partial<CourseFormData>) => {
  return instance.post<any, ApiResponse<Course>>(`/course/${id}`, data)
}

/** 删除课程 */
export const deleteCourse = (id: number) => {
  return instance.delete<any, ApiResponse<null>>(`/course/${id}`)
}
```

```typescript
// src/api/enrollment.ts
import instance from './index'
import type { ApiResponse, PageResponse, StudentCourse } from '@/types'

/** 学生选课 */
export const enrollCourse = (courseId: number) => {
  return instance.post<any, ApiResponse<StudentCourse>>('/student-course/enroll', { courseId })
}

/** 学生退课 */
export const dropCourse = (enrollmentId: number) => {
  return instance.delete<any, ApiResponse<null>>(`/student-course/${enrollmentId}`)
}

/** 获取已选课程 */
export const getEnrolledCourses = (params?: {
  page?: number
  pageSize?: number
  status?: string
}) => {
  return instance.get<any, ApiResponse<PageResponse<StudentCourse>>>('/student-course/enrolled', { params })
}

/** 获取可选课程 */
export const getAvailableCourses = (params?: {
  page?: number
  pageSize?: number
}) => {
  return instance.get<any, ApiResponse<PageResponse<StudentCourse>>>('/student-course/available', { params })
}
```

### 14.3 文件上传示例

```typescript
// src/api/upload.ts
import instance from './index'
import type { ApiResponse } from '@/types'

/** 上传头像 */
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return instance.post<any, ApiResponse<{ url: string }>>('/upload/avatar', formData, {
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
  
  return instance.post<any, ApiResponse<{ url: string }>>('/upload/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
```

---

## 15. 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | admin | 管理员，拥有所有权限 |
| teacher | 123456 | teacher | 教师，可管理课程 |
| student | 123456 | student | 学生，可选课退课 |

---

## 16. 注意事项

### 16.1 日期格式

- 日期字段：`yyyy-MM-dd`（如：2024-09-01）
- 日期时间字段：`yyyy-MM-dd'T'HH:mm:ss'Z'`（如：2024-09-01T08:30:00Z）

### 16.2 分页参数兼容

后端同时支持两种分页参数：
- `current` + `size`（推荐）
- `page` + `pageSize`

### 16.3 性别字段

- 数据库存储：`M`（男）/ `F`（女）
- 前端显示时需要转换

### 16.4 状态字段

所有状态字段使用**小写英文**：
- 学生状态：`active` / `inactive` / `graduated`
- 课程状态：`open` / `closed` / `full` / `archived`
- 消息状态：`read` / `unread`
- 选课状态：`active` / `dropped` / `completed`

### 16.5 权限控制

前端权限控制仅用于UI展示，**所有权限验证必须在后端进行**。

---

## 📞 联系方式

如有问题，请联系后端开发人员。

---

*文档最后更新：2024-12-14*
