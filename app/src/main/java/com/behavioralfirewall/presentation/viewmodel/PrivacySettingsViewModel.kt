package com.behavioralfirewall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behavioralfirewall.domain.security.PrivacyControlManager
import com.behavioralfirewall.domain.security.SecureBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val privacyControlManager: PrivacyControlManager,
    private val backupManager: SecureBackupManager
) : ViewModel() {

    private val _cloudSyncEnabled = MutableStateFlow(privacyControlManager.isCloudSyncEnabled())
    val cloudSyncEnabled = _cloudSyncEnabled.asStateFlow()

    private val _analyticsEnabled = MutableStateFlow(privacyControlManager.isAnalyticsEnabled())
    val analyticsEnabled = _analyticsEnabled.asStateFlow()

    private val _discreetNotifications = MutableStateFlow(privacyControlManager.isDiscreetNotificationsEnabled())
    val discreetNotifications = _discreetNotifications.asStateFlow()

    private val _exportPayload = MutableStateFlow("")
    val exportPayload = _exportPayload.asStateFlow()

    private val _backupPayload = MutableStateFlow("")
    val backupPayload = _backupPayload.asStateFlow()

    private val _actionStatus = MutableStateFlow("")
    val actionStatus = _actionStatus.asStateFlow()

    fun toggleCloudSync(enabled: Boolean) {
        privacyControlManager.setCloudSyncEnabled(enabled)
        _cloudSyncEnabled.value = enabled
        _actionStatus.value = "Cloud Sync state updated."
    }

    fun toggleAnalytics(enabled: Boolean) {
        privacyControlManager.setAnalyticsEnabled(enabled)
        _analyticsEnabled.value = enabled
        _actionStatus.value = "Analytics telemetry state updated."
    }

    fun toggleDiscreetNotifications(enabled: Boolean) {
        privacyControlManager.setDiscreetNotificationsEnabled(enabled)
        _discreetNotifications.value = enabled
        _actionStatus.value = "Notification privacy state updated."
    }

    fun exportLocalData() {
        viewModelScope.launch {
            val data = privacyControlManager.exportBehavioralDataJson()
            _exportPayload.value = data
            _actionStatus.value = "Local telemetry packaged for export."
        }
    }

    fun clearBehavioralData() {
        viewModelScope.launch {
            privacyControlManager.deleteLocalBehavioralData()
            _actionStatus.value = "All historical behavioral logs erased."
        }
    }

    fun createSecureBackup() {
        viewModelScope.launch {
            val payload = backupManager.generateBackupPayload()
            _backupPayload.value = payload
            _actionStatus.value = "Secure encrypted backup generated."
        }
    }

    fun restoreSecureBackup(payload: String) {
        viewModelScope.launch {
            val success = backupManager.restoreBackupPayload(payload)
            if (success) {
                _actionStatus.value = "Recovery backup restored successfully."
            } else {
                _actionStatus.value = "Error: Backup corrupt or invalid key."
            }
        }
    }

    fun clearActionStatus() {
        _actionStatus.value = ""
    }
}
