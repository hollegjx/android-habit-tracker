package com.example.cur_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import com.example.cur_app.data.local.AiCharacterManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 聊天用户实体
 */
@Entity(tableName = "chat_users")
data class ChatUser(
    @PrimaryKey
    val userId: String, // 用户ID
    val nickname: String, // 昵称
    val avatar: String, // 头像（emoji或URL）
    val isAiBot: Boolean = false, // 是否为AI机器人
    val aiType: String? = null, // AI类型（如果是AI机器人）
    val lastSeenTime: Date = Date(), // 最后活跃时间
    val isOnline: Boolean = false // 是否在线
)

/**
 * 聊天消息实体
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val messageId: Long = 0,
    val conversationId: String, // 对话ID
    val senderId: String, // 发送者ID
    val receiverId: String, // 接收者ID
    val content: String, // 消息内容
    val messageType: MessageType = MessageType.TEXT, // 消息类型
    val timestamp: Date = Date(), // 发送时间
    val isRead: Boolean = false, // 是否已读
    val isFromMe: Boolean = false // 是否为自己发送
)

/**
 * 聊天对话实体
 */
@Entity(tableName = "chat_conversations")
data class ChatConversation(
    @PrimaryKey
    val conversationId: String, // 对话ID
    val otherUserId: String, // 对方用户ID
    val lastMessage: String = "", // 最后一条消息
    val lastMessageTime: Date = Date(), // 最后消息时间
    val unreadCount: Int = 0, // 未读消息数
    val isPinned: Boolean = false, // 是否置顶
    val isArchived: Boolean = false // 是否归档
)

/**
 * 消息类型枚举
 */
enum class MessageType {
    TEXT, // 文本消息
    IMAGE, // 图片消息
    SYSTEM // 系统消息
}

/**
 * 聊天列表显示用的数据模型
 */
data class ChatListItem(
    val conversation: ChatConversation,
    val otherUser: ChatUser,
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int,
    val isOnline: Boolean
)

/**
 * 预定义的AI机器人数据
 */
object AiBots {
    val aiUsers = listOf(
        ChatUser(
            userId = "ai_bot_default",
            nickname = "AI伙伴",
            avatar = "🤖",
            isAiBot = true,
            aiType = "default",
            isOnline = true
        ),
        ChatUser(
            userId = "ai_xiaomei", 
            nickname = "小美",
            avatar = "💕",
            isAiBot = true,
            aiType = "xiaomei",
            isOnline = true
        ),
        ChatUser(
            userId = "ai_leiming",
            nickname = "雷鸣", 
            avatar = "⚡",
            isAiBot = true,
            aiType = "leiming",
            isOnline = true
        ),
        ChatUser(
            userId = "ai_mengmeng",
            nickname = "萌萌",
            avatar = "😄",
            isAiBot = true,
            aiType = "mengmeng", 
            isOnline = true
        )
    )
    
    fun getDefaultAiBot() = aiUsers.first()
    
    /**
     * 根据当前选中的AI角色获取对应的ChatUser
     */
    fun getCurrentAiCharacterAsUser(): ChatUser {
        val currentCharacter = runBlocking { AiCharacterManager.currentCharacter.first() }
        return ChatUser(
            userId = "ai_current_character",
            nickname = currentCharacter.name,
            avatar = currentCharacter.iconEmoji,
            isAiBot = true,
            aiType = "current_character",
            isOnline = true
        )
    }
}

/**
 * 模拟聊天数据
 */
object MockChatData {
    fun getMockChatUsers() = listOf(
        ChatUser(
            userId = "user_001",
            nickname = "张小明",
            avatar = "😊",
            isOnline = true
        ),
        ChatUser(
            userId = "user_002", 
            nickname = "李小红",
            avatar = "🥰",
            isOnline = false
        ),
        ChatUser(
            userId = "user_003",
            nickname = "王小强",
            avatar = "😎",
            isOnline = true
        )
    ) + AiBots.aiUsers
    
    fun getMockConversations() = listOf(
        ChatConversation(
            conversationId = "conv_ai_default",
            otherUserId = "ai_bot_default",
            lastMessage = "今天的学习计划完成得怎么样？",
            lastMessageTime = Date(System.currentTimeMillis() - 5 * 60 * 1000), // 5分钟前
            unreadCount = 2
        ),
        ChatConversation(
            conversationId = "conv_001", 
            otherUserId = "user_001",
            lastMessage = "今天的英语单词背完了吗？",
            lastMessageTime = Date(System.currentTimeMillis() - 30 * 60 * 1000), // 30分钟前
            unreadCount = 1
        ),
        ChatConversation(
            conversationId = "conv_002",
            otherUserId = "user_002",
            lastMessage = "一起复习数学吧！",
            lastMessageTime = Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000), // 2小时前
            unreadCount = 0
        ),
        ChatConversation(
            conversationId = "conv_003",
            otherUserId = "user_003", 
            lastMessage = "周末去图书馆学习吗？",
            lastMessageTime = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), // 1天前
            unreadCount = 3
        )
    )
} 