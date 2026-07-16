package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.data.database.DatabaseMigrator.runFixes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlinx.coroutines.Dispatchers

object DatabaseFactory {
    private var database: Database? = null

    fun init(jdbcUrl: String = "jdbc:sqlite:data/genesys21.db") {
        if (jdbcUrl.contains("data/")) {
            setupDatabaseDirectory()
            applySqliteOptimizations(jdbcUrl)
        }

        val dataSource = hikari(jdbcUrl)
        database = Database.connect(dataSource)

        runMigrations()
    }

    private fun setupDatabaseDirectory() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private fun applySqliteOptimizations(jdbcUrl: String) {
        try {
            val path = jdbcUrl.removePrefix("jdbc:sqlite:")
            if (File(path).exists()) {
                Class.forName("org.sqlite.JDBC")
                java.sql.DriverManager.getConnection(jdbcUrl).use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("PRAGMA journal_mode=WAL;")
                        stmt.execute("PRAGMA synchronous=NORMAL;")
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun hikari(jdbcUrl: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.sqlite.JDBC"
        config.jdbcUrl = jdbcUrl
        config.maximumPoolSize = if (jdbcUrl.contains(":memory:")) 1 else 3
        config.isAutoCommit = true
        config.validate()
        return HikariDataSource(config)
    }

    private fun runMigrations() {
        transaction {
            // Executa correções estruturais antes de deixar o Exposed criar as tabelas
            runFixes()

            // CORREÇÃO: Suprimindo aviso de depreciação para manter a simplicidade do SQLite no Exposed
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
                OrderItemsTable,
                BookingServicesTable,
                BookingServiceImagesTable,
                MerchantAvailabilityTable,
                WeeklyAvailabilityTable,
                BlockedDatesTable,
                AppointmentsTable,
                AppointmentNotesTable,
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}
