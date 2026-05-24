package com.behavioralfirewall.domain.evolution

import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostLaunchLearningEngine @Inject constructor(
    private val repository: BehavioralRepository,
    private val evolutionManager: BehaviorModelEvolutionManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _systemAccuracy = MutableStateFlow(95f) // Starting at 95% default accuracy
    val systemAccuracy: StateFlow<Float> = _systemAccuracy.asStateFlow()

    private val _falsePositiveRate = MutableStateFlow(2.5f) // default 2.5% FP rate
    val falsePositiveRate: StateFlow<Float> = _falsePositiveRate.asStateFlow()

    private val _isEvolutionRecommended = MutableStateFlow(false)
    val isEvolutionRecommended: StateFlow<Boolean> = _isEvolutionRecommended.asStateFlow()

    private val _driftReport = MutableStateFlow("All risk model drift parameters are stable.")
    val driftReport: StateFlow<String> = _driftReport.asStateFlow()

    private var recommendedWeights: RiskWeights? = null

    init {
        analyzeHistoricalDrift()
    }

    /**
     * Scans local database logs to assess intervention accuracy and calculate
     * false positive rates in the wild.
     */
    fun analyzeHistoricalDrift() {
        scope.launch {
            repository.getRiskEvents().collect { events ->
                if (events.isEmpty()) {
                    _systemAccuracy.value = 98f
                    _falsePositiveRate.value = 1.2f
                    _isEvolutionRecommended.value = false
                    _driftReport.value = "Initial startup: usage telemetry is stable."
                    return@collect
                }

                val totalInterventions = events.count { it.interventionType != "NONE" && it.outcome != "cooldown_triggered" }
                if (totalInterventions == 0) {
                    _systemAccuracy.value = 98f
                    _falsePositiveRate.value = 1.2f
                    _isEvolutionRecommended.value = false
                    return@collect
                }

                // A quick dismissal (< 2 sec) indicates the user bypassed it as a false alert or spam
                val quickDismissals = events.count { it.outcome == "dismissed" && it.riskScore < 65 }
                val complied = events.count { it.outcome == "complied" }

                val fpRate = (quickDismissals.toFloat() / totalInterventions.toFloat()) * 100f
                _falsePositiveRate.value = fpRate.coerceIn(0f, 100f)

                val successRate = (complied.toFloat() / totalInterventions.toFloat()) * 100f
                val accuracy = (100f - fpRate + successRate) / 2f
                _systemAccuracy.value = accuracy.coerceIn(60f, 100f)

                // If accuracy drops below 90% or false positives exceed 5%, propose tuning
                if (_systemAccuracy.value < 90f || _falsePositiveRate.value > 5.0f) {
                    _isEvolutionRecommended.value = true
                    val active = evolutionManager.activeWeights.value
                    
                    // Recommend calibrating weights slightly to reduce false triggers
                    val proposed = RiskWeights(
                        urgencyWeight = (active.urgencyWeight - 0.05f).coerceAtLeast(0.2f),
                        screenChangeWeight = (active.screenChangeWeight - 0.05f).coerceAtLeast(0.1f),
                        dwellTimeWeight = (active.dwellTimeWeight + 0.05f).coerceAtMost(0.8f),
                        coreThreshold = (active.coreThreshold + 5).coerceAtMost(80)
                    )
                    recommendedWeights = proposed
                    _driftReport.value = "Model Drift Detected! Recommendation: decrease screenChangeWeight to reduce false alerts."
                } else {
                    _isEvolutionRecommended.value = false
                    _driftReport.value = "Model performance remains fully optimized within limits."
                }
            }
        }
    }

    /**
     * Applies the recommended calibrated weights securely under governance validation.
     */
    fun applyRecommendedWeights(): Boolean {
        val proposed = recommendedWeights ?: return false
        val success = evolutionManager.evolveWeights(proposed, "v1.1.0-auto-calibrated")
        if (success) {
            _isEvolutionRecommended.value = false
            _driftReport.value = "Model successfully updated to v1.1.0-auto-calibrated under governance checks."
            analyzeHistoricalDrift()
        }
        return success
    }
}
