package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.model.ChatMessage
import com.itbenevides.genesys21.domain.repository.ChatRepository

class GetChatMessagesUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(refId: String) = repository.getMessagesByRefId(refId)
}

class SendChatMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: ChatMessage) = repository.sendMessage(message)
}
