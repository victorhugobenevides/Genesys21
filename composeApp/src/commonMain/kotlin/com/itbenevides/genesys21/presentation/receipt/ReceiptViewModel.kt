package com.itbenevides.genesys21.presentation.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.ReceiptRepository
import com.itbenevides.genesys21.domain.service.ReceiptParserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ReceiptUiState(
    val receipts: List<Receipt> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Todas",
    val selectedReceipt: Receipt? = null,
    val isScanning: Boolean = false,
    val isLoading: Boolean = false,
    val backupMessage: String? = null,
    val showBackupDialog: Boolean = false,
    val showScanDialog: Boolean = false,
    val geminiApiKey: String = "",
    val selectedImageBytes: ByteArray? = null,
    val selectedMimeType: String? = null,
    val chatMessages: List<ReceiptChatMessage> = emptyList(),
    val pendingParsedReceipt: Receipt? = null
)

class ReceiptViewModel(
    private val repository: ReceiptRepository,
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
        loadAllReceipts()
    }

    fun loadAllReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllReceipts()
            _uiState.value = _uiState.value.copy(isLoading = false)
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
        if (show) {
            val welcomeMsg = ReceiptChatMessage(
                id = "welcome",
                text = "Olá! Eu sou seu assistente Genesys. Como posso ajudar com suas notas fiscais hoje? Você pode me enviar uma foto, um PDF ou colar o link de um QR Code.",
                sender = MessageSender.AI,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            _uiState.value = _uiState.value.copy(
                showScanDialog = true,
                selectedImageBytes = null,
                selectedMimeType = null,
                chatMessages = listOf(welcomeMsg),
                pendingParsedReceipt = null
            )
        } else {
            _uiState.value = _uiState.value.copy(showScanDialog = false)
        }
    }

    fun onGeminiApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(geminiApiKey = key)
    }

    fun openBackupDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBackupDialog = show, backupMessage = null)
    }

    fun onImageSelected(bytes: ByteArray?, mimeType: String? = null) {
        _uiState.value = _uiState.value.copy(selectedImageBytes = bytes, selectedMimeType = mimeType)
    }

    fun sendChatMessage(text: String, fileBase64: String? = null, mimeType: String? = null) {
        val userMsg = ReceiptChatMessage(
            id = com.itbenevides.genesys21.util.GenesysUUID.randomUUID(),
            text = text,
            sender = MessageSender.USER,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            fileBase64 = fileBase64,
            mimeType = mimeType
        )

        _uiState.value = _uiState.value.copy(
            chatMessages = _uiState.value.chatMessages + userMsg,
            isScanning = true
        )

        viewModelScope.launch {
            try {
                val receipt = parserService.parseReceiptDynamic(
                    rawText = text,
                    imageBase64 = fileBase64,
                    apiKey = if (_uiState.value.geminiApiKey.isNotBlank()) _uiState.value.geminiApiKey else null,
                    mimeType = mimeType
                )

                val aiText = if (receipt.valorTotal > 0) {
                    "Entendido! Consegui extrair os dados da nota da **${receipt.emitente}**. \n\n" +
                    "📅 Data: ${receipt.dataEmissao}\n" +
                    "💰 Valor Total: **R$ ${receipt.valorTotal}**\n" +
                    "🛍️ Itens: ${receipt.items.size} encontrado(s).\n\n" +
                    "Deseja salvar esta nota no seu histórico?"
                } else {
                    "Recebi as informações, mas não consegui identificar valores ou itens nesta nota. Pode tentar enviar uma foto mais nítida?"
                }

                val aiMsg = ReceiptChatMessage(
                    id = com.itbenevides.genesys21.util.GenesysUUID.randomUUID(),
                    text = aiText,
                    sender = MessageSender.AI,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    parsedReceipt = receipt
                )

                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + aiMsg,
                    isScanning = false,
                    pendingParsedReceipt = if (receipt.valorTotal > 0) receipt else null
                )
            } catch (e: Exception) {
                val errorText = if (e.message?.contains("429") == true) {
                    "Limite diário de uso gratuito da IA atingido! 🛑 \n\nO Google permite apenas algumas análises gratuitas por dia. Tente novamente mais tarde ou amanhã."
                } else {
                    "Ops, tive um problema ao processar essa nota: ${e.message}"
                }

                val errorMsg = ReceiptChatMessage(
                    id = "err-" + Clock.System.now().toEpochMilliseconds(),
                    text = errorText,
                    sender = MessageSender.AI,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + errorMsg,
                    isScanning = false
                )
            }
        }
    }

    fun savePendingReceipt() {
        _uiState.value.pendingParsedReceipt?.let { receipt ->
            viewModelScope.launch {
                repository.saveReceipt(receipt)
                _uiState.value = _uiState.value.copy(
                    showScanDialog = false,
                    pendingParsedReceipt = null
                )
            }
        }
    }

    fun deleteReceipt(id: String) {
        viewModelScope.launch {
            repository.deleteReceipt(id)
            if (_uiState.value.selectedReceipt?.id == id) {
                _uiState.value = _uiState.value.copy(selectedReceipt = null)
            }
        }
    }

    fun updateReceipt(receipt: Receipt) {
        viewModelScope.launch {
            repository.saveReceipt(receipt)
            if (_uiState.value.selectedReceipt?.id == receipt.id) {
                _uiState.value = _uiState.value.copy(selectedReceipt = receipt)
            }
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
