package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.core.constants.AppConstants
import com.behavioralfirewall.domain.model.InterventionType
import com.behavioralfirewall.domain.model.RiskLevel
import com.behavioralfirewall.domain.model.RiskState
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrictionManager @Inject constructor(
    private val browserDetector: BrowserDetector
) {
    // Configurable parameters that can be customized in runtime settings
    var lateNightStartHour = AppConstants.LATE_NIGHT_START_HOUR
    var lateNightEndHour = AppConstants.LATE_NIGHT_END_HOUR
    var highRiskDelaySeconds = 15
    var switchCountThreshold = 3

    // Tracking app switching behavior
    private var lastBrowserLaunchTime = 0L
    private var browserLaunchCount = 0

    fun evaluateFriction(
        packageName: String,
        riskState: RiskState,
        failedInterventionCount: Int
    ): InterventionType? {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isLateNight = currentHour >= lateNightStartHour || currentHour < lateNightEndHour

        // Rule 4: Multiple failed interventions -> escalate severity to HARD_INTERRUPT
        if (failedInterventionCount >= 3) {
            return InterventionType.HARD_INTERRUPT
        }

        // Check if package is browser
        if (browserDetector.isBrowser(packageName)) {
            val now = System.currentTimeMillis()
            
            // Check for repeated browser switching behavior (switching within 60 seconds)
            if (now - lastBrowserLaunchTime < 60000) {
                browserLaunchCount++
            } else {
                browserLaunchCount = 1
            }
            lastBrowserLaunchTime = now

            // Rule 3: Repeated browser switching -> HARD_INTERRUPT
            if (browserLaunchCount >= switchCountThreshold) {
                return InterventionType.HARD_INTERRUPT
            }

            // Rule 1: Browser launch during HIGH or CRITICAL risk -> DELAY overlay
            if (riskState.level == RiskLevel.HIGH || riskState.level == RiskLevel.CRITICAL) {
                return InterventionType.DELAY
            }

            // Regular browser launch during MEDIUM risk -> WARNING
            if (riskState.level == RiskLevel.MEDIUM) {
                return InterventionType.WARNING
            }
        }

        // Rule 2: Play Store launch after 11 PM -> REFLECTION_PROMPT
        if (browserDetector.isPlayStore(packageName)) {
            if (isLateNight) {
                return InterventionType.REFLECTION_PROMPT
            }
            // Regular Play Store launch during high risk -> REFLECTION_PROMPT
            if (riskState.level == RiskLevel.HIGH || riskState.level == RiskLevel.CRITICAL) {
                return InterventionType.REFLECTION_PROMPT
            }
        }

        // General fallback: if risk state is CRITICAL, force BREATHING reset
        if (riskState.level == RiskLevel.CRITICAL) {
            return InterventionType.BREATHING
        }

        return null
    }

    fun resetTracking() {
        browserLaunchCount = 0
        lastBrowserLaunchTime = 0L
    }
}
