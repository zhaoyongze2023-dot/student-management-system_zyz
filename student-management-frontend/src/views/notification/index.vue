<template>
  <div class="notification-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <el-button type="primary" link @click="markAllRead" :disabled="unreadCount === 0">
            全部标为已读
          </el-button>
        </div>
      </template>
      
      <div class="notification-stats">
        <el-statistic title="未读消息" :value="unreadCount" />
        <el-statistic title="全部消息" :value="pagination.total" />
      </div>
      
      <el-divider />
      
      <div class="message-list" v-loading="loading">
        <el-empty v-if="!loading && messageList.length === 0" description="暂无消息" />
        
        <div
          v-for="message in messageList"
          :key="message.id"
          class="message-item"
          :class="{ unread: !message.isRead }"
          @click="handleRead(message)"
        >
          <div class="message-header">
            <span class="sender">
              {{ message.senderName || '系统通知' }}
            </span>
            <span class="time">{{ formatTime(message.createdAt) }}</span>
          </div>
          <div class="message-content">
            {{ message.content }}
          </div>
          <el-tag v-if="!message.isRead" type="danger" size="small" class="unread-tag">
            未读
          </el-tag>
        </div>
      </div>
      
      <div class="pagination-container" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessages, markMessageRead, getUnreadCount } from '@/api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import type { Message } from '@/types'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const messageList = ref<Message[]>([])
const unreadCount = ref(0)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 获取消息列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMessages({
      current: pagination.current,
      size: pagination.size
    })
    messageList.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 获取未读数
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {
    // 忽略错误
  }
}

// 标记消息已读
const handleRead = async (message: Message) => {
  if (message.isRead) return
  
  try {
    await markMessageRead(message.id)
    message.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (e) {
    // 错误已在拦截器中处理
  }
}

// 全部标为已读
const markAllRead = async () => {
  try {
    for (const message of messageList.value) {
      if (!message.isRead) {
        await markMessageRead(message.id)
        message.isRead = true
      }
    }
    unreadCount.value = 0
    ElMessage.success('已全部标为已读')
  } catch (e) {
    // 错误已在拦截器中处理
  }
}

// 格式化时间
const formatTime = (time: string) => {
  const date = dayjs(time)
  const now = dayjs()
  
  if (now.diff(date, 'day') < 1) {
    return date.fromNow()
  } else if (now.diff(date, 'day') < 7) {
    return date.format('dddd HH:mm')
  } else {
    return date.format('YYYY-MM-DD HH:mm')
  }
}

onMounted(() => {
  fetchData()
  fetchUnreadCount()
})
</script>

<style lang="scss" scoped>
.notification-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .notification-stats {
    display: flex;
    gap: 40px;
    
    :deep(.el-statistic__head) {
      font-size: 14px;
      color: #909399;
    }
    
    :deep(.el-statistic__content) {
      font-size: 28px;
    }
  }
  
  .message-list {
    min-height: 300px;
    
    .message-item {
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all 0.3s;
      position: relative;
      background: #f5f7fa;
      
      &:hover {
        background: #e9ecf0;
      }
      
      &.unread {
        background: #ecf5ff;
        border-left: 3px solid #409eff;
        
        &:hover {
          background: #d9ecff;
        }
      }
      
      .message-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;
        
        .sender {
          font-weight: bold;
          color: #333;
        }
        
        .time {
          font-size: 12px;
          color: #999;
        }
      }
      
      .message-content {
        color: #666;
        line-height: 1.6;
      }
      
      .unread-tag {
        position: absolute;
        top: 15px;
        right: 15px;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
