import request from '@/utils/request'
import type { ApiResponse, ClassInfo, DictItem } from '@/types'

/** 获取班级列表 */
export const getClasses = () => {
  return request.get<any, ApiResponse<ClassInfo[]>>('/dict/classes')
}

/** 获取状态字典 */
export const getStatusDict = () => {
  return request.get<any, ApiResponse<DictItem[]>>('/dict/status')
}

/** 获取性别字典 */
export const getGenderDict = () => {
  return request.get<any, ApiResponse<DictItem[]>>('/dict/gender')
}
