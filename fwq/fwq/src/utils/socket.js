const { verifyAccessToken } = require('./auth');
const db = require('./database');

const connectedUsers = new Map(); // 存储在线用户

function initializeSocket(io) {
  // Socket.IO 认证中间件
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth.token;
      if (!token) {
        return next(new Error('认证失败'));
      }

      const decoded = verifyAccessToken(token);
      const user = await db('users').where('id', decoded.userId).first();
      
      if (!user || !user.is_active) {
        return next(new Error('用户不存在或已被禁用'));
      }

      socket.userId = user.id;
      socket.userInfo = {
        id: user.id,
        uid: user.uid,
        username: user.username,
        nickname: user.nickname,
        avatar_url: user.avatar_url
      };
      
      next();
    } catch (error) {
      next(new Error('认证失败'));
    }
  });

  io.on('connection', (socket) => {
    console.log(`用户 ${socket.userInfo.username} 已连接`);
    
    // 记录在线用户
    connectedUsers.set(socket.userId, {
      socketId: socket.id,
      userInfo: socket.userInfo,
      connectedAt: new Date()
    });

    // 加入用户专属房间
    socket.join(`user_${socket.userId}`);

    // 获取用户的对话房间并加入
    getUserConversations(socket.userId).then(conversations => {
      conversations.forEach(conv => {
        socket.join(`conversation_${conv.conversation_id}`);
      });
    });

    // 发送消息
    socket.on('send_message', async (data) => {
      try {
        const { conversationId, content, messageType = 'text', replyToId } = data;
        
        // 验证用户是否在对话中
        const participant = await db('conversation_participants')
          .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
          .where('conversations.conversation_id', conversationId)
          .where('conversation_participants.user_id', socket.userId)
          .first();

        if (!participant) {
          socket.emit('error', { message: '您不在此对话中' });
          return;
        }

        // 创建消息
        const messageId = require('./auth').generateUUID();
        const [message] = await db('messages')
          .insert({
            message_id: messageId,
            conversation_id: participant.conversation_id,
            sender_id: socket.userId,
            content,
            message_type: messageType,
            reply_to_id: replyToId || null,
            sent_at: new Date()
          })
          .returning('*');

        // 更新对话最后消息时间
        await db('conversations')
          .where('id', participant.conversation_id)
          .update({ last_message_at: new Date() });

        // 获取完整消息信息
        const fullMessage = await db('messages')
          .select('messages.*', 'users.username', 'users.nickname', 'users.avatar_url')
          .leftJoin('users', 'users.id', 'messages.sender_id')
          .where('messages.id', message.id)
          .first();

        // 广播消息到对话房间
        io.to(`conversation_${conversationId}`).emit('new_message', fullMessage);

      } catch (error) {
        console.error('发送消息错误:', error);
        socket.emit('error', { message: '发送消息失败' });
      }
    });

    // 加入对话
    socket.on('join_conversation', (conversationId) => {
      socket.join(`conversation_${conversationId}`);
    });

    // 离开对话
    socket.on('leave_conversation', (conversationId) => {
      socket.leave(`conversation_${conversationId}`);
    });

    // 标记消息已读
    socket.on('mark_as_read', async (data) => {
      try {
        const { conversationId } = data;
        
        await db('conversation_participants')
          .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
          .where('conversations.conversation_id', conversationId)
          .where('conversation_participants.user_id', socket.userId)
          .update({ last_read_at: new Date() });

        socket.emit('messages_read', { conversationId });
      } catch (error) {
        console.error('标记已读错误:', error);
      }
    });

    // 断开连接
    socket.on('disconnect', () => {
      console.log(`用户 ${socket.userInfo.username} 已断开连接`);
      connectedUsers.delete(socket.userId);
    });
  });

  // 返回实用函数
  return {
    getOnlineUsers: () => Array.from(connectedUsers.values()),
    sendToUser: (userId, event, data) => {
      const user = connectedUsers.get(userId);
      if (user) {
        io.to(user.socketId).emit(event, data);
      }
    },
    sendToConversation: (conversationId, event, data) => {
      io.to(`conversation_${conversationId}`).emit(event, data);
    }
  };
}

// 获取用户的对话列表
async function getUserConversations(userId) {
  return db('conversation_participants')
    .select('conversations.conversation_id')
    .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
    .where('conversation_participants.user_id', userId);
}

module.exports = {
  initializeSocket,
  connectedUsers
};