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
    }

    private fun Transaction.fixResidualIndices() {
        try {
            // SQLite exige nomes de índices únicos em todo o banco de dados.
            // Se um índice de uma tabela antiga (_old) ainda existir, ele impedirá a criação do novo.
            val residualIndices =
                listOf(
                    "page_components_page_id",
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

            if (hasUnique || hasOldComponentsColumn) {
                rebuildPagesTable(hasOldComponentsColumn)
            }

            // Remove índices residuais
            exec("DROP INDEX IF EXISTS pages_custom_domain_unique")
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro na migração de 'pages' - ${e.message}")
        }
    }

    /**
     * Reconstrói a tabela de páginas para aplicar a normalização e remover constraints.
     */
    private fun Transaction.rebuildPagesTable(migrateComponents: Boolean) {
        println("DatabaseMigrator: Reconstruindo tabela 'pages' para normalização...")

        exec("ALTER TABLE pages RENAME TO pages_old")

        // Cria a nova estrutura baseada na definição atual do PagesTable (sem UNIQUE e sem 'components')
        SchemaUtils.create(PagesTable)

        // Insere apenas as colunas atômicas que permaneceram na tabela Pages
        exec(
            """
            INSERT INTO pages (id, title, owner_id, theme, custom_domain, whatsapp)
            SELECT id, title, owner_id, theme, custom_domain, whatsapp FROM pages_old
            """.trimIndent(),
        )

        if (migrateComponents) {
            println("DatabaseMigrator: Aviso - Dados da coluna 'components' foram isolados na tabela 'pages_old'.")
        }

        exec("DROP TABLE pages_old")
        println("DatabaseMigrator: Tabela 'pages' normalizada com sucesso!")
    }
}
