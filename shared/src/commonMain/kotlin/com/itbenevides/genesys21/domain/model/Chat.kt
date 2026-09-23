package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    /** UUID */
    val id: String,
    /** Order ID or Appointment ID */
    val refId: String,
    val senderNick: String,
    val content: String,
    val isFromMerchant: Boolean = false,
    val createdAt: Long = 0,
)
