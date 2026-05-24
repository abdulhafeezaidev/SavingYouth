package com.behavioralfirewall.domain.engine

import com.behavioralfirewall.data.local.entities.AccountabilityPartnerEntity
import com.behavioralfirewall.data.local.entities.InvitationEntity
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
class AccountabilityManager @Inject constructor(
    private val repository: BehavioralRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _partners = MutableStateFlow<List<AccountabilityPartnerEntity>>(emptyList())
    val partners: StateFlow<List<AccountabilityPartnerEntity>> = _partners.asStateFlow()

    private val _invitations = MutableStateFlow<List<InvitationEntity>>(emptyList())
    val invitations: StateFlow<List<InvitationEntity>> = _invitations.asStateFlow()

    init {
        loadPartnersAndInvitations()
    }

    private fun loadPartnersAndInvitations() {
        scope.launch {
            repository.getPartners().collect {
                _partners.value = it
            }
        }
        scope.launch {
            repository.getInvitations().collect {
                _invitations.value = it
            }
        }
    }

    fun sendInvitation(email: String, relationshipType: String) {
        scope.launch {
            val token = UUID.randomUUID().toString().take(6).uppercase() // 6 digit token-based secure pairing
            val newInvite = InvitationEntity(
                id = UUID.randomUUID().toString(),
                email = email,
                token = token,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
            repository.saveInvitation(newInvite)
        }
    }

    fun acceptInvitation(token: String) {
        scope.launch {
            // Find and validate token match
            val pendingInvites = _invitations.value
            val match = pendingInvites.find { it.token.trim().equals(token.trim(), ignoreCase = true) }
            if (match != null) {
                repository.saveInvitation(match.copy(status = "ACCEPTED"))
                
                // Insert confirmed relationship into database
                val partner = AccountabilityPartnerEntity(
                    id = UUID.randomUUID().toString(),
                    name = match.email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = match.email,
                    relationshipType = "FRIEND",
                    trustLevel = "STANDARD",
                    notificationsEnabled = true,
                    createdAt = System.currentTimeMillis(),
                    isSynced = true
                )
                repository.savePartner(partner)
            }
        }
    }

    fun removePartner(partnerId: String) {
        scope.launch {
            repository.removePartner(partnerId)
        }
    }

    fun toggleNotifications(partnerId: String, enabled: Boolean) {
        scope.launch {
            val partner = _partners.value.find { it.id == partnerId }
            if (partner != null) {
                repository.savePartner(partner.copy(notificationsEnabled = enabled))
            }
        }
    }
}
