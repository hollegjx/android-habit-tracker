#!/bin/bash

echo "=== 服务器状态检查 ==="

echo "1. 检查PM2进程..."
pm2 status

echo -e "\n2. 检查端口监听..."
netstat -tlnp | grep -E ":(80|443|3000)\s"

echo -e "\n3. 检查防火墙状态..."
sudo ufw status

echo -e "\n4. 测试本地API..."
echo "Health Check:"
curl -s http://localhost:3000/health || echo "❌ localhost:3000 失败"

echo -e "\nAPI Test:"
curl -s http://localhost:3000/api/ai-characters || echo "❌ API测试失败"

echo -e "\n5. 检查Nginx配置..."
sudo nginx -t

echo -e "\n6. 检查Nginx状态..."
sudo systemctl status nginx --no-pager

echo -e "\n7. 测试通过Nginx的访问..."
curl -s http://localhost/health || echo "❌ Nginx代理失败"

echo -e "\n8. 检查进程..."
ps aux | grep -E "(node|nginx)" | grep -v grep

echo -e "\n=== 检查完成 ==="