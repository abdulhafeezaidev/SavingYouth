package com.behavioralfirewall.domain.release

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagManager @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) {
    private val _isVpnFilterEnabled = MutableStateFlow(secureStorageManager.getBooleanSetting("ff_vpn_filter_v2", true))
    val isVpnFilterEnabled = _isVpnFilterEnabled.asStateFlow()

    private val _isDiscreetModeDefault = MutableStateFlow(secureStorageManager.getBooleanSetting("ff_discreet_default_v2", true))
    val isDiscreetModeDefault = _isDiscreetModeDefault.asStateFlow()

    fun updateFeatureFlag(flagKey: String, enabled: Boolean) {
        secureStorageManager.saveBooleanSetting(flagKey, enabled)
        when (flagKey) {
            "ff_vpn_filter_v2" -> _isVpnFilterEnabled.value = enabled
            "ff_discreet_default_v2" -> _isDiscreetModeDefault.value = enabled
        }
    }
}
