package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.ReceiptItemsTable
import com.itbenevides.genesys21.data.database.ReceiptsTable
import com.itbenevides.genesys21.domain.model.Receipt
import com.itbenevides.genesys21.domain.model.ReceiptItem
import com.itbenevides.genesys21.domain.repository.ReceiptRepository
import com.itbenevides.genesys21.domain.util.BackupManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteReceiptRepository : ReceiptRepository {

    // No servidor, não usamos Flow para persistência direta (geralmente via REST),
    // mas implementamos para satisfazer a interface se necessário em mocks/testes.
    override val receipts: Flow<List<Receipt>> = MutableStateFlow(emptyList())

    private fun ResultRow.toReceiptItem() = ReceiptItem(
        descricao = this[ReceiptItemsTable.descricao],
        quantidade = this[ReceiptItemsTable.quantidade],
        valorUnitario = this[ReceiptItemsTable.valorUnitario],
        valorTotal = this[ReceiptItemsTable.valorTotal]
    )

    private fun ResultRow.toReceipt(items: List<ReceiptItem>) = Receipt(
        id = this[ReceiptsTable.id],
        userId = this[ReceiptsTable.userId],
        storeId = this[ReceiptsTable.storeId],
        chaveAcesso = this[ReceiptsTable.chaveAcesso],
        emitente = this[ReceiptsTable.emitente],
        cnpjEmitente = this[ReceiptsTable.cnpjEmitente],
        dataEmissao = this[ReceiptsTable.dataEmissao],
        valorTotal = this[ReceiptsTable.valorTotal],
        categoria = this[ReceiptsTable.categoria],
        imagePath = this[ReceiptsTable.imagePath],
        onlineUrl = this[ReceiptsTable.onlineUrl],
        fileBase64 = this[ReceiptsTable.fileBase64],
        fileMimeType = this[ReceiptsTable.fileMimeType],
        items = items,
        createdAt = this[ReceiptsTable.createdAt]
    )

    override suspend fun getAllReceipts(): Result<List<Receipt>> = try {
        dbQuery {
            val allReceiptsRows = ReceiptsTable.selectAll().where { ReceiptsTable.deletedAt.isNull() }.toList()
            val allReceipts = allReceiptsRows.map { row ->
                val items = ReceiptItemsTable.selectAll().where { ReceiptItemsTable.receiptId eq row[ReceiptsTable.id] }
                    .map { it.toReceiptItem() }
                row.toReceipt(items)
            }
            Result.success(allReceipts)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getReceiptsByStore(storeId: String): Result<List<Receipt>> = try {
        dbQuery {
            val rows = ReceiptsTable.selectAll().where { (ReceiptsTable.storeId eq storeId) and (ReceiptsTable.deletedAt.isNull()) }.toList()
            val receipts = rows.map { row ->
                val items = ReceiptItemsTable.selectAll().where { ReceiptItemsTable.receiptId eq row[ReceiptsTable.id] }
                    .map { it.toReceiptItem() }
                row.toReceipt(items)
            }
            Result.success(receipts)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveReceipt(receipt: Receipt): Result<Unit> = try {
        // Como a interface não tem context, usamos os dados do objeto se disponíveis
        val userId = receipt.userId
        if (userId != null) {
            saveReceiptWithUser(receipt, userId)
        } else {
            Result.failure(Exception("UserID obrigatório para salvar nota"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getReceiptsByUser(userId: String): Result<List<Receipt>> = try {
        dbQuery {
            val rows = ReceiptsTable.selectAll().where { (ReceiptsTable.userId eq userId) and (ReceiptsTable.deletedAt.isNull()) }.toList()
            val receipts = rows.map { row ->
                val items = ReceiptItemsTable.selectAll().where { ReceiptItemsTable.receiptId eq row[ReceiptsTable.id] }
                    .map { it.toReceiptItem() }
                row.toReceipt(items)
            }
            Result.success(receipts)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveReceiptWithUser(receipt: Receipt, userId: String): Result<Unit> = try {
        dbQuery {
            val exists = ReceiptsTable.selectAll().where { ReceiptsTable.id eq receipt.id }.count() > 0
            if (exists) {
                ReceiptsTable.update({ ReceiptsTable.id eq receipt.id }) {
                    it[ReceiptsTable.userId] = userId
                    it[storeId] = receipt.storeId
                    it[chaveAcesso] = receipt.chaveAcesso
                    it[emitente] = receipt.emitente
                    it[cnpjEmitente] = receipt.cnpjEmitente
                    it[dataEmissao] = receipt.dataEmissao
                    it[valorTotal] = receipt.valorTotal
                    it[categoria] = receipt.categoria
                    it[imagePath] = receipt.imagePath
                    it[onlineUrl] = receipt.onlineUrl
                    it[fileBase64] = receipt.fileBase64
                    it[fileMimeType] = receipt.fileMimeType
                    it[updatedAt] = System.currentTimeMillis()
                }
                // Remove itens antigos e insere novos
                ReceiptItemsTable.deleteWhere { receiptId eq receipt.id }
            } else {
                ReceiptsTable.insert {
                    it[id] = receipt.id
                    it[ReceiptsTable.userId] = userId
                    it[storeId] = receipt.storeId
                    it[chaveAcesso] = receipt.chaveAcesso
                    it[emitente] = receipt.emitente
                    it[cnpjEmitente] = receipt.cnpjEmitente
                    it[dataEmissao] = receipt.dataEmissao
                    it[valorTotal] = receipt.valorTotal
                    it[categoria] = receipt.categoria
                    it[imagePath] = receipt.imagePath
                    it[onlineUrl] = receipt.onlineUrl
                    it[fileBase64] = receipt.fileBase64
                    it[fileMimeType] = receipt.fileMimeType
                }
            }

            receipt.items.forEach { item ->
                ReceiptItemsTable.insert {
                    it[receiptId] = receipt.id
                    it[descricao] = item.descricao
                    it[quantidade] = item.quantidade
                    it[valorUnitario] = item.valorUnitario
                    it[valorTotal] = item.valorTotal
                }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteReceipt(id: String): Result<Unit> = try {
        dbQuery {
            ReceiptsTable.update({ ReceiptsTable.id eq id }) {
                it[deletedAt] = System.currentTimeMillis()
            }
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun exportToJson(): String {
        return "" // Não implementado no server
    }

    override fun importFromJson(jsonString: String): Result<Unit> {
        return Result.failure(Exception("Não implementado no server"))
    }
}
