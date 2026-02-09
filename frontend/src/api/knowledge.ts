import { http } from '@/utils/request'
import type { KnowledgeBase, Document, ApiResponse, PageResult, SpaceType } from '@/types'

export const knowledgeApi = {
  // 获取知识库列表（后端直接返回分页数据）
  getList(params: { scope?: SpaceType; keyword?: string; pageNum: number; pageSize: number }) {
    return http.get<any, PageResult<KnowledgeBase>>('/api/kb', { params })
  },

  // 获取知识库详情
  getById(id: number) {
    return http.get<any, ApiResponse<KnowledgeBase>>(`/api/kb/${id}`)
  },

  // 获取知识库下的文档列表
  getDocuments(id: number, params: { keyword?: string; pageNum: number; pageSize: number }) {
    return http.get<any, PageResult<Document>>(`/api/kb/${id}/documents`, { params })
  },

  // 创建知识库
  create(scope: SpaceType, data: Partial<KnowledgeBase>) {
    // scope 作为 URL 参数，data 作为请求体
    return http.post<any, ApiResponse<KnowledgeBase>>('/api/kb', data, {
      params: { scope }
    })
  },

  // 更新知识库
  update(id: number, data: Partial<KnowledgeBase>) {
    return http.put<any, ApiResponse<KnowledgeBase>>(`/api/kb/${id}`, data)
  },

  // 删除知识库
  delete(id: number) {
    return http.delete<any, ApiResponse>(`/api/kb/${id}`)
  }
}
