package com.behavioralfirewall.domain.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ServiceHealthState(
    val accessibilityServiceActive: Boolean = false,
    val vpnServiceActive: Boolean = false,
    val overlayServiceActive: Boolean = false,
    val dbOptimized: Boolean = true,
    val memoryUsageKb: Long = 0L
)

data class DiagnosticLog(
    val timestamp: Long,
    val category: String, // "LATENCY", "SERVICE", "MEMORY", "CRASH"
    val message: String
)

@Singleton
class DiagnosticsManager @Inject constructor() {
    
    private val _healthState = MutableStateFlow(ServiceHealthState())
    val healthState = _healthState.asStateFlow()
    
    private val _diagnosticLogs = MutableStateFlow<List<DiagnosticLog>>(emptyList())
    val diagnosticLogs = _diagnosticLogs.asStateFlow()
    
    private val _systemHealthPercentage = MutableStateFlow(100)
    val systemHealthPercentage = _systemHealthPercentage.asStateFlow()

    fun updateServiceHealth(
        accessibilityActive: Boolean,
        vpnActive: Boolean,
        overlayActive: Boolean
    ) {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / 1024 // KB
        
        _healthState.value = ServiceHealthState(
            accessibilityServiceActive = accessibilityActive,
            vpnServiceActive = vpnActive,
            overlayServiceActive = overlayActive,
            dbOptimized = true,
            memoryUsageKb = usedMem
        )
        
        // Calculate health percentage
        var score = 50
        if (accessibilityActive) score += 20
        if (vpnActive) score += 20
        if (overlayActive) score += 10
        _systemHealthPercentage.value = score
        
        addLog("SERVICE", "Services status updated: Accessibility=$accessibilityActive, VPN=$vpnActive, Overlay=$overlayActive")
    }

    fun logLatency(component: String, durationMs: Long) {
        addLog("LATENCY", "$component operations took $durationMs ms")
    }
    
    fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / 1024
        addLog("MEMORY", "Memory usage: ${usedMem / 1024} MB / ${runtime.maxMemory() / (1024 * 1024)} MB max")
    }

    fun logCrashRecovered(serviceName: String, reason: String) {
        addLog("CRASH", "Recovered from $serviceName crash: $reason")
        _systemHealthPercentage.value = (_systemHealthPercentage.value - 15).coerceIn(0, 100)
    }

    private fun addLog(category: String, message: String) {
        val newLog = DiagnosticLog(
            timestamp = System.currentTimeMillis(),
            category = category,
            message = message
        )
        val currentList = _diagnosticLogs.value.toMutableList()
        currentList.add(0, newLog)
        if (currentList.size > 50) {
            currentList.removeAt(currentList.lastIndex) // Keep logs capped at 50
        }
        _diagnosticLogs.value = currentList
    }
}
