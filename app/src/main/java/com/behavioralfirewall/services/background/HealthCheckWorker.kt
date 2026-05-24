package com.behavioralfirewall.services.background

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.behavioralfirewall.domain.engine.DiagnosticsManager
import com.behavioralfirewall.services.foreground.ForegroundMonitoringService
import com.behavioralfirewall.services.vpn.BehavioralFirewallVpnService
import com.behavioralfirewall.services.vpn.VpnStateManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HealthCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val diagnosticsManager: DiagnosticsManager,
    private val vpnStateManager: VpnStateManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Check/Restart ForegroundMonitoringService
            val monitoringIntent = Intent(context, ForegroundMonitoringService::class.java)
            context.startForegroundService(monitoringIntent)
            
            // 2. Check/Restart VPN Service if vpnStateManager was active
            val vpnIsActive = vpnStateManager.isVpnActive.value
            if (vpnIsActive) {
                val vpnIntent = Intent(context, BehavioralFirewallVpnService::class.java)
                context.startService(vpnIntent)
            }
            
            // 3. Update Service Health in DiagnosticsManager
            diagnosticsManager.updateServiceHealth(
                accessibilityActive = true,
                vpnActive = vpnIsActive,
                overlayActive = true
            )
            
            diagnosticsManager.logMemoryUsage()
            
            Result.success()
        } catch (e: Exception) {
            diagnosticsManager.logCrashRecovered("HealthCheckWorker", e.localizedMessage ?: "Unknown error")
            Result.retry()
        }
    }
}
