package com.itbenevides.genesys21.domain.repository

import com.itbenevides.genesys21.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface ReceiptRepository {
    val receipts: Flow<List<Receipt>>
    suspend fun saveReceipt(receipt: Receipt): Result<Unit>
    suspend fun deleteReceipt(id: String): Result<Unit>
    suspend fun getAllReceipts(): Result<List<Receipt>>
    fun exportToJson(): String
    fun importFromJson(jsonString: String): Result<Unit>
}
