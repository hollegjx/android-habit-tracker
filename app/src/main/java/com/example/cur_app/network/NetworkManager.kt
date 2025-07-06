package com.example.cur_app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络连接管理器
 * 负责服务器连接检测、网络状态监控和错误处理
 */
@Singleton
class NetworkManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val authApiService: com.example.cur_app.data.remote.AuthApiService,
    private val networkTester: SimpleNetworkTester
) {
    private val _isServerConnected = MutableStateFlow(false)
    val isServerConnected: StateFlow<Boolean> = _isServerConnected.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.UNKNOWN)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    private val _lastConnectionCheck = MutableStateFlow(0L)
    val lastConnectionCheck: StateFlow<Long> = _lastConnectionCheck.asStateFlow()
    
    companion object {
        private const val TAG = "NetworkManager"
        private const val CONNECTION_CHECK_INTERVAL = 30_000L // 30秒
    }
    
    /**
     * 连接状态枚举
     */
    enum class ConnectionStatus {
        UNKNOWN,        // 未知状态
        CONNECTED,      // 已连接
        DISCONNECTED,   // 已断开
        CHECKING,       // 检查中
        ERROR           // 错误状态
    }
    
    /**
     * 检查设备网络连接状态
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * 检查服务器连接状态
     */
    suspend fun checkServerConnection(): Boolean {
        if (!isNetworkAvailable()) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            _isServerConnected.value = false
            return false
        }
        
        _connectionStatus.value = ConnectionStatus.CHECKING
        
        return try {
            // 使用简化的网络测试器
            val result = networkTester.testServerConnection()
            val isConnected = result is NetworkTestResult.Success
            
            _isServerConnected.value = isConnected
            _connectionStatus.value = if (isConnected) ConnectionStatus.CONNECTED else ConnectionStatus.ERROR
            _lastConnectionCheck.value = System.currentTimeMillis()
            
            Log.d(TAG, "服务器连接检查结果: $isConnected")
            if (result is NetworkTestResult.Error) {
                Log.e(TAG, "服务器连接错误: ${result.error} - ${result.details}")
            }
            isConnected
        } catch (e: Exception) {
            Log.e(TAG, "服务器连接检查失败", e)
            _isServerConnected.value = false
            _connectionStatus.value = ConnectionStatus.ERROR
            false
        }
    }
    
    /**
     * 测试API连通性
     */
    suspend fun testApiConnection(): Boolean {
        return try {
            val result = networkTester.testApiPing()
            val isConnected = result is NetworkTestResult.Success
            
            Log.d(TAG, "API连通性测试结果: $isConnected")
            if (result is NetworkTestResult.Error) {
                Log.e(TAG, "API连通性错误: ${result.error} - ${result.details}")
            }
            isConnected
        } catch (e: Exception) {
            Log.e(TAG, "API连通性测试失败", e)
            false
        }
    }
    
    /**
     * 测试登录功能
     */
    suspend fun testLogin(): Boolean {
        return try {
            val result = networkTester.testLogin()
            val isSuccessful = result is NetworkTestResult.Success
            
            Log.d(TAG, "登录测试结果: $isSuccessful")
            if (result is NetworkTestResult.Error) {
                Log.e(TAG, "登录测试错误: ${result.error} - ${result.details}")
            }
            isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "登录测试失败", e)
            false
        }
    }
    
    /**
     * 定期检查服务器连接状态
     */
    suspend fun performPeriodicConnectionCheck(): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastCheck = _lastConnectionCheck.value
        
        // 如果距离上次检查超过指定间隔，则重新检查
        if (currentTime - lastCheck > CONNECTION_CHECK_INTERVAL) {
            return checkServerConnection()
        }
        
        return _isServerConnected.value
    }
    
    /**
     * 处理网络错误并返回用户友好的错误消息
     */
    fun handleNetworkError(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> ServerConfig.ErrorMessages.SERVER_CONNECTION_FAILED
            is ConnectException -> ServerConfig.ErrorMessages.NETWORK_ERROR
            is SocketTimeoutException -> ServerConfig.ErrorMessages.TIMEOUT_ERROR
            else -> {
                Log.e(TAG, "网络错误", throwable)
                ServerConfig.ErrorMessages.API_ERROR
            }
        }
    }
    
    /**
     * 处理HTTP响应错误
     */
    fun handleHttpError(response: Response<*>): String {
        return when (response.code()) {
            401 -> ServerConfig.ErrorMessages.AUTH_ERROR
            403 -> "权限不足"
            404 -> "请求的资源不存在"
            500 -> "服务器内部错误"
            502, 503 -> "服务器暂时不可用"
            else -> "HTTP错误: ${response.code()}"
        }
    }
    
    /**
     * 执行网络请求并处理结果
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<com.example.cur_app.network.ApiResponse<T>>
    ): NetworkResult<T> {
        // 检查网络连接
        if (!isNetworkAvailable()) {
            return NetworkResult.NetworkError(Exception(ServerConfig.ErrorMessages.NETWORK_ERROR))
        }
        
        return try {
            val response = apiCall()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error(body?.message ?: "未知错误")
                }
            } else {
                NetworkResult.Error(handleHttpError(response), response.code())
            }
        } catch (e: Exception) {
            Log.e(TAG, "API调用失败", e)
            NetworkResult.NetworkError(e)
        }
    }
    
    /**
     * 显示连接错误对话框的辅助方法
     */
    fun getConnectionErrorMessage(): String {
        return when (_connectionStatus.value) {
            ConnectionStatus.DISCONNECTED -> ServerConfig.ErrorMessages.NETWORK_ERROR
            ConnectionStatus.ERROR -> ServerConfig.ErrorMessages.SERVER_CONNECTION_FAILED
            else -> ServerConfig.ErrorMessages.API_ERROR
        }
    }
    
    /**
     * 重置连接状态
     */
    fun resetConnectionStatus() {
        _connectionStatus.value = ConnectionStatus.UNKNOWN
        _isServerConnected.value = false
        _lastConnectionCheck.value = 0L
    }
}