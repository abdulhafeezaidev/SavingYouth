package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "risk_events",
    indices = [
        androidx.room.Index(value = ["timestamp"]),
        androidx.room.Index(value = ["outcome"])
    ]
)
data class RiskEventEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val riskScore: Int,
    val triggerType: String,
    val interventionType: String,
    val outcome: String
)
