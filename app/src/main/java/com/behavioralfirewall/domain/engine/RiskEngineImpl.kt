package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.core.constants.AppConstants
import java.util.Calendar
import javax.inject.Inject

class RiskEngineImpl @Inject constructor(
    private val browserDetector: BrowserDetector,
    private val adaptiveRiskManager: AdaptiveRiskManager
) : RiskEngine {

    override fun calculateRiskScore(
        packageName: String,
        sessionDurationMs: Long,
        switchCount: Int,
        stressLevel: Int
    ): Int {
        var score = 0

        // Time-based scoring (Late night escalation with adaptive learning weight)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= AppConstants.LATE_NIGHT_START_HOUR || currentHour < AppConstants.LATE_NIGHT_END_HOUR) {
            score += (AppConstants.SCORE_LATE_NIGHT * adaptiveRiskManager.lateNightMultiplier).toInt()
        }

        // Browser Detection & Session Duration
        if (browserDetector.isBrowser(packageName)) {
            if (sessionDurationMs > AppConstants.THRESHOLD_LONG_BROWSER_SESSION_MS) {
                score += AppConstants.SCORE_LONG_BROWSER_SESSION
            }
        }

        // Play Store openings (with adaptive multiplier)
        if (browserDetector.isPlayStore(packageName)) {
            score += (AppConstants.SCORE_PLAY_STORE * adaptiveRiskManager.playStoreMultiplier).toInt()
        }

        // Repetitive switching detection (with adaptive learning switching weight)
        if (switchCount > 5) {
            score += (AppConstants.SCORE_REPETITIVE_SWITCHING * adaptiveRiskManager.switchingMultiplier).toInt()
        }

        // Stress level contribution
        if (stressLevel > 70) {
            score += AppConstants.SCORE_HIGH_STRESS
        }

        return score.coerceIn(0, 100)
    }
}
