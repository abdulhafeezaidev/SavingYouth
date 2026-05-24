package com.behavioralfirewall.domain.engine.escalation

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskyAppDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Configurable list of VPN app package names
    private val vpnPackages = mutableSetOf(
        "com.nordvpn.android",
        "com.expressvpn.vpn",
        "com.surfshark.vpnclient",
        "com.protonvpn.android",
        "co.windscribe.windscribe",
        "com.tunnelbear.android",
        "com.cloudflare.onedotonedotonedotone",
        "com.vpngate.client",
        "com.strongvpn",
        "com.cyberghost.vpn",
        "com.hotspotshield.android"
    )

    // Configurable list of risky browsers (including incognito-first or secondary browsers)
    private val riskyBrowsers = mutableSetOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.opera.browser",
        "com.UCMobile.intl",
        "com.microsoft.emmx",
        "com.duckduckgo.mobile.android",
        "com.yandex.browser",
        "com.sec.android.app.sbrowser"
    )

    private val installedVpns = mutableSetOf<String>()
    private val installedBrowsers = mutableSetOf<String>()

    init {
        scanInstalledApps()
    }

    /**
     * Scans currently installed packages to identify active VPNs or browsers.
     */
    fun scanInstalledApps() {
        val pm = context.packageManager
        try {
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            synchronized(this) {
                installedVpns.clear()
                installedBrowsers.clear()
                for (pkg in packages) {
                    val pkgName = pkg.packageName
                    if (vpnPackages.contains(pkgName)) {
                        installedVpns.add(pkgName)
                    }
                    if (riskyBrowsers.contains(pkgName)) {
                        installedBrowsers.add(pkgName)
                    }
                }
            }
        } catch (e: Exception) {
            // Safety fallback for package querying constraints on modern Android
            e.printStackTrace()
        }
    }

    fun isVpnApp(packageName: String): Boolean {
        return vpnPackages.contains(packageName)
    }

    fun isRiskyBrowser(packageName: String): Boolean {
        return riskyBrowsers.contains(packageName)
    }

    fun getInstalledVpns(): Set<String> {
        synchronized(this) {
            return installedVpns.toSet()
        }
    }

    fun getInstalledBrowsers(): Set<String> {
        synchronized(this) {
            return installedBrowsers.toSet()
        }
    }

    fun addVpnPackage(packageName: String) {
        vpnPackages.add(packageName)
        scanInstalledApps()
    }

    fun addBrowserPackage(packageName: String) {
        riskyBrowsers.add(packageName)
        scanInstalledApps()
    }
}
