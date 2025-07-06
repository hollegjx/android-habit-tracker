package com.example.cur_app.domain.usecase

import com.example.cur_app.data.repository.AiRepository
import com.example.cur_app.data.repository.AiMessageResult
import com.example.cur_app.data.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI功能用例
 * 简化的AI业务逻辑处理
 */
@Singleton
class AiUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository
) {
    
    /**
     * AI聊天
     */
    suspend fun chatWithAi(
        message: String,
        characterId: String? = null,
        conversationHistory: List<String> = emptyList()
    ): Result<String> {
        return try {
            when (val result = aiRepository.chatWithAi(message, characterId, conversationHistory)) {
                is AiMessageResult.Success -> Result.success(result.message)
                is AiMessageResult.Error -> Result.failure(Exception(result.message))
                AiMessageResult.Loading -> Result.failure(Exception("正在处理中..."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成鼓励消息
     */
    suspend fun generateEncouragement(
        habitName: String,
        currentStreak: Int,
        completionRate: Float,
        characterId: Long? = null
    ): Result<String> {
        return try {
            when (val result = aiRepository.generateEncouragement(habitName, currentStreak, completionRate, characterId)) {
                is AiMessageResult.Success -> Result.success(result.message)
                is AiMessageResult.Error -> Result.failure(Exception(result.message))
                AiMessageResult.Loading -> Result.failure(Exception("正在生成中..."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成提醒消息
     */
    suspend fun generateReminder(
        habitName: String,
        missedDays: Int,
        characterId: Long? = null
    ): Result<String> {
        return try {
            when (val result = aiRepository.generateReminder(habitName, missedDays, characterId)) {
                is AiMessageResult.Success -> Result.success(result.message)
                is AiMessageResult.Error -> Result.failure(Exception(result.message))
                AiMessageResult.Loading -> Result.failure(Exception("正在生成中..."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成庆祝消息
     */
    suspend fun generateCelebration(
        habitName: String,
        achievement: String,
        characterId: Long? = null
    ): Result<String> {
        return try {
            when (val result = aiRepository.generateCelebration(habitName, achievement, characterId)) {
                is AiMessageResult.Success -> Result.success(result.message)
                is AiMessageResult.Error -> Result.failure(Exception(result.message))
                AiMessageResult.Loading -> Result.failure(Exception("正在生成中..."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}