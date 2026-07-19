package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anonymous_questions",
    indices = [
        Index(value = ["scope_id", "created_at"]),
        Index(value = ["scope_id", "author", "reply_status", "reply_due_at"]),
    ]
)
data class AnonymousQuestionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("scope_id") val scopeId: String,
    val author: String,
    val content: String,
    @ColumnInfo("reply_due_at") val replyDueAt: Long?,
    @ColumnInfo("reply_status") val replyStatus: String,
    @ColumnInfo("created_at") val createdAt: Long,
)
