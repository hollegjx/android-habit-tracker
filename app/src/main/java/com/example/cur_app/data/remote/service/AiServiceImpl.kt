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
    private val instanceId = System.currentTimeMillis().toString().takeLast(4)
    
    init {
        android.util.Log.e("AiServiceImpl", "🔴 AiServiceImpl[$instanceId] 初始化")
    }
    
    /**
     * 设置API密钥
     */
    fun setApiKey(key: String) {
        android.util.Log.e("AiServiceImpl", "🔴 AiServiceImpl[$instanceId] 收到API密钥: ${key.take(10)}...")
        apiKey = key
        android.util.Log.e("AiServiceImpl", "🔴 AiServiceImpl[$instanceId] 当前API密钥长度: ${apiKey.length}")
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
     * AI聊天对话
     */
    suspend fun chatWithAi(
        characterType: String,
        userMessage: String,
        conversationHistory: List<String> = emptyList()
    ): NetworkResult<String> {
        android.util.Log.e("AiServiceImpl", "🔴 AiServiceImpl[$instanceId] chatWithAi调用 - API密钥状态: ${if(apiKey.isEmpty()) "空" else "已设置(${apiKey.length}字符)"}")
        
        val systemPrompt = buildChatPrompt(characterType)
        val messages = mutableListOf<MessageDto>()
        
        // 添加系统提示
        messages.add(MessageDto("system", systemPrompt))
        
        // 添加对话历史（限制最近5轮对话）
        val recentHistory = conversationHistory.takeLast(10) // 最多5轮对话
        recentHistory.forEachIndexed { index, message ->
            val role = if (index % 2 == 0) "user" else "assistant"
            messages.add(MessageDto(role, message))
        }
        
        // 添加当前用户消息
        messages.add(MessageDto("user", userMessage))
        
        val request = AiRequestDto(
            messages = messages,
            maxTokens = 150,
            temperature = 0.9
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
    
    /**
     * 构建聊天对话的系统提示
     */
    private fun buildChatPrompt(characterType: String): String {
        val personality = when (characterType) {
            "encourager" -> """
                你是一个温暖、鼓励型的AI助手，名字叫“暖心”。
                你的性格特点：
                - 总是能找到用户的闪光点，给予正面反馈
                - 用温暖的话语鼓励用户
                - 关注用户的情绪和感受
                - 善于发现和赞美用户的进步
            """
            "strict" -> """
                你是一个严格但关心的AI导师，名字叫“智者”。
                你的性格特点：
                - 注重纪律和执行力
                - 用数据和事实说话
                - 直接但不失关怀
                - 强调目标和成果的重要性
            """
            "friend" -> """
                你是一个轻松幽默的AI朋友，名字叫“小鸣”。
                你的性格特点：
                - 轻松活泼，善于调节气氛
                - 会用幽默的方式鼓励用户
                - 像真正的朋友一样关心用户
                - 使用较为随意的语言风格
            """
            "mentor" -> """
                你是一个智慧温和的AI导师，名字叫“智心”。
                你的性格特点：
                - 具有深度的洞察力和经验
                - 善于给出深思熟虑的建议
                - 关注用户的长期成长
                - 用智慧的话语引导用户思考
            """
            else -> """
                你是一个友善智能的AI助手，名字叫“小三”。
                你的性格特点：
                - 友善耐心，愿意倾听
                - 给予用户有用的建议和支持
                - 保持乐观积极的态度
                - 适度关注用户的习惯养成
            """
        }
        
        return """
            $personality
            
            作为一个习惯追踪应用的AI助手，你要：
            1. 与用户进行自然的对话
            2. 根据你的个性回复用户的问题
            3. 适当时候关心用户的习惯情况
            4. 回复要简洁明了，一般控制在50-100字
            5. 保持你的角色特色，但不要过度表演
            
            记住，你是用户的习惯养成伙伴，要像真正的朋友一样与他们交流。
        """.trimIndent()
    }
} 