package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveRiskManager @Inject constructor(
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Dynamically updated weights accessed by the Risk Engine
    var lateNightMultiplier = 1.0f
    var switchingMultiplier = 1.0f
    var playStoreMultiplier = 1.0f

    init {
        loadAdaptations()
    }

    private fun loadAdaptations() {
        scope.launch {
            repository.getRiskEvents().collect { events ->
                if (events.isEmpty()) return@collect
                
                // If user repeatedly relapses after midnight (>= 2 relapses):
                // -> automatically escalate late-night multiplier
                val lateNightRelapses = events.count { 
                    it.outcome == "dismissed" && isLateNightTime(it.timestamp) 
                }
                if (lateNightRelapses >= 2) {
                    lateNightMultiplier = 1.5f
                }

                // If user triggers switching blocks frequently:
                // -> increase browser switching multiplier
                val browserSwitches = events.count {
                    it.triggerType.contains("switch") || 
                    it.interventionType == "HARD_INTERRUPT"
                }
                if (browserSwitches >= 3) {
                    switchingMultiplier = 1.4f
                }
            }
        }
    }

    private fun isLateNightTime(timestamp: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour >= 23 || hour < 5
    }
}
