package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moment_profiles")
data class MomentProfileEntity(
    @PrimaryKey
    @ColumnInfo("assistant_id")
    val assistantId: String,
    @ColumnInfo("cover_uri")
    val coverUri: String,
    @ColumnInfo("last_viewed_at")
    val lastViewedAt: Long,
)

