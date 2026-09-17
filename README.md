# CMS 内容管理系统

企业级 CMS 内容管理后台，基于 Java 17 + Spring Boot 3 + React 18 + TypeScript 构建。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.x
- Spring Security 6
- Spring JDBC (JdbcTemplate) — 不使用 ORM
- MySQL 8.x
- Redis
- JWT (Access Token + Refresh Token)
- Knife4j / Springdoc OpenAPI
- Lombok / Jackson / Hibernate Validator
- SLF4J + Logback

### 前端
- React 18 + TypeScript
- Vite 5
- Ant Design 5
- Tailwind CSS
- React Router 6
- Zustand (状态管理)
- Axios (HTTP 客户端)
- ECharts (图表)
- dayjs (日期处理)

## 项目结构

```
cms-project
├── backend/                # 后端 Spring Boot 项目
│   ├── src/main/java/com/example/cms/
│   │   ├── common/         # 通用: 响应封装、异常处理、工具类
│   │   ├── config/         # 配置: Security、Redis、CORS、OpenAPI
│   │   ├── security/       # 安全: JWT、过滤器、UserDetailsService
│   │   ├── auth/           # 认证模块: 登录、刷新Token
│   │   ├── user/           # 用户管理
│   │   ├── role/           # 角色管理
│   │   ├── menu/           # 菜单管理
│   │   ├── category/       # 分类管理
│   │   ├── article/        # 文章管理
│   │   ├── notice/         # 通知管理
│   │   ├── log/            # 日志管理
│   │   └── dashboard/      # 仪表盘
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   └── sql/
│       ├── schema.sql      # 建表脚本
│       └── data.sql        # 初始数据
├── frontend/               # 前端 React 项目
│   └── src/
│       ├── api/            # API 请求封装
│       ├── components/     # 通用组件 (Permission)
│       ├── layouts/        # 布局组件
│       ├── pages/          # 页面
│       ├── router/         # 路由
│       ├── stores/         # Zustand 状态
│       ├── types/          # TypeScript 类型
│       └── utils/          # 工具 (Axios 封装)
├── docs/                   # 部署文档
│   ├── deploy-bt.md        # 宝塔面板部署
│   ├── deploy-manual.md    # 命令行手动部署
│   └── deploy-cicd.md      # GitHub Actions CI/CD
├── deploy/
│   └── cms-backend.service # systemd 服务文件模板
├── .github/workflows/
│   └── deploy.yml          # CI/CD 工作流
└── README.md
```

## 快速开始

### 1. 启动 MySQL 和 Redis

确保本地有 MySQL 8 和 Redis 运行。

```bash
# 创建数据库并导入初始数据
mysql -u root -p < backend/sql/schema.sql
mysql -u root -p < backend/sql/data.sql
```

`data.sql` 是幂等初始化脚本，不会清空已有业务数据；重复执行只会补齐缺失的初始数据。

#### 2. 启动后端

```bash
cd backend

# 使用 Maven 编译运行
mvn spring-boot:run

# 或指定环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端启动后:
- API 地址: http://localhost:8080/api/v1
- Swagger 文档: http://localhost:8080/api/doc.html

#### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问: http://localhost:3000

### 生产部署

部署到服务器时不使用 Docker，直接用 jar + Nginx 即可，详见:

- `docs/deploy-bt.md` — 宝塔面板部署指南
- `docs/deploy-manual.md` — 纯命令行手动部署指南

打包命令:

```bash
# 后端打包
cd backend
mvn package -DskipTests
# 产物: target/cms-backend-1.0.0.jar

# 前端打包
cd frontend
npm run build
# 产物: dist/
```

启动 jar 时指定 prod 配置:

```bash
java -jar cms-backend-1.0.0.jar --spring.profiles.active=prod
```

如果数据库 / Redis 地址不是默认值，用环境变量覆盖:

```bash
DB_HOST=192.168.1.100 DB_PASSWORD=yourpass JWT_SECRET=your-strong-secret REDIS_HOST=192.168.1.100 java -jar cms-backend-1.0.0.jar --spring.profiles.active=prod
```

### CI/CD 自动部署

项目内置 GitHub Actions 工作流，push 到 main 分支自动构建并部署到服务器，详见 `docs/deploy-cicd.md`。首次配置只需 3 步：生成 SSH 密钥 → 服务器装 systemd 服务 → GitHub 填 4 个 Secrets。

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 (全部权限) |
| editor | admin123 | 内容编辑 (仅内容模块) |

## 核心功能

### 认证与权限
- JWT 双 Token 机制 (Access Token + Refresh Token)
- Access Token 过期自动刷新
- 基于 RBAC 的权限控制
- 前端按钮级权限控制
- 后端接口级权限校验

### 系统管理
- 用户管理: CRUD、启用/禁用、重置密码、分配角色
- 角色管理: CRUD、权限分配 (菜单树)
- 菜单管理: 树形结构、目录/菜单/按钮三种类型

### 内容管理
- 分类管理: 树形分类、排序、启用/禁用
- 文章管理: CRUD、草稿/发布/下线、批量删除、Markdown 编辑
- 通知管理: CRUD、发布/撤回、已读/未读

### 仪表盘
- 用户总数、文章总数、分类总数等统计
- 用户增长趋势图
- 文章发布趋势图
- 分类文章统计饼图

### 日志管理
- 登录日志: 记录登录用户名、IP、User-Agent、结果
- 操作日志: 记录操作人、请求方法、地址、参数、耗时

## 架构原则

1. **数据库访问**: 统一使用 JdbcTemplate，不引入 ORM
2. **分层架构**: Controller → Service → Repository → JdbcTemplate
3. **统一响应**: 所有 API 返回 `{ code, message, data }` 格式
4. **统一异常**: `@RestControllerAdvice` 全局异常处理
5. **参数绑定**: 所有 SQL 使用 `?` 参数绑定，防止注入
6. **逻辑删除**: 所有业务表使用 `deleted` 字段
7. **事务管理**: 多表操作使用 `@Transactional`
8. **前后端分离**: 前端通过 Axios 调用后端 RESTful API

## 环境变量

以下变量均支持环境变量覆盖；开发环境有本地默认值，生产环境必须显式设置数据库密码和 JWT 密钥：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_HOST | MySQL 主机 | localhost |
| DB_PORT | MySQL 端口 | 3306 |
| DB_NAME | 数据库名 | cms_db |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | dev: 123456；prod: 必填 |
| REDIS_HOST | Redis 主机 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |
| REDIS_PASSWORD | Redis 密码 | (空) |
| JWT_SECRET | JWT 密钥 | dev: 内置默认；prod: 必填 |

## License

MIT
