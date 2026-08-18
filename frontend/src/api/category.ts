import request from '@/utils/request'
import type { ApiResponse, CategoryVO, CategoryCreateDTO, CategoryUpdateDTO } from '@/types'

export const categoryApi = {
  getTree: () =>
    request.get<ApiResponse<CategoryVO[]>>('/categories/tree').then((r) => r.data.data),

  getAll: () =>
    request.get<ApiResponse<CategoryVO[]>>('/categories').then((r) => r.data.data),

  getById: (id: number) =>
    request.get<ApiResponse<CategoryVO>>(`/categories/${id}`).then((r) => r.data.data),

  create: (data: CategoryCreateDTO) =>
    request.post<ApiResponse<number>>('/categories', data).then((r) => r.data.data),

  update: (data: CategoryUpdateDTO) =>
    request.put<ApiResponse<void>>('/categories', data).then((r) => r.data),

  delete: (id: number) =>
    request.delete<ApiResponse<void>>(`/categories/${id}`).then((r) => r.data),

  updateStatus: (id: number, status: number) =>
    request.patch<ApiResponse<void>>(`/categories/${id}/status`, { status }).then((r) => r.data),
}
