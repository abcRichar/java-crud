# CI/CD 配置指南（GitHub Actions）

本项目已内置 GitHub Actions 工作流（`.github/workflows/deploy.yml`），push 到 main 分支自动构建部署。下面是首次配置步骤。

## 整体流程

```
你 push 代码到 GitHub
       ↓
GitHub Actions 自动触发
       ↓
构建后端 jar + 前端 dist
       ↓
SSH 上传到服务器
       ↓
systemctl restart cms-backend
       ↓
健康检查通过 → 部署完成
```

全程无需手动操作，push 完等 2-3 分钟就部署好了。

## 第一步：服务器配置 SSH 密钥

GitHub Actions 需要通过 SSH 免密连接你的服务器，所以要生成一对密钥。

### 1.1 在本地生成密钥对

```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/cms-deploy-key -N ""
```

生成两个文件：
- `~/.ssh/cms-deploy-key` —— **私钥**（给 GitHub 用）
- `~/.ssh/cms-deploy-key.pub` —— **公钥**（放服务器上）

### 1.2 把公钥放到服务器

```bash
# 本地执行，把公钥传到服务器
ssh-copy-id -i ~/.ssh/cms-deploy-key.pub root@你的服务器IP

# 或者手动追加
cat ~/.ssh/cms-deploy-key.pub | ssh root@你的服务器IP "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

### 1.3 验证免密登录

```bash
ssh -i ~/.ssh/cms-deploy-key root@你的服务器IP "echo OK"
# 输出 OK 就说明配好了
```

## 第二步：服务器配置 systemd 服务

CI/CD 重启后端用 `systemctl restart cms-backend`，需要先装好 systemd 服务文件。

### 2.1 上传服务文件

```bash
# 把项目里的 deploy/cms-backend.service 传到服务器
scp deploy/cms-backend.service root@你的服务器IP:/etc/systemd/system/
```

### 2.2 修改服务文件中的密码

```bash
ssh root@你的服务器IP
vi /etc/systemd/system/cms-backend.service
# 至少修改以下两行：
#   Environment=DB_PASSWORD=请填写数据库密码
#   Environment=JWT_SECRET=请填写至少32字节的随机密钥
```

### 2.3 创建日志目录并启动

```bash
mkdir -p /www/wwwroot/cms/backend/logs
systemctl daemon-reload
systemctl enable cms-backend
systemctl start cms-backend
systemctl status cms-backend  # 看到 active (running) 就 OK
```

## 第三步：GitHub 配置 Secrets

进入你的 GitHub 仓库 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**，添加以下 4 个：

| Secret 名 | 值 | 说明 |
|-----------|-----|------|
| `SSH_HOST` | 你的服务器IP | 如 `123.45.67.89` |
| `SSH_PORT` | 22 | 宝塔默认端口，改过就填改后的 |
| `SSH_USER` | root | 登录用户名 |
| `SSH_KEY` | （私钥内容） | 把 `~/.ssh/cms-deploy-key` 文件内容**全部**粘贴进去，包括 `-----BEGIN` 和 `-----END` 行 |

> 添加 `SSH_KEY` 时：在本地执行 `cat ~/.ssh/cms-deploy-key`，把输出的**全部内容**（从 `-----BEGIN OPENSSH PRIVATE KEY-----` 到 `-----END OPENSSH PRIVATE KEY-----`）复制粘贴。

## 第四步：触发部署

配置完成后有两种方式触发：

1. **自动触发**：`git push origin main`，GitHub Actions 自动跑
2. **手动触发**：GitHub 仓库 → **Actions** → 选 `Deploy CMS` → 右侧 `Run workflow` 按钮

## 部署日志

在 GitHub 仓库的 **Actions** 页面可以看到每次部署的完整日志，包括：
- 后端编译输出
- 前端构建输出
- SSH 上传结果
- 重启结果
- 健康检查结果（HTTP 状态码）

如果健康检查失败，日志里会自动打印最后 20 行后端日志，方便排查。

## 常见问题

### Q1: SSH 连接被拒绝
检查 `SSH_PORT` 是否正确，宝塔可能改过 SSH 端口。在服务器上 `vi /etc/ssh/sshd_config` 找 `Port` 确认。

### Q2: 健康检查报 502/503
Spring Boot 还在启动（需要 10-15 秒），工作流里 sleep 5 秒可能不够。改 `deploy.yml` 最后一个 step 的 `sleep 5` 改成 `sleep 15`。

### Q3: Maven 构建失败
GitHub Actions 首次构建会下载所有依赖（3-5 分钟），后续有缓存会快。如果报依赖找不到，检查 `pom.xml` 里的仓库地址。

### Q4: 前端 rsync 失败
服务器没装 rsync。`yum install rsync`（CentOS）或 `apt install rsync`（Ubuntu）装一下。

### Q5: 不想每次 push 都部署
把 `deploy.yml` 里的 `push` 触发删掉，只保留 `workflow_dispatch`，改成手动部署：
```yaml
on:
  workflow_dispatch:  # 只手动触发
```
