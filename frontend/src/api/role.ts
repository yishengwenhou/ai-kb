import { http } from '@/utils/request'
import type { ApiResponse, PageResult, Role } from '@/types'

export const roleApi = {
  // 获取角色列表
  getList(params: { pageNum: number; pageSize: number; keyword?: string; status?: number }) {
    return http.get<any, PageResult<Role>>('/api/role/list', { params })
  },

  // 获取所有角色（不分页，用于下拉选择）
  getAll() {
    return http.get<any, PageResult<Role>>('/api/role/list', {
      params: { pageNum: 1, pageSize: 100 }
    })
  }
}
