package com.behavioralfirewall.domain.engine.escalation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentRiskEngine @Inject constructor(
    private val sequenceEngine: EscalationSequenceEngine
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _riskScore = MutableStateFlow(0)
    val riskScore: StateFlow<Int> = _riskScore.asStateFlow()

    private var lastSignalTime = 0L

    init {
        // Start background decay coroutine (decays 2 points every 10 seconds of inactivity)
        scope.launch {
            while (true) {
                delay(10000L)
                decayRiskScore()
            }
        }

        // Monitor sequence changes
        scope.launch {
            sequenceEngine.escalationState.collect { state ->
                val baseScore = when (state.stage) {
                    EscalationStage.NORMAL -> 0
                    EscalationStage.SUSPICIOUS -> 25
                    EscalationStage.ESCALATING -> 50
                    EscalationStage.HIGH_RISK -> 75
                    EscalationStage.CRITICAL -> 95
                }
                
                val calculatedScore = (baseScore + (state.confidence * 5)).toInt().coerceIn(0, 100)
                
                // Only bump the score upwards automatically on new stage activation,
                // allowing it to decay downward if there are no new active triggers.
                if (calculatedScore > _riskScore.value) {
                    _riskScore.value = calculatedScore
                    lastSignalTime = System.currentTimeMillis()
                }
            }
        }
    }

    /**
     * Reports that a new signal is processed, resetting the decay timeout.
     */
    fun notifySignalReceived() {
        lastSignalTime = System.currentTimeMillis()
    }

    private fun decayRiskScore() {
        val now = System.currentTimeMillis()
        // If 15 seconds have passed with no new signal, decay by 2 points
        if (now - lastSignalTime > 15000L && _riskScore.value > 0) {
            _riskScore.value = (_riskScore.value - 2).coerceAtLeast(0)
            
            // If the score decays back to 0, clear in-memory signals to reset state
            if (_riskScore.value == 0) {
                sequenceEngine.clearSignals()
            }
        }
    }
    
    fun simulateDecay() {
        _riskScore.value = (_riskScore.value - 10).coerceAtLeast(0)
        if (_riskScore.value == 0) {
            sequenceEngine.clearSignals()
        }
    }
}
