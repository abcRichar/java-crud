# CMS 系统手动部署指南（服务器无 Docker 版）

## 0. 部署架构

```
浏览器
  │
  ▼
Nginx (80 端口)
  ├── /            → 前端静态文件 (dist)
  └── /api/*       → 反向代理到后端
                        │
                        ▼
                Spring Boot (8080, context-path=/api)
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
          MySQL (3306)        Redis (6379)
```

前后端分离，后端只监听内网，所有流量统一走 Nginx 80 端口，不用额外开 8080。

## 1. 服务器要求

| 项目 | 要求 |
|------|------|
| 系统 | CentOS 7+ / Ubuntu 20.04+ |
| 配置 | 2 核 4G 起步 |
| 软件 | JDK 17、Maven 3.9、MySQL 8、Redis 5+、Nginx、Node 18+（只构建前端时需要） |
| 端口 | 只开放 80 |

## 2. 安装依赖

### Ubuntu / Debian

```bash
apt update
apt install -y openjdk-17-jdk maven mysql-server redis-server nginx curl
# Node 18+（前端构建用，装完即用）
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt install -y nodejs
```

### CentOS / Rocky

```bash
yum install -y java-17-openjdk-devel maven nginx redis curl
# MySQL 8（官方源）
rpm -Uvh https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm
yum install -y mysql-community-server
# Node 18+（前端构建用）
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
yum install -y nodejs
```

### 启动 MySQL 和 Redis

```bash
systemctl enable --now mysqld        # CentOS 是 mysqld，Ubuntu 是 mysql
systemctl enable --now redis
systemctl enable --now nginx
```

## 3. 初始化数据库

把项目里的 SQL 文件传到服务器（`backend/sql/schema.sql` 和 `data.sql`）：

```bash
mkdir -p /opt/cms/sql
# 本地执行：scp backend/sql/schema.sql backend/sql/data.sql root@服务器IP:/opt/cms/sql/
```

导入（schema.sql 自带建库，data.sql 可安全重复执行）：

```bash
mysql -uroot -p < /opt/cms/sql/schema.sql
mysql -uroot -p < /opt/cms/sql/data.sql
```

验证：

```bash
mysql -uroot -p -e "USE cms_db; SHOW TABLES;"   # 应看到 13 张表
```

生产环境建议建专用账号，别用 root：

```sql
CREATE USER IF NOT EXISTS 'cms'@'localhost' IDENTIFIED BY '换成你自己的强密码';
GRANT ALL PRIVILEGES ON cms_db.* TO 'cms'@'localhost';
FLUSH PRIVILEGES;
```

## 4. 构建后端

两种方式任选：

### 方式 A：在服务器上构建（推荐，服务器要有 Maven）

```bash
mkdir -p /opt/cms/backend
# 把 backend 目录整个传上去：scp -r backend root@服务器IP:/opt/cms/
cd /opt/cms/backend
mvn package -DskipTests
# 产物：target/cms-backend-1.0.0.jar
```

### 方式 B：本机构建后上传

本机装好 Maven 后执行 `mvn package -DskipTests`，然后：

```bash
scp target/cms-backend-1.0.0.jar root@服务器IP:/opt/cms/backend/
```

## 5. 配置并启动后端

后端用 `prod` profile，全部连接信息通过环境变量注入（注意：**这些变量没有默认值，少一个都启动失败**）：

| 环境变量 | 示例值 | 说明 |
|----------|--------|------|
| DB_HOST | 127.0.0.1 | MySQL 地址 |
| DB_PORT | 3306 | MySQL 端口 |
| DB_NAME | cms_db | 库名 |
| DB_USERNAME | cms | 数据库账号 |
| DB_PASSWORD | 你的密码 | 数据库密码 |
| REDIS_HOST | 127.0.0.1 | Redis 地址 |
| REDIS_PORT | 6379 | Redis 端口 |
| REDIS_PASSWORD | (留空) | Redis 密码，没有就设为空串 |
| JWT_SECRET | (可选) | 签名密钥，建议生产换一个随机值 |

