 package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.LevelDefinitionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 等级定义数据访问对象
 */
@Dao
interface LevelDefinitionDao {
    
    // ============ 查询操作 ============
    
    /**
     * 获取指定类别的所有等级定义（按等级索引排序）
     */
    @Query("SELECT * FROM level_definitions WHERE category = :category ORDER BY level_index ASC")
    suspend fun getLevelsByCategory(category: String): List<LevelDefinitionEntity>
    
    /**
     * 获取指定类别的所有等级定义（Flow版本）
     */
    @Query("SELECT * FROM level_definitions WHERE category = :category ORDER BY level_index ASC")
    fun getLevelsByCategoryFlow(category: String): Flow<List<LevelDefinitionEntity>>
    
    /**
     * 获取所有等级定义
     */
    @Query("SELECT * FROM level_definitions ORDER BY category, level_index ASC")
    suspend fun getAllLevels(): List<LevelDefinitionEntity>
    
    /**
     * 获取指定类别和等级索引的等级定义
     */
    @Query("SELECT * FROM level_definitions WHERE category = :category AND level_index = :levelIndex LIMIT 1")
    suspend fun getLevelByIndex(category: String, levelIndex: Int): LevelDefinitionEntity?
    
    /**
     * 检查是否存在默认等级配置
     */
    @Query("SELECT COUNT(*) FROM level_definitions WHERE is_default = 1")
    suspend fun hasDefaultLevels(): Int
    
    /**
     * 获取指定类别的最高等级索引
     */
    @Query("SELECT MAX(level_index) FROM level_definitions WHERE category = :category")
    suspend fun getMaxLevelIndex(category: String): Int?
    
    // ============ 插入操作 ============
    
    /**
     * 插入等级定义
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelDefinitionEntity): Long
    
    /**
     * 批量插入等级定义
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevels(levels: List<LevelDefinitionEntity>)
    
    // ============ 更新操作 ============
    
    /**
     * 更新等级定义
     */
    @Update
    suspend fun updateLevel(level: LevelDefinitionEntity)
    
    /**
     * 更新等级称号
     */
    @Query("UPDATE level_definitions SET title = :title, updated_at = :updateTime WHERE category = :category AND level_index = :levelIndex")
    suspend fun updateLevelTitle(category: String, levelIndex: Int, title: String, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新经验值要求
     */
    @Query("UPDATE level_definitions SET exp_threshold = :expThreshold, updated_at = :updateTime WHERE category = :category AND level_index = :levelIndex")
    suspend fun updateExpThreshold(category: String, levelIndex: Int, expThreshold: Int, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新等级图标
     */
    @Query("UPDATE level_definitions SET icon = :icon, updated_at = :updateTime WHERE category = :category AND level_index = :levelIndex")
    suspend fun updateLevelIcon(category: String, levelIndex: Int, icon: String, updateTime: Long = System.currentTimeMillis())
    
    // ============ 删除操作 ============
    
    /**
     * 删除指定类别的所有等级定义
     */
    @Query("DELETE FROM level_definitions WHERE category = :category")
    suspend fun deleteLevelsByCategory(category: String)
    
    /**
     * 删除指定等级定义
     */
    @Query("DELETE FROM level_definitions WHERE category = :category AND level_index = :levelIndex")
    suspend fun deleteLevel(category: String, levelIndex: Int)
    
    /**
     * 清空所有等级定义
     */
    @Query("DELETE FROM level_definitions")
    suspend fun deleteAllLevels()
    
    /**
     * 删除非默认等级定义（保留默认配置）
     */
    @Query("DELETE FROM level_definitions WHERE is_default = 0")
    suspend fun deleteCustomLevels()
}