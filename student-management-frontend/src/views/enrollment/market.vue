<template>
  <div class="enrollment-market-page">
    <el-card class="header-card">
      <div class="page-header">
        <h2>选课大厅</h2>
        <p>浏览并选择您感兴趣的课程</p>
      </div>
    </el-card>
    
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索课程名称/教师"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 课程卡片列表 -->
    <div class="course-grid" v-loading="loading">
      <el-empty v-if="!loading && courseList.length === 0" description="暂无可选课程" />
      
      <el-card
        v-for="course in courseList"
        :key="course.id"
        class="course-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <span class="course-name">{{ course.courseName }}</span>
            <el-tag :type="getStatusType(course)" size="small">
              {{ getStatusText(course) }}
            </el-tag>
          </div>
        </template>
        
        <div class="course-info">
          <p><el-icon><User /></el-icon> 授课教师：{{ course.teacherName || '待定' }}</p>
          <p><el-icon><Clock /></el-icon> 学分：{{ course.credits || '-' }} 学分</p>
          <p><el-icon><Location /></el-icon> 地点：{{ course.location || '待定' }}</p>
          <div class="capacity-info">
            <span>剩余名额：</span>
            <el-progress
              :percentage="getCapacityPercentage(course)"
              :status="getCapacityStatus(course)"
              :stroke-width="10"
            />
          </div>
        </div>
        
        <template #footer>
          <el-button
            type="primary"
            :disabled="!canEnroll(course)"
            :loading="enrollingId === course.courseId"
            @click="handleEnroll(course)"
            class="enroll-btn"
          >
            {{ canEnroll(course) ? '立即选课' : '不可选' }}
          </el-button>
        </template>
      </el-card>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="pagination.total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAvailableCourses, enrollCourse } from '@/api/enrollment'
import type { StudentCourse } from '@/types'

const loading = ref(false)
const courseList = ref<StudentCourse[]>([])
const enrollingId = ref<number>()

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0
})

// 获取可选课程
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAvailableCourses({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    courseList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

// 获取容量百分比
const getCapacityPercentage = (course: StudentCourse) => {
  const capacity = (course as any).capacity || 50
  const enrolled = (course as any).enrolled || 0
  return Math.round((enrolled / capacity) * 100)
}

// 获取容量状态
const getCapacityStatus = (course: StudentCourse) => {
  const percentage = getCapacityPercentage(course)
  if (percentage >= 100) return 'exception'
  if (percentage >= 80) return 'warning'
  return 'success'
}

// 获取状态类型
const getStatusType = (course: StudentCourse) => {
  const percentage = getCapacityPercentage(course)
  if (percentage >= 100) return 'danger'
  if (percentage >= 80) return 'warning'
  return 'success'
}

// 获取状态文字
const getStatusText = (course: StudentCourse) => {
  const percentage = getCapacityPercentage(course)
  if (percentage >= 100) return '已满'
  if (percentage >= 80) return '即将满员'
  return '可选'
}

// 判断是否可以选课
const canEnroll = (course: StudentCourse) => {
  const percentage = getCapacityPercentage(course)
  return percentage < 100
}

// 选课
const handleEnroll = async (course: StudentCourse) => {
  try {
    await ElMessageBox.confirm(
      `确定要选修课程"${course.courseName}"吗？`,
      '确认选课',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    enrollingId.value = course.courseId
    await enrollCourse(course.courseId)
    ElMessage.success('选课成功！')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 错误已在拦截器中处理
    }
  } finally {
    enrollingId.value = undefined
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.enrollment-market-page {
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
  
  .search-card {
    margin-bottom: 20px;
    
    .search-form {
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }
  
  .course-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    min-height: 200px;
  }
  
  .course-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .course-name {
        font-size: 16px;
        font-weight: bold;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        margin-right: 10px;
      }
    }
    
    .course-info {
      p {
        margin: 10px 0;
        color: #666;
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: #409eff;
        }
      }
      
      .capacity-info {
        margin-top: 15px;
        
        span {
          display: block;
          margin-bottom: 5px;
          color: #666;
        }
      }
    }
    
    :deep(.el-card__footer) {
      padding: 15px 20px;
    }
    
    .enroll-btn {
      width: 100%;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
