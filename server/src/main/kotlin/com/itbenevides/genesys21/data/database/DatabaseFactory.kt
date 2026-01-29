package com.itbenevides.genesys21.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    private const val DB_PATH = "data/genesys21.db"
    
    fun init() {
        setupDatabaseDirectory()
        
        // CORREÇÃO: Removido o bloco que deletava o arquivo .db para preservar os dados.

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
            if (File(DB_PATH).exists()) {
                Class.forName("org.sqlite.JDBC")
                java.sql.DriverManager.getConnection("jdbc:sqlite:$DB_PATH").use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("PRAGMA journal_mode=WAL;")
                        stmt.execute("PRAGMA synchronous=NORMAL;")
                    }
                }
            }
        } catch (e: Exception) { }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.sqlite.JDBC"
        config.jdbcUrl = "jdbc:sqlite:$DB_PATH"
        config.maximumPoolSize = 3 
        config.isAutoCommit = true
        config.validate()
        return HikariDataSource(config)
    }

    private fun runMigrations() {
        transaction {
            // Usamos createMissingTablesAndColumns para apenas adicionar o que faltar, preservando os dados existentes.
            SchemaUtils.createMissingTablesAndColumns(
                PagesTable, 
                PageComponentsTable,
                ProductsTable,
                ProductImagesTable,
                ComponentProductsTable,
                CartsTable, 
                CartItemsTable,
                OrdersTable,
                OrderItemsTable
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
