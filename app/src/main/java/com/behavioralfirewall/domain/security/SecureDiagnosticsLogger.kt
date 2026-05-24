package com.behavioralfirewall.domain.security

import android.util.Log
import com.behavioralfirewall.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureDiagnosticsLogger @Inject constructor() {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, redact(message))
        }
    }

    fun i(tag: String, message: String) {
        Log.i(tag, redact(message))
    }

    fun w(tag: String, message: String) {
        Log.w(tag, redact(message))
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, redact(message), throwable)
    }

    private fun redact(message: String): String {
        var redacted = message
        
        // 1. Redact Emails
        val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        redacted = redacted.replace(emailPattern, "[REDACTED_EMAIL]")

        // 2. Redact URLs
        val urlPattern = "(https?://)?(www\\.)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/[a-zA-Z0-9-._~:/?#\\[\\]@!$&'()*+,;=]*)?".toRegex()
        redacted = redacted.replace(urlPattern, "[REDACTED_URL]")

        return redacted
    }
}
