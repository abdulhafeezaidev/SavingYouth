package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountability_partners")
data class AccountabilityPartnerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val relationshipType: String,
    val trustLevel: String, // "STANDARD" or "HIGH"
    val notificationsEnabled: Boolean,
    val createdAt: Long,
    val isSynced: Boolean = false
)
