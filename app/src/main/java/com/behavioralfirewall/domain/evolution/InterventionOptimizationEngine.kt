package com.behavioralfirewall.domain.evolution

import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class FatigueLevel {
    LOW,
    MEDIUM,
    HIGH
}

@Singleton
class InterventionOptimizationEngine @Inject constructor(
    private val repository: BehavioralRepository
) {
    private val _fatigueLevel = MutableStateFlow(FatigueLevel.LOW)
    val fatigueLevel: StateFlow<FatigueLevel> = _fatigueLevel.asStateFlow()

    private val _dynamicCooldownMultiplier = MutableStateFlow(1.0f)
    val dynamicCooldownMultiplier: StateFlow<Float> = _dynamicCooldownMultiplier.asStateFlow()

    private val _triggerDiagnosticMessage = MutableStateFlow("Intervention triggers are balanced.")
    val triggerDiagnosticMessage: StateFlow<String> = _triggerDiagnosticMessage.asStateFlow()

    private val _recommendedInterventionStyle = MutableStateFlow("REFLECTION_PROMPT (Highest Success)")
    val recommendedInterventionStyle: StateFlow<String> = _recommendedInterventionStyle.asStateFlow()

    /**
     * Reports an outcome to track consecutive dismissals/comply history in memory.
     * Steps up friction barriers and pauses to protect users from alert fatigue.
     */
    fun recordInterventionOutcome(isComplied: Boolean) {
        if (isComplied) {
            // User responded positively, reduce fatigue multiplier
            _fatigueLevel.value = FatigueLevel.LOW
            _dynamicCooldownMultiplier.value = 1.0f
            _triggerDiagnosticMessage.value = "Intervention triggers are fully balanced."
        } else {
            // User dismissed, increase fatigue level
            val currentMultiplier = _dynamicCooldownMultiplier.value
            if (currentMultiplier < 4.0f) {
                val next = currentMultiplier * 2.0f
                _dynamicCooldownMultiplier.value = next
                _fatigueLevel.value = if (next >= 4.0f) FatigueLevel.HIGH else FatigueLevel.MEDIUM
                _triggerDiagnosticMessage.value = "Intervention fatigue detected! Dynamic cooldown increased to ${next}x."
            }
        }
    }

    /**
     * Resets fatigue scaling back to default multiplier.
     */
    fun resetFatigue() {
        _fatigueLevel.value = FatigueLevel.LOW
        _dynamicCooldownMultiplier.value = 1.0f
        _triggerDiagnosticMessage.value = "Intervention triggers are fully balanced."
    }

    /**
     * Recommends intervention patterns based on the user's compliance rate.
     */
    fun computeRecommendedInterventionType(successRate: Float): String {
        return when {
            successRate > 0.85f -> {
                _recommendedInterventionStyle.value = "PROGRESSIVE_DELAY (Fast compliance)"
                "PROGRESSIVE_DELAY"
            }
            successRate > 0.50f -> {
                _recommendedInterventionStyle.value = "REFLECTION_PROMPT (Interactive reflection)"
                "REFLECTION_PROMPT"
            }
            else -> {
                _recommendedInterventionStyle.value = "STRICT_COOLDOWN (Maximum structure)"
                "STRICT_COOLDOWN"
            }
        }
    }
}
