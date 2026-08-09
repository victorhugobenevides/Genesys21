package com.itbenevides.genesys21.presentation.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.data.repository.ReceiptLocalRepository
import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.service.ReceiptParserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReceiptUiState(
    val receipts: List<Receipt> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Todas",
    val selectedReceipt: Receipt? = null,
    val isScanning: Boolean = false,
    val backupMessage: String? = null,
    val showBackupDialog: Boolean = false,
    val showScanDialog: Boolean = false,
    val geminiApiKey: String = "",
    val selectedImageBytes: ByteArray? = null,
    val selectedMimeType: String? = null
)

class ReceiptViewModel(
    private val repository: ReceiptLocalRepository,
    private val parserService: ReceiptParserService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.receipts.collect { list ->
                _uiState.value = _uiState.value.copy(receipts = list)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun selectReceipt(receipt: Receipt?) {
        _uiState.value = _uiState.value.copy(selectedReceipt = receipt)
    }

    fun openScanDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showScanDialog = show, selectedImageBytes = null)
    }

    fun onGeminiApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(geminiApiKey = key)
    }

    fun onImageSelected(bytes: ByteArray?, mimeType: String? = null) {
        _uiState.value = _uiState.value.copy(selectedImageBytes = bytes, selectedMimeType = mimeType)
    }

    fun openBackupDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBackupDialog = show, backupMessage = null)
    }

    fun processReceiptText(rawText: String, imageBase64: String? = null, apiKey: String? = null) {
        val mimeType = _uiState.value.selectedMimeType
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                // Chamada via backend (Segurança e Anti-Injection)
                val newReceipt = parserService.parseReceiptDynamic(rawText, imageBase64, apiKey, mimeType)
                repository.saveReceipt(newReceipt)
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    showScanDialog = false,
                    selectedReceipt = newReceipt,
                    selectedImageBytes = null,
                    selectedMimeType = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    backupMessage = "❌ Erro ao processar nota: ${e.message}"
                )
            }
        }
    }

    fun deleteReceipt(id: String) {
        repository.deleteReceipt(id)
        if (_uiState.value.selectedReceipt?.id == id) {
            _uiState.value = _uiState.value.copy(selectedReceipt = null)
        }
    }

    fun exportBackupJson(): String {
        return repository.exportToJson()
    }

    fun importBackupJson(jsonString: String) {
        val result = repository.importFromJson(jsonString)
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(backupMessage = "✅ Backup importado com sucesso!")
        } else {
            _uiState.value = _uiState.value.copy(backupMessage = "❌ Erro ao importar JSON: ${result.exceptionOrNull()?.message}")
        }
    }
}
