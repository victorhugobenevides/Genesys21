package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.ChatMessage

interface ChatRepository {
    suspend fun getMessagesByRefId(refId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage): Result<Unit>
}
