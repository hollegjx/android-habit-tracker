package com.example.cur_app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Ping响应数据类
 */
@Serializable
data class PingResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String? = null,
    val server: String? = null
)