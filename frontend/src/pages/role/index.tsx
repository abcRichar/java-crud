import { useEffect, useState } from 'react'
import {
  Table, Button, Space, Input, Modal, Form, Tag, Popconfirm, message, Card, Tree, Select,
} from 'antd'
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { roleApi } from '@/api/role'
import { menuApi } from '@/api/menu'
import Permission from '@/components/Permission'
import type { RoleVO, MenuVO } from '@/types'
import dayjs from 'dayjs'

// Build tree data for antd Tree component
function buildTreeData(menus: MenuVO[]): any[] {
  return menus.map((menu) => ({
    key: menu.id,
    title: `${menu.title}${menu.permission ? ` (${menu.permission})` : ''}`,
    children: menu.children ? buildTreeData(menu.children) : undefined,
  }))
}

// Collect all menu IDs (for expanding)
function collectAllIds(menus: MenuVO[]): number[] {
  const ids: number[] = []
  for (const m of menus) {
    ids.push(m.id)
    if (m.children) ids.push(...collectAllIds(m.children))
  }
  return ids
}

export default function RoleList() {
  const [data, setData] = useState<RoleVO[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState({ page: 1, pageSize: 10, keyword: '', status: undefined as number | undefined })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRole, setEditingRole] = useState<RoleVO | null>(null)
  const [menuTree, setMenuTree] = useState<MenuVO[]>([])
  const [checkedKeys, setCheckedKeys] = useState<number[]>([])
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await roleApi.getList(query)
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
    menuApi.getTree().then(setMenuTree).catch(() => {})
  }, [])

  const handleAdd = () => {
    setEditingRole(null)
    form.resetFields()
    form.setFieldsValue({ status: 1 })
    setCheckedKeys([])
    setModalVisible(true)
  }

  const handleEdit = (record: RoleVO) => {
    setEditingRole(record)
    form.resetFields()
    form.setFieldsValue({
      name: record.name,
      code: record.code,
      remark: record.remark,
      status: record.status,
    })
    setCheckedKeys(record.menuIds || [])
    setModalVisible(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    const menuIds = checkedKeys
    if (editingRole) {
      await roleApi.update({ id: editingRole.id, ...values, menuIds })
      message.success('编辑成功')
    } else {
      await roleApi.create({ ...values, menuIds })
      message.success('新增成功')
    }
    setModalVisible(false)
    fetchData()
  }

  const handleDelete = async (id: number) => {
    await roleApi.delete(id)
    message.success('删除成功')
    fetchData()
  }

  const handleStatusChange = async (id: number, status: number) => {
    await roleApi.updateStatus(id, status)
    message.success(status === 1 ? '已启用' : '已禁用')
    fetchData()
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '角色名称', dataIndex: 'name', width: 140, render: (v: string) => <span style={{ fontWeight: 500 }}>{v}</span> },
    { title: '角色编码', dataIndex: 'code', width: 140 },
    { title: '备注', dataIndex: 'remark', width: 200, ellipsis: true },
    {
      title: '状态', dataIndex: 'status', width: 80,
      render: (status: number) => <Tag color={status === 1 ? 'success' : 'error'}>{status === 1 ? '启用' : '禁用'}</Tag>,
    },
    {
      title: '创建时间', dataIndex: 'createdAt', width: 170,
      render: (t: string) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '操作', key: 'action', width: 170, fixed: 'right' as const,
      render: (_: unknown, record: RoleVO) => (
        <Space size={0}>
          <Permission permission="role:update">
            <Button type="link" size="small" onClick={() => handleEdit(record)}>编辑</Button>
          </Permission>
          <Permission permission="role:update">
            <Button type="link" size="small" onClick={() => handleStatusChange(record.id, record.status === 1 ? 0 : 1)}>
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
          </Permission>
          <Permission permission="role:delete">
            <Popconfirm title="确认删除该角色?" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger>删除</Button>
            </Popconfirm>
          </Permission>
        </Space>
      ),
    },
  ]

  const treeData = buildTreeData(menuTree)
  const allKeys = collectAllIds(menuTree)

  return (
    <div className="cms-page">
      <Card className="cms-toolbar-card">
        <Space wrap size={12}>
          <Input
            placeholder="角色名称/编码"
            allowClear
            prefix={<SearchOutlined style={{ color: '#c0c4d0' }} />}
            style={{ width: 220 }}
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
            onPressEnter={() => setQuery({ ...query, page: 1 })}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={() => setQuery({ ...query, page: 1 })}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>
          <Permission permission="role:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} style={{ marginLeft: 'auto' }}>新增角色</Button>
          </Permission>
        </Space>
      </Card>

      <Card className="cms-table-card">
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 900 }}
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
        title={editingRole ? '编辑角色' : '新增角色'}
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
          <Form.Item name="name" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item name="code" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input placeholder="请输入角色编码 (如: admin, editor)" disabled={!!editingRole} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea placeholder="请输入备注" rows={2} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              placeholder="请选择状态"
              options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]}
            />
          </Form.Item>
          <Form.Item label="菜单权限">
            <Tree
              checkable
              defaultExpandAll
              treeData={treeData}
              checkedKeys={checkedKeys}
              onCheck={(checked) => {
                const keys = Array.isArray(checked) ? checked : (checked as { checked: number[] }).checked
                setCheckedKeys(keys as number[])
              }}
              style={{
                maxHeight: 300, overflow: 'auto',
                border: '1px solid #eef0f5', borderRadius: 10, padding: 10,
                background: '#fafbfd',
              }}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
