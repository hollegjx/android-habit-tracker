package com.example.cur_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 服务器AI角色数据传输对象
 * 对应服务器端ai_characters表的字段
 */
@Serializable
data class ServerAiCharacterDto(
    @SerialName("character_id")
    val character_id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String,
    
    @SerialName("personality")
    val personality: String,
    
    @SerialName("avatar_url")
    val avatar_url: String? = null,
    
    @SerialName("system_prompt")
    val system_prompt: String? = null,
    
    @SerialName("model")
    val model: String? = null,
    
    @SerialName("model_config")
    val model_config: String? = null,
    
    @SerialName("is_active")
    val is_active: Boolean = true,
    
    @SerialName("created_at")
    val created_at: String? = null,
    
    @SerialName("updated_at")
    val updated_at: String? = null
)

/**
 * 服务器AI角色响应包装
 */
@Serializable
data class AiCharactersResponse(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("data")
    val data: List<ServerAiCharacterDto>,
    
    @SerialName("message")
    val message: String? = null
)