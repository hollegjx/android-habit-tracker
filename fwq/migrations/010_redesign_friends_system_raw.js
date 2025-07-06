/**
 * 好友系统完全重新设计迁移 - 使用原始SQL
 * 兼容Node.js v10 + Knex v3
 */

exports.up = function(knex) {
  return knex.transaction(async (trx) => {
    console.log('开始好友系统完全重新设计...');

    // 1. 备份现有数据
    const existingFriendships = await trx('friendships').select('*');
    console.log(`备份 ${existingFriendships.length} 条现有好友关系数据`);

    // 2. 删除现有的好友表
    await trx.schema.dropTableIfExists('friendships');
    console.log('删除旧的好友表');

    // 3. 使用原始SQL创建增强的好友表
    await trx.raw(`
      CREATE TABLE friendships (
        id SERIAL PRIMARY KEY,
        requester_id INTEGER NOT NULL,
        addressee_id INTEGER NOT NULL,
        status VARCHAR(50) DEFAULT 'pending',
        requester_message VARCHAR(500),
        reject_reason VARCHAR(500),
        friendship_alias VARCHAR(100),
        is_starred BOOLEAN DEFAULT false,
        is_muted BOOLEAN DEFAULT false,
        is_blocked BOOLEAN DEFAULT false,
        conversation_id VARCHAR(100),
        unread_count INTEGER DEFAULT 0,
        last_message_at TIMESTAMP,
        last_read_at TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_friendship_addressee FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT unique_friendship_pair UNIQUE (requester_id, addressee_id)
      )
    `);
    
    await trx.raw(`
      CREATE INDEX idx_friendship_users ON friendships(requester_id, addressee_id);
      CREATE INDEX idx_friendship_status ON friendships(status);
      CREATE INDEX idx_friendship_conversation ON friendships(conversation_id);
      CREATE INDEX idx_friendship_last_message ON friendships(last_message_at);
    `);
    console.log('创建新的增强好友表');

    // 4. 创建好友请求通知表
    await trx.raw(`
      CREATE TABLE friend_notifications (
        id SERIAL PRIMARY KEY,
        friendship_id INTEGER NOT NULL,
        user_id INTEGER NOT NULL,
        type VARCHAR(50) NOT NULL,
        message VARCHAR(500),
        is_read BOOLEAN DEFAULT false,
        read_at TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_notification_friendship FOREIGN KEY (friendship_id) REFERENCES friendships(id) ON DELETE CASCADE,
        CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
      )
    `);
    
    await trx.raw(`
      CREATE INDEX idx_user_notifications ON friend_notifications(user_id, is_read);
      CREATE INDEX idx_notification_type ON friend_notifications(type);
    `);
    console.log('创建好友通知表');

    // 5. 创建或更新对话表
    const hasConversations = await trx.schema.hasTable('conversations');
    if (!hasConversations) {
      await trx.raw(`
        CREATE TABLE conversations (
          id SERIAL PRIMARY KEY,
          conversation_id VARCHAR(100) UNIQUE NOT NULL,
          type VARCHAR(50) DEFAULT 'private',
          name VARCHAR(200),
          description TEXT,
          avatar_url VARCHAR(500),
          created_by INTEGER,
          is_active BOOLEAN DEFAULT true,
          last_message_at TIMESTAMP,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          CONSTRAINT fk_conversation_creator FOREIGN KEY (created_by) REFERENCES users(id)
        )
      `);
      
      await trx.raw(`
        CREATE INDEX idx_conversation_type ON conversations(type);
        CREATE INDEX idx_conversation_last_message ON conversations(last_message_at);
      `);
      console.log('创建对话表');
    }

    // 6. 创建或更新对话参与者表
    const hasParticipants = await trx.schema.hasTable('conversation_participants');
    if (!hasParticipants) {
      await trx.raw(`
        CREATE TABLE conversation_participants (
          id SERIAL PRIMARY KEY,
          conversation_id INTEGER NOT NULL,
          user_id INTEGER NOT NULL,
          role VARCHAR(50) DEFAULT 'member',
          joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          last_read_at TIMESTAMP,
          is_muted BOOLEAN DEFAULT false,
          is_pinned BOOLEAN DEFAULT false,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          CONSTRAINT fk_participant_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
          CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
          CONSTRAINT unique_conversation_participant UNIQUE (conversation_id, user_id)
        )
      `);
      
      await trx.raw(`
        CREATE INDEX idx_participant_user ON conversation_participants(user_id);
      `);
      console.log('创建对话参与者表');
    }

    // 7. 创建或更新消息表
    const hasMessages = await trx.schema.hasTable('messages');
    if (!hasMessages) {
      await trx.raw(`
        CREATE TABLE messages (
          id SERIAL PRIMARY KEY,
          message_id VARCHAR(100) UNIQUE NOT NULL,
          conversation_id INTEGER NOT NULL,
          sender_id INTEGER,
          content TEXT NOT NULL,
          message_type VARCHAR(50) DEFAULT 'text',
          media_url VARCHAR(1000),
          media_metadata TEXT,
          reply_to_id INTEGER,
          is_edited BOOLEAN DEFAULT false,
          edited_at TIMESTAMP,
          is_deleted BOOLEAN DEFAULT false,
          deleted_at TIMESTAMP,
          is_read BOOLEAN DEFAULT false,
          read_at TIMESTAMP,
          is_delivered BOOLEAN DEFAULT false,
          delivered_at TIMESTAMP,
          reactions TEXT,
          mentions TEXT,
          sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
          CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL,
          CONSTRAINT fk_message_reply FOREIGN KEY (reply_to_id) REFERENCES messages(id)
        )
      `);
      
      await trx.raw(`
        CREATE INDEX idx_message_conversation_time ON messages(conversation_id, sent_at);
        CREATE INDEX idx_message_sender ON messages(sender_id);
        CREATE INDEX idx_message_type ON messages(message_type);
      `);
      console.log('创建消息表');
    }

    // 8. 恢复现有好友数据（转换为新格式）
    for (const friendship of existingFriendships) {
      await trx('friendships').insert({
        requester_id: friendship.requester_id,
        addressee_id: friendship.addressee_id,
        status: friendship.status,
        created_at: friendship.created_at,
        updated_at: friendship.updated_at
      });
    }
    console.log(`恢复 ${existingFriendships.length} 条好友关系数据`);

    console.log('好友系统重新设计完成！');
  });
};

exports.down = function(knex) {
  return knex.transaction(async (trx) => {
    console.log('回滚好友系统重新设计...');
    
    // 删除新创建的表
    await trx.schema.dropTableIfExists('friend_notifications');
    await trx.schema.dropTableIfExists('messages');
    await trx.schema.dropTableIfExists('conversation_participants');
    await trx.schema.dropTableIfExists('conversations');
    await trx.schema.dropTableIfExists('friendships');
    
    // 恢复简单的好友表
    await trx.raw(`
      CREATE TABLE friendships (
        id SERIAL PRIMARY KEY,
        requester_id INTEGER NOT NULL,
        addressee_id INTEGER NOT NULL,
        status VARCHAR(50) DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    
    console.log('好友系统回滚完成');
  });
};