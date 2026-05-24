package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.StreakEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorSummaryGenerator @Inject constructor(
    private val repository: BehavioralRepository
) {
    suspend fun generateAnonymizedSummary(streak: StreakEntity?): String {
        val events = repository.getRiskEvents().firstOrNull() ?: emptyList()
        
        val complied = events.count { it.outcome == "complied" }
        val dismissed = events.count { it.outcome == "dismissed" }
        val totalTriggers = complied + dismissed
        
        val successRate = if (totalTriggers > 0) {
            (complied.toFloat() / totalTriggers.toFloat() * 100).toInt()
        } else {
            100
        }

        val streakDays = streak?.currentDays ?: 1
        val longestStreak = streak?.longestDays ?: 1
        val avgRiskScore = if (events.isNotEmpty()) {
            events.map { it.riskScore }.average().toInt()
        } else {
            0
        }

        return buildString {
            append("Behavioral Firewall Recovery Summary:\n")
            append("- Recovery Streak: $streakDays days (Longest Milestone: $longestStreak days)\n")
            append("- Calming Intervention Compliance: $successRate%\n")
            append("- Daily Recovery Index: ${100 - avgRiskScore}/100\n")
            append("- Total Triggers Defused: ${events.size}\n")
            append("\n[Privacy Guard: Explicit package names, URLs, and search sequences are kept strictly local.]")
        }
    }
}
