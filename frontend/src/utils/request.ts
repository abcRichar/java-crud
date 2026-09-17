import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'
import type { ApiResponse } from '@/types'

const TOKEN_KEY = 'cms_access_token'
const REFRESH_TOKEN_KEY = 'cms_refresh_token'
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem(TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function clearTokens(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

const request: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor: attach token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// Response interceptor: unified error handling + token refresh
let isRefreshing = false
let pendingQueue: Array<{
  resolve: () => void
  reject: (error: unknown) => void
}> = []

request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResponse
    if (data.code === 200) {
      return response
    }
    // Business error
    message.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || 'Error'))
  },
  async (error) => {
    const originalRequest = error.config

    // 401: try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({
            resolve: () => {
              originalRequest.headers.Authorization = `Bearer ${getToken()}`
              resolve(request(originalRequest))
            },
            reject,
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          throw new Error('No refresh token')
        }

        const res = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string }>>(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken },
        )

        if (res.data.code === 200) {
          const { accessToken, refreshToken: newRefresh } = res.data.data
          setTokens(accessToken, newRefresh)
          originalRequest.headers.Authorization = `Bearer ${accessToken}`

          // Process pending requests
          pendingQueue.forEach(({ resolve }) => resolve())
          pendingQueue = []

          return request(originalRequest)
        }
        throw new Error('Refresh failed')
      } catch (refreshError) {
        clearTokens()
        pendingQueue.forEach(({ reject }) => reject(refreshError))
        pendingQueue = []
        message.error('登录已过期，请重新登录')
        window.location.href = '/#/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // Other errors
    const msg = error.response?.data?.message || error.message || '网络错误'
    if (error.response?.status !== 401) {
      message.error(msg)
    }
    return Promise.reject(error)
  },
)

export default request
