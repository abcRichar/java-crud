import { useEffect, useState } from 'react'
import {
  Table, Button, Space, Input, Select, Modal, Form, InputNumber, Tag, Popconfirm, message, Card,
} from 'antd'
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { userApi, type UserQuery } from '@/api/user'
import { roleApi } from '@/api/role'
import { useAuthStore } from '@/stores/auth'
import Permission from '@/components/Permission'
import type { UserVO, RoleVO } from '@/types'
import dayjs from 'dayjs'

export default function UserList() {
  const [data, setData] = useState<UserVO[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<UserQuery>({ page: 1, pageSize: 10 })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<UserVO | null>(null)
  const [roles, setRoles] = useState<RoleVO[]>([])
  const [form] = Form.useForm()
  const hasPermission = useAuthStore((s) => s.hasPermission)

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await userApi.getList(query)
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
    roleApi.getAll().then(setRoles).catch(() => {})
  }, [])

  const handleSearch = () => {
    setQuery({ ...query, page: 1 })
  }

  const handleAdd = () => {
    setEditingUser(null)
    form.resetFields()
    form.setFieldsValue({ status: 1 })
    setModalVisible(true)
  }

  const handleEdit = (record: UserVO) => {
    setEditingUser(record)
    form.resetFields()
    form.setFieldsValue({
      nickname: record.nickname,
      email: record.email,
      phone: record.phone,
      status: record.status,
      roleIds: record.roleIds,
    })
    setModalVisible(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (editingUser) {
      await userApi.update({ id: editingUser.id, ...values })
      message.success('编辑成功')
    } else {
      await userApi.create(values)
      message.success('新增成功')
    }
    setModalVisible(false)
    fetchData()
  }

  const handleDelete = async (id: number) => {
    await userApi.delete(id)
    message.success('删除成功')
    fetchData()
  }

  const handleStatusChange = async (id: number, status: number) => {
    await userApi.updateStatus(id, status)
    message.success(status === 1 ? '已启用' : '已禁用')
    fetchData()
  }

  const handleResetPassword = async (id: number) => {
    Modal.confirm({
      title: '重置密码',
      content: <Input.Password placeholder="请输入新密码 (至少6位)" id="reset-pwd" />,
      onOk: async () => {
        const input = document.getElementById('reset-pwd') as HTMLInputElement
        const password = input?.value
        if (!password || password.length < 6) {
          message.error('密码长度至少6位')
          return Promise.reject()
        }
        await userApi.resetPassword(id, password)
        message.success('密码重置成功')
      },
    })
  }

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    {
      title: '用户',
      dataIndex: 'username',
      width: 140,
      render: (_: unknown, record: UserVO) => (
        <Space>
          <span style={{ fontWeight: 500 }}>{record.username}</span>
          {record.id === 1 && <Tag color="geekblue">超级管理员</Tag>}
        </Space>
      ),
    },
    { title: '昵称', dataIndex: 'nickname', width: 120 },
    { title: '邮箱', dataIndex: 'email', width: 180, ellipsis: true },
    { title: '手机号', dataIndex: 'phone', width: 130 },
    {
      title: '角色',
      dataIndex: 'roleIds',
      width: 160,
      render: (ids: number[]) => (
        <>
          {(ids || []).map((id) => {
            const role = roles.find((r) => r.id === id)
            return role ? <Tag key={id} color="purple">{role.name}</Tag> : null
          })}
        </>
      ),
    },
    {
      title: '状态', dataIndex: 'status', width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'error'}>{status === 1 ? '启用' : '禁用'}</Tag>
      ),
    },
    {
      title: '创建时间', dataIndex: 'createdAt', width: 170,
      render: (t: string) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '操作', key: 'action', width: 230, fixed: 'right' as const,
      render: (_: unknown, record: UserVO) => (
        <Space size={0}>
          <Permission permission="user:update">
            <Button type="link" size="small" onClick={() => handleEdit(record)}>编辑</Button>
          </Permission>
          <Permission permission="user:update">
            <Button type="link" size="small" onClick={() => handleStatusChange(record.id, record.status === 1 ? 0 : 1)}>
              {record.status === 1 ? '禁用' : '启用'}
            </Button>
          </Permission>
          <Permission permission="user:reset">
            <Button type="link" size="small" onClick={() => handleResetPassword(record.id)}>重置密码</Button>
          </Permission>
          <Permission permission="user:delete">
            <Popconfirm title="确认删除该用户?" onConfirm={() => handleDelete(record.id)}>
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
            placeholder="用户名/昵称/邮箱"
            allowClear
            prefix={<SearchOutlined style={{ color: '#c0c4d0' }} />}
            style={{ width: 220 }}
            value={query.keyword}
            onChange={(e) => setQuery({ ...query, keyword: e.target.value })}
            onPressEnter={handleSearch}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 110 }}
            value={query.status}
            onChange={(v) => setQuery({ ...query, status: v })}
            options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={fetchData}>刷新</Button>
          <Permission permission="user:create">
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} style={{ marginLeft: 'auto' }}>新增用户</Button>
          </Permission>
        </Space>
      </Card>

      <Card className="cms-table-card">
        <Table
          rowKey="id"
          columns={columns}
          dataSource={data}
          loading={loading}
          scroll={{ x: 1100 }}
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
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={520}
        className="cms-modal"
        forceRender
        okText="保存"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" style={{ paddingTop: 4 }}>
          {!editingUser && (
            <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }, { min: 3, max: 64 }]}>
              <Input placeholder="请输入用户名" />
            </Form.Item>
          )}
          {!editingUser && (
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }, { min: 6, max: 32 }]}>
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          )}
          <Form.Item name="nickname" label="昵称" rules={[{ required: true, message: '请输入昵称' }]}>
            <Input placeholder="请输入昵称" />
          </Form.Item>
          <Form.Item name="email" label="邮箱" rules={[{ type: 'email', message: '邮箱格式不正确' }]}>
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input placeholder="请输入手机号" />
          </Form.Item>
          {editingUser && (
            <Form.Item name="status" label="状态">
              <Select options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} />
            </Form.Item>
          )}
          <Form.Item name="roleIds" label="角色">
            <Select
              mode="multiple"
              placeholder="请选择角色"
              optionFilterProp="label"
              options={roles.map((r) => ({ label: r.name, value: r.id }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
