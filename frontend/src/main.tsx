import React from 'react'
import ReactDOM from 'react-dom/client'
import { ConfigProvider, App as AntdApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import App from './App'
import './index.css'

dayjs.locale('zh-cn')

const cmsTheme = {
  token: {
    colorPrimary: '#4f46e5',
    colorInfo: '#4f46e5',
    colorSuccess: '#16a34a',
    colorWarning: '#f59e0b',
    colorError: '#ef4444',
    colorBgLayout: '#f4f5fb',
    colorBorderSecondary: '#eef0f5',
    borderRadius: 8,
    fontSize: 14,
    controlHeight: 34,
  },
  components: {
    Layout: {
      headerBg: '#ffffff',
      siderBg: '#ffffff',
      bodyBg: '#f4f5fb',
      headerHeight: 60,
      headerPadding: '0 24px',
    },
    Menu: {
      itemBorderRadius: 8,
      itemMarginInline: 12,
      itemHeight: 42,
      itemSelectedBg: '#eef2ff',
      itemSelectedColor: '#4f46e5',
      itemHoverBg: '#f4f5fb',
      iconSize: 16,
      collapsedIconSize: 18,
    },
    Table: {
      headerBg: '#fafbfd',
      headerColor: '#374151',
      headerSplitColor: 'transparent',
      rowHoverBg: '#f4f6ff',
      cellPaddingBlock: 14,
    },
    Card: {
      headerBg: 'transparent',
      headerFontSize: 15,
    },
    Button: {
      fontWeight: 500,
      primaryShadow: '0 2px 6px rgba(79, 70, 229, 0.25)',
      defaultShadow: 'none',
    },
    Modal: {
      titleFontSize: 16,
      contentBg: '#ffffff',
    },
    Tag: {
      borderRadiusSM: 4,
    },
  },
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider locale={zhCN} theme={cmsTheme}>
      <AntdApp>
        <App />
      </AntdApp>
    </ConfigProvider>
  </React.StrictMode>,
)
