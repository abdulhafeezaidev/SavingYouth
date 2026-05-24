package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey
    val id: String,
    val packageName: String,
    val duration: Long,
    val timestamp: Long
)
