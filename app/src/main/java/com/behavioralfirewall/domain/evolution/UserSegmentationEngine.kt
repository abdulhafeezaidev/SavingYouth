package com.behavioralfirewall.domain.evolution

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class UserSegment {
    HIGH_RISK,
    MODERATE_RISK,
    RECOVERY_STAGE,
    STABLE
}

@Singleton
class UserSegmentationEngine @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) {
    private val segmentKey = "user_behavior_segment_v1"

    private val _activeSegment = MutableStateFlow(UserSegment.MODERATE_RISK)
    val activeSegment: StateFlow<UserSegment> = _activeSegment.asStateFlow()

    init {
        loadSegment()
    }

    private fun loadSegment() {
        val saved = secureStorageManager.getToken(segmentKey)
        if (saved != null) {
            _activeSegment.value = try {
                UserSegment.valueOf(saved)
            } catch (e: Exception) {
                UserSegment.MODERATE_RISK
            }
        }
    }

    /**
     * Dynamically updates user segmentation profile based on risk trends and streak compliance.
     */
    fun reevaluateSegment(averageRiskScore: Float, successRate: Float, currentStreakDays: Int) {
        val calculated = when {
            averageRiskScore > 75f && successRate < 0.60f -> UserSegment.HIGH_RISK
            currentStreakDays >= 30 && averageRiskScore < 30f -> UserSegment.STABLE
            currentStreakDays >= 7 && successRate >= 0.80f -> UserSegment.RECOVERY_STAGE
            else -> UserSegment.MODERATE_RISK
        }

        _activeSegment.value = calculated
        secureStorageManager.saveToken(segmentKey, calculated.name)
    }

    /**
     * Resolves the custom risk trigger threshold for the active user segment.
     * High-risk profiles trigger overlays earlier (at lower scores) to prevent relapse escalation.
     */
    fun getThresholdForSegment(): Int {
        return when (_activeSegment.value) {
            UserSegment.HIGH_RISK -> 45      // Highly conservative, triggers quickly
            UserSegment.MODERATE_RISK -> 60  // Standard threshold
            UserSegment.RECOVERY_STAGE -> 70 // Gives more breathing room
            UserSegment.STABLE -> 80         // Bypasses warnings except in extreme risk
        }
    }

    fun getNotificationIntensity(): String {
        return when (_activeSegment.value) {
            UserSegment.HIGH_RISK -> "STRICT_URGENT"
            UserSegment.MODERATE_RISK -> "DISCREET_STANDARD"
            UserSegment.RECOVERY_STAGE -> "DISCREET_SUPPORTIVE"
            UserSegment.STABLE -> "SILENT_MINIMAL"
        }
    }

    fun getInterventionStyle(): String {
        return when (_activeSegment.value) {
            UserSegment.HIGH_RISK -> "STRICT_SCREEN_LOCK"
            UserSegment.MODERATE_RISK -> "REFLECTION_PROMPT"
            UserSegment.RECOVERY_STAGE -> "COGNITIVE_FRICTION"
            UserSegment.STABLE -> "AMBIENT_NOTIFICATION"
        }
    }
}
