package com.behavioralfirewall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.behavioralfirewall.data.local.entities.AccountabilityPartnerEntity
import com.behavioralfirewall.data.local.entities.InvitationEntity
import com.behavioralfirewall.domain.engine.AccountabilityManager
import com.behavioralfirewall.domain.engine.BehaviorSummaryGenerator
import com.behavioralfirewall.domain.engine.EmergencySupportManager
import com.behavioralfirewall.domain.engine.RecoverySupportEngine
import com.behavioralfirewall.domain.engine.StreakManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountabilityViewModel @Inject constructor(
    private val accountabilityManager: AccountabilityManager,
    private val emergencySupportManager: EmergencySupportManager,
    private val recoverySupportEngine: RecoverySupportEngine,
    private val streakManager: StreakManager,
    private val summaryGenerator: BehaviorSummaryGenerator
) : ViewModel() {

    val partners: StateFlow<List<AccountabilityPartnerEntity>> = accountabilityManager.partners
    val invitations: StateFlow<List<InvitationEntity>> = accountabilityManager.invitations
    val isEmergencyActive: StateFlow<Boolean> = emergencySupportManager.isEmergencyModeActive
    val isReflectionActive: StateFlow<Boolean> = recoverySupportEngine.isReflectionActive

    fun sendInvitation(email: String, relationshipType: String) {
        accountabilityManager.sendInvitation(email, relationshipType)
    }

    fun acceptInvitation(token: String) {
        accountabilityManager.acceptInvitation(token)
    }

    fun removePartner(partnerId: String) {
        accountabilityManager.removePartner(partnerId)
    }

    fun toggleNotifications(partnerId: String, enabled: Boolean) {
        accountabilityManager.toggleNotifications(partnerId, enabled)
    }

    fun activateEmergencyMode() {
        emergencySupportManager.activateEmergencyMode()
    }

    fun deactivateEmergencyMode() {
        emergencySupportManager.deactivateEmergencyMode()
    }

    fun alertPartners() {
        emergencySupportManager.alertPartners()
    }

    fun startRecoveryReflection() {
        recoverySupportEngine.startRecoveryReflection()
    }

    fun completeReflection(trigger: String, emotionalState: String, mitigationPlan: String) {
        recoverySupportEngine.completeReflection(trigger, emotionalState, mitigationPlan)
    }

    fun generateAnonymizedSummary(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val streakVal = streakManager.streak.value
            val summary = summaryGenerator.generateAnonymizedSummary(streakVal)
            onResult(summary)
        }
    }
}
