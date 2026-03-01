package com.itbenevides.genesys21.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import com.itbenevides.genesys21.data.database.DatabaseMigrator.runFixes

object DatabaseFactory {
    private const val DB_PATH = "data/genesys21.db"
    private var testDatabase: Database? = null
    
    /**
     * Configura um banco de dados para testes.
     * Quando configurado, todas as queries usarão este banco.
     */
    fun configureTestDatabase(database: Database?) {
        testDatabase = database
    }
    
    fun init() {
        setupDatabaseDirectory()
        
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
            runFixes()

            @Suppress("DEPRECATION")
            SchemaUtils.createMissingTablesAndColumns(
                CategoriesTable,
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

            // CRIAÇÃO SEGURA DE ÍNDICES:
            // Usamos SQL puro para garantir que a inicialização não crash se o índice já existir.
            exec("CREATE INDEX IF NOT EXISTS pages_owner_id ON pages (owner_id);")
            exec("CREATE INDEX IF NOT EXISTS products_owner_id ON products (owner_id);")
            // Índice único seguro para o domínio customizado
            exec("CREATE UNIQUE INDEX IF NOT EXISTS pages_custom_domain ON pages (custom_domain);")
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T {
        val db = testDatabase
        return if (db != null) {
            newSuspendedTransaction(Dispatchers.IO, db) { block() }
        } else {
            newSuspendedTransaction(Dispatchers.IO) { block() }
        }
    }
}
