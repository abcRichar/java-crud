import request from '@/utils/request'
import type { ApiResponse, LoginRequest, LoginVO, UserInfoVO } from '@/types'

export const authApi = {
  login: (data: LoginRequest) =>
    request.post<ApiResponse<LoginVO>>('/auth/login', data).then((r) => r.data.data),

  refresh: (refreshToken: string) =>
    request.post<ApiResponse<LoginVO>>('/auth/refresh', { refreshToken }).then((r) => r.data.data),

  getUserInfo: () =>
    request.get<ApiResponse<UserInfoVO>>('/auth/userinfo').then((r) => r.data.data),

  logout: () => request.post<ApiResponse<void>>('/auth/logout').then((r) => r.data),
}
