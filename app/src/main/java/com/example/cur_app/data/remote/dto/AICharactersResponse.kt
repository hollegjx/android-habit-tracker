package com.example.cur_app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * AI角色列表响应数据类
 */
@Serializable
data class AICharactersResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<AICharacterDto>? = null
)

/**
 * AI角色数据传输对象
 */
@Serializable
data class AICharacterDto(
    val id: String,
    val name: String,
    val description: String,
    val personality: String,
    val systemPrompt: String,
    val model: String,
    val modelConfig: String,
    val isActive: Boolean = true,
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * AI聊天请求数据类
 */
@Serializable
data class AIChatRequest(
    val characterId: String,
    val message: String,
    val conversationId: String? = null
)

/**
 * AI聊天响应数据类
 */
@Serializable
data class AIChatResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AIChatData? = null
)

@Serializable
data class AIChatData(
    val response: String,
    val conversationId: String,
    val characterId: String,
    val timestamp: String
)

/**
 * 更新AI角色请求数据类
 */
@Serializable
data class UpdateAICharacterRequest(
    val name: String? = null,
    val description: String? = null,
    val personality: String? = null,
    val systemPrompt: String? = null,
    val model: String? = null,
    val modelConfig: String? = null,
    val isActive: Boolean? = null
)

/**
 * AI角色响应数据类
 */
@Serializable
data class AICharacterResponse(
    val success: Boolean,
    val message: String? = null,
    val data: AICharacterDto? = null
)