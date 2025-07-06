package com.example.cur_app.data.ai

import com.example.cur_app.data.database.entities.AiCharacterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI API 客户端
 * 简化的AI API调用实现，直接与zetatechs API通信
 */
@Singleton
class AiApiClient @Inject constructor() {
    
    private val apiKey = "sk-dNbz8p92k2M1hIhx03F43cFdCfF94d158f14675c4d04107d"
    private val baseUrl = "https://api.zetatechs.com/v1/chat/completions"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    /**
     * 调用AI聊天API
     */
    suspend fun chat(
        userMessage: String,
        characterType: String = "friend",
        conversationHistory: List<String> = emptyList()
    ): AiResponse = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AiApiClient", "🤖 开始AI聊天请求")
            android.util.Log.d("AiApiClient", "🤖 用户消息: $userMessage")
            android.util.Log.d("AiApiClient", "🤖 角色类型: $characterType")
            
            val systemPrompt = getSystemPrompt(characterType)
            val messages = buildMessages(systemPrompt, userMessage, conversationHistory)
            
            val requestBody = JSONObject().apply {
                put("model", "gpt-4.1")
                put("messages", messages)
                put("temperature", 0.9)
                put("max_tokens", 150)
            }
            
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                android.util.Log.d("AiApiClient", "🤖 API响应成功")
                parseResponse(responseBody)
            } else {
                android.util.Log.e("AiApiClient", "🤖 API请求失败: ${response.code}")
                AiResponse.Error("API请求失败: ${response.code}")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AiApiClient", "🤖 API调用异常: ${e.message}", e)
            AiResponse.Error("网络连接失败: ${e.message}")
        }
    }
    
    /**
     * 调用AI聊天API（支持具体角色信息）
     */
    suspend fun chatWithCharacter(
        userMessage: String,
        character: AiCharacterEntity,
        conversationHistory: List<String> = emptyList()
    ): AiResponse = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("AiApiClient", "🤖 开始AI聊天请求（具体角色）")
            android.util.Log.d("AiApiClient", "🤖 用户消息: $userMessage")
            android.util.Log.d("AiApiClient", "🤖 角色名称: ${character.name}")
            
            val systemPrompt = getCharacterSystemPrompt(character)
            android.util.Log.d("AiApiClient", "🤖 生成的角色系统提示词: $systemPrompt")
            val messages = buildMessages(systemPrompt, userMessage, conversationHistory)
            
            val requestBody = JSONObject().apply {
                put("model", "gpt-4.1")
                put("messages", messages)
                put("temperature", 0.9)
                put("max_tokens", 150)
            }
            
            android.util.Log.d("AiApiClient", "🤖 完整请求JSON: ${requestBody.toString(2)}")
            
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                android.util.Log.d("AiApiClient", "🤖 API响应成功（具体角色）")
                parseResponse(responseBody)
            } else {
                android.util.Log.e("AiApiClient", "🤖 API请求失败: ${response.code}")
                AiResponse.Error("API请求失败: ${response.code}")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AiApiClient", "🤖 API调用异常: ${e.message}", e)
            AiResponse.Error("网络连接失败: ${e.message}")
        }
    }
    
    /**
     * 构建消息数组
     */
    private fun buildMessages(
        systemPrompt: String,
        userMessage: String,
        conversationHistory: List<String>
    ): JSONArray {
        val messages = JSONArray()
        
        // 添加系统提示
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        
        // 添加对话历史（最近5轮对话）
        val recentHistory = conversationHistory.takeLast(10)
        recentHistory.forEachIndexed { index, message ->
            val role = if (index % 2 == 0) "user" else "assistant"
            messages.put(JSONObject().apply {
                put("role", role)
                put("content", message)
            })
        }
        
        // 添加当前用户消息
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })
        
        return messages
    }
    
    /**
     * 解析API响应
     */
    private fun parseResponse(responseBody: String?): AiResponse {
        return try {
            if (responseBody.isNullOrEmpty()) {
                return AiResponse.Error("服务器返回空响应")
            }
            
            val json = JSONObject(responseBody)
            val choices = json.getJSONArray("choices")
            
            if (choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                val content = message.getString("content")
                
                android.util.Log.d("AiApiClient", "🤖 AI回复: $content")
                AiResponse.Success(content.trim())
            } else {
                AiResponse.Error("AI服务返回空回复")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AiApiClient", "🤖 解析响应失败: ${e.message}", e)
            AiResponse.Error("解析AI响应失败")
        }
    }
    
    /**
     * 获取系统提示词
     */
    private fun getSystemPrompt(characterType: String): String {
        return when (characterType) {
            "encourager" -> """
                你是一个温暖、鼓励型的AI助手，名字叫"暖心"。
                你的性格特点：
                - 总是能找到用户的闪光点，给予正面反馈
                - 用温暖的话语鼓励用户
                - 关注用户的情绪和感受
                - 善于发现和赞美用户的进步
                
                作为一个习惯追踪应用的AI助手，你要：
                1. 与用户进行自然的对话
                2. 根据你的个性回复用户的问题
                3. 适当时候关心用户的习惯情况
                4. 回复要简洁明了，一般控制在50-100字
                5. 保持你的角色特色，但不要过度表演
                
                记住，你是用户的习惯养成伙伴，要像真正的朋友一样与他们交流。
            """.trimIndent()
            
            "strict" -> """
                你是一个严格但关心的AI导师，名字叫"智者"。
                你的性格特点：
                - 注重纪律和执行力
                - 用数据和事实说话
                - 直接但不失关怀
                - 强调目标和成果的重要性
                
                作为一个习惯追踪应用的AI助手，你要：
                1. 与用户进行自然的对话
                2. 根据你的个性回复用户的问题
                3. 适当时候关心用户的习惯情况
                4. 回复要简洁明了，一般控制在50-100字
                5. 保持你的角色特色，但不要过度表演
                
                记住，你是用户的习惯养成伙伴，要像真正的导师一样指导他们。
            """.trimIndent()
            
            "friend" -> """
                你是一个轻松幽默的AI朋友，名字叫"小鸣"。
                你的性格特点：
                - 轻松活泼，善于调节气氛
                - 会用幽默的方式鼓励用户
                - 像真正的朋友一样关心用户
                - 使用较为随意的语言风格
                
                作为一个习惯追踪应用的AI助手，你要：
                1. 与用户进行自然的对话
                2. 根据你的个性回复用户的问题
                3. 适当时候关心用户的习惯情况
                4. 回复要简洁明了，一般控制在50-100字
                5. 保持你的角色特色，但不要过度表演
                
                记住，你是用户的习惯养成伙伴，要像真正的朋友一样与他们交流。
            """.trimIndent()
            
            "mentor" -> """
                你是一个智慧温和的AI导师，名字叫"智心"。
                你的性格特点：
                - 具有深度的洞察力和经验
                - 善于给出深思熟虑的建议
                - 关注用户的长期成长
                - 用智慧的话语引导用户思考
                
                作为一个习惯追踪应用的AI助手，你要：
                1. 与用户进行自然的对话
                2. 根据你的个性回复用户的问题
                3. 适当时候关心用户的习惯情况
                4. 回复要简洁明了，一般控制在50-100字
                5. 保持你的角色特色，但不要过度表演
                
                记住，你是用户的习惯养成伙伴，要像真正的导师一样引导他们。
            """.trimIndent()
            
            else -> """
                你是一个友善智能的AI助手，名字叫"小助"。
                你的性格特点：
                - 友善耐心，愿意倾听
                - 给予用户有用的建议和支持
                - 保持乐观积极的态度
                - 适度关注用户的习惯养成
                
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
    
    /**
     * 生成角色系统提示词
     */
    private fun getCharacterSystemPrompt(character: AiCharacterEntity): String {
        return """
            你是一个名叫"${character.name}"的AI助手。
            
            你的性格特点：
            ${character.personality}
            
            你的说话风格：
            ${character.speakingStyle}
            
            你的技能和擅长领域：
            ${character.skills.removeSurrounding("[", "]").replace("\"", "").split(",").joinToString("\n") { "- ${it.trim()}" }}
            
            作为一个习惯追踪应用的AI助手，你要：
            1. 与用户进行自然的对话
            2. 根据你的个性和说话风格回复用户的问题
            3. 适当时候关心用户的习惯情况
            4. 回复要简洁明了，一般控制在50-100字
            5. 始终保持你的角色身份，以"${character.name}"的名字与用户交流
            6. 体现你的个性特点，但不要过度表演
            
            记住，你是用户的习惯养成伙伴，要像真正的${character.name}一样与他们交流。
        """.trimIndent()
    }
}

/**
 * AI响应结果
 */
sealed class AiResponse {
    data class Success(val message: String) : AiResponse()
    data class Error(val error: String) : AiResponse()
}