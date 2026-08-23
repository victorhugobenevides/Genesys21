package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object DraftsTable : BaseTable("drafts") {
    val id = varchar("id", 50) // UUID
    val ownerId = varchar("owner_id", 100).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val pageId = varchar("page_id", 50) // Original page ID
    val content = text("content") // Serialized Page object

    override val primaryKey = PrimaryKey(id)
}
