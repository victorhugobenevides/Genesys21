package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction

/**
 * Utilitário para lidar com migrações e correções estruturais manuais
 * que o framework ORM não suporta nativamente (ex: SQLite constraints).
 */
object DatabaseMigrator {
    /**
     * Corrige conflitos de índices e restrições residuais.
     */
    fun Transaction.runFixes() {
        fixStoresTable()
        fixCustomDomainConstraint()
        fixResidualIndices()
        fixAppointmentsTable()
        fixUsersTableStructural()
        fixUsersTable()
        fixCartItemsTable()
    }

    private fun Transaction.fixUsersTableStructural() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='users'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            if (tableSql.isNotBlank() && !tableSql.contains("permissions", true)) {
                println("DatabaseMigrator: Adicionando coluna 'permissions' à tabela 'users'...")
                exec("ALTER TABLE users ADD COLUMN permissions TEXT DEFAULT ''")
            }
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro ao adicionar coluna 'permissions' - ${e.message}")
        }
    }

    private fun Transaction.fixUsersTable() {
        try {
            val allPerms = com.itbenevides.genesys21.domain.model.UserPermission.entries.joinToString(",") { it.name }
            // Garante que o victorkoto@gmail.com seja SUPERADMIN e tenha todas as permissões se existir
            exec("UPDATE users SET role = 'SUPERADMIN', permissions = '$allPerms' WHERE email = 'victorkoto@gmail.com'")
        } catch (e: Exception) {
            // Ignora se a tabela ainda não existir
        }
    }

    private fun Transaction.fixStoresTable() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='stores'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            if (tableSql.isNotBlank() && !tableSql.contains("origin_zip_code", true)) {
                println("DatabaseMigrator: Tabela 'stores' antiga detectada. Reconstruindo para adicionar novos campos...")
                rebuildStoresTable()
            }
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro ao corrigir tabela 'stores' - ${e.message}")
        }
    }

    private fun Transaction.rebuildStoresTable() {
        exec("DROP TABLE IF EXISTS stores_old")
        exec("ALTER TABLE stores RENAME TO stores_old")

        SchemaUtils.create(StoresTable)

        val oldCols = exec("PRAGMA table_info(stores_old)") { rs ->
            val list = mutableListOf<String>()
            while(rs.next()) list.add(rs.getString("name"))
            list
        } ?: emptyList()

        val columns = listOf("id", "owner_id", "name", "description", "logo_url", "whatsapp")
        val commonCols = columns.filter { oldCols.contains(it) }

        if (commonCols.isNotEmpty()) {
            val colsStr = commonCols.joinToString()
            exec("INSERT INTO stores ($colsStr) SELECT $colsStr FROM stores_old")
        }

        exec("DROP TABLE stores_old")
        println("DatabaseMigrator: Tabela 'stores' reconstruída com sucesso.")
    }

    private fun Transaction.fixAppointmentsTable() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='appointments'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            if (tableSql.isNotBlank()) {
                val hasStrictFK = tableSql.contains("REFERENCES", true) &&
                                 (tableSql.contains("booking_services", true) ||
                                  tableSql.contains("fk_appointments_service_id", true) ||
                                  tableSql.contains("service_id", true))

                if (hasStrictFK || !tableSql.contains("merchant_id", true) || !tableSql.contains("start_time_ms", true) || tableSql.contains("customer_notes", true)) {
                    println("DatabaseMigrator: Reconstruindo tabela 'appointments' para o novo formato...")
                    exec("DROP INDEX IF EXISTS idx_appointments_start_time")
                    exec("DROP TABLE IF EXISTS appointment_notes")
                    exec("DROP TABLE IF EXISTS appointments_old")
                    exec("ALTER TABLE appointments RENAME TO appointments_old")
                    SchemaUtils.create(AppointmentsTable)
                    SchemaUtils.create(AppointmentNotesTable)

                    try {
                        exec("""
                            INSERT INTO appointments (id, service_id, customer_name, customer_phone, status, merchant_id, start_time_ms, end_time_ms)
                            SELECT id, service_id, customer_name, customer_phone, status, 'admin', 0, 0 FROM appointments_old
                        """.trimIndent())
                    } catch (e: Exception) {
                        println("DatabaseMigrator: Não foi possível migrar dados antigos de agendamentos.")
                    }

                    exec("DROP TABLE appointments_old")
                }
            }
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro ao corrigir tabela 'appointments' - ${e.message}")
        }
    }

    private fun Transaction.fixResidualIndices() {
        try {
            val residualIndices =
                listOf(
                    "page_components_page_id",
                    "pages_custom_domain",
                    "pages_custom_domain_unique",
                    "pages_owner_id",
                    "categories_owner_id",
                    "products_owner_id",
                )
            residualIndices.forEach { indexName ->
                val tblName =
                    exec("SELECT tbl_name FROM sqlite_master WHERE type='index' AND name='$indexName'") { rs ->
                        if (rs.next()) rs.getString("tbl_name") else null
                    }

                if (tblName != null && tblName.endsWith("_old")) {
                    println("DatabaseMigrator: Removendo índice residual '$indexName' da tabela '$tblName'")
                    exec("DROP INDEX IF EXISTS $indexName")
                }
            }
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro ao limpar índices - ${e.message}")
        }
    }

    private fun Transaction.fixCustomDomainConstraint() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='pages'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            val hasUnique = tableSql.contains("custom_domain", true) && tableSql.contains("UNIQUE", true)
            val hasOldComponentsColumn = tableSql.contains("components", true)
            val isMissingAudit = !tableSql.contains("created_at", true)

            if (hasUnique || hasOldComponentsColumn || isMissingAudit) {
                rebuildPagesTable(hasOldComponentsColumn)
            }

            exec("DROP INDEX IF EXISTS pages_custom_domain")
            exec("DROP INDEX IF EXISTS pages_custom_domain_unique")
            exec("DROP INDEX IF EXISTS idx_pages_custom_domain")
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro na migração de 'pages' - ${e.message}")
        }
    }

    private fun Transaction.rebuildPagesTable(migrateComponents: Boolean) {
        println("DatabaseMigrator: Reconstruindo tabela 'pages' para normalização e auditoria...")

        exec("DROP TABLE IF EXISTS pages_old")
        exec("ALTER TABLE pages RENAME TO pages_old")

        SchemaUtils.create(PagesTable)

        val columns = mutableListOf("id", "title", "theme", "custom_domain", "whatsapp")

        val oldCols = exec("PRAGMA table_info(pages_old)") { rs ->
            val list = mutableListOf<String>()
            while(rs.next()) list.add(rs.getString("name"))
            list
        } ?: emptyList()

        val storeIdCol = if (oldCols.contains("store_id")) "store_id" else if (oldCols.contains("owner_id")) "owner_id" else null

        val insertCols = columns.toMutableList()
        val selectCols = columns.toMutableList()

        if (storeIdCol != null) {
            insertCols.add("store_id")
            selectCols.add(storeIdCol)
        }

        if (oldCols.contains("created_at")) {
            insertCols.add("created_at")
            selectCols.add("created_at")
        }
        if (oldCols.contains("updated_at")) {
            insertCols.add("updated_at")
            selectCols.add("updated_at")
        }

        val sql = "INSERT INTO pages (${insertCols.joinToString()}) SELECT ${selectCols.joinToString()} FROM pages_old"
        exec(sql)

        exec("DROP TABLE pages_old")
        println("DatabaseMigrator: Tabela 'pages' normalizada com sucesso!")
    }

    private fun Transaction.fixCartItemsTable() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='cart_items'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            // Reconstruir se:
            // 1. Não tiver appointment_data
            // 2. product_id for NOT NULL (queremos que seja opcional para serviços)
            val isProductIdNotNull = tableSql.contains("product_id", true) && tableSql.contains("NOT NULL", true)

            if (tableSql.isNotBlank() && (!tableSql.contains("appointment_data", true) || isProductIdNotNull)) {
                println("DatabaseMigrator: Reconstruindo tabela 'cart_items' para suporte total a agendamentos (product_id opcional)...")
                exec("DROP TABLE IF EXISTS cart_items_old")
                exec("ALTER TABLE cart_items RENAME TO cart_items_old")
                SchemaUtils.create(CartItemsTable)
                // Não tentamos migrar dados de carrinho temporário para simplificar
                exec("DROP TABLE cart_items_old")
            }
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro ao corrigir tabela 'cart_items' - ${e.message}")
        }
    }
}
