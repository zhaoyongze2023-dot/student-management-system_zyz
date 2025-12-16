<template>
  <div class="dashboard-page">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎回来，{{ userStore.user?.username }}！</h1>
          <p>{{ getGreeting() }}</p>
        </div>
        <div class="welcome-date">
          <div class="date">{{ currentDate }}</div>
          <div class="time">{{ currentTime }}</div>
        </div>
      </div>
    </el-card>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.studentCount }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">课程总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.enrollmentCount }}</div>
            <div class="stat-label">选课记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.messageCount }}</div>
            <div class="stat-label">未读消息</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <!-- 快捷操作 -->
      <el-col :span="12">
        <el-card class="quick-actions-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <div
              v-for="action in quickActions"
              :key="action.path"
              class="action-item"
              @click="router.push(action.path)"
            >
              <div class="action-icon" :style="{ background: action.color }">
                <el-icon><component :is="action.icon" /></el-icon>
              </div>
              <span class="action-name">{{ action.name }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最近消息 -->
      <el-col :span="12">
        <el-card class="recent-messages-card">
          <template #header>
            <div class="card-header">
              <span>最近消息</span>
              <el-button type="primary" link @click="router.push('/notification')">
                查看全部
              </el-button>
            </div>
          </template>
          <div class="message-list">
            <el-empty v-if="recentMessages.length === 0" description="暂无消息" :image-size="80" />
            <div
              v-for="message in recentMessages"
              :key="message.id"
              class="message-item"
              @click="router.push('/notification')"
            >
              <div class="message-icon" :class="{ unread: !message.isRead }">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.createdAt) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 学生角色显示已选课程 -->
    <el-card v-if="userStore.isStudent" class="my-courses-card">
      <template #header>
        <div class="card-header">
          <span>我的课程</span>
          <el-button type="primary" link @click="router.push('/enrollment/mine')">
            查看全部
          </el-button>
        </div>
      </template>
      <el-table :data="myCourses" stripe>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="credits" label="学分" width="80" align="center" />
        <el-table-column prop="location" label="上课地点" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '进行中' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="myCourses.length === 0" description="暂未选修课程" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getStudentList } from '@/api/student'
import { getCourseList } from '@/api/course'
import { getEnrolledCourses } from '@/api/enrollment'
import { getMessages, getUnreadCount } from '@/api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import type { Message, StudentCourse } from '@/types'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = reactive({
  studentCount: 0,
  courseCount: 0,
  enrollmentCount: 0,
  messageCount: 0
})

// 时间
const currentDate = ref('')
const currentTime = ref('')
let timer: ReturnType<typeof setInterval> | null = null

// 最近消息
const recentMessages = ref<Message[]>([])

// 我的课程（学生）
const myCourses = ref<StudentCourse[]>([])

// 快捷操作
const quickActions = computed(() => {
  const actions = []
  
  if (userStore.isAdmin || userStore.isTeacher) {
    actions.push(
      { name: '学生列表', path: '/student/list', icon: 'User', color: '#667eea' },
      { name: '课程管理', path: '/course/list', icon: 'Reading', color: '#f5576c' }
    )
  }
  
  if (userStore.isStudent) {
    actions.push(
      { name: '选课大厅', path: '/enrollment/market', icon: 'Collection', color: '#4facfe' },
      { name: '我的课程', path: '/enrollment/mine', icon: 'Notebook', color: '#43e97b' }
    )
  }
  
  actions.push(
    { name: '消息通知', path: '/notification', icon: 'Bell', color: '#ffa726' },
    { name: '个人中心', path: '/profile', icon: 'UserFilled', color: '#ab47bc' }
  )
  
  return actions
})

// 获取问候语
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息哦~'
  if (hour < 9) return '早上好，新的一天开始了！'
  if (hour < 12) return '上午好，工作顺利！'
  if (hour < 14) return '中午好，记得吃午饭哦~'
  if (hour < 18) return '下午好，继续加油！'
  if (hour < 22) return '晚上好，今天辛苦了！'
  return '夜深了，注意休息哦~'
}

// 更新时间
const updateTime = () => {
  const now = dayjs()
  currentDate.value = now.format('YYYY年MM月DD日 dddd')
  currentTime.value = now.format('HH:mm:ss')
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 获取学生数
    if (userStore.isAdmin || userStore.isTeacher) {
      const studentRes = await getStudentList({ current: 1, size: 1 })
      stats.studentCount = studentRes.data?.total || 0
    }
    
    // 获取课程数
    const courseRes = await getCourseList({ current: 1, size: 1 })
    stats.courseCount = courseRes.data?.total || 0
    
    // 获取选课数
    if (userStore.isStudent) {
      const enrollRes = await getEnrolledCourses({ page: 1, pageSize: 1 })
      stats.enrollmentCount = enrollRes.data?.total || 0
    }
    
    // 获取未读消息数
    const unreadRes = await getUnreadCount()
    stats.messageCount = unreadRes.data?.count || 0
  } catch (e) {
    // 忽略错误
  }
}

// 获取最近消息
const fetchRecentMessages = async () => {
  try {
    const res = await getMessages({ current: 1, size: 5 })
    recentMessages.value = res.data?.records || []
  } catch (e) {
    // 忽略错误
  }
}

// 获取我的课程
const fetchMyCourses = async () => {
  if (!userStore.isStudent) return
  
  try {
    const res = await getEnrolledCourses({ page: 1, pageSize: 5 })
    myCourses.value = res.data?.records || []
  } catch (e) {
    // 忽略错误
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  
  fetchStats()
  fetchRecentMessages()
  fetchMyCourses()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  .welcome-card {
    margin-bottom: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    
    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: #fff;
      
      .welcome-text {
        h1 {
          margin: 0 0 10px;
          font-size: 24px;
        }
        
        p {
          margin: 0;
          opacity: 0.9;
        }
      }
      
      .welcome-date {
        text-align: right;
        
        .date {
          font-size: 14px;
          opacity: 0.9;
        }
        
        .time {
          font-size: 32px;
          font-weight: bold;
          font-family: 'Courier New', monospace;
        }
      }
    }
  }
  
  .stats-row {
    margin-bottom: 20px;
    
    .stat-card {
      display: flex;
      align-items: center;
      padding: 10px;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        
        .el-icon {
          font-size: 28px;
          color: #fff;
        }
      }
      
      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #333;
        }
        
        .stat-label {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
  
  .quick-actions-card {
    margin-bottom: 20px;
    
    .quick-actions {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      
      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20px;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #f5f7fa;
          transform: translateY(-3px);
        }
        
        .action-icon {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 10px;
          
          .el-icon {
            font-size: 24px;
            color: #fff;
          }
        }
        
        .action-name {
          font-size: 14px;
          color: #333;
        }
      }
    }
  }
  
  .recent-messages-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .message-list {
      .message-item {
        display: flex;
        align-items: flex-start;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: background 0.3s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        .message-icon {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: #e9ecf0;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
          flex-shrink: 0;
          
          &.unread {
            background: #409eff;
            
            .el-icon {
              color: #fff;
            }
          }
          
          .el-icon {
            color: #909399;
          }
        }
        
        .message-content {
          flex: 1;
          min-width: 0;
          
          .message-text {
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .message-time {
            font-size: 12px;
            color: #999;
            margin-top: 4px;
          }
        }
      }
    }
  }
  
  .my-courses-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
