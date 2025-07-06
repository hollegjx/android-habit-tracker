package com.example.cur_app.data.remote.error

import kotlinx.serialization.Serializable

/**
 * 网络错误封装类
 */
sealed class NetworkError : Exception() {
    
    /**
     * 网络连接错误
     */
    data class NetworkException(
        override val message: String = "网络连接失败，请检查网络设置"
    ) : NetworkError()
    
    /**
     * HTTP错误
     */
    data class HttpException(
        val code: Int,
        override val message: String
    ) : NetworkError() {
        companion object {
            fun fromCode(code: Int): HttpException {
                return when (code) {
                    400 -> HttpException(code, "请求参数错误")
                    401 -> HttpException(code, "API密钥无效或已过期")
                    403 -> HttpException(code, "访问被拒绝，请检查权限")
                    404 -> HttpException(code, "请求的服务不存在")
                    429 -> HttpException(code, "请求过于频繁，请稍后重试")
                    500 -> HttpException(code, "服务器内部错误")
                    502 -> HttpException(code, "网关错误，服务暂时不可用")
                    503 -> HttpException(code, "服务暂时不可用")
                    else -> HttpException(code, "请求失败（错误代码：$code）")
                }
            }
        }
    }
    
    /**
     * AI服务特定错误
     */
    data class AiServiceException(
        val errorType: String,
        val errorCode: String?,
        override val message: String
    ) : NetworkError() {
        companion object {
            fun fromErrorType(type: String, code: String?, message: String): AiServiceException {
                val friendlyMessage = when (type) {
                    "invalid_api_key" -> "API密钥无效，请检查配置"
                    "insufficient_quota" -> "API配额不足，请检查账户余额"
                    "model_not_found" -> "指定的AI模型不存在"
                    "context_length_exceeded" -> "对话内容过长，请缩短对话"
                    "rate_limit_exceeded" -> "请求过于频繁，请稍后重试"
                    "server_error" -> "AI服务暂时不可用"
                    else -> message.ifEmpty { "AI服务发生未知错误" }
                }
                return AiServiceException(type, code, friendlyMessage)
            }
        }
    }
    
    /**
     * 解析错误
     */
    data class ParseException(
        override val message: String = "数据解析失败"
    ) : NetworkError()
    
    /**
     * 超时错误
     */
    data class TimeoutException(
        override val message: String = "请求超时，请检查网络连接"
    ) : NetworkError()
    
    /**
     * 未知错误
     */
    data class UnknownException(
        override val message: String = "发生未知错误"
    ) : NetworkError()
}

/**
 * 网络结果封装类
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val error: NetworkError) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

/**
 * 错误处理工具
 */
object ErrorHandler {
    
    /**
     * 处理Retrofit异常
     */
    fun handleRetrofitException(throwable: Throwable): NetworkError {
        return when (throwable) {
            is retrofit2.HttpException -> {
                NetworkError.HttpException.fromCode(throwable.code())
            }
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> {
                NetworkError.NetworkException()
            }
            is java.net.UnknownHostException -> {
                NetworkError.NetworkException("无法连接到服务器，请检查网络")
            }
            is kotlinx.serialization.SerializationException -> {
                NetworkError.ParseException("服务器返回数据格式错误")
            }
            else -> {
                NetworkError.UnknownException(throwable.message ?: "未知错误")
            }
        }
    }
    
    /**
     * 获取用户友好的错误消息
     */
    fun getErrorMessage(error: NetworkError): String {
        return when (error) {
            is NetworkError.NetworkException -> error.message
            is NetworkError.HttpException -> error.message
            is NetworkError.AiServiceException -> error.message
            is NetworkError.ParseException -> error.message
            is NetworkError.TimeoutException -> error.message
            is NetworkError.UnknownException -> error.message
        }
    }
} 