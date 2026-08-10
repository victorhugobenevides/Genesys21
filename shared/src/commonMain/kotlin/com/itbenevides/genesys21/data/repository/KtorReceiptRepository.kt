package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.repository.ReceiptRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class KtorReceiptRepository(
    private val client: HttpClient,
    private val baseUrl: String,
    private val authRepository: AuthRepository
) : ReceiptRepository {

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    override val receipts: Flow<List<Receipt>> = _receipts.asStateFlow()

    override suspend fun getAllReceipts(): Result<List<Receipt>> = try {
        val token = authRepository.getCurrentUserToken() ?: throw Exception("Usuário não autenticado")
        val response = client.get("$baseUrl/api/receipts") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) {
            val list = response.body<List<Receipt>>()
            _receipts.value = list
            Result.success(list)
        } else {
            Result.failure(Exception("Erro ao buscar notas: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveReceipt(receipt: Receipt): Result<Unit> = try {
        val token = authRepository.getCurrentUserToken() ?: throw Exception("Usuário não autenticado")
        val response = client.post("$baseUrl/api/receipts") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(receipt)
        }
        if (response.status.isSuccess()) {
            getAllReceipts() // Atualiza cache local
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao salvar nota: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteReceipt(id: String): Result<Unit> = try {
        val token = authRepository.getCurrentUserToken() ?: throw Exception("Usuário não autenticado")
        val response = client.delete("$baseUrl/api/receipts/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.isSuccess()) {
            getAllReceipts() // Atualiza cache local
            Result.success(Unit)
        } else {
            Result.failure(Exception("Erro ao excluir nota: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun exportToJson(): String = "" // Implementar via Shared se necessário

    override fun importFromJson(jsonString: String): Result<Unit> = Result.failure(Exception("Use importação via Servidor"))
}
