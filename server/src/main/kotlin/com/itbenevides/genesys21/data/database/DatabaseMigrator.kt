package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Utilitário para lidar com migrações e correções estruturais manuais
 * que o framework ORM não suporta nativamente (ex: SQLite constraints).
 */
object DatabaseMigrator {

    /**
     * Corrige a restrição UNIQUE do campo custom_domain no SQLite, 
     * reconstruindo a tabela se necessário.
     */
    fun Transaction.fixCustomDomainConstraint() {
        try {
            val tableSql = exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='pages'") { rs ->
                if (rs.next()) rs.getString("sql") else ""
            } ?: ""

            if (tableSql.contains("custom_domain", true) && tableSql.contains("UNIQUE", true)) {
                rebuildPagesTable()
            }
            
            // Remove índices residuais
            exec("DROP INDEX IF EXISTS pages_custom_domain_unique")
            
        } catch (e: Exception) {
            println("DatabaseMigrator: Erro na migração - ${e.message}")
        }
    }

    private fun Transaction.rebuildPagesTable() {
        println("DatabaseMigrator: Removendo restrição UNIQUE via reconstrução de tabela...")
        
        exec("ALTER TABLE pages RENAME TO pages_old")
        
        // Cria a nova estrutura baseada na definição atual do PagesTable (sem UNIQUE)
        SchemaUtils.create(PagesTable)
        
        exec("""
            INSERT INTO pages (id, title, owner_id, theme, custom_domain, whatsapp, components) 
            SELECT id, title, owner_id, theme, custom_domain, whatsapp, components FROM pages_old
        """.trimIndent())
        
        exec("DROP TABLE pages_old")
        println("DatabaseMigrator: Sucesso!")
    }
}
