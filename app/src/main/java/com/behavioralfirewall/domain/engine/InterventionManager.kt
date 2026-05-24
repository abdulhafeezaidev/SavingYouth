package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.RiskEventEntity
import com.behavioralfirewall.domain.model.InterventionState
import com.behavioralfirewall.domain.model.InterventionType
import com.behavioralfirewall.domain.repository.BehavioralRepository
import com.behavioralfirewall.domain.launch.KillSwitchManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionManager @Inject constructor(
    private val riskStateManager: RiskStateManager,
    private val stateMachine: InterventionStateMachine,
    private val frictionManager: FrictionManager,
    private val repository: BehavioralRepository,
    private val appMonitoringManager: AppMonitoringManager,
    private val recommendationEngine: InterventionRecommendationEngine,
    private val recoveryScoreCalculator: RecoveryScoreCalculator,
    private val streakManager: StreakManager,
    private val killSwitchManager: KillSwitchManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _activeIntervention = MutableStateFlow<InterventionType?>(null)
    val activeIntervention: StateFlow<InterventionType?> = _activeIntervention.asStateFlow()

    private val _failedInterventions = MutableStateFlow(0)
    val failedInterventions: StateFlow<Int> = _failedInterventions.asStateFlow()

    private val _todayInterventionCount = MutableStateFlow(0)
    val todayInterventionCount: StateFlow<Int> = _todayInterventionCount.asStateFlow()

    private val _successRate = MutableStateFlow(1.0f) // 100% initial
    val successRate: StateFlow<Float> = _successRate.asStateFlow()

    private val _cooldownRemainingMs = MutableStateFlow(0L)
    val cooldownRemainingMs: StateFlow<Long> = _cooldownRemainingMs.asStateFlow()

    private val _mostCommonTrigger = MutableStateFlow("N/A")
    val mostCommonTrigger: StateFlow<String> = _mostCommonTrigger.asStateFlow()

    // Configurable cooldown duration (30 seconds default)
    var cooldownDurationMs = 30000L
    private var cooldownJob: Job? = null

    init {
        stateMachine.transitionTo(InterventionState.MONITORING)
        observeRiskStateAndAppFlow()
        loadHistoricalAnalytics()
    }

    private fun observeRiskStateAndAppFlow() {
        scope.launch {
            combine(
                riskStateManager.riskState,
                appMonitoringManager.activeAppFlow
            ) { riskState, activeApp ->
                Pair(riskState, activeApp)
            }.collect { (riskState, activeApp) ->
                if (activeApp.isEmpty() || activeApp == "com.behavioralfirewall") return@collect
                
                val currentState = stateMachine.getCurrentState()

                // Suppress updates during active overlays or active cooldowns
                if (currentState == InterventionState.INTERRUPTING || currentState == InterventionState.COOLDOWN) {
                    return@collect
                }

                // Honor local emergency kill switches
                if (killSwitchManager.isModuleKilled("overlay") || killSwitchManager.isModuleKilled("global")) {
                    return@collect
                }

                // Check friction engine guidelines
                val detectedIntervention = frictionManager.evaluateFriction(
                    packageName = activeApp,
                    riskState = riskState,
                    failedInterventionCount = _failedInterventions.value
                )

                if (detectedIntervention != null) {
                    val finalIntervention = recommendationEngine.getRecommendedIntervention(detectedIntervention)
                    triggerIntervention(finalIntervention, activeApp, riskState.score)
                }
            }
        }
    }

    private fun loadHistoricalAnalytics() {
        scope.launch {
            repository.getRiskEvents().collect { events ->
                if (events.isEmpty()) return@collect
                
                // Track today's timestamp limit
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
                
                val todayEvents = events.filter { it.timestamp >= todayStart && it.interventionType != "NONE" }
                _todayInterventionCount.value = todayEvents.size

                // Success rate calculation
                val complied = events.count { it.outcome == "complied" }
                val dismissed = events.count { it.outcome == "dismissed" }
                val totalResolved = complied + dismissed
                if (totalResolved > 0) {
                    _successRate.value = complied.toFloat() / totalResolved.toFloat()
                }

                // Identify most frequent trigger
                val triggerApp = events.filter { it.triggerType != "cooldown" && it.triggerType != "unknown" }
                    .groupBy { it.triggerType }
                    .maxByOrNull { it.value.size }?.key ?: "None"
                
                _mostCommonTrigger.value = triggerApp.substringAfterLast(".")
            }
        }
    }

    fun triggerIntervention(type: InterventionType, triggerPackage: String, riskScore: Int) {
        if (killSwitchManager.isModuleKilled("overlay") || killSwitchManager.isModuleKilled("global")) {
            return
        }

        val nextState = if (_failedInterventions.value >= 3) {
            InterventionState.ESCALATED
        } else {
            InterventionState.INTERRUPTING
        }
        
        if (stateMachine.transitionTo(nextState)) {
            _activeIntervention.value = type
            logOutcome(
                triggerPackage = triggerPackage,
                riskScore = riskScore,
                interventionType = type,
                outcome = "triggered"
            )
        }
    }

    fun dismissIntervention(triggerPackage: String = "unknown", riskScore: Int = 50) {
        val type = _activeIntervention.value ?: return
        _activeIntervention.value = null
        
        // Escalate failed count
        _failedInterventions.value += 1
        
        logOutcome(
            triggerPackage = triggerPackage,
            riskScore = riskScore,
            interventionType = type,
            outcome = "dismissed"
        )
        
        // Recalculate daily recovery score in background
        scope.launch {
            val currentStreak = streakManager.streak.value
            recoveryScoreCalculator.calculateAndSaveDailyScore(currentStreak)
        }
        
        startCooldown()
    }

    fun complyIntervention(triggerPackage: String = "unknown", riskScore: Int = 50) {
        val type = _activeIntervention.value ?: return
        _activeIntervention.value = null
        
        // Clear failed states on compliance
        _failedInterventions.value = 0
        frictionManager.resetTracking()

        logOutcome(
            triggerPackage = triggerPackage,
            riskScore = riskScore,
            interventionType = type,
            outcome = "complied"
        )
        
        // Recalculate daily recovery score in background
        scope.launch {
            val currentStreak = streakManager.streak.value
            recoveryScoreCalculator.calculateAndSaveDailyScore(currentStreak)
        }
        
        startCooldown()
    }

    private fun startCooldown() {
        if (stateMachine.transitionTo(InterventionState.COOLDOWN)) {
            logOutcome(
                triggerPackage = "cooldown",
                riskScore = 0,
                interventionType = InterventionType.WARNING,
                outcome = "cooldown_triggered"
            )

            cooldownJob?.cancel()
            cooldownJob = scope.launch {
                var remaining = cooldownDurationMs
                while (remaining > 0) {
                    _cooldownRemainingMs.value = remaining
                    delay(1000)
                    remaining -= 1000
                }
                _cooldownRemainingMs.value = 0L
                stateMachine.transitionTo(InterventionState.MONITORING)
            }
        }
    }

    private fun logOutcome(
        triggerPackage: String,
        riskScore: Int,
        interventionType: InterventionType,
        outcome: String
    ) {
        scope.launch {
            val logEntity = RiskEventEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                riskScore = riskScore,
                triggerType = triggerPackage,
                interventionType = interventionType.name,
                outcome = outcome
            )
            repository.saveRiskEvent(logEntity)
        }
    }
}
