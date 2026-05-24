package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.StreakEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakManager @Inject constructor(
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _streak = MutableStateFlow<StreakEntity?>(null)
    val streak: StateFlow<StreakEntity?> = _streak.asStateFlow()

    init {
        loadStreak()
        observeRiskEvents()
    }

    private fun loadStreak() {
        scope.launch {
            repository.getStreak().collect { streakEntity ->
                if (streakEntity == null) {
                    val newStreak = StreakEntity(
                        streakStartDate = System.currentTimeMillis(),
                        currentDays = 1,
                        longestDays = 1,
                        lastRelapseAt = 0L,
                        recoveryScore = 100,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.saveStreak(newStreak)
                } else {
                    _streak.value = streakEntity
                    updateStreakDaysIfNeeded(streakEntity)
                }
            }
        }
    }

    private fun updateStreakDaysIfNeeded(current: StreakEntity) {
        val today = Calendar.getInstance()
        val lastUpdate = Calendar.getInstance().apply { timeInMillis = current.lastUpdated }
        
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        lastUpdate.set(Calendar.HOUR_OF_DAY, 0)
        lastUpdate.set(Calendar.MINUTE, 0)
        lastUpdate.set(Calendar.SECOND, 0)
        lastUpdate.set(Calendar.MILLISECOND, 0)

        val diffDays = ((today.timeInMillis - lastUpdate.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        
        if (diffDays > 0) {
            val newDays = current.currentDays + diffDays
            val newLongest = if (newDays > current.longestDays) newDays else current.longestDays
            scope.launch {
                repository.saveStreak(
                    current.copy(
                        currentDays = newDays,
                        longestDays = newLongest,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun observeRiskEvents() {
        scope.launch {
            repository.getRiskEvents().collect { events ->
                if (events.isEmpty()) return@collect
                val latest = events.first()
                
                // Relapse Rule: Accidental openings do NOT reset streaks.
                // Only meaningful relapses count: e.g. User deliberately dismissed an intervention with riskScore >= 80 (CRITICAL/HIGH)
                if (latest.outcome == "dismissed" && latest.riskScore >= 80) {
                    val now = System.currentTimeMillis()
                    val currentStreak = _streak.value
                    
                    if (currentStreak != null && now - currentStreak.lastRelapseAt > 60000) { // 1 min debounce throttle
                        val resetStreak = currentStreak.copy(
                            streakStartDate = now,
                            currentDays = 0,
                            lastRelapseAt = now,
                            lastUpdated = now
                        )
                        repository.saveStreak(resetStreak)
                    }
                }
            }
        }
    }

    fun freezeStreak() {
        scope.launch {
            val current = _streak.value ?: return@launch
            // Freeze logic pushes the last updated timestamp to simulate daily compliance
            repository.saveStreak(
                current.copy(
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }
}
