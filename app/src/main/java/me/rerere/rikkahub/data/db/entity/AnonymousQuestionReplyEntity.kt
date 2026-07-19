package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anonymous_question_replies",
    foreignKeys = [
        ForeignKey(
            entity = AnonymousQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["question_id", "created_at"]),
        Index(value = ["question_id", "author", "kind"], unique = true),
        Index(value = ["author", "reply_status", "reply_due_at"]),
    ]
)
data class AnonymousQuestionReplyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo("question_id") val questionId: String,
    val author: String,
    val kind: String,
    val content: String,
    @ColumnInfo("reply_due_at") val replyDueAt: Long?,
    @ColumnInfo("reply_status") val replyStatus: String,
    @ColumnInfo("created_at") val createdAt: Long,
)
