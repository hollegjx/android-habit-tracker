const db = require('../utils/database');

// 获取系统统计信息
async function getSystemStats(req, res) {
  try {
    // 用户统计
    const userStats = await db('users')
      .select(
        db.raw('COUNT(*) as total_users'),
        db.raw('COUNT(CASE WHEN is_active = true THEN 1 END) as active_users'),
        db.raw('COUNT(CASE WHEN role = \'admin\' THEN 1 END) as admin_users'),
        db.raw('COUNT(CASE WHEN created_at >= NOW() - INTERVAL \'7 days\' THEN 1 END) as new_users_week')
      )
      .first();

    // 消息统计
    const messageStats = await db('messages')
      .select(
        db.raw('COUNT(*) as total_messages'),
        db.raw('COUNT(CASE WHEN sent_at >= NOW() - INTERVAL \'1 day\' THEN 1 END) as messages_today'),
        db.raw('COUNT(CASE WHEN sent_at >= NOW() - INTERVAL \'7 days\' THEN 1 END) as messages_week')
      )
      .where('is_deleted', false)
      .first();

    // 对话统计
    const conversationStats = await db('conversations')
      .select(
        db.raw('COUNT(*) as total_conversations'),
        db.raw('COUNT(CASE WHEN type = \'private\' THEN 1 END) as private_conversations'),
        db.raw('COUNT(CASE WHEN type = \'group\' THEN 1 END) as group_conversations'),
        db.raw('COUNT(CASE WHEN type = \'ai\' THEN 1 END) as ai_conversations')
      )
      .first();

    // AI角色统计
    const aiCharacterStats = await db('ai_characters')
      .select(
        db.raw('COUNT(*) as total_characters'),
        db.raw('COUNT(CASE WHEN is_active = true THEN 1 END) as active_characters')
      )
      .first();

    res.json({
      success: true,
      data: {
        users: userStats,
        messages: messageStats,
        conversations: conversationStats,
        aiCharacters: aiCharacterStats
      }
    });
  } catch (error) {
    console.error('获取系统统计错误:', error);
    res.status(500).json({
      success: false,
      message: '获取系统统计失败'
    });
  }
}

