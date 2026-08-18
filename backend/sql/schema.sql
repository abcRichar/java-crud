-- ============================================================
-- CMS 内容管理系统 - 数据库 Schema
-- MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS cms_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cms_db;

-- -----------------------------------------------------------
-- sys_user 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    nickname    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
    avatar      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像',
    email       VARCHAR(128) NOT NULL DEFAULT '' COMMENT '邮箱',
    phone       VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '手机号',
    password    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- sys_role 角色表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    code        VARCHAR(64)  NOT NULL COMMENT '角色编码',
    remark      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -----------------------------------------------------------
-- sys_menu 菜单/权限表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID, 0=顶级',
    name        VARCHAR(64)  NOT NULL COMMENT '路由名称',
    title       VARCHAR(64)  NOT NULL COMMENT '显示标题',
    path        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '路由路径',
    component   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '前端组件路径',
    icon        VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '图标',
    type        VARCHAR(20)  NOT NULL DEFAULT 'MENU' COMMENT '类型: DIRECTORY/MENU/BUTTON',
    permission  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '权限标识',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见: 0=隐藏, 1=显示',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单/权限表';

-- -----------------------------------------------------------
-- sys_user_role 用户角色关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    role_id     BIGINT       NOT NULL COMMENT '角色ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------------
-- sys_role_menu 角色菜单关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id     BIGINT       NOT NULL COMMENT '角色ID',
    menu_id     BIGINT       NOT NULL COMMENT '菜单ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- -----------------------------------------------------------
-- cms_category 分类表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cms_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID, 0=顶级',
    name        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    slug        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '分类别名',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- -----------------------------------------------------------
-- cms_article 文章表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cms_article (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title           VARCHAR(255) NOT NULL COMMENT '标题',
    summary         VARCHAR(500) NOT NULL DEFAULT '' COMMENT '摘要',
    content         LONGTEXT     NOT NULL COMMENT '内容(Markdown)',
    cover           VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图',
    category_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '分类ID',
    author_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '作者ID',
    author_name     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '作者名',
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/OFFLINE',
    view_count      INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    like_count      INT          NOT NULL DEFAULT 0 COMMENT '点赞量',
    seo_title       VARCHAR(255) NOT NULL DEFAULT '' COMMENT 'SEO标题',
    seo_description VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'SEO描述',
    published_at    DATETIME     NULL COMMENT '发布时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_author_id (author_id),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- -----------------------------------------------------------
-- cms_tag 标签表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cms_tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '标签名称',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- -----------------------------------------------------------
-- cms_article_tag 文章标签关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cms_article_tag (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id  BIGINT       NOT NULL COMMENT '文章ID',
    tag_id      BIGINT       NOT NULL COMMENT '标签ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    KEY idx_article_id (article_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- -----------------------------------------------------------
-- sys_notice 通知表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_notice (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(255) NOT NULL COMMENT '通知标题',
    content     LONGTEXT     NOT NULL COMMENT '通知内容',
    type        VARCHAR(20)  NOT NULL DEFAULT 'NOTICE' COMMENT '类型: SYSTEM/NOTICE/ACTIVITY/MESSAGE',
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/WITHDRAWN',
    created_by  BIGINT       NOT NULL DEFAULT 0 COMMENT '创建人ID',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- -----------------------------------------------------------
-- sys_notice_user 通知用户关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_notice_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    notice_id   BIGINT       NOT NULL COMMENT '通知ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
    read_at     DATETIME     NULL COMMENT '阅读时间',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notice_user (notice_id, user_id),
    KEY idx_notice_id (notice_id),
    KEY idx_user_id (user_id),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知用户关联表';

-- -----------------------------------------------------------
-- sys_login_log 登录日志表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_login_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    ip          VARCHAR(64)  NOT NULL DEFAULT '' COMMENT 'IP地址',
    user_agent  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'User-Agent',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '登录结果: 0=失败, 1=成功',
    message     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '消息',
    login_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- -----------------------------------------------------------
-- sys_operation_log 操作日志表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '用户ID',
    username    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '用户名',
    method      VARCHAR(10)  NOT NULL DEFAULT '' COMMENT '请求方法',
    url         VARCHAR(500) NOT NULL DEFAULT '' COMMENT '请求地址',
    params      TEXT         NULL COMMENT '请求参数',
    operation   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '操作内容',
    ip          VARCHAR(64)  NOT NULL DEFAULT '' COMMENT 'IP地址',
    cost_time   BIGINT       NOT NULL DEFAULT 0 COMMENT '耗时(ms)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
