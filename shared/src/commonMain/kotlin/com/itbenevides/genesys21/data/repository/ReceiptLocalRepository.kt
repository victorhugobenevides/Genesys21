package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.repository.ReceiptRepository
import com.itbenevides.genesys21.domain.util.BackupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReceiptLocalRepository : ReceiptRepository {

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    override val receipts: Flow<List<Receipt>> = _receipts.asStateFlow()

    override suspend fun getAllReceipts(): Result<List<Receipt>> {
        return Result.success(_receipts.value)
    }

    override suspend fun saveReceipt(receipt: Receipt): Result<Unit> {
        val currentList = _receipts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == receipt.id }
        if (index >= 0) {
            currentList[index] = receipt
        } else {
            currentList.add(0, receipt) // Adiciona no topo
        }
        _receipts.value = currentList
        return Result.success(Unit)
    }

    override suspend fun deleteReceipt(id: String): Result<Unit> {
        _receipts.value = _receipts.value.filterNot { it.id == id }
        return Result.success(Unit)
    }

    override fun exportToJson(): String {
        return BackupManager.exportToJson(_receipts.value)
    }

    override fun importFromJson(jsonString: String): Result<Unit> {
        return BackupManager.importFromJson(jsonString).map { importedList ->
            val currentList = _receipts.value.toMutableList()
            importedList.forEach { newReceipt ->
                if (currentList.none { it.id == newReceipt.id }) {
                    currentList.add(0, newReceipt)
                }
            }
            _receipts.value = currentList
        }
    }
}
