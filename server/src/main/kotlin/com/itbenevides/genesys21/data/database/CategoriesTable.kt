package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

/**
 * Tabela dedicada para categorias, permitindo expansão futura (cores, ícones, etc).
 */
object CategoriesTable : IntIdTable("categories") {
    val ownerId = varchar("owner_id", 100).index() // Vincula a categoria ao lojista
    val name = varchar("name", 100)
    val icon = varchar("icon_name", 50).nullable()
    val color = varchar("color_hex", 10).nullable()
    
    init {
        // Garante que o mesmo lojista não crie categorias duplicadas com o mesmo nome
        uniqueIndex("unique_category_per_owner", ownerId, name)
    }
}
