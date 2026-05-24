package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.RecoveryScoreEntity
import com.behavioralfirewall.data.local.entities.StreakEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryScoreCalculator @Inject constructor(
    private val repository: BehavioralRepository
) {
    suspend fun calculateAndSaveDailyScore(streak: StreakEntity?): Int {
        val events = repository.getRiskEvents().firstOrNull() ?: emptyList()
        
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEvents = events.filter { it.timestamp >= todayStart }

        // 1. Streak Consistency (Max 30 pts)
        val currentStreak = streak?.currentDays ?: 0
        val streakPts = (currentStreak * 3).coerceAtMost(30)

        // 2. Reduced Risky Behavior (Max 30 pts)
        val highRiskTriggers = todayEvents.count { it.riskScore >= 60 }
        val behaviorPts = (30 - (highRiskTriggers * 5)).coerceAtLeast(0)

        // 3. Successful Intervention Compliance (Max 25 pts)
        val triggersWithIntervention = todayEvents.filter { 
            it.interventionType != "NONE" && 
            it.outcome in listOf("complied", "dismissed") 
        }
        val compliedCount = triggersWithIntervention.count { it.outcome == "complied" }
        val compliancePts = if (triggersWithIntervention.isNotEmpty()) {
            ((compliedCount.toFloat() / triggersWithIntervention.size.toFloat()) * 25).toInt()
        } else {
            25 // Full marks if no interventions were needed (healthy, disciplined day!)
        }

        // 4. Lower Late-Night Activity (Max 15 pts)
        val lateNightTriggers = todayEvents.count { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour >= 23 || hour < 5
        }
        val lateNightPts = (15 - (lateNightTriggers * 5)).coerceAtLeast(0)

        val totalScore = (streakPts + behaviorPts + compliancePts + lateNightPts).coerceIn(0, 100)

        val scoreEntity = RecoveryScoreEntity(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            score = totalScore,
            streakConsistencyContribution = streakPts,
            reducedRiskyBehaviorContribution = behaviorPts,
            interventionComplianceContribution = compliancePts,
            lateNightActivityDeduction = 15 - lateNightPts
        )
        repository.saveRecoveryScore(scoreEntity)

        return totalScore
    }
}
