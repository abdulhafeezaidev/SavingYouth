package com.behavioralfirewall.domain.launch

import android.util.Log
import java.util.regex.Pattern

interface CrashReporter {
    fun logCrash(throwable: Throwable, contextData: Map<String, String> = emptyMap())
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun setCustomKey(key: String, value: String)
}

object PrivacyRedactor {
    private val EMAIL_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    private val URL_PATTERN: Pattern = Pattern.compile(
        "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"
    )

    // Regex to match typical package names (e.g. com.example.app)
    private val PACKAGE_PATTERN: Pattern = Pattern.compile(
        "\\b[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)+\\b"
    )

    // A whitelist of safe system or app-specific package prefixes to keep for diagnostics
    private val SAFE_PACKAGES = listOf(
        "com.behavioralfirewall",
        "android.os",
        "android.app",
        "android.view",
        "androidx.compose",
        "kotlinx.coroutines"
    )

    /**
     * Redacts sensitive strings (emails, URLs, non-whitelisted package names).
     */
    fun redact(input: String): String {
        if (input.isEmpty()) return input
        var redacted = input

        // 1. Redact Emails
        redacted = EMAIL_PATTERN.matcher(redacted).replaceAll("[REDACTED_EMAIL]")

        // 2. Redact URLs
        redacted = URL_PATTERN.matcher(redacted).replaceAll("[REDACTED_URL]")

        // 3. Redact non-whitelisted Package Names
        val matcher = PACKAGE_PATTERN.matcher(redacted)
        val sb = StringBuffer()
        while (matcher.find()) {
            val pkg = matcher.group()
            val isSafe = SAFE_PACKAGES.any { pkg.startsWith(it) }
            if (isSafe) {
                matcher.appendReplacement(sb, pkg)
            } else {
                matcher.appendReplacement(sb, "[REDACTED_PKG]")
            }
        }
        matcher.appendTail(sb)
        redacted = sb.toString()

        return redacted
    }

    /**
     * Redacts sensitive fields within a context map.
     */
    fun redactMap(map: Map<String, Any>): Map<String, Any> {
        return map.mapValues { (_, value) ->
            if (value is String) redact(value) else value
        }
    }
}

/**
 * A standard development/local crash reporter that outputs safely redacted logs to logcat.
 */
class LogcatCrashReporter : CrashReporter {
    override fun logCrash(throwable: Throwable, contextData: Map<String, String>) {
        val redactedContext = contextData.mapValues { PrivacyRedactor.redact(it.value) }
        val redactedMsg = PrivacyRedactor.redact(throwable.localizedMessage ?: "Unknown Error")
        Log.e("BehavioralFirewallRC", "Crash Captured safely: $redactedMsg. Context: $redactedContext", throwable)
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val redactedParams = PrivacyRedactor.redactMap(params)
        Log.i("BehavioralFirewallRC", "Analytics Event: $eventName, Parameters: $redactedParams")
    }

    override fun setCustomKey(key: String, value: String) {
        Log.d("BehavioralFirewallRC", "Crash Custom Key: $key = ${PrivacyRedactor.redact(value)}")
    }
}
