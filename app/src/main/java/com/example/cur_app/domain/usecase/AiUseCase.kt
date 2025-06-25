package com.example.cur_app.domain.usecase

import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.data.database.entities.AiConversationEntity
import com.example.cur_app.data.repository.AiRepository
import com.example.cur_app.data.repository.AiMessageResult
import com.example.cur_app.data.repository.AiServiceStatus

import com.example.cur_app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI交互业务逻辑用例
 * 封装AI相关的复杂业务逻辑，提供智能交互接口
 */
@Singleton
class AiUseCase @Inject constructor(
    private val aiRepository: AiRepository,

    private val preferencesRepository: PreferencesRepository
) {
    
    // ========== AI角色管理 ==========
    
    /**
     * 获取所有可用的AI角色
     */
    fun getAllCharacters(): Flow<List<AiCharacterEntity>> {
        return aiRepository.getAllActiveCharacters()
    }
    
    /**
     * 获取当前选中的AI角色
     */
    suspend fun getCurrentCharacter(): Result<AiCharacterEntity> {
        return try {
            val character = aiRepository.getSelectedCharacter()
                ?: return Result.failure(IllegalStateException("未选择AI角色"))
            Result.success(character)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 选择AI角色
     */
    suspend fun selectCharacter(characterId: Long): Result<Unit> {
        return try {
            aiRepository.selectCharacter(characterId)
            preferencesRepository.saveSelectedCharacterId(characterId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 初始化默认AI角色
     */
    suspend fun initializeDefaultCharacters(): Result<Unit> {
        return try {
            // 检查是否已有角色
            val existingCharacters = aiRepository.getAllActiveCharacters().first()
            if (existingCharacters.isNotEmpty()) {
                return Result.success(Unit)
            }
            
            // 创建默认角色
            val defaultCharacters = listOf(
                AiCharacterEntity(
                    name = "小鼓励",
                    type = "encouraging",
                    description = "温暖贴心的鼓励者，总是给你正能量",
                    personality = "温和、乐观、支持性强",
                    avatar = "😊",
                    speakingStyle = "gentle",
                    motivationStyle = "praise",
                    greetingMessages = """["新的一天开始了！你准备好迎接挑战了吗？", "早上好！今天也要保持好心情哦～"]""",
                    encouragementMessages = """["你做得真棒！继续加油！", "每一小步都是进步，为你感到骄傲！"]""",
                    reminderMessages = """["温柔提醒：是时候完成今天的习惯了～", "别忘记你的小目标哦！"]""",
                    celebrationMessages = """["太棒了！你又完成了一个目标！", "恭喜你！坚持就是胜利！"]""",
                    isSelected = true,
                    isActive = true
                ),
                AiCharacterEntity(
                    name = "严师",
                    type = "strict",
                    description = "严格的导师，帮你保持纪律",
                    personality = "严格、直接、目标导向",
                    avatar = "🎯",
                    speakingStyle = "strict",
                    motivationStyle = "challenge",
                    greetingMessages = """["今天的目标明确了吗？开始行动！", "时间不等人，立即开始执行计划！"]""",
                    encouragementMessages = """["不错，但还能做得更好！", "达标只是基础，追求卓越！"]""",
                    reminderMessages = """["你的习惯进度落后了！立即行动！", "没有借口，现在就去完成！"]""",
                    celebrationMessages = """["达成目标是应该的，继续保持！", "这就是纪律的力量！"]""",
                    isSelected = false,
                    isActive = true
                ),
                AiCharacterEntity(
                    name = "小伙伴",
                    type = "friendly",
                    description = "亲切的朋友，陪伴你一起成长",
                    personality = "友好、亲近、理解力强",
                    avatar = "👥",
                    speakingStyle = "casual",
                    motivationStyle = "support",
                    greetingMessages = """["嗨！今天感觉怎么样？", "我们一起加油吧！"]""",
                    encouragementMessages = """["你真的很努力呢！", "我看到你的进步了！"]""",
                    reminderMessages = """["朋友，记得完成今天的小目标哦～", "一起坚持，我陪着你！"]""",
                    celebrationMessages = """["太棒了！为你开心！", "我们一起庆祝这个成就！"]""",
                    isSelected = false,
                    isActive = true
                ),
                AiCharacterEntity(
                    name = "智者",
                    type = "mentor",
                    description = "睿智的导师，给你深刻的人生建议",
                    personality = "智慧、深刻、启发性",
                    avatar = "🧙‍♂️",
                    speakingStyle = "professional",
                    motivationStyle = "guide",
                    greetingMessages = """["每一天都是成长的机会。", "智慧来自于持续的行动。"]""",
                    encouragementMessages = """["你正在走向更好的自己。", "坚持的背后是成长的力量。"]""",
                    reminderMessages = """["优秀是一种习惯，请继续保持。", "真正的成长来自日复一日的坚持。"]""",
                    celebrationMessages = """["这是你努力的结果，值得赞许。", "每个成就都是智慧的体现。"]""",
                    isSelected = false,
                    isActive = true
                )
            )
            
            defaultCharacters.forEach { character ->
                aiRepository.createCharacter(character)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 智能交互 ==========
    
    /**
     * 生成打卡完成鼓励消息
     */
    suspend fun generateCompletionEncouragement(
        itemTitle: String,
        currentStreak: Int,
        completionRate: Float
    ): Result<AiMessage> {
        return try {
            when (val result = aiRepository.generateEncouragement(
                habitName = itemTitle,
                currentStreak = currentStreak,
                completionRate = completionRate
            )) {
                is AiMessageResult.Success -> Result.success(
                    AiMessage(
                        content = result.message,
                        type = "encouragement",
                        isFromNetwork = result.isFromNetwork,
                        timestamp = System.currentTimeMillis()
                    )
                )
                is AiMessageResult.Error -> Result.failure(
                    IllegalStateException(result.message)
                )
                AiMessageResult.Loading -> Result.failure(
                    IllegalStateException("AI服务正在处理中")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成打卡提醒消息
     */
    suspend fun generateCheckInReminder(
        itemTitle: String,
        missedDays: Int
    ): Result<AiMessage> {
        return try {
            when (val result = aiRepository.generateReminder(
                habitName = itemTitle,
                missedDays = missedDays
            )) {
                is AiMessageResult.Success -> Result.success(
                    AiMessage(
                        content = result.message,
                        type = "reminder",
                        isFromNetwork = result.isFromNetwork,
                        timestamp = System.currentTimeMillis()
                    )
                )
                is AiMessageResult.Error -> Result.failure(
                    IllegalStateException(result.message)
                )
                AiMessageResult.Loading -> Result.failure(
                    IllegalStateException("AI服务正在处理中")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成成就庆祝消息
     */
    suspend fun generateAchievementCelebration(
        itemTitle: String,
        achievementTitle: String
    ): Result<AiMessage> {
        return try {
            when (val result = aiRepository.generateCelebration(
                habitName = itemTitle,
                achievement = achievementTitle
            )) {
                is AiMessageResult.Success -> Result.success(
                    AiMessage(
                        content = result.message,
                        type = "celebration",
                        isFromNetwork = result.isFromNetwork,
                        timestamp = System.currentTimeMillis()
                    )
                )
                is AiMessageResult.Error -> Result.failure(
                    IllegalStateException(result.message)
                )
                AiMessageResult.Loading -> Result.failure(
                    IllegalStateException("AI服务正在处理中")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成每日激励消息
     */
    suspend fun generateDailyMotivation(): Result<AiMessage> {
        return try {
            // 暂时使用固定的激励消息，后续集成CheckInRepository后会改为动态消息
            val message = "新的一天开始了！让我们一起完成今天的打卡目标吧！💪"
            
            Result.success(
                AiMessage(
                    content = message,
                    type = "daily_motivation",
                    isFromNetwork = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取个性化建议
     */
    suspend fun getPersonalizedAdvice(): Result<AiMessage> {
        return try {
            // 暂时使用通用建议，后续集成CheckInRepository后会改为个性化建议
            val advice = "建议从简单的目标开始，比如每天学习30分钟、运动15分钟或储蓄10元。小目标成就大梦想！"
            
            Result.success(
                AiMessage(
                    content = advice,
                    type = "advice",
                    isFromNetwork = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 对话历史管理 ==========
    
    /**
     * 获取所有对话记录
     */
    fun getAllConversations(): Flow<List<AiConversationEntity>> {
        return aiRepository.getAllConversations()
    }
    
    /**
     * 获取与指定打卡项目相关的对话
     */
    fun getCheckInConversations(checkInItemId: Long): Flow<List<AiConversationEntity>> {
        return aiRepository.getConversationsByHabit(checkInItemId)
    }
    
    /**
     * 获取未读对话数量
     */
    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val count = aiRepository.getUnreadCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 标记对话为已读
     */
    suspend fun markConversationAsRead(conversationId: Long): Result<Unit> {
        return try {
            aiRepository.markConversationAsRead(conversationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 对AI回复进行评分
     */
    suspend fun rateAiResponse(conversationId: Long, rating: Int): Result<Unit> {
        return try {
            if (rating !in 1..5) {
                return Result.failure(IllegalArgumentException("评分必须在1-5之间"))
            }
            
            aiRepository.rateConversation(conversationId, rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== AI服务管理 ==========
    
    /**
     * 配置API密钥
     */
    suspend fun configureApiKey(apiKey: String): Result<Unit> {
        return try {
            if (apiKey.isBlank()) {
                return Result.failure(IllegalArgumentException("API密钥不能为空"))
            }
            
            // 保存API密钥
            preferencesRepository.saveApiKey(apiKey.trim())
            aiRepository.setApiKey(apiKey.trim())
            
            // 测试连接
            when (val status = aiRepository.healthCheck()) {
                is AiServiceStatus.Available -> Result.success(Unit)
                is AiServiceStatus.Unavailable -> Result.failure(
                    IllegalStateException("API密钥验证失败: ${status.reason}")
                )
                AiServiceStatus.Checking -> Result.success(Unit) // 异步检查，暂时返回成功
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查AI服务状态
     */
    suspend fun checkServiceStatus(): Result<ServiceStatusInfo> {
        return try {
            val hasApiKey = preferencesRepository.hasApiKey()
            val status = if (hasApiKey) {
                aiRepository.healthCheck()
            } else {
                AiServiceStatus.Unavailable("未配置API密钥")
            }
            
            val statusInfo = ServiceStatusInfo(
                hasApiKey = hasApiKey,
                status = status,
                canUseLocalFallback = true
            )
            
            Result.success(statusInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取AI使用统计
     */
    suspend fun getAiUsageStats(): Result<AiUsageStats> {
        return try {
            val conversations = aiRepository.getAllConversations().first()
            val networkMessages = conversations.count { it.responseTime > 1000 }
            val localMessages = conversations.size - networkMessages
            
            val stats = AiUsageStats(
                totalConversations = conversations.size,
                networkMessages = networkMessages,
                localMessages = localMessages,
                averageResponseTime = conversations.map { it.responseTime }.average().toLong()
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * AI消息封装
 */
data class AiMessage(
    val content: String,
    val type: String,
    val isFromNetwork: Boolean,
    val timestamp: Long
)

/**
 * 服务状态信息
 */
data class ServiceStatusInfo(
    val hasApiKey: Boolean,
    val status: AiServiceStatus,
    val canUseLocalFallback: Boolean
)

/**
 * AI使用统计
 */
data class AiUsageStats(
    val totalConversations: Int,
    val networkMessages: Int,
    val localMessages: Int,
    val averageResponseTime: Long
) 