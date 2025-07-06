package com.example.cur_app.data.database.dao

import androidx.room.*
import com.example.cur_app.data.database.entities.AiCharacterEntity
import kotlinx.coroutines.flow.Flow

/**
 * AI角色数据访问对象
 */
@Dao
interface AiCharacterDao {
    
    @Query("SELECT * FROM ai_characters WHERE isActive = 1 ORDER BY isDefault DESC, name ASC")
    fun getAllActiveCharacters(): Flow<List<AiCharacterEntity>>
    
    @Query("SELECT * FROM ai_characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: Long): AiCharacterEntity?
    
    @Query("SELECT * FROM ai_characters WHERE characterId = :characterId")
    suspend fun getCharacterByCharacterId(characterId: String): AiCharacterEntity?
    
    @Query("SELECT * FROM ai_characters WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedCharacter(): AiCharacterEntity?
    
    @Query("SELECT * FROM ai_characters WHERE type = :type AND isActive = 1")
    fun getCharactersByType(type: String): Flow<List<AiCharacterEntity>>
    
    @Query("SELECT COUNT(*) FROM ai_characters")
    suspend fun getCharacterCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: AiCharacterEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<AiCharacterEntity>): List<Long>
    
    @Update
    suspend fun updateCharacter(character: AiCharacterEntity)
    
    @Query("UPDATE ai_characters SET isSelected = CASE WHEN id = :characterId THEN 1 ELSE 0 END")
    suspend fun selectCharacter(characterId: Long)
    
    @Query("UPDATE ai_characters SET usageCount = usageCount + 1, lastUsedAt = :timestamp WHERE id = :characterId")
    suspend fun incrementUsage(characterId: Long, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteCharacter(character: AiCharacterEntity)
    
    @Query("DELETE FROM ai_characters")
    suspend fun deleteAllCharacters()
} 