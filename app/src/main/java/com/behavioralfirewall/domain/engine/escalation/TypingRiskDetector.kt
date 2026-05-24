package com.behavioralfirewall.domain.engine.escalation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class TypingRiskDetector @Inject constructor(
    private val keywordRiskAnalyzer: KeywordRiskAnalyzer
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val textInputFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)

    private val _detectedKeywords = MutableSharedFlow<KeywordMatch>(extraBufferCapacity = 32)
    val detectedKeywords: SharedFlow<KeywordMatch> = _detectedKeywords.asSharedFlow()

    init {
        scope.launch {
            textInputFlow
                .debounce(800L) // Wait for 800ms of typing inactivity to save CPU/battery
                .distinctUntilChanged()
                .collect { text ->
                    val matches = keywordRiskAnalyzer.analyze(text)
                    for (match in matches) {
                        _detectedKeywords.emit(match)
                    }
                }
        }
    }

    /**
     * Receives keystrokes or text entry events. Ignores secure password fields automatically.
     */
    fun onTextChanged(text: String, isPassword: Boolean) {
        if (isPassword) return // Cryptographically secure/sensitive check - skip immediately

        val sanitized = text.trim()
        if (sanitized.isEmpty()) return

        textInputFlow.tryEmit(sanitized)
    }
}
