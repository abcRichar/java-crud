import { useEffect, useState } from 'react'
import { Card, Col, Row, Spin } from 'antd'
import {
  UserOutlined,
  FileTextOutlined,
  AppstoreOutlined,
  RiseOutlined,
  TeamOutlined,
  EditOutlined,
  CalendarOutlined,
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import { dashboardApi, type DashboardSummary, type TrendData } from '@/api/dashboard'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const statCards = [
  { key: 'userCount', title: '用户总数', icon: <TeamOutlined />, color: 'linear-gradient(135deg, #4f46e5, #7c3aed)' },
  { key: 'articleCount', title: '文章总数', icon: <FileTextOutlined />, color: 'linear-gradient(135deg, #0ea5e9, #2563eb)' },
  { key: 'publishedCount', title: '已发布文章', icon: <RiseOutlined />, color: 'linear-gradient(135deg, #10b981, #059669)' },
  { key: 'categoryCount', title: '分类总数', icon: <AppstoreOutlined />, color: 'linear-gradient(135deg, #f59e0b, #d97706)' },
  { key: 'todayNewUsers', title: '今日新增用户', icon: <UserOutlined />, color: 'linear-gradient(135deg, #ec4899, #db2777)' },
  { key: 'todayNewArticles', title: '今日新增文章', icon: <EditOutlined />, color: 'linear-gradient(135deg, #8b5cf6, #6d28d9)' },
]

export default function Dashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [userTrend, setUserTrend] = useState<TrendData | null>(null)
  const [articleTrend, setArticleTrend] = useState<TrendData | null>(null)
  const [categoryStats, setCategoryStats] = useState<{ names: string[]; counts: number[] } | null>(null)
  const [loading, setLoading] = useState(true)
  const user = useAuthStore((s) => s.user)

  useEffect(() => {
    Promise.all([
      dashboardApi.getSummary(),
      dashboardApi.getUserTrend(30),
      dashboardApi.getArticleTrend(30),
      dashboardApi.getCategoryStats(),
    ]).then(([s, ut, at, cs]) => {
      setSummary(s)
      setUserTrend(ut)
      setArticleTrend(at)
      setCategoryStats(cs)
    }).finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>
  }

  const userTrendOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: userTrend?.dates || [], boundaryGap: false, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#9ca3af' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f1f6' } }, axisLabel: { color: '#9ca3af' } },
    series: [{
      data: userTrend?.counts || [], type: 'line', smooth: true,
      symbol: 'circle', symbolSize: 6,
      lineStyle: { width: 3, color: '#4f46e5' },
      itemStyle: { color: '#4f46e5' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(79, 70, 229, 0.25)' },
            { offset: 1, color: 'rgba(79, 70, 229, 0)' },
          ],
        },
      },
    }],
  }

  const articleTrendOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: articleTrend?.dates || [], axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#9ca3af' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f1f6' } }, axisLabel: { color: '#9ca3af' } },
    series: [{
      data: articleTrend?.counts || [], type: 'bar',
      barWidth: 14, borderRadius: [6, 6, 0, 0],
      itemStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#a5b4fc' },
          ],
        },
      },
    }],
  }

  const categoryOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 8, top: 'center', textStyle: { color: '#6b7280' } },
    color: ['#4f46e5', '#0ea5e9', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6', '#f97316'],
    series: [{
      type: 'pie',
      radius: ['45%', '72%'],
      center: ['38%', '50%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 600 }, itemStyle: { shadowBlur: 12, shadowColor: 'rgba(0,0,0,0.2)' } },
      data: (categoryStats?.names || []).map((name, i) => ({ name, value: categoryStats?.counts[i] || 0 })),
    }],
  }

  const greeting = dayjs().hour() < 12 ? '早上好' : dayjs().hour() < 18 ? '下午好' : '晚上好'

  return (
    <div className="cms-page">
      {/* 欢迎横幅 */}
      <Card className="chart-card" style={{ background: 'linear-gradient(135deg, #4f46e5 0%, #6d28d9 100%)', border: 'none' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: 20, fontWeight: 700, color: '#fff' }}>
              {greeting}，{user?.nickname || user?.username} 👋
            </div>
            <div style={{ fontSize: 13.5, color: 'rgba(255,255,255,0.8)', marginTop: 6 }}>
              今天是 {dayjs().format('YYYY年MM月DD日 dddd')}，欢迎回到 CMS 管理平台
            </div>
          </div>
          <div style={{ display: 'flex', gap: 28 }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 26, fontWeight: 700, color: '#fff' }}>{summary?.articleCount || 0}</div>
              <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.75)', marginTop: 2 }}>内容总量</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 26, fontWeight: 700, color: '#fff' }}>{summary?.userCount || 0}</div>
              <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.75)', marginTop: 2 }}>用户总量</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 26, fontWeight: 700, color: '#fff' }}>{summary?.publishedCount || 0}</div>
              <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.75)', marginTop: 2 }}>已发布</div>
            </div>
          </div>
        </div>
      </Card>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]}>
        {statCards.map((item) => (
          <Col xs={12} sm={12} md={8} lg={4} key={item.key}>
            <Card className="stat-card" styles={{ body: { padding: 20 } }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <div className="stat-icon" style={{ background: item.color }}>
                  {item.icon}
                </div>
                <div>
                  <div style={{ fontSize: 13, color: '#9ca3af', marginBottom: 4 }}>{item.title}</div>
                  <div style={{ fontSize: 22, fontWeight: 700, color: '#111827', lineHeight: 1 }}>
                    {(summary as any)?.[item.key] ?? 0}
                  </div>
                </div>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      {/* 图表 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card
            className="chart-card"
            title={
              <span><CalendarOutlined style={{ color: '#4f46e5', marginRight: 8 }} />用户增长趋势（近 30 天）</span>
            }
          >
            <ReactECharts option={userTrendOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            className="chart-card"
            title={
              <span><RiseOutlined style={{ color: '#6366f1', marginRight: 8 }} />文章发布趋势（近 30 天）</span>
            }
          >
            <ReactECharts option={articleTrendOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card
            className="chart-card"
            title={
              <span><AppstoreOutlined style={{ color: '#f59e0b', marginRight: 8 }} />分类文章统计</span>
            }
          >
            <ReactECharts option={categoryOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
