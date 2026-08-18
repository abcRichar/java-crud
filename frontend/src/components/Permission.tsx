import type { ReactNode } from 'react'
import { useAuthStore } from '@/stores/auth'

interface PermissionProps {
  permission: string
  children: ReactNode
  fallback?: ReactNode
}

export default function Permission({ permission, children, fallback = null }: PermissionProps) {
  const hasPermission = useAuthStore((s) => s.hasPermission)

  if (!hasPermission(permission)) {
    return <>{fallback}</>
  }

  return <>{children}</>
}
