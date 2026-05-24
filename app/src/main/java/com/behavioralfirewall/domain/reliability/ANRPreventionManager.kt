package com.behavioralfirewall.domain.reliability

import com.behavioralfirewall.domain.security.SecureDiagnosticsLogger
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ANRPreventionManager @Inject constructor(
    private val logger: SecureDiagnosticsLogger
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Safely launches an IO coroutine, measuring latency to detect ANR conditions.
     */
    fun <T> executeSecureBackgroundTask(
        taskName: String,
        block: suspend () -> T
    ) {
        scope.launch(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                block()
            } catch (e: Exception) {
                logger.e("ANRPrevention", "Task $taskName failed: ", e)
            } finally {
                val duration = System.currentTimeMillis() - start
                if (duration > 2000L) {
                    logger.w("ANRPrevention", "WARNING: Task $taskName took $duration ms on Dispatchers.IO. Higher potential for ANR.")
                }
            }
        }
    }
}
