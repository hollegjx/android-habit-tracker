package com.example.cur_app.di

import android.content.Context
import com.example.cur_app.data.remote.AuthApiService
import com.example.cur_app.data.remote.api.AiApiService
import com.example.cur_app.data.remote.api.ChatApiService
import com.example.cur_app.data.remote.api.FriendApiService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.json.Json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.*
import java.security.cert.X509Certificate

/**
 * 网络相关的依赖注入模块
 * 提供Retrofit、OkHttp、API服务等网络组件
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 基础服务器URL
     */
    @Provides
    @Named("BASE_URL")
    fun provideBaseUrl(): String {
        // 使用云服务器地址和域名
        return "https://dkht.gjxlsy.top/"
    }
    
    /**
     * AI API基础URL
     */
    @Provides
    @Named("AI_BASE_URL")
    fun provideAiBaseUrl(): String {
        // 直接连接zetatechs AI API
        return "https://api.zetatechs.com/"
    }

    /**
     * 提供Gson实例
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()
    }

    /**
     * 提供HTTP缓存
     */
    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10 * 1024 * 1024L // 10MB
        val cacheDir = File(context.cacheDir, "http-cache")
        return Cache(cacheDir, cacheSize)
    }

    /**
     * 提供HTTP日志拦截器
     */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(@ApplicationContext context: Context): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(
            // 添加自定义日志处理器，确保使用明确的TAG
            HttpLoggingInterceptor.Logger { message ->
                android.util.Log.d("OkHttp", message)
                // 同时输出到我们自己的日志标签
                android.util.Log.i("NetworkRequest", message)
                // 强制输出到系统日志
                println("HTTP_LOG: $message")
            }
        ).apply {
            // 设置详细的日志级别
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * 提供调试拦截器
     */
    @Provides
    @Singleton
    @Named("debug")
    fun provideDebugInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val startTime = System.currentTimeMillis()
            
            // 使用更强力的日志输出
            android.util.Log.e("FORCE_DEBUG", "🚀🚀🚀 NETWORK REQUEST START 🚀🚀🚀")
            android.util.Log.e("FORCE_DEBUG", "Method: ${request.method}")
            android.util.Log.e("FORCE_DEBUG", "URL: ${request.url}")
            android.util.Log.e("FORCE_DEBUG", "Headers: ${request.headers}")
            
            // 多种输出方式
            println("🚀🚀🚀 NETWORK REQUEST: ${request.method} ${request.url}")
            System.out.println("🚀🚀🚀 SYSTEM OUT: ${request.method} ${request.url}")
            
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            
            // 强制输出响应信息
            android.util.Log.e("FORCE_DEBUG", "📥📥📥 NETWORK RESPONSE END 📥📥📥")
            android.util.Log.e("FORCE_DEBUG", "Status: ${response.code} ${response.message}")
            android.util.Log.e("FORCE_DEBUG", "Duration: ${endTime - startTime}ms")
            
            println("📥📥📥 NETWORK RESPONSE: ${response.code} ${response.message} (${endTime - startTime}ms)")
            System.out.println("📥📥📥 SYSTEM OUT: ${response.code} ${response.message}")
            
            response
        }
    }

    /**
     * 提供认证拦截器
     */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthInterceptor(dataStore: DataStore<Preferences>): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val url = originalRequest.url.toString()
            
            // 无需认证的端点
            val noAuthEndpoints = listOf("/auth/", "/health", "/ping", "/api/ping")
            if (noAuthEndpoints.any { url.contains(it) }) {
                return@Interceptor chain.proceed(originalRequest)
            }
            
            // 获取访问令牌
            val accessToken = runCatching {
                kotlinx.coroutines.runBlocking {
                    dataStore.data.map { preferences ->
                        preferences[androidx.datastore.preferences.core.stringPreferencesKey("access_token")]
                    }.first()
                }
            }.getOrNull()
            
            val newRequest = if (accessToken != null) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }
            
            chain.proceed(newRequest)
        }
    }

    /**
     * 提供通用请求头拦截器
     */
    @Provides
    @Singleton
    @Named("header")
    fun provideHeaderInterceptor(@ApplicationContext context: Context): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            val newRequest = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "HabitTracker-Android/${getAppVersion(context)}")
                .addHeader("X-Platform", "Android")
                .addHeader("X-App-Version", getAppVersion(context))
                .addHeader("X-OS-Version", android.os.Build.VERSION.RELEASE)
                .addHeader("X-Device-Model", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                .build()
            
            chain.proceed(newRequest)
        }
    }

    /**
     * 提供OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("header") headerInterceptor: Interceptor,
        @Named("auth") authInterceptor: Interceptor,
        @Named("debug") debugInterceptor: Interceptor
    ): OkHttpClient {
        // 信任所有证书的SSL配置（仅用于开发环境）
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(debugInterceptor)  // 首先添加调试拦截器
            .addInterceptor(headerInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)  // 最后添加详细日志拦截器
            .connectTimeout(30, TimeUnit.SECONDS)  // 增加超时时间
            .readTimeout(60, TimeUnit.SECONDS)     // 增加超时时间
            .writeTimeout(60, TimeUnit.SECONDS)    // 增加超时时间
            .callTimeout(90, TimeUnit.SECONDS)     // 增加总超时时间
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    /**
     * 提供Retrofit实例
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("BASE_URL") baseUrl: String,
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * 提供认证API服务
     */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    /**
     * 提供AI API专用的OkHttpClient
     */
    @Provides
    @Singleton
    @Named("ai")
    fun provideAiOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("header") headerInterceptor: Interceptor,
        @Named("debug") debugInterceptor: Interceptor
    ): OkHttpClient {
        // AI API不需要身份验证拦截器，使用API Key验证
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(debugInterceptor)  // 首先添加调试拦截器
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)  // 最后添加详细日志拦截器
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }
    
    /**
     * 提供AI API专用的Retrofit实例
     */
    @Provides
    @Singleton
    @Named("ai")
    fun provideAiRetrofit(
        @Named("AI_BASE_URL") aiBaseUrl: String,
        @Named("ai") okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(aiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * 提供AI API服务
     */
    @Provides
    @Singleton
    fun provideAiApiService(@Named("ai") retrofit: Retrofit): AiApiService {
        return retrofit.create(AiApiService::class.java)
    }
    
    /**
     * 提供聊天API服务
     */
    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }
    
    /**
     * 提供好友API服务
     */
    @Provides
    @Singleton
    fun provideFriendApiService(retrofit: Retrofit): FriendApiService {
        return retrofit.create(FriendApiService::class.java)
    }
    
    /**
     * 提供Json序列化器
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    /**
     * 获取应用版本号
     */
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
} 