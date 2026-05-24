package com.behavioralfirewall.core.constants

object AppConstants {
    val KNOWN_BROWSERS = listOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.opera.browser",
        "com.UCMobile.intl",
        "com.microsoft.emmx",
        "com.duckduckgo.mobile.android"
    )

    const val PLAY_STORE_PACKAGE = "com.android.vending"

    // Risk Scoring Configuration
    const val LATE_NIGHT_START_HOUR = 23
    const val LATE_NIGHT_END_HOUR = 5
    
    const val SCORE_LATE_NIGHT = 20
    const val SCORE_PLAY_STORE = 25
    const val SCORE_LONG_BROWSER_SESSION = 15
    const val SCORE_REPETITIVE_SWITCHING = 10
    const val SCORE_HIGH_STRESS = 15

    const val THRESHOLD_LONG_BROWSER_SESSION_MS = 20 * 60 * 1000L // 20 minutes
}
