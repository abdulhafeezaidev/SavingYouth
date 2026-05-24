package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_summaries")
data class AnalyticsSummaryEntity(
    @PrimaryKey
    val id: String,
    val dateString: String, // format: "yyyy-MM-dd"
    val totalTriggers: Int,
    val successCount: Int,
    val dismissCount: Int,
    val recoveryScore: Int,
    val peakRiskHour: Int,
    val topRiskyApp: String,
    val lastSyncedAt: Long = 0L
)
