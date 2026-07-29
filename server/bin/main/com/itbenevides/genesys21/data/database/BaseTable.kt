package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.Table

/**
 * Classe base para todas as tabelas do sistema, garantindo padronização
 * de auditoria e suporte a soft delete.
 */
abstract class BaseTable(name: String) : Table(name) {
    val createdAt = long("created_at").clientDefault { System.currentTimeMillis() }
    val updatedAt = long("updated_at").clientDefault { System.currentTimeMillis() }
    val deletedAt = long("deleted_at").nullable()
}
