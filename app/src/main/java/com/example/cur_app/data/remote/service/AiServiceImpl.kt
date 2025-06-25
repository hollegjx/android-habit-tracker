package com.example.cur_app.data.remote.service

import com.example.cur_app.data.remote.api.AiApiService
import com.example.cur_app.data.remote.dto.*
import com.example.cur_app.data.remote.error.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI服务实现类
 * 处理与AI API的交互，包含错误处理和降级策略
 */
@Singleton
class AiServiceImpl @Inject constructor(
    private val apiService: AiApiService,
    private val json: Json
) {
    
    // API密钥（实际项目中应从安全配置获取）
    private var apiKey: String = ""
    
    /**
     * 设置API密钥
     */
    fun setApiKey(key: String) {
        apiKey = key
    }
    
    /**
     * 发送AI聊天请求
     */
    suspend fun chatCompletion(request: AiRequestDto): NetworkResult<AiResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isEmpty()) {
                    return@withContext NetworkResult.Error(
                        NetworkError.AiServiceException(
                            "missing_api_key", 
                            null, 
                            "请先配置AI服务API密钥"
                        )
                    )
                }
                
                val authHeader = AiApiService.formatAuthHeader(apiKey)
                val response = apiService.chatCompletions(authHeader, request)
                
                when {
                    response.isSuccessful -> {
                        response.body()?.let { responseBody ->
                            NetworkResult.Success(responseBody)
                        } ?: NetworkResult.Error(
                            NetworkError.ParseException("服务器返回空数据")
                        )
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        val aiError = parseAiError(errorBody)
                        NetworkResult.Error(aiError)
                    }
                }
                
            } catch (e: Exception) {
                NetworkResult.Error(ErrorHandler.handleRetrofitException(e))
            }
        }
    }
    
    /**
     * 健康检查
     */
    suspend fun healthCheck(): NetworkResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isEmpty()) {
                    return@withContext NetworkResult.Error(
                        NetworkError.AiServiceException(
                            "missing_api_key", 
                            null, 
                            "请先配置AI服务API密钥"
                        )
                    )
                }
                
                val authHeader = AiApiService.formatAuthHeader(apiKey)
                val response = apiService.healthCheck(authHeader)
                
                if (response.isSuccessful) {
                    NetworkResult.Success(true)
                } else {
                    NetworkResult.Error(
                        NetworkError.HttpException.fromCode(response.code())
                    )
                }
                
            } catch (e: Exception) {
                NetworkResult.Error(ErrorHandler.handleRetrofitException(e))
            }
        }
    }
    
    /**
     * 生成鼓励消息
     */
    suspend fun generateEncouragement(
        characterType: String,
        habitName: String,
        currentStreak: Int,
        completionRate: Float
    ): NetworkResult<String> {
        val systemPrompt = buildEncouragementPrompt(characterType, habitName, currentStreak, completionRate)
        val request = AiRequestDto(
            messages = listOf(
                MessageDto("system", systemPrompt),
                MessageDto("user", "请给我一句鼓励的话")
            ),
            maxTokens = 100,
            temperature = 0.8
        )
        
        return when (val result = chatCompletion(request)) {
            is NetworkResult.Success -> {
                val message = result.data.choices.firstOrNull()?.message?.content
                if (message.isNullOrBlank()) {
                    NetworkResult.Error(NetworkError.ParseException("AI返回空消息"))
                } else {
                    NetworkResult.Success(message.trim())
                }
            }
            is NetworkResult.Error -> result
            NetworkResult.Loading -> NetworkResult.Loading
        }
    }
    
    /**
     * 生成提醒消息
     */
    suspend fun generateReminder(
        characterType: String,
        habitName: String,
        missedDays: Int
    ): NetworkResult<String> {
        val systemPrompt = buildReminderPrompt(characterType, habitName, missedDays)
        val request = AiRequestDto(
            messages = listOf(
                MessageDto("system", systemPrompt),
                MessageDto("user", "提醒我完成今天的习惯")
            ),
            maxTokens = 80,
            temperature = 0.7
        )
        
        return when (val result = chatCompletion(request)) {
            is NetworkResult.Success -> {
                val message = result.data.choices.firstOrNull()?.message?.content
                if (message.isNullOrBlank()) {
                    NetworkResult.Error(NetworkError.ParseException("AI返回空消息"))
                } else {
                    NetworkResult.Success(message.trim())
                }
            }
            is NetworkResult.Error -> result
            NetworkResult.Loading -> NetworkResult.Loading
        }
    }
    
    /**
     * 解析AI服务错误
     */
    private fun parseAiError(errorBody: String?): NetworkError {
        return try {
            if (errorBody.isNullOrBlank()) {
                NetworkError.UnknownException("服务器返回未知错误")
            } else {
                val errorResponse = json.decodeFromString<AiErrorResponseDto>(errorBody)
                NetworkError.AiServiceException.fromErrorType(
                    errorResponse.error.type,
                    errorResponse.error.code,
                    errorResponse.error.message
                )
            }
        } catch (e: Exception) {
            NetworkError.ParseException("错误信息解析失败")
        }
    }
    
    /**
     * 构建鼓励消息的系统提示
     */
    private fun buildEncouragementPrompt(
        characterType: String,
        habitName: String,
        currentStreak: Int,
        completionRate: Float
    ): String {
        val personality = when (characterType) {
            "encourager" -> "你是一个温暖的鼓励者，总是能找到用户的闪光点，用正能量的话语激励用户。"
            "strict" -> "你是一个严格但关心的导师，会用数据和事实来激励用户，注重结果和改进。"
            "friend" -> "你是用户的好朋友，用轻松幽默的方式鼓励用户，让用户感到轻松愉快。"
            "mentor" -> "你是一个智慧的导师，会给出深思熟虑的建议，帮助用户成长。"
            else -> "你是一个友善的AI助手，会鼓励用户继续坚持良好的习惯。"
        }
        
        return """
            $personality
            
            用户当前的习惯是：$habitName
            已经连续坚持：$currentStreak 天
            总体完成率：${(completionRate * 100).toInt()}%
            
            请根据这些信息，生成一句50字以内的鼓励话语，要体现你的个性特点。
            回复要简洁、温暖、具体，避免套话。
        """.trimIndent()
    }
    
    /**
     * 构建提醒消息的系统提示
     */
    private fun buildReminderPrompt(
        characterType: String,
        habitName: String,
        missedDays: Int
    ): String {
        val personality = when (characterType) {
            "encourager" -> "你是一个温和的提醒者，用关怀的语气提醒用户。"
            "strict" -> "你是一个直接的提醒者，会强调坚持的重要性。"
            "friend" -> "你是用户的好朋友，用友善的方式提醒用户。"
            "mentor" -> "你是一个智慧的导师，会结合道理来提醒用户。"
            else -> "你是一个友善的提醒助手。"
        }
        
        val missedContext = if (missedDays > 0) {
            "用户已经 $missedDays 天没有完成这个习惯了。"
        } else {
            "今天是新的一天。"
        }
        
        return """
            $personality
            
            用户的习惯是：$habitName
            $missedContext
            
            请生成一句40字以内的提醒话语，要体现你的个性特点。
            语气要合适，避免过于严厉或过于轻松。
        """.trimIndent()
    }
} 