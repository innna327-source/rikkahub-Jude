package me.rerere.usagetracker

import android.graphics.drawable.Drawable

data class AppUsageSummary(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val totalTimeForegroundMillis: Long,
    val lastTimeUsedMillis: Long,
    val launchCount: Int,
)

enum class UsageStatsPeriod {
    Today,
    Yesterday,
    Last7Days,
}
