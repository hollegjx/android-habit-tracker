const express = require('express');
const { authenticate, requireAdmin } = require('../middleware/auth');
const {
  getSystemStats,
  getUsers,
  getUserLoginActivities,
  quickHealthCheck
} = require('../controllers/adminController-fast');

const router = express.Router();

// 快速健康检查 - 不需要认证
router.get('/health', quickHealthCheck);

// 需要认证的路由
router.use(authenticate, requireAdmin);

// 获取系统统计信息
router.get('/stats', getSystemStats);

// 获取用户列表
router.get('/users', getUsers);

// 获取用户登录活动
router.get('/user-login-activities', getUserLoginActivities);

module.exports = router;