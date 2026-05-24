package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.UserStateEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

data class CombinedRiskSignal(
    val packageName: String,
    val durationMs: Long,
    val switchCount: Int,
    val stressLevel: Int,
    val batteryModeIntervalMs: Long
)

@Singleton
class RiskProcessingPipeline @Inject constructor(
    private val appMonitoringManager: AppMonitoringManager,
    private val batteryEfficiencyManager: BatteryEfficiencyManager,
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _switchCount = MutableStateFlow(0)
    
    // Combine raw flows into a single unified signal flow, debounced to avoid CPU thrashing
    val combinedSignals: Flow<CombinedRiskSignal> = combine(
        appMonitoringManager.activeAppFlow,
        _switchCount,
        repository.getUserState().map { it?.stressLevel ?: 0 }.distinctUntilChanged(),
        batteryEfficiencyManager.currentIntervalMs
    ) { activeApp, switchCount, stressLevel, intervalMs ->
        CombinedRiskSignal(
            packageName = activeApp,
            durationMs = appMonitoringManager.getActiveAppDuration(),
            switchCount = switchCount,
            stressLevel = stressLevel,
            batteryModeIntervalMs = intervalMs
        )
    }.debounce { signal ->
        // Dynamically throttle emissions based on active battery optimization modes!
        // Low-risk daytime (large interval) has longer debounce than critical late-night
        if (signal.batteryModeIntervalMs >= 30000L) 1000L else 300L
    }.distinctUntilChanged()

    init {
        // Automatically decay app switch count over time to conserve state accuracy
        scope.launch {
            while (isActive) {
                delay(10000L)
                if (_switchCount.value > 0) {
                    _switchCount.value = _switchCount.value - 1
                }
            }
        }
    }
    
    fun incrementSwitchCount() {
        _switchCount.value = _switchCount.value + 1
    }
    
    fun resetSwitchCount() {
        _switchCount.value = 0
    }
}
