package com.itbenevides.genesys21.data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object ProductsTable : Table("products") {
    val id = varchar("id", 50)
    val ownerId = varchar("owner_id", 100).index()
    val name = varchar("name", 255)
    val price = double("price")
    val description = text("description").nullable()
    
    val categoryId = reference(
        "category_id", 
        CategoriesTable, 
        onDelete = ReferenceOption.SET_NULL, 
        onUpdate = ReferenceOption.CASCADE
    ).nullable()
    
    val stock = integer("stock").default(0)
    
    // Simplificação: Armazenamos o produto completo em JSON para manter sincronia com o App
    val detailsJson = text("details_json").default("{}")

    override val primaryKey = PrimaryKey(id)
}

object ProductImagesTable : Table("product_images") {
    val id = integer("id").autoIncrement()
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val imageUrl = text("image_url")
    val order = integer("image_order")

    override val primaryKey = PrimaryKey(id)
}

object ComponentProductsTable : Table("component_products") {
    val id = integer("id").autoIncrement()
    val componentId = integer("component_id").references(PageComponentsTable.id, onDelete = ReferenceOption.CASCADE)
    val productId = varchar("product_id", 50).references(ProductsTable.id, onDelete = ReferenceOption.CASCADE)
    val order = integer("product_order")

    override val primaryKey = PrimaryKey(id)
}
