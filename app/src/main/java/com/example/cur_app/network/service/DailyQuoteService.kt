package com.example.cur_app.network.service

import com.example.cur_app.network.ApiConfig
import com.example.cur_app.network.model.DailyQuote
import com.example.cur_app.network.model.DailyQuoteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * 每日一句服务
 * 负责获取每日一句数据，包含网络请求和本地缓存
 */
@Singleton
class DailyQuoteService @Inject constructor() {
    
    companion object {
        private const val TAG = "DailyQuoteService"
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    // 简单内存缓存
    private var cachedQuote: DailyQuote? = null
    private var cacheTime: Long = 0
    private val cacheValidDuration = 30 * 60 * 1000L // 30分钟
    
    /**
     * 获取每日一句
     * 直接使用本地正能量语录，不进行网络请求
     */
    suspend fun getDailyQuote(): DailyQuote {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始获取每日一句（本地模式）")
                
                // 检查缓存是否有效（每日更新一次）
                if (isCacheValidForDaily()) {
                    Log.d(TAG, "使用今日已缓存的语录")
                    cachedQuote?.let { return@withContext it }
                }
                
                Log.d(TAG, "获取新的本地语录")
                
                // 直接使用本地正能量语录
                val localQuote = getRandomLocalQuote()
                Log.d(TAG, "获取本地语录成功：${localQuote.content}")
                
                // 更新缓存
                cachedQuote = localQuote
                cacheTime = System.currentTimeMillis()
                return@withContext localQuote
                
            } catch (e: Exception) {
                Log.e(TAG, "获取每日一句出现异常", e)
                // 异常处理，返回默认语录
                return@withContext DailyQuote.getDefault()
            }
        }
    }
    
    /**
     * 获取随机本地语录
     */
    private fun getRandomLocalQuote(): DailyQuote {
        val localQuotes = DailyQuote.getLocalQuotes()
        val randomIndex = (System.currentTimeMillis() % localQuotes.size).toInt()
        return localQuotes[randomIndex]
    }
    
    /**
     * 检查缓存是否有效（每日更新）
     */
    private fun isCacheValidForDaily(): Boolean {
        if (cachedQuote == null || cacheTime == 0L) {
            return false
        }
        
        // 检查是否是同一天
        val currentTime = System.currentTimeMillis()
        val cacheDay = cacheTime / (24 * 60 * 60 * 1000L)
        val currentDay = currentTime / (24 * 60 * 60 * 1000L)
        
        return cacheDay == currentDay
    }
    
    /**
     * 从网络获取每日一句
     */
    private suspend fun fetchFromNetwork(): DailyQuote? {
        return withContext(Dispatchers.IO) {
            try {
            val url = ApiConfig.DailyQuote.buildUrl()
            Log.d(TAG, "请求URL: $url")
            
            val request = Request.Builder()
                .url(url)
                .apply {
                    ApiConfig.getCommonHeaders().forEach { (key, value) ->
                        addHeader(key, value)
                        Log.d(TAG, "添加请求头: $key = $value")
                    }
                }
                .build()
            
            Log.d(TAG, "开始网络请求")
            val response = httpClient.newCall(request).execute()
            
            Log.d(TAG, "响应状态码: ${response.code}")
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                Log.d(TAG, "响应内容: $body")
                
                if (!body.isNullOrEmpty()) {
                    val quoteResponse = json.decodeFromString<DailyQuoteResponse>(body)
                    Log.d(TAG, "解析成功: ${quoteResponse.content}")
                    DailyQuote.fromResponse(quoteResponse)
                } else {
                    Log.w(TAG, "响应内容为空")
                    null
                }
            } else {
                Log.w(TAG, "请求失败，状态码: ${response.code}")
                null
            }
            } catch (e: Exception) {
                Log.e(TAG, "网络请求异常", e)
                null
            }
        }
    }
    
    /**
     * 获取本地备用数据
     */
    private fun getLocalFallback(): DailyQuote {
        return cachedQuote ?: run {
            // 如果没有缓存，从本地语录中随机选择一个
            val localQuotes = DailyQuote.getLocalQuotes()
            val randomIndex = (System.currentTimeMillis() % localQuotes.size).toInt()
            localQuotes[randomIndex]
        }
    }
    
    /**
     * 检查缓存是否有效
     */
    private fun isCacheValid(): Boolean {
        return cachedQuote != null && 
               (System.currentTimeMillis() - cacheTime) < cacheValidDuration
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        cachedQuote = null
        cacheTime = 0
    }
    
    /**
     * 强制刷新（清除缓存并重新获取）
     */
    suspend fun refresh(): DailyQuote {
        Log.d(TAG, "强制刷新：清除所有缓存")
        clearCache()
        return getDailyQuote()
    }
    
    /**
     * 重置所有数据（用于解决缓存污染问题）
     */
    suspend fun resetAllData(): DailyQuote {
        Log.d(TAG, "重置所有数据：强制清除缓存和获取新数据")
        cachedQuote = null
        cacheTime = 0
        // 直接获取新的本地语录
        val freshQuote = getRandomLocalQuote()
        cachedQuote = freshQuote
        cacheTime = System.currentTimeMillis()
        Log.d(TAG, "重置完成，获取到新数据：${freshQuote.content}")
        return freshQuote
    }
} 