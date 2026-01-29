package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object PagesTable : Table("pages") {
    val id = varchar("id", 50)
    val title = varchar("title", 255)
    val ownerId = varchar("owner_id", 100).nullable()
    val theme = varchar("theme", 50)
    val customDomain = varchar("custom_domain", 255).nullable()
    val whatsapp = varchar("whatsapp", 20).nullable()

    override val primaryKey = PrimaryKey(id)
}

/**
 * Normalização: Componentes de página agora usam IDs autoincrementais.
 */
object PageComponentsTable : IntIdTable("page_components") {
    val pageId = varchar("page_id", 50).references(PagesTable.id, onDelete = ReferenceOption.CASCADE)
    val type = varchar("type", 100)
    val customLabel = varchar("custom_label", 255).nullable()
    val isFilterable = bool("is_filterable").default(false)
    val order = integer("order_index")
    val content = text("content").nullable() 
    val configuration = text("configuration").nullable()
}
