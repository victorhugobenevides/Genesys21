package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Tabela dedicada para categorias, permitindo expansão futura (cores, ícones, etc).
 * Redesenhada para usar UUIDs e suporte multi-tenant (StoreId).
 */
object CategoriesTable : BaseTable("categories") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val parentId = varchar("parent_id", 50).references(id, onDelete = ReferenceOption.SET_NULL).nullable()
    val icon = varchar("icon_name", 50).nullable()
    val color = varchar("color_hex", 10).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("unique_category_per_store", storeId, name)
    }
}
