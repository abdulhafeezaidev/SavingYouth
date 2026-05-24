package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocked_attempts",
    indices = [
        androidx.room.Index(value = ["timestamp"])
    ]
)
data class BlockedAttemptEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val domain: String,
    val category: String,
    val actionTaken: String // "BLOCKED" or "BYPASSED"
)
