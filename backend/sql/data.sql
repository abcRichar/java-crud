-- ============================================================
-- CMS 内容管理系统 - 幂等初始数据
-- MySQL 8.x
--
-- 说明：
-- 1. 脚本不会删除或覆盖现有业务数据。
-- 2. 重复执行只会补齐缺失的初始账号、角色、菜单和示例内容。
-- 3. 生产环境首次部署后请立即修改默认账号密码。
-- ============================================================
USE cms_db;

-- -----------------------------------------------------------
-- 管理员账号 (密码: admin123, BCrypt)
-- -----------------------------------------------------------
INSERT INTO sys_user (username, nickname, email, phone, password, status)
SELECT 'admin', '超级管理员', 'admin@example.com', '13800000000',
       '$2a$10$CVTIttGYYdMcOrmI/U0Gruj9SHosuyWNEAgh1aBsH2uBnGYcEXffG', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT INTO sys_user (username, nickname, email, phone, password, status)
SELECT 'editor', '内容编辑', 'editor@example.com', '13800000001',
       '$2a$10$E7m6O1Owx7ondTqH4n.emuncnc3I9IvSEzuq1D0eJ3Gc8AehRpzv6', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'editor');

-- -----------------------------------------------------------
-- 角色
-- -----------------------------------------------------------
INSERT INTO sys_role (name, code, remark, status)
SELECT '超级管理员', 'admin', '系统最高权限', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'admin');

INSERT INTO sys_role (name, code, remark, status)
SELECT '内容编辑', 'editor', '只能管理内容相关模块', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'editor');

-- -----------------------------------------------------------
-- 用户角色关联
-- -----------------------------------------------------------
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.code = 'admin'
WHERE u.username = 'admin';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.code = 'editor'
WHERE u.username = 'editor';

-- -----------------------------------------------------------
-- 菜单/权限
-- -----------------------------------------------------------
INSERT IGNORE INTO sys_menu
    (id, parent_id, name, title, path, component, icon, type, permission, sort, visible, status)
VALUES
    (1, 0, 'dashboard', '仪表盘', '/dashboard', 'dashboard/index', 'DashboardOutlined', 'MENU', '', 1, 1, 1),
    (10, 0, 'system', '系统管理', '/system', '', 'SettingOutlined', 'DIRECTORY', '', 2, 1, 1),
    (20, 0, 'content', '内容管理', '/content', '', 'FileTextOutlined', 'DIRECTORY', '', 3, 1, 1),
    (30, 0, 'log', '日志管理', '/log', '', 'AuditOutlined', 'DIRECTORY', '', 4, 1, 1),
    (11, 10, 'user', '用户管理', '/system/user', 'system/user/index', 'UserOutlined', 'MENU', '', 1, 1, 1),
    (12, 10, 'role', '角色管理', '/system/role', 'system/role/index', 'TeamOutlined', 'MENU', '', 2, 1, 1),
    (13, 10, 'menu', '菜单管理', '/system/menu', 'system/menu/index', 'MenuOutlined', 'MENU', '', 3, 1, 1),
    (21, 20, 'category', '分类管理', '/content/category', 'content/category/index', 'AppstoreOutlined', 'MENU', '', 1, 1, 1),
    (22, 20, 'article', '文章管理', '/content/article', 'content/article/index', 'ProfileOutlined', 'MENU', '', 2, 1, 1),
    (23, 20, 'notice', '通知管理', '/content/notice', 'content/notice/index', 'NotificationOutlined', 'MENU', '', 3, 1, 1),
    (31, 30, 'login-log', '登录日志', '/log/login', 'log/login/index', 'LoginOutlined', 'MENU', 'log:login:list', 1, 1, 1),
    (32, 30, 'operation-log', '操作日志', '/log/operation', 'log/operation/index', 'FileSyncOutlined', 'MENU', 'log:operation:list', 2, 1, 1);

