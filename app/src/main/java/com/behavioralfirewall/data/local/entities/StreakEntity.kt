package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    val id: String = "current_streak",
    val streakStartDate: Long,
    val currentDays: Int,
    val longestDays: Int,
    val lastRelapseAt: Long,
    val recoveryScore: Int,
    val lastUpdated: Long
)
