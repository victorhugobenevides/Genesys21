package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction

/**
 * Utilitário para lidar com migrações e correções estruturais manuais.
 */
object DatabaseMigrator {

    fun Transaction.runFixes() {
        fixCustomDomainConstraint()
        fixResidualIndices()
    }

    private fun Transaction.fixResidualIndices() {
        try {
            val residualIndices = listOf("page_components_page_id", "pages_custom_domain_unique", "pages_owner_id", "products_owner_id")
            
            residualIndices.forEach { indexName ->
                exec("DROP INDEX IF EXISTS $indexName")
            }
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro ao limpar índices - ${e.message}")
        }
    }

    private fun Transaction.fixCustomDomainConstraint() {
        try {
            // Verifica se a tabela pages existe e se pages_old já existe
            val pagesExists = exec("SELECT name FROM sqlite_master WHERE type='table' AND name='pages'") { rs -> rs.next() } == true
            val pagesOldExists = exec("SELECT name FROM sqlite_master WHERE type='table' AND name='pages_old'") { rs -> rs.next() } == true

            if (!pagesExists) return

            val tableSql = exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='pages'") { rs ->
                if (rs.next()) rs.getString("sql") else ""
            } ?: ""

            // Verifica se a tabela ainda tem a estrutura antiga ou a coluna de componentes obsoleta
            val needsMigration = tableSql.contains("components", true) || tableSql.contains("UNIQUE", true)

            if (needsMigration) {
                if (pagesOldExists) {
                    exec("DROP TABLE IF EXISTS pages_old")
                }
                rebuildPagesTable()
            }
            
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro na migração de 'pages' - ${e.message}")
        }
    }

    private fun Transaction.rebuildPagesTable() {
        println("DatabaseMigrator: Reconstruindo tabela 'pages' para normalização...")
        
        exec("ALTER TABLE pages RENAME TO pages_old")
        
        // Cria a nova estrutura baseada na definição atual
        SchemaUtils.create(PagesTable)
        
        // Tenta copiar os dados de forma segura, ignorando colunas que não existem mais
        exec("""
            INSERT INTO pages (id, title, owner_id, theme, custom_domain, whatsapp, components_json) 
            SELECT id, title, owner_id, theme, custom_domain, whatsapp, '[]' FROM pages_old
        """.trimIndent())
        
        exec("DROP TABLE pages_old")
        println("DatabaseMigrator: Tabela 'pages' normalizada com sucesso!")
    }
}
