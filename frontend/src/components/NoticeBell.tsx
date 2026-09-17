import { useEffect, useState } from 'react'
import { Badge, Button, Drawer, Empty, List, Spin, Tag, Tooltip } from 'antd'
import { BellOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { noticeApi } from '@/api/notice'
import type { NoticeUserVO } from '@/types'

const typeMap: Record<string, { text: string; color: string }> = {
  SYSTEM: { text: '系统', color: 'red' },
  NOTICE: { text: '通知', color: 'blue' },
  ACTIVITY: { text: '活动', color: 'purple' },
  MESSAGE: { text: '消息', color: 'default' },
}

export default function NoticeBell() {
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)
  const [notices, setNotices] = useState<NoticeUserVO[]>([])

  const fetchUnreadCount = async () => {
    try {
      setUnreadCount(await noticeApi.getUnreadCount())
    } catch {
      setUnreadCount(0)
    }
  }

  const fetchNotices = async () => {
    setLoading(true)
    try {
      const result = await noticeApi.getMyNotices({ page: 1, pageSize: 20 })
      setNotices(result.records)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchUnreadCount()
  }, [])

  const handleOpen = () => {
    setOpen(true)
    fetchNotices()
  }

  const handleRead = async (notice: NoticeUserVO) => {
    if (notice.isRead === 1) {
      return
    }
    await noticeApi.markAsRead(notice.id)
    setNotices((items) => items.map((item) => (
      item.id === notice.id ? { ...item, isRead: 1, readAt: dayjs().format('YYYY-MM-DD HH:mm:ss') } : item
    )))
    setUnreadCount((count) => Math.max(0, count - 1))
  }

  return (
    <>
      <Tooltip title="通知">
        <Button
          type="text"
          onClick={handleOpen}
          icon={
            <Badge count={unreadCount} size="small" offset={[-2, 2]}>
              <BellOutlined style={{ fontSize: 17 }} />
            </Badge>
          }
        />
      </Tooltip>

      <Drawer title="我的通知" open={open} onClose={() => setOpen(false)} width={420}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: 48 }}><Spin /></div>
        ) : notices.length === 0 ? (
          <Empty description="暂无通知" />
        ) : (
          <List
            dataSource={notices}
            renderItem={(notice) => (
              <List.Item
                onClick={() => handleRead(notice)}
                style={{
                  cursor: notice.isRead === 1 ? 'default' : 'pointer',
                  background: notice.isRead === 1 ? 'transparent' : '#f4f6ff',
                  borderRadius: 8,
                  paddingInline: 12,
                  marginBottom: 8,
                }}
              >
                <List.Item.Meta
                  title={
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <Tag color={(typeMap[notice.type] || { color: 'default' }).color}>
                        {(typeMap[notice.type] || { text: notice.type }).text}
                      </Tag>
                      <span style={{ fontWeight: notice.isRead === 1 ? 400 : 600 }}>{notice.title}</span>
                    </div>
                  }
                  description={
                    <div>
                      <div style={{ color: '#4b5563', whiteSpace: 'pre-wrap', marginBottom: 6 }}>
                        {notice.content}
                      </div>
                      <div style={{ color: '#9ca3af', fontSize: 12 }}>
                        {dayjs(notice.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                      </div>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Drawer>
    </>
  )
}
