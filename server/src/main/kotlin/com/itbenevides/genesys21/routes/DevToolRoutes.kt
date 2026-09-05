package com.itbenevides.genesys21.routes

import com.itbenevides.genesys21.data.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

        // Relatório de Segurança e Superfície de Ataque
        get("/security/report") {
            val stripeSk = System.getenv("STRIPE_SECRET_KEY") ?: ""
            val isStripeTest = stripeSk.contains("test") || stripeSk.isBlank()

            val tables = listOf(
                UsersTable, StoresTable, ProductsTable, PageComponentsTable,
                BookingServicesTable, DomainMappingsTable, AuditLogsTable
            )

            val diagnostic = transaction {
                val adminEmail = System.getenv("OWNER_EMAIL") ?: "victorkoto@gmail.com"
                val adminEntry = UsersTable.selectAll().where { UsersTable.email eq adminEmail }.firstOrNull()

                mapOf(
                    "dogmaCheck" to mapOf(
                        "configuredEmail" to adminEmail,
                        "foundInDb" to (adminEntry != null),
                        "actualRoleInDb" to (adminEntry?.get(UsersTable.role) ?: "MISSING")
                    ),
                    "stripeIntegrity" to mapOf(
                        "mode" to if (isStripeTest) "TEST/INSECURE" else "LIVE",
                        "keyPresent" to stripeSk.isNotBlank()
                    ),
                    "surfaceArea" to mapOf(
                        "totalTables" to tables.size,
                        "rebuildEnabled" to (System.getenv("DB_REBUILD") == "true")
                    )
                )
            }

            call.respond(diagnostic)
        }
    }
}
