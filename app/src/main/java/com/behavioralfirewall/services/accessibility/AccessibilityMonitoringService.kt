package com.behavioralfirewall.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.behavioralfirewall.domain.engine.AppMonitoringManager
import com.behavioralfirewall.domain.engine.escalation.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityMonitoringService : AccessibilityService() {

    @Inject
    lateinit var appMonitoringManager: AppMonitoringManager

    @Inject
    lateinit var typingRiskDetector: TypingRiskDetector

    @Inject
    lateinit var playStoreMonitor: PlayStoreBehaviorMonitor

    @Inject
    lateinit var riskyAppDetector: RiskyAppDetector

    @Inject
    lateinit var sequenceEngine: EscalationSequenceEngine

    @Inject
    lateinit var intentRiskEngine: IntentRiskEngine

    @Inject
    lateinit var keywordRiskAnalyzer: KeywordRiskAnalyzer

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()

        // Collect detected keywords from the TypingRiskDetector flow
        serviceScope.launch {
            typingRiskDetector.detectedKeywords.collect { match ->
                sequenceEngine.addSignal(
                    SequenceSignal(
                        type = SequenceSignalType.KEYWORD_TYPED,
                        description = "Risky keyword typed: '${match.keyword}' (Confidence: ${String.format("%.2f", match.confidence)})"
                    )
                )
                intentRiskEngine.notifySignalReceived()
            }
        }

        // Collect signals from the Play Store Behavior Monitor
        serviceScope.launch {
            playStoreMonitor.signals.collect { signal ->
                when (signal.type) {
                    PlayStoreSignalType.OPENED -> {
                        sequenceEngine.addSignal(
                            SequenceSignal(
                                type = SequenceSignalType.PLAY_STORE_LAUNCHED,
                                description = "Play Store opened"
                            )
                        )
                        intentRiskEngine.notifySignalReceived()
                    }
                    PlayStoreSignalType.SEARCHED -> {
                        val matches = keywordRiskAnalyzer.analyze(signal.query)
                        if (matches.isNotEmpty()) {
                            sequenceEngine.addSignal(
                                SequenceSignal(
                                    type = SequenceSignalType.KEYWORD_TYPED,
                                    description = "Play Store search for: '${matches.first().keyword}'"
                                )
                            )
                            intentRiskEngine.notifySignalReceived()
                        }
                    }
                    PlayStoreSignalType.INSTALL_CLICKED -> {
                        sequenceEngine.addSignal(
                            SequenceSignal(
                                type = SequenceSignalType.KEYWORD_TYPED,
                                description = "Play Store install click triggered"
                            )
                        )
                        intentRiskEngine.notifySignalReceived()
                    }
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: ""

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (packageName.isEmpty()) return
                appMonitoringManager.emitActiveApp(packageName)

                // VPN and browser launching sequences
                if (riskyAppDetector.isVpnApp(packageName)) {
                    sequenceEngine.addSignal(
                        SequenceSignal(
                            type = SequenceSignalType.VPN_LAUNCHED,
                            description = "VPN App launch detected: $packageName"
                        )
                    )
                    intentRiskEngine.notifySignalReceived()
                } else if (riskyAppDetector.isRiskyBrowser(packageName)) {
                    sequenceEngine.addSignal(
                        SequenceSignal(
                            type = SequenceSignalType.BROWSER_LAUNCHED,
                            description = "Risky Browser launch detected: $packageName"
                        )
                    )
                    intentRiskEngine.notifySignalReceived()
                } else if (packageName == "com.android.vending") {
                    playStoreMonitor.onPlayStoreOpened()
                }
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val isPassword = event.isPassword
                val textList = event.text
                if (textList != null && textList.isNotEmpty()) {
                    val text = textList.joinToString(" ")
                    typingRiskDetector.onTextChanged(text, isPassword)

                    // Track searches within Play Store
                    if (packageName == "com.android.vending") {
                        playStoreMonitor.onPlayStoreSearch(text)
                    }
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if (packageName == "com.android.vending") {
                    val textList = event.text
                    if (textList != null && textList.isNotEmpty()) {
                        val text = textList.joinToString(" ").lowercase()
                        if (text.contains("install") || text.contains("download") || text.contains("get") || text.contains("update")) {
                            playStoreMonitor.onInstallClicked()
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
