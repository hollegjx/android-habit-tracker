package com.example.cur_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AI服务响应数据传输对象
 */
@Serializable
data class AiResponseDto(
    @SerialName("id") 
    val id: String,
    
    @SerialName("object") 
    val objectType: String,
    
    @SerialName("created") 
    val created: Long,
    
    @SerialName("model") 
    val model: String,
    
    @SerialName("choices") 
    val choices: List<ChoiceDto>,
    
    @SerialName("usage") 
    val usage: UsageDto?
)

@Serializable
data class ChoiceDto(
    @SerialName("index") 
    val index: Int,
    
    @SerialName("message") 
    val message: MessageDto,
    
    @SerialName("finish_reason") 
    val finishReason: String?
)

@Serializable
data class UsageDto(
    @SerialName("prompt_tokens") 
    val promptTokens: Int,
    
    @SerialName("completion_tokens") 
    val completionTokens: Int,
    
    @SerialName("total_tokens") 
    val totalTokens: Int
)

/**
 * AI错误响应
 */
@Serializable
data class AiErrorResponseDto(
    @SerialName("error") 
    val error: ErrorDetailDto
)

@Serializable
data class ErrorDetailDto(
    @SerialName("message") 
    val message: String,
    
    @SerialName("type") 
    val type: String,
    
    @SerialName("param") 
    val param: String? = null,
    
    @SerialName("code") 
    val code: String? = null
) 