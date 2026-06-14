package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.CartItem
import com.itbenevides.genesys21.domain.model.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqliteCartRepository {
    suspend fun getCart(userId: String): List<CartItem> =
        dbQuery {
            // 1. Garantir que o carrinho mestre exista
            if (CartsTable.selectAll().where { CartsTable.userId eq userId }.count() == 0L) {
                CartsTable.insert { it[CartsTable.userId] = userId }
                return@dbQuery emptyList<CartItem>()
            }

            // 2. Busca com tratamento de nulos para produtos de template
            (CartItemsTable leftJoin ProductsTable leftJoin CategoriesTable)
                .selectAll().where { CartItemsTable.userId eq userId }
                .map { row ->
                    val productId = row[CartItemsTable.productId]
                    val hasProductMaster = row.getOrNull(ProductsTable.id) != null

                    val images =
                        if (hasProductMaster) {
                            ProductImagesTable.selectAll()
                                .where { ProductImagesTable.productId eq productId }
                                .orderBy(ProductImagesTable.order to SortOrder.ASC)
                                .map { it[ProductImagesTable.imageUrl] }
                        } else {
                            emptyList()
                        }

                    CartItem(
                        product =
                            Product(
                                id = productId,
                                name = if (hasProductMaster) row[ProductsTable.name] else "Produto de Exemplo",
                                price = if (hasProductMaster) row[ProductsTable.price] else 0.0,
                                imageUrls = images,
                                description = if (hasProductMaster) (row[ProductsTable.description] ?: "") else "",
                                categoryId = row[ProductsTable.categoryId]?.value,
                                categoryName = row.getOrNull(CategoriesTable.name),
                                stock = if (hasProductMaster) row[ProductsTable.stock] else 0,
                            ),
                        quantity = row[CartItemsTable.quantity],
                    )
                }
        }

    suspend fun saveCart(
        userId: String,
        items: List<CartItem>,
    ) = dbQuery {
        if (CartsTable.selectAll().where { CartsTable.userId eq userId }.count() == 0L) {
            CartsTable.insert { it[CartsTable.userId] = userId }
        }

        CartItemsTable.deleteWhere { CartItemsTable.userId eq userId }

        items.forEach { item ->
            CartItemsTable.insert {
                it[CartItemsTable.userId] = userId
                it[productId] = item.product.id
                it[quantity] = item.quantity
            }
        }
    }
}
