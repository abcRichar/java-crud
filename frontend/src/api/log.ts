import request from '@/utils/request'
import type { ApiResponse, PageResult } from '@/types'

export interface LoginLogVO {
  id: number
  username: string
  ip: string
  user_agent: string
  status: number
  message: string
  login_time: string
}

export interface OperationLogVO {
  id: number
  user_id: number
  username: string
  method: string
  url: string
  params: string
  operation: string
  ip: string
  cost_time: number
  created_at: string
}

export const logApi = {
  getLoginLogs: (params: { keyword?: string; status?: number; page?: number; pageSize?: number }) =>
    request.get<ApiResponse<PageResult<LoginLogVO>>>('/logs/login', { params }).then((r) => r.data.data),

  getOperationLogs: (params: { keyword?: string; page?: number; pageSize?: number }) =>
    request.get<ApiResponse<PageResult<OperationLogVO>>>('/logs/operation', { params }).then((r) => r.data.data),
}
