package com.behavioralfirewall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behavioralfirewall.domain.engine.AppMonitoringManager
import com.behavioralfirewall.domain.engine.InterventionManager
import com.behavioralfirewall.domain.engine.RiskStateManager
import com.behavioralfirewall.domain.model.InterventionType
import com.behavioralfirewall.domain.model.RiskState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    riskStateManager: RiskStateManager,
    appMonitoringManager: AppMonitoringManager,
    private val interventionManager: InterventionManager,
    private val syncRepository: com.behavioralfirewall.data.remote.appwrite.SyncRepository,
    private val vpnStateManager: com.behavioralfirewall.services.vpn.VpnStateManager,
    private val safeBrowsingManager: com.behavioralfirewall.domain.engine.SafeBrowsingManager,
    private val appCooldownManager: com.behavioralfirewall.domain.engine.AppCooldownManager,
    private val browserSessionManager: com.behavioralfirewall.domain.engine.BrowserSessionManager,
    private val diagnosticsManager: com.behavioralfirewall.domain.engine.DiagnosticsManager,
    private val batteryEfficiencyManager: com.behavioralfirewall.domain.engine.BatteryEfficiencyManager,
    private val integrityManager: com.behavioralfirewall.domain.security.MonitoringIntegrityManager,
    private val resilienceManager: com.behavioralfirewall.domain.security.ResilienceManager
) : ViewModel() {

    init {
        checkIntegrity()
        resilienceManager.recordHeartbeat()
    }

    val riskState: StateFlow<RiskState> = riskStateManager.riskState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RiskState()
    )

    val activeApp: StateFlow<String> = appMonitoringManager.activeAppFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    // VPN and Safe Browsing streams
    val isVpnActive: StateFlow<Boolean> = vpnStateManager.isVpnActive
    val blockedAttemptsToday: StateFlow<Int> = safeBrowsingManager.blockedAttemptsTodayCount
    
    // Cooldown streams
    val cooldownActive: StateFlow<Boolean> = appCooldownManager.cooldownActive
    val cooldownTimeRemaining: StateFlow<Long> = appCooldownManager.cooldownTimeRemaining

    // Intervention status streams
    val activeIntervention: StateFlow<InterventionType?> = interventionManager.activeIntervention
    val todayInterventionCount: StateFlow<Int> = interventionManager.todayInterventionCount
    val successRate: StateFlow<Float> = interventionManager.successRate
    val cooldownRemainingMs: StateFlow<Long> = interventionManager.cooldownRemainingMs
    val mostCommonTrigger: StateFlow<String> = interventionManager.mostCommonTrigger
    val failedInterventions: StateFlow<Int> = interventionManager.failedInterventions

    // Integrity and Resilience metrics
    val integrityState = integrityManager.integrityState
    val repairNeeded = resilienceManager.repairNeeded

    fun checkIntegrity() {
        integrityManager.checkIntegrity()
    }

    fun resetResilienceCrashes() {
        resilienceManager.resetCrashes()
    }

    // Optimization metrics
    val systemHealthPercentage: StateFlow<Int> = diagnosticsManager.systemHealthPercentage
    val batteryMode: StateFlow<com.behavioralfirewall.domain.engine.BatteryOptimizationMode> = batteryEfficiencyManager.optimizationMode

    fun triggerTestIntervention(type: InterventionType) {
        interventionManager.triggerIntervention(type, "com.mock.riskyapp", 85)
    }

    fun manualComply() {
        interventionManager.complyIntervention("manual_test", 0)
    }

    fun manualDismiss() {
        interventionManager.dismissIntervention("manual_test", 0)
    }
}
