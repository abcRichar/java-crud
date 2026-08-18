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
        <Route path="system/user" element={<UserList />} />
        <Route path="system/role" element={<RoleList />} />
        <Route path="system/menu" element={<MenuList />} />
        <Route path="content/category" element={<CategoryList />} />
        <Route path="content/article" element={<ArticleList />} />
        <Route path="content/notice" element={<NoticeList />} />
        <Route path="log/login" element={<LoginLog />} />
        <Route path="log/operation" element={<OperationLog />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
