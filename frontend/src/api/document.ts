import { http } from '@/utils/request'
import type { Document, ApiResponse } from '@/types'

export const documentApi = {
  // 获取文档列表（分页/按层级）
  list(kbId: number, parentId: number = 0) {
    return http.get<any, Document[]>('/api/doc/list', {
      params: { kbId, parentId }
    })
  },

  // 获取文档详情
  getDetail(id: number) {
    return http.get<any, Document>(`/api/doc/detail/${id}`)
  },

  // 获取面包屑导航
  getBreadcrumb(id: number) {
    return http.get<any, Document[]>(`/api/doc/breadcrumb/${id}`)
  },

  // 更新文档内容
  updateContent(data: { id: number; content: string }) {
    return http.put<any, ApiResponse>('/api/doc/content', data)
  },

  // 上传文档
  upload(kbId: number, file: File, parentId?: number) {
    const formData = new FormData()
    formData.append('kbId', kbId.toString())
    formData.append('file', file)
    if (parentId !== undefined) {
      formData.append('parentId', parentId.toString())
    }
    return http.upload<any, ApiResponse<Document>>('/api/doc/upload', formData)
  },

  // 创建文件夹/文档节点（统一接口）
  createNode(kbId: number, data: { parentId: number; title: string; type: string }) {
    return http.post<any, ApiResponse<Document>>(`/api/doc/create`, { kbId, ...data })
  },

  // 移动/排序节点
  moveNode(data: { id: number; parentId?: number; sort?: number }) {
    return http.post<any, ApiResponse>('/api/doc/move', data)
  },

  // 下载文档
  download(id: number) {
    return http.download(`/api/doc/download/${id}`)
  },

  // 删除文档
  delete(id: number) {
    return http.delete<any, ApiResponse>(`/api/doc/delete/${id}`)
  }
}