INSERT IGNORE INTO sys_menu (id, parent_id, name, title, type, permission, sort, status) VALUES
    (101, 11, 'user:list', '查看', 'BUTTON', 'user:list', 1, 1),
    (102, 11, 'user:create', '新增', 'BUTTON', 'user:create', 2, 1),
    (103, 11, 'user:update', '编辑', 'BUTTON', 'user:update', 3, 1),
    (104, 11, 'user:delete', '删除', 'BUTTON', 'user:delete', 4, 1),
    (105, 11, 'user:reset', '重置密码', 'BUTTON', 'user:reset', 5, 1),
    (111, 12, 'role:list', '查看', 'BUTTON', 'role:list', 1, 1),
    (112, 12, 'role:create', '新增', 'BUTTON', 'role:create', 2, 1),
    (113, 12, 'role:update', '编辑', 'BUTTON', 'role:update', 3, 1),
    (114, 12, 'role:delete', '删除', 'BUTTON', 'role:delete', 4, 1),
    (121, 13, 'menu:list', '查看', 'BUTTON', 'menu:list', 1, 1),
    (122, 13, 'menu:create', '新增', 'BUTTON', 'menu:create', 2, 1),
    (123, 13, 'menu:update', '编辑', 'BUTTON', 'menu:update', 3, 1),
    (124, 13, 'menu:delete', '删除', 'BUTTON', 'menu:delete', 4, 1),
    (201, 21, 'category:list', '查看', 'BUTTON', 'category:list', 1, 1),
    (202, 21, 'category:create', '新增', 'BUTTON', 'category:create', 2, 1),
    (203, 21, 'category:update', '编辑', 'BUTTON', 'category:update', 3, 1),
    (204, 21, 'category:delete', '删除', 'BUTTON', 'category:delete', 4, 1),
    (211, 22, 'article:list', '查看', 'BUTTON', 'article:list', 1, 1),
    (212, 22, 'article:create', '新增', 'BUTTON', 'article:create', 2, 1),
    (213, 22, 'article:update', '编辑', 'BUTTON', 'article:update', 3, 1),
    (214, 22, 'article:delete', '删除', 'BUTTON', 'article:delete', 4, 1),
    (215, 22, 'article:publish', '发布', 'BUTTON', 'article:publish', 5, 1),
    (216, 22, 'article:offline', '下线', 'BUTTON', 'article:offline', 6, 1),
    (221, 23, 'notice:list', '查看', 'BUTTON', 'notice:list', 1, 1),
    (222, 23, 'notice:create', '新增', 'BUTTON', 'notice:create', 2, 1),
    (223, 23, 'notice:update', '编辑', 'BUTTON', 'notice:update', 3, 1),
    (224, 23, 'notice:delete', '删除', 'BUTTON', 'notice:delete', 4, 1),
    (225, 23, 'notice:publish', '发布', 'BUTTON', 'notice:publish', 5, 1),
    (226, 23, 'notice:withdraw', '撤回', 'BUTTON', 'notice:withdraw', 6, 1);

-- admin 始终拥有全部菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.code = 'admin' AND m.deleted = 0;

-- editor 仅拥有内容管理权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.code = 'editor'
  AND m.deleted = 0
  AND (
      m.id IN (1, 20, 21, 22, 23)
      OR (m.parent_id IN (21, 22, 23) AND m.type = 'BUTTON')
  );

-- -----------------------------------------------------------
-- 分类初始数据
-- -----------------------------------------------------------
INSERT IGNORE INTO cms_category (id, parent_id, name, slug, sort, status) VALUES
    (1, 0, '技术', 'tech', 1, 1),
    (2, 0, '生活', 'life', 2, 1),
    (3, 1, 'Java', 'java', 1, 1),
    (4, 1, 'Spring', 'spring', 2, 1),
    (5, 1, 'React', 'react', 3, 1),
    (6, 1, 'Vue', 'vue', 4, 1),
    (7, 2, '随笔', 'essay', 1, 1),
    (8, 2, '摄影', 'photography', 2, 1);

