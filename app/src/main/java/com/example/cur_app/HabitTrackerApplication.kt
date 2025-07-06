package com.example.cur_app

import android.app.Application
import android.util.Log
import com.example.cur_app.data.database.DefaultDataInitializer
import com.example.cur_app.data.database.HabitTrackerDatabase
import com.example.cur_app.data.local.AiCharacterManager
import com.example.cur_app.data.local.SelectedAiCharacter
import com.example.cur_app.data.remote.service.AiCharacterSyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI习惯追踪应用主类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class HabitTrackerApplication : Application() {
    
    @Inject
    lateinit var aiCharacterSyncService: AiCharacterSyncService
    
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
                
                // 同步AI角色数据（如果需要）
                syncAiCharactersIfNeeded()
                
                // 初始化AI角色管理器
                initializeAiCharacterManager(database)
                
            } catch (e: Exception) {
                Log.e(TAG, "数据库访问失败: ${e.message}", e)
            }
        }
    }
    
    /**
     * 同步AI角色数据（如果需要）
     */
    private suspend fun syncAiCharactersIfNeeded() {
        try {
            Log.d(TAG, "检查是否需要同步AI角色数据")
            
            if (aiCharacterSyncService.shouldSync()) {
                Log.d(TAG, "开始同步AI角色数据...")
                val result = aiCharacterSyncService.syncAiCharacters()
                
                result.onSuccess {
                    Log.d(TAG, "AI角色数据同步成功")
                }.onFailure { error ->
                    Log.e(TAG, "AI角色数据同步失败: ${error.message}", error)
                }
            } else {
                Log.d(TAG, "AI角色数据无需同步")
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI角色同步检查失败: ${e.message}", e)
        }
    }
    
    /**
     * 初始化AI角色管理器，从数据库加载当前选择的AI角色
     */
    private suspend fun initializeAiCharacterManager(database: HabitTrackerDatabase) {
        try {
            Log.d(TAG, "开始初始化AI角色管理器")
            val selectedCharacter = database.aiCharacterDao().getSelectedCharacter()
            
            if (selectedCharacter != null) {
                Log.d(TAG, "找到已选择的AI角色: ${selectedCharacter.name}")
                val uiModel = selectedCharacter.toUiModel()
                AiCharacterManager.updateCurrentCharacter(
                    SelectedAiCharacter(
                        id = uiModel.id,
                        name = uiModel.name,
                        iconEmoji = uiModel.iconEmoji,
                        subtitle = uiModel.subtitle,
                        backgroundColor = uiModel.backgroundColor
                    )
                )
            } else {
                Log.d(TAG, "没有找到已选择的AI角色，使用默认角色")
                // 如果没有选择的角色，选择第一个可用的角色
                val firstCharacter = database.aiCharacterDao().getAllActiveCharacters().let { flow ->
                    // 由于这是一个Flow，我们需要收集第一个值
                    var characters = listOf<com.example.cur_app.data.database.entities.AiCharacterEntity>()
                    flow.collect { characters = it }
                    characters.firstOrNull()
                }
                
                firstCharacter?.let { character ->
                    Log.d(TAG, "自动选择第一个AI角色: ${character.name}")
                    database.aiCharacterDao().selectCharacter(character.id)
                    val uiModel = character.toUiModel()
                    AiCharacterManager.updateCurrentCharacter(
                        SelectedAiCharacter(
                            id = uiModel.id,
                            name = uiModel.name,
                            iconEmoji = uiModel.iconEmoji,
                            subtitle = uiModel.subtitle,
                            backgroundColor = uiModel.backgroundColor
                        )
                    )
                }
            }
            Log.d(TAG, "AI角色管理器初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "AI角色管理器初始化失败: ${e.message}", e)
        }
    }
} 