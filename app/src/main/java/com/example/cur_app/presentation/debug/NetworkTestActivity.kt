package com.example.cur_app.presentation.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.network.NetworkManager
import com.example.cur_app.network.NetworkTestResult
import com.example.cur_app.network.SimpleNetworkTester
import com.example.cur_app.network.EnhancedNetworkTester
import com.example.cur_app.ui.theme.Cur_appTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkTestActivity : ComponentActivity() {
    private val viewModel: NetworkTestViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cur_appTheme {
                NetworkTestScreen(viewModel)
            }
        }
    }
}

@HiltViewModel
class NetworkTestViewModel @Inject constructor(
    private val networkManager: NetworkManager,
    private val networkTester: SimpleNetworkTester,
    private val enhancedNetworkTester: EnhancedNetworkTester
) : ViewModel() {
    
    private val _testResults = MutableStateFlow<List<TestResult>>(emptyList())
    val testResults: StateFlow<List<TestResult>> = _testResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    data class TestResult(
        val testName: String,
        val status: TestStatus,
        val message: String,
        val details: String = ""
    )
    
    enum class TestStatus { SUCCESS, ERROR, LOADING, PENDING }
    
    fun runAllTests() {
        viewModelScope.launch {
            _isLoading.value = true
            _testResults.value = emptyList()
            
            val results = mutableListOf<TestResult>()
            
            // 测试1: 设备网络状态检查
            results.add(TestResult("设备网络状态检查", TestStatus.LOADING, "检查中..."))
            _testResults.value = results.toList()
            
            val networkStatus = enhancedNetworkTester.checkNetworkStatus()
            results[results.size - 1] = TestResult(
                "设备网络状态检查",
                if (networkStatus.isConnected) TestStatus.SUCCESS else TestStatus.ERROR,
                "网络类型: ${networkStatus.networkType}",
                networkStatus.details
            )
            _testResults.value = results.toList()
            
            if (!networkStatus.isConnected) {
                _isLoading.value = false
                return@launch
            }
            
            // 测试2: 多服务器URL连通性测试
            results.add(TestResult("多服务器URL连通性测试", TestStatus.LOADING, "正在测试多个URL..."))
            _testResults.value = results.toList()
            
            val serverResults = enhancedNetworkTester.testMultipleServers()
            val successfulServer = serverResults.find { it.isSuccess }
            
            results[results.size - 1] = TestResult(
                "多服务器URL连通性测试",
                if (successfulServer != null) TestStatus.SUCCESS else TestStatus.ERROR,
                if (successfulServer != null) "找到可用服务器: ${successfulServer.baseUrl}" else "所有服务器都无法连接",
                serverResults.joinToString("\n") { "${it.baseUrl}: ${if (it.isSuccess) "成功" else it.error ?: "失败"}" }
            )
            _testResults.value = results.toList()
            
            // 添加详细的服务器测试结果
            for (serverResult in serverResults) {
                // 健康检查结果
                if (serverResult.healthCheck != null) {
                    results.add(TestResult(
                        "健康检查 - ${serverResult.baseUrl}",
                        if (serverResult.healthCheck.isSuccess) TestStatus.SUCCESS else TestStatus.ERROR,
                        "HTTP ${serverResult.healthCheck.statusCode} (${serverResult.healthCheck.duration}ms)",
                        serverResult.healthCheck.responseBody.take(200) + if (serverResult.healthCheck.responseBody.length > 200) "..." else ""
                    ))
                }
                
                // API Ping结果
                if (serverResult.apiPing != null) {
                    results.add(TestResult(
                        "API Ping - ${serverResult.baseUrl}",
                        if (serverResult.apiPing.isSuccess) TestStatus.SUCCESS else TestStatus.ERROR,
                        "HTTP ${serverResult.apiPing.statusCode} (${serverResult.apiPing.duration}ms)",
                        serverResult.apiPing.responseBody.take(200) + if (serverResult.apiPing.responseBody.length > 200) "..." else ""
                    ))
                }
                
                // 登录测试结果
                if (serverResult.loginTest != null) {
                    results.add(TestResult(
                        "登录测试 - ${serverResult.baseUrl}",
                        if (serverResult.loginTest.isSuccess) TestStatus.SUCCESS else TestStatus.ERROR,
                        "HTTP ${serverResult.loginTest.statusCode} (${serverResult.loginTest.duration}ms)",
                        serverResult.loginTest.responseBody.take(200) + if (serverResult.loginTest.responseBody.length > 200) "..." else ""
                    ))
                }
                
                _testResults.value = results.toList()
            }
            
            _isLoading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkTestScreen(viewModel: NetworkTestViewModel) {
    val testResults by viewModel.testResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("网络连接测试") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.runAllTests() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "测试中..." else "开始网络测试")
            }
            
            if (testResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "测试结果",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        testResults.forEach { result ->
                            TestResultItem(result)
                        }
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "配置信息",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "服务器地址: http://38.207.179.136:3000",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "健康检查: /health",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "API Ping: /api/ping",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "登录端点: /api/auth/login",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TestResultItem(result: NetworkTestViewModel.TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (result.status) {
                NetworkTestViewModel.TestStatus.SUCCESS -> Color(0xFFE8F5E8)
                NetworkTestViewModel.TestStatus.ERROR -> Color(0xFFFFEBEE)
                NetworkTestViewModel.TestStatus.LOADING -> Color(0xFFF3E5F5)
                NetworkTestViewModel.TestStatus.PENDING -> Color(0xFFF5F5F5)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.testName,
                    style = MaterialTheme.typography.titleMedium
                )
                when (result.status) {
                    NetworkTestViewModel.TestStatus.SUCCESS -> Text("✅", fontSize = 18.sp)
                    NetworkTestViewModel.TestStatus.ERROR -> Text("❌", fontSize = 18.sp)
                    NetworkTestViewModel.TestStatus.LOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    NetworkTestViewModel.TestStatus.PENDING -> Text("⏳", fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
                color = when (result.status) {
                    NetworkTestViewModel.TestStatus.SUCCESS -> Color(0xFF4CAF50)
                    NetworkTestViewModel.TestStatus.ERROR -> Color(0xFFE91E63)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (result.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.details,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}