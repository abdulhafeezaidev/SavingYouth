package com.behavioralfirewall.services.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.behavioralfirewall.domain.engine.RiskStateManager
import com.behavioralfirewall.domain.security.SecureStorageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundMonitoringService : Service() {

    @Inject
    lateinit var riskStateManager: RiskStateManager

    @Inject
    lateinit var secureStorageManager: SecureStorageManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        riskStateManager.startMonitoring(scope)
        try {
            val overlayIntent = Intent(this, com.behavioralfirewall.services.overlay.OverlayInterventionService::class.java)
            startService(overlayIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        riskStateManager.stopMonitoring()
        try {
            val overlayIntent = Intent(this, com.behavioralfirewall.services.overlay.OverlayInterventionService::class.java)
            stopService(overlayIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        job.cancel()

        // Hardening: Schedule an automatic restart fallback via AlarmManager
        val restartIntent = Intent(applicationContext, ForegroundMonitoringService::class.java)
        val pendingIntent = android.app.PendingIntent.getService(
            this, 1, restartIntent,
            android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.set(
            android.app.AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 5000,
            pendingIntent
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "monitoring_channel",
            "Behavioral Monitoring",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val discreetEnabled = secureStorageManager.getBooleanSetting("discreet_notifications", true)
        
        val title = if (discreetEnabled) "Wellbeing Monitor Active" else "Behavioral Firewall Active"
        val text = if (discreetEnabled) "Your wellbeing checks are running." else "Monitoring risk indicators."

        return NotificationCompat.Builder(this, "monitoring_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_secure)
            .build()
    }
}
