#!/bin/bash

# 部署日志系统到云服务器的脚本
# 使用方法：chmod +x deploy-logging-system.sh && ./deploy-logging-system.sh

echo "🚀 开始部署日志管理系统到云服务器..."

SERVER_IP="38.207.179.136"
SERVER_USER="gjx"
SERVER_PATH="/home/gjx/habit-tracker-server"

echo "📁 创建部署文件夹..."
mkdir -p deploy_files
mkdir -p deploy_files/src/utils
mkdir -p deploy_files/src/controllers
mkdir -p deploy_files/src/routes
mkdir -p deploy_files/public

echo "📋 复制修改后的文件..."

# 复制新创建的日志工具
cp src/utils/logger.js deploy_files/src/utils/

# 复制修改后的控制器
cp src/controllers/userController.js deploy_files/src/controllers/
cp src/controllers/adminController.js deploy_files/src/controllers/

# 复制修改后的路由
cp src/routes/admin.js deploy_files/src/routes/

# 复制修改后的管理员页面
cp public/admin.html deploy_files/public/

echo "📦 创建部署包..."
cd deploy_files
tar -czf ../logging-system-deploy.tar.gz *
cd ..

echo "📤 准备上传文件到服务器..."
echo "请手动执行以下命令将文件上传到服务器："
echo ""
echo "1. 上传部署包："
echo "   scp logging-system-deploy.tar.gz ${SERVER_USER}@${SERVER_IP}:~/"
echo ""
echo "2. 登录服务器："
echo "   ssh ${SERVER_USER}@${SERVER_IP}"
echo ""
echo "3. 在服务器上执行以下命令："
echo "   cd ${SERVER_PATH}"
echo "   cp ~/logging-system-deploy.tar.gz ."
echo "   tar -xzf logging-system-deploy.tar.gz"
echo "   pm2 restart all  # 或者重启服务器进程"
echo ""
echo "🔍 部署完成后验证："
echo "   curl -X GET \"https://dkht.gjxlsy.top/api/admin/debug/database\" \\"
echo "     -H \"Authorization: Bearer YOUR_ADMIN_TOKEN\""
echo ""

# 创建详细的部署说明文件
cat > DEPLOY_INSTRUCTIONS.md << 'EOF'
# 日志系统部署指南

## 修改的文件列表

1. **新增文件**：
   - `src/utils/logger.js` - 日志管理工具类

2. **修改文件**：
   - `src/controllers/userController.js` - 添加详细的用户搜索日志
   - `src/controllers/adminController.js` - 添加日志管理API
   - `src/routes/admin.js` - 添加新的管理员路由
   - `public/admin.html` - 添加日志查看界面

## 新增功能

### 1. 系统日志管理
- **路径**: GET `/api/admin/detailed-logs`
- **功能**: 获取系统日志，支持级别、分类、搜索过滤
- **参数**: `level`, `category`, `search`, `limit`

### 2. 清空内存日志
- **路径**: POST `/api/admin/logs/clear`
- **功能**: 清空内存中的日志记录

### 3. 数据库调试信息
- **路径**: GET `/api/admin/debug/database`
- **功能**: 获取完整的数据库调试信息，包括所有用户UID列表

### 4. 管理员界面增强
- **系统日志模块**: 实时查看、过滤系统日志
- **数据库调试模块**: 显示所有用户和统计信息

## 部署后验证步骤

1. **检查API可用性**：
   ```bash
   curl -X GET "https://dkht.gjxlsy.top/api/admin/debug/database" \
     -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
   ```

2. **访问管理员后台**：
   - URL: https://dkht.gjxlsy.top/admin.html
   - 点击 "📋 系统日志" 和 "🗄️ 数据库调试" 按钮

3. **测试用户搜索日志**：
   - 在客户端尝试搜索任何UID
   - 在管理员后台查看日志记录

## 解决的问题

- ✅ 用户搜索无法找到用户的问题可以通过日志详细诊断
- ✅ 管理员可以看到数据库中所有实际存在的用户UID
- ✅ 错误信息更加详细和分类化
- ✅ 系统运行状态可视化监控

EOF

echo "📝 已创建部署说明文件: DEPLOY_INSTRUCTIONS.md"
echo "✅ 部署包已准备完成: logging-system-deploy.tar.gz"