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
  return jwt.sign(payload, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '24h'
  });
}

/**
 * 生成JWT刷新令牌
 */
function generateRefreshToken(payload) {
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
 * 生成UUID
 */
function generateUUID() {
  return crypto.randomUUID();
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