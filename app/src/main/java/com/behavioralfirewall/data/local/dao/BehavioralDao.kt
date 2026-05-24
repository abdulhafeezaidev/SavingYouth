package com.behavioralfirewall.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.behavioralfirewall.data.local.entities.AppUsageEntity
import com.behavioralfirewall.data.local.entities.RiskEventEntity
import com.behavioralfirewall.data.local.entities.UserStateEntity
import kotlinx.coroutines.flow.Flow

import com.behavioralfirewall.data.local.entities.StreakEntity
import com.behavioralfirewall.data.local.entities.TriggerSequenceEntity
import com.behavioralfirewall.data.local.entities.RecoveryScoreEntity
import com.behavioralfirewall.data.local.entities.AnalyticsSummaryEntity
import com.behavioralfirewall.data.local.entities.BlockedAttemptEntity
import com.behavioralfirewall.data.local.entities.AccountabilityPartnerEntity
import com.behavioralfirewall.data.local.entities.InvitationEntity
import com.behavioralfirewall.data.local.entities.EmergencyEventEntity

@Dao
interface BehavioralDao {
    @Query("SELECT * FROM user_state LIMIT 1")
    fun getUserState(): Flow<UserStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserState(state: UserStateEntity)

    @Query("SELECT * FROM risk_events ORDER BY timestamp DESC")
    fun getAllRiskEvents(): Flow<List<RiskEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskEvent(event: RiskEventEntity)

    @Query("SELECT * FROM app_usage ORDER BY timestamp DESC")
    fun getAllAppUsage(): Flow<List<AppUsageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(usage: AppUsageEntity)

    // Streak tracking queries
    @Query("SELECT * FROM streaks WHERE id = 'current_streak' LIMIT 1")
    fun getStreak(): Flow<StreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity)

    // Trigger sequences queries
    @Query("SELECT * FROM trigger_sequences ORDER BY occurrenceCount DESC")
    fun getTriggerSequences(): Flow<List<TriggerSequenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriggerSequence(sequence: TriggerSequenceEntity)

    // Recovery score queries
    @Query("SELECT * FROM recovery_scores ORDER BY timestamp DESC")
    fun getRecoveryScores(): Flow<List<RecoveryScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveryScore(score: RecoveryScoreEntity)

    // Analytics summaries queries
    @Query("SELECT * FROM analytics_summaries ORDER BY dateString DESC")
    fun getAnalyticsSummaries(): Flow<List<AnalyticsSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsSummary(summary: AnalyticsSummaryEntity)

    @Query("SELECT * FROM analytics_summaries WHERE lastSyncedAt = 0")
    suspend fun getUnsyncedSummaries(): List<AnalyticsSummaryEntity>

    // Accountability partner queries
    @Query("SELECT * FROM accountability_partners ORDER BY createdAt DESC")
    fun getPartners(): Flow<List<AccountabilityPartnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: AccountabilityPartnerEntity)

    @Query("DELETE FROM accountability_partners WHERE id = :id")
    suspend fun deletePartner(id: String)

    // Invitation queries
    @Query("SELECT * FROM accountability_invitations ORDER BY createdAt DESC")
    fun getInvitations(): Flow<List<InvitationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: InvitationEntity)

    // Emergency event queries
    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getEmergencyEvents(): Flow<List<EmergencyEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyEvent(event: EmergencyEventEntity)

    // Blocked attempts queries
    @Query("SELECT * FROM blocked_attempts ORDER BY timestamp DESC")
    fun getBlockedAttempts(): Flow<List<BlockedAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedAttempt(attempt: BlockedAttemptEntity)

    @Query("DELETE FROM risk_events WHERE timestamp < :threshold")
    suspend fun pruneRiskEvents(threshold: Long): Int

    @Query("DELETE FROM blocked_attempts WHERE timestamp < :threshold")
    suspend fun pruneBlockedAttempts(threshold: Long): Int
}
