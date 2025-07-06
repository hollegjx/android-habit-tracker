const express = require('express');
const router = express.Router();
const friendController = require('../controllers/friendController');
const { authenticate } = require('../middleware/auth');
const logger = require('../utils/logger');

/**
 * 好友管理路由 - 完全重新设计版本
 * 所有路由都需要身份验证
 */

// 所有好友路由都需要认证
router.use(authenticate);

// 中间件：记录所有好友API请求
router.use((req, res, next) => {
  logger.info('FRIEND_API', `好友API请求: ${req.method} ${req.path}`, {
    userId: req.user ? req.user.userId : undefined,
    ip: req.ip,
    userAgent: req.get('User-Agent'),
    query: req.query,
    body: req.method === 'POST' || req.method === 'PUT' ? req.body : undefined
  });
  next();
});

// 搜索用户
router.get('/search/:uid', friendController.searchUserByUID);

// 好友请求管理
router.post('/request', friendController.sendFriendRequest);
router.get('/requests', friendController.getFriendRequests);
router.post('/requests/:requestId', friendController.handleFriendRequest);

// 好友列表管理
router.get('/', friendController.getFriends);
router.delete('/:friendId', friendController.removeFriend);
router.put('/:friendId/settings', friendController.updateFriendSettings);

// 好友通知
router.get('/notifications', friendController.getFriendNotifications);

// 健康检查
router.get('/health', (req, res) => {
  res.json({
    success: true,
    message: '好友API服务正常',
    timestamp: new Date().toISOString()
  });
});

module.exports = router;