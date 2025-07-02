const { verifyAccessToken } = require('../utils/auth');
const db = require('../utils/database');

// 认证中间件
async function authenticate(req, res, next) {
  try {
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        message: '未提供认证令牌'
      });
    }

    const token = authHeader.substring(7);
    const decoded = verifyAccessToken(token);
    
    // 验证用户是否存在且活跃
    const user = await db('users')
      .where('id', decoded.userId)
      .first();
    
    if (!user || !user.is_active) {
      return res.status(401).json({
        success: false,
        message: '用户不存在或已被禁用'
      });
    }

    req.user = {
      userId: user.id,
      uid: user.uid,
      username: user.username,
      email: user.email,
      role: user.role
    };
    
    next();
  } catch (error) {
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: '令牌已过期',
        code: 'TOKEN_EXPIRED'
      });
    } else if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        success: false,
        message: '无效的令牌'
      });
    }
    
    res.status(401).json({
      success: false,
      message: '认证失败'
    });
  }
}

// 管理员权限检查
function requireAdmin(req, res, next) {
  if (req.user.role !== 'admin') {
    return res.status(403).json({
      success: false,
      message: '需要管理员权限'
    });
  }
  next();
}

// 可选认证中间件（允许匿名访问）
async function optionalAuth(req, res, next) {
  try {
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      req.user = null;
      return next();
    }

    const token = authHeader.substring(7);
    const decoded = verifyAccessToken(token);
    
    const user = await db('users')
      .where('id', decoded.userId)
      .first();
    
    if (user && user.is_active) {
      req.user = {
        userId: user.id,
        uid: user.uid,
        username: user.username,
        email: user.email,
        role: user.role
      };
    } else {
      req.user = null;
    }
    
    next();
  } catch (error) {
    req.user = null;
    next();
  }
}

module.exports = {
  authenticate,
  requireAdmin,
  optionalAuth
};