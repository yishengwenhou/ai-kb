import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('access_token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refresh_token') || '')
  const userInfo = ref<User | null>(
    localStorage.getItem('user_info')
      ? JSON.parse(localStorage.getItem('user_info')!)
      : null
  )

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const email = computed(() => userInfo.value?.email || '')
  const avatarUrl = computed(() => userInfo.value?.avatarUrl || userInfo.value?.avatar || '')

  // 方法
  function setToken(accessToken: string, newRefreshToken?: string) {
    token.value = accessToken
    localStorage.setItem('access_token', accessToken)
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
      localStorage.setItem('refresh_token', newRefreshToken)
    }
  }

  function setUserInfo(user: User) {
    userInfo.value = user
    localStorage.setItem('user_info', JSON.stringify(user))
  }

  function clearAuth() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('user_info')
  }

  async function fetchUserInfo() {
    try {
      const userProfile = await authApi.getProfile()
      setUserInfo(userProfile)
      return userProfile
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  async function refreshAccessToken() {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }
    try {
      const response = await authApi.refreshToken({ tokenVo: { accessToken: '', refreshToken: refreshToken.value } })
      setToken(response.accessToken, response.refreshToken)
      return response.accessToken
    } catch (error) {
      clearAuth()
      throw error
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    userName,
    email,
    avatarUrl,
    setToken,
    setUserInfo,
    clearAuth,
    fetchUserInfo,
    refreshAccessToken
  }
})
