package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.RiskEventEntity
import com.behavioralfirewall.data.local.entities.AppUsageEntity
import com.behavioralfirewall.data.local.entities.TriggerSequenceEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorPatternAnalyzer @Inject constructor(
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _patterns = MutableStateFlow<String>("")
    val patterns: StateFlow<String> = _patterns.asStateFlow()

    init {
        analyzePatterns()
    }

    fun analyzePatterns() {
        scope.launch {
            val events = repository.getRiskEvents().firstOrNull() ?: return@launch
            val usages = repository.getAppUsage().firstOrNull() ?: return@launch

            if (events.isEmpty()) return@launch

            // 1. Detect Risky Hours
            val peakHour = events.groupBy { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                cal.get(Calendar.HOUR_OF_DAY)
            }.maxByOrNull { it.value.size }?.key ?: -1

            // 2. Weekend vulnerability check
            val weekendTriggers = events.count { 
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                val day = cal.get(Calendar.DAY_OF_WEEK)
                day == Calendar.SATURDAY || day == Calendar.SUNDAY
            }
            val weekendReport = if (weekendTriggers > events.size / 2) {
                "Weekend vulnerability detected."
            } else {
                "Normal weekend patterns."
            }

            // 3. Trace sequences
            detectAndLogSequences(events, usages)

            _patterns.value = buildString {
                append("Peak risk hour: ${if (peakHour != -1) "$peakHour:00" else "N/A"}. ")
                append(weekendReport)
            }
        }
    }

    private suspend fun detectAndLogSequences(events: List<RiskEventEntity>, usages: List<AppUsageEntity>) {
        if (events.isEmpty() || usages.isEmpty()) return

        val sortedUsages = usages.sortedBy { it.timestamp }.takeLast(10)
        val sortedEvents = events.sortedBy { it.timestamp }.takeLast(5)

        val browserLaunches = sortedUsages.filter { 
            it.packageName.contains("chrome") || 
            it.packageName.contains("firefox") || 
            it.packageName.contains("browser") 
        }
        val playStoreLaunches = sortedUsages.filter { it.packageName.contains("vending") }

        // Sequential Check: Play Store Openings strongly predict Browser Installs & switching under High Risk
        if (playStoreLaunches.isNotEmpty() && browserLaunches.isNotEmpty() && sortedEvents.any { it.riskScore >= 70 }) {
            val chain = "Play Store -> Browser Launch -> High Risk Escalation"
            val id = UUID.nameUUIDFromBytes(chain.toByteArray()).toString()
            
            val existing = repository.getTriggerSequences().firstOrNull()?.find { it.id == id }
            if (existing != null) {
                repository.saveTriggerSequence(
                    existing.copy(
                        occurrenceCount = existing.occurrenceCount + 1,
                        lastOccurredAt = System.currentTimeMillis()
                    )
                )
            } else {
                repository.saveTriggerSequence(
                    TriggerSequenceEntity(
                        id = id,
                        sequenceChain = chain,
                        occurrenceCount = 1,
                        lastOccurredAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
