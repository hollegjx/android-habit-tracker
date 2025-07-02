const express = require('express');
const { validate } = require('../middleware/validation');
const { authenticate } = require('../middleware/auth');
const { sendMessageSchema } = require('../utils/validation');
const {
  getConversations,
  getMessages,
  sendMessage,
  chatWithAI,
  markAsRead
} = require('../controllers/chatController');

const router = express.Router();

// 所有聊天路由都需要认证
router.use(authenticate);

// 获取对话列表
router.get('/conversations', getConversations);

// 获取对话消息
router.get('/conversations/:conversationId/messages', getMessages);

// 发送消息
router.post('/messages', validate(sendMessageSchema), sendMessage);

// 与AI聊天
router.post('/ai/chat', chatWithAI);

// 标记消息为已读
router.post('/conversations/:conversationId/read', markAsRead);

module.exports = router;