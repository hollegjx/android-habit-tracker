const express = require('express');
const { validate } = require('../middleware/validation');
const { authenticate } = require('../middleware/auth');
const { updateProfileSchema } = require('../utils/validation');
const {
  getProfile,
  updateProfile,
  getUserStats,
  searchUsers,
  updateOnlineStatus
} = require('../controllers/userController');

const router = express.Router();

// 所有用户路由都需要认证
router.use(authenticate);

// 用户资料管理
router.get('/profile', getProfile);
router.put('/profile', validate(updateProfileSchema), updateProfile);

// 用户统计信息
router.get('/stats', getUserStats);

// 用户搜索 (通用搜索，不涉及好友关系)
router.get('/search', searchUsers);

// 更新在线状态
router.post('/online-status', updateOnlineStatus);

// 健康检查
router.get('/health', (req, res) => {
  res.json({
    success: true,
    message: '用户API服务正常',
    timestamp: new Date().toISOString()
  });
});

module.exports = router;