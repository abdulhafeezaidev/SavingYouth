package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.BlockedAttemptEntity
import com.behavioralfirewall.domain.model.InterventionType
import com.behavioralfirewall.domain.repository.BehavioralRepository
import com.behavioralfirewall.domain.launch.KillSwitchManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeBrowsingManager @Inject constructor(
    private val repository: BehavioralRepository,
    private val domainRiskAnalyzer: DomainRiskAnalyzer,
    private val interventionManager: InterventionManager,
    private val killSwitchManager: KillSwitchManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _blockedAttemptsTodayCount = MutableStateFlow(0)
    val blockedAttemptsTodayCount: StateFlow<Int> = _blockedAttemptsTodayCount.asStateFlow()

    init {
        loadBlockedCount()
    }

    private fun loadBlockedCount() {
        scope.launch {
            repository.getBlockedAttempts().collect { list ->
                _blockedAttemptsTodayCount.value = list.size
            }
        }
    }

    // Triggered directly from VPN DNS intercepts
    fun evaluateDnsRequest(domain: String): Boolean {
        if (killSwitchManager.isModuleKilled("vpn") || killSwitchManager.isModuleKilled("global")) {
            return false // Bypass local filter completely if killed
        }

        val (isBlocked, category) = domainRiskAnalyzer.isDomainBlocked(domain)
        if (isBlocked) {
            logBlockedAttempt(domain, category)
            
            // Instantly open calm Compose blocked reflection overlay
            scope.launch(Dispatchers.Main) {
                interventionManager.triggerIntervention(
                    type = InterventionType.REFLECTION_PROMPT,
                    triggerPackage = "com.dns.block.$domain",
                    riskScore = 90
                )
            }
            return true // Intercept & Redirect
        }
        return false // Allowed normally
    }

    private fun logBlockedAttempt(domain: String, category: String) {
        scope.launch {
            val entity = BlockedAttemptEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                domain = domain,
                category = category,
                actionTaken = "BLOCKED"
            )
            repository.saveBlockedAttempt(entity)
        }
    }
}
