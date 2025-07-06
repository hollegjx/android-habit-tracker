package com.example.cur_app.data.remote.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络状态
 */
data class NetworkStatus(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val isMetered: Boolean = false,
    val signalStrength: Int = 0 // 0-4，信号强度
)

/**
 * 连接类型
 */
enum class ConnectionType {
    NONE,       // 无网络
    WIFI,       // WiFi
    CELLULAR,   // 移动网络
    ETHERNET,   // 有线网络
    OTHER       // 其他类型
}

/**
 * 网络状态监控器
 * 实时监控网络连接状态和类型
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NetworkMonitor"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * 网络状态流
     */
    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            
            override fun onAvailable(network: Network) {
                Log.d(TAG, "网络连接可用: $network")
                trySend(getCurrentNetworkStatus())
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "网络连接丢失: $network")
                trySend(getCurrentNetworkStatus())
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Log.d(TAG, "网络能力变更: $network")
                trySend(getCurrentNetworkStatus())
            }
            
            override fun onUnavailable() {
                Log.d(TAG, "网络不可用")
                trySend(NetworkStatus(
                    isConnected = false,
                    connectionType = ConnectionType.NONE
                ))
            }
        }
        
        // 注册网络监听
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // 发送初始状态
        trySend(getCurrentNetworkStatus())
        
        // 清理回调
        awaitClose {
            Log.d(TAG, "取消注册网络监听")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * 获取当前网络状态
     */
    fun getCurrentNetworkStatus(): NetworkStatus {
        val activeNetwork = connectivityManager.activeNetwork
        
        if (activeNetwork == null) {
            return NetworkStatus(
                isConnected = false,
                connectionType = ConnectionType.NONE
            )
        }
        
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkStatus(
                isConnected = false,
                connectionType = ConnectionType.NONE
            )
        
        val connectionType = when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                ConnectionType.WIFI
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                ConnectionType.CELLULAR
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                ConnectionType.ETHERNET
            }
            else -> ConnectionType.OTHER
        }
        
        val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        
        val isMetered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        
        val signalStrength = try {
            when (connectionType) {
                ConnectionType.WIFI -> getWifiSignalStrength(networkCapabilities)
                ConnectionType.CELLULAR -> getCellularSignalStrength(networkCapabilities)
                else -> 0
            }
        } catch (e: Exception) {
            Log.w(TAG, "获取信号强度失败", e)
            0
        }
        
        return NetworkStatus(
            isConnected = isConnected,
            connectionType = connectionType,
            isMetered = isMetered,
            signalStrength = signalStrength
        )
    }
    
    /**
     * 获取WiFi信号强度
     */
    private fun getWifiSignalStrength(capabilities: NetworkCapabilities): Int {
        // 这里可以通过WifiManager获取更详细的信号强度
        // 简化实现，返回固定值
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) 4 else 0
    }
    
    /**
     * 获取移动网络信号强度
     */
    private fun getCellularSignalStrength(capabilities: NetworkCapabilities): Int {
        // 这里可以通过TelephonyManager获取信号强度
        // 简化实现，返回固定值
        return if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) 3 else 0
    }
    
    /**
     * 检查是否连接到互联网
     */
    fun isConnectedToInternet(): Boolean {
        return getCurrentNetworkStatus().isConnected
    }
    
    /**
     * 检查是否为计费网络
     */
    fun isMeteredConnection(): Boolean {
        return getCurrentNetworkStatus().isMetered
    }
    
    /**
     * 获取连接类型显示名称
     */
    fun getConnectionDisplayName(connectionType: ConnectionType): String {
        return when (connectionType) {
            ConnectionType.NONE -> "无网络"
            ConnectionType.WIFI -> "WiFi"
            ConnectionType.CELLULAR -> "移动网络"
            ConnectionType.ETHERNET -> "有线网络"
            ConnectionType.OTHER -> "其他网络"
        }
    }
    
    /**
     * 获取信号强度描述
     */
    fun getSignalStrengthDescription(strength: Int): String {
        return when (strength) {
            0 -> "无信号"
            1 -> "信号很弱"
            2 -> "信号较弱"
            3 -> "信号良好"
            4 -> "信号很强"
            else -> "未知"
        }
    }
} 