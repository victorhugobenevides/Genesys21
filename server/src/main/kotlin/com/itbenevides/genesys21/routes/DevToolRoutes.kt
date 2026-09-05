package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Ferramentas de desenvolvimento e introspecção para IA.
 * NOTA: Em produção real, estas rotas devem ser protegidas por Header de API Key ou IP.
 */
fun Route.devToolRoutes() {
    route("/api/dev") {

        // Retorna o esquema atual do banco para que a IA entenda as tabelas
        get("/schema") {
            try {
                val tables = listOf(
                    UsersTable, StoresTable, ProductsTable, ProductImagesTable,
                    CategoriesTable, PagesTable, PageComponentsTable,
                    ComponentProductsTable, BookingServicesTable, AppointmentsTable,
                    OrderStatusLogsTable, AuditLogsTable, DomainMappingsTable,
                    CartsTable, CartItemsTable, AddressesTable
                )

                val schemaInfo = tables.associate { table ->
                    table.tableName to table.columns.map { col ->
                        mapOf(
                            "name" to col.name,
                            "type" to col.columnType.toString(),
                            "nullable" to col.columnType.nullable
                        )
                    }
                }

                call.respond(mapOf(
                    "status" to "success",
                    "database" to "SQLite (Exposed)",
                    "schema" to schemaInfo
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Erro ao ler esquema")))
            }
        }

        // Diagnóstico avançado de ambiente
        get("/env") {
            val isProd = System.getenv("PROD_MODE") == "true"
            val ownerEmail = System.getenv("OWNER_EMAIL") ?: "not_set"

            call.respond(mapOf(
                "environment" to if (isProd) "PRODUCTION" else "DEVELOPMENT",
                "ownerEmail" to ownerEmail,
                "serverTime" to System.currentTimeMillis(),
                "jvmVersion" to System.getProperty("java.version"),
                "os" to System.getProperty("os.name")
            ))
        }
    }
}
