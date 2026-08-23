package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String, // UUID
    val refId: String, // Order ID or Appointment ID
    val senderNick: String,
    val content: String,
    val isFromMerchant: Boolean = false,
    val createdAt: Long = 0
)
