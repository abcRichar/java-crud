import { useEffect, useState } from 'react'
import { HashRouter } from 'react-router-dom'
import { Spin } from 'antd'
import AppRouter from '@/router'
import { initAuth } from '@/stores/auth'

export default function App() {
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    initAuth().finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    )
  }

  return (
    <HashRouter>
      <AppRouter />
    </HashRouter>
  )
}
