package com.behavioralfirewall.data.remote.appwrite

import android.content.Context
import com.behavioralfirewall.data.local.entities.StreakEntity
import com.behavioralfirewall.data.local.entities.AnalyticsSummaryEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clientProvider: AppwriteClientProvider,
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        // Managed by WorkManager for battery efficiency
    }

    suspend fun triggerSync(): Boolean {
        try {
            val client = clientProvider.provideClient(context)
            val databases = Databases(client)

            // 1. Sync Streak Data (Anonymized)
            val streak = repository.getStreak().firstOrNull()
            if (streak != null) {
                val data = mapOf(
                    "streakStartDate" to streak.streakStartDate,
                    "currentDays" to streak.currentDays,
                    "longestDays" to streak.longestDays,
                    "lastRelapseAt" to streak.lastRelapseAt,
                    "recoveryScore" to streak.recoveryScore,
                    "lastUpdated" to streak.lastUpdated
                )
                try {
                    databases.createDocument(
                        databaseId = "behavioral_firewall",
                        collectionId = "streaks",
                        documentId = "current_streak",
                        data = data
                    )
                } catch (e: Exception) {
                    try {
                        databases.updateDocument(
                            databaseId = "behavioral_firewall",
                            collectionId = "streaks",
                            documentId = "current_streak",
                            data = data
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            // 2. Offline-First batched sync for unsynced summaries (Anonymized metadata only)
            val unsyncedList = repository.getUnsyncedSummaries()
            for (summary in unsyncedList) {
                try {
                    databases.createDocument(
                        databaseId = "behavioral_firewall",
                        collectionId = "analytics_summaries",
                        documentId = summary.id,
                        data = mapOf(
                            "dateString" to summary.dateString,
                            "totalTriggers" to summary.totalTriggers,
                            "successCount" to summary.successCount,
                            "dismissCount" to summary.dismissCount,
                            "recoveryScore" to summary.recoveryScore,
                            "peakRiskHour" to summary.peakRiskHour,
                            "topRiskyApp" to summary.topRiskyApp
                        )
                    )
                    // Mark as synced locally on success
                    repository.saveAnalyticsSummary(
                        summary.copy(lastSyncedAt = System.currentTimeMillis())
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
