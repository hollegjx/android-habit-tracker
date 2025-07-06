package com.example.cur_app.presentation.debug

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cur_app.data.repository.AiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AiTestActivity : AppCompatActivity() {
    
    @Inject
    lateinit var aiRepository: AiRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.e("AiTestActivity", "🎯 测试AI新架构开始")
        Log.e("AiTestActivity", "🎯 AiRepository实例: $aiRepository")
        
        lifecycleScope.launch {
            try {
                Log.e("AiTestActivity", "🎯 开始调用AI API...")
                val result = aiRepository.chatWithAi("你好", null, emptyList())
                Log.e("AiTestActivity", "🎯 AI API调用结果: $result")
            } catch (e: Exception) {
                Log.e("AiTestActivity", "🎯 AI API调用异常: ${e.message}", e)
            }
        }
        
        finish()
    }
}