package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.AppUsageEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoringOptimizationManager @Inject constructor(
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Debounce window in milliseconds
    private val debounceDelayMs = 300L
    
    // Batch queue for app usage events to write to the database
    private val eventQueue = mutableListOf<AppUsageEntity>()
    private val queueMutex = Mutex()
    private val batchWriteIntervalMs = 5000L // Write every 5 seconds
    
    // Flow of debounced package names
    private val _optimizedAppFlow = MutableSharedFlow<String>(replay = 1)
    val optimizedAppFlow: Flow<String> = _optimizedAppFlow.asSharedFlow().distinctUntilChanged()
    
    private var lastEmittedApp = ""
    private var debounceJob: Job? = null
    
    init {
        startBatchProcessor()
    }
    
    /**
     * Process a raw app-switch event with debouncing to filter out transient/system overlays.
     */
    fun processAppSwitch(packageName: String) {
        if (packageName == lastEmittedApp || packageName.isBlank()) return
        
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceDelayMs)
            lastEmittedApp = packageName
            _optimizedAppFlow.emit(packageName)
            
            // Queue an AppUsageEvent in memory instead of writing directly to DB
            queueAppUsage(packageName)
        }
    }
    
    private suspend fun queueAppUsage(packageName: String) {
        queueMutex.withLock {
            eventQueue.add(
                AppUsageEntity(
                    id = UUID.randomUUID().toString(),
                    packageName = packageName,
                    timestamp = System.currentTimeMillis(),
                    duration = 0L // Updated on switch or aggregated later
                )
            )
        }
    }
    
    private fun startBatchProcessor() {
        scope.launch {
            while (isActive) {
                delay(batchWriteIntervalMs)
                flushEvents()
            }
        }
    }
    
    suspend fun flushEvents() {
        val eventsToWrite = queueMutex.withLock {
            if (eventQueue.isEmpty()) return@withLock emptyList<AppUsageEntity>()
            val copy = ArrayList(eventQueue)
            eventQueue.clear()
            copy
        }
        
        if (eventsToWrite.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    eventsToWrite.forEach { event ->
                        repository.saveAppUsage(event)
                    }
                } catch (e: Exception) {
                    // Fallback to avoid losing events: put back in queue
                    queueMutex.withLock {
                        eventQueue.addAll(0, eventsToWrite)
                    }
                }
            }
        }
    }
    
    fun cancel() {
        scope.cancel()
    }
}
