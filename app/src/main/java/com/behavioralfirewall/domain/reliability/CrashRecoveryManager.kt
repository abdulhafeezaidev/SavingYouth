package com.behavioralfirewall.domain.reliability

import android.content.Context
import com.behavioralfirewall.domain.security.SecureStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashRecoveryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorageManager: SecureStorageManager
) {
    private val _lastCrashReason = MutableStateFlow<String?>(null)
    val lastCrashReason = _lastCrashReason.asStateFlow()

    init {
        detectCrashState()
    }

    private fun detectCrashState() {
        try {
            val cleanShutdown = secureStorageManager.getBooleanSetting("clean_shutdown", true)
            if (!cleanShutdown) {
                val reason = secureStorageManager.getToken("crash_reason") ?: "Unexpected OS Terminate / Process Death"
                _lastCrashReason.value = reason
                secureStorageManager.saveToken("crash_reason", "None")
            }
            // Set dirty session state for current launch
            secureStorageManager.saveBooleanSetting("clean_shutdown", false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logCrash(reason: String) {
        secureStorageManager.saveToken("crash_reason", reason)
        secureStorageManager.saveBooleanSetting("clean_shutdown", false)
    }

    fun registerCleanShutdown() {
        secureStorageManager.saveBooleanSetting("clean_shutdown", true)
    }

    /**
     * Prevents duplicate service runs by tracking startup markers with timestamps.
     */
    fun isDuplicateServiceStart(serviceName: String): Boolean {
        val lastStart = secureStorageManager.getToken("${serviceName}_start_time")?.toLongOrNull() ?: 0L
        val currentTime = System.currentTimeMillis()
        
        // If service started less than 3 seconds ago, treat it as a duplicate launch request
        if (currentTime - lastStart < 3000L) {
            return true
        }
        secureStorageManager.saveToken("${serviceName}_start_time", currentTime.toString())
        return false
    }
}
