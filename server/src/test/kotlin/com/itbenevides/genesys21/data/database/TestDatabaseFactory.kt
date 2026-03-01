package com.itbenevides.genesys21.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.UUID

/**
 * Factory para banco de dados de teste (SQLite em arquivo temporário).
 */
object TestDatabaseFactory {
    private var dataSource: HikariDataSource? = null
    private var database: Database? = null

    fun init(): Database {
        cleanup()

        val tempFile = File.createTempFile("genesys21-test-${UUID.randomUUID()}", ".db")
        tempFile.deleteOnExit()

        val config = HikariConfig().apply {
            driverClassName = "org.sqlite.JDBC"
            jdbcUrl = "jdbc:sqlite:${tempFile.absolutePath}"
            maximumPoolSize = 1
            isAutoCommit = true
        }

        val newDataSource = HikariDataSource(config)
        dataSource = newDataSource
        val db = Database.connect(newDataSource)
        database = db

        DatabaseFactory.configureTestDatabase(db)

        transaction(db) {
            SchemaUtils.create(
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
        }

        return db
    }

    fun cleanup() {
        dataSource?.close()
        dataSource = null
        database = null
        DatabaseFactory.configureTestDatabase(null)
    }
}
