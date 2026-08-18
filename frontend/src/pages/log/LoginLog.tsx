import { useEffect, useState } from 'react'
import { Table, Button, Space, Input, Select, Card, Tag } from 'antd'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { logApi } from '@/api/log'
import type { LoginLogVO } from '@/api/log'
import dayjs from 'dayjs'

export default function LoginLog() {
  const [data, setData] = useState<LoginLogVO[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState({
    page: 1,
    pageSize: 10,
    keyword: '',
    status: undefined as number | undefined,
  })

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await logApi.getLoginLogs(query)
      setData(result.records)
      setTotal(result.total)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [query])

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '用户名', dataIndex: 'username', width: 120, render: (v: string) => <span style={{ fontWeight: 500 }}>{v}</span> },
    { title: 'IP地址', dataIndex: 'ip', width: 140 },
    {
      title: 'User-Agent', dataIndex: 'user_agent', width: 300, ellipsis: true,
    },
    {
      title: '结果', dataIndex: 'status', width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'error'}>{status === 1 ? '成功' : '失败'}</Tag>
      ),
    },
    { title: '消息', dataIndex: 'message', width: 120 },
    {
      title: '登录时间', dataIndex: 'login_time', width: 170,
      render: (t: string) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
  ]

  return (
    <div className="cms-page">
      <Card className="cms-toolbar-card">
        <Space wrap size={12}>
          <Input
            placeholder="用户名"
            allowClear
            prefix={<SearchOutlined style={{ color: '#c0c4d0' }} />}
            style={{ width: 200 }}
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
            onPressEnter={() => setQuery({ ...query, page: 1 })}
          />
          <Select
            placeholder="结果"
            allowClear
            style={{ width: 110 }}
            value={query.status}
            onChange={(v) => setQuery({ ...query, status: v, page: 1 })}
            options={[{ label: '成功', value: 1 }, { label: '失败', value: 0 }]}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={() => setQuery({ ...query, page: 1 })}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>
        </Space>
      </Card>

      <Card className="cms-table-card">
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1000 }}
          pagination={{
            current: query.page,
            pageSize: query.pageSize,
            total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page, pageSize) => setQuery({ ...query, page, pageSize }),
          }}
        />
      </Card>
    </div>
  )
}
