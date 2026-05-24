package com.behavioralfirewall.domain.evolution

import com.behavioralfirewall.domain.launch.ReleaseDeploymentManager
import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ExperimentGroup {
    CONTROL,                     // Standard warning dialogs
    EXPERIMENTAL_INTERACTIVE,    // Interactive breath & reflection prompt
    EXPERIMENTAL_DELAY           // Time-delay progressive countdowns
}

@Singleton
class ABTestingManager @Inject constructor(
    private val releaseDeploymentManager: ReleaseDeploymentManager,
    private val secureStorageManager: SecureStorageManager
) {
    private val experimentGroupKey = "ab_experiment_group_v1"

    private val _activeGroup = MutableStateFlow(ExperimentGroup.CONTROL)
    val activeGroup: StateFlow<ExperimentGroup> = _activeGroup.asStateFlow()

    init {
        allocateUserToExperiment()
    }

    private fun allocateUserToExperiment() {
        val saved = secureStorageManager.getToken(experimentGroupKey)
        if (saved != null) {
            _activeGroup.value = try {
                ExperimentGroup.valueOf(saved)
            } catch (e: Exception) {
                ExperimentGroup.CONTROL
            }
            return
        }

        // Use the on-device user seed (0-99) generated during launch configurations (Day 13)
        // to assign experimental cohorts evenly and deterministically
        val seed = releaseDeploymentManager.getUserSeed()
        val assigned = when {
            seed < 33 -> ExperimentGroup.CONTROL
            seed < 66 -> ExperimentGroup.EXPERIMENTAL_INTERACTIVE
            else -> ExperimentGroup.EXPERIMENTAL_DELAY
        }

        _activeGroup.value = assigned
        secureStorageManager.saveToken(experimentGroupKey, assigned.name)
    }

    /**
     * Determines which overlay pattern to launch based on A/B test partition.
     */
    fun resolveActiveInterventionStyle(fallbackStyle: String): String {
        return when (_activeGroup.value) {
            ExperimentGroup.CONTROL -> fallbackStyle
            ExperimentGroup.EXPERIMENTAL_INTERACTIVE -> "REFLECTION_PROMPT"
            ExperimentGroup.EXPERIMENTAL_DELAY -> "PROGRESSIVE_DELAY"
        }
    }

    /**
     * Toggles onboarding flows dynamically for A/B conversion testing.
     */
    fun getOnboardingStepCount(): Int {
        return when (_activeGroup.value) {
            ExperimentGroup.CONTROL -> 4
            ExperimentGroup.EXPERIMENTAL_INTERACTIVE -> 3 // Compact onboarding
            ExperimentGroup.EXPERIMENTAL_DELAY -> 5        // Detailed educational onboarding
        }
    }

    /**
     * Safely changes active test group for local diagnostics simulation.
     */
    fun forceExperimentCohort(group: ExperimentGroup) {
        _activeGroup.value = group
        secureStorageManager.saveToken(experimentGroupKey, group.name)
    }
}