-- -----------------------------------------------------------
-- 示例文章
-- -----------------------------------------------------------
INSERT INTO cms_article
    (title, summary, content, category_id, author_id, author_name, status, view_count, like_count,
     seo_title, seo_description, published_at)
SELECT 'Spring Boot 3 新特性详解', '深入了解 Spring Boot 3 的核心新特性',
       '# Spring Boot 3 新特性\n\n本文介绍 Spring Boot 3 的主要改进...',
       4, u.id, u.username, 'PUBLISHED', 1280, 56,
       'Spring Boot 3 新特性', 'Spring Boot 3 核心新特性详解', NOW()
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM cms_article WHERE title = 'Spring Boot 3 新特性详解' AND deleted = 0);

INSERT INTO cms_article
    (title, summary, content, category_id, author_id, author_name, status, view_count, like_count,
     seo_title, seo_description, published_at)
SELECT 'React 18 并发渲染机制', '探索 React 18 的 Concurrent Features',
       '# React 18 并发渲染\n\nReact 18 引入了并发渲染...',
       5, u.id, u.username, 'PUBLISHED', 890, 42,
       'React 18 并发渲染', 'React 18 并发渲染机制详解', NOW()
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM cms_article WHERE title = 'React 18 并发渲染机制' AND deleted = 0);

INSERT INTO cms_article
    (title, summary, content, category_id, author_id, author_name, status, view_count, like_count,
     seo_title, seo_description, published_at)
SELECT 'JdbcTemplate 最佳实践', '使用 JdbcTemplate 的最佳实践指南',
       '# JdbcTemplate 最佳实践\n\nSpring JdbcTemplate 是...',
       3, u.id, u.username, 'PUBLISHED', 650, 28,
       'JdbcTemplate 最佳实践', 'JdbcTemplate 使用指南', NOW()
FROM sys_user u
WHERE u.username = 'editor'
  AND NOT EXISTS (SELECT 1 FROM cms_article WHERE title = 'JdbcTemplate 最佳实践' AND deleted = 0);

INSERT INTO cms_article
    (title, summary, content, category_id, author_id, author_name, status, view_count, like_count,
     seo_title, seo_description, published_at)
SELECT 'Tailwind CSS 实战技巧', 'Tailwind CSS 高效开发技巧',
       '# Tailwind CSS 实战\n\nTailwind CSS 是一款...',
       5, u.id, u.username, 'DRAFT', 0, 0,
       '', '', NULL
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM cms_article WHERE title = 'Tailwind CSS 实战技巧' AND deleted = 0);

-- -----------------------------------------------------------
-- 示例通知
-- -----------------------------------------------------------
INSERT INTO sys_notice (title, content, type, status, created_by)
SELECT '系统维护通知', '系统将于本周六凌晨 2:00-4:00 进行维护升级，期间服务暂停。',
       'SYSTEM', 'PUBLISHED', u.id
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM sys_notice WHERE title = '系统维护通知' AND deleted = 0);

INSERT INTO sys_notice (title, content, type, status, created_by)
SELECT '新功能上线', '文章管理模块新增 Markdown 编辑器，欢迎使用！',
       'NOTICE', 'PUBLISHED', u.id
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM sys_notice WHERE title = '新功能上线' AND deleted = 0);

INSERT INTO sys_notice (title, content, type, status, created_by)
SELECT '欢迎使用 CMS', '欢迎使用 CMS 内容管理系统，如有问题请联系管理员。',
       'MESSAGE', 'PUBLISHED', u.id
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (SELECT 1 FROM sys_notice WHERE title = '欢迎使用 CMS' AND deleted = 0);

-- 初始通知发送给脚本执行时已存在的启用用户
INSERT IGNORE INTO sys_notice_user (notice_id, user_id)
SELECT n.id, u.id
FROM sys_notice n
CROSS JOIN sys_user u
WHERE n.title IN ('系统维护通知', '新功能上线', '欢迎使用 CMS')
  AND n.status = 'PUBLISHED'
  AND n.deleted = 0
  AND u.deleted = 0
  AND u.status = 1;
