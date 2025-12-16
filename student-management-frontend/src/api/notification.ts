import request from '@/utils/request'
import type { ApiResponse, PageResponse, Message } from '@/types'

/** 发送消息 */
export const sendMessage = (senderId: number, receiverId: number, content: string) => {
  return request.post<any, ApiResponse<Message>>('/notification/send', null, {
    params: { senderId, receiverId, content }
  })
}

/** 获取消息列表 */
export const getMessages = (params?: {
  current?: number
  size?: number
}) => {
  return request.get<any, ApiResponse<PageResponse<Message>>>('/notification/messages', { params })
}

/** 标记消息已读 */
export const markMessageRead = (messageId: number) => {
  return request.post<any, ApiResponse<null>>(`/notification/messages/${messageId}/read`)
}

/** 获取未读消息数 */
export const getUnreadCount = () => {
  return request.get<any, ApiResponse<{ count: number }>>('/notification/unread-count')
}