// 获取用户列表
async function getUsers(req, res) {
  try {
    const { page = 1, limit = 20, search, role, status } = req.query;
    const offset = (page - 1) * limit;

    let query = db('users')
      .select('id', 'uid', 'username', 'email', 'nickname', 'role', 'is_active', 'email_verified', 'last_login_at', 'created_at');

    // 搜索过滤
    if (search) {
      query = query.where(function() {
        this.where('username', 'ilike', `%${search}%`)
          .orWhere('email', 'ilike', `%${search}%`)
          .orWhere('nickname', 'ilike', `%${search}%`)
          .orWhere('uid', 'ilike', `%${search}%`);
      });
    }

    // 角色过滤
    if (role) {
      query = query.where('role', role);
    }

    // 状态过滤
    if (status === 'active') {
      query = query.where('is_active', true);
    } else if (status === 'inactive') {
      query = query.where('is_active', false);
    }

    const users = await query
      .orderBy('created_at', 'desc')
      .limit(limit)
      .offset(offset);

    // 获取总数
    const totalQuery = db('users').count('* as count');
    if (search) {
      totalQuery.where(function() {
        this.where('username', 'ilike', `%${search}%`)
          .orWhere('email', 'ilike', `%${search}%`)
          .orWhere('nickname', 'ilike', `%${search}%`)
          .orWhere('uid', 'ilike', `%${search}%`);
      });
    }
    if (role) totalQuery.where('role', role);
    if (status === 'active') totalQuery.where('is_active', true);
    else if (status === 'inactive') totalQuery.where('is_active', false);

    const total = await totalQuery.first();

    res.json({
      success: true,
      data: {
        users,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: parseInt(total.count),
          totalPages: Math.ceil(total.count / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取用户列表错误:', error);
    res.status(500).json({
      success: false,
      message: '获取用户列表失败'
    });
  }
}

// 更新用户状态
async function updateUserStatus(req, res) {
  try {
    const { userId } = req.params;
    const { isActive, role } = req.body;

    const user = await db('users').where('id', userId).first();
    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 防止管理员禁用自己
    if (userId == req.user.userId && isActive === false) {
      return res.status(400).json({
        success: false,
        message: '不能禁用自己的账号'
      });
    }

    const updateData = { updated_at: new Date() };
    if (isActive !== undefined) updateData.is_active = isActive;
    if (role !== undefined) updateData.role = role;

    await db('users').where('id', userId).update(updateData);

    res.json({
      success: true,
      message: '用户状态更新成功'
    });
  } catch (error) {
    console.error('更新用户状态错误:', error);
    res.status(500).json({
      success: false,
      message: '更新用户状态失败'
    });
  }
}

// 获取系统日志（简化版）
async function getSystemLogs(req, res) {
  try {
    const { page = 1, limit = 50, type } = req.query;
    const offset = (page - 1) * limit;

    // 这里可以实现真正的日志系统，目前返回用户活动日志
    let query = db('users')
      .select(
        'username',
        'email',
        'last_login_at as timestamp',
        db.raw('\'user_login\' as type'),
        db.raw('\'用户登录\' as description')
      )
      .whereNotNull('last_login_at');

    if (type === 'login') {
      // 只显示登录日志
    }

    const logs = await query
      .orderBy('last_login_at', 'desc')
      .limit(limit)
      .offset(offset);

    res.json({
      success: true,
      data: logs
    });
  } catch (error) {
    console.error('获取系统日志错误:', error);
    res.status(500).json({
      success: false,
      message: '获取系统日志失败'
    });
  }
}

// 获取用户登录活动
async function getUserLoginActivities(req, res) {
  try {
    const { page = 1, limit = 20, days = 7 } = req.query;
    const offset = (page - 1) * limit;

    // 获取最近指定天数内的用户登录活动
    const activities = await db('users')
      .select(
        'id',
        'uid',
        'username',
        'nickname',
        'email',
        'last_login_at',
        'is_active',
        'role'
      )
      .whereNotNull('last_login_at')
      .where('last_login_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
      .orderBy('last_login_at', 'desc')
      .limit(limit)
      .offset(offset);

    // 获取总数
    const total = await db('users')
      .count('* as count')
      .whereNotNull('last_login_at')
      .where('last_login_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
      .first();

    res.json({
      success: true,
      data: {
        activities,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: parseInt(total.count),
          totalPages: Math.ceil(total.count / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取用户登录活动错误:', error);
    res.status(500).json({
      success: false,
      message: '获取用户登录活动失败'
    });
  }
}

// 获取好友请求数据
async function getFriendRequests(req, res) {
  try {
    const { page = 1, limit = 20, status = 'all' } = req.query;
    const offset = (page - 1) * limit;

    let query = db('friendships as f')
      .select(
        'f.id',
        'f.status',
        'f.created_at',
        'f.updated_at',
        'requester.uid as requester_uid',
        'requester.username as requester_username',
        'requester.nickname as requester_nickname',
        'addressee.uid as addressee_uid',
        'addressee.username as addressee_username',
        'addressee.nickname as addressee_nickname'
      )
      .leftJoin('users as requester', 'f.requester_id', 'requester.id')
      .leftJoin('users as addressee', 'f.addressee_id', 'addressee.id');

    // 根据状态过滤
    if (status !== 'all') {
      query = query.where('f.status', status);
    }

    const friendRequests = await query
      .orderBy('f.created_at', 'desc')
      .limit(limit)
      .offset(offset);

    // 获取总数
    let totalQuery = db('friendships').count('* as count');
    if (status !== 'all') {
      totalQuery = totalQuery.where('status', status);
    }
    const total = await totalQuery.first();

    // 获取各状态统计
    const statusStats = await db('friendships')
      .select('status')
      .count('* as count')
      .groupBy('status');

    const stats = {
      pending: 0,
      accepted: 0,
      declined: 0,
      blocked: 0
    };
    statusStats.forEach(stat => {
      stats[stat.status] = parseInt(stat.count);
    });

    res.json({
      success: true,
      data: {
        friendRequests,
        stats,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: parseInt(total.count),
          totalPages: Math.ceil(total.count / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取好友请求数据错误:', error);
    res.status(500).json({
      success: false,
      message: '获取好友请求数据失败'
    });
  }
}

// 获取最近聊天消息
async function getRecentChatMessages(req, res) {
  try {
    const { page = 1, limit = 50, days = 7, conversationType = 'all' } = req.query;
    const offset = (page - 1) * limit;

    let query = db('messages as m')
      .select(
        'm.id',
        'm.message_id',
        'm.message_type',
        'm.content',
        'm.sent_at',
        'm.is_deleted',
        'sender.uid as sender_uid',
        'sender.username as sender_username',
        'sender.nickname as sender_nickname',
        'c.conversation_id',
        'c.type as conversation_type',
        'c.name as conversation_name'
      )
      .leftJoin('users as sender', 'm.sender_id', 'sender.id')
      .leftJoin('conversations as c', 'm.conversation_id', 'c.id')
      .where('m.sent_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
      .where('m.is_deleted', false);

    // 根据对话类型过滤
    if (conversationType !== 'all') {
      query = query.where('c.type', conversationType);
    }

    const messages = await query
      .orderBy('m.sent_at', 'desc')
      .limit(limit)
      .offset(offset);

    // 获取总数
    let totalQuery = db('messages as m')
      .leftJoin('conversations as c', 'm.conversation_id', 'c.id')
      .count('* as count')
      .where('m.sent_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
      .where('m.is_deleted', false);
    
    if (conversationType !== 'all') {
      totalQuery = totalQuery.where('c.type', conversationType);
    }
    const total = await totalQuery.first();

    // 获取消息类型统计
    const typeStats = await db('messages as m')
      .leftJoin('conversations as c', 'm.conversation_id', 'c.id')
      .select('c.type as conversation_type')
      .count('* as count')
      .where('m.sent_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
      .where('m.is_deleted', false)
      .groupBy('c.type');

    const stats = {
      private: 0,
      group: 0,
      ai: 0
    };
    typeStats.forEach(stat => {
      if (stat.conversation_type) {
        stats[stat.conversation_type] = parseInt(stat.count);
      }
    });

    res.json({
      success: true,
      data: {
        messages,
        stats,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: parseInt(total.count),
          totalPages: Math.ceil(total.count / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取最近聊天消息错误:', error);
    res.status(500).json({
      success: false,
      message: '获取最近聊天消息失败'
    });
  }
}

// 获取用户活动日志
async function getUserActivityLogs(req, res) {
  try {
    const { page = 1, limit = 50, userId, activityType = 'all', days = 30 } = req.query;
    const offset = (page - 1) * limit;

    // 构建活动日志查询，包含多种活动类型
    const activities = [];

    // 用户登录活动
    if (activityType === 'all' || activityType === 'login') {
      const loginActivities = await db('users')
        .select(
          'id as user_id',
          'uid',
          'username',
          'nickname',
          'last_login_at as activity_time',
          db.raw("'login' as activity_type"),
          db.raw("'用户登录' as activity_description"),
          db.raw("json_build_object('ip', null, 'user_agent', null) as activity_data")
        )
        .whereNotNull('last_login_at')
        .where('last_login_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
        .modify(queryBuilder => {
          if (userId) {
            queryBuilder.where('id', userId);
          }
        });
      
      activities.push(...loginActivities);
    }

    // 好友请求活动
    if (activityType === 'all' || activityType === 'friendship') {
      const friendshipActivities = await db('friendships as f')
        .select(
          'requester.id as user_id',
          'requester.uid',
          'requester.username',
          'requester.nickname',
          'f.created_at as activity_time',
          db.raw("'friendship' as activity_type"),
          db.raw("CASE WHEN f.status = 'pending' THEN '发送好友请求' WHEN f.status = 'accepted' THEN '接受好友请求' WHEN f.status = 'declined' THEN '拒绝好友请求' ELSE '好友关系变更' END as activity_description"),
          db.raw("json_build_object('status', f.status, 'addressee_username', addressee.username) as activity_data")
        )
        .leftJoin('users as requester', 'f.requester_id', 'requester.id')
        .leftJoin('users as addressee', 'f.addressee_id', 'addressee.id')
        .where('f.created_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
        .modify(queryBuilder => {
          if (userId) {
            queryBuilder.where('requester.id', userId);
          }
        });
      
      activities.push(...friendshipActivities);
    }

    // 消息发送活动
    if (activityType === 'all' || activityType === 'message') {
      const messageActivities = await db('messages as m')
        .select(
          'sender.id as user_id',
          'sender.uid',
          'sender.username',
          'sender.nickname',
          'm.sent_at as activity_time',
          db.raw("'message' as activity_type"),
          db.raw("'发送消息' as activity_description"),
          db.raw("json_build_object('message_type', m.message_type, 'conversation_type', c.type, 'conversation_name', c.name) as activity_data")
        )
        .leftJoin('users as sender', 'm.sender_id', 'sender.id')
        .leftJoin('conversations as c', 'm.conversation_id', 'c.id')
        .where('m.sent_at', '>=', db.raw(`NOW() - INTERVAL '${parseInt(days)} days'`))
        .where('m.is_deleted', false)
        .whereNotNull('m.sender_id')
        .modify(queryBuilder => {
          if (userId) {
            queryBuilder.where('sender.id', userId);
          }
        });
      
      activities.push(...messageActivities);
    }

    // 合并并排序所有活动
    const sortedActivities = activities
      .sort((a, b) => new Date(b.activity_time) - new Date(a.activity_time))
      .slice(offset, offset + parseInt(limit));

    // 计算总数
    const totalCount = activities.length;

    // 获取活动类型统计
    const activityStats = activities.reduce((stats, activity) => {
      stats[activity.activity_type] = (stats[activity.activity_type] || 0) + 1;
      return stats;
    }, {});

    res.json({
      success: true,
      data: {
        activities: sortedActivities,
        stats: activityStats,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: totalCount,
          totalPages: Math.ceil(totalCount / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取用户活动日志错误:', error);
    res.status(500).json({
      success: false,
      message: '获取用户活动日志失败'
    });
  }
}

module.exports = {
  getSystemStats,
  getUsers,
  updateUserStatus,
  getSystemLogs,
  getUserLoginActivities,
  getFriendRequests,
  getRecentChatMessages,
  getUserActivityLogs
};