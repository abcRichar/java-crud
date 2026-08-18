import { useEffect, useState } from 'react'
import {
  Tree, Button, Space, Modal, Form, Input, Select, InputNumber, Tag, Popconfirm, message, Card,
} from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import { menuApi } from '@/api/menu'
import Permission from '@/components/Permission'
import type { MenuVO } from '@/types'

function buildTreeData(menus: MenuVO[]): any[] {
  return menus.map((menu) => ({
    key: menu.id,
    title: (
      <Space size={6}>
        <span style={{ fontWeight: 500 }}>{menu.title}</span>
        <Tag color={menu.type === 'DIRECTORY' ? 'blue' : menu.type === 'MENU' ? 'green' : 'orange'}>
          {menu.type === 'DIRECTORY' ? '目录' : menu.type === 'MENU' ? '菜单' : '按钮'}
        </Tag>
        {menu.permission && <Tag>{menu.permission}</Tag>}
        {menu.status === 0 && <Tag color="red">禁用</Tag>}
      </Space>
    ),
    children: menu.children ? buildTreeData(menu.children) : undefined,
    menuData: menu,
  }))
}

export default function MenuList() {
  const [menus, setMenus] = useState<MenuVO[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingMenu, setEditingMenu] = useState<MenuVO | null>(null)
  const [form] = Form.useForm()

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await menuApi.getTree()
      setMenus(result)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const handleAdd = (pid?: number) => {
    setEditingMenu(null)
    form.resetFields()
    form.setFieldsValue({ parentId: pid || 0, type: 'MENU', sort: 0, visible: 1, status: 1 })
    setModalVisible(true)
  }

  const handleEdit = (record: MenuVO) => {
    setEditingMenu(record)
    form.resetFields()
    form.setFieldsValue({
      parentId: record.parentId,
      name: record.name,
      title: record.title,
      path: record.path,
      component: record.component,
      icon: record.icon,
      type: record.type,
      permission: record.permission,
      sort: record.sort,
      visible: record.visible,
      status: record.status,
    })
    setModalVisible(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingMenu) {
      await menuApi.update({ id: editingMenu.id, ...values })
      message.success('编辑成功')
    } else {
      await menuApi.create(values)
      message.success('新增成功')
    }
    setModalVisible(false)
    fetchData()
  }

  const handleDelete = async (id: number) => {
    await menuApi.delete(id)
    message.success('删除成功')
    fetchData()
  }

  const treeData = buildTreeData(menus)

  const renderActions = (node: any) => {
    const menu: MenuVO = node.menuData
    return (
      <Space size={0} style={{ marginLeft: 16 }}>
        <Permission permission="menu:create">
          <Button type="link" size="small" onClick={() => handleAdd(menu.id)}>新增子项</Button>
        </Permission>
        <Permission permission="menu:update">
          <Button type="link" size="small" onClick={() => handleEdit(menu)}>编辑</Button>
        </Permission>
        <Permission permission="menu:delete">
          <Popconfirm title="确认删除该菜单?" onConfirm={() => handleDelete(menu.id)}>
            <Button type="link" size="small" danger>删除</Button>
          </Popconfirm>
        </Permission>
      </Space>
    )
  }

  // Flatten menus for parent select
  const flattenMenus = (menus: MenuVO[], depth = 0): { label: string; value: number }[] => {
    const result: { label: string; value: number }[] = []
    for (const m of menus) {
      if (m.type !== 'BUTTON') {
        result.push({ label: `${'　'.repeat(depth)}${m.title}`, value: m.id })
        if (m.children) result.push(...flattenMenus(m.children, depth + 1))
      }
    }
    return result
  }

  return (
    <div className="cms-page">
      <Card className="cms-toolbar-card">
        <Space size={12}>
          <Permission permission="menu:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleAdd(0)}>新增菜单</Button>
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
          titleRender={(node) => (
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
              {node.title}
              {renderActions(node)}
            </div>
          )}
        />
      </Card>

      <Modal
        title={editingMenu ? '编辑菜单' : '新增菜单'}
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
          <Form.Item name="parentId" label="父菜单">
            <Select
              options={[{ label: '顶级菜单', value: 0 }, ...flattenMenus(menus)]}
              placeholder="请选择父菜单"
            />
          </Form.Item>
          <Form.Item name="type" label="类型" rules={[{ required: true, message: '请选择类型' }]}>
            <Select options={[
              { label: '目录', value: 'DIRECTORY' },
              { label: '菜单', value: 'MENU' },
              { label: '按钮', value: 'BUTTON' },
            ]} />
          </Form.Item>
          <Form.Item name="name" label="路由名称" rules={[{ required: true, message: '请输入路由名称' }]}>
            <Input placeholder="如: user, role, article" />
          </Form.Item>
          <Form.Item name="title" label="显示标题" rules={[{ required: true, message: '请输入显示标题' }]}>
            <Input placeholder="如: 用户管理" />
          </Form.Item>
          <Form.Item name="path" label="路由路径">
            <Input placeholder="如: /system/user" />
          </Form.Item>
          <Form.Item name="component" label="组件路径">
            <Input placeholder="如: system/user/index" />
          </Form.Item>
          <Form.Item name="icon" label="图标">
            <Input placeholder="如: UserOutlined" />
          </Form.Item>
          <Form.Item name="permission" label="权限标识">
            <Input placeholder="如: user:list, user:create" />
          </Form.Item>
          <Space size={16}>
            <Form.Item name="sort" label="排序">
              <InputNumber min={0} style={{ width: 140 }} />
            </Form.Item>
            <Form.Item name="visible" label="是否可见">
              <Select style={{ width: 120 }} options={[{ label: '显示', value: 1 }, { label: '隐藏', value: 0 }]} />
            </Form.Item>
            <Form.Item name="status" label="状态">
              <Select style={{ width: 120 }} options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} />
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </div>
  )
}
