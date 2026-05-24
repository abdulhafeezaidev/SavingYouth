package com.behavioralfirewall.domain.reliability

import com.behavioralfirewall.domain.engine.AppMonitoringManager
import com.behavioralfirewall.domain.engine.RiskStateManager
import com.behavioralfirewall.domain.security.SecureDiagnosticsLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemSimulationEngine @Inject constructor(
    private val appMonitoringManager: AppMonitoringManager,
    private val riskStateManager: RiskStateManager,
    private val logger: SecureDiagnosticsLogger
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    private var simulationJob: Job? = null

    /**
     * Simulates a rapid user behavior loop (60 app switches in a minute) to stress test
     * risk scoring combinations, filters, and overlays.
     */
    fun startRapidSwitchingSimulation() {
        if (_isSimulating.value) return
        _isSimulating.value = true
        
        simulationJob = scope.launch {
            logger.i("SimulationEngine", "Starting Stress Test: Rapid Switching Simulation...")
            val testPackages = listOf(
                "com.android.chrome",
                "com.sec.android.app.sbrowser",
                "com.android.vending",
                "com.instagram.android",
                "com.twitter.android",
                "com.behavioralfirewall"
            )
            
            repeat(60) { index ->
                val pkg = testPackages[index % testPackages.size]
                logger.d("SimulationEngine", "Simulating app switch to: $pkg")
                appMonitoringManager.emitActiveApp(pkg)
                delay(800L) // Switch every 800ms
            }
            
            logger.i("SimulationEngine", "Rapid Switching Simulation Completed.")
            _isSimulating.value = false
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _isSimulating.value = false
    }
}
