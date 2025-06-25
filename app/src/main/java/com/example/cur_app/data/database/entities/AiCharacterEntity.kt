package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * AI角色实体类
 * 存储AI陪伴者的个性、外观和交互配置
 */
@Entity(tableName = "ai_characters")
@Serializable
data class AiCharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val name: String,                      // 角色名称
    val type: String,                      // 角色类型：encourager, strict, friend, mentor
    val description: String,               // 角色描述
    val avatar: String,                    // 头像资源标识
    
    // 个性特征
    val personality: String,               // 个性描述JSON字符串
    val speakingStyle: String,             // 说话风格：gentle, strict, casual, professional
    val motivationStyle: String,           // 激励方式：praise, challenge, support, guide
    
    // 交互配置
    val greetingMessages: String,          // 问候语列表（JSON数组）
    val encouragementMessages: String,     // 鼓励语列表（JSON数组）
    val reminderMessages: String,          // 提醒语列表（JSON数组）
    val celebrationMessages: String,       // 庆祝语列表（JSON数组）
    
    // TTS配置
    val voiceId: String = "default",       // 语音ID
    val speechRate: Float = 1.0f,          // 语速
    val speechPitch: Float = 1.0f,         // 音调
    
    // 状态信息
    val isDefault: Boolean = false,        // 是否为默认角色
    val isActive: Boolean = true,          // 是否激活
    val isSelected: Boolean = false,       // 是否被选中使用
    val unlocked: Boolean = true,          // 是否已解锁
    
    // 使用统计
    val usageCount: Int = 0,               // 使用次数
    val lastUsedAt: Long? = null,          // 最后使用时间
    val userRating: Float = 0f,            // 用户评分 0.0-5.0
    
    // 时间信息
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val updatedAt: Long = System.currentTimeMillis()   // 更新时间
) 