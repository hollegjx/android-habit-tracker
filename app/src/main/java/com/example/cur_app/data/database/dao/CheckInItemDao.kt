package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.CheckInItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * 打卡项目数据访问对象
 * 定义打卡项目相关的数据库操作
 */
@Dao
interface CheckInItemDao {
    
    // ========== 查询操作 ==========
    
    /**
     * 获取所有活跃的打卡项目
     */
    @Query("SELECT * FROM checkin_items WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getAllActiveItems(): Flow<List<CheckInItemEntity>>
    
    /**
     * 根据类型获取打卡项目
     */
    @Query("SELECT * FROM checkin_items WHERE type = :type AND isActive = 1 ORDER BY createdAt ASC")
    fun getItemsByType(type: String): Flow<List<CheckInItemEntity>>
    
    /**
     * 根据ID获取打卡项目
     */
    @Query("SELECT * FROM checkin_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): CheckInItemEntity?
    
    /**
     * 根据ID获取打卡项目（Flow版本）
     */
    @Query("SELECT * FROM checkin_items WHERE id = :itemId")
    fun getItemByIdFlow(itemId: Long): Flow<CheckInItemEntity?>
    
    /**
     * 获取所有项目（包括已停用的）
     */
    @Query("SELECT * FROM checkin_items ORDER BY isActive DESC, createdAt ASC")
    fun getAllItems(): Flow<List<CheckInItemEntity>>
    
    /**
     * 搜索打卡项目
     */
    @Query("SELECT * FROM checkin_items WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isActive = 1")
    fun searchItems(query: String): Flow<List<CheckInItemEntity>>
    
    // ========== 插入操作 ==========
    
    /**
     * 插入新的打卡项目
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CheckInItemEntity): Long
    
    /**
     * 批量插入打卡项目
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CheckInItemEntity>): List<Long>
    
    // ========== 更新操作 ==========
    
    /**
     * 更新打卡项目
     */
    @Update
    suspend fun updateItem(item: CheckInItemEntity)
    
    /**
     * 批量更新打卡项目
     */
    @Update
    suspend fun updateItems(items: List<CheckInItemEntity>)
    
    /**
     * 切换项目激活状态
     */
    @Query("UPDATE checkin_items SET isActive = NOT isActive, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun toggleItemActive(
        itemId: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    // ========== 删除操作 ==========
    
    /**
     * 删除打卡项目
     */
    @Delete
    suspend fun deleteItem(item: CheckInItemEntity)
    
    /**
     * 根据ID删除打卡项目
     */
    @Query("DELETE FROM checkin_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)
    
    /**
     * 删除所有非活跃项目
     */
    @Query("DELETE FROM checkin_items WHERE isActive = 0")
    suspend fun deleteInactiveItems()
    
    /**
     * 删除所有项目（重置功能用）
     */
    @Query("DELETE FROM checkin_items")
    suspend fun deleteAllItems()
    
    /**
     * 获取所有项目（同步版本，重置功能用）
     */
    @Query("SELECT * FROM checkin_items")
    suspend fun getAllItemsSync(): List<CheckInItemEntity>
    
    // ========== 统计查询 ==========
    
    /**
     * 获取活跃项目总数
     */
    @Query("SELECT COUNT(*) FROM checkin_items WHERE isActive = 1")
    suspend fun getActiveItemCount(): Int
    
    /**
     * 根据类型获取项目数量
     */
    @Query("SELECT COUNT(*) FROM checkin_items WHERE type = :type AND isActive = 1")
    suspend fun getItemCountByType(type: String): Int
    
    /**
     * 根据类型获取激活的项目列表
     */
    @Query("SELECT * FROM checkin_items WHERE type = :type AND isActive = 1 ORDER BY createdAt ASC")
    suspend fun getActiveItemsByType(type: String): List<CheckInItemEntity>
    
    /**
     * 获取所有类型列表
     */
    @Query("SELECT DISTINCT type FROM checkin_items WHERE isActive = 1")
    suspend fun getAllTypes(): List<String>
} 