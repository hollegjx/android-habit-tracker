package com.example.cur_app.data.remote.service

import com.example.cur_app.data.remote.api.ChatApiService
import com.example.cur_app.data.remote.api.AiChatRequest
import com.example.cur_app.data.remote.error.NetworkResult
import com.example.cur_app.data.remote.error.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务器聊天服务实现类
 * 调用服务器的AI聊天API
 */
@Singleton
class ServerChatService @Inject constructor(
    private val chatApiService: ChatApiService
) {
    
    /**
     * 与AI角色聊天
     */
    suspend fun chatWithAi(
        characterId: String,
        userMessage: String,
        conversationHistory: List<String> = emptyList()
    ): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("ServerChatService", "🔵 调用服务器AI聊天API: characterId=$characterId, message=$userMessage")
                
                val request = AiChatRequest(
                    characterId = characterId,
                    message = userMessage
                )
                
                val response = chatApiService.chatWithAI(request)
                
                when {
                    response.isSuccessful -> {
                        response.body()?.let { responseBody ->
                            if (responseBody.success && responseBody.data != null) {
                                android.util.Log.d("ServerChatService", "🔵 服务器AI聊天成功: ${responseBody.data.message}")
                                NetworkResult.Success(responseBody.data.message)
                            } else {
                                android.util.Log.e("ServerChatService", "🔵 服务器AI聊天失败: ${responseBody.message}")
                                NetworkResult.Error(
                                    NetworkError.AiServiceException(
                                        "server_error",
                                        null,
                                        responseBody.message ?: "服务器返回错误"
                                    )
                                )
                            }
                        } ?: NetworkResult.Error(
                            NetworkError.ParseException("服务器返回空数据")
                        )
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("ServerChatService", "🔵 HTTP错误: ${response.code()}, body: $errorBody")
                        NetworkResult.Error(
                            NetworkError.HttpException.fromCode(response.code())
                        )
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ServerChatService", "🔵 请求异常: ${e.message}", e)
                NetworkResult.Error(
                    NetworkError.NetworkException(e.message ?: "网络请求失败")
                )
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
        // 构建鼓励消息请求
        val encouragementMessage = "请为我的习惯「$habitName」生成一句鼓励的话。我已经连续坚持了${currentStreak}天，完成率是${(completionRate * 100).toInt()}%。"
        
        return chatWithAi(
            characterId = characterType,
            userMessage = encouragementMessage
        )
    }
    
    /**
     * 生成提醒消息
     */
    suspend fun generateReminder(
        characterType: String,
        habitName: String,
        missedDays: Int
    ): NetworkResult<String> {
        // 构建提醒消息请求
        val reminderMessage = if (missedDays > 0) {
            "我已经$missedDays 天没有完成「$habitName」这个习惯了，请提醒我今天要完成。"
        } else {
            "请提醒我今天要完成「$habitName」这个习惯。"
        }
        
        return chatWithAi(
            characterId = characterType,
            userMessage = reminderMessage
        )
    }
}