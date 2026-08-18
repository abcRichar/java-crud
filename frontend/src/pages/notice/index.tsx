import { useEffect, useState } from 'react'
import {
  Table, Button, Space, Input, Select, Modal, Form, Tag, Popconfirm, message, Card,
} from 'antd'
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { noticeApi, type NoticeQuery } from '@/api/notice'
import Permission from '@/components/Permission'
import type { NoticeVO } from '@/types'
import dayjs from 'dayjs'

const typeMap: Record<string, { text: string; color: string }> = {
  SYSTEM: { text: '系统', color: 'red' },
  NOTICE: { text: '通知', color: 'blue' },
  ACTIVITY: { text: '活动', color: 'purple' },
  MESSAGE: { text: '消息', color: 'default' },
}

const statusMap: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'default' },
  PUBLISHED: { text: '已发布', color: 'success' },
  WITHDRAWN: { text: '已撤回', color: 'warning' },
}

export default function NoticeList() {
  const [data, setData] = useState<NoticeVO[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<NoticeQuery>({ page: 1, pageSize: 10 })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingNotice, setEditingNotice] = useState<NoticeVO | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await noticeApi.getList(query)
      setData(result.records)
      setTotal(result.total)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [query])

  const handleAdd = () => {
    setEditingNotice(null)
    form.resetFields()
    form.setFieldsValue({ type: 'NOTICE' })
    setModalVisible(true)
  }

  const handleEdit = (record: NoticeVO) => {
    setEditingNotice(record)
    form.resetFields()
    // Fetch full notice
    noticeApi.getById(record.id).then((full) => {
      form.setFieldsValue({
        title: full.title,
        content: full.content,
        type: full.type,
      })
      setModalVisible(true)
    }).catch(() => {})
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingNotice) {
      await noticeApi.update({ id: editingNotice.id, ...values })
      message.success('编辑成功')
    } else {
      await noticeApi.create(values)
      message.success('新增成功')
    }
    setModalVisible(false)
    fetchData()
  }

  const handleDelete = async (id: number) => {
    await noticeApi.delete(id)
    message.success('删除成功')
    fetchData()
  }

  const handlePublish = async (id: number) => {
    await noticeApi.publish(id)
    message.success('发布成功')
    fetchData()
  }

  const handleWithdraw = async (id: number) => {
    await noticeApi.withdraw(id)
    message.success('撤回成功')
    fetchData()
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '标题', dataIndex: 'title', width: 260, ellipsis: true, render: (v: string) => <span style={{ fontWeight: 500 }}>{v}</span> },
    {
      title: '类型', dataIndex: 'type', width: 90,
      render: (type: string) => {
        const t = typeMap[type] || { text: type, color: 'default' }
        return <Tag color={t.color}>{t.text}</Tag>
      },
    },
    {
      title: '状态', dataIndex: 'status', width: 90,
      render: (status: string) => {
        const s = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={s.color}>{s.text}</Tag>
      },
    },
    {
      title: '创建时间', dataIndex: 'createdAt', width: 170,
      render: (t: string) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: unknown, record: NoticeVO) => (
        <Space size={0}>
          <Permission permission="notice:update">
            <Button type="link" size="small" onClick={() => handleEdit(record)}>编辑</Button>
          </Permission>
          <Permission permission="notice:publish">
            {record.status !== 'PUBLISHED' && (
              <Button type="link" size="small" onClick={() => handlePublish(record.id)}>发布</Button>
            )}
          </Permission>
          <Permission permission="notice:withdraw">
            {record.status === 'PUBLISHED' && (
              <Button type="link" size="small" onClick={() => handleWithdraw(record.id)}>撤回</Button>
            )}
          </Permission>
          <Permission permission="notice:delete">
            <Popconfirm title="确认删除该通知?" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger>删除</Button>
            </Popconfirm>
          </Permission>
        </Space>
      ),
    },
  ]

  return (
    <div className="cms-page">
      <Card className="cms-toolbar-card">
        <Space wrap size={12}>
          <Input
            placeholder="通知标题"
            allowClear
            prefix={<SearchOutlined style={{ color: '#c0c4d0' }} />}
            style={{ width: 200 }}
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
            onPressEnter={() => setQuery({ ...query, page: 1 })}
          />
          <Select
            placeholder="类型"
            allowClear
            style={{ width: 120 }}
            value={query.type}
            onChange={(v) => setQuery({ ...query, type: v, page: 1 })}
            options={Object.entries(typeMap).map(([value, { text }]) => ({ label: text, value }))}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 120 }}
            value={query.status}
            onChange={(v) => setQuery({ ...query, status: v, page: 1 })}
            options={Object.entries(statusMap).map(([value, { text }]) => ({ label: text, value }))}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={() => setQuery({ ...query, page: 1 })}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>
          <Permission permission="notice:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} style={{ marginLeft: 'auto' }}>新增通知</Button>
          </Permission>
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

      <Modal
        title={editingNotice ? '编辑通知' : '新增通知'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={640}
        className="cms-modal"
        forceRender
        okText="保存"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" style={{ paddingTop: 4 }}>
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input placeholder="请输入通知标题" />
          </Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true, message: '请选择类型' }]}>
            <Select options={Object.entries(typeMap).map(([value, { text }]) => ({ label: text, value }))} />
          </Form.Item>
          <Form.Item name="content" label="内容" rules={[{ required: true, message: '请输入内容' }]}>
            <Input.TextArea rows={6} placeholder="请输入通知内容" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
