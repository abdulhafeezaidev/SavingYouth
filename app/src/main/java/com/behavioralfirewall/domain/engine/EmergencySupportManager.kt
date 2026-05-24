package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.EmergencyEventEntity
import com.behavioralfirewall.domain.model.InterventionType
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
class EmergencySupportManager @Inject constructor(
    private val repository: BehavioralRepository,
    private val interventionManager: InterventionManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _isEmergencyModeActive = MutableStateFlow(false)
    val isEmergencyModeActive: StateFlow<Boolean> = _isEmergencyModeActive.asStateFlow()

    fun activateEmergencyMode() {
        _isEmergencyModeActive.value = true
        
        scope.launch {
            val event = EmergencyEventEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                actionTaken = "LOCKOUT_ACTIVATED"
            )
            repository.saveEmergencyEvent(event)
            
            // Direct launch of critical box-breathing intervention
            interventionManager.triggerIntervention(
                type = InterventionType.BREATHING,
                triggerPackage = "com.emergency.lockout",
                riskScore = 100
            )
        }
    }

    fun deactivateEmergencyMode() {
        _isEmergencyModeActive.value = false
    }

    fun alertPartners() {
        scope.launch {
            val event = EmergencyEventEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                actionTaken = "PARTNER_ALERTED"
            )
            repository.saveEmergencyEvent(event)
        }
    }
}
