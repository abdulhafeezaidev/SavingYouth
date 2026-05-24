package com.behavioralfirewall.services.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehavioralNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "behavioral_reminders"

    // Anti-spam throttling limit
    private var lastNotificationTime = 0L
    private val minThrottlingIntervalMs = 600000L // 10 minutes minimum separation interval

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Recovery Support Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Motivational notifications and companion summaries."
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun dispatchNotification(title: String, message: String): Boolean {
        val now = System.currentTimeMillis()
        
        // Anti-spam guard
        if (now - lastNotificationTime < minThrottlingIntervalMs) {
            return false
        }
        
        lastNotificationTime = now

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        return true
    }
}
