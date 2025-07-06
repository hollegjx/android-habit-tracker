package com.example.cur_app.data.repository

import com.example.cur_app.data.ai.AiService
import com.example.cur_app.data.database.dao.AiCharacterDao
import com.example.cur_app.data.database.dao.AiConversationDao
import com.example.cur_app.data.database.entities.AiCharacterEntity
import com.example.cur_app.data.database.entities.AiConversationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI服务数据仓库
 * 简化的AI服务封装，统一管理AI功能和数据存储
 */
@Singleton
class AiRepository @Inject constructor(
    private val aiService: AiService,
    private val aiCharacterDao: AiCharacterDao,
    private val aiConversationDao: AiConversationDao
) {
    
    init {
        android.util.Log.d("AiRepository", "🎯 新的AiRepository初始化完成")
    }
    
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
        
        return try {
            val encouragement = aiService.generateEncouragement(
                habitName, currentStreak, completionRate, character.type
            )
            
            val result = AiMessageResult.Success(encouragement, true)
            
            // 记录对话历史
            saveConversation(
                characterId = character.id,
                aiMessage = encouragement,
                messageType = "encouragement"
            )
            
            incrementCharacterUsage(character.id)
            result
            
        } catch (e: Exception) {
            AiMessageResult.Error("生成鼓励消息失败: ${e.message}")
        }
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
        
        return try {
            val reminder = aiService.generateReminder(habitName, missedDays, character.type)
            
            val result = AiMessageResult.Success(reminder, true)
            
            // 记录对话历史
            saveConversation(
                characterId = character.id,
                aiMessage = reminder,
                messageType = "reminder"
            )
            
            incrementCharacterUsage(character.id)
            result
            
        } catch (e: Exception) {
            AiMessageResult.Error("生成提醒消息失败: ${e.message}")
        }
    }
    
    /**
     * AI聊天对话
     */
    suspend fun chatWithAi(
        userMessage: String,
        characterId: String? = null,
        conversationHistory: List<String> = emptyList()
    ): AiMessageResult {
        android.util.Log.d("AiRepository", "🎯 开始AI聊天")
        android.util.Log.d("AiRepository", "🎯 用户消息: $userMessage")
        android.util.Log.d("AiRepository", "🎯 角色ID: $characterId")
        
        val character = characterId?.let { aiCharacterDao.getCharacterByCharacterId(it) }
            ?: getSelectedCharacter()
            ?: return AiMessageResult.Error("未找到可用的AI角色")
        
        android.util.Log.d("AiRepository", "🎯 获取到的角色信息:")
        android.util.Log.d("AiRepository", "🎯 - 角色ID: ${character.id}")
        android.util.Log.d("AiRepository", "🎯 - 角色名称: ${character.name}")
        android.util.Log.d("AiRepository", "🎯 - 角色类型: ${character.type}")
        android.util.Log.d("AiRepository", "🎯 - 性格描述: ${character.personality}")
        android.util.Log.d("AiRepository", "🎯 - 说话风格: ${character.speakingStyle}")
        
        val characterType = character.type
        android.util.Log.d("AiRepository", "🎯 使用角色类型: $characterType")
        
        return try {
            // 调用新的角色聊天服务
            val aiResponse = aiService.chatWithCharacter(userMessage, character, conversationHistory)
            android.util.Log.d("AiRepository", "🎯 AI回复: $aiResponse")
            
            val result = AiMessageResult.Success(
                message = aiResponse,
                isFromNetwork = true // 简化：不再区分网络/本地，由AiService内部处理
            )
            
            // 记录对话历史
            saveConversation(
                characterId = character.id,
                userMessage = userMessage,
                aiMessage = aiResponse,
                messageType = "chat"
            )
            
            // 增加角色使用次数
            incrementCharacterUsage(character.id)
            
            result
            
        } catch (e: Exception) {
            android.util.Log.e("AiRepository", "🎯 AI聊天失败: ${e.message}", e)
            AiMessageResult.Error("AI聊天失败: ${e.message}")
        }
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
        
        return try {
            val celebration = aiService.generateCelebration(habitName, achievement, character.type)
            
            val result = AiMessageResult.Success(celebration, true)
            
            // 记录对话历史
            saveConversation(
                characterId = character.id,
                aiMessage = celebration,
                messageType = "celebration"
            )
            
            incrementCharacterUsage(character.id)
            result
            
        } catch (e: Exception) {
            AiMessageResult.Error("生成庆祝消息失败: ${e.message}")
        }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 保存对话记录
     */
    private suspend fun saveConversation(
        characterId: Long,
        userMessage: String = "",
        aiMessage: String,
        messageType: String,
        habitId: Long? = null
    ): Long {
        val conversation = AiConversationEntity(
            characterId = characterId,
            habitId = habitId,
            userMessage = userMessage,
            aiMessage = aiMessage,
            messageType = messageType,
            timestamp = System.currentTimeMillis(),
            sessionId = generateSessionId(),
            hasAudio = false,
            emotionalTone = inferEmotionalTone(messageType),
            responseTime = 1500L, // 简化：统一响应时间
            tokenCount = aiMessage.length / 4
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