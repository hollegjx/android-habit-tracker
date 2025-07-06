package com.example.cur_app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * 健康检查响应数据类
 */
@Serializable
data class HealthCheckResponse(
    val status: String,
    val timestamp: String? = null,
    val uptime: Double? = null,
    val message: String? = null
)