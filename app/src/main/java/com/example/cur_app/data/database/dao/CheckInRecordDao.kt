package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.CheckInRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 打卡记录数据访问对象
 * 定义打卡记录相关的数据库操作
 */
@Dao
interface CheckInRecordDao {
    
    // ========== 查询操作 ==========
    
    /**
     * 获取指定项目的所有记录
     */
    @Query("SELECT * FROM checkin_records WHERE itemId = :itemId ORDER BY date DESC")
    fun getRecordsByItem(itemId: Long): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取指定日期的所有记录
     */
    @Query("SELECT * FROM checkin_records WHERE date = :date ORDER BY createdAt ASC")
    fun getRecordsByDate(date: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取指定日期的所有记录（同步版本）
     */
    @Query("SELECT * FROM checkin_records WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getRecordsByDateSync(date: String): List<CheckInRecordEntity>
    
    /**
     * 获取指定日期的所有记录（Flow版本）
     */
    @Query("SELECT * FROM checkin_records WHERE date = :date ORDER BY createdAt ASC")
    fun getRecordsByDateFlow(date: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取指定项目在指定日期的记录
     */
    @Query("SELECT * FROM checkin_records WHERE itemId = :itemId AND date = :date")
    suspend fun getRecordByItemAndDate(itemId: Long, date: String): CheckInRecordEntity?
    
    /**
     * 获取指定项目在指定日期的记录（Flow版本）
     */
    @Query("SELECT * FROM checkin_records WHERE itemId = :itemId AND date = :date")
    fun getRecordByItemAndDateFlow(itemId: Long, date: String): Flow<CheckInRecordEntity?>
    
    /**
     * 获取指定日期范围内的记录
     */
    @Query("SELECT * FROM checkin_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取指定项目在日期范围内的记录
     */
    @Query("SELECT * FROM checkin_records WHERE itemId = :itemId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsByItemAndDateRange(itemId: Long, startDate: String, endDate: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取指定项目的已完成记录
     */
    @Query("SELECT * FROM checkin_records WHERE itemId = :itemId AND isCompleted = 1 ORDER BY date DESC")
    fun getCompletedRecordsByItem(itemId: Long): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取今日所有记录
     */
    @Query("SELECT * FROM checkin_records WHERE date = date('now', 'localtime') ORDER BY createdAt ASC")
    fun getTodayRecords(): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取本周所有记录
     */
    @Query("""
        SELECT * FROM checkin_records 
        WHERE date BETWEEN date('now', 'weekday 0', '-6 days', 'localtime') 
        AND date('now', 'localtime') 
        ORDER BY date ASC
    """)
    fun getThisWeekRecords(): Flow<List<CheckInRecordEntity>>
    
    /**
     * 获取本月所有记录
     */
    @Query("""
        SELECT * FROM checkin_records 
        WHERE date BETWEEN date('now', 'start of month', 'localtime') 
        AND date('now', 'localtime') 
        ORDER BY date ASC
    """)
    fun getThisMonthRecords(): Flow<List<CheckInRecordEntity>>
    
    // ========== 插入操作 ==========
    
    /**
     * 插入新记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CheckInRecordEntity): Long
    
    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<CheckInRecordEntity>): List<Long>
    
    // ========== 更新操作 ==========
    
    /**
     * 更新记录
     */
    @Update
    suspend fun updateRecord(record: CheckInRecordEntity)
    
    /**
     * 批量更新记录
     */
    @Update
    suspend fun updateRecords(records: List<CheckInRecordEntity>)
    
    /**
     * 标记项目为已完成
     */
    @Query("""
        UPDATE checkin_records 
        SET isCompleted = 1, 
            actualValue = :value, 
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE itemId = :itemId AND date = :date
    """)
    suspend fun markItemCompleted(
        itemId: Long,
        date: String,
        value: Int,
        completedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 取消项目完成状态
     */
    @Query("""
        UPDATE checkin_records 
        SET isCompleted = 0, 
            actualValue = 0, 
            completedAt = NULL,
            updatedAt = :updatedAt
        WHERE itemId = :itemId AND date = :date
    """)
    suspend fun unmarkItemCompleted(
        itemId: Long,
        date: String,
        updatedAt: Long = System.currentTimeMillis()
    )
    
    // ========== 删除操作 ==========
    
    /**
     * 删除记录
     */
    @Delete
    suspend fun deleteRecord(record: CheckInRecordEntity)
    
    /**
     * 根据ID删除记录
     */
    @Query("DELETE FROM checkin_records WHERE id = :recordId")
    suspend fun deleteRecordById(recordId: Long)
    
    /**
     * 删除指定项目的所有记录
     */
    @Query("DELETE FROM checkin_records WHERE itemId = :itemId")
    suspend fun deleteRecordsByItem(itemId: Long)
    
    /**
     * 删除指定日期范围的记录
     */
    @Query("DELETE FROM checkin_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun deleteRecordsByDateRange(startDate: String, endDate: String)
    
    /**
     * 删除所有记录（重置功能用）
     */
    @Query("DELETE FROM checkin_records")
    suspend fun deleteAllRecords()
    
    /**
     * 获取所有记录（同步版本，重置功能用）
     */
    @Query("SELECT * FROM checkin_records")
    suspend fun getAllRecordsSync(): List<CheckInRecordEntity>
    
    // ========== 统计查询 ==========
    
    /**
     * 获取指定项目的完成天数
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE itemId = :itemId AND isCompleted = 1")
    suspend fun getCompletedDaysCount(itemId: Long): Int
    
    /**
     * 获取指定项目的总记录天数
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE itemId = :itemId")
    suspend fun getTotalDaysCount(itemId: Long): Int
    
    /**
     * 获取今日完成的项目数量
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE date = :date AND isCompleted = 1")
    suspend fun getTodayCompletedCount(date: String): Int
    
    /**
     * 获取今日总项目数量
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE date = :date")
    suspend fun getTodayTotalCount(date: String): Int
    
    /**
     * 获取指定类型和日期的已完成记录
     */
    @Query("""
        SELECT r.* FROM checkin_records r
        INNER JOIN checkin_items i ON r.itemId = i.id
        WHERE i.type = :type AND r.date = :date AND r.isCompleted = 1
        ORDER BY r.completedAt ASC
    """)
    suspend fun getCompletedRecordsByTypeAndDate(type: String, date: String): List<CheckInRecordEntity>
} 