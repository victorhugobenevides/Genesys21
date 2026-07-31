package com.itbenevides.genesys21.data.database

import com.itbenevides.genesys21.data.database.DatabaseMigrator.runFixes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlinx.coroutines.Dispatchers

object DatabaseFactory {
    private var database: Database? = null
    private var dataSource: HikariDataSource? = null

    fun init(
        jdbcUrl: String = "jdbc:sqlite:data/genesys21.db?journal_mode=WAL&busy_timeout=10000",
        rebuild: Boolean = true,
    ) {
        if (database != null && !rebuild) return

        // Fecha o pool anterior se existir para evitar vazamento de conexões
        // e travas no SQLite (especialmente em memória compartilhada)
        dataSource?.close()

        if (rebuild && jdbcUrl.contains("data/")) {
            val path = jdbcUrl.removePrefix("jdbc:sqlite:")
            File(path).delete()
            File("$path-shm").delete()
            File("$path-wal").delete()
        }

        if (jdbcUrl.contains("data/")) {
            setupDatabaseDirectory()
        }

        val ds = hikari(jdbcUrl)
        dataSource = ds
        database = Database.connect(ds)

        if (jdbcUrl.contains("data/")) {
            applySqliteOptimizations()
        }

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

    private fun applySqliteOptimizations() {
        // WAL mode e Busy Timeout já estão no JDBC URL default, mas reforçamos aqui via PRAGMA
        transaction(database) {
            exec("PRAGMA journal_mode=WAL;")
            exec("PRAGMA busy_timeout=10000;")
            exec("PRAGMA synchronous=NORMAL;")
        }
    }

    private fun hikari(jdbcUrl: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.sqlite.JDBC"
        config.jdbcUrl = jdbcUrl
        // Para SQLite, o mais estável para evitar BusyException é usar apenas 1 conexão de escrita.
        // O modo WAL já permite múltiplas leituras, mas Exposed/Hikari gerenciam melhor com pool de 1.
        config.maximumPoolSize = 1
        config.isAutoCommit = true
        // Adiciona um gancho para garantir PRAGMAs em cada conexão do pool
        config.connectionInitSql = "PRAGMA journal_mode=WAL; PRAGMA busy_timeout=10000; PRAGMA synchronous=NORMAL;"
        config.validate()
        return HikariDataSource(config)
    }

    private fun runMigrations() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
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
                AddressesTable,
                MediaTable,
                AuditLogsTable,
            )
            runFixes()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        if (TransactionManager.currentOrNull() != null) {
            block()
        } else {
            newSuspendedTransaction(Dispatchers.IO, db = database) { block() }
        }

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
                AddressesTable,
                MediaTable,
                AuditLogsTable,
            )
            runMigrations()
        }
    }
}
