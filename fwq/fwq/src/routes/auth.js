const express = require('express');
const { validate } = require('../middleware/validation');
const { authenticate } = require('../middleware/auth');
const {
  registerSchema,
  loginSchema,
  resetPasswordSchema,
  sendCodeSchema,
  refreshTokenSchema
} = require('../utils/validation');
const {
  register,
  login,
  refreshToken,
  logout,
  sendVerificationCode,
  resetPassword
} = require('../controllers/authController');

const router = express.Router();

// 用户注册
router.post('/register', validate(registerSchema), register);

// 用户登录
router.post('/login', validate(loginSchema), login);

// 刷新令牌
router.post('/refresh', validate(refreshTokenSchema), refreshToken);

// 用户登出
router.post('/logout', authenticate, logout);

// 发送验证码
router.post('/send-code', validate(sendCodeSchema), sendVerificationCode);

// 重置密码
router.post('/reset-password', validate(resetPasswordSchema), resetPassword);

module.exports = router;