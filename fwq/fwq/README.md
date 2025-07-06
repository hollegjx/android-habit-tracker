# 习惯追踪服务器

## 简化版后端服务器

### 功能特点
- ✅ 完整的CORS支持
- ✅ 详细的请求/响应日志
- ✅ 用户认证API
- ✅ 习惯管理API
- ✅ 健康检查端点
- ✅ 错误处理

### API 端点

#### 基础端点
- `GET /health` - 服务器健康检查
- `GET /api/ping` - API连通性测试

#### 认证端点
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

#### 用户端点
- `GET /api/users/profile` - 获取用户信息

#### 习惯端点
- `GET /api/habits` - 获取习惯列表
- `POST /api/habits` - 创建新习惯

### 启动命令
```bash
npm install
npm start
```

### 测试用户
- 用户名: test
- 密码: 123456