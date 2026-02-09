import { http } from '@/utils/request'
import type { RecycleBinItem, ApiResponse, PageResult } from '@/types'

export const recycleApi = {
  // 获取回收站列表
  getList(params: {
    type?: string
    keyword?: string
    pageNum: number
    pageSize: number
  }) {
    return http.get<any, ApiResponse<PageResult<RecycleBinItem>>>('/api/recycle/list', { params })
  },

  // 恢复文档
  restoreDocument(id: number) {
    return http.post<any, ApiResponse>(`/api/recycle/restore/document/${id}`)
  },

  // 恢复知识库
  restoreKnowledgeBase(id: number) {
    return http.post<any, ApiResponse>(`/api/recycle/restore/kb/${id}`)
  },

  // 永久删除文档
  permanentDeleteDocument(id: number) {
    return http.delete<any, ApiResponse>(`/api/recycle/permanent/document/${id}`)
  },

  // 永久删除知识库
  permanentDeleteKnowledgeBase(id: number) {
    return http.delete<any, ApiResponse>(`/api/recycle/permanent/kb/${id}`)
  },

  // 批量恢复
  batchRestore(ids: number[], type: 'document' | 'knowledgeBase') {
    return http.post<any, ApiResponse>('/api/recycle/batch/restore', { ids, type })
  },

  // 批量永久删除
  batchPermanentDelete(ids: number[], type: 'document' | 'knowledgeBase') {
    return http.post<any, ApiResponse>('/api/recycle/batch/delete', { ids, type })
  },

  // 清空回收站
  clear() {
    return http.delete<any, ApiResponse>('/api/recycle/clear')
  }
}
