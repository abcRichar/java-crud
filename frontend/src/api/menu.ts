import request from '@/utils/request'
import type { ApiResponse, MenuVO, MenuCreateDTO, MenuUpdateDTO } from '@/types'

export const menuApi = {
  getTree: () =>
    request.get<ApiResponse<MenuVO[]>>('/menus/tree').then((r) => r.data.data),

  getMyMenus: () =>
    request.get<ApiResponse<MenuVO[]>>('/menus/my-menus').then((r) => r.data.data),

  getById: (id: number) =>
    request.get<ApiResponse<MenuVO>>(`/menus/${id}`).then((r) => r.data.data),

  create: (data: MenuCreateDTO) =>
    request.post<ApiResponse<number>>('/menus', data).then((r) => r.data.data),

  update: (data: MenuUpdateDTO) =>
    request.put<ApiResponse<void>>('/menus', data).then((r) => r.data),

  delete: (id: number) =>
    request.delete<ApiResponse<void>>(`/menus/${id}`).then((r) => r.data),
}
