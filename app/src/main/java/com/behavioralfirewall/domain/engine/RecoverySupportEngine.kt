package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.RiskEventEntity
import com.behavioralfirewall.domain.repository.BehavioralRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoverySupportEngine @Inject constructor(
    private val repository: BehavioralRepository,
    private val streakManager: StreakManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _isReflectionActive = MutableStateFlow(false)
    val isReflectionActive: StateFlow<Boolean> = _isReflectionActive.asStateFlow()

    fun startRecoveryReflection() {
        _isReflectionActive.value = true
    }

    fun completeReflection(
        triggerSource: String,
        emotionalState: String,
        mitigationPlan: String
    ) {
        _isReflectionActive.value = false
        
        scope.launch {
            // Save recovery planning as a positive compiled outcome in database
            val log = RiskEventEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                riskScore = 0,
                triggerType = "recovery_reflection",
                interventionType = "REFLECTION_PROMPT",
                outcome = "complied"
            )
            repository.saveRiskEvent(log)
            
            // Restart recovery plan and freeze streak variables to start a clean daily run
            streakManager.freezeStreak()
        }
    }
}
