const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const crypto = require('crypto');

/**
 * 生成11位数字UID
 */
function generateUID() {
  return Math.floor(10000000000 + Math.random() * 90000000000).toString();
}

/**
 * 哈希密码
 */
async function hashPassword(password) {
  return bcrypt.hash(password, 12);
}

/**
 * 验证密码
 */
async function verifyPassword(password, hash) {
  return bcrypt.compare(password, hash);
}

/**
 * 生成JWT访问令牌
 */
function generateAccessToken(payload) {
  // 确保payload是对象，JWT_SECRET存在
  if (!payload || typeof payload !== 'object') {
    throw new Error('Payload must be a plain object');
  }
  if (!process.env.JWT_SECRET) {
    throw new Error('JWT_SECRET is not defined');
  }
  return jwt.sign(payload, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '24h'
  });
}

/**
 * 生成JWT刷新令牌
 */
function generateRefreshToken(payload) {
  // 确保payload是对象，JWT_REFRESH_SECRET存在
  if (!payload || typeof payload !== 'object') {
    throw new Error('Payload must be a plain object');
  }
  if (!process.env.JWT_REFRESH_SECRET) {
    throw new Error('JWT_REFRESH_SECRET is not defined');
  }
  return jwt.sign(payload, process.env.JWT_REFRESH_SECRET, {
    expiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d'
  });
}

/**
 * 验证访问令牌
 */
function verifyAccessToken(token) {
  return jwt.verify(token, process.env.JWT_SECRET);
}

/**
 * 验证刷新令牌
 */
function verifyRefreshToken(token) {
  return jwt.verify(token, process.env.JWT_REFRESH_SECRET);
}

/**
 * 生成随机验证码
 */
function generateVerificationCode(length = 6) {
  return Math.floor(Math.pow(10, length - 1) + Math.random() * (Math.pow(10, length) - Math.pow(10, length - 1))).toString();
}

/**
 * 生成UUID (兼容Node.js v10)
 */
function generateUUID() {
  // 为Node.js v10兼容性，使用手动生成UUID
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

module.exports = {
  generateUID,
  hashPassword,
  verifyPassword,
  generateAccessToken,
  generateRefreshToken,
  verifyAccessToken,
  verifyRefreshToken,
  generateVerificationCode,
  generateUUID
};