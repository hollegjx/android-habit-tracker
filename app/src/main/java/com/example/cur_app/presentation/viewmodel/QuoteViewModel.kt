package com.example.cur_app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cur_app.network.model.DailyQuote
import com.example.cur_app.network.service.DailyQuoteService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

/**
 * 每日一句ViewModel
 * 管理每日一句的加载和状态
 */
@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val dailyQuoteService: DailyQuoteService
) : ViewModel() {

    companion object {
        private const val TAG = "QuoteViewModel"
    }

    // ========== UI状态定义 ==========
    
    private val _uiState = MutableStateFlow(QuoteUiState())
    val uiState: StateFlow<QuoteUiState> = _uiState.asStateFlow()
    
    private val _currentQuote = MutableStateFlow(DailyQuote.getDefault())
    val currentQuote: StateFlow<DailyQuote> = _currentQuote.asStateFlow()
    
    // ========== 初始化 ==========
    
    init {
        loadDailyQuote()
    }
    
    // ========== 公共方法 ==========
    
    /**
     * 加载每日一句
     */
    fun loadDailyQuote() {
        viewModelScope.launch {
            Log.d(TAG, "开始加载每日一句")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val quote = dailyQuoteService.getDailyQuote()
                _currentQuote.value = quote
                
                // 判断是否来自网络（通过内容判断）
                val isFromNetwork = quote.content != DailyQuote.getDefault().content &&
                                  !DailyQuote.getLocalQuotes().any { it.content == quote.content }
                
                Log.d(TAG, "加载成功，来源：${if (isFromNetwork) "网络" else "本地"}")
                Log.d(TAG, "内容：${quote.content}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isFromNetwork = isFromNetwork
                )
            } catch (e: Exception) {
                Log.e(TAG, "加载每日一句失败", e)
                val fallbackQuote = DailyQuote.getDefault()
                _currentQuote.value = fallbackQuote
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message,
                    isFromNetwork = false
                )
            }
        }
    }
    
    /**
     * 刷新每日一句
     */
    fun refresh() {
        viewModelScope.launch {
            Log.d(TAG, "开始刷新每日一句")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val quote = dailyQuoteService.refresh()
                _currentQuote.value = quote
                
                Log.d(TAG, "刷新成功，来源：本地正能量语录")
                Log.d(TAG, "内容：${quote.content}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isFromNetwork = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "刷新每日一句失败", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * 强制重置所有数据（解决缓存污染问题）
     */
    fun forceReset() {
        viewModelScope.launch {
            Log.d(TAG, "强制重置所有数据")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val quote = dailyQuoteService.resetAllData()
                _currentQuote.value = quote
                
                Log.d(TAG, "重置成功，来源：本地正能量语录")
                Log.d(TAG, "内容：${quote.content}")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isFromNetwork = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "重置数据失败", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 每日一句UI状态
 */
data class QuoteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFromNetwork: Boolean = false
) 