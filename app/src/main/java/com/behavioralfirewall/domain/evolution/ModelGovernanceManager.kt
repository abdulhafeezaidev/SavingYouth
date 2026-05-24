package com.behavioralfirewall.domain.evolution

import javax.inject.Inject
import javax.inject.Singleton

data class RiskWeights(
    val urgencyWeight: Float,
    val screenChangeWeight: Float,
    val dwellTimeWeight: Float,
    val coreThreshold: Int
)

@Singleton
class ModelGovernanceManager @Inject constructor() {

    // Define safety limits for weights to prevent remote overrides from compromising user protection
    private val minUrgencyWeight = 0.1f
    private val maxUrgencyWeight = 1.5f

    private val minScreenChangeWeight = 0.05f
    private val maxScreenChangeWeight = 1.2f

    private val minDwellTimeWeight = 0.05f
    private val maxDwellTimeWeight = 1.2f

    private val minCoreThreshold = 30
    private val maxCoreThreshold = 85

    /**
     * Validates proposed model weights against absolute safety parameters.
     * Returns true if the proposed model satisfies all stability bounds.
     */
    fun validateWeights(weights: RiskWeights): Boolean {
        if (weights.urgencyWeight !in minUrgencyWeight..maxUrgencyWeight) return false
        if (weights.screenChangeWeight !in minScreenChangeWeight..maxScreenChangeWeight) return false
        if (weights.dwellTimeWeight !in minDwellTimeWeight..maxDwellTimeWeight) return false
        if (weights.coreThreshold !in minCoreThreshold..maxCoreThreshold) return false
        return true
    }

    /**
     * Asserts that a safety override is forced if weights degrade dangerously.
     */
    fun getSafeFallbackWeights(): RiskWeights {
        return RiskWeights(
            urgencyWeight = 0.5f,
            screenChangeWeight = 0.3f,
            dwellTimeWeight = 0.2f,
            coreThreshold = 60
        )
    }
}
