import request from '@/utils/request'
import type {
  ApiResponse,
  PageResult,
  UserVO,
  UserCreateDTO,
  UserUpdateDTO,
  UserOptionVO,
} from '@/types'

export interface UserQuery {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}

export const userApi = {
  getList: (params: UserQuery) =>
    request.get<ApiResponse<PageResult<UserVO>>>('/users', { params }).then((r) => r.data.data),

  getById: (id: number) =>
    request.get<ApiResponse<UserVO>>(`/users/${id}`).then((r) => r.data.data),

  getOptions: () =>
    request.get<ApiResponse<UserOptionVO[]>>('/users/options').then((r) => r.data.data),

  create: (data: UserCreateDTO) =>
    request.post<ApiResponse<number>>('/users', data).then((r) => r.data.data),

  update: (data: UserUpdateDTO) =>
    request.put<ApiResponse<void>>('/users', data).then((r) => r.data),

  delete: (id: number) =>
    request.delete<ApiResponse<void>>(`/users/${id}`).then((r) => r.data),

  updateStatus: (id: number, status: number) =>
    request.patch<ApiResponse<void>>(`/users/${id}/status`, { status }).then((r) => r.data),

  resetPassword: (id: number, password: string) =>
    request.patch<ApiResponse<void>>(`/users/${id}/password`, { password }).then((r) => r.data),
}
