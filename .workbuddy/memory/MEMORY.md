# Project Memory - CMS 内容管理系统

## 项目概述
- 全栈 CMS 内容管理系统，前后端分离
- 后端: Java 17 + Spring Boot 3.2.5 + Spring Security 6 + JdbcTemplate + MySQL 8 + Redis + JWT
- 前端: React 18 + TypeScript + Vite 5 + Ant Design 5 + Tailwind CSS + Zustand + Axios + ECharts

## 架构约束
- **数据库访问**: 只用 JdbcTemplate，禁止 MyBatis/JPA/Hibernate ORM
- **分层**: Controller → Service → Repository → JdbcTemplate
- **RBAC**: User → UserRole → Role → RoleMenu → Menu
- **JWT**: 双 Token (Access + Refresh)，Access 过期自动刷新

## 关键设计
- 后端 context-path: /api，前端 Vite proxy /api → localhost:8080
- 菜单类型: DIRECTORY / MENU / BUTTON
- 逻辑删除: deleted 字段
- 操作日志: AOP @OperationLog 注解自动记录
- 前端权限: <Permission permission="xxx"> 组件控制按钮显隐

## 默认账号
- admin / admin123 (超级管理员)
- editor / admin123 (内容编辑)

## 验证状态
- 前端 TypeScript 类型检查: 通过 (零错误)
- 前端 Vite 构建: 通过
- 后端: 代码结构完整，需要 Maven 编译验证 (本机未装 Maven)
