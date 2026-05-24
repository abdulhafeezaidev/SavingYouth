package com.behavioralfirewall.domain.repository

import com.behavioralfirewall.data.local.entities.*
import kotlinx.coroutines.flow.Flow

interface BehavioralRepository {
    fun getUserState(): Flow<UserStateEntity?>
    suspend fun saveUserState(state: UserStateEntity)
    fun getRiskEvents(): Flow<List<RiskEventEntity>>
    suspend fun saveRiskEvent(event: RiskEventEntity)
    fun getAppUsage(): Flow<List<AppUsageEntity>>
    suspend fun saveAppUsage(usage: AppUsageEntity)

    // Streak methods
    fun getStreak(): Flow<StreakEntity?>
    suspend fun saveStreak(streak: StreakEntity)

    // Trigger sequence methods
    fun getTriggerSequences(): Flow<List<TriggerSequenceEntity>>
    suspend fun saveTriggerSequence(sequence: TriggerSequenceEntity)

    // Recovery score methods
    fun getRecoveryScores(): Flow<List<RecoveryScoreEntity>>
    suspend fun saveRecoveryScore(score: RecoveryScoreEntity)

    // Analytics summary methods
    fun getAnalyticsSummaries(): Flow<List<AnalyticsSummaryEntity>>
    suspend fun saveAnalyticsSummary(summary: AnalyticsSummaryEntity)
    suspend fun getUnsyncedSummaries(): List<AnalyticsSummaryEntity>

    // Accountability partner methods
    fun getPartners(): Flow<List<AccountabilityPartnerEntity>>
    suspend fun savePartner(partner: AccountabilityPartnerEntity)
    suspend fun removePartner(id: String)

    // Invitation methods
    fun getInvitations(): Flow<List<InvitationEntity>>
    suspend fun saveInvitation(invitation: InvitationEntity)

    // Emergency event methods
    fun getEmergencyEvents(): Flow<List<EmergencyEventEntity>>
    suspend fun saveEmergencyEvent(event: EmergencyEventEntity)

    // Blocked attempts methods
    fun getBlockedAttempts(): Flow<List<BlockedAttemptEntity>>
    suspend fun saveBlockedAttempt(attempt: BlockedAttemptEntity)

    suspend fun pruneRiskEvents(threshold: Long): Int
    suspend fun pruneBlockedAttempts(threshold: Long): Int
}
