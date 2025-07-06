package com.example.cur_app.network

import android.util.Log
import kotlinx.coroutines.withTimeout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.*
import java.security.cert.X509Certificate

@Singleton
class SimpleNetworkTester @Inject constructor() {
    
    companion object {
        private const val TAG = "SimpleNetworkTester"
        private const val BASE_URL = "http://38.207.179.136:3000"
    }
    
    private val client: OkHttpClient by lazy {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())
        
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "HabitTracker-Android/1.0")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    
    suspend fun testServerConnection(): NetworkTestResult {
        return try {
            withTimeout(30_000) {
                val request = Request.Builder()
                    .url("$BASE_URL/health")
                    .get()
                    .build()
                
                Log.d(TAG, "Testing server connection to: $BASE_URL/health")
                
                val response = client.newCall(request).execute()
                response.use {
                    val body = it.body?.string()
                    Log.d(TAG, "Health check response: code=${it.code}, body=$body")
                    
                    if (it.isSuccessful) {
                        NetworkTestResult.Success("Server is reachable", body ?: "")
                    } else {
                        NetworkTestResult.Error("HTTP ${it.code}", body ?: "No response body")
                    }
                }
            }
        } catch (e: ConnectException) {
            Log.e(TAG, "Connection failed", e)
            NetworkTestResult.Error("Connection refused", "Failed to connect to server: ${e.message}")
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Connection timeout", e)
            NetworkTestResult.Error("Connection timeout", "Server did not respond in time: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO error", e)
            NetworkTestResult.Error("Network error", "IO error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            NetworkTestResult.Error("Unexpected error", "Error: ${e.message}")
        }
    }
    
    suspend fun testApiPing(): NetworkTestResult {
        return try {
            withTimeout(30_000) {
                val request = Request.Builder()
                    .url("$BASE_URL/api/ping")
                    .get()
                    .build()
                
                Log.d(TAG, "Testing API ping to: $BASE_URL/api/ping")
                
                val response = client.newCall(request).execute()
                response.use {
                    val body = it.body?.string()
                    Log.d(TAG, "Ping response: code=${it.code}, body=$body")
                    
                    if (it.isSuccessful) {
                        NetworkTestResult.Success("API is reachable", body ?: "")
                    } else {
                        NetworkTestResult.Error("HTTP ${it.code}", body ?: "No response body")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "API ping failed", e)
            NetworkTestResult.Error("API ping failed", "Error: ${e.message}")
        }
    }
    
    suspend fun testLogin(): NetworkTestResult {
        return try {
            withTimeout(30_000) {
                val loginData = """
                    {
                        "username": "test",
                        "password": "123456",
                        "deviceInfo": {
                            "appVersion": "1.0.0",
                            "deviceId": "test_device",
                            "deviceName": "Test Device",
                            "manufacturer": "Test",
                            "model": "TestMode",
                            "osVersion": "16",
                            "platform": "Android"
                        }
                    }
                """.trimIndent()
                
                val requestBody = loginData.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/api/auth/login")
                    .post(requestBody)
                    .build()
                
                Log.d(TAG, "Testing login to: $BASE_URL/api/auth/login")
                Log.d(TAG, "Login payload: $loginData")
                
                val response = client.newCall(request).execute()
                response.use {
                    val body = it.body?.string()
                    Log.d(TAG, "Login response: code=${it.code}, body=$body")
                    
                    if (it.isSuccessful) {
                        NetworkTestResult.Success("Login successful", body ?: "")
                    } else {
                        NetworkTestResult.Error("HTTP ${it.code}", body ?: "No response body")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login test failed", e)
            NetworkTestResult.Error("Login test failed", "Error: ${e.message}")
        }
    }
}

sealed class NetworkTestResult {
    data class Success(val message: String, val response: String) : NetworkTestResult()
    data class Error(val error: String, val details: String) : NetworkTestResult()
}