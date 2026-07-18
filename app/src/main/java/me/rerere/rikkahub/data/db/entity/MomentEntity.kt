package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "moments",
    indices = [
        Index(value = ["assistant_id", "created_at"]),
        Index(value = ["assistant_id", "reply_status", "reply_due_at"]),
    ]
)
data class MomentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("assistant_id")
    val assistantId: String,
    @ColumnInfo("author")
    val author: String,
    @ColumnInfo("content")
    val content: String,
    @ColumnInfo("context_note")
    val contextNote: String,
    @ColumnInfo("image_description")
    val imageDescription: String,
    @ColumnInfo("images")
    val images: String,
    @ColumnInfo("reply_due_at")
    val replyDueAt: Long,
    @ColumnInfo("reply_status")
    val replyStatus: String,
    @ColumnInfo("ai_liked")
    val aiLiked: Boolean,
    @ColumnInfo("ai_reply_content")
    val aiReplyContent: String,
    @ColumnInfo("replied_at")
    val repliedAt: Long?,
    @ColumnInfo("ai_reply_seen_at")
    val aiReplySeenAt: Long?,
    @ColumnInfo("user_liked")
    val userLiked: Boolean,
    @ColumnInfo("created_at")
    val createdAt: Long,
)

