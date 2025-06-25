package com.example.cur_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AI服务请求数据传输对象
 */
@Serializable
data class AiRequestDto(
    @SerialName("model") 
    val model: String = "gpt-3.5-turbo",
    
    @SerialName("messages") 
    val messages: List<MessageDto>,
    
    @SerialName("max_tokens") 
    val maxTokens: Int = 150,
    
    @SerialName("temperature") 
    val temperature: Double = 0.7,
    
    @SerialName("user") 
    val userId: String? = null
)

@Serializable
data class MessageDto(
    @SerialName("role") 
    val role: String, // "system", "user", "assistant"
    
    @SerialName("content") 
    val content: String
) 