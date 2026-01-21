package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.data.database.DatabaseMigrator.fixCustomDomainConstraint
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
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
        createBackup()
        
        // Aplica todas as otimizações via conexão JDBC direta antes de iniciar o pool
        // Isso evita erros de "cannot change ... inside a transaction" do Hikari/Exposed
        applySqliteOptimizations()

        val dataSource = hikari()
        Database.connect(dataSource)
        
        runMigrations()
    }

    private fun setupDatabaseDirectory() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private fun applySqliteOptimizations() {
        try {
            Class.forName("org.sqlite.JDBC")
            java.sql.DriverManager.getConnection("jdbc:sqlite:$DB_PATH").use { conn ->
                conn.createStatement().use { stmt ->
                    // Estas pragmas alteram o arquivo de banco de dados ou estado global da conexão
                    // e devem ser feitas fora de transações.
                    stmt.execute("PRAGMA journal_mode=WAL;")
                    stmt.execute("PRAGMA synchronous=NORMAL;")
                    stmt.execute("PRAGMA temp_store=MEMORY;")
                    stmt.execute("PRAGMA mmap_size=30000000000;") // Otimização de mapeamento de memória
                }
            }
            println("DatabaseFactory: Otimizações SQLite aplicadas com sucesso.")
        } catch (e: Exception) {
            println("DatabaseFactory: Falha ao aplicar otimizações - ${e.message}")
        }
    }

    private fun createBackup() {
        val dbFile = File(DB_PATH)
        if (!dbFile.exists()) return

        try {
            val backupFolder = File("data/backups")
            if (!backupFolder.exists()) backupFolder.mkdirs()

            val timestamp = LocalDateTime.now().toString().replace(":", "-")
            val backupFile = File(backupFolder, "genesys21_backup_$timestamp.db")

            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            println("DatabaseFactory: Backup de segurança criado em ${backupFile.path}")
            
            val files = backupFolder.listFiles()?.sortedByDescending { it.lastModified() }
            if (files != null && files.size > 5) {
                files.drop(5).forEach { it.delete() }
            }
        } catch (e: Exception) {
            println("DatabaseFactory: Falha ao criar backup - ${e.message}")
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.sqlite.JDBC"
        config.jdbcUrl = "jdbc:sqlite:$DB_PATH"
        config.maximumPoolSize = 3 
        
        // Deixamos autoCommit como true por padrão para o pool, 
        // o Exposed gerenciará as transações explicitamente.
        config.isAutoCommit = true
        
        // Removido connectionInitSql para evitar conflitos com transações automáticas do driver
        
        config.validate()
        return HikariDataSource(config)
    }

    private fun runMigrations() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(PagesTable)
            fixCustomDomainConstraint()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
