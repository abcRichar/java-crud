import { useEffect, useState } from 'react'
import {
  Table, Button, Space, Input, Select, Modal, Form, Tag, Popconfirm, message, Card, Drawer,
} from 'antd'
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { articleApi, type ArticleQuery } from '@/api/article'
import { categoryApi } from '@/api/category'
import Permission from '@/components/Permission'
import type { ArticleVO, CategoryVO } from '@/types'
import dayjs from 'dayjs'

const statusMap: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'default' },
  PUBLISHED: { text: '已发布', color: 'success' },
  OFFLINE: { text: '已下线', color: 'warning' },
}

export default function ArticleList() {
  const [data, setData] = useState<ArticleVO[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<ArticleQuery>({ page: 1, pageSize: 10 })
  const [categories, setCategories] = useState<CategoryVO[]>([])
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [editingArticle, setEditingArticle] = useState<ArticleVO | null>(null)
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([])
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await articleApi.getList(query)
      setData(result.records)
      setTotal(result.total)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [query])

  useEffect(() => {
    categoryApi.getAll().then(setCategories).catch(() => {})
  }, [])

  // Flatten categories for select
  const flattenCats = (cats: CategoryVO[]): { label: string; value: number }[] => {
    const result: { label: string; value: number }[] = []
    for (const c of cats) {
      result.push({ label: c.name, value: c.id })
      if (c.children) result.push(...flattenCats(c.children))
    }
    return result
  }

  const handleAdd = () => {
    setEditingArticle(null)
    form.resetFields()
    form.setFieldsValue({ status: 'DRAFT', cover: '' })
    setDrawerVisible(true)
  }

  const handleEdit = (record: ArticleVO) => {
    setEditingArticle(record)
    form.resetFields()
    // Fetch full article with content
    articleApi.getById(record.id).then((full) => {
      form.setFieldsValue({
        title: full.title,
        summary: full.summary,
        content: full.content,
        cover: full.cover,
        categoryId: full.categoryId,
        seoTitle: full.seoTitle,
        seoDescription: full.seoDescription,
      })
      setDrawerVisible(true)
    }).catch(() => {})
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingArticle) {
      await articleApi.update({ id: editingArticle.id, ...values })
      message.success('编辑成功')
    } else {
      await articleApi.create(values)
      message.success('新增成功')
    }
    setDrawerVisible(false)
    fetchData()
  }

  const handleDelete = async (id: number) => {
    await articleApi.delete(id)
    message.success('删除成功')
    fetchData()
  }

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的文章')
      return
    }
    await articleApi.batchDelete(selectedRowKeys)
    message.success('批量删除成功')
    setSelectedRowKeys([])
    fetchData()
  }

  const handlePublish = async (id: number) => {
    await articleApi.publish(id)
    message.success('发布成功')
    fetchData()
  }

  const handleOffline = async (id: number) => {
    await articleApi.offline(id)
    message.success('下线成功')
    fetchData()
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '标题', dataIndex: 'title', width: 260, ellipsis: true, render: (v: string) => <span style={{ fontWeight: 500 }}>{v}</span> },
    { title: '分类', dataIndex: 'categoryName', width: 100 },
    { title: '作者', dataIndex: 'authorName', width: 100 },
    {
      title: '状态', dataIndex: 'status', width: 90,
      render: (status: string) => {
        const s = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={s.color}>{s.text}</Tag>
      },
    },
    { title: '浏览', dataIndex: 'viewCount', width: 80 },
    { title: '点赞', dataIndex: 'likeCount', width: 80 },
    {
      title: '创建时间', dataIndex: 'createdAt', width: 170,
      render: (t: string) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: unknown, record: ArticleVO) => (
        <Space size={0}>
          <Permission permission="article:update">
            <Button type="link" size="small" onClick={() => handleEdit(record)}>编辑</Button>
          </Permission>
          <Permission permission="article:publish">
            {record.status !== 'PUBLISHED' && (
              <Button type="link" size="small" onClick={() => handlePublish(record.id)}>发布</Button>
            )}
          </Permission>
          <Permission permission="article:offline">
            {record.status === 'PUBLISHED' && (
              <Button type="link" size="small" onClick={() => handleOffline(record.id)}>下线</Button>
            )}
          </Permission>
          <Permission permission="article:delete">
            <Popconfirm title="确认删除该文章?" onConfirm={() => handleDelete(record.id)}>
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
            placeholder="文章标题"
            allowClear
            prefix={<SearchOutlined style={{ color: '#c0c4d0' }} />}
            style={{ width: 200 }}
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
            onPressEnter={() => setQuery({ ...query, page: 1 })}
          />
          <Select
            placeholder="分类"
            allowClear
            style={{ width: 150 }}
            value={query.categoryId}
            onChange={(v) => setQuery({ ...query, categoryId: v, page: 1 })}
            options={flattenCats(categories)}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 120 }}
            value={query.status}
            onChange={(v) => setQuery({ ...query, status: v, page: 1 })}
            options={[
              { label: '草稿', value: 'DRAFT' },
              { label: '已发布', value: 'PUBLISHED' },
              { label: '已下线', value: 'OFFLINE' },
            ]}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={() => setQuery({ ...query, page: 1 })}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>
          <Permission permission="article:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} style={{ marginLeft: 'auto' }}>新增文章</Button>
          </Permission>
          <Permission permission="article:delete">
            <Popconfirm title={`确认删除选中的 ${selectedRowKeys.length} 篇文章?`} onConfirm={handleBatchDelete}
              disabled={selectedRowKeys.length === 0}>
              <Button danger disabled={selectedRowKeys.length === 0}>批量删除</Button>
            </Popconfirm>
          </Permission>
        </Space>
      </Card>

      <Card className="cms-table-card">
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1200 }}
          rowSelection={{
            selectedRowKeys,
            onChange: (keys) => setSelectedRowKeys(keys as number[]),
          }}
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

      <Drawer
        title={editingArticle ? '编辑文章' : '新增文章'}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
        width={680}
        className="cms-drawer"
        forceRender
        extra={
          <Space>
            <Button onClick={() => setDrawerVisible(false)}>取消</Button>
            <Button type="primary" onClick={handleSubmit}>保存</Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
            <Input placeholder="请输入文章标题" />
          </Form.Item>
          <Form.Item name="categoryId" label="分类">
            <Select placeholder="请选择分类" options={flattenCats(categories)} />
          </Form.Item>
          <Form.Item name="summary" label="摘要">
            <Input.TextArea rows={2} placeholder="请输入文章摘要" />
          </Form.Item>
          <Form.Item name="cover" label="封面图 URL">
            <Input placeholder="请输入封面图 URL（可选）" />
          </Form.Item>
          <Form.Item name="content" label="内容 (Markdown)">
            <Input.TextArea
              rows={12}
              placeholder="请输入文章内容 (支持 Markdown)"
              style={{ fontFamily: "'JetBrains Mono', Consolas, 'Courier New', monospace", fontSize: 13 }}
            />
          </Form.Item>
          <Form.Item name="seoTitle" label="SEO 标题">
            <Input placeholder="请输入 SEO 标题" />
          </Form.Item>
          <Form.Item name="seoDescription" label="SEO 描述">
            <Input.TextArea rows={2} placeholder="请输入 SEO 描述" />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
}
