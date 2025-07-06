const db = require('../utils/database');

// 优化后的系统统计信息 - 简化查询
async function getSystemStats(req, res) {
  const startTime = Date.now();
  console.log('🔍 开始获取系统统计...');
  
  try {
    // 简化查询 - 只获取基本统计
    const userCount = await db('users').count('* as count').first();
    const messageCount = await db('messages').count('* as count').first();
    
    const endTime = Date.now();
    console.log(`✅ 系统统计获取完成，耗时: ${endTime - startTime}ms`);
    
    res.json({
      success: true,
      data: {
        users: { total_users: userCount.count },
        messages: { total_messages: messageCount.count },
        performance: {
          query_time_ms: endTime - startTime
        }
      },
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    const endTime = Date.now();
    console.error(`❌ 系统统计获取失败，耗时: ${endTime - startTime}ms`, error);
    res.status(500).json({
      success: false,
      message: '获取系统统计失败',
      error: error.message,
      performance: {
        query_time_ms: endTime - startTime
      }
    });
  }
}

// 优化后的用户列表 - 分页和限制字段
async function getUsers(req, res) {
  const startTime = Date.now();
  console.log('🔍 开始获取用户列表...');
  
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10; // 限制每页10条
    const offset = (page - 1) * limit;

    // 只查询必要字段
    const users = await db('users')
      .select('user_id', 'username', 'nickname', 'email', 'role', 'is_active', 'created_at')
      .limit(limit)
      .offset(offset)
      .orderBy('created_at', 'desc');

    const totalCount = await db('users').count('* as count').first();

    const endTime = Date.now();
    console.log(`✅ 用户列表获取完成，耗时: ${endTime - startTime}ms`);

    res.json({
      success: true,
      data: {
        users,
        pagination: {
          page,
          limit,
          total: parseInt(totalCount.count),
          pages: Math.ceil(totalCount.count / limit)
        },
        performance: {
          query_time_ms: endTime - startTime
        }
      }
    });
  } catch (error) {
    const endTime = Date.now();
    console.error(`❌ 用户列表获取失败，耗时: ${endTime - startTime}ms`, error);
    res.status(500).json({
      success: false,
      message: '获取用户列表失败',
      error: error.message,
      performance: {
        query_time_ms: endTime - startTime
      }
    });
  }
}

// 优化后的用户登录活动 - 限制时间范围
async function getUserLoginActivities(req, res) {
  const startTime = Date.now();
  console.log('🔍 开始获取用户活动...');
  
  try {
    // 只查询最近7天的活动
    const activities = await db('refresh_tokens')
      .join('users', 'refresh_tokens.user_id', 'users.user_id')
      .select(
        'users.username',
        'users.nickname', 
        'refresh_tokens.created_at as login_time',
        'refresh_tokens.expires_at'
      )
      .where('refresh_tokens.created_at', '>=', db.raw("NOW() - INTERVAL '7 days'"))
      .orderBy('refresh_tokens.created_at', 'desc')
      .limit(50); // 限制50条记录

    const endTime = Date.now();
    console.log(`✅ 用户活动获取完成，耗时: ${endTime - startTime}ms`);

    res.json({
      success: true,
      data: activities,
      performance: {
        query_time_ms: endTime - startTime
      }
    });
  } catch (error) {
    const endTime = Date.now();
    console.error(`❌ 用户活动获取失败，耗时: ${endTime - startTime}ms`, error);
    res.status(500).json({
      success: false,
      message: '获取用户活动失败',
      error: error.message,
      performance: {
        query_time_ms: endTime - startTime
      }
    });
  }
}

// 快速健康检查
async function quickHealthCheck(req, res) {
  const startTime = Date.now();
  
  try {
    // 最简单的数据库连接测试
    await db.raw('SELECT 1');
    
    const endTime = Date.now();
    
    res.json({
      success: true,
      message: '数据库连接正常',
      performance: {
        db_ping_ms: endTime - startTime
      },
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    const endTime = Date.now();
    res.status(500).json({
      success: false,
      message: '数据库连接失败',
      error: error.message,
      performance: {
        db_ping_ms: endTime - startTime
      }
    });
  }
}

// 其他原有函数的简化版本...
const updateUserStatus = require('./adminController').updateUserStatus;
const createUser = require('./adminController').createUser;
const getSystemLogs = require('./adminController').getSystemLogs;
const getFriendRequests = require('./adminController').getFriendRequests;
const getRecentChatMessages = require('./adminController').getRecentChatMessages;
const getUserActivityLogs = require('./adminController').getUserActivityLogs;

module.exports = {
  getSystemStats,
  getUsers,
  getUserLoginActivities,
  quickHealthCheck,
  updateUserStatus,
  createUser,
  getSystemLogs,
  getFriendRequests,
  getRecentChatMessages,
  getUserActivityLogs
};