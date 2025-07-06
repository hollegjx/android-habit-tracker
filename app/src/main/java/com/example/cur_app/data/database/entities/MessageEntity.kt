package com.example.cur_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

/**
 * 消息实体
 * 对应服务器端的 messages 表
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["message_id"], unique = true),
        Index(value = ["conversation_id", "sent_at"]),
        Index(value = ["sender_id"]),
        Index(value = ["message_type"]),
        Index(value = ["is_read"]),
        Index(value = ["reply_to_id"])
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
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "message_id")
    val messageId: String,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: Long,
    
    @ColumnInfo(name = "sender_id")
    val senderId: String? = null,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "message_type")
    val messageType: String = TYPE_TEXT, // text, image, file, voice, video, system
    
    @ColumnInfo(name = "media_url")
    val mediaUrl: String? = null,
    
    @ColumnInfo(name = "media_metadata")
    val mediaMetadata: String? = null, // JSON格式的媒体元数据
    
    @ColumnInfo(name = "reply_to_id")
    val replyToId: Long? = null,
    
    @ColumnInfo(name = "is_edited")
    val isEdited: Boolean = false,
    
    @ColumnInfo(name = "edited_at")
    val editedAt: Long? = null,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "read_at")
    val readAt: Long? = null,
    
    @ColumnInfo(name = "is_delivered")
    val isDelivered: Boolean = false,
    
    @ColumnInfo(name = "delivered_at")
    val deliveredAt: Long? = null,
    
    @ColumnInfo(name = "reactions")
    val reactions: String? = null, // JSON格式的反应数据
    
    @ColumnInfo(name = "mentions")
    val mentions: String? = null, // JSON格式的提及数据
    
    @ColumnInfo(name = "sent_at")
    val sentAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // 消息类型常量
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
        const val TYPE_FILE = "file"
        const val TYPE_VOICE = "voice"
        const val TYPE_VIDEO = "video"
        const val TYPE_SYSTEM = "system"
    }
}