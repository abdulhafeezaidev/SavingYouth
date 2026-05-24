package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_sequences")
data class TriggerSequenceEntity(
    @PrimaryKey
    val id: String,
    val sequenceChain: String, // e.g. "FATIGUE -> com.android.vending -> com.android.chrome"
    val occurrenceCount: Int,
    val lastOccurredAt: Long
)
