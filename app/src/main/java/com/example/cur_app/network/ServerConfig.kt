package com.example.cur_app.network

/**
 * 服务器配置和连接管理
 */
object ServerConfig {
    // 多个服务器地址配置（用于网络调试）
    val SERVER_URLS = listOf(
        "https://dkht.gjxlsy.top",        // 云服务器域名 (优先)
        "http://38.207.179.136:3000",     // 云服务器IP
        "http://10.0.2.2:3000",           // Android模拟器host映射
        "http://localhost:3000"            // localhost
    )
    
    const val BASE_URL = "https://dkht.gjxlsy.top"  // 使用云服务器域名
    const val API_BASE_URL = "$BASE_URL/api"
    const val HEALTH_CHECK_URL = "$BASE_URL/health"
    const val PING_URL = "$BASE_URL/api/ping"
    const val SOCKET_URL = BASE_URL
    
    // 网络超时配置
    const val CONNECT_TIMEOUT = 15_000L // 15秒
    const val READ_TIMEOUT = 30_000L    // 30秒
    const val WRITE_TIMEOUT = 30_000L   // 30秒
    const val CALL_TIMEOUT = 45_000L    // 45秒总超时
    
    // API路径配置
    object ApiPaths {
        // 服务器状态
        const val HEALTH_CHECK = "health"
        const val PING = "api/ping"
        
        // 认证相关
        const val AUTH_LOGIN = "api/auth/login"
        const val AUTH_REGISTER = "api/auth/register"
        const val AUTH_REFRESH = "api/auth/refresh"
        const val AUTH_LOGOUT = "api/auth/logout"
        
        // 用户相关
        const val USER_PROFILE = "api/users/profile"
        const val USER_FRIENDS = "api/users/friends"
        const val FRIEND_REQUEST = "api/users/friends/request"
        
        // AI角色相关
        const val AI_CHARACTERS = "api/ai-characters"
        fun AI_CHARACTER_DETAIL(characterId: String) = "api/ai-characters/$characterId"
        fun AI_CHARACTER_CHAT(characterId: String) = "api/ai-characters/$characterId/chat"
        
        // 习惯相关
        const val HABITS = "api/habits"
        fun HABIT_DETAIL(habitId: String) = "api/habits/$habitId"
        fun HABIT_COMPLETE(habitId: String) = "api/habits/$habitId/complete"
        fun HABIT_STATISTICS(habitId: String) = "api/habits/$habitId/statistics"
        
        // 聊天相关
        const val CHAT_MESSAGES = "api/chat/messages"
        const val CHAT_CONVERSATIONS = "api/chat/conversations"
        fun CHAT_CONVERSATION_MESSAGES(conversationId: String) = "api/chat/conversations/$conversationId/messages"
    }
    
    // 错误消息配置
    object ErrorMessages {
        const val SERVER_CONNECTION_FAILED = "无法连接到服务器，你可能无法正常使用AI功能"
        const val NETWORK_ERROR = "网络连接错误，请检查网络设置"
        const val API_ERROR = "服务器响应错误，请稍后再试"
        const val TIMEOUT_ERROR = "请求超时，请检查网络连接"
        const val AUTH_ERROR = "认证失败，请重新登录"
    }
}

/**
 * 网络结果封装
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : NetworkResult<T>()
    data class NetworkError<T>(val exception: Throwable) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

/**
 * API响应基础结构
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

/**
 * 健康检查响应
 */
data class HealthCheckResponse(
    val status: String,
    val timestamp: String,
    val uptime: Double
)

/**
 * Ping响应
 */
data class PingResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String,
    val server: String
)