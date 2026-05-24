package com.behavioralfirewall.domain.evolution

import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FalsePositiveReducer @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) {
    private val whitelistKey = "fp_domain_whitelist_v1"
    private val safeSessionsCountKey = "fp_safe_sessions_v1"

    private val _sensitivityDiscount = MutableStateFlow(1.0f) // No discount initially (1.0x)
    val sensitivityDiscount: StateFlow<Float> = _sensitivityDiscount.asStateFlow()

    private val _safeSessionsCount = MutableStateFlow(0)
    val safeSessionsCount: StateFlow<Int> = _safeSessionsCount.asStateFlow()

    private val whitelistedDomains = mutableSetOf<String>()

    init {
        loadWhitelist()
        loadSafeSessions()
    }

    private fun loadWhitelist() {
        val raw = secureStorageManager.getToken(whitelistKey) ?: ""
        if (raw.isNotEmpty()) {
            whitelistedDomains.addAll(raw.split(","))
        }
    }

    private fun loadSafeSessions() {
        val count = secureStorageManager.getToken(safeSessionsCountKey)?.toIntOrNull() ?: 0
        _safeSessionsCount.value = count
        recalculateSensitivityDiscount(count)
    }

    /**
     * Increment stable usage sessions without interventions.
     * Gradually reduces model sensitivity to ensure low-risk users experience minimal friction.
     */
    fun incrementSafeSession() {
        val count = _safeSessionsCount.value + 1
        _safeSessionsCount.value = count
        secureStorageManager.saveToken(safeSessionsCountKey, count.toString())
        recalculateSensitivityDiscount(count)
    }

    private fun recalculateSensitivityDiscount(safeSessions: Int) {
        // Reduce sensitivity by 2% per safe session, up to a maximum discount of 30% (0.7x multiplier)
        val discount = 1.0f - (safeSessions * 0.02f).coerceAtMost(0.3f)
        _sensitivityDiscount.value = discount
    }

    /**
     * Reports a domain was whitelisted by the user to prevent duplicate blocks.
     */
    fun addDomainToWhitelist(domain: String) {
        whitelistedDomains.add(domain)
        secureStorageManager.saveToken(whitelistKey, whitelistedDomains.joinToString(","))
    }

    /**
     * Checks if a domain is whitelisted by the user or safe classification.
     */
    fun isDomainWhitelisted(domain: String): Boolean {
        return whitelistedDomains.contains(domain)
    }

    /**
     * Resets the user's safe sessions and sensitivity multiplier upon high risk triggers.
     */
    fun resetSafeSessions() {
        _safeSessionsCount.value = 0
        secureStorageManager.saveToken(safeSessionsCountKey, "0")
        _sensitivityDiscount.value = 1.0f
    }
}
