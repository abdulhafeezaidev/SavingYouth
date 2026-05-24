package com.behavioralfirewall.domain.reliability

import android.content.Context
import com.behavioralfirewall.domain.repository.BehavioralRepository
import com.behavioralfirewall.domain.security.SecureDiagnosticsLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EdgeCaseHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BehavioralRepository,
    private val logger: SecureDiagnosticsLogger
) {
    private val _activeInterventionShowing = MutableStateFlow(false)
    val activeInterventionShowing = _activeInterventionShowing.asStateFlow()

    fun requestInterventionLock(): Boolean {
        if (_activeInterventionShowing.value) {
            logger.w("EdgeCaseHandler", "Intervention block requested but an overlay is already showing. Overlapping prevented.")
            return false // Locked
        }
        _activeInterventionShowing.value = true
        return true // Lock acquired
    }

    fun releaseInterventionLock() {
        _activeInterventionShowing.value = false
    }

    /**
     * Safely recovers when a corrupted database is detected.
     * Triggers fallback reset routines while maintaining user streaks.
     */
    suspend fun handleCorruptedDatabase(e: Exception) {
        logger.e("EdgeCaseHandler", "Critical: Local Database Corruption Detected! Performing fallback reset.", e)
        try {
            val current = System.currentTimeMillis()
            repository.pruneRiskEvents(current + 86400000L)
            repository.pruneBlockedAttempts(current + 86400000L)
        } catch (ex: Exception) {
            logger.e("EdgeCaseHandler", "DB Fallback Reset Failed: ", ex)
        }
    }
}
