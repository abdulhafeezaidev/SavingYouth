package com.behavioralfirewall.domain.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCooldownManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _cooldownActive = MutableStateFlow(false)
    val cooldownActive: StateFlow<Boolean> = _cooldownActive.asStateFlow()

    private val _cooldownTimeRemaining = MutableStateFlow(0L)
    val cooldownTimeRemaining: StateFlow<Long> = _cooldownTimeRemaining.asStateFlow()

    private var cooldownJob: Job? = null

    fun triggerAppCooldown(durationSeconds: Int = 300) { // Default 5 minutes re-entry block
        _cooldownActive.value = true
        cooldownJob?.cancel()
        cooldownJob = scope.launch {
            var remaining = durationSeconds.toLong() * 1000
            while (remaining > 0) {
                _cooldownTimeRemaining.value = remaining
                delay(1000L)
                remaining -= 1000
            }
            _cooldownTimeRemaining.value = 0L
            _cooldownActive.value = false
        }
    }

    fun isAppCooldownActive(): Boolean = _cooldownActive.value
}
