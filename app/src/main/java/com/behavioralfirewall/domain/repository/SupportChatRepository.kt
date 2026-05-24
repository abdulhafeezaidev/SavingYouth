package com.behavioralfirewall.domain.repository

import com.behavioralfirewall.domain.model.SupportMessage
import kotlinx.coroutines.flow.Flow

interface SupportChatRepository {
    fun getMessages(partnerId: String): Flow<List<SupportMessage>>
    suspend fun sendMessage(recipientId: String, text: String): Boolean
    suspend fun markAsRead(messageId: String)
}
