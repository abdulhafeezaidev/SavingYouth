package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.model.RiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class BatteryOptimizationMode {
    LIGHTWEIGHT, // Daytime low-risk, 30s checks
    BALANCED,    // Standard, 15s checks
    AGGRESSIVE   // High-risk late-night or active elevated risk, 5s checks
}

@Singleton
class BatteryEfficiencyManager @Inject constructor() {
    
    private val _optimizationMode = MutableStateFlow(BatteryOptimizationMode.BALANCED)
    val optimizationMode: StateFlow<BatteryOptimizationMode> = _optimizationMode.asStateFlow()
    
    private val _currentIntervalMs = MutableStateFlow(15000L)
    val currentIntervalMs: StateFlow<Long> = _currentIntervalMs.asStateFlow()

    /**
     * Re-calculate the monitoring interval based on risk level and time of day.
     */
    fun updateEfficiencySettings(currentRiskLevel: RiskLevel) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Late-night period (10 PM to 4 AM) is high-risk
        val isLateNight = hour >= 22 || hour < 4
        
        val mode = when {
            currentRiskLevel == RiskLevel.CRITICAL || currentRiskLevel == RiskLevel.HIGH -> {
                BatteryOptimizationMode.AGGRESSIVE
            }
            isLateNight -> {
                // Late night without active relapse escalation triggers BALANCED or AGGRESSIVE depending on risk
                if (currentRiskLevel == RiskLevel.MEDIUM) {
                    BatteryOptimizationMode.AGGRESSIVE
                } else {
                    BatteryOptimizationMode.BALANCED
                }
            }
            currentRiskLevel == RiskLevel.LOW -> {
                // Daytime low-risk is extremely battery-efficient
                BatteryOptimizationMode.LIGHTWEIGHT
            }
            else -> {
                BatteryOptimizationMode.BALANCED
            }
        }
        
        val interval = when (mode) {
            BatteryOptimizationMode.LIGHTWEIGHT -> 30000L // 30 seconds
            BatteryOptimizationMode.BALANCED -> 15000L    // 15 seconds
            BatteryOptimizationMode.AGGRESSIVE -> 5000L   // 5 seconds
        }
        
        _optimizationMode.value = mode
        _currentIntervalMs.value = interval
    }
}
