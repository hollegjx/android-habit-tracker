const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const { createServer } = require('http');
const { Server } = require('socket.io');
require('dotenv').config();

const authRoutes = require('./src/routes/auth');
const userRoutes = require('./src/routes/users');
const friendRoutes = require('./src/routes/friends');
const chatRoutes = require('./src/routes/chat');
const aiCharacterRoutes = require('./src/routes/aiCharacter');
const adminRoutes = require('./src/routes/admin');
const adminFastRoutes = require('./src/routes/admin-fast');
const habitRoutes = require('./src/routes/habits');
const errorHandler = require('./src/middleware/errorHandler');
const { initializeSocket } = require('./src/utils/socket');

const app = express();
const server = createServer(app);
const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
  }
});

// 中间件配置
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", "'unsafe-inline'"],
      scriptSrcAttr: ["'unsafe-inline'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      imgSrc: ["'self'", "data:", "https:"],
      connectSrc: ["'self'", "http:", "https:", "http://localhost:*", "http://dkht.gjxlsy.top"]
    }
  },
  crossOriginOpenerPolicy: false
}));

app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept', 'Origin', 'User-Agent', 'X-Platform', 'X-App-Version', 'X-OS-Version', 'X-Device-Model'],
  credentials: false
}));

app.use(morgan('combined'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// 静态文件服务
app.use('/uploads', express.static('uploads'));
app.use('/', express.static('public'));

// 路由配置
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/friends', friendRoutes);
app.use('/api/chat', chatRoutes);
app.use('/api/ai-characters', aiCharacterRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/admin-fast', adminFastRoutes); // 快速测试路由
app.use('/api/habits', habitRoutes);

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// API连通性测试
app.get('/api/ping', (req, res) => {
  res.json({
    success: true,
    message: 'pong',
    timestamp: new Date().toISOString(),
    server: 'habit-tracker-api'
  });
});

// Socket.IO 初始化
initializeSocket(io);

// 错误处理中间件
app.use(errorHandler);

// 404处理 - 只处理API路由，不影响静态文件
app.use('/api/*', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'API接口不存在'
  });
});

// 对于非API路由的404，返回简单的HTML响应
app.use('*', (req, res) => {
  // 如果请求的是HTML文件或者没有扩展名，返回友好的404页面
  if (req.originalUrl.endsWith('.html') || !req.originalUrl.includes('.')) {
    res.status(404).send(`
      <html>
        <head><title>404 - 页面未找到</title></head>
        <body>
          <h1>404 - 页面未找到</h1>
          <p>请求的资源 ${req.originalUrl} 不存在</p>
          <p><a href="/">返回首页</a> | <a href="/admin.html">管理后台</a></p>
        </body>
      </html>
    `);
  } else {
    res.status(404).json({
      success: false,
      message: '资源不存在'
    });
  }
});

const PORT = process.env.PORT || 3000;

server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 服务器运行在端口 ${PORT}`);
  console.log(`📊 健康检查: http://localhost:${PORT}/health`);
  console.log(`📁 文件上传: http://localhost:${PORT}/uploads`);
});
