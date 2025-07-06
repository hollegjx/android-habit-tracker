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
    val characterId: String,               // 角色唯一标识符
    val name: String,                      // 角色名称
    val subtitle: String,                  // 角色副标题
    val type: String,                      // 角色类型：encourager, strict, friend, mentor
    val description: String,               // 角色描述
    val avatar: String,                    // 头像资源标识
    val iconEmoji: String,                 // 角色图标emoji
    val backgroundColors: String,          // 背景颜色（JSON数组存储）
    val skills: String,                    // 技能列表（JSON数组存储）
    
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
) {
    /**
     * 将AiCharacterEntity转换为UI使用的AiCharacter对象
     */
    fun toUiModel(): com.example.cur_app.presentation.screens.AiCharacter {
        // 解析背景颜色JSON
        val backgroundColors = try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(backgroundColors)
                .map { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it)) }
        } catch (e: Exception) {
            listOf(
                androidx.compose.ui.graphics.Color(0xFFff9a9e),
                androidx.compose.ui.graphics.Color(0xFFfecfef)
            )
        }
        
        // 解析技能列表JSON
        val skillsList = try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(skills)
        } catch (e: Exception) {
            listOf("AI助手", "智能陪伴")
        }
        
        return com.example.cur_app.presentation.screens.AiCharacter(
            id = characterId,
            name = name,
            subtitle = subtitle,
            description = description,
            skills = skillsList,
            backgroundColor = backgroundColors,
            iconEmoji = iconEmoji,
            greeting = try {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(greetingMessages).firstOrNull() ?: "你好！"
            } catch (e: Exception) {
                "你好！"
            },
            personality = personality,
            speakingStyle = speakingStyle,
            mood = "${iconEmoji} ${name}今天心情很好呢～"
        )
    }
} 