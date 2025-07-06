const db = require('../utils/database');
const logger = require('../utils/logger');

/**
 * 用户管理控制器 - 重新设计版本
 * 专门处理用户资料相关功能，好友功能已移至 friendController
 */

// 获取用户信息
async function getProfile(req, res) {
  try {
    const user = await db('users')
      .select('id', 'uid', 'username', 'email', 'nickname', 'phone', 'avatar_url', 'role', 'created_at', 'last_login_at')
      .where('id', req.user.userId)
      .first();

    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 格式化用户数据以匹配前端期望的结构
    const formattedUser = {
      userId: user.id.toString(),
      uid: user.uid,
      username: user.username,
      email: user.email,
      nickname: user.nickname,
      phone: user.phone,
      avatarUrl: user.avatar_url,
      role: user.role,
      joinedAt: new Date(user.created_at).getTime(),
      lastLoginAt: user.last_login_at ? new Date(user.last_login_at).getTime() : null,
      updatedAt: new Date().getTime()
    };

    logger.info('USER_PROFILE', '获取用户资料', {
      userId: user.id,
      username: user.username
    });

    res.json({
      success: true,
      data: formattedUser
    });
  } catch (error) {
    logger.error('USER_PROFILE', '获取用户信息失败', {
      userId: req.user ? req.user.userId : undefined,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '获取用户信息失败'
    });
  }
}

// 更新用户信息
async function updateProfile(req, res) {
  try {
    const { nickname, phone, avatar } = req.body;
    const userId = req.user.userId;

    const updateData = {
      updated_at: new Date()
    };

    if (nickname !== undefined) updateData.nickname = nickname || null;
    if (phone !== undefined) updateData.phone = phone || null;
    if (avatar !== undefined) updateData.avatar_url = avatar || null;

    await db('users')
      .where('id', userId)
      .update(updateData);

    // 获取更新后的用户信息
    const user = await db('users')
      .select('id', 'uid', 'username', 'email', 'nickname', 'phone', 'avatar_url', 'role', 'created_at', 'last_login_at')
      .where('id', userId)
      .first();

    // 格式化用户数据
    const formattedUser = {
      userId: user.id.toString(),
      uid: user.uid,
      username: user.username,
      email: user.email,
      nickname: user.nickname,
      phone: user.phone,
      avatarUrl: user.avatar_url,
      role: user.role,
      joinedAt: new Date(user.created_at).getTime(),
      lastLoginAt: user.last_login_at ? new Date(user.last_login_at).getTime() : null,
      updatedAt: new Date().getTime()
    };

    logger.info('USER_PROFILE', '更新用户资料', {
      userId: user.id,
      username: user.username,
      updateFields: Object.keys(updateData).filter(key => key !== 'updated_at')
    });

    res.json({
      success: true,
      message: '用户信息更新成功',
      data: formattedUser
    });
  } catch (error) {
    logger.error('USER_PROFILE', '更新用户信息失败', {
      userId: req.user ? req.user.userId : undefined,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '更新用户信息失败'
    });
  }
}

// 获取用户统计信息
async function getUserStats(req, res) {
  try {
    const userId = req.user.userId;

    // 并行查询各种统计数据
    const [
      friendsCount,
      pendingRequestsCount,
      conversationsCount
    ] = await Promise.all([
      // 好友数量
      db('friendships')
        .where(function() {
          this.where('requester_id', userId).orWhere('addressee_id', userId);
        })
        .where('status', 'accepted')
        .count('* as count')
        .first(),
      
      // 待处理好友请求数量
      db('friendships')
        .where('addressee_id', userId)
        .where('status', 'pending')
        .count('* as count')
        .first(),
      
      // 活跃对话数量
      db('conversation_participants')
        .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
        .where('conversation_participants.user_id', userId)
        .where('conversations.is_active', true)
        .count('* as count')
        .first()
    ]);

    const stats = {
      friendsCount: parseInt(friendsCount.count) || 0,
      pendingRequestsCount: parseInt(pendingRequestsCount.count) || 0,
      conversationsCount: parseInt(conversationsCount.count) || 0
    };

    logger.info('USER_STATS', '获取用户统计', {
      userId,
      stats
    });

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    logger.error('USER_STATS', '获取用户统计失败', {
      userId: req.user ? req.user.userId : undefined,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '获取用户统计失败'
    });
  }
}

// 搜索用户 (公开信息，不涉及好友关系)
async function searchUsers(req, res) {
  try {
    const { q, limit = 10 } = req.query;
    
    if (!q || q.trim().length < 2) {
      return res.status(400).json({
        success: false,
        message: '搜索关键词至少需要2个字符'
      });
    }

    const searchTerm = `%${q.trim()}%`;
    
    const users = await db('users')
      .select('id', 'uid', 'username', 'nickname', 'avatar_url', 'last_login_at')
      .where('is_active', true)
      .where(function() {
        this.where('username', 'ilike', searchTerm)
          .orWhere('nickname', 'ilike', searchTerm)
          .orWhere('uid', 'ilike', searchTerm);
      })
      .limit(parseInt(limit))
      .orderBy('last_login_at', 'desc');

    // 格式化搜索结果
    const formattedUsers = users.map(user => ({
      userId: user.id.toString(),
      uid: user.uid,
      username: user.username,
      nickname: user.nickname,
      avatarUrl: user.avatar_url,
      isOnline: user.last_login_at ? (new Date() - new Date(user.last_login_at)) < 300000 : false
    }));

    logger.info('USER_SEARCH', '搜索用户', {
      searchTerm: q,
      resultCount: formattedUsers.length,
      requestUserId: req.user ? req.user.userId : undefined
    });

    res.json({
      success: true,
      data: formattedUsers
    });
  } catch (error) {
    logger.error('USER_SEARCH', '搜索用户失败', {
      searchTerm: req.query.q,
      requestUserId: req.user ? req.user.userId : undefined,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '搜索用户失败'
    });
  }
}

// 更新用户在线状态
async function updateOnlineStatus(req, res) {
  try {
    const userId = req.user.userId;
    
    await db('users')
      .where('id', userId)
      .update({
        last_login_at: new Date()
      });

    logger.debug('USER_STATUS', '更新在线状态', { userId });

    res.json({
      success: true,
      message: '在线状态已更新'
    });
  } catch (error) {
    logger.error('USER_STATUS', '更新在线状态失败', {
      userId: req.user ? req.user.userId : undefined,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '更新在线状态失败'
    });
  }
}

module.exports = {
  getProfile,
  updateProfile,
  getUserStats,
  searchUsers,
  updateOnlineStatus
};