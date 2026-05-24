package com.behavioralfirewall.domain.launch

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionStabilityGuard @Inject constructor(
    private val killSwitchManager: KillSwitchManager,
    private val secureStorageManager: SecureStorageManager
) {
    private val crashCountKey = "stability_crash_count_v1"
    private val lastCrashTimeKey = "stability_last_crash_time_v1"
    private val overlayTriggersKey = "stability_overlay_triggers_v1"
    private val vpnFailuresKey = "stability_vpn_failures_v1"

    private val _statusMessage = MutableStateFlow("System Stable")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        checkCrashLoopSpike()
    }

    /**
     * Increment and evaluate crash loop spikes during application startup.
     * If the app crashed repeatedly in a short duration, activate global kill switch.
     */
    private fun checkCrashLoopSpike() {
        val now = System.currentTimeMillis()
        val lastCrash = secureStorageManager.getToken(lastCrashTimeKey)?.toLongOrNull() ?: 0L
        var crashCount = secureStorageManager.getToken(crashCountKey)?.toIntOrNull() ?: 0

        if (now - lastCrash < 300000) { // 5 minutes window
            crashCount++
            if (crashCount >= 3) {
                killSwitchManager.setKillSwitch("global", true)
                _statusMessage.value = "CRITICAL: Global kill switch activated due to crash loop detection."
            } else {
                _statusMessage.value = "Warning: App restarted after quick crash ($crashCount/3)"
            }
        } else {
            // Reset count if the app has run stably past the window
            crashCount = 1
            _statusMessage.value = "System Stable"
        }

        secureStorageManager.saveToken(lastCrashTimeKey, now.toString())
        secureStorageManager.saveToken(crashCountKey, crashCount.toString())
    }

    /**
     * Reports an intervention overlay was displayed.
     * Monitors frequency to prevent spam or UI freeze locks.
     */
    fun reportOverlayTrigger() {
        val now = System.currentTimeMillis()
        val savedTriggers = getSavedTimestamps(overlayTriggersKey)
        val filtered = savedTriggers.filter { now - it < 300000 } // 5 minutes rolling window
        val updated = filtered + now

        saveTimestamps(overlayTriggersKey, updated)

        if (updated.size >= 5) {
            killSwitchManager.setKillSwitch("overlay", true)
            _statusMessage.value = "ALERT: Overlays auto-disabled due to intervention overload (>5 triggers in 5m)."
        }
    }

    /**
     * Reports a VPN connection attempt failure or crash.
     * Monitors frequency to prevent battery drainage from constant reconnection loops.
     */
    fun reportVpnFailure() {
        val now = System.currentTimeMillis()
        val savedFailures = getSavedTimestamps(vpnFailuresKey)
        val filtered = savedFailures.filter { now - it < 600000 } // 10 minutes rolling window
        val updated = filtered + now

        saveTimestamps(vpnFailuresKey, updated)

        if (updated.size >= 3) {
            killSwitchManager.setKillSwitch("vpn", true)
            _statusMessage.value = "ALERT: VPN filter auto-disabled due to stability failures (>3 restarts/fails in 10m)."
        }
    }

    /**
     * Clears all recorded stability errors to resume normal operation.
     */
    fun resetGuardStats() {
        secureStorageManager.saveToken(crashCountKey, "0")
        secureStorageManager.saveToken(lastCrashTimeKey, "0")
        saveTimestamps(overlayTriggersKey, emptyList())
        saveTimestamps(vpnFailuresKey, emptyList())
        killSwitchManager.resetAllKillSwitches()
        _statusMessage.value = "System Stable"
    }

    private fun getSavedTimestamps(key: String): List<Long> {
        val raw = secureStorageManager.getToken(key) ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.toLongOrNull() }
    }

    private fun saveTimestamps(key: String, list: List<Long>) {
        val serialized = list.joinToString(",")
        secureStorageManager.saveToken(key, serialized)
    }
}
