package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.ChatMessage
import com.itbenevides.genesys21.domain.repository.ChatRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class KtorChatRepository(
    private val client: HttpClient,
    private val baseUrl: String
) : ChatRepository {

    override suspend fun getMessagesByRefId(refId: String): Result<List<ChatMessage>> = try {
        val messages = client.get("$baseUrl/api/chat/$refId").body<List<ChatMessage>>()
        Result.success(messages)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> = try {
        val response = client.post("$baseUrl/api/chat") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }
        if (response.status.isSuccess()) Result.success(Unit)
        else Result.failure(Exception("Erro ao enviar mensagem"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
