package com.behavioralfirewall.domain.release

import com.behavioralfirewall.domain.security.SecureDiagnosticsLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinalPerformanceValidator @Inject constructor(
    private val logger: SecureDiagnosticsLogger
) {
    private var startupStartTime: Long = 0L

    fun recordAppLaunchStart() {
        startupStartTime = System.currentTimeMillis()
    }

    fun recordAppLaunchComplete() {
        if (startupStartTime == 0L) return
        val duration = System.currentTimeMillis() - startupStartTime
        logger.i("PerformanceValidator", "Production App Startup Completed. Latency: $duration ms")
        if (duration > 1500L) {
            logger.w("PerformanceValidator", "App Startup took longer than 1.5 seconds. Check DI and Database init latencies.")
        }
        startupStartTime = 0L
    }

    fun auditDatabaseLatency(operationName: String, durationMs: Long) {
        logger.i("PerformanceValidator", "Database IO operation $operationName latency: $durationMs ms")
        if (durationMs > 200L) {
            logger.w("PerformanceValidator", "WARNING: DB operation $operationName exceeded 200ms. Check indexing and query optimizations.")
        }
    }
}
