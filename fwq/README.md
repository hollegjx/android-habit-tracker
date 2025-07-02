# 习惯追踪应用后端服务器

基于 Node.js + Express + PostgreSQL + Socket.IO 构建的现代化后端API服务。

## 🌟 主要功能

### 用户系统
- ✅ 用户注册/登录（支持用户名或邮箱登录）
- ✅ JWT访问令牌 + 刷新令牌机制
- ✅ 11位数字UID系统（用于加好友）
- ✅ 密码重置（邮箱验证码）
- ✅ 用户资料管理
- ✅ 好友系统（添加/接受/拒绝好友请求）

### 聊天系统
- ✅ 实时聊天（Socket.IO）
- ✅ 私聊、群聊、AI聊天
- ✅ 消息历史记录
- ✅ 未读消息计数
- ✅ 消息已读状态

### AI角色系统
- ✅ 多AI角色配置
- ✅ 自定义AI个性和提示词
- ✅ 支持多种AI模型（GPT-3.5/GPT-4）
- ✅ 角色管理（增删改查）

### 管理员系统
- ✅ 用户管理（查看/禁用/角色管理）
- ✅ AI角色管理
- ✅ 系统统计数据
- ✅ 用户活动日志

## 🚀 快速部署指南

### 方式一：本地部署

#### 1. 环境要求
- Node.js 16+ 
- PostgreSQL 12+
- Redis 6+ (可选，用于缓存)

#### 2. 安装依赖
```bash
cd fwq
npm install
```

#### 3. 配置环境变量
```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置文件
nano .env
```

**重要配置项：**
```env
# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=habit_tracker
DB_USER=postgres
DB_PASSWORD=your_password

# JWT密钥（必须修改）
JWT_SECRET=your_super_secret_jwt_key_here
JWT_REFRESH_SECRET=your_super_secret_refresh_key_here

# AI配置 (使用zetatechs API中转)
NEWAPI_API_KEY=your_zetatechs_api_key
AI_BASE_URL=https://api.zetatechs.com/v1

# 管理员账号
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=admin123456
```

#### 4. 创建数据库
```bash
# 登录PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE habit_tracker;
CREATE USER habit_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE habit_tracker TO habit_user;
```

#### 5. 运行数据库迁移
```bash
# 安装全局knex CLI（如果没有的话）
npm install -g knex

# 运行迁移和种子数据
npm run setup
```

#### 6. 启动服务器
```bash
# 开发模式（自动重启）
npm run dev

# 生产模式
npm start
```

#### 7. 验证部署
访问：http://localhost:3000/health

### 方式二：云服务器部署（推荐）

#### 1. 服务器准备
推荐配置：
- CPU: 2核+
- 内存: 4GB+
- 硬盘: 20GB+
- 系统: Ubuntu 20.04+

#### 2. 安装必要软件
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# 安装Nginx
sudo apt install nginx -y

# 安装PM2（进程管理器）
sudo npm install -g pm2
```

#### 3. 配置PostgreSQL
```bash
# 切换到postgres用户
sudo -u postgres psql

# 创建数据库和用户
CREATE DATABASE habit_tracker;
CREATE USER habit_user WITH PASSWORD 'secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE habit_tracker TO habit_user;
\q
```

#### 4. 部署应用
```bash
# 创建应用目录
sudo mkdir -p /var/www/habit-tracker
sudo chown $USER:$USER /var/www/habit-tracker

# 上传代码到服务器
cd /var/www/habit-tracker
git clone <your-repo-url> .
# 或者使用 scp 上传 fwq 文件夹

# 安装依赖
npm install --production

# 配置环境变量
cp .env.example .env
nano .env
```

#### 5. 配置环境变量（生产环境）
```env
NODE_ENV=production
PORT=3000

# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=habit_tracker
DB_USER=habit_user
DB_PASSWORD=secure_password_here

# JWT密钥（必须是强密码）
JWT_SECRET=your_very_strong_jwt_secret_key_minimum_32_characters
JWT_REFRESH_SECRET=your_very_strong_refresh_secret_key_minimum_32_characters

# AI配置 (使用zetatechs API中转)
NEWAPI_API_KEY=your_zetatechs_api_key
AI_BASE_URL=https://api.zetatechs.com/v1

# 管理员账号
ADMIN_EMAIL=your_admin@email.com
ADMIN_PASSWORD=very_secure_admin_password
```

#### 6. 初始化数据库
```bash
npm run setup
```

#### 7. 配置PM2
```bash
# 创建PM2配置文件
cat > ecosystem.config.js << EOF
module.exports = {
  apps: [{
    name: 'habit-tracker-api',
    script: 'server.js',
    instances: 'max',
    exec_mode: 'cluster',
    env: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    error_file: './logs/err.log',
    out_file: './logs/out.log',
    log_file: './logs/combined.log',
    time: true
  }]
};
EOF

