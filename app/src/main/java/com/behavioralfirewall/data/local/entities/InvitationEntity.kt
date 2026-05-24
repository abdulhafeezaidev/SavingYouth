package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountability_invitations")
data class InvitationEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val token: String,
    val status: String, // "PENDING", "ACCEPTED", "REJECTED"
    val createdAt: Long
)
