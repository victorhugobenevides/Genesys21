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

    fun init(
        jdbcUrl: String = "jdbc:sqlite:data/genesys21.db",
        rebuild: Boolean = true,
    ) {
        if (rebuild && jdbcUrl.contains("data/")) {
            val path = jdbcUrl.removePrefix("jdbc:sqlite:")
            File(path).delete()
            File("$path-shm").delete()
            File("$path-wal").delete()
        }

        if (jdbcUrl.contains("data/")) {
            setupDatabaseDirectory()
            applySqliteOptimizations(jdbcUrl)
        }

        val dataSource = hikari(jdbcUrl)
        database = Database.connect(dataSource)

        if (rebuild) {
            dropAndRebuild()
            Seeder.seedInitialData()
        } else {
            runMigrations()
            // Garante dados iniciais básicos se o banco estiver vazio
            Seeder.seedInitialData()
        }
    }

    private fun setupDatabaseDirectory() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private fun applySqliteOptimizations(jdbcUrl: String) {
        // Removido temporariamente para evitar SQLException: Query returns results
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
            SchemaUtils.create(
                UsersTable,
                StoresTable,
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
                MediaTable,
                AuditLogsTable,
            )
            runFixes()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    fun dropAndRebuild() {
        transaction {
            SchemaUtils.drop(
                UsersTable,
                StoresTable,
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
                MediaTable,
                AuditLogsTable,
            )
            runMigrations()
        }
    }
}