# 创建日志目录
mkdir logs

# 启动应用
pm2 start ecosystem.config.js
pm2 save
pm2 startup
```

#### 8. 配置Nginx反向代理
```bash
# 创建Nginx配置
sudo nano /etc/nginx/sites-available/habit-tracker
```

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # Socket.IO支持
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Server $host;
    }

    # 文件上传大小限制
    client_max_body_size 10M;
}
```

```bash
# 启用配置
sudo ln -s /etc/nginx/sites-available/habit-tracker /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### 9. 配置SSL证书（推荐）
```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取SSL证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加：0 12 * * * /usr/bin/certbot renew --quiet
```

#### 10. 配置防火墙
```bash
# 允许必要端口
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

## 📡 API 接口文档

### 认证接口

#### 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com", 
  "password": "password123",
  "confirmPassword": "password123",
  "nickname": "测试用户",
  "phone": "13800138000"
}
```

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",  // 支持用户名或邮箱
  "password": "password123",
  "deviceInfo": {
    "deviceId": "device123",
    "deviceName": "Android Device",
    "platform": "Android"
  }
}
```

#### 刷新令牌
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your_refresh_token"
}
```

### 用户接口

#### 获取用户信息
```
GET /api/users/profile
Authorization: Bearer <access_token>
```

#### 通过UID搜索用户
```
GET /api/users/search/{uid}
Authorization: Bearer <access_token>
```

#### 发送好友请求
```
POST /api/users/friends/request
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "uid": "12345678901"
}
```

### 聊天接口

#### 获取对话列表
```
GET /api/chat/conversations
Authorization: Bearer <access_token>
```

#### 发送消息
```
POST /api/chat/messages
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "conversationId": "conversation_uuid",
  "content": "Hello World!",
  "messageType": "text"
}
```

#### AI聊天
```
POST /api/chat/ai/chat
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "characterId": "character_uuid",
  "message": "你好，请介绍一下自己"
}
```

### AI角色接口

#### 获取AI角色列表
```
GET /api/ai-characters
```

#### 创建AI角色（管理员）
```
POST /api/ai-characters
Authorization: Bearer <admin_access_token>
Content-Type: application/json

{
  "name": "新AI助手",
  "description": "专业的学习助手",
  "personality": "友善、专业、耐心",
  "systemPrompt": "你是一个专业的学习助手...",
  "model": "gpt-3.5-turbo",
  "modelConfig": {
    "temperature": 0.7,
    "maxTokens": 1000
  }
}
```

## 🔧 管理和维护

### 查看服务状态
```bash
# PM2状态
pm2 status

# 查看日志
pm2 logs habit-tracker-api

# 重启服务
pm2 restart habit-tracker-api
```

### 数据库维护
```bash
# 备份数据库
pg_dump -U habit_user -h localhost habit_tracker > backup_$(date +%Y%m%d).sql

# 恢复数据库
psql -U habit_user -h localhost habit_tracker < backup_20231201.sql
```

### 更新应用
```bash
# 拉取最新代码
git pull origin main

# 安装新依赖
npm install --production

# 运行数据库迁移（如果有）
npm run migrate

# 重启服务
pm2 restart habit-tracker-api
```

## 📞 客户端配置

更新Android应用中的服务器地址：

```kotlin
// NetworkModule.kt
@Named("BASE_URL")
fun provideBaseUrl(): String {
    return "https://your-domain.com/api/v1/"  // 替换为你的服务器地址
}
```

## 🛡️ 安全建议

1. **定期更新系统和依赖包**
2. **使用强密码和JWT密钥**
3. **启用防火墙**
4. **定期备份数据库**
5. **监控服务器资源使用情况**
6. **设置日志轮转**

## 📈 监控和性能优化

### 设置监控
```bash
# 安装监控工具
pm2 install pm2-server-monit

# 查看监控面板
pm2 monit
```

### 性能优化
- 数据库索引优化
- Redis缓存（可选）
- CDN静态资源加速
- Gzip压缩

## 🆘 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查PostgreSQL服务状态
   - 验证数据库配置信息
   - 检查防火墙设置

2. **AI聊天不工作**
   - 验证OpenAI API密钥
   - 检查网络连接
   - 查看API配额使用情况

3. **Socket.IO连接问题**
   - 检查Nginx WebSocket配置
   - 验证跨域设置
   - 查看客户端认证令牌

### 查看详细日志
```bash
# 应用日志
pm2 logs habit-tracker-api --lines 100

# Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# PostgreSQL日志
sudo tail -f /var/log/postgresql/postgresql-*.log
```

## 📧 技术支持

如有问题，请查看：
1. 检查环境配置是否正确
2. 查看服务器日志
3. 验证数据库连接
4. 确认API密钥有效性

---

**部署完成后，您的API服务将在 `https://your-domain.com` 提供服务！** 🎉