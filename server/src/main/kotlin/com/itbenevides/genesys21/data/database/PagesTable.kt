package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.PageComponent
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object PagesTable : Table("pages") {
    val id = varchar("id", 50)
    val title = varchar("title", 255)
    val ownerId = varchar("owner_id", 100).nullable()
    val theme = varchar("theme", 50)
    val customDomain = varchar("custom_domain", 255).nullable()
    val whatsapp = varchar("whatsapp", 20).nullable()
    val components = json<List<PageComponent>>("components", Json { ignoreUnknownKeys = true })

    override val primaryKey = PrimaryKey(id)
}
