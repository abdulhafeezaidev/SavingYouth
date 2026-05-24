package com.behavioralfirewall.domain.launch

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class AppEnvironment {
    BETA,
    PRODUCTION
}

@Singleton
class ReleaseDeploymentManager @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) {
    private val _currentEnvironment = MutableStateFlow(AppEnvironment.PRODUCTION)
    val currentEnvironment: StateFlow<AppEnvironment> = _currentEnvironment.asStateFlow()

    private val _rolloutPercentage = MutableStateFlow(100) // Default to 100% unless configured otherwise
    val rolloutPercentage: StateFlow<Int> = _rolloutPercentage.asStateFlow()

    private val _isRollbackActive = MutableStateFlow(false)
    val isRollbackActive: StateFlow<Boolean> = _isRollbackActive.asStateFlow()

    private val userRolloutSeedKey = "user_rollout_seed_v1"
    private val userEnvironmentKey = "user_environment_v1"
    private val rollbackActiveKey = "rollback_active_v1"
    private val rolloutPercentageKey = "rollout_percentage_v1"

    private var userSeed: Int = 0

    init {
        // Retrieve or generate secure seed (0-99) for rollout allocation
        val savedSeed = secureStorageManager.getToken(userRolloutSeedKey)
        if (savedSeed == null) {
            val newSeed = Random.nextInt(100)
            secureStorageManager.saveToken(userRolloutSeedKey, newSeed.toString())
            userSeed = newSeed
        } else {
            userSeed = savedSeed.toIntOrNull() ?: Random.nextInt(100)
        }

        // Retrieve saved environment
        val savedEnv = secureStorageManager.getToken(userEnvironmentKey)
        if (savedEnv != null) {
            _currentEnvironment.value = try {
                AppEnvironment.valueOf(savedEnv)
            } catch (e: Exception) {
                AppEnvironment.PRODUCTION
            }
        }

        // Retrieve saved rollout percentage
        val savedPercentage = secureStorageManager.getToken(rolloutPercentageKey)
        if (savedPercentage != null) {
            _rolloutPercentage.value = savedPercentage.toIntOrNull() ?: 100
        }

        // Retrieve rollback state
        _isRollbackActive.value = secureStorageManager.getBooleanSetting(rollbackActiveKey, false)
    }

    /**
     * Determines whether the user falls inside a specific staged rollout threshold.
     */
    fun isAllocatedToRollout(targetPercentage: Int): Boolean {
        if (_isRollbackActive.value) return false
        return userSeed < targetPercentage
    }

    /**
     * Set the current app environment (BETA or PRODUCTION).
     */
    fun setEnvironment(environment: AppEnvironment) {
        _currentEnvironment.value = environment
        secureStorageManager.saveToken(userEnvironmentKey, environment.name)
    }

    /**
     * Updates the rollout percentage remotely (from Appwrite or admin console).
     */
    fun updateRolloutPercentage(percentage: Int) {
        val clamped = percentage.coerceIn(0, 100)
        _rolloutPercentage.value = clamped
        secureStorageManager.saveToken(rolloutPercentageKey, clamped.toString())
    }

    /**
     * Triggers an emergency rollback, instantly setting rollout percentage to 0%
     * and turning off rolled-out features for local stability.
     */
    fun triggerEmergencyRollback() {
        _isRollbackActive.value = true
        secureStorageManager.saveBooleanSetting(rollbackActiveKey, true)
        updateRolloutPercentage(0)
    }

    /**
     * Resolves rollback state and resumes normal staged operations.
     */
    fun clearEmergencyRollback() {
        _isRollbackActive.value = false
        secureStorageManager.saveBooleanSetting(rollbackActiveKey, false)
    }

    /**
     * Evaluates feature flags against the user's rollout group allocation.
     * Some complex features are only enabled at higher rollout stages.
     */
    fun isFeatureAvailable(featureKey: String): Boolean {
        if (_isRollbackActive.value) return false

        // Feature release stages mapping: some features require higher rollout stages
        val requiredStage = when (featureKey) {
            "feature_vpn_filter" -> 50      // Staged VPN activation starts at 50%
            "feature_strict_overlays" -> 20 // Overlay warnings start at 20%
            "feature_accountability" -> 10  // Accountability messaging starts at 10%
            "feature_adaptive_risk" -> 0   // Core learning enabled for all
            else -> 0
        }

        // If Beta user, bypass restrictions for immediate testing
        if (_currentEnvironment.value == AppEnvironment.BETA) {
            return true
        }

        // Normal Production users check staged rollout allocation
        return isAllocatedToRollout(_rolloutPercentage.value.coerceAtMost(100)) && isAllocatedToRollout(100 - requiredStage + 1)
    }

    fun getUserSeed(): Int = userSeed
}
