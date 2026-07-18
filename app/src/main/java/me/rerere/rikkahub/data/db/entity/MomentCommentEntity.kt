package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "moment_comments",
    foreignKeys = [
        ForeignKey(
            entity = MomentEntity::class,
            parentColumns = ["id"],
            childColumns = ["moment_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["moment_id", "created_at"]),
        Index(value = ["author", "reply_status", "reply_due_at"]),
    ]
)
data class MomentCommentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("moment_id")
    val momentId: String,
    @ColumnInfo("author")
    val author: String,
    @ColumnInfo("content")
    val content: String,
    @ColumnInfo("reply_due_at")
    val replyDueAt: Long?,
    @ColumnInfo("reply_status")
    val replyStatus: String,
    @ColumnInfo("seen_at")
    val seenAt: Long?,
    @ColumnInfo("created_at")
    val createdAt: Long,
)

