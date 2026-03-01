package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object PagesTable : Table("pages") {
    val id = varchar("id", 50)
    val ownerId = varchar("owner_id", 100)
    val title = varchar("title", 200)
    // O índice único será criado manualmente no DatabaseFactory para evitar crashes de inicialização
    val customDomain = varchar("custom_domain", 255).nullable()
    val whatsapp = varchar("whatsapp", 50).nullable()
    val theme = varchar("theme", 50).default("ROYAL")
    val componentsJson = text("components_json").default("[]")
    
    override val primaryKey = PrimaryKey(id)
}

object PageComponentsTable : IntIdTable("page_components") {
    val pageId = varchar("page_id", 50).index()
    val type = varchar("type", 50)
    val customLabel = varchar("custom_label", 100).nullable()
    val isFilterable = bool("is_filterable").default(true)
    val order = integer("order_index")
    val content = text("content").nullable()
}
