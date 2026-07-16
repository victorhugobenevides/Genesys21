package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object PagesTable : Table("pages") {
    val id = varchar("id", 50)
    val ownerId = varchar("owner_id", 100).index("idx_pages_owner_id") // ÍNDICE: Busca rápida por dono
    val title = varchar("title", 200)
    val customDomain = varchar("custom_domain", 255).nullable().uniqueIndex("idx_pages_custom_domain") // ÍNDICE: Unicidade e busca instantânea
    val whatsapp = varchar("whatsapp", 50).nullable()
    val theme = varchar("theme", 50).default("ROYAL")

    override val primaryKey = PrimaryKey(id)
}

object PageComponentsTable : IntIdTable("page_components") {
    val pageId = varchar("page_id", 50).index("idx_page_components_page_id") // ÍNDICE: Busca rápida de blocos de uma página
    val type = varchar("type", 50)
    val customLabel = varchar("custom_label", 100).nullable()
    val isFilterable = bool("is_filterable").default(true)
    val order = integer("order_index")
    val content = text("content").nullable()
}
