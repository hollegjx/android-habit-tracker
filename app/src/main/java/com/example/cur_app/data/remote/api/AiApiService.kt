package com.example.cur_app.data.remote.api

import com.example.cur_app.data.remote.dto.AiRequestDto
import com.example.cur_app.data.remote.dto.AiResponseDto
import retrofit2.Response
import retrofit2.http.*

/**
 * AI服务API接口
 * 支持OpenAI兼容的API调用
 */
interface AiApiService {
    
    /**
     * OpenAI Chat Completions接口
     */
    @POST("v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: AiRequestDto
    ): Response<AiResponseDto>
    
    /**
     * 健康检查接口
     */
    @GET("v1/models")
    suspend fun healthCheck(
        @Header("Authorization") authorization: String
    ): Response<Any>
    
    companion object {
        // OpenAI官方API地址
        const val OPENAI_BASE_URL = "https://api.openai.com/"
        
        // 国内中转服务地址（示例）
        const val CHINA_PROXY_BASE_URL = "https://api.openai-proxy.com/"
        
        // 本地开发测试地址
        const val LOCAL_TEST_BASE_URL = "http://localhost:8080/"
        
        /**
         * 格式化Authorization头
         */
        fun formatAuthHeader(apiKey: String): String {
            return "Bearer $apiKey"
        }
    }
} 