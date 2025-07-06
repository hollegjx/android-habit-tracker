package com.example.cur_app.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room数据库类型转换器
 * 用于处理复杂数据类型的序列化和反序列化
 */
class DatabaseConverters {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // ========== 字符串列表转换 ==========
    
    /**
     * 将字符串列表转换为JSON字符串
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    
    /**
     * 将JSON字符串转换为字符串列表
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ========== 整数列表转换 ==========
    
    /**
     * 将整数列表转换为JSON字符串
     */
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return json.encodeToString(value)
    }
    
    /**
     * 将JSON字符串转换为整数列表
     */
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return try {
            json.decodeFromString<List<Int>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ========== Map转换 ==========
    
    /**
     * 将Map转换为JSON字符串
     */
    @TypeConverter
    fun fromStringMap(value: Map<String, Any>): String {
        return json.encodeToString(value)
    }
    
    /**
     * 将JSON字符串转换为Map
     */
    @TypeConverter
    fun toStringMap(value: String): Map<String, Any> {
        return try {
            json.decodeFromString<Map<String, Any>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
    

} 