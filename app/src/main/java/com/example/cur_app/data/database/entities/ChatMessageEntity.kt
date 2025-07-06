package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * 聊天消息实体类
 * 存储聊天消息记录
 */
@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"]),
        Index(value = ["senderId"]),
        Index(value = ["receiverId"])
    ]
)
@Serializable
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 对话信息
    val conversationId: String,            // 对话ID
    val senderId: String,                  // 发送者ID
    val receiverId: String,                // 接收者ID
    
    // 消息内容
    val content: String,                   // 消息内容
    val messageType: String = "TEXT",      // 消息类型：TEXT, IMAGE, SYSTEM
    val metadata: String = "",             // 消息元数据（JSON格式）
    
    // 状态信息
    val isRead: Boolean = false,           // 是否已读
    val isFromMe: Boolean = false,         // 是否为自己发送
    val isDeleted: Boolean = false,        // 是否已删除
    val isSent: Boolean = true,            // 是否发送成功
    
    // 时间信息
    val timestamp: Long = System.currentTimeMillis(),  // 发送时间
    val readTimestamp: Long? = null,       // 阅读时间
    val editTimestamp: Long? = null,       // 编辑时间
    
    // 附加信息
    val replyToMessageId: Long? = null,    // 回复的消息ID
    val forwardFromMessageId: Long? = null, // 转发来源消息ID
    val reactions: String = "",            // 表情反应（JSON数组）
    
    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)