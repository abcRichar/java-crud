import { useEffect, useState } from 'react'
import { Table, Button, Space, Input, Card, Tag, Tooltip } from 'antd'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { logApi } from '@/api/log'
import type { OperationLogVO } from '@/api/log'
import dayjs from 'dayjs'

const methodColorMap: Record<string, string> = {
  GET: 'blue',
  POST: 'green',
  PUT: 'orange',
  DELETE: 'red',
  PATCH: 'purple',
}

export default function OperationLog() {
  const [data, setData] = useState<OperationLogVO[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState({ page: 1, pageSize: 10, keyword: '' })

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await logApi.getOperationLogs(query)
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
    { title: '用户', dataIndex: 'username', width: 100 },
    {
      title: '方法', dataIndex: 'method', width: 70,
      render: (method: string) => <Tag color={methodColorMap[method] || 'default'}>{method}</Tag>,
    },
    { title: '操作', dataIndex: 'operation', width: 120, render: (v: string) => <span style={{ fontWeight: 500 }}>{v}</span> },
    {
      title: '请求地址', dataIndex: 'url', width: 250, ellipsis: true,
      render: (url: string) => <Tooltip title={url}><span>{url}</span></Tooltip>,
    },
    {
      title: '请求参数', dataIndex: 'params', width: 300, ellipsis: true,
      render: (params: string) => params ? <Tooltip title={params}><span style={{ fontSize: 12 }}>{params}</span></Tooltip> : '-',
    },
    { title: 'IP', dataIndex: 'ip', width: 130 },
    {
      title: '耗时', dataIndex: 'cost_time', width: 80,
      render: (ms: number) => (
        <span style={{ color: ms > 1000 ? '#ef4444' : '#6b7280', fontWeight: ms > 1000 ? 600 : 400 }}>
          {ms}ms
        </span>
      ),
    },
    {
      title: '时间', dataIndex: 'created_at', width: 170,
      render: (t: string) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
  ]

  return (
    <div className="cms-page">
      <Card className="cms-toolbar-card">
        <Space wrap size={12}>
          <Input
            placeholder="用户名/操作/地址"
            allowClear
            prefix={<SearchOutlined style={{ color: '#c0c4d0' }} />}
            style={{ width: 220 }}
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
            onPressEnter={() => setQuery({ ...query, page: 1 })}
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
          scroll={{ x: 1300 }}
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
