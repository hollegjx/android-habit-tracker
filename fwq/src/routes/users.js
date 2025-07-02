const express = require('express');
const { validate } = require('../middleware/validation');
const { authenticate } = require('../middleware/auth');
const { updateProfileSchema, addFriendSchema } = require('../utils/validation');
const {
  getProfile,
  updateProfile,
  searchUserByUID,
  sendFriendRequest,
  getFriendRequests,
  handleFriendRequest,
  getFriends
} = require('../controllers/userController');

const router = express.Router();

// 所有用户路由都需要认证
router.use(authenticate);

// 获取当前用户信息
router.get('/profile', getProfile);

// 更新用户信息
router.put('/profile', validate(updateProfileSchema), updateProfile);

// 通过UID搜索用户
router.get('/search/:uid', searchUserByUID);

// 发送好友请求
router.post('/friends/request', validate(addFriendSchema), sendFriendRequest);

// 获取好友请求列表
router.get('/friends/requests', getFriendRequests);

// 处理好友请求
router.post('/friends/requests/:requestId', handleFriendRequest);

// 获取好友列表
router.get('/friends', getFriends);

module.exports = router;