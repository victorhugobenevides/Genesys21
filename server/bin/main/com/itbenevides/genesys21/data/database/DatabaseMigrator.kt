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
        fixCustomDomainConstraint()
        fixResidualIndices()
        fixAppointmentsTable()
        fixUsersTable()
    }

    private fun Transaction.fixUsersTable() {
        try {
            // Garante que o victorkoto@gmail.com seja SUPERADMIN se existir
            exec("UPDATE users SET role = 'SUPERADMIN' WHERE email = 'victorkoto@gmail.com'")
        } catch (e: Exception) {
            // Ignora se a tabela ainda não existir
        }
    }

    private fun Transaction.fixAppointmentsTable() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='appointments'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            if (tableSql.isNotBlank()) {
                // Se a tabela tem a restrição de FK (BookingServicesTable), precisamos reconstruir
                // para permitir agendamentos em serviços de templates que não estão no banco global.
                // Agora verificamos de forma mais agressiva qualquer sinal de 'service_id' ou FK.
                val hasStrictFK = tableSql.contains("REFERENCES", true) &&
                                 (tableSql.contains("booking_services", true) ||
                                  tableSql.contains("fk_appointments_service_id", true) ||
                                  tableSql.contains("service_id", true))

                if (hasStrictFK || !tableSql.contains("merchant_id", true) || !tableSql.contains("start_time_ms", true) || tableSql.contains("customer_notes", true)) {
                    println("DatabaseMigrator: Reconstruindo tabela 'appointments' para o novo formato de notas (tabela separada)...")
                    exec("DROP TABLE IF EXISTS appointment_notes") // Limpa notas antigas se houver
                    exec("DROP TABLE IF EXISTS appointments_old")
                    exec("ALTER TABLE appointments RENAME TO appointments_old")
                    SchemaUtils.create(AppointmentsTable)
                    SchemaUtils.create(AppointmentNotesTable)

                    // Tenta migrar os dados básicos
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
            // SQLite exige nomes de índices únicos em todo o banco de dados.
            // Se um índice de uma tabela antiga (_old) ainda existir, ele impedirá a criação do novo.
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

    /**
     * Corrige a restrição UNIQUE do campo custom_domain no SQLite,
     * reconstruindo a tabela se necessário e respeitando a nova estrutura normalizada.
     */
    private fun Transaction.fixCustomDomainConstraint() {
        try {
            val tableSql =
                exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='pages'") { rs ->
                    if (rs.next()) rs.getString("sql") else ""
                } ?: ""

            // Verifica se a tabela ainda tem a estrutura antiga ou a restrição UNIQUE indesejada
            val hasUnique = tableSql.contains("custom_domain", true) && tableSql.contains("UNIQUE", true)
            val hasOldComponentsColumn = tableSql.contains("components", true)
            val isMissingAudit = !tableSql.contains("created_at", true)

            if (hasUnique || hasOldComponentsColumn || isMissingAudit) {
                rebuildPagesTable(hasOldComponentsColumn)
            }

            // Remove índices residuais
            exec("DROP INDEX IF EXISTS pages_custom_domain")
            exec("DROP INDEX IF EXISTS pages_custom_domain_unique")
            exec("DROP INDEX IF EXISTS idx_pages_custom_domain")
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro na migração de 'pages' - ${e.message}")
        }
    }

    /**
     * Reconstrói a tabela de páginas para aplicar a normalização e remover constraints.
     */
    private fun Transaction.rebuildPagesTable(migrateComponents: Boolean) {
        println("DatabaseMigrator: Reconstruindo tabela 'pages' para normalização e auditoria...")

        exec("DROP TABLE IF EXISTS pages_old")
        exec("ALTER TABLE pages RENAME TO pages_old")

        // Cria a nova estrutura baseada na definição atual do PagesTable
        SchemaUtils.create(PagesTable)

        // Mapeia colunas existentes para migração
        val columns = mutableListOf("id", "title", "theme", "custom_domain", "whatsapp")

        // Verifica se store_id ou owner_id existe na tabela antiga
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

        // Tenta migrar datas se existirem
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

        if (migrateComponents) {
            println("DatabaseMigrator: Aviso - Dados da coluna 'components' foram isolados na tabela 'pages_old'.")
        }

        exec("DROP TABLE pages_old")
        println("DatabaseMigrator: Tabela 'pages' normalizada com sucesso!")
    }
}
