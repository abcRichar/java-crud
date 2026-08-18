import { useMemo, useState } from 'react'
import { Layout, Menu, Avatar, Dropdown, Breadcrumb, Badge, Button, Tooltip } from 'antd'
import {
  DashboardOutlined,
  SettingOutlined,
  FileTextOutlined,
  AuditOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'
import type { MenuVO } from '@/types'
import dayjs from 'dayjs'

const { Header, Sider, Content } = Layout

// Icon mapping
const iconMap: Record<string, React.ReactNode> = {
  DashboardOutlined: <DashboardOutlined />,
  SettingOutlined: <SettingOutlined />,
  FileTextOutlined: <FileTextOutlined />,
  AuditOutlined: <AuditOutlined />,
  UserOutlined: <UserOutlined />,
}

function buildAntdMenu(menus: MenuVO[]): any[] {
  return menus
    .filter((m) => m.visible === 1)
    .map((menu) => {
      const children = menu.children ? buildAntdMenu(menu.children) : []
      return {
        key: menu.path || `menu-${menu.id}`,
        icon: menu.icon ? iconMap[menu.icon] || <FileTextOutlined /> : undefined,
        label: menu.title,
        children: children.length > 0 ? children : undefined,
      }
    })
}

function findMenuPath(menus: MenuVO[], targetPath: string): MenuVO[] {
  for (const menu of menus) {
    if (menu.path === targetPath) {
      return [menu]
    }
    if (menu.children) {
      const found = findMenuPath(menu.children, targetPath)
      if (found.length > 0) {
        return [menu, ...found]
      }
    }
  }
  return []
}

export default function MainLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const [fullscreen, setFullscreen] = useState(false)
  const location = useLocation()
  const navigate = useNavigate()
  const { user, menus, logout } = useAuthStore()

  const antdMenus = useMemo(() => buildAntdMenu(menus), [menus])

  const breadcrumbItems = useMemo(() => {
    const currentPath = '/' + location.pathname.split('/').filter(Boolean).join('/')
    const pathMenus = findMenuPath(menus, currentPath)
    return [
      { title: '首页' },
      ...pathMenus.map((m) => ({ title: m.title })),
    ]
  }, [menus, location.pathname])

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key) navigate(key)
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen()
      setFullscreen(true)
    } else {
      document.exitFullscreen()
      setFullscreen(false)
    }
  }

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: user?.nickname || user?.username,
      disabled: true,
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ]

  const selectedKeys = useMemo(() => {
    const path = '/' + location.pathname.split('/').filter(Boolean).join('/')
    // Find the deepest menu path that matches
    const findDeepest = (menus: MenuVO[]): string => {
      for (const m of menus) {
        if (m.path === path) return m.path || ''
        if (m.children) {
          const child = findDeepest(m.children)
          if (child) return child
        }
      }
      return ''
    }
    return [findDeepest(menus) || path]
  }, [menus, location.pathname])

  const today = dayjs().format('MM月DD日 dddd')

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={224}
        theme="light"
        style={{
          borderRight: '1px solid #eef0f5',
          boxShadow: '1px 0 8px rgba(16, 24, 40, 0.04)',
          zIndex: 20,
        }}
      >
        <div className="sider-logo">
          <div className="sider-logo-badge">{collapsed ? 'C' : 'CM'}</div>
          {!collapsed && (
            <div>
              <div className="sider-logo-text">CMS 管理系统</div>
              <div className="sider-logo-sub">CONTENT MANAGEMENT</div>
            </div>
          )}
        </div>
        <Menu
          mode="inline"
          selectedKeys={selectedKeys}
          items={antdMenus}
          onClick={handleMenuClick}
          style={{ border: 'none', paddingTop: 12, paddingBottom: 12 }}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            padding: '0 24px',
            background: '#ffffff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderBottom: '1px solid #eef0f5',
            boxShadow: '0 1px 4px rgba(16, 24, 40, 0.04)',
            position: 'sticky',
            top: 0,
            zIndex: 10,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <Button
              type="text"
              onClick={() => setCollapsed(!collapsed)}
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              style={{ fontSize: 16 }}
            />
            <Breadcrumb items={breadcrumbItems} />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ fontSize: 13, color: '#9ca3af', marginRight: 8 }}>{today}</span>
            <Tooltip title="通知">
              <Button
                type="text"
                icon={
                  <Badge count={0} size="small" offset={[-2, 2]}>
                    <BellOutlined style={{ fontSize: 17 }} />
                  </Badge>
                }
              />
            </Tooltip>
            <Tooltip title={fullscreen ? '退出全屏' : '全屏'}>
              <Button
                type="text"
                onClick={toggleFullscreen}
                icon={fullscreen ? <FullscreenExitOutlined style={{ fontSize: 17 }} /> : <FullscreenOutlined style={{ fontSize: 17 }} />}
              />
            </Tooltip>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', padding: '4px 8px', borderRadius: 8, transition: 'background 0.2s' }}>
                <Avatar
                  size={30}
                  src={user?.avatar}
                  icon={<UserOutlined />}
                  style={{ background: 'linear-gradient(135deg, #4f46e5, #7c3aed)' }}
                />
                <span style={{ fontSize: 14, fontWeight: 500, color: '#374151' }}>
                  {user?.nickname || user?.username}
                </span>
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content style={{ padding: 20, minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
