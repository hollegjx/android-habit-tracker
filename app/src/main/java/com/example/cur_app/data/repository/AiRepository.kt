package com.example.cur_app.data.repository

import com.example.cur_app.data.database.dao.AiCharacterDao
import com.example.cur_app.data.database.dao.AiConversationDao
import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.data.database.entities.AiConversationEntity
import com.example.cur_app.data.remote.error.NetworkResult
import com.example.cur_app.data.remote.monitor.NetworkMonitor
import com.example.cur_app.data.remote.service.AiServiceImpl
import com.example.cur_app.data.remote.service.LocalFallbackService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI服务数据仓库
 * 协调网络AI服务和本地数据，提供智能降级和缓存策略
 */
@Singleton
class AiRepository @Inject constructor(
    private val aiCharacterDao: AiCharacterDao,
    private val aiConversationDao: AiConversationDao,
    private val aiService: AiServiceImpl,
    private val localFallbackService: LocalFallbackService,
    private val networkMonitor: NetworkMonitor
) {
    
    // ========== AI角色管理 ==========
    
    /**
     * 获取所有活跃的AI角色
     */
    fun getAllActiveCharacters(): Flow<List<AiCharacterEntity>> {
        return aiCharacterDao.getAllActiveCharacters()
    }
    
    /**
     * 获取当前选中的AI角色
     */
    suspend fun getSelectedCharacter(): AiCharacterEntity? {
        return aiCharacterDao.getSelectedCharacter()
    }
    
    /**
     * 根据类型获取AI角色
     */
    fun getCharactersByType(type: String): Flow<List<AiCharacterEntity>> {
        return aiCharacterDao.getCharactersByType(type)
    }
    
    /**
     * 选择AI角色
     */
    suspend fun selectCharacter(characterId: Long) {
        aiCharacterDao.selectCharacter(characterId)
    }
    
    /**
     * 创建新的AI角色
     */
    suspend fun createCharacter(character: AiCharacterEntity): Long {
        return aiCharacterDao.insertCharacter(character)
    }
    
    /**
     * 更新AI角色
     */
    suspend fun updateCharacter(character: AiCharacterEntity) {
        aiCharacterDao.updateCharacter(character)
    }
    
    /**
     * 增加角色使用次数
     */
    suspend fun incrementCharacterUsage(characterId: Long) {
        aiCharacterDao.incrementUsage(characterId)
    }
    
    // ========== AI对话管理 ==========
    
    /**
     * 获取所有对话记录
     */
    fun getAllConversations(): Flow<List<AiConversationEntity>> {
        return aiConversationDao.getAllConversations()
    }
    
    /**
     * 获取指定角色的对话记录
     */
    fun getConversationsByCharacter(characterId: Long): Flow<List<AiConversationEntity>> {
        return aiConversationDao.getConversationsByCharacter(characterId)
    }
    
    /**
     * 获取与习惯相关的对话记录
     */
    fun getConversationsByHabit(habitId: Long): Flow<List<AiConversationEntity>> {
        return aiConversationDao.getConversationsByHabit(habitId)
    }
    
    /**
     * 获取未读对话数量
     */
    suspend fun getUnreadCount(): Int {
        return aiConversationDao.getUnreadCount()
    }
    
    /**
     * 标记对话为已读
     */
    suspend fun markConversationAsRead(conversationId: Long) {
        aiConversationDao.markAsRead(conversationId)
    }
    
    /**
     * 对AI回复进行评分
     */
    suspend fun rateConversation(conversationId: Long, rating: Int) {
        aiConversationDao.rateConversation(conversationId, rating)
    }
    
    // ========== AI智能交互 ==========
    
    /**
     * 生成鼓励消息
     */
    suspend fun generateEncouragement(
        habitName: String,
        currentStreak: Int,
        completionRate: Float,
        characterId: Long? = null
    ): AiMessageResult {
        val character = characterId?.let { aiCharacterDao.getCharacterById(it) }
            ?: getSelectedCharacter()
            ?: return AiMessageResult.Error("未找到可用的AI角色")
        
        val characterType = character.type
        
        // 检查网络状态并选择服务
        val result = if (isNetworkAvailable()) {
            // 尝试使用在线AI服务
            when (val networkResult = aiService.generateEncouragement(
                characterType, habitName, currentStreak, completionRate
            )) {
                is NetworkResult.Success -> AiMessageResult.Success(
                    message = networkResult.data,
                    isFromNetwork = true
                )
                is NetworkResult.Error -> {
                    // 网络服务失败，降级到本地服务
                    val localResult = localFallbackService.generateLocalEncouragement(
                        characterType, habitName, currentStreak, completionRate
                    )
                    when (localResult) {
                        is NetworkResult.Success -> AiMessageResult.Success(
                            message = localResult.data,
                            isFromNetwork = false
                        )
                        else -> AiMessageResult.Error("生成鼓励消息失败")
                    }
                }
                NetworkResult.Loading -> AiMessageResult.Loading
            }
        } else {
            // 直接使用本地服务
            val localResult = localFallbackService.generateLocalEncouragement(
                characterType, habitName, currentStreak, completionRate
            )
            when (localResult) {
                is NetworkResult.Success -> AiMessageResult.Success(
                    message = localResult.data,
                    isFromNetwork = false
                )
                else -> AiMessageResult.Error("生成鼓励消息失败")
            }
        }
        
        // 记录对话历史
        if (result is AiMessageResult.Success) {
            saveConversation(
                characterId = character.id,
                aiMessage = result.message,
                messageType = "encouragement",
                isFromNetwork = result.isFromNetwork
            )
            
            // 增加角色使用次数
            incrementCharacterUsage(character.id)
        }
        
        return result
    }
    
    /**
     * 生成提醒消息
     */
    suspend fun generateReminder(
        habitName: String,
        missedDays: Int,
        characterId: Long? = null
    ): AiMessageResult {
        val character = characterId?.let { aiCharacterDao.getCharacterById(it) }
            ?: getSelectedCharacter()
            ?: return AiMessageResult.Error("未找到可用的AI角色")
        
        val characterType = character.type
        
        // 检查网络状态并选择服务
        val result = if (isNetworkAvailable()) {
            // 尝试使用在线AI服务
            when (val networkResult = aiService.generateReminder(characterType, habitName, missedDays)) {
                is NetworkResult.Success -> AiMessageResult.Success(
                    message = networkResult.data,
                    isFromNetwork = true
                )
                is NetworkResult.Error -> {
                    // 网络服务失败，降级到本地服务
                    val localResult = localFallbackService.generateLocalReminder(
                        characterType, habitName, missedDays
                    )
                    when (localResult) {
                        is NetworkResult.Success -> AiMessageResult.Success(
                            message = localResult.data,
                            isFromNetwork = false
                        )
                        else -> AiMessageResult.Error("生成提醒消息失败")
                    }
                }
                NetworkResult.Loading -> AiMessageResult.Loading
            }
        } else {
            // 直接使用本地服务
            val localResult = localFallbackService.generateLocalReminder(
                characterType, habitName, missedDays
            )
            when (localResult) {
                is NetworkResult.Success -> AiMessageResult.Success(
                    message = localResult.data,
                    isFromNetwork = false
                )
                else -> AiMessageResult.Error("生成提醒消息失败")
            }
        }
        
        // 记录对话历史
        if (result is AiMessageResult.Success) {
            saveConversation(
                characterId = character.id,
                aiMessage = result.message,
                messageType = "reminder",
                isFromNetwork = result.isFromNetwork
            )
            
            // 增加角色使用次数
            incrementCharacterUsage(character.id)
        }
        
        return result
    }
    
    /**
     * 生成庆祝消息
     */
    suspend fun generateCelebration(
        habitName: String,
        achievement: String,
        characterId: Long? = null
    ): AiMessageResult {
        val character = characterId?.let { aiCharacterDao.getCharacterById(it) }
            ?: getSelectedCharacter()
            ?: return AiMessageResult.Error("未找到可用的AI角色")
        
        val characterType = character.type
        
        // 优先使用本地庆祝消息（响应更快）
        val localResult = localFallbackService.generateLocalCelebration(
            characterType, habitName, achievement
        )
        
        val result = when (localResult) {
            is NetworkResult.Success -> AiMessageResult.Success(
                message = localResult.data,
                isFromNetwork = false
            )
            else -> AiMessageResult.Error("生成庆祝消息失败")
        }
        
        // 记录对话历史
        if (result is AiMessageResult.Success) {
            saveConversation(
                characterId = character.id,
                aiMessage = result.message,
                messageType = "celebration",
                isFromNetwork = result.isFromNetwork
            )
            
            // 增加角色使用次数
            incrementCharacterUsage(character.id)
        }
        
        return result
    }
    
    /**
     * 设置API密钥
     */
    fun setApiKey(apiKey: String?) {
        if (apiKey != null) {
            aiService.setApiKey(apiKey)
        }
    }
    
    /**
     * 健康检查AI服务
     */
    suspend fun healthCheck(): AiServiceStatus {
        return when (val result = aiService.healthCheck()) {
            is NetworkResult.Success -> AiServiceStatus.Available
            is NetworkResult.Error -> AiServiceStatus.Unavailable(result.error.message ?: "未知错误")
            NetworkResult.Loading -> AiServiceStatus.Checking
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 检查网络是否可用
     */
    private suspend fun isNetworkAvailable(): Boolean {
        return try {
            networkMonitor.networkStatus.first().isConnected
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 保存对话记录
     */
    private suspend fun saveConversation(
        characterId: Long,
        userMessage: String = "",
        aiMessage: String,
        messageType: String,
        habitId: Long? = null,
        isFromNetwork: Boolean = false
    ): Long {
        val conversation = AiConversationEntity(
            characterId = characterId,
            habitId = habitId,
            userMessage = userMessage,
            aiMessage = aiMessage,
            messageType = messageType,
            timestamp = System.currentTimeMillis(),
            sessionId = generateSessionId(),
            hasAudio = false, // TODO: 后续支持TTS
            emotionalTone = inferEmotionalTone(messageType),
            responseTime = if (isFromNetwork) 2000L else 100L, // 模拟响应时间
            tokenCount = aiMessage.length / 4 // 简单估算token数
        )
        
        return aiConversationDao.insertConversation(conversation)
    }
    
    /**
     * 生成会话ID
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * 推断情感色调
     */
    private fun inferEmotionalTone(messageType: String): String {
        return when (messageType) {
            "encouragement" -> "positive"
            "celebration" -> "excited"
            "reminder" -> "neutral"
            "greeting" -> "positive"
            else -> "neutral"
        }
    }
}

/**
 * AI消息结果封装
 */
sealed class AiMessageResult {
    data class Success(
        val message: String,
        val isFromNetwork: Boolean
    ) : AiMessageResult()
    
    data class Error(val message: String) : AiMessageResult()
    data object Loading : AiMessageResult()
}

/**
 * AI服务状态
 */
sealed class AiServiceStatus {
    data object Available : AiServiceStatus()
    data class Unavailable(val reason: String) : AiServiceStatus()
    data object Checking : AiServiceStatus()
} 