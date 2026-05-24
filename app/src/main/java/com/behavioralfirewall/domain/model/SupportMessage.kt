package com.behavioralfirewall.domain.model

data class SupportMessage(
    val id: String,
    val senderId: String,
    val recipientId: String,
    val messageText: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
