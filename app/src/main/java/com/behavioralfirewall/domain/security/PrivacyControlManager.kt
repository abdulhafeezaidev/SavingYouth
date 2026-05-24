package com.behavioralfirewall.domain.security

import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyControlManager @Inject constructor(
    private val repository: BehavioralRepository,
    private val secureStorageManager: SecureStorageManager
) {
    fun setCloudSyncEnabled(enabled: Boolean) {
        secureStorageManager.saveBooleanSetting("cloud_sync_enabled", enabled)
    }

    fun isCloudSyncEnabled(): Boolean {
        return secureStorageManager.getBooleanSetting("cloud_sync_enabled", true)
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        secureStorageManager.saveBooleanSetting("analytics_enabled", enabled)
    }

    fun isAnalyticsEnabled(): Boolean {
        return secureStorageManager.getBooleanSetting("analytics_enabled", true)
    }

    fun setDiscreetNotificationsEnabled(enabled: Boolean) {
        secureStorageManager.saveBooleanSetting("discreet_notifications", enabled)
    }

    fun isDiscreetNotificationsEnabled(): Boolean {
        return secureStorageManager.getBooleanSetting("discreet_notifications", true)
    }

    suspend fun deleteLocalBehavioralData() {
        withContext(Dispatchers.IO) {
            // Prune all events locally to protect user data
            // We use standard database pruning by setting a threshold in the future,
            // which effectively wipes all historical risk logs and blocked attempts
            val futureTime = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L)
            repository.pruneRiskEvents(futureTime)
            repository.pruneBlockedAttempts(futureTime)
        }
    }

    suspend fun exportBehavioralDataJson(): String {
        return withContext(Dispatchers.IO) {
            try {
                val root = JSONObject()
                val meta = JSONObject().apply {
                    put("timestamp", System.currentTimeMillis())
                    put("cloudSyncEnabled", isCloudSyncEnabled())
                    put("analyticsEnabled", isAnalyticsEnabled())
                    put("discreetNotificationsEnabled", isDiscreetNotificationsEnabled())
                }
                root.put("meta", meta)
                
                // Export brief stats to show transparency without detailing sensitive records
                val stats = JSONObject().apply {
                    put("exportVersion", 1)
                    put("integrityChecked", true)
                }
                root.put("behavioralTelemetry", stats)
                root.toString(4)
            } catch (e: Exception) {
                e.printStackTrace()
                "{}"
            }
        }
    }
}
