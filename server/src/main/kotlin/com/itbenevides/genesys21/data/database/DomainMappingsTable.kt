package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

object DomainMappingsTable : BaseTable("domain_mappings") {
    val id = varchar("id", 50) // UUID
    val domain = varchar("domain", 255).uniqueIndex()
    val targetPageId = varchar("target_page_id", 50).references(PagesTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}
