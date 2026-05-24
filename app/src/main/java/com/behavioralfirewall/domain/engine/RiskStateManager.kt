package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.engine.escalation.IntentRiskEngine
import com.behavioralfirewall.domain.model.RiskLevel
import com.behavioralfirewall.domain.model.RiskState
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
class RiskStateManager @Inject constructor(
    private val riskEngine: RiskEngine,
    private val riskProcessingPipeline: RiskProcessingPipeline,
    private val batteryEfficiencyManager: BatteryEfficiencyManager,
    private val intentRiskEngine: IntentRiskEngine
) {
    private val _riskState = MutableStateFlow(RiskState())
    val riskState: StateFlow<RiskState> = _riskState.asStateFlow()

    private var evaluationJob: Job? = null
    private var lastPackage = ""

    fun startMonitoring(scope: CoroutineScope) {
        if (evaluationJob != null) return

        evaluationJob = scope.launch(Dispatchers.Default) {
            riskProcessingPipeline.combinedSignals.collect { signal ->
                if (signal.packageName.isNotEmpty() && signal.packageName != lastPackage) {
                    riskProcessingPipeline.incrementSwitchCount()
                    lastPackage = signal.packageName
                }
                
                val telemetryScore = riskEngine.calculateRiskScore(
                    packageName = signal.packageName,
                    sessionDurationMs = signal.durationMs,
                    switchCount = signal.switchCount,
                    stressLevel = signal.stressLevel
                )

                val intentScore = intentRiskEngine.riskScore.value
                val score = maxOf(telemetryScore, intentScore)

                val level = when {
                    score >= 80 -> RiskLevel.CRITICAL
                    score >= 60 -> RiskLevel.HIGH
                    score >= 30 -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }

                // Dynamically scale battery monitoring checks based on new risk level
                batteryEfficiencyManager.updateEfficiencySettings(level)

                _riskState.value = RiskState(
                    score = score,
                    level = level,
                    activeTriggers = emptyList()
                )
            }
        }
    }

    fun stopMonitoring() {
        evaluationJob?.cancel()
        evaluationJob = null
        riskProcessingPipeline.resetSwitchCount()
    }
}
