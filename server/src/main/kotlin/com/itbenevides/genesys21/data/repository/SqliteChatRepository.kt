package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.MessagesTable
import com.itbenevides.genesys21.domain.model.ChatMessage
import com.itbenevides.genesys21.domain.repository.ChatRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteChatRepository : ChatRepository {

    override suspend fun getMessagesByRefId(refId: String): Result<List<ChatMessage>> = try {
        dbQuery {
            val messages = MessagesTable.selectAll()
                .where { (MessagesTable.refId eq refId) and (MessagesTable.deletedAt.isNull()) }
                .orderBy(MessagesTable.createdAt to SortOrder.ASC)
                .map { it.toChatMessage() }
            Result.success(messages)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> = try {
        dbQuery {
            MessagesTable.insert {
                it[id] = message.id.ifBlank { java.util.UUID.randomUUID().toString() }
                it[refId] = message.refId
                it[senderNick] = message.senderNick
                it[content] = message.content
                it[isFromMerchant] = message.isFromMerchant
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun ResultRow.toChatMessage() = ChatMessage(
        id = this[MessagesTable.id],
        refId = this[MessagesTable.refId],
        senderNick = this[MessagesTable.senderNick],
        content = this[MessagesTable.content],
        isFromMerchant = this[MessagesTable.isFromMerchant],
        createdAt = this[MessagesTable.createdAt]
    )
}
