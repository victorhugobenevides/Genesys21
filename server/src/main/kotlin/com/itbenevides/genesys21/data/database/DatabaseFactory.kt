package com.itbenevides.genesys21.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File
import kotlinx.coroutines.Dispatchers

object DatabaseFactory {
    private var database: Database? = null
    private var dataSource: HikariDataSource? = null

    fun init(
        jdbcUrl: String = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:data/genesys21.db?journal_mode=WAL&busy_timeout=10000",
        rebuild: Boolean = false,
    ) {
        if (database != null && !rebuild) return

        dataSource?.close()

        val isSqlite = jdbcUrl.startsWith("jdbc:sqlite:")
        val dbPath = if (isSqlite) jdbcUrl.substringAfter("jdbc:sqlite:").substringBefore("?") else null

        if (rebuild && isSqlite && dbPath != null) {
            val file = File(dbPath)
            if (file.exists()) file.delete()
            File("$dbPath-shm").delete()
            File("$dbPath-wal").delete()
        }

        if (isSqlite && dbPath != null) {
            setupDatabaseDirectory()
        }

        val ds = hikari(jdbcUrl, isSqlite)
        dataSource = ds

        // 1. Run Flyway Migrations
        runFlyway(ds)

        // 2. Connect Exposed
        database = Database.connect(ds)

        // 3. Seed initial data if needed
        Seeder.seedInitialData()
    }

    private fun runFlyway(ds: javax.sql.DataSource) {
        val flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
        flyway.migrate()
    }

    private fun setupDatabaseDirectory() {
        val dataFolder = File("data")
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    private fun hikari(jdbcUrl: String, isSqlite: Boolean): HikariDataSource {
        val config = HikariConfig()
        if (isSqlite) {
            config.driverClassName = "org.sqlite.JDBC"
            config.maximumPoolSize = 1
            config.connectionInitSql = "PRAGMA journal_mode=WAL; PRAGMA busy_timeout=10000; PRAGMA synchronous=NORMAL;"
        } else {
            config.driverClassName = "org.postgresql.Driver"
            config.maximumPoolSize = 10
        }
        config.jdbcUrl = jdbcUrl
        config.isAutoCommit = true
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        if (TransactionManager.currentOrNull() != null) {
            block()
        } else {
            newSuspendedTransaction(Dispatchers.IO, db = database) { block() }
        }
}
