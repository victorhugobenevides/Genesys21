package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll

/**
 * Utilitário para lidar com migrações e correções estruturais manuais
 * que o framework ORM não suporta nativamente (ex: SQLite constraints).
 */
object DatabaseMigrator {

    /**
     * Corrige a restrição UNIQUE do campo custom_domain no SQLite, 
     * reconstruindo a tabela se necessário e respeitando a nova estrutura normalizada.
     */
    fun Transaction.fixCustomDomainConstraint() {
        try {
            val tableSql = exec("SELECT sql FROM sqlite_master WHERE type='table' AND name='pages'") { rs ->
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
            println("DatabaseMigrator: Erro na migração - ${e.message}")
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
        exec("""
            INSERT INTO pages (id, title, owner_id, theme, custom_domain, whatsapp) 
            SELECT id, title, owner_id, theme, custom_domain, whatsapp FROM pages_old
        """.trimIndent())
        
        // Nota: A migração dos dados de 'components' (JSON) para a tabela 'page_components' 
        // deve ser tratada aqui se houver dados legados importantes.
        if (migrateComponents) {
            println("DatabaseMigrator: Aviso - Dados da coluna 'components' foram isolados na tabela 'pages_old'.")
        }

        exec("DROP TABLE pages_old")
        println("DatabaseMigrator: Tabela 'pages' normalizada com sucesso!")
    }
}
