package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_events")
data class EmergencyEventEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val actionTaken: String // "LOCKOUT_ACTIVATED", "PARTNER_ALERTED", "BREATHING_FINISHED"
)
