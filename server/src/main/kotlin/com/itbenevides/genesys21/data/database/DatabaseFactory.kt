package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.data.database.DatabaseMigrator.fixCustomDomainConstraint
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime

object DatabaseFactory {
    private const val DB_PATH = "data/genesys21.db"
    
    fun init() {
        setupDatabaseDirectory()
        // 1. Antes de conectar, fazemos um backup de segurança
        createBackup()
        
        connectToDatabase()
        runMigrations()
    }

    private fun setupDatabaseDirectory() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private fun createBackup() {
        val dbFile = File(DB_PATH)
        if (!dbFile.exists()) return

        try {
            val backupFolder = File("data/backups")
            if (!backupFolder.exists()) backupFolder.mkdirs()

            // Criamos um nome único baseado na data/hora
            val timestamp = LocalDateTime.now().toString().replace(":", "-")
            val backupFile = File(backupFolder, "genesys21_backup_$timestamp.db")

            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("DatabaseFactory: Backup de segurança criado em ${backupFile.path}")
            
            // Opcional: Limpar backups muito antigos (manter apenas os últimos 5)
            val files = backupFolder.listFiles()?.sortedByDescending { it.lastModified() }
            if (files != null && files.size > 5) {
                files.drop(5).forEach { it.delete() }
            }
        } catch (e: Exception) {
            println("DatabaseFactory: Falha ao criar backup - ${e.message}")
        }
    }

    private fun connectToDatabase() {
        val driverClassName = "org.sqlite.JDBC"
        val jdbcUrl = "jdbc:sqlite:$DB_PATH"
        Database.connect(jdbcUrl, driverClassName)
    }

    private fun runMigrations() {
        transaction {
            addLogger(StdOutSqlLogger)
            
            // SEGURO: Apenas adiciona novas tabelas e colunas. Nunca apaga dados.
            SchemaUtils.createMissingTablesAndColumns(PagesTable)
            
            // MANUAL: Lida com casos complexos do SQLite (como restrições UNIQUE)
            fixCustomDomainConstraint()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