用 systemd 托管，进程崩溃自动重启：

```bash
cat > /etc/systemd/system/cms-backend.service <<'EOF'
[Unit]
Description=CMS Backend
After=network.target mysql.service redis.service

[Service]
User=root
WorkingDirectory=/opt/cms/backend
Environment=DB_HOST=127.0.0.1
Environment=DB_PORT=3306
Environment=DB_NAME=cms_db
Environment=DB_USERNAME=cms
Environment=DB_PASSWORD=你的数据库密码
Environment=REDIS_HOST=127.0.0.1
Environment=REDIS_PORT=6379
Environment="REDIS_PASSWORD="
Environment=JWT_SECRET=换成一段随机长字符串
ExecStart=/usr/bin/java -jar /opt/cms/backend/cms-backend-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable --now cms-backend
```

验证启动成功：

```bash
journalctl -u cms-backend -f          # 实时看日志，看到 "Started CmsApplication" 即成功
curl http://127.0.0.1:8080/api/auth/login -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# 返回 JSON 里带 accessToken 就说明后端 + 数据库 + Redis 全通了
```

## 6. 构建前端 + Nginx

### 构建前端

```bash
cd frontend
npm install
npm run build
# 产物：dist/
```

把 dist 传到服务器：

```bash
scp -r dist root@服务器IP:/opt/cms/frontend/
```

### Nginx 配置

前端打包后请求路径是 `/api`（相对路径，无需改代码），Nginx 直接透传给后端——后端 context-path 也是 `/api`，所以 **proxy_pass 不要加路径改写**：

```bash
cat > /etc/nginx/conf.d/cms.conf <<'EOF'
server {
    listen 80;
    server_name _;

    # 前端静态资源
    root /opt/cms/frontend/dist;
    index index.html;

    # 单页应用路由回退（刷新不 404）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端接口反向代理（不加尾部斜杠，原样透传）
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
EOF

nginx -t        # 配置语法检查
systemctl reload nginx
```

## 7. 上线验证清单

```bash
# 1. 首页可访问
curl -I http://服务器IP/

# 2. 登录接口通
curl http://服务器IP/api/auth/login -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 3. 浏览器打开 http://服务器IP/ 用 admin/admin123 登录
```

## 8. 常见问题

**Q1: 启动报 "Could not resolve placeholder 'DB_HOST'"**
环境变量没设全。prod 配置里所有 `DB_*` / `REDIS_*` 变量都是必填的，`REDIS_PASSWORD` 也要设为空串。

**Q2: MySQL root 登录不上去**
Ubuntu 的 MySQL 8 root 默认用 auth_socket，直接 `sudo mysql` 免密进，再改密码或按第 3 节建专用账号。

**Q3: 8080 端口忘了关 / 防火墙挡了 80**
后端监听 127.0.0.1 就不用开 8080。80 端口被挡：`firewall-cmd --permanent --add-port=80/tcp && firewall-cmd --reload`（CentOS）或 `ufw allow 80`（Ubuntu）。

**Q4: 需要换域名/加 HTTPS**
改 `/etc/nginx/conf.d/cms.conf` 里的 `server_name`，HTTPS 用 certbot 申请证书，只动 Nginx，后端不用改。

**Q5: 更新部署**
```bash
# 后端：传新 jar → systemctl restart cms-backend
# 前端：传新 dist → 无需重启 Nginx，直接覆盖
# 数据库：schema.sql 有变更时先备份再执行，data.sql 可安全重复执行
```

**Q6: 时区差 8 小时**
服务器时区不是 Asia/Shanghai 时，给 MySQL 设置：
```sql
SET GLOBAL time_zone = '+08:00';
```

**Q7: 端口被占用**
`ss -lntp | grep 8080` 查占用进程；`kill` 掉后 `systemctl restart cms-backend`。

## 9. 默认账号

- 超级管理员：admin / admin123
- 内容编辑：editor / admin123

上线后第一时间改密码。
