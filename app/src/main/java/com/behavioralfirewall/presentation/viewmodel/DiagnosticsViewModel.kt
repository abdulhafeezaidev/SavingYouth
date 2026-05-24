package com.behavioralfirewall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behavioralfirewall.domain.engine.*
import com.behavioralfirewall.domain.launch.*
import com.behavioralfirewall.domain.evolution.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val diagnosticsManager: DiagnosticsManager,
    private val batteryEfficiencyManager: BatteryEfficiencyManager,
    private val memorySafetyManager: MemorySafetyManager,
    private val systemSimulationEngine: com.behavioralfirewall.domain.reliability.SystemSimulationEngine,
    private val crashRecoveryManager: com.behavioralfirewall.domain.reliability.CrashRecoveryManager,
    private val releaseDeploymentManager: ReleaseDeploymentManager,
    private val killSwitchManager: KillSwitchManager,
    private val productionStabilityGuard: ProductionStabilityGuard,
    private val productionMonitoringManager: ProductionMonitoringManager,
    private val userFeedbackLoopManager: UserFeedbackLoopManager,
    
    // Evolution Injections
    private val evolutionManager: BehaviorModelEvolutionManager,
    private val postLaunchLearningEngine: PostLaunchLearningEngine,
    private val interventionOptimizationEngine: InterventionOptimizationEngine,
    private val falsePositiveReducer: FalsePositiveReducer,
    private val userSegmentationEngine: UserSegmentationEngine,
    private val abTestingManager: ABTestingManager
) : ViewModel() {

    val healthState: StateFlow<ServiceHealthState> = diagnosticsManager.healthState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServiceHealthState())

    val diagnosticLogs: StateFlow<List<DiagnosticLog>> = diagnosticsManager.diagnosticLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemHealthPercentage: StateFlow<Int> = diagnosticsManager.systemHealthPercentage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val batteryMode: StateFlow<BatteryOptimizationMode> = batteryEfficiencyManager.optimizationMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryOptimizationMode.BALANCED)

    val currentIntervalMs: StateFlow<Long> = batteryEfficiencyManager.currentIntervalMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15000L)

    val trackedOverlaysCount: StateFlow<Int> = memorySafetyManager.trackedOverlaysCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val memoryState: StateFlow<String> = memorySafetyManager.systemMemoryState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Normal")

    val isSimulating: StateFlow<Boolean> = systemSimulationEngine.isSimulating
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastCrashReason: StateFlow<String?> = crashRecoveryManager.lastCrashReason
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Staged Rollout Flows
    val currentEnvironment: StateFlow<AppEnvironment> = releaseDeploymentManager.currentEnvironment
    val rolloutPercentage: StateFlow<Int> = releaseDeploymentManager.rolloutPercentage
    val isRollbackActive: StateFlow<Boolean> = releaseDeploymentManager.isRollbackActive
    val userRolloutSeed: Int = releaseDeploymentManager.getUserSeed()

    // Emergency Kill Switches
    val isGlobalSystemKilled: StateFlow<Boolean> = killSwitchManager.isGlobalSystemKilled
    val isVpnModuleKilled: StateFlow<Boolean> = killSwitchManager.isVpnModuleKilled
    val isOverlayKilled: StateFlow<Boolean> = killSwitchManager.isOverlayKilled
    val isAccessibilityKilled: StateFlow<Boolean> = killSwitchManager.isAccessibilityKilled

    // Stability Guard State
    val stabilityMessage: StateFlow<String> = productionStabilityGuard.statusMessage

    // In-Wild Monitoring Metrics
    val crashRate: StateFlow<Float> = productionMonitoringManager.crashRate
    val anrRate: StateFlow<Float> = productionMonitoringManager.anrRate
    val vpnFailureRate: StateFlow<Float> = productionMonitoringManager.vpnFailureRate
    val interventionFailureRate: StateFlow<Float> = productionMonitoringManager.interventionFailureRate
    val serviceUptimePercent: StateFlow<Float> = productionMonitoringManager.serviceUptimePercent
    val onboardingCompleted: StateFlow<Boolean> = productionMonitoringManager.onboardingCompleted

    // Anonymous User Feedback Flow
    val feedbackList: StateFlow<List<AnonymousFeedback>> = userFeedbackLoopManager.feedbackList
    val isSubmittingFeedback: StateFlow<Boolean> = userFeedbackLoopManager.isSubmitting

    // === EVOLUTION FLOWS ===
    val activeWeights: StateFlow<RiskWeights> = evolutionManager.activeWeights
    val modelVersion: StateFlow<String> = evolutionManager.modelVersion
    
    val systemAccuracy: StateFlow<Float> = postLaunchLearningEngine.systemAccuracy
    val falsePositiveRate: StateFlow<Float> = postLaunchLearningEngine.falsePositiveRate
    val isEvolutionRecommended: StateFlow<Boolean> = postLaunchLearningEngine.isEvolutionRecommended
    val driftReport: StateFlow<String> = postLaunchLearningEngine.driftReport

    val fatigueLevel: StateFlow<FatigueLevel> = interventionOptimizationEngine.fatigueLevel
    val dynamicCooldownMultiplier: StateFlow<Float> = interventionOptimizationEngine.dynamicCooldownMultiplier
    val recommendedInterventionStyle: StateFlow<String> = interventionOptimizationEngine.recommendedInterventionStyle

    val sensitivityDiscount: StateFlow<Float> = falsePositiveReducer.sensitivityDiscount
    val safeSessionsCount: StateFlow<Int> = falsePositiveReducer.safeSessionsCount

    val activeSegment: StateFlow<UserSegment> = userSegmentationEngine.activeSegment
    val activeExperimentGroup: StateFlow<ExperimentGroup> = abTestingManager.activeGroup

    fun startSimulation() {
        systemSimulationEngine.startRapidSwitchingSimulation()
    }

    fun stopSimulation() {
        systemSimulationEngine.stopSimulation()
    }

    fun triggerEmergencyCleanup() {
        viewModelScope.launch {
            memorySafetyManager.performEmergencyMemoryCleanup()
            diagnosticsManager.logLatency("Manual Cleanup", 12L)
        }
    }

    // Rollout Actions
    fun setEnvironment(environment: AppEnvironment) {
        releaseDeploymentManager.setEnvironment(environment)
    }

    fun updateRolloutPercentage(percentage: Int) {
        releaseDeploymentManager.updateRolloutPercentage(percentage)
    }

    fun triggerEmergencyRollback() {
        releaseDeploymentManager.triggerEmergencyRollback()
    }

    fun clearEmergencyRollback() {
        releaseDeploymentManager.clearEmergencyRollback()
    }

    // Kill Switch Actions
    fun setKillSwitch(moduleKey: String, killed: Boolean) {
        killSwitchManager.setKillSwitch(moduleKey, killed)
    }

    // Stability Guard Actions
    fun resetGuardStats() {
        productionStabilityGuard.resetGuardStats()
    }

    fun simulateStabilityOverlayTrigger() {
        productionStabilityGuard.reportOverlayTrigger()
    }

    fun simulateStabilityVpnFailure() {
        productionStabilityGuard.reportVpnFailure()
    }

    // Telemetry Actions
    fun simulateAppLaunch() {
        productionMonitoringManager.incrementSession()
    }

    fun simulateAnrReport() {
        productionMonitoringManager.reportAnr()
    }

    fun simulateCrashReport() {
        productionMonitoringManager.reportCrash(RuntimeException("Simulated Production Crash"))
    }

    fun simulateVpnConnectionFailure() {
        productionMonitoringManager.reportVpnFailure()
    }

    fun trackIntervention(type: String, success: Boolean) {
        productionMonitoringManager.trackInterventionTriggered(type)
        if (success) {
            productionMonitoringManager.trackInterventionAccepted(type)
        } else {
            productionMonitoringManager.trackInterventionRejected(type)
        }
    }

    // Feedback Actions
    fun submitFeedback(category: FeedbackCategory, rawText: String) {
        userFeedbackLoopManager.submitFeedback(category, rawText) { success ->
            if (success) {
                diagnosticsManager.logLatency("Feedback Submitted", 15L)
            }
        }
    }

    fun clearFeedbackLogs() {
        userFeedbackLoopManager.clearFeedbackLogs()
    }

    // === EVOLUTION ACTIONS ===
    
    fun triggerWeightEvolution(urgency: Float, screen: Float, dwell: Float, threshold: Int) {
        val proposed = RiskWeights(urgency, screen, dwell, threshold)
        evolutionManager.evolveWeights(proposed, "v1.2.0-manual-evolution")
    }

    fun triggerWeightRollback() {
        evolutionManager.rollbackToDefaults()
    }

    fun applyRecommendedWeights() {
        postLaunchLearningEngine.applyRecommendedWeights()
    }

    fun forceExperimentCohort(group: ExperimentGroup) {
        abTestingManager.forceExperimentCohort(group)
    }

    /**
     * Simulates safe app usage without any interventions.
     * Gradually reduces model sensitivity and segments user into recovery/stable categories.
     */
    fun simulateSafeUsageSession() {
        falsePositiveReducer.incrementSafeSession()
        
        // Reevaluate user segmentation dynamically based on safe progress
        val currentDiscount = sensitivityDiscount.value
        val safeCount = safeSessionsCount.value
        val avgRisk = 25f * currentDiscount
        
        userSegmentationEngine.reevaluateSegment(
            averageRiskScore = avgRisk,
            successRate = 0.95f,
            currentStreakDays = safeCount
        )
        
        diagnosticsManager.logLatency("Simulate Safe Session ($safeCount)", 8L)
    }

    /**
     * Simulates a high-risk event or overlay dismissal failure.
     * Resets safe session streaks, increases fatigue, and segments user into higher-risk categories.
     */
    fun simulateHighRiskFailure() {
        falsePositiveReducer.resetSafeSessions()
        interventionOptimizationEngine.recordInterventionOutcome(isComplied = false)
        
        // Reevaluate segmentation into High Risk category due to relapse spike
        userSegmentationEngine.reevaluateSegment(
            averageRiskScore = 80f,
            successRate = 0.40f,
            currentStreakDays = 0
        )
        
        diagnosticsManager.logLatency("Simulate High-Risk Failure", 14L)
    }

    fun resetEvolutionMetrics() {
        falsePositiveReducer.resetSafeSessions()
        interventionOptimizationEngine.resetFatigue()
        evolutionManager.rollbackToDefaults()
        
        userSegmentationEngine.reevaluateSegment(
            averageRiskScore = 50f,
            successRate = 0.75f,
            currentStreakDays = 0
        )
        
        diagnosticsManager.logLatency("Evolution Metrics Reset", 10L)
    }
}

