package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recovery_scores")
data class RecoveryScoreEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val score: Int, // 0 to 100
    val streakConsistencyContribution: Int,
    val reducedRiskyBehaviorContribution: Int,
    val interventionComplianceContribution: Int,
    val lateNightActivityDeduction: Int
)
