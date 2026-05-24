package com.behavioralfirewall.domain.launch

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KillSwitchManager @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) {
    private val globalKillKey = "kill_switch_global_v1"
    private val vpnKillKey = "kill_switch_vpn_v1"
    private val overlayKillKey = "kill_switch_overlay_v1"
    private val accessibilityKillKey = "kill_switch_accessibility_v1"

    private val _isGlobalSystemKilled = MutableStateFlow(secureStorageManager.getBooleanSetting(globalKillKey, false))
    val isGlobalSystemKilled: StateFlow<Boolean> = _isGlobalSystemKilled.asStateFlow()

    private val _isVpnModuleKilled = MutableStateFlow(secureStorageManager.getBooleanSetting(vpnKillKey, false))
    val isVpnModuleKilled: StateFlow<Boolean> = _isVpnModuleKilled.asStateFlow()

    private val _isOverlayKilled = MutableStateFlow(secureStorageManager.getBooleanSetting(overlayKillKey, false))
    val isOverlayKilled: StateFlow<Boolean> = _isOverlayKilled.asStateFlow()

    private val _isAccessibilityKilled = MutableStateFlow(secureStorageManager.getBooleanSetting(accessibilityKillKey, false))
    val isAccessibilityKilled: StateFlow<Boolean> = _isAccessibilityKilled.asStateFlow()

    /**
     * Updates specific kill switch status securely.
     */
    fun setKillSwitch(moduleKey: String, killed: Boolean) {
        when (moduleKey) {
            "global" -> {
                _isGlobalSystemKilled.value = killed
                secureStorageManager.saveBooleanSetting(globalKillKey, killed)
            }
            "vpn" -> {
                _isVpnModuleKilled.value = killed
                secureStorageManager.saveBooleanSetting(vpnKillKey, killed)
            }
            "overlay" -> {
                _isOverlayKilled.value = killed
                secureStorageManager.saveBooleanSetting(overlayKillKey, killed)
            }
            "accessibility" -> {
                _isAccessibilityKilled.value = killed
                secureStorageManager.saveBooleanSetting(accessibilityKillKey, killed)
            }
        }
    }

    /**
     * Resolves all kill switches to normal active operations.
     */
    fun resetAllKillSwitches() {
        setKillSwitch("global", false)
        setKillSwitch("vpn", false)
        setKillSwitch("overlay", false)
        setKillSwitch("accessibility", false)
    }

    /**
     * Checks if the system should skip a module's operation due to emergency overrides.
     */
    fun isModuleKilled(moduleKey: String): Boolean {
        if (_isGlobalSystemKilled.value) return true
        return when (moduleKey) {
            "vpn" -> _isVpnModuleKilled.value
            "overlay" -> _isOverlayKilled.value
            "accessibility" -> _isAccessibilityKilled.value
            else -> false
        }
    }
}
