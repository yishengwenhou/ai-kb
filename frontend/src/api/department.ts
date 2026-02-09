import { http } from '@/utils/request'
import type { Department, DepartmentVo, ApiResponse, PageResult } from '@/types'

export const departmentApi = {
  // 获取部门树（后端返回 List<DepartmentVo>）
  getTree(parentId?: number) {
    return http.get<any, Department[]>('/api/dept', {
      params: parentId ? { parentId } : {}
    })
  },

  // 分页查询部门（后端返回 IPage<DepartmentVo>）
  getPage(params: { keyword?: string; pageNum: number; pageSize: number }) {
    return http.get<any, { records: DepartmentVo[]; total: number; size: number; current: number; pages: number }>('/api/dept/list', {
      params: {
        keyword: params.keyword,
        pageNum: params.pageNum,
        pageSize: params.pageSize
      }
    })
  },

  // 创建部门
  create(data: Partial<Department>) {
    return http.post<any>('/api/dept', data)
  },

  // 更新部门（后端需要 id 作为路径参数）
  update(data: Partial<Department> & { id: number }) {
    return http.put<any>(`/api/dept/${data.id}`, data)
  },

  // 删除部门
  delete(id: number) {
    return http.delete<any>(`/api/dept/${id}`)
  }
}
