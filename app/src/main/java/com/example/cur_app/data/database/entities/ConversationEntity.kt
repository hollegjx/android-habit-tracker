package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * 对话实体
 * 对应服务器端的 conversations 表
 */
@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["conversation_id"], unique = true),
        Index(value = ["type"]),
        Index(value = ["last_message_at"]),
        Index(value = ["created_by"]),
        Index(value = ["is_active"])
    ]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "type")
    val type: String = TYPE_PRIVATE, // private, group, ai
    
    @ColumnInfo(name = "name")
    val name: String? = null,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @ColumnInfo(name = "created_by")
    val createdBy: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "last_message_at")
    val lastMessageAt: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // 对话类型常量
        const val TYPE_PRIVATE = "private"
        const val TYPE_GROUP = "group"
        const val TYPE_AI = "ai"
    }
}