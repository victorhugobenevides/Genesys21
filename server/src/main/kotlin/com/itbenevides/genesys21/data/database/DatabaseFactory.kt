package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.domain.model.PageComponent
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object PagesTable : Table("pages") {
    val id = varchar("id", 50)
    val title = varchar("title", 255)
    val ownerId = varchar("owner_id", 100).nullable()
    val theme = varchar("theme", 50)
    val customDomain = varchar("custom_domain", 255).nullable().uniqueIndex() // NOVO CAMPO
    val components = json<List<PageComponent>>("components", Json { ignoreUnknownKeys = true })

    override val primaryKey = PrimaryKey(id)
}

object DatabaseFactory {
    fun init() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()

        val driverClassName = "org.sqlite.JDBC"
        val jdbcUrl = "jdbc:sqlite:data/genesys21.db"
        Database.connect(jdbcUrl, driverClassName)
        
        transaction {
            SchemaUtils.create(PagesTable)
            // Caso a tabela já exista, adicionamos a coluna se ela não existir
            val columns = PagesTable.columns.map { it.name }
            if ("custom_domain" !in columns) {
                exec("ALTER TABLE pages ADD COLUMN custom_domain VARCHAR(255)")
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction { block() }
}
