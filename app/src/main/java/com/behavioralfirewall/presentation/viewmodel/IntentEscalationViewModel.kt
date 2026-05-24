package com.behavioralfirewall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behavioralfirewall.domain.engine.escalation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntentEscalationViewModel @Inject constructor(
    private val typingRiskDetector: TypingRiskDetector,
    private val playStoreMonitor: PlayStoreBehaviorMonitor,
    private val riskyAppDetector: RiskyAppDetector,
    private val sequenceEngine: EscalationSequenceEngine,
    private val riskEngine: IntentRiskEngine,
    private val falsePositiveReducer: FalsePositiveReducer
) : ViewModel() {

    val riskScore = riskEngine.riskScore
    val escalationState = sequenceEngine.escalationState

    private val _activeSignals = MutableStateFlow<List<SequenceSignal>>(emptyList())
    val activeSignals: StateFlow<List<SequenceSignal>> = _activeSignals.asStateFlow()

    private val _falsePositiveEvents = MutableStateFlow<List<String>>(emptyList())
    val falsePositiveEvents: StateFlow<List<String>> = _falsePositiveEvents.asStateFlow()

    private val _keywordMatchHistory = MutableStateFlow<List<String>>(emptyList())
    val keywordMatchHistory: StateFlow<List<String>> = _keywordMatchHistory.asStateFlow()

    init {
        viewModelScope.launch {
            sequenceEngine.escalationState.collect {
                _activeSignals.value = sequenceEngine.getActiveSignals()
            }
        }

        viewModelScope.launch {
            typingRiskDetector.detectedKeywords.collect { match ->
                val record = "Keyword matched: [Redacted] (Fuzzy Match: '${match.matchedWord}', Base: +${match.baseScore}, Conf: ${String.format("%.1f", match.confidence)})"
                _keywordMatchHistory.value = listOf(record) + _keywordMatchHistory.value.take(19)
            }
        }
    }

    fun simulateTyping(text: String) {
        val isTechnical = falsePositiveReducer.evaluateFalsePositive(text, "", false)
        if (isTechnical) {
            _falsePositiveEvents.value = listOf("Safe Developer Term: '$text' (suppressed and ignored)") + _falsePositiveEvents.value.take(19)
        }
        typingRiskDetector.onTextChanged(text, false)
        refreshSignals()
    }

    fun simulatePlayStoreLateNight() {
        sequenceEngine.addSignal(
            SequenceSignal(
                type = SequenceSignalType.PLAY_STORE_LAUNCHED,
                description = "Simulated Play Store opened (Late Night context)"
            )
        )
        riskEngine.notifySignalReceived()
        refreshSignals()
    }

    fun simulateVpnLaunch() {
        sequenceEngine.addSignal(
            SequenceSignal(
                type = SequenceSignalType.VPN_LAUNCHED,
                description = "Simulated VPN App launch"
            )
        )
        riskEngine.notifySignalReceived()
        refreshSignals()
    }

    fun simulateBrowserLaunch() {
        sequenceEngine.addSignal(
            SequenceSignal(
                type = SequenceSignalType.BROWSER_LAUNCHED,
                description = "Simulated Browser session launch"
            )
        )
        riskEngine.notifySignalReceived()
        refreshSignals()
    }

    fun triggerDecay() {
        riskEngine.simulateDecay()
        refreshSignals()
    }

    fun clearAll() {
        sequenceEngine.clearSignals()
        _falsePositiveEvents.value = emptyList()
        _keywordMatchHistory.value = emptyList()
        refreshSignals()
    }

    private fun refreshSignals() {
        _activeSignals.value = sequenceEngine.getActiveSignals()
    }
}
