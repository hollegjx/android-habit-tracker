package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * AI对话历史实体类
 * 存储用户与AI角色的交互对话记录
 */
@Entity(
    tableName = "ai_conversations",
    foreignKeys = [
        ForeignKey(
            entity = AiCharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["characterId"]),
        Index(value = ["timestamp"])
    ]
)
@Serializable
data class AiConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 关联信息
    val characterId: Long,                 // 关联的AI角色ID
    val habitId: Long? = null,             // 关联的习惯ID（可选）
    
    // 对话内容
    val userMessage: String = "",          // 用户消息（可为空，表示AI主动发起）
    val aiMessage: String,                 // AI回复消息
    val messageType: String,               // 消息类型：greeting, encouragement, reminder, celebration, chat
    val context: String = "",              // 上下文信息（JSON格式）
    
    // 交互信息
    val timestamp: Long = System.currentTimeMillis(),  // 对话时间
    val sessionId: String = "",            // 会话ID（用于关联一次完整对话）
    val userRating: Int? = null,           // 用户对AI回复的评分 1-5
    val isRead: Boolean = false,           // 用户是否已读
    
    // TTS信息
    val hasAudio: Boolean = false,         // 是否有语音输出
    val audioPath: String = "",            // 音频文件路径
    val playCount: Int = 0,                // 播放次数
    
    // 元数据
    val emotionalTone: String = "neutral", // 情感色调：positive, neutral, negative, excited, calm
    val responseTime: Long = 0,            // AI响应时间（毫秒）
    val tokenCount: Int = 0,               // AI模型token消耗
    
    // 时间信息
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val updatedAt: Long = System.currentTimeMillis()   // 更新时间
) 