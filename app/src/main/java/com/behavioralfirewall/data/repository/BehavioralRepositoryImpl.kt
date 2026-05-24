package com.behavioralfirewall.data.repository

import com.behavioralfirewall.data.local.dao.BehavioralDao
import com.behavioralfirewall.data.local.entities.*
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BehavioralRepositoryImpl @Inject constructor(
    private val dao: BehavioralDao
) : BehavioralRepository {
    override fun getUserState(): Flow<UserStateEntity?> = dao.getUserState()
    override suspend fun saveUserState(state: UserStateEntity) = dao.insertUserState(state)
    override fun getRiskEvents(): Flow<List<RiskEventEntity>> = dao.getAllRiskEvents()
    override suspend fun saveRiskEvent(event: RiskEventEntity) = dao.insertRiskEvent(event)
    override fun getAppUsage(): Flow<List<AppUsageEntity>> = dao.getAllAppUsage()
    override suspend fun saveAppUsage(usage: AppUsageEntity) = dao.insertAppUsage(usage)

    override fun getStreak(): Flow<StreakEntity?> = dao.getStreak()
    override suspend fun saveStreak(streak: StreakEntity) = dao.insertStreak(streak)

    override fun getTriggerSequences(): Flow<List<TriggerSequenceEntity>> = dao.getTriggerSequences()
    override suspend fun saveTriggerSequence(sequence: TriggerSequenceEntity) = dao.insertTriggerSequence(sequence)

    override fun getRecoveryScores(): Flow<List<RecoveryScoreEntity>> = dao.getRecoveryScores()
    override suspend fun saveRecoveryScore(score: RecoveryScoreEntity) = dao.insertRecoveryScore(score)

    override fun getAnalyticsSummaries(): Flow<List<AnalyticsSummaryEntity>> = dao.getAnalyticsSummaries()
    override suspend fun saveAnalyticsSummary(summary: AnalyticsSummaryEntity) = dao.insertAnalyticsSummary(summary)
    override suspend fun getUnsyncedSummaries(): List<AnalyticsSummaryEntity> = dao.getUnsyncedSummaries()

    override fun getPartners(): Flow<List<AccountabilityPartnerEntity>> = dao.getPartners()
    override suspend fun savePartner(partner: AccountabilityPartnerEntity) = dao.insertPartner(partner)
    override suspend fun removePartner(id: String) = dao.deletePartner(id)

    override fun getInvitations(): Flow<List<InvitationEntity>> = dao.getInvitations()
    override suspend fun saveInvitation(invitation: InvitationEntity) = dao.insertInvitation(invitation)

    override fun getEmergencyEvents(): Flow<List<EmergencyEventEntity>> = dao.getEmergencyEvents()
    override suspend fun saveEmergencyEvent(event: EmergencyEventEntity) = dao.insertEmergencyEvent(event)

    override fun getBlockedAttempts(): Flow<List<BlockedAttemptEntity>> = dao.getBlockedAttempts()
    override suspend fun saveBlockedAttempt(attempt: BlockedAttemptEntity) = dao.insertBlockedAttempt(attempt)

    override suspend fun pruneRiskEvents(threshold: Long): Int = dao.pruneRiskEvents(threshold)
    override suspend fun pruneBlockedAttempts(threshold: Long): Int = dao.pruneBlockedAttempts(threshold)
}
