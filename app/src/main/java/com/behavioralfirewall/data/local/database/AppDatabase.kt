package com.behavioralfirewall.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.behavioralfirewall.data.local.dao.BehavioralDao
import com.behavioralfirewall.data.local.entities.AppUsageEntity
import com.behavioralfirewall.data.local.entities.RiskEventEntity
import com.behavioralfirewall.data.local.entities.UserStateEntity
import com.behavioralfirewall.data.local.entities.StreakEntity
import com.behavioralfirewall.data.local.entities.TriggerSequenceEntity
import com.behavioralfirewall.data.local.entities.RecoveryScoreEntity
import com.behavioralfirewall.data.local.entities.AnalyticsSummaryEntity
import com.behavioralfirewall.data.local.entities.AccountabilityPartnerEntity
import com.behavioralfirewall.data.local.entities.InvitationEntity
import com.behavioralfirewall.data.local.entities.EmergencyEventEntity
import com.behavioralfirewall.data.local.entities.BlockedAttemptEntity

@Database(
    entities = [
        UserStateEntity::class,
        RiskEventEntity::class,
        AppUsageEntity::class,
        StreakEntity::class,
        TriggerSequenceEntity::class,
        RecoveryScoreEntity::class,
        AnalyticsSummaryEntity::class,
        AccountabilityPartnerEntity::class,
        InvitationEntity::class,
        EmergencyEventEntity::class,
        BlockedAttemptEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun behavioralDao(): BehavioralDao
}
