package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.util.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReceiptLocalRepository {

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()

    init {
        // Carrega notas pré-cadastradas/exemplo iniciais para teste imediato
        loadInitialMockData()
    }

    fun getAllReceipts(): List<Receipt> {
        return _receipts.value
    }

    fun getReceiptById(id: String): Receipt? {
        return _receipts.value.find { it.id == id }
    }

    fun saveReceipt(receipt: Receipt) {
        val currentList = _receipts.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == receipt.id }
        if (index >= 0) {
            currentList[index] = receipt
        } else {
            currentList.add(0, receipt) // Adiciona no topo
        }
        _receipts.value = currentList
    }

    fun deleteReceipt(id: String) {
        _receipts.value = _receipts.value.filterNot { it.id == id }
    }

    fun exportToJson(): String {
        return BackupManager.exportToJson(_receipts.value)
    }

    fun importFromJson(jsonString: String): Result<Unit> {
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

    private fun loadInitialMockData() {
        val mockReceipt = Receipt(
            id = "mock-10032026",
            chaveAcesso = "31260322124687000100550120001565881575876081",
            emitente = "KARZEN ELETRO",
            cnpjEmitente = "22.124.687/0001-00",
            dataEmissao = "10/03/2026",
            valorTotal = 158.49,
            categoria = "Eletrônicos",
            onlineUrl = "https://nfe.fazenda.mg.gov.br/nfe/portal/consultas/consulta-completa-nfe/31260322124687000100550120001565881575876081",
            items = listOf(
                com.itbenevides.genesys21.domain.model.ReceiptItem(
                    descricao = "Aparador de Pelos Mondial Super Body Groom",
                    quantidade = 1.0,
                    valorUnitario = 158.49,
                    valorTotal = 158.49
                )
            ),
            createdAt = 1773140400000L
        )
        _receipts.value = listOf(mockReceipt)
    }
}
