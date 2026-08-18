import { useEffect, useState } from 'react'
import {
  Tree, Button, Space, Modal, Form, Input, InputNumber, Select, Tag, Popconfirm, message, Card,
} from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { categoryApi } from '@/api/category'
import Permission from '@/components/Permission'
import type { CategoryVO } from '@/types'

function buildTreeData(categories: CategoryVO[]): any[] {
  return categories.map((cat) => ({
    key: cat.id,
    title: (
      <Space size={6}>
        <span style={{ fontWeight: 500 }}>{cat.name}</span>
        {cat.slug && <span style={{ color: '#9ca3af', fontSize: 12 }}>{cat.slug}</span>}
        <Tag color={cat.status === 1 ? 'success' : 'error'}>{cat.status === 1 ? '启用' : '禁用'}</Tag>
      </Space>
    ),
    children: cat.children ? buildTreeData(cat.children) : undefined,
    catData: cat,
  }))
}

export default function CategoryList() {
  const [categories, setCategories] = useState<CategoryVO[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingCat, setEditingCat] = useState<CategoryVO | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await categoryApi.getTree()
      setCategories(result)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const handleAdd = (parentId = 0) => {
    setEditingCat(null)
    form.resetFields()
    form.setFieldsValue({ parentId, sort: 0, status: 1 })
    setModalVisible(true)
  }

  const handleEdit = (record: CategoryVO) => {
    setEditingCat(record)
    form.resetFields()
    form.setFieldsValue({
      parentId: record.parentId,
      name: record.name,
      slug: record.slug,
      sort: record.sort,
      status: record.status,
    })
    setModalVisible(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingCat) {
      await categoryApi.update({ id: editingCat.id, ...values })
      message.success('编辑成功')
    } else {
      await categoryApi.create(values)
      message.success('新增成功')
    }
    setModalVisible(false)
    fetchData()
  }

  const handleDelete = async (id: number) => {
    await categoryApi.delete(id)
    message.success('删除成功')
    fetchData()
  }

  const handleStatusChange = async (id: number, status: number) => {
    await categoryApi.updateStatus(id, status)
    message.success(status === 1 ? '已启用' : '已禁用')
    fetchData()
  }

  const treeData = buildTreeData(categories)

  const flattenCats = (cats: CategoryVO[], depth = 0): { label: string; value: number }[] => {
    const result: { label: string; value: number }[] = []
    for (const c of cats) {
      result.push({ label: `${'　'.repeat(depth)}${c.name}`, value: c.id })
      if (c.children) result.push(...flattenCats(c.children, depth + 1))
    }
    return result
  }

  return (
    <div className="cms-page">
      <Card className="cms-toolbar-card">
        <Space size={12}>
          <Permission permission="category:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd(0)}>新增分类</Button>
          </Permission>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>
        </Space>
      </Card>

      <Card
        className="cms-table-card"
        loading={loading}
        styles={{ body: { padding: 12 } }}
      >
        <Tree
          treeData={treeData}
          defaultExpandAll
          blockNode
          titleRender={(node) => {
            const cat: CategoryVO = node.catData
            return (
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                {node.title}
                <Space size={0} style={{ marginLeft: 16 }}>
                  <Permission permission="category:create">
                    <Button type="link" size="small" onClick={() => handleAdd(cat.id)}>新增子项</Button>
                  </Permission>
                  <Permission permission="category:update">
                    <Button type="link" size="small" onClick={() => handleEdit(cat)}>编辑</Button>
                  </Permission>
                  <Permission permission="category:update">
                    <Button type="link" size="small" onClick={() => handleStatusChange(cat.id, cat.status === 1 ? 0 : 1)}>
                      {cat.status === 1 ? '禁用' : '启用'}
                    </Button>
                  </Permission>
                  <Permission permission="category:delete">
                    <Popconfirm title="确认删除该分类?" onConfirm={() => handleDelete(cat.id)}>
                      <Button type="link" size="small" danger>删除</Button>
                    </Popconfirm>
                  </Permission>
                </Space>
              </div>
            )
          }}
        />
      </Card>

      <Modal
        title={editingCat ? '编辑分类' : '新增分类'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={480}
        className="cms-modal"
        forceRender
        okText="保存"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" style={{ paddingTop: 4 }}>
          <Form.Item name="parentId" label="父分类">
            <Select options={[{ label: '顶级分类', value: 0 }, ...flattenCats(categories)]} placeholder="请选择父分类" />
          </Form.Item>
          <Form.Item name="name" label="分类名称" rules={[{ required: true, message: '请输入分类名称' }]}>
            <Input placeholder="请输入分类名称" />
          </Form.Item>
          <Form.Item name="slug" label="分类别名">
            <Input placeholder="如: java, spring, react" />
          </Form.Item>
          <Space size={16}>
            <Form.Item name="sort" label="排序">
              <InputNumber min={0} style={{ width: 160 }} />
            </Form.Item>
            <Form.Item name="status" label="状态">
              <Select style={{ width: 160 }} options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} />
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </div>
  )
}
