package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object MediaTable : BaseTable("media") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE)
    val ownerId = varchar("owner_id", 100).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val url = text("url")
    val fileName = varchar("file_name", 255)
    val mimeType = varchar("mime_type", 100)
    val sizeBytes = long("size_bytes")
    val altText = varchar("alt_text", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}
