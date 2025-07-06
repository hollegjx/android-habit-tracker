const db = require('../utils/database');
const { generateUUID } = require('../utils/auth');
const axios = require('axios');

// 获取对话列表
async function getConversations(req, res) {
  try {
    const userId = req.user.userId;

    const conversations = await db('conversation_participants')
      .select(
        'conversations.conversation_id',
        'conversations.type',
        'conversations.name',
        'conversations.last_message_at',
        'last_messages.content as last_message',
        'last_messages.message_type as last_message_type',
        'senders.nickname as last_sender_name'
      )
      .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
      .leftJoin('messages as last_messages', function() {
        this.on('last_messages.conversation_id', '=', 'conversations.id')
          .andOn('last_messages.sent_at', '=', db.raw('conversations.last_message_at'));
      })
      .leftJoin('users as senders', 'senders.id', 'last_messages.sender_id')
      .where('conversation_participants.user_id', userId)
      .orderBy('conversations.last_message_at', 'desc');

    // 获取每个对话的参与者信息
    for (let conv of conversations) {
      if (conv.type === 'private') {
        // 私聊：获取对方用户信息
        const otherUser = await db('conversation_participants')
          .select('users.uid', 'users.username', 'users.nickname', 'users.avatar_url')
          .join('users', 'users.id', 'conversation_participants.user_id')
          .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
          .where('conversations.conversation_id', conv.conversation_id)
          .where('conversation_participants.user_id', '!=', userId)
          .first();
        
        conv.otherUser = otherUser;
        conv.displayName = otherUser ? otherUser.nickname || otherUser.username : '未知用户';
        conv.avatar = otherUser ? otherUser.avatar_url : null;
      } else {
        // 群聊或AI聊天
        conv.displayName = conv.name || '群聊';
      }

      // 计算未读消息数
      const participant = await db('conversation_participants')
        .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
        .where('conversations.conversation_id', conv.conversation_id)
        .where('conversation_participants.user_id', userId)
        .first();

      const unreadCount = await db('messages')
        .join('conversations', 'conversations.id', 'messages.conversation_id')
        .where('conversations.conversation_id', conv.conversation_id)
        .where('messages.sent_at', '>', participant.last_read_at || '1970-01-01')
        .where('messages.sender_id', '!=', userId)
        .count('* as count')
        .first();

      conv.unreadCount = parseInt(unreadCount.count);
    }

    // 格式化对话数据以匹配前端期望的结构
    const formattedConversations = conversations.map(conv => ({
      conversationId: conv.conversation_id,
      type: conv.type.toUpperCase(),
      name: conv.displayName,
      avatar: conv.avatar,
      lastMessage: conv.last_message ? {
        messageId: null,
        content: conv.last_message,
        messageType: conv.last_message_type || 'TEXT',
        timestamp: conv.last_message_at ? new Date(conv.last_message_at).getTime() : null,
        senderInfo: conv.last_sender_name ? {
          nickname: conv.last_sender_name
        } : null
      } : null,
      unreadCount: conv.unreadCount || 0,
      isPinned: false,
      isMuted: false,
      isArchived: false,
      createdAt: new Date().getTime(),
      updatedAt: conv.last_message_at ? new Date(conv.last_message_at).getTime() : new Date().getTime(),
      participants: conv.otherUser ? [{
        userId: conv.otherUser.uid,
        user: {
          userId: conv.otherUser.uid,
          username: conv.otherUser.username,
          nickname: conv.otherUser.nickname,
          avatarUrl: conv.otherUser.avatar_url
        }
      }] : []
    }));

    res.json({
      success: true,
      data: {
        conversations: formattedConversations,
        total: formattedConversations.length,
        page: 1,
        size: formattedConversations.length,
        hasMore: false
      }
    });
  } catch (error) {
    console.error('获取对话列表错误:', error);
    res.status(500).json({
      success: false,
      message: '获取对话列表失败'
    });
  }
}

