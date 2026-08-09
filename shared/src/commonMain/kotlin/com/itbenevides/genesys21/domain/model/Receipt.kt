package com.itbenevides.genesys21.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    val id: String,
    val chaveAcesso: String? = null,
    val emitente: String,
    val cnpjEmitente: String? = null,
    val dataEmissao: String,
    val valorTotal: Double,
    val categoria: String = "Geral",
    val imagePath: String? = null,
    val onlineUrl: String? = null,
    val items: List<ReceiptItem> = emptyList(),
    val createdAt: Long = 0L
)

@Serializable
data class ReceiptItem(
    val descricao: String,
    val quantidade: Double = 1.0,
    val valorUnitario: Double = 0.0,
    val valorTotal: Double = 0.0
)
