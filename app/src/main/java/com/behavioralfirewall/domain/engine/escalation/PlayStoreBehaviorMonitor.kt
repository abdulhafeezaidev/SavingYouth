package com.behavioralfirewall.domain.engine.escalation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlayStoreSignal(
    val type: PlayStoreSignalType,
    val query: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class PlayStoreSignalType {
    OPENED, SEARCHED, INSTALL_CLICKED
}

@Singleton
class PlayStoreBehaviorMonitor @Inject constructor() {
    private val _signals = MutableSharedFlow<PlayStoreSignal>(extraBufferCapacity = 32)
    val signals: SharedFlow<PlayStoreSignal> = _signals.asSharedFlow()

    private var playStoreOpenTime = 0L

    /**
     * Reports when the user navigates into the Google Play Store app.
     */
    fun onPlayStoreOpened() {
        playStoreOpenTime = System.currentTimeMillis()
        _signals.tryEmit(PlayStoreSignal(PlayStoreSignalType.OPENED))
    }

    /**
     * Reports when the user searches for a tool or app inside the Play Store.
     */
    fun onPlayStoreSearch(query: String) {
        if (query.isBlank()) return
        _signals.tryEmit(PlayStoreSignal(PlayStoreSignalType.SEARCHED, query))
    }

    /**
     * Reports when the user initiates an installation process inside the Play Store.
     */
    fun onInstallClicked() {
        _signals.tryEmit(PlayStoreSignal(PlayStoreSignalType.INSTALL_CLICKED))
    }

    /**
     * Computes the current active duration spent inside the Play Store.
     */
    fun getSessionDurationMs(): Long {
        if (playStoreOpenTime == 0L) return 0L
        return System.currentTimeMillis() - playStoreOpenTime
    }
}
