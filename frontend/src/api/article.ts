import request from '@/utils/request'
import type { ApiResponse, PageResult, ArticleVO, ArticleCreateDTO, ArticleUpdateDTO } from '@/types'

export interface ArticleQuery {
  keyword?: string
  categoryId?: number
  status?: string
  page?: number
  pageSize?: number
}

export const articleApi = {
  getList: (params: ArticleQuery) =>
    request.get<ApiResponse<PageResult<ArticleVO>>>('/articles', { params }).then((r) => r.data.data),

  getById: (id: number) =>
    request.get<ApiResponse<ArticleVO>>(`/articles/${id}`).then((r) => r.data.data),

  create: (data: ArticleCreateDTO) =>
    request.post<ApiResponse<number>>('/articles', data).then((r) => r.data.data),

  update: (data: ArticleUpdateDTO) =>
    request.put<ApiResponse<void>>('/articles', data).then((r) => r.data),

  delete: (id: number) =>
    request.delete<ApiResponse<void>>(`/articles/${id}`).then((r) => r.data),

  batchDelete: (ids: number[]) =>
    request.delete<ApiResponse<void>>('/articles/batch', { data: { ids } }).then((r) => r.data),

  publish: (id: number) =>
    request.patch<ApiResponse<void>>(`/articles/${id}/publish`).then((r) => r.data),

  offline: (id: number) =>
    request.patch<ApiResponse<void>>(`/articles/${id}/offline`).then((r) => r.data),
}
