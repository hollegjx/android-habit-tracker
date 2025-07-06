const db = require('../utils/database');
const { generateUUID } = require('../utils/auth');
const logger = require('../utils/logger');

/**
 * 好友管理控制器 - 完全重新设计版本
 * 支持增强的好友系统功能
 */

// 通过UID搜索用户
async function searchUserByUID(req, res) {
  const startTime = Date.now();
  const { uid } = req.params;
  const requestUser = req.user;
  
  logger.info('FRIEND_SEARCH', `搜索用户开始`, {
    searchedUID: uid,
    requestUserId: requestUser ? requestUser.userId : undefined,
    requestUsername: requestUser ? requestUser.username : undefined,
    requestIP: req.ip,
    userAgent: req.get('User-Agent')
  });

  try {
    // 参数验证
    if (!uid || uid.trim() === '') {
      logger.warn('FRIEND_SEARCH', 'UID参数为空', { requestUserId: requestUser ? requestUser.userId : undefined });
      return res.status(400).json({
        success: false,
        message: 'UID不能为空'
      });
    }

    // 查询目标用户
    const targetUser = await db('users')
      .select('id', 'uid', 'username', 'nickname', 'avatar_url', 'is_active', 'created_at', 'last_login_at')
      .where('uid', uid)
      .first();

    logger.info('FRIEND_SEARCH', '用户查询结果', {
      searchedUID: uid,
      found: !!targetUser,
      userData: targetUser ? {
        id: targetUser.id,
        uid: targetUser.uid,
        username: targetUser.username,
        is_active: targetUser.is_active
      } : null
    });

    if (!targetUser) {
      logger.warn('FRIEND_SEARCH', '用户不存在', { searchedUID: uid });
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    if (!targetUser.is_active) {
      logger.warn('FRIEND_SEARCH', '用户已被禁用', { 
        searchedUID: uid,
        userId: targetUser.id 
      });
      return res.status(404).json({
        success: false,
        message: '该用户暂时不可用'
      });
    }

    // 不能搜索自己
    if (targetUser.id === requestUser.userId) {
      return res.status(400).json({
        success: false,
        message: '不能添加自己为好友'
      });
    }

    // 检查好友关系状态
    const friendship = await db('friendships')
      .where(function() {
        this.where('requester_id', requestUser.userId).where('addressee_id', targetUser.id);
      })
      .orWhere(function() {
        this.where('requester_id', targetUser.id).where('addressee_id', requestUser.userId);
      })
      .first();

    logger.debug('FRIEND_SEARCH', '好友关系检查', {
      friendship: friendship ? {
        id: friendship.id,
        status: friendship.status,
        requester_id: friendship.requester_id,
        addressee_id: friendship.addressee_id
      } : null
    });

    // 格式化返回数据
    const formattedUser = {
      userId: targetUser.id.toString(),
      uid: targetUser.uid,
      username: targetUser.username,
      nickname: targetUser.nickname,
      avatarUrl: targetUser.avatar_url,
      isOnline: targetUser.last_login_at ? (new Date() - new Date(targetUser.last_login_at)) < 300000 : false,
      friendshipStatus: friendship ? friendship.status : null,
      friendshipId: friendship ? friendship.id.toString() : null,
      canSendRequest: !friendship || ['declined', 'blocked'].includes(friendship.status)
    };

    const duration = Date.now() - startTime;
    logger.info('FRIEND_SEARCH', '搜索成功', {
      searchedUID: uid,
      foundUser: {
        userId: targetUser.id,
        username: targetUser.username
      },
      friendshipStatus: friendship ? friendship.status : 'none',
      duration: `${duration}ms`
    });

    res.json({
      success: true,
      data: formattedUser
    });
  } catch (error) {
    const duration = Date.now() - startTime;
    logger.error('FRIEND_SEARCH', '搜索失败', {
      searchedUID: uid,
      requestUserId: requestUser ? requestUser.userId : undefined,
      error: {
        message: error.message,
        stack: error.stack
      },
      duration: `${duration}ms`
    });

    res.status(500).json({
      success: false,
      message: '搜索用户失败'
    });
  }
}

// 发送好友请求
async function sendFriendRequest(req, res) {
  const startTime = Date.now();
  const { uid, message } = req.body;
  const requesterId = req.user.userId;
  
  logger.info('FRIEND_REQUEST', '发送好友请求开始', {
    targetUID: uid,
    requesterId,
    message: message ? message.substring(0, 50) : null
  });

  try {
    // 参数验证
    if (!uid) {
      return res.status(400).json({
        success: false,
        message: 'UID不能为空'
      });
    }

    // 查找目标用户
    const targetUser = await db('users')
      .select('id', 'uid', 'username', 'is_active')
      .where('uid', uid)
      .where('is_active', true)
      .first();

    if (!targetUser) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    if (targetUser.id === requesterId) {
      return res.status(400).json({
        success: false,
        message: '不能添加自己为好友'
      });
    }

    // 检查是否已经存在好友关系
    const existingFriendship = await db('friendships')
      .where(function() {
        this.where('requester_id', requesterId).where('addressee_id', targetUser.id);
      })
      .orWhere(function() {
        this.where('requester_id', targetUser.id).where('addressee_id', requesterId);
      })
      .first();

    if (existingFriendship) {
      const statusMessages = {
        pending: '好友请求已存在，请等待对方回应',
        accepted: '你们已经是好友了',
        declined: '好友请求已被拒绝',
        blocked: '无法发送好友请求'
      };
      
      return res.status(400).json({
        success: false,
        message: statusMessages[existingFriendship.status] || '好友关系已存在'
      });
    }

    // 开始事务创建好友请求
    await db.transaction(async (trx) => {
      // 创建好友关系记录
      const [friendship] = await trx('friendships')
        .insert({
          requester_id: requesterId,
          addressee_id: targetUser.id,
          status: 'pending',
          requester_message: message || null,
          created_at: new Date(),
          updated_at: new Date()
        })
        .returning('*');

      // 创建好友请求通知
      await trx('friend_notifications')
        .insert({
          friendship_id: friendship.id,
          user_id: targetUser.id,
          type: 'request',
          message: message || `${req.user.username} 想要添加你为好友`,
          is_read: false,
          created_at: new Date(),
          updated_at: new Date()
        });

      logger.info('FRIEND_REQUEST', '好友请求创建成功', {
        friendshipId: friendship.id,
        requesterId,
        targetUserId: targetUser.id,
        message: message || null
      });
    });

    const duration = Date.now() - startTime;
    logger.info('FRIEND_REQUEST', '好友请求发送成功', {
      targetUID: uid,
      requesterId,
      duration: `${duration}ms`
    });

    res.json({
      success: true,
      message: '好友请求已发送'
    });
  } catch (error) {
    const duration = Date.now() - startTime;
    logger.error('FRIEND_REQUEST', '发送好友请求失败', {
      targetUID: uid,
      requesterId,
      error: {
        message: error.message,
        stack: error.stack
      },
      duration: `${duration}ms`
    });

    res.status(500).json({
      success: false,
      message: '发送好友请求失败'
    });
  }
}

// 获取好友请求列表
async function getFriendRequests(req, res) {
  try {
    const userId = req.user.userId;
    const { type = 'received' } = req.query; // received 或 sent

    let query = db('friendships')
      .select(
        'friendships.id',
        'friendships.status',
        'friendships.requester_message',
        'friendships.created_at',
        'users.id as user_id',
        'users.uid',
        'users.username',
        'users.nickname',
        'users.avatar_url',
        'users.last_login_at'
      );

    if (type === 'received') {
      // 收到的好友请求
      query = query
        .join('users', 'users.id', 'friendships.requester_id')
        .where('friendships.addressee_id', userId);
    } else {
      // 发送的好友请求
      query = query
        .join('users', 'users.id', 'friendships.addressee_id')
        .where('friendships.requester_id', userId);
    }

    const requests = await query
      .where('friendships.status', 'pending')
      .orderBy('friendships.created_at', 'desc');

    // 格式化好友请求数据
    const formattedRequests = requests.map(req => ({
      id: req.id.toString(),
      status: req.status,
      message: req.requester_message,
      createdAt: new Date(req.created_at).getTime(),
      user: {
        userId: req.user_id.toString(),
        uid: req.uid,
        username: req.username,
        nickname: req.nickname,
        avatarUrl: req.avatar_url,
        isOnline: req.last_login_at ? (new Date() - new Date(req.last_login_at)) < 300000 : false
      }
    }));

    logger.info('FRIEND_REQUESTS', '获取好友请求列表', {
      userId,
      type,
      count: formattedRequests.length
    });

    res.json({
      success: true,
      data: formattedRequests
    });
  } catch (error) {
    logger.error('FRIEND_REQUESTS', '获取好友请求失败', {
      userId: req.user.userId,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '获取好友请求失败'
    });
  }
}

// 处理好友请求 (接受/拒绝)
async function handleFriendRequest(req, res) {
  const startTime = Date.now();
  const { requestId } = req.params;
  const { action, message } = req.body; // 'accept' or 'decline'
  const userId = req.user.userId;

  logger.info('FRIEND_HANDLE', '处理好友请求开始', {
    requestId,
    action,
    userId,
    replyMessage: message ? message.substring(0, 50) : null
  });

  try {
    if (!['accept', 'decline'].includes(action)) {
      return res.status(400).json({
        success: false,
        message: '无效的操作'
      });
    }

    // 验证好友请求
    const friendship = await db('friendships')
      .where('id', requestId)
      .where('addressee_id', userId)
      .where('status', 'pending')
      .first();

    if (!friendship) {
      return res.status(404).json({
        success: false,
        message: '好友请求不存在或已处理'
      });
    }

    // 开始事务处理请求
    await db.transaction(async (trx) => {
      const newStatus = action === 'accept' ? 'accepted' : 'declined';
      
      // 更新好友关系状态
      await trx('friendships')
        .where('id', requestId)
        .update({
          status: newStatus,
          reject_reason: action === 'decline' ? message : null,
          updated_at: new Date()
        });

      // 创建回复通知
      await trx('friend_notifications')
        .insert({
          friendship_id: friendship.id,
          user_id: friendship.requester_id,
          type: newStatus,
          message: message || (action === 'accept' ? 
            `${req.user.username} 接受了你的好友请求` : 
            `${req.user.username} 拒绝了你的好友请求`),
          is_read: false,
          created_at: new Date(),
          updated_at: new Date()
        });

      // 如果接受好友请求，创建私聊对话
      if (action === 'accept') {
        const conversationId = generateUUID();
        
        // 创建对话
        const [conversation] = await trx('conversations')
          .insert({
            conversation_id: conversationId,
            type: 'private',
            created_by: userId,
            is_active: true,
            created_at: new Date(),
            updated_at: new Date()
          })
          .returning('*');

        // 添加对话参与者
        await trx('conversation_participants').insert([
          {
            conversation_id: conversation.id,
            user_id: friendship.requester_id,
            role: 'member',
            joined_at: new Date(),
            created_at: new Date(),
            updated_at: new Date()
          },
          {
            conversation_id: conversation.id,
            user_id: userId,
            role: 'member',
            joined_at: new Date(),
            created_at: new Date(),
            updated_at: new Date()
          }
        ]);

        // 更新好友关系的对话ID
        await trx('friendships')
          .where('id', requestId)
          .update({
            conversation_id: conversationId
          });

        logger.info('FRIEND_HANDLE', '创建私聊对话', {
          requestId,
          conversationId,
          conversation_id: conversation.id
        });
      }
    });

    const duration = Date.now() - startTime;
    logger.info('FRIEND_HANDLE', '好友请求处理成功', {
      requestId,
      action,
      userId,
      duration: `${duration}ms`
    });

    res.json({
      success: true,
      message: action === 'accept' ? '已接受好友请求' : '已拒绝好友请求'
    });
  } catch (error) {
    const duration = Date.now() - startTime;
    logger.error('FRIEND_HANDLE', '处理好友请求失败', {
      requestId,
      action,
      userId,
      error: {
        message: error.message,
        stack: error.stack
      },
      duration: `${duration}ms`
    });

    res.status(500).json({
      success: false,
      message: '处理好友请求失败'
    });
  }
}

// 获取好友列表
async function getFriends(req, res) {
  try {
    const userId = req.user.userId;

    const friends = await db('friendships')
      .select(
        'users.id',
        'users.uid',
        'users.username',
        'users.nickname',
        'users.avatar_url',
        'users.last_login_at',
        'friendships.id as friendship_id',
        'friendships.conversation_id',
        'friendships.friendship_alias',
        'friendships.is_starred',
        'friendships.is_muted',
        'friendships.unread_count',
        'friendships.last_message_at',
        'friendships.created_at as friend_since'
      )
      .join('users', function() {
        this.on(function() {
          this.on('users.id', '=', 'friendships.requester_id')
            .andOn('friendships.addressee_id', '=', userId);
        }).orOn(function() {
          this.on('users.id', '=', 'friendships.addressee_id')
            .andOn('friendships.requester_id', '=', userId);
        });
      })
      .where('friendships.status', 'accepted')
      .where('users.is_active', true)
      .orderBy([
        { column: 'friendships.is_starred', order: 'desc' },
        { column: 'friendships.last_message_at', order: 'desc' },
        { column: 'users.last_login_at', order: 'desc' }
      ]);

    // 格式化好友列表数据
    const formattedFriends = friends.map(friend => ({
      userId: friend.id.toString(),
      uid: friend.uid,
      username: friend.username,
      nickname: friend.nickname,
      displayName: friend.friendship_alias || friend.nickname || friend.username,
      avatarUrl: friend.avatar_url,
      isOnline: friend.last_login_at ? (new Date() - new Date(friend.last_login_at)) < 300000 : false,
      lastSeenTime: friend.last_login_at ? new Date(friend.last_login_at).getTime() : null,
      friendSince: new Date(friend.friend_since).getTime(),
      friendshipId: friend.friendship_id.toString(),
      conversationId: friend.conversation_id,
      isStarred: friend.is_starred,
      isMuted: friend.is_muted,
      unreadCount: friend.unread_count || 0,
      lastMessageAt: friend.last_message_at ? new Date(friend.last_message_at).getTime() : null
    }));

    logger.info('FRIEND_LIST', '获取好友列表', {
      userId,
      friendCount: formattedFriends.length
    });

    res.json({
      success: true,
      data: formattedFriends
    });
  } catch (error) {
    logger.error('FRIEND_LIST', '获取好友列表失败', {
      userId: req.user.userId,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '获取好友列表失败'
    });
  }
}

// 删除好友
async function removeFriend(req, res) {
  try {
    const { friendId } = req.params;
    const userId = req.user.userId;

    // 查找好友关系
    const friendship = await db('friendships')
      .where(function() {
        this.where('requester_id', userId).where('addressee_id', friendId);
      })
      .orWhere(function() {
        this.where('requester_id', friendId).where('addressee_id', userId);
      })
      .where('status', 'accepted')
      .first();

    if (!friendship) {
      return res.status(404).json({
        success: false,
        message: '好友关系不存在'
      });
    }

    // 开始事务删除好友关系
    await db.transaction(async (trx) => {
      // 删除好友关系
      await trx('friendships')
        .where('id', friendship.id)
        .del();

      // 删除相关通知
      await trx('friend_notifications')
        .where('friendship_id', friendship.id)
        .del();

      // 如果有对话，标记为非活跃但保留聊天记录
      if (friendship.conversation_id) {
        await trx('conversations')
          .where('conversation_id', friendship.conversation_id)
          .update({
            is_active: false,
            updated_at: new Date()
          });
      }
    });

    logger.info('FRIEND_REMOVE', '删除好友成功', {
      userId,
      friendId,
      friendshipId: friendship.id
    });

    res.json({
      success: true,
      message: '已删除好友'
    });
  } catch (error) {
    logger.error('FRIEND_REMOVE', '删除好友失败', {
      userId: req.user.userId,
      friendId: req.params.friendId,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '删除好友失败'
    });
  }
}

// 更新好友设置 (备注、星标、静音等)
async function updateFriendSettings(req, res) {
  try {
    const { friendId } = req.params;
    const { alias, isStarred, isMuted } = req.body;
    const userId = req.user.userId;

    // 查找好友关系
    const friendship = await db('friendships')
      .where(function() {
        this.where('requester_id', userId).where('addressee_id', friendId);
      })
      .orWhere(function() {
        this.where('requester_id', friendId).where('addressee_id', userId);
      })
      .where('status', 'accepted')
      .first();

    if (!friendship) {
      return res.status(404).json({
        success: false,
        message: '好友关系不存在'
      });
    }

    // 构建更新数据
    const updateData = {
      updated_at: new Date()
    };

    if (alias !== undefined) updateData.friendship_alias = alias || null;
    if (isStarred !== undefined) updateData.is_starred = isStarred;
    if (isMuted !== undefined) updateData.is_muted = isMuted;

    // 更新好友设置
    await db('friendships')
      .where('id', friendship.id)
      .update(updateData);

    logger.info('FRIEND_SETTINGS', '更新好友设置', {
      userId,
      friendId,
      friendshipId: friendship.id,
      settings: { alias, isStarred, isMuted }
    });

    res.json({
      success: true,
      message: '好友设置已更新'
    });
  } catch (error) {
    logger.error('FRIEND_SETTINGS', '更新好友设置失败', {
      userId: req.user.userId,
      friendId: req.params.friendId,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '更新好友设置失败'
    });
  }
}

// 获取好友通知列表
async function getFriendNotifications(req, res) {
  try {
    const userId = req.user.userId;
    const { limit = 20, offset = 0 } = req.query;

    const notifications = await db('friend_notifications')
      .select(
        'friend_notifications.*',
        'users.uid',
        'users.username',
        'users.nickname',
        'users.avatar_url'
      )
      .join('friendships', 'friendships.id', 'friend_notifications.friendship_id')
      .join('users', function() {
        this.on('users.id', '=', 'friendships.requester_id')
          .orOn('users.id', '=', 'friendships.addressee_id');
      })
      .where('friend_notifications.user_id', userId)
      .where('users.id', '!=', userId)
      .orderBy('friend_notifications.created_at', 'desc')
      .limit(limit)
      .offset(offset);

    // 格式化通知数据
    const formattedNotifications = notifications.map(notif => ({
      id: notif.id.toString(),
      type: notif.type,
      message: notif.message,
      isRead: notif.is_read,
      readAt: notif.read_at ? new Date(notif.read_at).getTime() : null,
      createdAt: new Date(notif.created_at).getTime(),
      fromUser: {
        uid: notif.uid,
        username: notif.username,
        nickname: notif.nickname,
        avatarUrl: notif.avatar_url
      }
    }));

    res.json({
      success: true,
      data: formattedNotifications
    });
  } catch (error) {
    logger.error('FRIEND_NOTIFICATIONS', '获取通知失败', {
      userId: req.user.userId,
      error: {
        message: error.message,
        stack: error.stack
      }
    });

    res.status(500).json({
      success: false,
      message: '获取通知失败'
    });
  }
}

module.exports = {
  searchUserByUID,
  sendFriendRequest,
  getFriendRequests,
  handleFriendRequest,
  getFriends,
  removeFriend,
  updateFriendSettings,
  getFriendNotifications
};