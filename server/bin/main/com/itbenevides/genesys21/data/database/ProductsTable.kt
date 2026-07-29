package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object ProductsTable : BaseTable("products") {
    val id = varchar("id", 50) // UUID
    val storeId = varchar("store_id", 50).references(StoresTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val price = double("price")
    val description = text("description").nullable()
    val categoryId = varchar("category_id", 50).references(CategoriesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val stock = integer("stock").default(0)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Normalização: Tabela para armazenar as URLs de imagem de forma atômica.
 */
object ProductImagesTable : BaseTable("product_images") {
    val id = varchar("id", 50) // UUID
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val imageUrl = text("image_url")
    val order = integer("image_order")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Tabela de junção para normalizar a relação entre Componentes de Página e Produtos.
 */
object ComponentProductsTable : Table("component_products") {
    val id = integer("id").autoIncrement()
    val componentId = reference("component_id", PageComponentsTable, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val order = integer("product_order")

    override val primaryKey = PrimaryKey(id)
}
