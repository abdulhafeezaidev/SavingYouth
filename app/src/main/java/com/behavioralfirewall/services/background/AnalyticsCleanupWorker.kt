package com.behavioralfirewall.services.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behavioralfirewall.domain.repository.BehavioralRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AnalyticsCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: BehavioralRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Prune events older than 14 days (14 days * 24h * 60m * 60s * 1000ms)
            val threshold = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000L)
            repository.pruneRiskEvents(threshold)
            repository.pruneBlockedAttempts(threshold)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
