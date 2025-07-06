package com.example.cur_app.data.repository

import com.example.cur_app.data.database.dao.AiCharacterDao
import com.example.cur_app.data.database.entities.AiCharacterEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI角色数据仓储
 * 管理AI角色的数据访问逻辑
 */
@Singleton
class AiCharacterRepository @Inject constructor(
    private val aiCharacterDao: AiCharacterDao
) {
    
    /**
     * 获取所有激活的AI角色
     */
    fun getAllActiveCharacters(): Flow<List<AiCharacterEntity>> {
        return aiCharacterDao.getAllActiveCharacters()
    }
    
    /**
     * 获取当前选中的AI角色
     */
    suspend fun getSelectedCharacter(): AiCharacterEntity? {
        return aiCharacterDao.getSelectedCharacter()
    }
    
    /**
     * 选择AI角色
     */
    suspend fun selectCharacter(characterId: Long) {
        aiCharacterDao.selectCharacter(characterId)
    }
    
    /**
     * 根据ID获取角色
     */
    suspend fun getCharacterById(characterId: Long): AiCharacterEntity? {
        return aiCharacterDao.getCharacterById(characterId)
    }
    
    /**
     * 增加角色使用次数
     */
    suspend fun incrementUsage(characterId: Long) {
        aiCharacterDao.incrementUsage(characterId)
    }
}