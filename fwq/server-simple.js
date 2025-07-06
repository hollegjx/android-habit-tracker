const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const { createServer } = require('http');
const { Server } = require('socket.io');
require('dotenv').config();

const authRoutes = require('./src/routes/auth');
const userRoutes = require('./src/routes/users');
const chatRoutes = require('./src/routes/chat');
const aiCharacterRoutes = require('./src/routes/aiCharacter');
const adminRoutes = require('./src/routes/admin');
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

// CORS配置
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

// 健康检查 (在路由之前)
app.get('/health', (req, res) => {
  console.log('Health check requested');
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

// API连通性测试
app.get('/api/ping', (req, res) => {
  console.log('API ping requested');
  res.json({
    success: true,
    message: 'pong',
    timestamp: new Date().toISOString(),
    server: 'habit-tracker-api'
  });
});

// 路由配置
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/chat', chatRoutes);
app.use('/api/ai-characters', aiCharacterRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/habits', habitRoutes);

// Socket.IO 初始化
try {
  initializeSocket(io);
  console.log('Socket.IO initialized successfully');
} catch (error) {
  console.error('Socket.IO initialization failed:', error);
}

// 错误处理中间件
app.use(errorHandler);

// 404处理
app.use('*', (req, res) => {
  console.log('404 - Route not found:', req.method, req.originalUrl);
  res.status(404).json({
    success: false,
    message: '接口不存在',
    path: req.originalUrl,
    method: req.method
  });
});

const PORT = process.env.PORT || 3000;

server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 服务器运行在端口 ${PORT}`);
  console.log(`📊 健康检查: http://localhost:${PORT}/health`);
  console.log(`🔗 API测试: http://localhost:${PORT}/api/ping`);
  console.log(`📁 文件上传: http://localhost:${PORT}/uploads`);
  console.log(`🌐 服务器监听在所有网络接口上`);
});

// 优雅关闭
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  server.close(() => {
    console.log('Process terminated');
  });
});

process.on('SIGINT', () => {
  console.log('SIGINT received, shutting down gracefully');
  server.close(() => {
    console.log('Process terminated');
  });
});