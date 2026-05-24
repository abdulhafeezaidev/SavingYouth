package com.behavioralfirewall.domain.evolution

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorModelEvolutionManager @Inject constructor(
    private val governanceManager: ModelGovernanceManager,
    private val secureStorageManager: SecureStorageManager
) {
    private val urgencyWeightKey = "evolved_urgency_weight_v1"
    private val screenChangeWeightKey = "evolved_screen_weight_v1"
    private val dwellTimeWeightKey = "evolved_dwell_weight_v1"
    private val coreThresholdKey = "evolved_core_threshold_v1"
    private val modelVersionKey = "evolved_model_version_v1"

    private val _activeWeights = MutableStateFlow(governanceManager.getSafeFallbackWeights())
    val activeWeights: StateFlow<RiskWeights> = _activeWeights.asStateFlow()

    private val _modelVersion = MutableStateFlow("v1.0.0-default")
    val modelVersion: StateFlow<String> = _modelVersion.asStateFlow()

    init {
        loadSavedWeights()
    }

    private fun loadSavedWeights() {
        val savedUrgency = secureStorageManager.getToken(urgencyWeightKey)?.toFloatOrNull()
        val savedScreen = secureStorageManager.getToken(screenChangeWeightKey)?.toFloatOrNull()
        val savedDwell = secureStorageManager.getToken(dwellTimeWeightKey)?.toFloatOrNull()
        val savedThreshold = secureStorageManager.getToken(coreThresholdKey)?.toIntOrNull()
        val version = secureStorageManager.getToken(modelVersionKey) ?: "v1.0.0-default"

        if (savedUrgency != null && savedScreen != null && savedDwell != null && savedThreshold != null) {
            val loaded = RiskWeights(savedUrgency, savedScreen, savedDwell, savedThreshold)
            if (governanceManager.validateWeights(loaded)) {
                _activeWeights.value = loaded
                _modelVersion.value = version
                return
            }
        }
        
        // If loaded values are invalid or absent, fallback to absolute safe defaults
        rollbackToDefaults()
    }

    /**
     * Attempts to evolve the active model parameters safely.
     * Rejects updates if they violate the governance stability thresholds.
     */
    fun evolveWeights(proposed: RiskWeights, newVersion: String): Boolean {
        if (governanceManager.validateWeights(proposed)) {
            _activeWeights.value = proposed
            _modelVersion.value = newVersion

            // Persist securely
            secureStorageManager.saveToken(urgencyWeightKey, proposed.urgencyWeight.toString())
            secureStorageManager.saveToken(screenChangeWeightKey, proposed.screenChangeWeight.toString())
            secureStorageManager.saveToken(dwellTimeWeightKey, proposed.dwellTimeWeight.toString())
            secureStorageManager.saveToken(coreThresholdKey, proposed.coreThreshold.toString())
            secureStorageManager.saveToken(modelVersionKey, newVersion)
            return true
        }
        return false
    }

    /**
     * Instantly rolls back weights to safe defaults.
     */
    fun rollbackToDefaults() {
        val fallback = governanceManager.getSafeFallbackWeights()
        _activeWeights.value = fallback
        _modelVersion.value = "v1.0.0-safe-fallback"

        secureStorageManager.saveToken(urgencyWeightKey, fallback.urgencyWeight.toString())
        secureStorageManager.saveToken(screenChangeWeightKey, fallback.screenChangeWeight.toString())
        secureStorageManager.saveToken(dwellTimeWeightKey, fallback.dwellTimeWeight.toString())
        secureStorageManager.saveToken(coreThresholdKey, fallback.coreThreshold.toString())
        secureStorageManager.saveToken(modelVersionKey, "v1.0.0-safe-fallback")
    }
}
