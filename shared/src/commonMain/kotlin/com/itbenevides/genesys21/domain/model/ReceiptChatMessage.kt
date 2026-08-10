package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MessageSender {
    USER, AI, SYSTEM
}

@Serializable
data class ReceiptChatMessage(
    val id: String,
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = 0L,
    val fileBase64: String? = null,
    val mimeType: String? = null,
    val parsedReceipt: Receipt? = null
)
