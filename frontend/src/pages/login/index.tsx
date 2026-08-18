import { useState } from 'react'
import { Form, Input, Button, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, SafetyCertificateOutlined, ThunderboltOutlined, TeamOutlined, ArrowRightOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/auth'

const features = [
  { icon: <SafetyCertificateOutlined />, title: '安全可靠', desc: 'JWT 双 Token 认证 + RBAC 细粒度权限控制' },
  { icon: <ThunderboltOutlined />, title: '高效灵活', desc: 'Spring Boot 3 + React 18，前后端分离架构' },
  { icon: <TeamOutlined />, title: '内容管理', desc: '文章 / 分类 / 通知一体化发布管理流程' },
]

export default function Login() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      await login(values.username, values.password)
      message.success('登录成功，欢迎回来')
      navigate('/dashboard')
    } catch {
      // Error already handled by interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      {/* 左侧品牌区 */}
      <div className="login-brand">
        <div style={{ position: 'relative', zIndex: 1, maxWidth: 560 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: 48 }}>
            <div style={{
              width: 46, height: 46, borderRadius: 13,
              background: 'rgba(255,255,255,0.18)',
              border: '1px solid rgba(255,255,255,0.25)',
              backdropFilter: 'blur(6px)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 22, fontWeight: 700, color: '#fff',
            }}>
              CM
            </div>
            <div>
              <div style={{ fontSize: 22, fontWeight: 700, letterSpacing: 1 }}>CMS 管理系统</div>
              <div style={{ fontSize: 12, opacity: 0.75, letterSpacing: 2, marginTop: 2 }}>CONTENT MANAGEMENT SYSTEM</div>
            </div>
          </div>

          <h1 style={{ fontSize: 36, fontWeight: 700, lineHeight: 1.3, marginBottom: 16, color: '#fff' }}>
            企业级内容管理平台
            <br />
            让内容创造价值
          </h1>
          <p style={{ fontSize: 15, opacity: 0.85, lineHeight: 1.8, marginBottom: 48, maxWidth: 460 }}>
            一站式管理文章、分类与通知，配合完善的权限体系与操作审计，
            为团队提供高效、安全的内容运营体验。
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            {features.map((f) => (
              <div key={f.title} style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <div style={{
                  width: 40, height: 40, borderRadius: 11, flexShrink: 0,
                  background: 'rgba(255,255,255,0.14)',
                  border: '1px solid rgba(255,255,255,0.2)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18,
                }}>
                  {f.icon}
                </div>
                <div>
                  <div style={{ fontSize: 14, fontWeight: 600 }}>{f.title}</div>
                  <div style={{ fontSize: 12.5, opacity: 0.75, marginTop: 1 }}>{f.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 右侧登录面板 */}
      <div className="login-panel">
        <div className="login-card">
          <div className="login-logo">CM</div>
          <div className="login-title">欢迎登录</div>
          <div className="login-subtitle">请输入您的账号密码进入管理平台</div>

          <Form
            name="login"
            onFinish={onFinish}
            size="large"
            autoComplete="off"
          >
            <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input prefix={<UserOutlined style={{ color: '#9ca3af' }} />} placeholder="用户名" />
            </Form.Item>

            <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password prefix={<LockOutlined style={{ color: '#9ca3af' }} />} placeholder="密码" />
            </Form.Item>

            <Form.Item style={{ marginTop: 28 }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="large">
                登 录 <ArrowRightOutlined />
              </Button>
            </Form.Item>
          </Form>

          <Divider plain style={{ color: '#d1d5db', fontSize: 12, margin: '8px 0 20px' }}>
            测试账号
          </Divider>
          <div style={{
            display: 'flex', justifyContent: 'center', gap: 8, fontSize: 12.5, color: '#6b7280',
            background: '#f8fafc', borderRadius: 8, padding: '10px 12px',
          }}>
            <span>admin / admin123</span>
            <span style={{ color: '#d1d5db' }}>|</span>
            <span>editor / admin123</span>
          </div>
        </div>
      </div>
    </div>
  )
}