// 获取对话消息
async function getMessages(req, res) {
  try {
    const { conversationId } = req.params;
    const { page = 1, limit = 50 } = req.query;
    const userId = req.user.userId;

    // 验证用户是否在对话中
    const participant = await db('conversation_participants')
      .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
      .where('conversations.conversation_id', conversationId)
      .where('conversation_participants.user_id', userId)
      .first();

    if (!participant) {
      return res.status(403).json({
        success: false,
        message: '您不在此对话中'
      });
    }

    const offset = (page - 1) * limit;

    const messages = await db('messages')
      .select(
        'messages.*',
        'users.username',
        'users.nickname',
        'users.avatar_url',
        'users.uid as sender_uid'
      )
      .leftJoin('users', 'users.id', 'messages.sender_id')
      .join('conversations', 'conversations.id', 'messages.conversation_id')
      .where('conversations.conversation_id', conversationId)
      .where('messages.is_deleted', false)
      .orderBy('messages.sent_at', 'desc')
      .limit(limit)
      .offset(offset);

    // 格式化消息数据以匹配前端期望的结构
    const formattedMessages = messages.reverse().map(msg => ({
      messageId: msg.message_id,
      conversationId: conversationId,
      senderId: msg.sender_id ? msg.sender_id.toString() : null,
      senderInfo: msg.sender_id ? {
        userId: msg.sender_id.toString(),
        username: msg.username,
        nickname: msg.nickname,
        avatarUrl: msg.avatar_url,
        uid: msg.sender_uid
      } : null,
      content: msg.content,
      messageType: msg.message_type,
      mediaUrl: msg.media_url,
      mediaMetadata: msg.media_metadata ? JSON.parse(msg.media_metadata) : null,
      replyTo: msg.reply_to_id,
      isRead: msg.is_read || false,
      readAt: msg.read_at ? new Date(msg.read_at).getTime() : null,
      isDelivered: msg.is_delivered || false,
      deliveredAt: msg.delivered_at ? new Date(msg.delivered_at).getTime() : null,
      isDeleted: msg.is_deleted || false,
      deletedAt: msg.deleted_at ? new Date(msg.deleted_at).getTime() : null,
      isEdited: msg.is_edited || false,
      editedAt: msg.edited_at ? new Date(msg.edited_at).getTime() : null,
      reactions: msg.reactions ? JSON.parse(msg.reactions) : [],
      mentions: msg.mentions ? JSON.parse(msg.mentions) : [],
      timestamp: new Date(msg.sent_at).getTime(),
      localId: null
    }));

    res.json({
      success: true,
      data: {
        messages: formattedMessages,
        total: formattedMessages.length,
        page: parseInt(page),
        size: parseInt(limit),
        hasMore: formattedMessages.length === parseInt(limit)
      }
    });
  } catch (error) {
    console.error('获取消息错误:', error);
    res.status(500).json({
      success: false,
      message: '获取消息失败'
    });
  }
}

