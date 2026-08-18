import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export interface DashboardSummary {
  userCount: number
  articleCount: number
  publishedCount: number
  categoryCount: number
  todayNewUsers: number
  todayNewArticles: number
}

export interface TrendData {
  dates: string[]
  counts: number[]
}

export const dashboardApi = {
  getSummary: () =>
    request.get<ApiResponse<DashboardSummary>>('/dashboard/summary').then((r) => r.data.data),

  getUserTrend: (days = 30) =>
    request.get<ApiResponse<TrendData>>('/dashboard/user-trend', { params: { days } }).then((r) => r.data.data),

  getArticleTrend: (days = 30) =>
    request.get<ApiResponse<TrendData>>('/dashboard/article-trend', { params: { days } }).then((r) => r.data.data),

  getCategoryStats: () =>
    request.get<ApiResponse<{ names: string[]; counts: number[] }>>('/dashboard/category-stats').then((r) => r.data.data),
}
