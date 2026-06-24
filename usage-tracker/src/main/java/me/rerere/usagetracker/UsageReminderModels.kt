package me.rerere.usagetracker

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class UsageReminderConfig(
    val rules: List<UsageReminderRule> = emptyList(),
)

@Serializable
data class UsageReminderRule(
    val packageName: String,
    val label: String,
    val thresholdMinutes: Int = 60,
    val enabled: Boolean = true,
)

@Serializable
data class UsageReminderState(
    val date: String = todayKey(),
    val appStates: Map<String, UsageReminderAppState> = emptyMap(),
)

@Serializable
data class UsageReminderAppState(
    val reminderCount: Int = 0,
    val ignored: Boolean = false,
    val lastEventTimeMillis: Long = 0L,
)

data class ForegroundAppEvent(
    val packageName: String,
    val timestampMillis: Long,
)

fun todayKey(): String = LocalDate.now(ZoneId.systemDefault()).toString()
