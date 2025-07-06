package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

/**
 * 好友通知实体
 * 对应服务器端的 friend_notifications 表
 */
@Entity(
    tableName = "friend_notifications",
    indices = [
        Index(value = ["user_id", "is_read"]),
        Index(value = ["type"]),
        Index(value = ["friendship_id"]),
        Index(value = ["created_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = FriendshipEntity::class,
            parentColumns = ["id"],
            childColumns = ["friendship_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FriendNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "friendship_id")
    val friendshipId: Long,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "type")
    val type: String, // request, accepted, declined, blocked
    
    @ColumnInfo(name = "message")
    val message: String? = null,
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "read_at")
    val readAt: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // 通知类型常量
        const val TYPE_REQUEST = "request"
        const val TYPE_ACCEPTED = "accepted"
        const val TYPE_DECLINED = "declined"
        const val TYPE_BLOCKED = "blocked"
    }
}