package com.behavioralfirewall.domain.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResilienceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureStorageManager: SecureStorageManager
) {
    private val _repairNeeded = MutableStateFlow(false)
    val repairNeeded = _repairNeeded.asStateFlow()

    init {
        checkAbnormalShutdown()
    }

    private fun checkAbnormalShutdown() {
        try {
            val lastActiveTime = secureStorageManager.getToken("last_active_time")?.toLongOrNull() ?: 0L
            val cleanShutdown = secureStorageManager.getBooleanSetting("clean_shutdown", true)
            val currentTime = System.currentTimeMillis()

            // If the app was not closed cleanly and the last heartbeat was within the last 5 minutes,
            // increment the unexpected termination count to alert the user of possible bypassing.
            if (!cleanShutdown && lastActiveTime > 0L && (currentTime - lastActiveTime) < 300000L) {
                val crashCount = secureStorageManager.getToken("crash_count")?.toIntOrNull() ?: 0
                val newCrashCount = crashCount + 1
                secureStorageManager.saveToken("crash_count", newCrashCount.toString())
                if (newCrashCount >= 3) {
                    _repairNeeded.value = true
                }
            } else {
                secureStorageManager.saveToken("crash_count", "0")
            }

            // Reset dirty shutdown state for the current active session
            secureStorageManager.saveBooleanSetting("clean_shutdown", false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun recordHeartbeat() {
        secureStorageManager.saveToken("last_active_time", System.currentTimeMillis().toString())
        secureStorageManager.saveBooleanSetting("clean_shutdown", false)
    }

    fun recordCleanTeardown() {
        secureStorageManager.saveBooleanSetting("clean_shutdown", true)
    }

    fun resetCrashes() {
        secureStorageManager.saveToken("crash_count", "0")
        _repairNeeded.value = false
    }
}
