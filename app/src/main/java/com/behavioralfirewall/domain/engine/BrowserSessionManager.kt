package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.model.InterventionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowserSessionManager @Inject constructor(
    private val appMonitoringManager: AppMonitoringManager,
    private val browserDetector: BrowserDetector,
    private val appCooldownManager: AppCooldownManager,
    private val interventionManager: InterventionManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var browserEscalationCount = 0

    init {
        observeBrowserSession()
    }

    private fun observeBrowserSession() {
        scope.launch {
            appMonitoringManager.activeAppFlow.collectLatest { activePackage ->
                if (browserDetector.isBrowser(activePackage)) {
                    // Lockout re-entry check
                    if (appCooldownManager.isAppCooldownActive()) {
                        interventionManager.triggerIntervention(
                            type = InterventionType.HARD_INTERRUPT,
                            triggerPackage = activePackage,
                            riskScore = 95
                        )
                    }

                    // Track duration in a cancellation-aware collectLatest loop
                    var durationMs: Long
                    while (true) {
                        delay(10000L)
                        durationMs = appMonitoringManager.getActiveAppDuration()
                        
                        // Escalate after 5 minutes of continuous browser usage
                        if (durationMs > 300000) {
                            browserEscalationCount++
                            if (browserEscalationCount >= 2) {
                                appCooldownManager.triggerAppCooldown(300)
                            }
                        }
                    }
                } else {
                    browserEscalationCount = 0
                }
            }
        }
    }
}
