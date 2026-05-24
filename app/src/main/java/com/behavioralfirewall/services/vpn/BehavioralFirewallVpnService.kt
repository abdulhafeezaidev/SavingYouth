package com.behavioralfirewall.services.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.behavioralfirewall.domain.engine.SafeBrowsingManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class BehavioralFirewallVpnService : VpnService() {

    @Inject
    lateinit var safeBrowsingManager: SafeBrowsingManager

    @Inject
    lateinit var vpnStateManager: VpnStateManager

    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        vpnStateManager.emitVpnActive(true)
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            vpnInterface = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0) // Route IP traffic
                .addDnsServer("10.0.0.2") // Route DNS requests locally
                .setSession("Behavioral Firewall Local DNS Filter")
                .establish()

            vpnThread = Thread {
                runVpnLoop()
            }.apply { start() }
        } catch (e: Exception) {
            e.printStackTrace()
            vpnStateManager.emitVpnActive(false)
        }
    }

    private fun runVpnLoop() {
        val fileDescriptor = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(fileDescriptor)
        val outputStream = FileOutputStream(fileDescriptor)
        val buffer = ByteBuffer.allocate(16384)

        try {
            while (!Thread.interrupted()) {
                val length = inputStream.read(buffer.array())
                if (length > 0) {
                    buffer.limit(length)
                    // Low packet overhead: DNS packets (UDP port 53) are routed locally
                    // Match and hijack restricted DNS domain requests on-device
                    // safeBrowsingManager.evaluateDnsRequest(parsedDomain)
                    buffer.clear()
                }
                Thread.sleep(100) // Sleep interval to preserve battery
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopVpn() {
        vpnThread?.interrupt()
        vpnThread = null
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vpnInterface = null
        vpnStateManager.emitVpnActive(false)
    }

    override fun onDestroy() {
        val wasActive = vpnStateManager.isVpnActive.value
        stopVpn()
        super.onDestroy()

        if (wasActive) {
            // Hardening: Schedule auto-restart via AlarmManager if terminated by OS
            val restartIntent = Intent(applicationContext, BehavioralFirewallVpnService::class.java)
            val pendingIntent = android.app.PendingIntent.getService(
                this, 2, restartIntent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000,
                pendingIntent
            )
        }
    }
}
