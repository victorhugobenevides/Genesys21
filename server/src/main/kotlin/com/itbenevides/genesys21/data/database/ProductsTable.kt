package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object ProductsTable : Table("products") {
    val id = varchar("id", 50)
    val ownerId = varchar("owner_id", 100)
    val name = varchar("name", 255)
    val price = double("price")
    val description = text("description").nullable()
    val category = varchar("category", 100).nullable()
    val stock = integer("stock").default(0)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Normalização: Tabela para armazenar as URLs de imagem de forma atômica.
 * Segue a 1ª Forma Normal (Valores Atômicos).
 */
object ProductImagesTable : Table("product_images") {
    val id = integer("id").autoIncrement()
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val imageUrl = text("image_url")
    val order = integer("image_order") // Para manter a sequência das fotos

    override val primaryKey = PrimaryKey(id)
}

/**
 * Tabela de junção para normalizar a relação entre Componentes de Página e Produtos.
 */
object ComponentProductsTable : Table("component_products") {
    val id = integer("id").autoIncrement()
    val componentId = integer("component_id").references(PageComponentsTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val order = integer("product_order")

    override val primaryKey = PrimaryKey(id)
}
