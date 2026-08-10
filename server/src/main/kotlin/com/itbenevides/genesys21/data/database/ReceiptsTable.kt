package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.data.database.BaseTable

object ReceiptsTable : BaseTable("receipts") {
    val id = varchar("id", 100)
    val userId = varchar("user_id", 100).references(UsersTable.id)
    val chaveAcesso = varchar("chave_acesso", 100).nullable()
    val emitente = varchar("emitente", 200)
    val cnpjEmitente = varchar("cnpj_emitente", 50).nullable()
    val dataEmissao = varchar("data_emissao", 50)
    val valorTotal = double("valor_total")
    val categoria = varchar("categoria", 100).default("Geral")
    val imagePath = text("image_path").nullable()
    val onlineUrl = text("online_url").nullable()
    val fileBase64 = text("file_base64").nullable()
    val fileMimeType = varchar("file_mime_type", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

object ReceiptItemsTable : BaseTable("receipt_items") {
    val id = integer("id").autoIncrement()
    val receiptId = varchar("receipt_id", 100).references(ReceiptsTable.id)
    val descricao = varchar("descricao", 500)
    val quantidade = double("quantidade").default(1.0)
    val valorUnitario = double("valor_unitario")
    val valorTotal = double("valor_total")

    override val primaryKey = PrimaryKey(id)
}
