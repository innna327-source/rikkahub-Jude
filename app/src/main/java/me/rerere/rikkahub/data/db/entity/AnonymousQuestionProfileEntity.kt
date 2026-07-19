package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "anonymous_question_profiles", primaryKeys = ["scope_id"])
data class AnonymousQuestionProfileEntity(
    @ColumnInfo("scope_id") val scopeId: String,
    @ColumnInfo("last_viewed_at") val lastViewedAt: Long,
)
