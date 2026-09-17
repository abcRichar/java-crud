import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import MainLayout from '@/layouts/MainLayout'
import Login from '@/pages/login'
import Dashboard from '@/pages/dashboard'
import UserList from '@/pages/user'
import RoleList from '@/pages/role'
import MenuList from '@/pages/menu'
import CategoryList from '@/pages/category'
import ArticleList from '@/pages/article'
import NoticeList from '@/pages/notice'
import LoginLog from '@/pages/log/LoginLog'
import OperationLog from '@/pages/log/OperationLog'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('cms_access_token')
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

function RequirePermission({ permission, children }: { permission: string; children: React.ReactNode }) {
  const hasPermission = useAuthStore((state) => state.hasPermission)
  if (!hasPermission(permission)) {
    return <Navigate to="/dashboard" replace />
  }
  return <>{children}</>
}

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="system/user" element={<RequirePermission permission="user:list"><UserList /></RequirePermission>} />
        <Route path="system/role" element={<RequirePermission permission="role:list"><RoleList /></RequirePermission>} />
        <Route path="system/menu" element={<RequirePermission permission="menu:list"><MenuList /></RequirePermission>} />
        <Route path="content/category" element={<RequirePermission permission="category:list"><CategoryList /></RequirePermission>} />
        <Route path="content/article" element={<RequirePermission permission="article:list"><ArticleList /></RequirePermission>} />
        <Route path="content/notice" element={<RequirePermission permission="notice:list"><NoticeList /></RequirePermission>} />
        <Route path="log/login" element={<RequirePermission permission="log:login:list"><LoginLog /></RequirePermission>} />
        <Route path="log/operation" element={<RequirePermission permission="log:operation:list"><OperationLog /></RequirePermission>} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
