package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.data.database.DatabaseMigrator.fixCustomDomainConstraint
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

/**
 * Responsável pela configuração e inicialização da infraestrutura de banco de dados.
 */
object DatabaseFactory {
    
    fun init() {
        setupDatabaseDirectory()
        connectToDatabase()
        runMigrations()
    }

    private fun setupDatabaseDirectory() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private fun connectToDatabase() {
        val driverClassName = "org.sqlite.JDBC"
        val jdbcUrl = "jdbc:sqlite:data/genesys21.db"
        Database.connect(jdbcUrl, driverClassName)
    }

    private fun runMigrations() {
        transaction {
            addLogger(StdOutSqlLogger)
            
            // Cria tabelas faltantes
            SchemaUtils.createMissingTablesAndColumns(PagesTable)
            
            // Executa correções estruturais necessárias (Legacy Refactoring)
            fixCustomDomainConstraint()
        }
    }

    /**
     * Helper genérico para executar queries em suspensão.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
