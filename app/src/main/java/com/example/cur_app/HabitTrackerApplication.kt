package com.example.cur_app

import android.app.Application
import android.util.Log
import com.example.cur_app.data.database.DefaultDataInitializer
import com.example.cur_app.data.database.HabitTrackerDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AI习惯追踪应用主类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class HabitTrackerApplication : Application() {
    
    companion object {
        private const val TAG = "HabitTrackerApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "应用启动，开始初始化")
        
        // 正常初始化数据库
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始初始化数据库")
                val database = HabitTrackerDatabase.getDatabase(this@HabitTrackerApplication)
                
                // 检查数据库状态
                val itemCount = database.checkInItemDao().getActiveItemCount()
                Log.d(TAG, "数据库访问成功，当前项目数量: $itemCount")
                
                if (itemCount == 0) {
                    Log.w(TAG, "数据库为空，开始强制初始化默认数据")
                    try {
                        // 强制调用DefaultDataInitializer
                        DefaultDataInitializer.initializeDefaultItems(this@HabitTrackerApplication, database)
                        Log.d(TAG, "强制初始化完成")
                        
                        // 再次检查数据
                        val newItemCount = database.checkInItemDao().getActiveItemCount()
                        Log.d(TAG, "初始化后项目数量: $newItemCount")
                    } catch (e: Exception) {
                        Log.e(TAG, "强制初始化失败: ${e.message}", e)
                    }
                } else {
                    Log.d(TAG, "数据库状态正常，包含 $itemCount 个项目")
                }
            } catch (e: Exception) {
                Log.e(TAG, "数据库访问失败: ${e.message}", e)
            }
        }
    }
} 