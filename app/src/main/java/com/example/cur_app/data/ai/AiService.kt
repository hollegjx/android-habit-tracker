package com.example.cur_app.data.ai

import com.example.cur_app.data.database.entities.AiCharacterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI服务
 * 统一的AI功能入口，包含智能降级策略
 */
@Singleton
class AiService @Inject constructor(
    private val aiApiClient: AiApiClient,
    private val localFallback: LocalAiService
) {
    
    /**
     * AI聊天
     * @param userMessage 用户消息
     * @param characterType AI角色类型 (encourager, strict, friend, mentor)
     * @param conversationHistory 对话历史
     * @return AI回复消息
     */
    suspend fun chat(
        userMessage: String,
        characterType: String = "friend",
        conversationHistory: List<String> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        android.util.Log.d("AiService", "🚀 开始AI聊天")
        android.util.Log.d("AiService", "🚀 用户消息: $userMessage")
        android.util.Log.d("AiService", "🚀 角色类型: $characterType")
        
        // 尝试调用在线AI API
        when (val result = aiApiClient.chat(userMessage, characterType, conversationHistory)) {
            is AiResponse.Success -> {
                android.util.Log.d("AiService", "🚀 在线AI回复成功: ${result.message}")
                result.message
            }
            is AiResponse.Error -> {
                android.util.Log.w("AiService", "🚀 在线AI失败，使用本地降级: ${result.error}")
                // 降级到本地AI服务
                localFallback.generateResponse(userMessage, characterType)
            }
        }
    }
    
    /**
     * AI聊天（支持具体角色信息）
     * @param userMessage 用户消息
     * @param character AI角色实体
     * @param conversationHistory 对话历史
     * @return AI回复消息
     */
    suspend fun chatWithCharacter(
        userMessage: String,
        character: AiCharacterEntity,
        conversationHistory: List<String> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        android.util.Log.d("AiService", "🚀 开始AI聊天（具体角色）")
        android.util.Log.d("AiService", "🚀 用户消息: $userMessage")
        android.util.Log.d("AiService", "🚀 角色名称: ${character.name}")
        
        // 尝试调用在线AI API，传递完整角色信息
        when (val result = aiApiClient.chatWithCharacter(userMessage, character, conversationHistory)) {
            is AiResponse.Success -> {
                android.util.Log.d("AiService", "🚀 在线AI回复成功: ${result.message}")
                result.message
            }
            is AiResponse.Error -> {
                android.util.Log.w("AiService", "🚀 在线AI失败，使用本地降级: ${result.error}")
                // 降级到本地AI服务，使用角色类型
                localFallback.generateResponse(userMessage, character.type)
            }
        }
    }
    
    /**
     * 生成鼓励消息
     * @param habitName 习惯名称
     * @param currentStreak 当前连击天数
     * @param completionRate 完成率
     * @param characterType AI角色类型
     * @return 鼓励消息
     */
    suspend fun generateEncouragement(
        habitName: String,
        currentStreak: Int,
        completionRate: Float,
        characterType: String = "encourager"
    ): String = withContext(Dispatchers.IO) {
        val encouragementMessage = "请为我的习惯「$habitName」生成一句鼓励的话。我已经连续坚持了${currentStreak}天，完成率是${(completionRate * 100).toInt()}%。"
        chat(encouragementMessage, characterType)
    }
    
    /**
     * 生成提醒消息
     * @param habitName 习惯名称
     * @param missedDays 错过的天数
     * @param characterType AI角色类型
     * @return 提醒消息
     */
    suspend fun generateReminder(
        habitName: String,
        missedDays: Int,
        characterType: String = "friend"
    ): String = withContext(Dispatchers.IO) {
        val reminderMessage = if (missedDays > 0) {
            "我已经${missedDays}天没有完成「$habitName」这个习惯了，请提醒我今天要完成。"
        } else {
            "请提醒我今天要完成「$habitName」这个习惯。"
        }
        chat(reminderMessage, characterType)
    }
    
    /**
     * 生成庆祝消息
     * @param habitName 习惯名称
     * @param achievement 成就描述
     * @param characterType AI角色类型
     * @return 庆祝消息
     */
    suspend fun generateCelebration(
        habitName: String,
        achievement: String,
        characterType: String = "encourager"
    ): String = withContext(Dispatchers.IO) {
        val celebrationMessage = "我在「$habitName」习惯上取得了新成就：$achievement。请为我庆祝一下！"
        chat(celebrationMessage, characterType)
    }
}