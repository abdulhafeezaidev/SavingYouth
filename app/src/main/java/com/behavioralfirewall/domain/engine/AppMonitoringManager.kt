package com.behavioralfirewall.domain.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AppMonitoringManager {
    val activeAppFlow: Flow<String>
    fun emitActiveApp(packageName: String)
    fun getActiveAppDuration(): Long
}

@Singleton
class AppMonitoringManagerImpl @Inject constructor() : AppMonitoringManager {
    private val _activeAppFlow = MutableStateFlow("")
    override val activeAppFlow: Flow<String> = _activeAppFlow

    private var appStartTime: Long = 0L

    override fun emitActiveApp(packageName: String) {
        if (_activeAppFlow.value != packageName) {
            _activeAppFlow.value = packageName
            appStartTime = System.currentTimeMillis()
        }
    }

    override fun getActiveAppDuration(): Long {
        if (appStartTime == 0L || _activeAppFlow.value.isEmpty()) return 0L
        return System.currentTimeMillis() - appStartTime
    }
}
