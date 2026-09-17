# CMS 系统宝塔面板部署指南

> 适用：宝塔面板 8.x + 服务器无 Docker  
> 前置：本指南假设你已经有打包好的 **cms-backend-1.0.0.jar**（后端）和 **dist/**（前端），  
> 没有的话见文末「0. 如何拿到这两个包」。

## 部署架构（宝塔视角）

```
宝塔面板
 ├── 软件商店: MySQL 8.0 + Redis + Nginx
 ├── 数据库: cms_db（手动导入 SQL）
 ├── Java 项目: cms-backend-1.0.0.jar，端口 8080（只监听本机）
 └── 网站: 域名/IP → dist 静态文件
                └── /api/* 反向代理到 127.0.0.1:8080
```

## 1. 装运行环境（软件商店）

打开宝塔 → 软件商店，安装：

| 软件         | 版本   | 说明                                    |
| ---------- | ---- | ------------------------------------- |
| MySQL      | 8.0  | 数据库（5.7 也能跑，推荐 8.0）                   |
| Redis      | 7.x  | 缓存                                    |
| Nginx      | 1.2x | Web 服务器（宝塔自带）                         |
| Java 项目管理器 | 最新   | 运行 Spring Boot jar，装好后在里面装 **JDK 17** |

装好后在「Java 项目管理器 → 环境管理」里确认 JDK 版本是 17（装 17 或更高都行）。

## 2. 创建数据库并导入

1. 宝塔 → **数据库 → 添加数据库**
   - 数据库名：`cms_db`
   - 用户名：随便，比如 `cms`
   - 密码：自己设一个强密码（记下来，后面要用）
   - 字符集：`utf8mb4`
2. 在数据库列表点 cms_db 后面的 **管理**（会打开 phpMyAdmin）
3. 点 phpMyAdmin 顶部 **导入** → 选择 `schema.sql` → 执行
4. 再次 **导入** → 选择 `data.sql` → 执行
5. 验证：左侧看到 13 张表（sys_user、cms_article 等）即成功

> data.sql 是幂等脚本，不会清空已有业务数据；重复执行只会补齐缺失的初始数据。

## 3. 部署后端 jar

### 方式 A：Java 项目管理器（推荐，图形化）

1. 宝塔 → **文件** → 进入 `/www/wwwroot`，新建目录 `cms/backend`
2. 把 `cms-backend-1.0.0.jar` 上传到 `/www/wwwroot/cms/backend/`
3. 打开 **Java 项目管理器 → 添加项目**
   - 项目类型：`Spring Boot`
   - 项目名称：`cms-backend`
   - Jar 路径：`/www/wwwroot/cms/backend/cms-backend-1.0.0.jar`
   - 项目端口：`8080`
   - 启动参数：`--spring.profiles.active=prod --server.address=127.0.0.1`
   - **环境变量**（数据库地址有本地默认值；`DB_PASSWORD` 和 `JWT_SECRET` 为必填）：
     | 变量名            | 值                                                |
     | -------------- | ------------------------------------------------ |
     | DB_HOST        | 127.0.0.1                                        |
     | DB_PORT        | 3306                                             |
     | DB_NAME        | cms_db                                           |
     | DB_USERNAME    | 你创建的数据库用户名                                       |
     | DB_PASSWORD    | 你设置的数据库密码                                        |
     | REDIS_HOST     | 127.0.0.1                                        |
     | REDIS_PORT     | 6379                                             |
     | REDIS_PASSWORD | Redis 密码，没设密码就留空串                                |
     | JWT_SECRET     | 随便一串随机字符（如 `cms-bt-deploy-2026-secret-key-xxxx`） |
4. 点 **提交** 后项目自动启动，点项目后面的 **日志** 看到 `Started CmsApplication` 即成功

> 界面差异：不同版本宝塔「环境变量」的位置可能叫「自定义配置」或要在启动命令里写  
> `export DB_HOST=... && java -jar ...`。以你面板实际界面为准，只要最终进程拿到这些变量就行。

### 方式 B：SSH 命令行（Java 项目管理器装不上/收费时）

```bash
# 上传 jar 到 /www/wwwroot/cms/backend/ 后执行
cat > /etc/systemd/system/cms-backend.service <<'EOF'
[Unit]
Description=CMS Backend
After=network.target mysql.service redis.service

[Service]
User=root
WorkingDirectory=/www/wwwroot/cms/backend
Environment=DB_HOST=127.0.0.1
Environment=DB_PORT=3306
Environment=DB_NAME=cms_db
Environment=DB_USERNAME=cms
Environment=DB_PASSWORD=请填写数据库密码
Environment=REDIS_HOST=127.0.0.1
Environment=REDIS_PORT=6379
Environment="REDIS_PASSWORD="
Environment=JWT_SECRET=请填写至少32字节的随机密钥
ExecStart=/usr/bin/java -jar /www/wwwroot/cms/backend/cms-backend-1.0.0.jar --spring.profiles.active=prod --server.address=127.0.0.1
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload && systemctl enable --now cms-backend
```

## 4. 部署前端

1. 宝塔 → **文件** → `/www/wwwroot/cms/` 新建目录 `frontend`
2. 把 `dist/` 里的**所有内容**（index.html、assets 文件夹）上传到 `/www/wwwroot/cms/frontend/`  
   （是 dist 里面的内容，不是 dist 这个文件夹本身）
3. 宝塔 → **网站 → 添加站点**
   - 域名：填服务器 IP 或你的域名（没有域名就填 IP）
   - 根目录：`/www/wwwroot/cms/frontend`
   - PHP 版本：纯静态，选「纯静态」即可
4. 点「提交」，然后进 **网站设置 → 配置文件**，把内容替换为：

```nginx
server {
    listen 80;
    server_name 你的域名或IP;
    root /www/wwwroot/cms/frontend;
    index index.html;

    # 单页应用路由回退（刷新页面不 404）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端接口反向代理（原样透传，不要加路径改写）
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 静态资源缓存
    location /assets/ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

1. 保存后点「重载配置」

## 5. 验证

```bash
# 面板 SSH 里执行，后端接口通不通（返回 JSON 里有 accessToken 即全通）
curl http://127.0.0.1:8080/api/v1/auth/login -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

浏览器打开 `http://你的IP/`，用 admin/admin123 登录。

## 6. 宝塔常见坑

**1. 浏览器打不开，只有 IP:8080 能通**  
8080 端口暴露到公网了。后端启动参数加了 `--server.address=127.0.0.1` 就不会有这个问题；  
再去宝塔「安全」页确认只放行了 80，没放行 8080。

**2. 登录报 401 / 接口 404**  
99% 是后端没拿到新版代码。看 Java 项目日志里的报错信息，如果是数据库连接失败，检查 DB_PASSWORD 等环境变量是否正确。

**3. 前端能开但接口全 404**  
检查 Nginx 的 `location /api/` 是否加了 `proxy_pass http://127.0.0.1:8080;`（不带 /api 后缀）。  
写成 `http://127.0.0.1:8080/api/` 会双重拼接，必 404。

**4. 数据库导入报错**  
phpMyAdmin 导入大文件会超时，schema.sql/data.sql 很小一般没事。报错就改用 SSH：  
`mysql -u用户名 -p密码 cms_db < /www/wwwroot/cms/sql/schema.sql`

**5. Redis 连接失败**  
宝塔 Redis 默认无密码仅本地。如果你在面板里给 Redis 设过密码，REDIS_PASSWORD 必须填；  
没设过就留空串（注意是空字符串，不是不填）。

**6. 更新部署**

- 后端：上传新 jar 覆盖 → Java 项目里重启
- 前端：上传新 dist 覆盖文件 → Nginx 不用动
- 数据库：schema.sql 有变更先备份再导；data.sql 幂等，可安全重跑且会保留已有业务数据

## 0. 如何拿到 jar 和 dist

项目根目录执行（Windows 本机）：

```bash
# 后端 jar → backend/target/cms-backend-1.0.0.jar
cd backend && mvn package -DskipTests

# 前端 dist → frontend/dist/
cd ../frontend && npm install && npm run build
```

然后通过宝塔「文件」上传这两个东西，或 SSH 的 scp 上传。

## 默认账号

- 超级管理员：admin / admin123
- 内容编辑：editor / admin123

上线后第一时间改密码。
