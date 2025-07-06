package com.example.cur_app.utils

/**
 * API调用结果封装类
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception, val message: String = exception.message ?: "Unknown error") : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * 安全API调用函数
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (exception: Exception) {
        ApiResult.Error(exception)
    }
}