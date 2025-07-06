const Joi = require('joi');

// 用户注册验证
const registerSchema = Joi.object({
  username: Joi.string().alphanum().min(3).max(30).required(),
  email: Joi.string().email().required(),
  password: Joi.string().min(6).required(),
  confirmPassword: Joi.string().valid(Joi.ref('password')).required(),
  nickname: Joi.string().min(1).max(100).required(),
  phone: Joi.string().pattern(/^1[3-9]\d{9}$/).optional(),
  verificationCode: Joi.string().length(6).optional()
});

// 用户登录验证
const loginSchema = Joi.object({
  username: Joi.string().required(),
  password: Joi.string().required(),
  deviceInfo: Joi.object({
    deviceId: Joi.string(),
    deviceName: Joi.string(),
    platform: Joi.string(),
    osVersion: Joi.string(),
    appVersion: Joi.string(),
    manufacturer: Joi.string(),
    model: Joi.string()
  }).optional()
});

// 密码重置验证
const resetPasswordSchema = Joi.object({
  email: Joi.string().email().required(),
  code: Joi.string().length(6).required(),
  newPassword: Joi.string().min(6).required(),
  confirmPassword: Joi.string().valid(Joi.ref('newPassword')).required()
});

// 发送验证码验证
const sendCodeSchema = Joi.object({
  target: Joi.string().required(),
  type: Joi.string().valid('email', 'phone').required(),
  purpose: Joi.string().valid('register', 'reset', 'login').required()
});

// 刷新令牌验证
const refreshTokenSchema = Joi.object({
  refreshToken: Joi.string().required()
});

// 用户资料更新验证
const updateProfileSchema = Joi.object({
  nickname: Joi.string().min(1).max(100).optional(),
  phone: Joi.string().pattern(/^1[3-9]\d{9}$/).optional().allow(''),
  avatar: Joi.string().uri().optional().allow('')
});

// 添加好友验证
const addFriendSchema = Joi.object({
  uid: Joi.string().length(11).pattern(/^\d+$/).required()
});

// 发送消息验证
const sendMessageSchema = Joi.object({
  conversationId: Joi.string().required(),
  content: Joi.string().required(),
  messageType: Joi.string().valid('text', 'image', 'file').default('text'),
  replyToId: Joi.number().integer().positive().optional()
});

// AI角色验证
const aiCharacterSchema = Joi.object({
  name: Joi.string().min(1).max(100).required(),
  description: Joi.string().max(500).optional(),
  personality: Joi.string().max(1000).optional(),
  systemPrompt: Joi.string().max(2000).required(),
  model: Joi.string().valid('gpt-3.5-turbo', 'gpt-4', 'gpt-4-turbo').default('gpt-3.5-turbo'),
  modelConfig: Joi.object({
    temperature: Joi.number().min(0).max(2).default(0.7),
    maxTokens: Joi.number().min(1).max(4000).default(1000),
    topP: Joi.number().min(0).max(1).default(1),
    frequencyPenalty: Joi.number().min(-2).max(2).default(0),
    presencePenalty: Joi.number().min(-2).max(2).default(0)
  }).default({})
});

module.exports = {
  registerSchema,
  loginSchema,
  resetPasswordSchema,
  sendCodeSchema,
  refreshTokenSchema,
  updateProfileSchema,
  addFriendSchema,
  sendMessageSchema,
  aiCharacterSchema
};