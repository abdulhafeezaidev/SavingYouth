package com.behavioralfirewall.domain.engine.escalation

import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FalsePositiveReducer @Inject constructor() {

    // Technical/Developer keyword lists that suppress trigger scoring
    private val technicalKeywords = setOf(
        "npm", "gradle", "git", "api", "debugging", "work", "setup", "corporate",
        "intellij", "android studio", "kotlin", "java", "swift", "deploy", "pr",
        "pull request", "sdk", "github", "gitlab", "terminal", "powershell", "cmd",
        "database", "sql", "server", "localhost", "docker", "kubernetes"
    )

    private val riskSmoothingFactor = 0.3f // Exponential smoothing constant (alpha)
    private var smoothedConfidence = 0.0f

    /**
     * Determines whether the current behavior sequence is a likely false positive.
     * Technical searches or corporate VPN connections during business hours are filtered.
     */
    fun evaluateFalsePositive(
        rawText: String,
        packageName: String,
        isVpnActive: Boolean
    ): Boolean {
        // 1. Text-based technical checks
        val normalized = rawText.lowercase().trim()
        if (normalized.isNotEmpty()) {
            val words = normalized.split(Regex("\\s+"))
            if (words.any { technicalKeywords.contains(it) }) {
                return true // Safe developer work search
            }
        }

        // 2. Temporal corporate VPN checks
        if (isVpnActive) {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

            val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
            val isWorkingHours = hourOfDay in 9..17

            if (isWeekday && isWorkingHours) {
                // Highly likely to be a corporate VPN connection for remote work
                return true
            }
        }

        return false
    }

    /**
     * Smooths out the confidence score using an Exponential Moving Average.
     */
    fun smoothRiskConfidence(incomingConfidence: Float): Float {
        smoothedConfidence = (riskSmoothingFactor * incomingConfidence) + ((1f - riskSmoothingFactor) * smoothedConfidence)
        return smoothedConfidence
    }

    fun resetSmoothing() {
        smoothedConfidence = 0.0f
    }
}
