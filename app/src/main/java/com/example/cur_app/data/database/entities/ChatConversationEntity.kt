package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * 聊天对话实体类
 * 存储聊天对话信息
 */
@Entity(
    tableName = "chat_conversations",
    indices = [
        Index(value = ["otherUserId"]),
        Index(value = ["lastMessageTime"]),
        Index(value = ["isPinned", "lastMessageTime"])
    ]
)
@Serializable
data class ChatConversationEntity(
    @PrimaryKey
    val conversationId: String,           // 对话ID
    
    // 对话基本信息
    val otherUserId: String,              // 对方用户ID
    val conversationType: String = "PRIVATE", // 对话类型：PRIVATE, GROUP, AI
    val title: String = "",               // 对话标题（群聊使用）
    val description: String = "",         // 对话描述
    
    // 最后消息信息
    val lastMessage: String = "",         // 最后一条消息内容
    val lastMessageTime: Long = System.currentTimeMillis(), // 最后消息时间
    val lastMessageSenderId: String = "", // 最后消息发送者ID
    val lastMessageType: String = "TEXT", // 最后消息类型
    
    // 状态信息
    val unreadCount: Int = 0,             // 未读消息数
    val isPinned: Boolean = false,        // 是否置顶
    val isArchived: Boolean = false,      // 是否归档
    val isMuted: Boolean = false,         // 是否静音
    val isBlocked: Boolean = false,       // 是否屏蔽
    
    // 设置信息
    val customAvatar: String = "",        // 自定义头像
    val customName: String = "",          // 自定义名称
    val theme: String = "default",        // 聊天主题
    
    // 统计信息
    val totalMessages: Int = 0,           // 总消息数
    val myMessages: Int = 0,              // 我发送的消息数
    val otherMessages: Int = 0,           // 对方发送的消息数
    
    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)