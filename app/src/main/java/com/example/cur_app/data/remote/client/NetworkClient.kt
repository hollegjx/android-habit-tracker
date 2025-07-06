package com.example.cur_app.data.remote.client

import android.content.Context
import com.example.cur_app.data.remote.interceptor.ApiKeyInterceptor
import com.example.cur_app.data.remote.interceptor.NetworkInterceptor
import com.example.cur_app.data.remote.interceptor.RetryInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType

/**
 * 网络客户端配置
 * 负责配置OkHttp和Retrofit实例
 */
@Singleton
class NetworkClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkInterceptor: NetworkInterceptor,
    private val apiKeyInterceptor: ApiKeyInterceptor,
    private val retryInterceptor: RetryInterceptor,
    private val json: Json
) {
    
    companion object {
        // 超时配置
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 60L
        
        // 缓存配置
        private const val CACHE_SIZE = 50L * 1024L * 1024L // 50MB
        private const val CACHE_DIR_NAME = "http_cache"
        
        // 连接池配置
        private const val MAX_IDLE_CONNECTIONS = 5
        private const val KEEP_ALIVE_DURATION = 5L // 分钟
    }
    
    /**
     * 创建基础OkHttp客户端
     */
    fun createOkHttpClient(enableLogging: Boolean = true): OkHttpClient {
        val builder = OkHttpClient.Builder()
            // 超时设置
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            
            // 连接池设置
            .connectionPool(
                ConnectionPool(
                    MAX_IDLE_CONNECTIONS,
                    KEEP_ALIVE_DURATION,
                    TimeUnit.MINUTES
                )
            )
            
            // 缓存设置
            .cache(createCache())
            
            // 添加拦截器（顺序很重要）
            .addInterceptor(networkInterceptor)      // 通用请求头和日志
            .addInterceptor(apiKeyInterceptor)       // API密钥
            .addInterceptor(retryInterceptor)        // 重试机制
        
        // 调试模式下添加详细日志
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addNetworkInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
    
    /**
     * 创建Retrofit实例
     */
    fun createRetrofit(baseUrl: String, okHttpClient: OkHttpClient? = null): Retrofit {
        val client = okHttpClient ?: createOkHttpClient()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
    
    /**
     * 创建专用于AI服务的Retrofit实例
     */
    fun createAiServiceRetrofit(baseUrl: String): Retrofit {
        // 为AI服务创建专门的OkHttp客户端，增加超时时间
        val aiClient = createOkHttpClient()
            .newBuilder()
            .readTimeout(120L, TimeUnit.SECONDS)  // AI响应可能较慢
            .writeTimeout(120L, TimeUnit.SECONDS)
            .build()
        
        return createRetrofit(baseUrl, aiClient)
    }
    
    /**
     * 创建HTTP缓存
     */
    private fun createCache(): Cache {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        return Cache(cacheDir, CACHE_SIZE)
    }
    
    /**
     * 清理缓存
     */
    fun clearCache() {
        try {
            val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            // 忽略清理缓存的错误
        }
    }
    
    /**
     * 获取缓存大小
     */
    fun getCacheSize(): Long {
        return try {
            val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
            if (cacheDir.exists()) {
                cacheDir.walkTopDown().sumOf { it.length() }
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * 网络配置管理器
 * 管理不同环境的网络配置
 */
@Singleton
class NetworkConfig @Inject constructor() {
    
    enum class Environment {
        PRODUCTION,    // 生产环境（OpenAI官方）
        CHINA_PROXY,   // 中国代理环境
        DEVELOPMENT,   // 开发环境
        LOCAL_TEST     // 本地测试环境
    }
    
    // 当前环境
    private var currentEnvironment = Environment.CHINA_PROXY
    
    /**
     * 设置当前环境
     */
    fun setEnvironment(environment: Environment) {
        currentEnvironment = environment
    }
    
    /**
     * 获取当前环境
     */
    fun getCurrentEnvironment(): Environment = currentEnvironment
    
    /**
     * 获取当前环境对应的基础URL
     */
    fun getBaseUrl(): String {
        return when (currentEnvironment) {
            Environment.PRODUCTION -> "https://api.openai.com/"
            Environment.CHINA_PROXY -> "https://api.openai-proxy.com/"
            Environment.DEVELOPMENT -> "https://api.openai-dev.com/"
            Environment.LOCAL_TEST -> "https://dkht.gjxlsy.top/"  // 云服务器测试
        }
    }
    
    /**
     * 获取环境显示名称
     */
    fun getEnvironmentDisplayName(): String {
        return when (currentEnvironment) {
            Environment.PRODUCTION -> "生产环境"
            Environment.CHINA_PROXY -> "中国代理"
            Environment.DEVELOPMENT -> "开发环境"
            Environment.LOCAL_TEST -> "本地测试"
        }
    }
    
    /**
     * 判断是否为生产环境
     */
    fun isProduction(): Boolean = currentEnvironment == Environment.PRODUCTION
    
    /**
     * 判断是否启用调试日志
     */
    fun isLoggingEnabled(): Boolean {
        return when (currentEnvironment) {
            Environment.PRODUCTION -> false
            else -> true
        }
    }
} 