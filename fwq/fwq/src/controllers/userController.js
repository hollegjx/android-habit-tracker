const db = require('../utils/database');
const { generateUUID } = require('../utils/auth');

// 获取用户信息
async function getProfile(req, res) {
  try {
    const user = await db('users')
      .select('id', 'uid', 'username', 'email', 'nickname', 'phone', 'avatar_url', 'role', 'created_at')
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
      updatedAt: new Date().getTime()
    };

    res.json({
      success: true,
      data: formattedUser
    });
  } catch (error) {
    console.error('获取用户信息错误:', error);
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

    if (nickname) updateData.nickname = nickname;
    if (phone !== undefined) updateData.phone = phone || null;
    if (avatar !== undefined) updateData.avatar_url = avatar || null;

    await db('users')
      .where('id', userId)
      .update(updateData);

    // 获取更新后的用户信息
    const user = await db('users')
      .select('id', 'uid', 'username', 'email', 'nickname', 'phone', 'avatar_url', 'role')
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
      joinedAt: new Date(user.created_at || new Date()).getTime(),
      updatedAt: new Date().getTime()
    };

    res.json({
      success: true,
      message: '用户信息更新成功',
      data: formattedUser
    });
  } catch (error) {
    console.error('更新用户信息错误:', error);
    res.status(500).json({
      success: false,
      message: '更新用户信息失败'
    });
  }
}

// 通过UID搜索用户
async function searchUserByUID(req, res) {
  try {
    const { uid } = req.params;

    const user = await db('users')
      .select('id', 'uid', 'username', 'nickname', 'avatar_url')
      .where('uid', uid)
      .where('is_active', true)
      .first();

    if (!user) {
      return res.status(404).json({
        success: false,
        message: '用户不存在'
      });
    }

    // 检查是否已经是好友
    const friendship = await db('friendships')
      .where(function() {
        this.where('requester_id', req.user.userId).where('addressee_id', user.id);
      })
      .orWhere(function() {
        this.where('requester_id', user.id).where('addressee_id', req.user.userId);
      })
      .first();

    // 格式化用户数据
    const formattedUser = {
      userId: user.id.toString(),
      uid: user.uid,
      username: user.username,
      nickname: user.nickname,
      avatarUrl: user.avatar_url,
      friendshipStatus: friendship ? friendship.status : null
    };

    res.json({
      success: true,
      data: formattedUser
    });
  } catch (error) {
    console.error('搜索用户错误:', error);
    res.status(500).json({
      success: false,
      message: '搜索用户失败'
    });
  }
}

// 发送好友请求
async function sendFriendRequest(req, res) {
  try {
    const { uid } = req.body;
    const requesterId = req.user.userId;

    // 查找目标用户
    const targetUser = await db('users')
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
      return res.status(400).json({
        success: false,
        message: existingFriendship.status === 'accepted' ? '已经是好友' : '好友请求已存在'
      });
    }

    // 创建好友请求
    await db('friendships').insert({
      requester_id: requesterId,
      addressee_id: targetUser.id,
      status: 'pending',
      created_at: new Date()
    });

    res.json({
      success: true,
      message: '好友请求已发送'
    });
  } catch (error) {
    console.error('发送好友请求错误:', error);
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

    const requests = await db('friendships')
      .select(
        'friendships.id',
        'friendships.status',
        'friendships.created_at',
        'users.uid',
        'users.username',
        'users.nickname',
        'users.avatar_url'
      )
      .join('users', 'users.id', 'friendships.requester_id')
      .where('friendships.addressee_id', userId)
      .where('friendships.status', 'pending')
      .orderBy('friendships.created_at', 'desc');

    // 格式化好友请求数据
    const formattedRequests = requests.map(req => ({
      id: req.id.toString(),
      status: req.status,
      createdAt: new Date(req.created_at).getTime(),
      requester: {
        userId: req.uid,
        uid: req.uid,
        username: req.username,
        nickname: req.nickname,
        avatarUrl: req.avatar_url
      }
    }));

    res.json({
      success: true,
      data: formattedRequests
    });
  } catch (error) {
    console.error('获取好友请求错误:', error);
    res.status(500).json({
      success: false,
      message: '获取好友请求失败'
    });
  }
}

// 处理好友请求
async function handleFriendRequest(req, res) {
  try {
    const { requestId } = req.params;
    const { action } = req.body; // 'accept' or 'decline'
    const userId = req.user.userId;

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
        message: '好友请求不存在'
      });
    }

    // 更新状态
    const newStatus = action === 'accept' ? 'accepted' : 'declined';
    await db('friendships')
      .where('id', requestId)
      .update({
        status: newStatus,
        updated_at: new Date()
      });

    // 如果接受好友请求，创建私聊对话
    if (action === 'accept') {
      const conversationId = generateUUID();
      
      // 创建对话
      const [conversation] = await db('conversations')
        .insert({
          conversation_id: conversationId,
          type: 'private',
          created_by: userId,
          created_at: new Date()
        })
        .returning('*');

      // 添加对话参与者
      await db('conversation_participants').insert([
        {
          conversation_id: conversation.id,
          user_id: friendship.requester_id,
          role: 'member',
          joined_at: new Date()
        },
        {
          conversation_id: conversation.id,
          user_id: userId,
          role: 'member',
          joined_at: new Date()
        }
      ]);
    }

    res.json({
      success: true,
      message: action === 'accept' ? '已接受好友请求' : '已拒绝好友请求'
    });
  } catch (error) {
    console.error('处理好友请求错误:', error);
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
      .orderBy('users.last_login_at', 'desc');

    // 格式化好友列表数据
    const formattedFriends = friends.map(friend => ({
      userId: friend.id.toString(),
      uid: friend.uid,
      username: friend.username,
      nickname: friend.nickname,
      avatarUrl: friend.avatar_url,
      isOnline: friend.last_login_at ? (new Date() - new Date(friend.last_login_at)) < 300000 : false, // 5分钟内算在线
      lastSeenTime: friend.last_login_at ? new Date(friend.last_login_at).getTime() : null,
      friendSince: new Date(friend.friend_since).getTime()
    }));

    res.json({
      success: true,
      data: formattedFriends
    });
  } catch (error) {
    console.error('获取好友列表错误:', error);
    res.status(500).json({
      success: false,
      message: '获取好友列表失败'
    });
  }
}

module.exports = {
  getProfile,
  updateProfile,
  searchUserByUID,
  sendFriendRequest,
  getFriendRequests,
  handleFriendRequest,
  getFriends
};