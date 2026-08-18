import request from '@/utils/request'
import type { ApiResponse, PageResult, NoticeVO, NoticeCreateDTO, NoticeUpdateDTO } from '@/types'

export interface NoticeQuery {
  keyword?: string
  type?: string
  status?: string
  page?: number
  pageSize?: number
}

export const noticeApi = {
  getList: (params: NoticeQuery) =>
    request.get<ApiResponse<PageResult<NoticeVO>>>('/notices', { params }).then((r) => r.data.data),

  getById: (id: number) =>
    request.get<ApiResponse<NoticeVO>>(`/notices/${id}`).then((r) => r.data.data),

  create: (data: NoticeCreateDTO) =>
    request.post<ApiResponse<number>>('/notices', data).then((r) => r.data.data),

  update: (data: NoticeUpdateDTO) =>
    request.put<ApiResponse<void>>('/notices', data).then((r) => r.data),

  delete: (id: number) =>
    request.delete<ApiResponse<void>>(`/notices/${id}`).then((r) => r.data),

  publish: (id: number) =>
    request.patch<ApiResponse<void>>(`/notices/${id}/publish`).then((r) => r.data),

  withdraw: (id: number) =>
    request.patch<ApiResponse<void>>(`/notices/${id}/withdraw`).then((r) => r.data),

  markAsRead: (id: number) =>
    request.patch<ApiResponse<void>>(`/notices/${id}/read`).then((r) => r.data),

  getUnreadCount: () =>
    request.get<ApiResponse<number>>('/notices/unread-count').then((r) => r.data.data),
}
