package com.behavioralfirewall.domain.launch

import com.behavioralfirewall.domain.security.PrivacyControlManager
import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionMonitoringManager @Inject constructor(
    private val privacyControlManager: PrivacyControlManager,
    private val secureStorageManager: SecureStorageManager
) {
    private val reporters = mutableListOf<CrashReporter>()

    // Local telemetry keys
    private val totalSessionsKey = "monitoring_sessions_count_v1"
    private val crashCountKey = "monitoring_crashes_count_v1"
    private val anrCountKey = "monitoring_anr_count_v1"
    private val vpnStartsKey = "monitoring_vpn_starts_count_v1"
    private val vpnFailuresKey = "monitoring_vpn_failures_count_v1"
    private val interventionsKey = "monitoring_interventions_count_v1"
    private val interventionSuccessesKey = "monitoring_intervention_success_count_v1"
    private val onboardingCompletedKey = "monitoring_onboarding_completed_v1"

    // Live StateFlows for Dashboard Display
    private val _crashRate = MutableStateFlow(0f)
    val crashRate: StateFlow<Float> = _crashRate.asStateFlow()

    private val _anrRate = MutableStateFlow(0f)
    val anrRate: StateFlow<Float> = _anrRate.asStateFlow()

    private val _vpnFailureRate = MutableStateFlow(0f)
    val vpnFailureRate: StateFlow<Float> = _vpnFailureRate.asStateFlow()

    private val _interventionFailureRate = MutableStateFlow(0f)
    val interventionFailureRate: StateFlow<Float> = _interventionFailureRate.asStateFlow()

    private val _serviceUptimePercent = MutableStateFlow(100f)
    val serviceUptimePercent: StateFlow<Float> = _serviceUptimePercent.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    init {
        // Register default privacy-redacting logcat reporter
        registerReporter(LogcatCrashReporter())
        loadTelemetry()
    }

    fun registerReporter(reporter: CrashReporter) {
        synchronized(reporters) {
            if (!reporters.contains(reporter)) {
                reporters.add(reporter)
            }
        }
    }

    private fun loadTelemetry() {
        val sessions = getIntSetting(totalSessionsKey, 1)
        val crashes = getIntSetting(crashCountKey, 0)
        val anrs = getIntSetting(anrCountKey, 0)
        val vpnStarts = getIntSetting(vpnStartsKey, 0)
        val vpnFails = getIntSetting(vpnFailuresKey, 0)
        val interventions = getIntSetting(interventionsKey, 0)
        val successes = getIntSetting(interventionSuccessesKey, 0)

        _crashRate.value = if (sessions > 0) (crashes.toFloat() / sessions) * 100f else 0f
        _anrRate.value = if (sessions > 0) (anrs.toFloat() / sessions) * 100f else 0f
        _vpnFailureRate.value = if (vpnStarts > 0) (vpnFails.toFloat() / vpnStarts) * 100f else 0f
        
        val failures = interventions - successes
        _interventionFailureRate.value = if (interventions > 0) (failures.toFloat() / interventions) * 100f else 0f
        _onboardingCompleted.value = secureStorageManager.getBooleanSetting(onboardingCompletedKey, false)

        // Compute simulated service uptime
        val expectedUptime = 100f - (_vpnFailureRate.value * 0.15f) - (_crashRate.value * 0.5f)
        _serviceUptimePercent.value = expectedUptime.coerceIn(0f, 100f)
    }

    /**
     * Increments sessions and tracks app launches.
     */
    fun incrementSession() {
        val current = getIntSetting(totalSessionsKey, 0)
        saveIntSetting(totalSessionsKey, current + 1)
        loadTelemetry()
    }

    /**
     * Reports an app crash safely in real users.
     */
    fun reportCrash(throwable: Throwable, contextData: Map<String, String> = emptyMap()) {
        val crashes = getIntSetting(crashCountKey, 0)
        saveIntSetting(crashCountKey, crashes + 1)
        loadTelemetry()

        if (privacyControlManager.isAnalyticsEnabled()) {
            synchronized(reporters) {
                reporters.forEach { it.logCrash(throwable, contextData) }
            }
        }
    }

    /**
     * Reports an ANR spike safely.
     */
    fun reportAnr() {
        val anrs = getIntSetting(anrCountKey, 0)
        saveIntSetting(anrCountKey, anrs + 1)
        loadTelemetry()

        if (privacyControlManager.isAnalyticsEnabled()) {
            synchronized(reporters) {
                reporters.forEach {
                    it.logEvent("stability_anr_detected", mapOf("timestamp" to System.currentTimeMillis()))
                }
            }
        }
    }

    /**
     * Reports a VPN connection start.
     */
    fun reportVpnStarted() {
        val vpnStarts = getIntSetting(vpnStartsKey, 0)
        saveIntSetting(vpnStartsKey, vpnStarts + 1)
        loadTelemetry()
    }

    /**
     * Reports a VPN connection failure.
     */
    fun reportVpnFailure() {
        val vpnFails = getIntSetting(vpnFailuresKey, 0)
        saveIntSetting(vpnFailuresKey, vpnFails + 1)
        loadTelemetry()

        if (privacyControlManager.isAnalyticsEnabled()) {
            synchronized(reporters) {
                reporters.forEach {
                    it.logEvent("stability_vpn_failure", mapOf("timestamp" to System.currentTimeMillis()))
                }
            }
        }
    }

    // PRIVACY-SAFE ANALYTICS TRACKING METHODS

    fun trackOnboardingCompletion() {
        secureStorageManager.saveBooleanSetting(onboardingCompletedKey, true)
        _onboardingCompleted.value = true
        trackEvent("onboarding_completion", mapOf("status" to "success"))
    }

    fun trackPermissionAttempt(permissionName: String, granted: Boolean) {
        trackEvent("permission_setup_result", mapOf(
            "permission_type" to permissionName,
            "is_granted" to granted
        ))
    }

    fun trackInterventionTriggered(interventionType: String) {
        val count = getIntSetting(interventionsKey, 0)
        saveIntSetting(interventionsKey, count + 1)
        loadTelemetry()

        trackEvent("intervention_triggered", mapOf("type" to interventionType))
    }

    fun trackInterventionAccepted(interventionType: String) {
        val count = getIntSetting(interventionSuccessesKey, 0)
        saveIntSetting(interventionSuccessesKey, count + 1)
        loadTelemetry()

        trackEvent("intervention_accepted", mapOf("type" to interventionType))
    }

    fun trackInterventionRejected(interventionType: String) {
        loadTelemetry()
        trackEvent("intervention_rejected", mapOf("type" to interventionType))
    }

    fun trackRelapseRecoverySuccess() {
        trackEvent("relapse_recovery_event", mapOf("state" to "recovered"))
    }

    fun trackStreakMilestone(milestoneDays: Int) {
        trackEvent("streak_milestone", mapOf("days" to milestoneDays))
    }

    fun trackEvent(eventName: String, params: Map<String, Any>) {
        if (privacyControlManager.isAnalyticsEnabled()) {
            val secureParams = PrivacyRedactor.redactMap(params)
            synchronized(reporters) {
                reporters.forEach { it.logEvent(eventName, secureParams) }
            }
        }
    }

    private fun getIntSetting(key: String, default: Int): Int {
        val saved = secureStorageManager.getToken(key) ?: return default
        return saved.toIntOrNull() ?: default
    }

    private fun saveIntSetting(key: String, value: Int) {
        secureStorageManager.saveToken(key, value.toString())
    }
}
