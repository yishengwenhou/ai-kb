import { http } from '@/utils/request'
import type { LoginForm, RegisterForm, ApiResponse, TokenInfo, User } from '@/types'

export const authApi = {
  // 登录 (后端直接返回 TokenInfo，不是 ApiResponse 包装)
  login(data: LoginForm) {
    return http.post<any, TokenInfo>('/api/auth/login', data)
  },

  // 注册
  register(data: RegisterForm) {
    return http.post<any, ApiResponse>('/api/auth/register', data)
  },

  // 刷新token
  refreshToken(data: { tokenVo: { accessToken: string; refreshToken: string } }) {
    return http.post<any, TokenInfo>('/api/auth/refresh', data)
  },

  // 获取当前用户信息
  getProfile() {
    return http.get<any, User>('/api/user/profile')
  }
}
