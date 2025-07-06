package com.example.cur_app.network

/**
 * API配置管理
 * 统一管理所有在线API接口信息
 */
object ApiConfig {
    
    // ========== 基础配置 ==========
    
    /**
     * 网络请求超时配置（秒）
     */
    const val CONNECT_TIMEOUT = 10L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    /**
     * 缓存配置
     */
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
    const val CACHE_MAX_AGE = 5 * 60 // 5分钟
    const val CACHE_MAX_STALE = 7 * 24 * 60 * 60 // 7天
    
    // ========== 每日一句API ==========
    
    /**
     * 每日一句API配置
     */
    object DailyQuote {
        const val BASE_URL = "https://api.nxvav.cn/"
        const val ENDPOINT = "api/yiyan"
        
        /**
         * 构建完整的每日一句请求URL
         */
        fun buildUrl(encode: String = "json", charset: String = "utf-8"): String {
            return "${BASE_URL}${ENDPOINT}/?encode=${encode}&charset=${charset}"
        }
    }
    
    // ========== 天气API（备用） ==========
    
    /**
     * 天气API配置（如需要）
     */
    object Weather {
        const val BASE_URL = "https://api.openweathermap.org/"
        const val ENDPOINT = "data/2.5/weather"
        
        // 注意：实际使用时需要申请API密钥
        const val API_KEY_PLACEHOLDER = "YOUR_API_KEY_HERE"
    }
    
    // ========== 位置服务API（备用） ==========
    
    /**
     * 位置服务API配置（如需要）
     */
    object Location {
        const val BASE_URL = "https://api.ip-api.com/"
        const val ENDPOINT = "json"
    }
    
    // ========== AI服务API（备用） ==========
    
    /**
     * AI服务API配置（如需要）
     */
    object AiService {
        const val BASE_URL = "https://api.openai.com/"
        const val CHAT_ENDPOINT = "v1/chat/completions"
        
        // 注意：实际使用时需要申请API密钥
        const val API_KEY_PLACEHOLDER = "YOUR_OPENAI_API_KEY"
    }
    
    // ========== 励志语录API（备用） ==========
    
    /**
     * 励志语录API配置（备用方案）
     */
    object MotivationalQuotes {
        const val BASE_URL = "https://api.quotable.io/"
        const val RANDOM_ENDPOINT = "random"
        const val QUOTES_ENDPOINT = "quotes"
    }
    
    // ========== 通用工具方法 ==========
    
    /**
     * 检查网络连接状态
     */
    fun isNetworkAvailable(): Boolean {
        // TODO: 实现网络连接检查
        return true
    }
    
    /**
     * 构建User-Agent
     */
    fun buildUserAgent(): String {
        return "CurApp/1.0 (Android; AI Habit Tracker)"
    }
    
    /**
     * 获取请求头
     */
    fun getCommonHeaders(): Map<String, String> {
        return mapOf(
            "User-Agent" to buildUserAgent(),
            "Accept" to "application/json",
            "Content-Type" to "application/json; charset=utf-8"
        )
    }
} 