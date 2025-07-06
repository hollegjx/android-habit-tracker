package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * 聊天用户实体类
 * 存储聊天用户信息
 */
@Entity(
    tableName = "chat_users",
    indices = [
        Index(value = ["isAiBot"]),
        Index(value = ["isOnline"]),
        Index(value = ["lastSeenTime"])
    ]
)
@Serializable
data class ChatUserEntity(
    @PrimaryKey
    val userId: String,                   // 用户ID
    
    // 基本信息
    val nickname: String,                 // 昵称
    val realName: String = "",            // 真实姓名
    val avatar: String,                   // 头像（emoji或URL）
    val bio: String = "",                 // 个人简介
    val email: String = "",               // 邮箱
    val phone: String = "",               // 手机号
    
    // AI机器人信息
    val isAiBot: Boolean = false,         // 是否为AI机器人
    val aiType: String? = null,           // AI类型
    val aiPersonality: String = "",       // AI个性描述
    val aiCapabilities: String = "",      // AI能力列表（JSON数组）
    
    // 状态信息
    val isOnline: Boolean = false,        // 是否在线
    val lastSeenTime: Long = System.currentTimeMillis(), // 最后活跃时间
    val status: String = "available",     // 状态：available, busy, away, invisible
    val statusMessage: String = "",       // 状态消息
    
    // 设置信息
    val language: String = "zh-CN",       // 语言设置
    val timezone: String = "Asia/Shanghai", // 时区设置
    val notificationSettings: String = "", // 通知设置（JSON）
    
    // 统计信息
    val totalMessages: Int = 0,           // 总发送消息数
    val totalConversations: Int = 0,      // 参与的对话数
    val averageResponseTime: Long = 0,    // 平均响应时间（秒）
    
    // 验证信息
    val isVerified: Boolean = false,      // 是否验证用户
    val verificationLevel: String = "none", // 验证级别：none, email, phone, official
    
    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)