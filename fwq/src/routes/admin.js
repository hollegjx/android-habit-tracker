const express = require('express');
const { authenticate, requireAdmin } = require('../middleware/auth');
const {
  getSystemStats,
  getUsers,
  updateUserStatus,
  createUser,
  getSystemLogs,
  getUserLoginActivities,
  getFriendRequests,
  getRecentChatMessages,
  getUserActivityLogs,
  getDetailedSystemLogs,
  clearMemoryLogs,
  getDatabaseDebugInfo
} = require('../controllers/adminController');

const router = express.Router();

// 所有管理员路由都需要认证和管理员权限
router.use(authenticate, requireAdmin);

// 获取系统统计信息
router.get('/stats', getSystemStats);

// 获取用户列表
router.get('/users', getUsers);

// 创建用户
router.post('/users', createUser);

// 更新用户状态
router.put('/users/:userId/status', updateUserStatus);

// 获取系统日志
router.get('/logs', getSystemLogs);

// 获取用户登录活动
router.get('/user-login-activities', getUserLoginActivities);

// 获取好友请求数据
router.get('/friend-requests', getFriendRequests);

// 获取最近聊天消息
router.get('/recent-chat-messages', getRecentChatMessages);

// 获取用户活动日志
router.get('/user-activity-logs', getUserActivityLogs);

// 获取详细系统日志
router.get('/detailed-logs', getDetailedSystemLogs);

// 清空内存日志
router.post('/logs/clear', clearMemoryLogs);

// 获取数据库调试信息
router.get('/debug/database', getDatabaseDebugInfo);

module.exports = router;