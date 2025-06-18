import request from './request'
import type { LoginRequest, LoginResponse } from '@/types'

// 认证相关API
export const authApi = {
  // 登录
  login: (data: LoginRequest) => 
    request.post<LoginResponse>('/auth/login', data),

  // 退出登录
  logout: () => 
    request.post('/auth/logout'),

  // 获取用户信息
  getUserInfo: () => 
    request.get('/auth/userinfo'),

  // 获取用户权限
  getUserPermissions: () => 
    request.get('/auth/permissions'),

  // 刷新token
  refreshToken: (refreshToken: string) => 
    request.post('/auth/refresh', { refreshToken })
} 