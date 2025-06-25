package com.example.cur_app.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通用网络拦截器
 * 处理请求头添加、响应日志和通用错误处理
 */
@Singleton
class NetworkInterceptor @Inject constructor() : Interceptor {
    
    companion object {
        private const val TAG = "NetworkInterceptor"
        private const val HEADER_USER_AGENT = "User-Agent"
        private const val HEADER_ACCEPT = "Accept"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        
        // 应用标识
        private const val USER_AGENT = "AIHabitTracker/1.0 (Android)"
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 构建新的请求，添加通用请求头
        val newRequest = originalRequest.newBuilder()
            .addHeader(HEADER_USER_AGENT, USER_AGENT)
            .addHeader(HEADER_ACCEPT, "application/json")
            .addHeader(HEADER_CONTENT_TYPE, "application/json; charset=utf-8")
            .build()
        
        // 记录请求信息
        logRequest(newRequest)
        
        val startTime = System.currentTimeMillis()
        
        return try {
            val response = chain.proceed(newRequest)
            val endTime = System.currentTimeMillis()
            
            // 记录响应信息
            logResponse(response, endTime - startTime)
            
            response
        } catch (e: Exception) {
            Log.e(TAG, "网络请求异常: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 记录请求信息
     */
    private fun logRequest(request: Request) {
        Log.d(TAG, """
            |========== 网络请求 ==========
            |URL: ${request.url}
            |方法: ${request.method}
            |请求头: ${request.headers}
            |请求体大小: ${request.body?.contentLength() ?: 0} bytes
            |============================
        """.trimMargin())
    }
    
    /**
     * 记录响应信息
     */
    private fun logResponse(response: Response, duration: Long) {
        val bodySize = response.body?.contentLength() ?: 0
        val statusCode = response.code
        val statusMessage = response.message
        
        Log.d(TAG, """
            |========== 网络响应 ==========
            |状态码: $statusCode $statusMessage
            |URL: ${response.request.url}
            |耗时: ${duration}ms
            |响应体大小: ${if (bodySize == -1L) "未知" else "$bodySize bytes"}
            |响应头: ${response.headers}
            |============================
        """.trimMargin())
        
        // 记录错误响应
        if (!response.isSuccessful) {
            Log.w(TAG, "HTTP错误响应: $statusCode $statusMessage")
        }
    }
}

/**
 * API密钥拦截器
 * 自动为需要的请求添加Authorization头
 */
@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    
    companion object {
        private const val TAG = "ApiKeyInterceptor"
        private const val HEADER_AUTHORIZATION = "Authorization"
    }
    
    // API密钥存储（实际项目中应从安全存储获取）
    private var apiKey: String = ""
    
    /**
     * 设置API密钥
     */
    fun setApiKey(key: String) {
        apiKey = key
        Log.d(TAG, "API密钥已更新")
    }
    
    /**
     * 清除API密钥
     */
    fun clearApiKey() {
        apiKey = ""
        Log.d(TAG, "API密钥已清除")
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 如果已经有Authorization头或者没有设置API密钥，直接执行原请求
        if (originalRequest.header(HEADER_AUTHORIZATION) != null || apiKey.isEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // 添加Authorization头
        val newRequest = originalRequest.newBuilder()
            .addHeader(HEADER_AUTHORIZATION, "Bearer $apiKey")
            .build()
        
        Log.d(TAG, "已为请求添加API密钥: ${originalRequest.url}")
        
        return chain.proceed(newRequest)
    }
}

/**
 * 重试拦截器
 * 自动重试失败的网络请求
 */
@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {
    
    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 1..MAX_RETRY_COUNT) {
            try {
                exception = null
                response?.close() // 关闭之前的响应
                
                response = chain.proceed(request)
                
                // 如果响应成功，直接返回
                if (response.isSuccessful) {
                    if (attempt > 1) {
                        Log.i(TAG, "请求在第 $attempt 次尝试后成功: ${request.url}")
                    }
                    return response
                }
                
                // 某些错误码不需要重试
                if (shouldNotRetry(response.code)) {
                    Log.d(TAG, "状态码 ${response.code} 不需要重试: ${request.url}")
                    return response
                }
                
                Log.w(TAG, "第 $attempt 次请求失败，状态码: ${response.code}")
                
            } catch (e: IOException) {
                exception = e
                Log.w(TAG, "第 $attempt 次请求异常: ${e.message}")
            }
            
            // 如果不是最后一次尝试，等待后重试
            if (attempt < MAX_RETRY_COUNT) {
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt) // 递增延迟
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("重试被中断", e)
                }
                Log.i(TAG, "正在进行第 ${attempt + 1} 次重试: ${request.url}")
            }
        }
        
        // 所有重试都失败了
        Log.e(TAG, "所有重试都失败了: ${request.url}")
        
        return response ?: throw (exception ?: IOException("未知网络错误"))
    }
    
    /**
     * 判断是否应该跳过重试
     */
    private fun shouldNotRetry(statusCode: Int): Boolean {
        return when (statusCode) {
            400, 401, 403, 404, 422 -> true // 客户端错误，重试无意义
            else -> false
        }
    }
} 