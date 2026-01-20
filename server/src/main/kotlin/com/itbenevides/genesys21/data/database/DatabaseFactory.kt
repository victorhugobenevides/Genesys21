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
    val customDomain = varchar("custom_domain", 255).nullable()
    val whatsapp = varchar("whatsapp", 20).nullable()
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
            addLogger(StdOutSqlLogger)
            
            // 1. Tenta criar se não existir
            SchemaUtils.createMissingTablesAndColumns(PagesTable)

            // 2. MIGRAÇÃO FORÇADA PARA REMOVER UNIQUE (Procedimento seguro SQLite)
            try {
                val tableSql = exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='pages'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

                // Se o SQL de criação da tabela ainda contiver a palavra UNIQUE no custom_domain
                if (tableSql.contains("custom_domain", true) && tableSql.contains("UNIQUE", true)) {
                    println("Detectada restrição UNIQUE. Iniciando reconstrução da tabela...")
                    
                    // Renomeia a antiga
                    exec("ALTER TABLE pages RENAME TO pages_old")
                    
                    // Cria a nova estrutura correta (sem UNIQUE)
                    SchemaUtils.create(PagesTable)
                    
                    // Copia os dados existentes
                    exec("INSERT INTO pages (id, title, owner_id, theme, custom_domain, whatsapp, components) " +
                         "SELECT id, title, owner_id, theme, custom_domain, whatsapp, components FROM pages_old")
                    
                    // Deleta a backup
                    exec("DROP TABLE pages_old")
                    
                    println("Tabela reconstruída com sucesso!")
                }
                
                // Remove qualquer índice residual
                exec("DROP INDEX IF EXISTS pages_custom_domain_unique")
                
            } catch (e: Exception) {
                println("Aviso na migração: ${e.message}")
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction { block() }
}
