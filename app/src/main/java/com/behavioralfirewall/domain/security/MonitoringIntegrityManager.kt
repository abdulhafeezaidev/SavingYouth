package com.behavioralfirewall.domain.security

import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import com.behavioralfirewall.services.vpn.VpnStateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class IntegrityState(
    val accessibilityActive: Boolean = false,
    val overlayPermissionGranted: Boolean = false,
    val vpnActive: Boolean = false,
    val batteryOptimizationExempted: Boolean = false,
    val systemIntact: Boolean = false
)

@Singleton
class MonitoringIntegrityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vpnStateManager: VpnStateManager
) {
    private val _integrityState = MutableStateFlow(IntegrityState())
    val integrityState = _integrityState.asStateFlow()

    fun checkIntegrity(): IntegrityState {
        val isAccessibilityActive = isAccessibilityServiceEnabled()
        val isOverlayGranted = Settings.canDrawOverlays(context)
        val isVpnActive = vpnStateManager.isVpnActive.value
        val isBatteryExempted = isBatteryOptimizationExempted()

        val intact = isAccessibilityActive && isOverlayGranted && isVpnActive && isBatteryExempted

        val newState = IntegrityState(
            accessibilityActive = isAccessibilityActive,
            overlayPermissionGranted = isOverlayGranted,
            vpnActive = isVpnActive,
            batteryOptimizationExempted = isBatteryExempted,
            systemIntact = intact
        )
        _integrityState.value = newState
        return newState
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedService = "${context.packageName}/com.behavioralfirewall.services.accessibility.AccessibilityMonitoringService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedService)
    }

    private fun isBatteryOptimizationExempted(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}