// 发送消息
async function sendMessage(req, res) {
  try {
    const { conversationId, content, messageType = 'text', replyToId } = req.body;
    const userId = req.user.userId;

    // 验证用户是否在对话中
    const participant = await db('conversation_participants')
      .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
      .where('conversations.conversation_id', conversationId)
      .where('conversation_participants.user_id', userId)
      .first();

    if (!participant) {
      return res.status(403).json({
        success: false,
        message: '您不在此对话中'
      });
    }

    // 创建消息
    const messageId = generateUUID();
    const [message] = await db('messages')
      .insert({
        message_id: messageId,
        conversation_id: participant.conversation_id,
        sender_id: userId,
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
      .select('messages.*', 'users.username', 'users.nickname', 'users.avatar_url', 'users.uid as sender_uid')
      .leftJoin('users', 'users.id', 'messages.sender_id')
      .where('messages.id', message.id)
      .first();

    // 格式化消息数据
    const formattedMessage = {
      messageId: fullMessage.message_id,
      conversationId: conversationId,
      senderId: fullMessage.sender_id.toString(),
      senderInfo: {
        userId: fullMessage.sender_id.toString(),
        username: fullMessage.username,
        nickname: fullMessage.nickname,
        avatarUrl: fullMessage.avatar_url,
        uid: fullMessage.sender_uid
      },
      content: fullMessage.content,
      messageType: fullMessage.message_type,
      mediaUrl: fullMessage.media_url,
      mediaMetadata: fullMessage.media_metadata ? JSON.parse(fullMessage.media_metadata) : null,
      replyTo: fullMessage.reply_to_id,
      isRead: false,
      readAt: null,
      isDelivered: true,
      deliveredAt: new Date().getTime(),
      isDeleted: false,
      deletedAt: null,
      isEdited: false,
      editedAt: null,
      reactions: [],
      mentions: [],
      timestamp: new Date(fullMessage.sent_at).getTime(),
      localId: null
    };

    res.json({
      success: true,
      data: {
        message: formattedMessage
      }
    });
  } catch (error) {
    console.error('发送消息错误:', error);
    res.status(500).json({
      success: false,
      message: '发送消息失败'
    });
  }
}

// 与AI角色聊天
async function chatWithAI(req, res) {
  try {
    const { characterId, message } = req.body;
    const userId = req.user.userId;

    // 获取AI角色信息
    const character = await db('ai_characters')
      .where('character_id', characterId)
      .where('is_active', true)
      .first();

    if (!character) {
      return res.status(404).json({
        success: false,
        message: 'AI角色不存在'
      });
    }

    // 查找或创建与AI的对话
    let conversation = await db('conversations')
      .select('conversations.*')
      .join('conversation_participants', 'conversations.id', 'conversation_participants.conversation_id')
      .where('conversations.type', 'ai')
      .where('conversations.name', character.name)
      .where('conversation_participants.user_id', userId)
      .first();

    if (!conversation) {
      // 创建新的AI对话
      const conversationId = generateUUID();
      
      const [newConversation] = await db('conversations')
        .insert({
          conversation_id: conversationId,
          type: 'ai',
          name: character.name,
          description: `与${character.name}的对话`,
          created_by: userId,
          created_at: new Date()
        })
        .returning('*');

      // 添加用户为参与者
      await db('conversation_participants').insert({
        conversation_id: newConversation.id,
        user_id: userId,
        role: 'member',
        joined_at: new Date()
      });

      conversation = newConversation;
    }

    // 保存用户消息
    const userMessageId = generateUUID();
    await db('messages').insert({
      message_id: userMessageId,
      conversation_id: conversation.id,
      sender_id: userId,
      content: message,
      message_type: 'text',
      sent_at: new Date()
    });

    // 获取对话历史（最近10条）
    const messageHistory = await db('messages')
      .select('content', 'sender_id')
      .where('conversation_id', conversation.id)
      .where('is_deleted', false)
      .orderBy('sent_at', 'desc')
      .limit(10);

    // 构建AI请求
    const modelConfig = JSON.parse(character.model_config || '{}');
    const messages = [
      {
        role: 'system',
        content: character.system_prompt
      }
    ];

    // 添加历史消息
    messageHistory.reverse().forEach(msg => {
      messages.push({
        role: msg.sender_id === userId ? 'user' : 'assistant',
        content: msg.content
      });
    });

    // 调用AI API
    const aiResponse = await callAIAPI(character.model, messages, modelConfig);

    // 保存AI回复
    const aiMessageId = generateUUID();
    const [aiMessage] = await db('messages')
      .insert({
        message_id: aiMessageId,
        conversation_id: conversation.id,
        sender_id: null, // AI消息没有sender_id
        content: aiResponse,
        message_type: 'text',
        metadata: JSON.stringify({
          aiCharacterId: character.character_id,
          aiCharacterName: character.name
        }),
        sent_at: new Date()
      })
      .returning('*');

    // 更新对话最后消息时间
    await db('conversations')
      .where('id', conversation.id)
      .update({ last_message_at: new Date() });

    res.json({
      success: true,
      data: {
        conversationId: conversation.conversation_id,
        message: aiMessage.content,
        character: {
          id: character.character_id,
          name: character.name,
          avatar: character.avatar_url
        }
      }
    });

  } catch (error) {
    console.error('AI聊天错误:', error);
    res.status(500).json({
      success: false,
      message: 'AI聊天失败'
    });
  }
}

// 调用AI API (使用zetatechs API中转)
async function callAIAPI(model, messages, config) {
  try {
    // 确保消息格式正确，system role需要转换为developer role
    const formattedMessages = messages.map(msg => ({
      role: msg.role === 'system' ? 'developer' : msg.role,
      content: msg.content
    }));

    const response = await axios.post(
      `${process.env.AI_BASE_URL}/chat/completions`,
      {
        model: model || process.env.DEFAULT_AI_MODEL || 'gpt-4.1',
        messages: formattedMessages,
        temperature: config.temperature || 0.7,
        max_tokens: config.maxTokens || 1000,
        top_p: config.topP || 1,
        frequency_penalty: config.frequencyPenalty || 0,
        presence_penalty: config.presencePenalty || 0
      },
      {
        headers: {
          'Authorization': `Bearer ${process.env.NEWAPI_API_KEY}`,
          'Content-Type': 'application/json'
        },
        timeout: 30000 // 30秒超时
      }
    );

    if (response.data && response.data.choices && response.data.choices[0]) {
      return response.data.choices[0].message.content;
    } else {
      throw new Error('AI API返回格式错误');
    }
  } catch (error) {
    console.error('AI API调用错误:', error.response ? error.response.data : error.message);
    
    // 更详细的错误处理
    if (error.response) {
      const status = error.response.status;
      const errorData = error.response.data;
      
      if (status === 401) {
        throw new Error('AI API认证失败，请检查API密钥');
      } else if (status === 429) {
        throw new Error('AI API请求过于频繁，请稍后重试');
      } else if (status === 500) {
        throw new Error('AI API服务器错误，请稍后重试');
      } else {
        throw new Error(`AI API错误: ${errorData && errorData.error ? errorData.error.message : error.message}`);
      }
    } else if (error.code === 'ECONNABORTED') {
      throw new Error('AI API请求超时，请稍后重试');
    } else {
      throw new Error('AI服务暂时不可用，请稍后重试');
    }
  }
}

// 标记消息为已读
async function markAsRead(req, res) {
  try {
    const { conversationId } = req.params;
    const userId = req.user.userId;

    await db('conversation_participants')
      .join('conversations', 'conversations.id', 'conversation_participants.conversation_id')
      .where('conversations.conversation_id', conversationId)
      .where('conversation_participants.user_id', userId)
      .update({ last_read_at: new Date() });

    res.json({
      success: true,
      message: '标记已读成功'
    });
  } catch (error) {
    console.error('标记已读错误:', error);
    res.status(500).json({
      success: false,
      message: '标记已读失败'
    });
  }
}

module.exports = {
  getConversations,
  getMessages,
  sendMessage,
  chatWithAI,
  markAsRead
};