package com.behavioralfirewall.domain.engine.escalation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class EscalationStage {
    NORMAL,       // Stage 0
    SUSPICIOUS,   // Stage 1
    ESCALATING,   // Stage 2
    HIGH_RISK,    // Stage 3
    CRITICAL      // Stage 4
}

data class EscalationState(
    val stage: EscalationStage,
    val confidence: Float,
    val explanation: String
)

data class SequenceSignal(
    val type: SequenceSignalType,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = ""
)

enum class SequenceSignalType {
    KEYWORD_TYPED,
    PLAY_STORE_LAUNCHED,
    VPN_LAUNCHED,
    BROWSER_LAUNCHED,
    APP_SWITCHING,
    LATE_NIGHT,
    PROLONGED_BROWSER
}

@Singleton
class EscalationSequenceEngine @Inject constructor(
    private val falsePositiveReducer: FalsePositiveReducer
) {
    private val slidingWindowMs = 15 * 60 * 1000L // 15-minute sliding window
    private val signals = mutableListOf<SequenceSignal>()

    private val _escalationState = MutableStateFlow(EscalationState(EscalationStage.NORMAL, 0.0f, "System monitoring active"))
    val escalationState: StateFlow<EscalationState> = _escalationState.asStateFlow()

    @Synchronized
    fun addSignal(signal: SequenceSignal) {
        // Clean expired signals before evaluating
        val now = System.currentTimeMillis()
        signals.removeAll { now - it.timestamp > slidingWindowMs }

        signals.add(signal)
        evaluateState()
    }

    @Synchronized
    fun clearSignals() {
        signals.clear()
        falsePositiveReducer.resetSmoothing()
        _escalationState.value = EscalationState(EscalationStage.NORMAL, 0.0f, "Signals cleared manually")
    }

    @Synchronized
    fun getActiveSignals(): List<SequenceSignal> {
        val now = System.currentTimeMillis()
        signals.removeAll { now - it.timestamp > slidingWindowMs }
        return signals.toList()
    }

    private fun evaluateState() {
        val now = System.currentTimeMillis()
        signals.removeAll { now - it.timestamp > slidingWindowMs }

        val hasKeyword = signals.any { it.type == SequenceSignalType.KEYWORD_TYPED }
        val hasPlayStore = signals.any { it.type == SequenceSignalType.PLAY_STORE_LAUNCHED }
        val hasVpn = signals.any { it.type == SequenceSignalType.VPN_LAUNCHED }
        val hasBrowser = signals.any { it.type == SequenceSignalType.BROWSER_LAUNCHED }
        val hasAppSwitching = signals.any { it.type == SequenceSignalType.APP_SWITCHING }
        val hasProlongedBrowser = signals.any { it.type == SequenceSignalType.PROLONGED_BROWSER }

        // Late night check
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val isLateNight = hour >= 23 || hour < 5

        val stage: EscalationStage
        val confidence: Float
        val explanation: String

        when {
            // Critical relapsing sequence
            (hasProlongedBrowser || hasBrowser) && hasVpn && isLateNight -> {
                stage = EscalationStage.CRITICAL
                confidence = 0.95f
                explanation = "Critical: Prolonged browsing session detected with active VPN at late night."
            }
            // High risk behavioral sequence
            hasKeyword && hasPlayStore && isLateNight -> {
                stage = EscalationStage.HIGH_RISK
                confidence = 0.85f
                explanation = "High Risk: Risky search keyword typed inside Play Store during late night hours."
            }
            hasBrowser && hasVpn && hasPlayStore -> {
                stage = EscalationStage.HIGH_RISK
                confidence = 0.80f
                explanation = "High Risk: Parallel opening of Browser, VPN, and Google Play Store."
            }
            // Escalating sequence
            hasKeyword && (hasBrowser || hasPlayStore || hasAppSwitching) -> {
                stage = EscalationStage.ESCALATING
                confidence = 0.65f
                explanation = "Escalating: Keyword search combined with active app navigation or switching."
            }
            hasVpn && hasBrowser -> {
                stage = EscalationStage.ESCALATING
                confidence = 0.60f
                explanation = "Escalating: VPN app launched concurrently with browser activity."
            }
            // Suspicious sequence
            hasKeyword -> {
                stage = EscalationStage.SUSPICIOUS
                confidence = 0.40f
                explanation = "Suspicious: Risky search term detected."
            }
            hasPlayStore && isLateNight -> {
                stage = EscalationStage.SUSPICIOUS
                confidence = 0.35f
                explanation = "Suspicious: Play Store launched late night."
            }
            hasAppSwitching && isLateNight -> {
                stage = EscalationStage.SUSPICIOUS
                confidence = 0.30f
                explanation = "Suspicious: Repeated app-switching behavior during late night hours."
            }
            // Normal system monitor
            else -> {
                stage = EscalationStage.NORMAL
                confidence = 0.0f
                explanation = "Normal: App operations are stable"
            }
        }

        // Apply false positive smoothing check
        val smoothedConf = falsePositiveReducer.smoothRiskConfidence(confidence)
        _escalationState.value = EscalationState(stage, smoothedConf, explanation)
    }
}
