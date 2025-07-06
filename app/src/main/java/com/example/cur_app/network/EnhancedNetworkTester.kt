package com.example.cur_app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.*
import java.security.cert.X509Certificate

@Singleton
class EnhancedNetworkTester @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "EnhancedNetworkTester"
    }
    
    private val client: OkHttpClient by lazy {
        Log.d(TAG, "🔧 初始化OkHttpClient...")
        
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())
        
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val startTime = System.currentTimeMillis()
                
                Log.d(TAG, "📤 发送请求: ${originalRequest.method} ${originalRequest.url}")
                Log.d(TAG, "📤 请求头: ${originalRequest.headers}")
                
                val request = originalRequest.newBuilder()
                    .addHeader("User-Agent", "HabitTracker-Android/1.0.0")
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Platform", "Android")
                    .addHeader("X-App-Version", "1.0.0")
                    .addHeader("X-Debug", "true")
                    .build()
                
                try {
                    val response = chain.proceed(request)
                    val duration = System.currentTimeMillis() - startTime
                    
                    Log.d(TAG, "📥 收到响应: ${response.code} (耗时: ${duration}ms)")
                    Log.d(TAG, "📥 响应头: ${response.headers}")
                    
                    response
                } catch (e: Exception) {
                    val duration = System.currentTimeMillis() - startTime
                    Log.e(TAG, "💥 请求失败 (耗时: ${duration}ms): ${e.javaClass.simpleName} - ${e.message}")
                    throw e
                }
            }
            .build()
    }
    
    /**
     * 检查设备网络状态
     */
    fun checkNetworkStatus(): NetworkStatusResult {
        Log.d(TAG, "🔍 检查网络状态...")
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        
        if (network == null) {
            Log.e(TAG, "❌ 没有活动网络")
            return NetworkStatusResult(
                isConnected = false,
                networkType = "无网络",
                details = "设备未连接到任何网络"
            )
        }
        
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities == null) {
            Log.e(TAG, "❌ 无法获取网络能力")
            return NetworkStatusResult(
                isConnected = false,
                networkType = "未知",
                details = "无法获取网络能力信息"
            )
        }
        
        val networkType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "移动数据"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "以太网"
            else -> "其他"
        }
        
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        Log.d(TAG, "✅ 网络状态: $networkType, 有Internet: $hasInternet, 已验证: $isValidated")
        
        return NetworkStatusResult(
            isConnected = hasInternet && isValidated,
            networkType = networkType,
            details = "Internet: $hasInternet, 验证: $isValidated"
        )
    }
    
    /**
     * 测试多个服务器URL的连通性
     */
    suspend fun testMultipleServers(): List<ServerTestResult> {
        Log.d(TAG, "🌐 开始测试多个服务器URL...")
        
        val results = mutableListOf<ServerTestResult>()
        
        for (baseUrl in ServerConfig.SERVER_URLS) {
            Log.d(TAG, "🎯 测试服务器: $baseUrl")
            
            val result = testSingleServer(baseUrl)
            results.add(result)
            
            // 如果找到一个可用的服务器，可以选择停止测试其他的
            if (result.isSuccess) {
                Log.d(TAG, "✅ 找到可用服务器，停止测试其他URL")
                break
            }
            
            // 在测试之间稍作延迟
            delay(500)
        }
        
        return results
    }
    
    /**
     * 测试单个服务器
     */
    suspend fun testSingleServer(baseUrl: String): ServerTestResult {
        Log.d(TAG, "🔍 测试服务器: $baseUrl")
        
        return try {
            withTimeout(15_000) {
                // 首先测试健康检查
                val healthResult = testEndpoint("$baseUrl/health", "GET")
                
                if (healthResult.isSuccess) {
                    // 健康检查成功，继续测试API ping
                    val pingResult = testEndpoint("$baseUrl/api/ping", "GET")
                    
                    if (pingResult.isSuccess) {
                        // API ping成功，测试登录
                        val loginResult = testLogin(baseUrl)
                        
                        ServerTestResult(
                            baseUrl = baseUrl,
                            isSuccess = true,
                            healthCheck = healthResult,
                            apiPing = pingResult,
                            loginTest = loginResult,
                            error = null
                        )
                    } else {
                        ServerTestResult(
                            baseUrl = baseUrl,
                            isSuccess = false,
                            healthCheck = healthResult,
                            apiPing = pingResult,
                            loginTest = null,
                            error = "API ping失败"
                        )
                    }
                } else {
                    ServerTestResult(
                        baseUrl = baseUrl,
                        isSuccess = false,
                        healthCheck = healthResult,
                        apiPing = null,
                        loginTest = null,
                        error = "健康检查失败"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 服务器测试异常: $baseUrl", e)
            ServerTestResult(
                baseUrl = baseUrl,
                isSuccess = false,
                healthCheck = null,
                apiPing = null,
                loginTest = null,
                error = "连接异常: ${e.javaClass.simpleName} - ${e.message}"
            )
        }
    }
    
    /**
     * 测试单个端点
     */
    private suspend fun testEndpoint(url: String, method: String): EndpointTestResult {
        Log.d(TAG, "🔗 测试端点: $method $url")
        
        return try {
            val request = Request.Builder()
                .url(url)
                .method(method, null)
                .build()
            
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            
            response.use {
                val body = it.body?.string() ?: ""
                Log.d(TAG, "✅ 端点响应: $url -> ${it.code} (${duration}ms)")
                Log.d(TAG, "📄 响应内容: $body")
                
                EndpointTestResult(
                    url = url,
                    isSuccess = it.isSuccessful,
                    statusCode = it.code,
                    responseBody = body,
                    duration = duration,
                    error = if (!it.isSuccessful) "HTTP ${it.code}" else null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 端点测试失败: $url", e)
            EndpointTestResult(
                url = url,
                isSuccess = false,
                statusCode = -1,
                responseBody = "",
                duration = -1,
                error = "${e.javaClass.simpleName}: ${e.message}"
            )
        }
    }
    
    /**
     * 测试登录功能
     */
    private suspend fun testLogin(baseUrl: String): EndpointTestResult {
        Log.d(TAG, "🔐 测试登录: $baseUrl")
        
        return try {
            val loginData = mapOf(
                "username" to "test",
                "password" to "123456",
                "deviceInfo" to mapOf(
                    "appVersion" to "1.0.0",
                    "deviceId" to "test_device",
                    "deviceName" to "Test Device",
                    "manufacturer" to "Test",
                    "model" to "TestMode",
                    "osVersion" to "16",
                    "platform" to "Android"
                )
            )
            
            val json = com.google.gson.Gson().toJson(loginData)
            val requestBody = json.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$baseUrl/api/auth/login")
                .post(requestBody)
                .build()
            
            Log.d(TAG, "🔐 发送登录请求: $json")
            
            val startTime = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            
            response.use {
                val body = it.body?.string() ?: ""
                Log.d(TAG, "🔐 登录响应: ${it.code} (${duration}ms)")
                Log.d(TAG, "🔐 登录内容: $body")
                
                EndpointTestResult(
                    url = "$baseUrl/api/auth/login",
                    isSuccess = it.isSuccessful,
                    statusCode = it.code,
                    responseBody = body,
                    duration = duration,
                    error = if (!it.isSuccessful) "登录失败: HTTP ${it.code}" else null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 登录测试失败: $baseUrl", e)
            EndpointTestResult(
                url = "$baseUrl/api/auth/login",
                isSuccess = false,
                statusCode = -1,
                responseBody = "",
                duration = -1,
                error = "登录异常: ${e.javaClass.simpleName} - ${e.message}"
            )
        }
    }
}

// 数据类
data class NetworkStatusResult(
    val isConnected: Boolean,
    val networkType: String,
    val details: String
)

data class ServerTestResult(
    val baseUrl: String,
    val isSuccess: Boolean,
    val healthCheck: EndpointTestResult?,
    val apiPing: EndpointTestResult?,
    val loginTest: EndpointTestResult?,
    val error: String?
)

data class EndpointTestResult(
    val url: String,
    val isSuccess: Boolean,
    val statusCode: Int,
    val responseBody: String,
    val duration: Long,
    val error: String?
)