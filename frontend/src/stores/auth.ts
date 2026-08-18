import { create } from 'zustand'
import type { UserInfoVO, MenuVO } from '@/types'
import { authApi } from '@/api/auth'
import { menuApi } from '@/api/menu'
import { getToken, setTokens, clearTokens, getRefreshToken } from '@/utils/request'

interface AuthState {
  user: UserInfoVO | null
  permissions: string[]
  menus: MenuVO[]
  loading: boolean
  initialized: boolean

  login: (username: string, password: string) => Promise<void>
  fetchUserInfo: () => Promise<void>
  fetchMenus: () => Promise<void>
  logout: () => Promise<void>
  hasPermission: (perm: string) => boolean
  reset: () => void
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  permissions: [],
  menus: [],
  loading: false,
  initialized: false,

  login: async (username: string, password: string) => {
    const result = await authApi.login({ username, password })
    setTokens(result.accessToken, result.refreshToken)
    set({
      user: result.user,
      permissions: result.user.permissions || [],
    })
    await get().fetchMenus()
  },

  fetchUserInfo: async () => {
    const userInfo = await authApi.getUserInfo()
    set({
      user: userInfo,
      permissions: userInfo.permissions || [],
    })
  },

  fetchMenus: async () => {
    const menus = await menuApi.getMyMenus()
    set({ menus })
  },

  logout: async () => {
    try {
      await authApi.logout()
    } catch {
      // ignore
    }
    clearTokens()
    set({ user: null, permissions: [], menus: [], initialized: false })
  },

  hasPermission: (perm: string) => {
    const { permissions } = get()
    if (permissions.includes('*') || permissions.includes('*:*')) return true
    return permissions.includes(perm)
  },

  reset: () => {
    clearTokens()
    set({ user: null, permissions: [], menus: [], initialized: false })
  },
}))

// Initialize auth state on app load
export async function initAuth(): Promise<boolean> {
  const token = getToken()
  if (!token) {
    return false
  }

  try {
    await useAuthStore.getState().fetchUserInfo()
    await useAuthStore.getState().fetchMenus()
    useAuthStore.setState({ initialized: true })
    return true
  } catch {
    clearTokens()
    return false
  }
}
