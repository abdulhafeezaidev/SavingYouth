package com.behavioralfirewall.domain.model

data class AccountabilityPartner(
    val id: String,
    val name: String,
    val email: String,
    val relationshipType: String, // "FRIEND", "FAMILY", "MENTOR", "SPONSOR"
    val trustLevel: String, // "STANDARD" or "HIGH"
    val notificationsEnabled: Boolean,
    val createdAt: Long
)
