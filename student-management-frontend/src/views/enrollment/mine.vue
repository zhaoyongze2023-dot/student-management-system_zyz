<template>
  <div class="my-enrollment-page">
    <el-card class="header-card">
      <div class="page-header">
        <h2>我的课程</h2>
        <p>查看已选课程和课程表</p>
      </div>
    </el-card>
    
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 课程列表视图 -->
      <el-tab-pane label="课程列表" name="list">
        <el-card v-loading="loading">
          <el-table :data="enrolledList" stripe border>
            <el-table-column prop="courseName" label="课程名称" min-width="150" />
            <el-table-column prop="courseCode" label="课程代码" width="120" />
            <el-table-column prop="teacherName" label="授课教师" width="100" />
            <el-table-column prop="credits" label="学分" width="80" align="center" />
            <el-table-column prop="location" label="上课地点" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'">
                  {{ row.status === 'active' ? '进行中' : '已完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enrolledAt" label="选课时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.enrolledAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  link
                  @click="handleDrop(row)"
                  :loading="droppingId === row.id"
                >
                  退课
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="!loading && enrolledList.length === 0" description="暂未选修任何课程" />
          
          <div class="pagination-container" v-if="pagination.total > 0">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              layout="total, prev, pager, next"
              @current-change="fetchData"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 课程表视图 -->
      <el-tab-pane label="我的课表" name="schedule">
        <el-card>
          <div class="schedule-table">
            <table>
              <thead>
                <tr>
                  <th>时间</th>
                  <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="time in timeSlots" :key="time">
                  <td class="time-cell">{{ time }}</td>
                  <td v-for="day in weekDays" :key="day.value" class="course-cell">
                    <div
                      v-for="course in getCoursesAtTime(day.value, time)"
                      :key="course.id"
                      class="course-block"
                      :style="{ backgroundColor: getCourseColor(course.courseId) }"
                    >
                      <div class="course-name">{{ course.courseName }}</div>
                      <div class="course-location">{{ course.location }}</div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEnrolledCourses, dropCourse } from '@/api/enrollment'
import dayjs from 'dayjs'
import type { StudentCourse } from '@/types'

const loading = ref(false)
const activeTab = ref('list')
const enrolledList = ref<StudentCourse[]>([])
const droppingId = ref<number>()

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 星期
const weekDays = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

// 时间段
const timeSlots = [
  '08:00-09:00',
  '09:00-10:00',
  '10:00-11:00',
  '11:00-12:00',
  '14:00-15:00',
  '15:00-16:00',
  '16:00-17:00',
  '17:00-18:00',
  '19:00-20:00',
  '20:00-21:00'
]

// 课程颜色
const courseColors = [
  '#409eff',
  '#67c23a',
  '#e6a23c',
  '#f56c6c',
  '#909399',
  '#00bcd4',
  '#9c27b0',
  '#ff5722'
]

const getCourseColor = (courseId: number) => {
  return courseColors[courseId % courseColors.length]
}

// 获取已选课程
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getEnrolledCourses({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    enrolledList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 退课
const handleDrop = async (course: StudentCourse) => {
  try {
    await ElMessageBox.confirm(
      `确定要退选课程"${course.courseName}"吗？退课后可能无法再次选上。`,
      '确认退课',
      {
        confirmButtonText: '确定退课',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    droppingId.value = course.id
    await dropCourse(course.id)
    ElMessage.success('退课成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 错误已在拦截器中处理
    }
  } finally {
    droppingId.value = undefined
  }
}

// 获取某时间段的课程
const getCoursesAtTime = (dayOfWeek: number, timeSlot: string) => {
  const parts = timeSlot.split('-')
  const slotStartTime = parts[0] || '00:00'
  return enrolledList.value.filter(course => {
    if (!course.schedules) return false
    return course.schedules.some(schedule => {
      return schedule.dayOfWeek === dayOfWeek &&
        (schedule.startTime || '') <= slotStartTime &&
        (schedule.endTime || '') > slotStartTime
    })
  })
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.my-enrollment-page {
  .header-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    
    .page-header {
      color: #fff;
      
      h2 {
        margin: 0 0 10px;
        font-size: 24px;
      }
      
      p {
        margin: 0;
        opacity: 0.9;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .schedule-table {
    overflow-x: auto;
    
    table {
      width: 100%;
      border-collapse: collapse;
      min-width: 800px;
      
      th, td {
        border: 1px solid #ebeef5;
        padding: 10px;
        text-align: center;
        min-width: 100px;
      }
      
      th {
        background: #f5f7fa;
        font-weight: bold;
      }
      
      .time-cell {
        background: #f5f7fa;
        font-size: 12px;
        white-space: nowrap;
      }
      
      .course-cell {
        vertical-align: top;
        height: 60px;
      }
      
      .course-block {
        padding: 5px;
        border-radius: 4px;
        color: #fff;
        font-size: 12px;
        margin: 2px 0;
        
        .course-name {
          font-weight: bold;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .course-location {
          opacity: 0.9;
          font-size: 11px;
        }
      }
    }
  }
}
</style>
