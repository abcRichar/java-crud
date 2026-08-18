import request from '@/utils/request'
import type { ApiResponse, PageResult, RoleVO, RoleCreateDTO, RoleUpdateDTO } from '@/types'

export const roleApi = {
  getList: (params: { keyword?: string; status?: number; page?: number; pageSize?: number }) =>
    request.get<ApiResponse<PageResult<RoleVO>>>('/roles', { params }).then((r) => r.data.data),

  getAll: () =>
    request.get<ApiResponse<RoleVO[]>>('/roles/all').then((r) => r.data.data),

  getById: (id: number) =>
    request.get<ApiResponse<RoleVO>>(`/roles/${id}`).then((r) => r.data.data),

  create: (data: RoleCreateDTO) =>
    request.post<ApiResponse<number>>('/roles', data).then((r) => r.data.data),

  update: (data: RoleUpdateDTO) =>
    request.put<ApiResponse<void>>('/roles', data).then((r) => r.data),

  delete: (id: number) =>
    request.delete<ApiResponse<void>>(`/roles/${id}`).then((r) => r.data),

  updateStatus: (id: number, status: number) =>
    request.patch<ApiResponse<void>>(`/roles/${id}/status`, { status }).then((r) => r.data),
}
