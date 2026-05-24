package com.behavioralfirewall.domain.release

import com.behavioralfirewall.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReleaseSafetyManager @Inject constructor() {

    /**
     * Ensures that simulation engines, developer logs, and diagnostics
     * are completely disabled in release builds to enforce production safety.
     */
    fun isDeveloperDiagnosticsAllowed(): Boolean {
        // Only allow Diagnostics dashboard and Simulation stress tools in debug builds!
        return BuildConfig.DEBUG
    }

    /**
     * Returns true if crash telemetry and telemetry logs are active.
     */
    fun isProductionLoggingRequired(): Boolean {
        return !BuildConfig.DEBUG
    }
}
