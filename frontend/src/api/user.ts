import { http } from '@/utils/request'
import type { User, ApiResponse, PageResult } from '@/types'

export const userApi = {
  // 获取用户列表（后端直接返回 IPage，不包装在 ApiResponse 中）
  getList(params: {
    keyword?: string
    deptId?: number
    status?: number
    role?: number
    pageNum: number
    pageSize: number
  }) {
    return http.get<any, PageResult<User>>('/api/user/list', { params })
  },

  // 获取用户详情
  getById(id: number) {
    return http.get<any, ApiResponse<User>>(`/api/user/${id}`)
  },

  // 创建用户（后端直接返回 User）
  create(data: Partial<User>) {
    return http.post<any, User>('/api/user/create', data)
  },

  // 更新用户（后端直接返回 User）
  update(data: Partial<User>) {
    return http.post<any, User>('/api/user/update', data)
  },

  // 删除用户（后端需要传用户ID）
  delete(id: number) {
    return http.post<any, string>('/api/user/delete', id)
  },

  // 修改个人信息（后端返回字符串）
  updateProfile(data: Partial<User>) {
    return http.put<any, string>('/api/user/profile', data)
  },

  // 修改密码（后端返回字符串）
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return http.put<any, string>('/api/user/password', data)
  }
}
