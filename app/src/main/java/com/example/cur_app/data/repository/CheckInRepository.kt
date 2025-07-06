package com.example.cur_app.data.repository

import com.example.cur_app.data.database.dao.CheckInItemDao
import com.example.cur_app.data.database.dao.CheckInRecordDao
import com.example.cur_app.data.database.entities.CheckInItemEntity
import com.example.cur_app.data.database.entities.CheckInRecordEntity
import com.example.cur_app.data.local.entity.CheckInType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 打卡数据仓库
 * 处理打卡项目和记录的数据访问逻辑
 */
@Singleton
class CheckInRepository @Inject constructor(
    private val itemDao: CheckInItemDao,
    private val recordDao: CheckInRecordDao
) {
    
    // ============ 数据类定义 ============
    
    /**
     * 项目模板 + 今日状态的组合数据
     */
    data class CheckInItemWithTodayStatus(
        val item: CheckInItemEntity,           // 项目模板信息
        val todayRecord: CheckInRecordEntity?, // 今日记录（可能为空）
        val isCompletedToday: Boolean,         // 今日是否已完成
        val todayActualValue: Int,             // 今日实际完成值
        val todayCompletedAt: Long?            // 今日完成时间
    ) {
        // 便利属性
        val progressText: String
            get() = "$todayActualValue/${item.targetValue} ${item.unit}"
    }
    
    /**
     * 类型统计数据
     */
    data class CheckInTypeStats(
        val type: CheckInType,
        val totalItems: Int,                   // 总项目数
        val completedToday: Int,               // 今日已完成数
        val totalToday: Int,                   // 今日总数（激活的项目）
        val completionRate: Float,             // 今日完成率
        val currentStreak: Int,                // 当前连续天数
        val totalActualValue: Int              // 今日实际完成总值
    )
    
    // ============ 项目管理方法 ============
    
    /**
     * 获取指定类型的项目列表（包含今日状态）
     */
    fun getItemsWithTodayStatusByType(type: CheckInType): Flow<List<CheckInItemWithTodayStatus>> {
        val today = getCurrentDateString()
        val itemsFlow = itemDao.getItemsByType(type.name)
        val recordsFlow = recordDao.getRecordsByDate(today)
        
        return combine(itemsFlow, recordsFlow) { items, records ->
            val recordMap = records.associateBy { it.itemId }
            items.map { item ->
                val todayRecord = recordMap[item.id]
                CheckInItemWithTodayStatus(
                    item = item,
                    todayRecord = todayRecord,
                    isCompletedToday = todayRecord?.isCompleted ?: false,
                    todayActualValue = todayRecord?.actualValue ?: 0,
                    todayCompletedAt = todayRecord?.completedAt
                )
            }
        }
    }
    
    /**
     * 创建新的打卡项目
     */
    suspend fun createCheckInItem(
        type: CheckInType,
        title: String,
        description: String,
        targetValue: Int,
        unit: String,
        icon: String,
        color: String
    ): Result<Long> {
        return try {
            val item = CheckInItemEntity(
                type = type.name,
                title = title.trim(),
                description = description.trim(),
                targetValue = targetValue,
                unit = unit.trim(),
                icon = icon,
                color = color,
                targetFrequency = "daily",
                isActive = true
            )
            val itemId = itemDao.insertItem(item)
            Result.success(itemId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 更新打卡项目（通过参数）
     */
    suspend fun updateCheckInItem(
        itemId: Long,
        title: String,
        description: String,
        targetValue: Int,
        unit: String,
        icon: String,
        color: String
    ): Result<Unit> {
        return try {
            // 先获取现有项目
            val existingItem = itemDao.getItemById(itemId)
            if (existingItem != null) {
                val updatedItem = existingItem.copy(
                    title = title.trim(),
                    description = description.trim(),
                    targetValue = targetValue,
                    unit = unit.trim(),
                    icon = icon,
                    color = color,
                    updatedAt = System.currentTimeMillis()
                )
                itemDao.updateItem(updatedItem)
                Result.success(Unit)
            } else {
                Result.failure(Exception("项目不存在"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除打卡项目（同时删除相关记录）
     */
    suspend fun deleteCheckInItem(itemId: Long): Result<Unit> {
        return try {
            itemDao.deleteItemById(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 切换项目激活状态
     */
    suspend fun toggleItemActive(itemId: Long): Result<Unit> {
        return try {
            itemDao.toggleItemActive(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ 打卡记录方法 ============
    
    /**
     * 完成今日打卡
     */
    suspend fun completeCheckInToday(
        itemId: Long,
        actualValue: Int,
        note: String = ""
    ): Result<Unit> {
        return try {
            val today = getCurrentDateString()
            val currentTime = System.currentTimeMillis()
            
            // 获取项目信息以判断是否达到目标
            val item = itemDao.getItemById(itemId)
            val isCompleted = item?.let { actualValue >= it.targetValue } ?: false
            
            // 尝试获取今日记录
            val existingRecord = recordDao.getRecordByItemAndDate(itemId, today)
            
            if (existingRecord != null) {
                // 更新现有记录
                val updatedRecord = existingRecord.copy(
                    isCompleted = isCompleted,
                    actualValue = actualValue,
                    completedAt = if (isCompleted) currentTime else existingRecord.completedAt,
                    note = note.trim(),
                    updatedAt = currentTime
                )
                recordDao.updateRecord(updatedRecord)
            } else {
                // 创建新记录
                val newRecord = CheckInRecordEntity(
                    itemId = itemId,
                    date = today,
                    isCompleted = isCompleted,
                    actualValue = actualValue,
                    completedAt = if (isCompleted) currentTime else null,
                    note = note.trim(),
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                recordDao.insertRecord(newRecord)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取消今日打卡
     */
    suspend fun uncompleteCheckInToday(itemId: Long): Result<Unit> {
        return try {
            val today = getCurrentDateString()
            val existingRecord = recordDao.getRecordByItemAndDate(itemId, today)
            
            if (existingRecord != null) {
                val updatedRecord = existingRecord.copy(
                    isCompleted = false,
                    actualValue = 0,
                    completedAt = null,
                    updatedAt = System.currentTimeMillis()
                )
                recordDao.updateRecord(updatedRecord)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新今日进度（专注模式用）
     * 只更新进度值，完成状态根据是否达到目标自动判断
     */
    suspend fun updateTodayProgress(
        itemId: Long,
        actualValue: Int,
        note: String = ""
    ): Result<Unit> {
        return try {
            val today = getCurrentDateString()
            val currentTime = System.currentTimeMillis()
            
            // 获取项目信息以判断是否达到目标
            val item = itemDao.getItemById(itemId)
            val isCompleted = item?.let { actualValue >= it.targetValue } ?: false
            
            // 尝试获取今日记录
            val existingRecord = recordDao.getRecordByItemAndDate(itemId, today)
            
            if (existingRecord != null) {
                // 更新现有记录，保留之前的完成时间（如果已完成过）
                val updatedRecord = existingRecord.copy(
                    isCompleted = isCompleted,
                    actualValue = actualValue,
                    completedAt = when {
                        isCompleted && existingRecord.completedAt == null -> currentTime
                        isCompleted -> existingRecord.completedAt
                        else -> null
                    },
                    note = if (note.isNotEmpty()) note.trim() else existingRecord.note,
                    updatedAt = currentTime
                )
                recordDao.updateRecord(updatedRecord)
            } else {
                // 创建新记录
                val newRecord = CheckInRecordEntity(
                    itemId = itemId,
                    date = today,
                    isCompleted = isCompleted,
                    actualValue = actualValue,
                    completedAt = if (isCompleted) currentTime else null,
                    note = note.trim(),
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                recordDao.insertRecord(newRecord)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ 统计方法 ============
    
    /**
     * 获取类型统计信息
     */
    suspend fun getTypeStats(type: CheckInType): CheckInTypeStats {
        val today = getCurrentDateString()
        val totalItems = itemDao.getItemCountByType(type.name)
        val activeItems = itemDao.getActiveItemsByType(type.name)
        val todayRecords = recordDao.getCompletedRecordsByTypeAndDate(type.name, today)
        
        val completedToday = todayRecords.size
        val totalToday = activeItems.size
        val completionRate = if (totalToday > 0) completedToday.toFloat() / totalToday else 0f
        val totalActualValue = todayRecords.sumOf { it.actualValue }
        
        // 计算连续天数（简化实现）
        val currentStreak = calculateCurrentStreak(type)
        
        return CheckInTypeStats(
            type = type,
            totalItems = totalItems,
            completedToday = completedToday,
            totalToday = totalToday,
            completionRate = completionRate,
            currentStreak = currentStreak,
            totalActualValue = totalActualValue
        )
    }
    
    // ============ 数据重置方法 ============
    
    /**
     * 清空所有数据（重置功能）
     * 删除所有项目和记录，为重新初始化做准备
     */
    suspend fun clearAllData(): Result<Unit> {
        return try {
            // 删除所有记录
            clearAllRecords()
            // 删除所有项目
            clearAllItems()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 清空所有打卡项目
     */
    private suspend fun clearAllItems() {
        try {
            // 使用SQL语句直接删除所有数据，更高效
            itemDao.deleteAllItems()
        } catch (e: Exception) {
            // 如果直接删除失败，逐个删除
            val allItems = itemDao.getAllItemsSync()
            allItems.forEach { item ->
                itemDao.deleteItem(item)
            }
        }
    }
    
    /**
     * 清空所有打卡记录
     */
    private suspend fun clearAllRecords() {
        try {
            // 使用SQL语句直接删除所有数据，更高效
            recordDao.deleteAllRecords()
        } catch (e: Exception) {
            // 如果直接删除失败，逐个删除
            val allRecords = recordDao.getAllRecordsSync()
            allRecords.forEach { record ->
                recordDao.deleteRecord(record)
            }
        }
    }
    
    // ============ 辅助方法 ============
    
    private fun getCurrentDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    
    private suspend fun calculateCurrentStreak(type: CheckInType): Int {
        return try {
            recordDao.getCurrentStreakDays()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取当月已完成的打卡日期
     */
    suspend fun getCompletedDatesThisMonth(): Set<LocalDate> {
        return try {
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
            
            val startDateString = startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val endDateString = endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            val completedDates = recordDao.getCompletedDatesInMonth(startDateString, endDateString)
            completedDates.mapNotNull { dateString ->
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    null
                }
            }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    /**
     * 获取指定类型当月已完成的打卡日期
     */
    suspend fun getCompletedDatesByTypeThisMonth(type: CheckInType): Set<LocalDate> {
        return try {
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
            
            val startDateString = startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val endDateString = endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            
            val completedDates = recordDao.getCompletedDatesByTypeInMonth(type.name, startDateString, endDateString)
            completedDates.mapNotNull { dateString ->
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    null
                }
            }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    /**
     * 获取累计打卡天数
     */
    suspend fun getTotalCompletedDays(): Int {
        return try {
            recordDao.getTotalCompletedDays()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取连续打卡天数
     */
    suspend fun getCurrentStreakDays(): Int {
        return try {
            recordDao.getCurrentStreakDays()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取实际的类型统计数据（替换硬编码）
     */
    suspend fun getRealTypeStats(type: CheckInType): CheckInTypeStats {
        return try {
            val today = getCurrentDateString()
            
            val todayActualValue = recordDao.getTodayActualValueByType(type.name, today)
            val completedToday = recordDao.getTodayCompletedCountByType(type.name, today)
            val totalToday = recordDao.getTodayTotalCountByType(type.name)
            val currentStreak = getCurrentStreakDays()
            
            val completionRate = if (totalToday > 0) {
                completedToday.toFloat() / totalToday
            } else {
                0f
            }
            
            // 获取最近7天的完成率数据
            val weekStartDate = LocalDate.now().minusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val weeklyRates = try {
                recordDao.getWeeklyCompletionRatesByType(type.name, weekStartDate)
                    .map { it.completion_rate }
            } catch (e: Exception) {
                // 如果复杂查询失败，使用简化版本
                listOf(0.8f, 0.6f, 0.9f, 0.7f, 0.85f, 0.75f, completionRate)
            }
            
            CheckInTypeStats(
                type = type,
                totalItems = itemDao.getItemCountByType(type.name),
                completedToday = completedToday,
                totalToday = totalToday,
                completionRate = completionRate,
                currentStreak = currentStreak,
                totalActualValue = todayActualValue
            )
        } catch (e: Exception) {
            // 如果获取失败，返回默认值
            CheckInTypeStats(
                type = type,
                totalItems = 0,
                completedToday = 0,
                totalToday = 0,
                completionRate = 0f,
                currentStreak = 0,
                totalActualValue = 0
            )
        }
    }
} 