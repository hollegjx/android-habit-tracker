package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * 好友关系实体
 * 对应服务器端的 friendships 表
 */
@Entity(
    tableName = "friendships",
    indices = [
        Index(value = ["requester_id", "addressee_id"], unique = true),
        Index(value = ["status"]),
        Index(value = ["conversation_id"]),
        Index(value = ["last_message_at"]),
        Index(value = ["is_starred"])
    ]
)
data class FriendshipEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "requester_id")
    val requesterId: String,
    
    @ColumnInfo(name = "addressee_id")
    val addresseeId: String,
    
    @ColumnInfo(name = "status")
    val status: String, // pending, accepted, declined, blocked
    
    @ColumnInfo(name = "requester_message")
    val requesterMessage: String? = null,
    
    @ColumnInfo(name = "reject_reason")
    val rejectReason: String? = null,
    
    @ColumnInfo(name = "friendship_alias")
    val friendshipAlias: String? = null, // 好友备注名
    
    @ColumnInfo(name = "is_starred")
    val isStarred: Boolean = false, // 是否特别关注
    
    @ColumnInfo(name = "is_muted")
    val isMuted: Boolean = false, // 是否静音
    
    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean = false, // 是否屏蔽
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String? = null, // 关联的对话ID
    
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0, // 未读消息数
    
    @ColumnInfo(name = "last_message_at")
    val lastMessageAt: Long? = null, // 最后消息时间
    
    @ColumnInfo(name = "last_read_at")
    val lastReadAt: Long? = null, // 最后阅读时间
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // 好友状态常量
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_DECLINED = "declined"
        const val STATUS_BLOCKED = "blocked"
    }
}