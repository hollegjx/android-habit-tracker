package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

/**
 * 对话参与者实体
 * 对应服务器端的 conversation_participants 表
 */
@Entity(
    tableName = "conversation_participants",
    indices = [
        Index(value = ["conversation_id", "user_id"], unique = true),
        Index(value = ["user_id"]),
        Index(value = ["role"]),
        Index(value = ["joined_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConversationParticipantEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "role")
    val role: String = ROLE_MEMBER, // owner, admin, member
    
    @ColumnInfo(name = "joined_at")
    val joinedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_read_at")
    val lastReadAt: Long? = null,
    
    @ColumnInfo(name = "is_muted")
    val isMuted: Boolean = false,
    
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // 角色常量
        const val ROLE_OWNER = "owner"
        const val ROLE_ADMIN = "admin"
        const val ROLE_MEMBER = "member"
    }
}