package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.CategoriesTable
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.data.database.PagesTable
import com.itbenevides.genesys21.data.database.ProductsTable
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqlitePageRepository(private val json: Json) : PageRepository {

    override suspend fun getPages(token: String): List<Page> = dbQuery {
        PagesTable.selectAll().where { PagesTable.ownerId eq token }
            .map { it.toPage() }
    }

    override suspend fun getPublicPage(id: String): Result<Page> = runCatching {
        dbQuery {
            PagesTable.selectAll().where { PagesTable.id eq id }
                .map { it.toPage() }
                .singleOrNull() ?: throw Exception("Página não encontrada")
        }
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> = runCatching {
        dbQuery {
            PagesTable.selectAll().where { PagesTable.customDomain eq domain }
                .map { it.toPage() }
                .firstOrNull() ?: throw Exception("Domínio não encontrado")
        }
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> = runCatching {
        dbQuery {
            if (isEditing) {
                PagesTable.update({ (PagesTable.id eq page.id) and (PagesTable.ownerId eq token) }) {
                    it[title] = page.title
                    it[customDomain] = page.customDomain
                    it[whatsapp] = page.whatsapp
                    it[theme] = page.theme.name
                    it[componentsJson] = json.encodeToString(page.components)
                }
            } else {
                PagesTable.insert {
                    it[id] = page.id
                    it[title] = page.title
                    it[ownerId] = token
                    it[customDomain] = page.customDomain
                    it[whatsapp] = page.whatsapp
                    it[theme] = page.theme.name
                    it[componentsJson] = json.encodeToString(page.components)
                }
            }
            Unit
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> = runCatching {
        dbQuery {
            PagesTable.deleteWhere { (this.id eq id) and (ownerId eq token) }
            Unit
        }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Upload de imagem é tratado diretamente no Application.kt"))
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> = runCatching {
        dbQuery {
            ProductsTable.selectAll().where { ProductsTable.ownerId eq token }.map { it.toProduct() }
        }
    }

    override suspend fun saveProduct(product: Product, token: String): Result<Unit> = runCatching {
        dbQuery {
            val exists = ProductsTable.selectAll().where { (ProductsTable.id eq product.id) and (ProductsTable.ownerId eq token) }.count() > 0
            
            val targetCategoryId = if ((product.categoryId ?: 0) > 0) product.categoryId else null

            if (exists) {
                ProductsTable.update({ (ProductsTable.id eq product.id) and (ProductsTable.ownerId eq token) }) {
                    it[name] = product.name
                    it[price] = product.price
                    it[description] = product.description
                    it[stock] = product.stock
                    it[categoryId] = targetCategoryId
                    it[detailsJson] = json.encodeToString(product)
                }
            } else {
                ProductsTable.insert {
                    it[id] = product.id
                    it[ownerId] = token
                    it[name] = product.name
                    it[price] = product.price
                    it[description] = product.description
                    it[stock] = product.stock
                    it[categoryId] = targetCategoryId
                    it[detailsJson] = json.encodeToString(product)
                }
            }
            Unit
        }
    }

    override suspend fun deleteProduct(id: String, token: String): Result<Unit> = runCatching {
        dbQuery {
            ProductsTable.deleteWhere { (this.id eq id) and (ownerId eq token) }
            Unit
        }
    }

    override suspend fun getCategories(token: String): Result<List<Category>> = runCatching {
        dbQuery {
            CategoriesTable.selectAll().where { CategoriesTable.ownerId eq token }.map { it.toCategory() }
        }
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> = runCatching {
        dbQuery {
            if ((category.id ?: 0) > 0) {
                CategoriesTable.update({ (CategoriesTable.id eq category.id!!) and (CategoriesTable.ownerId eq token) }) {
                    it[name] = category.name
                }
            } else {
                CategoriesTable.insert {
                    it[name] = category.name
                    it[ownerId] = token
                }
            }
            Unit
        }
    }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> = runCatching {
        dbQuery {
            CategoriesTable.deleteWhere { (this.id eq id) and (ownerId eq token) }
            Unit
        }
    }

    private fun ResultRow.toPage(): Page {
        val componentsRaw = this[PagesTable.componentsJson]
        val components = try { json.decodeFromString<List<PageComponent>>(componentsRaw) } catch (e: Exception) { emptyList() }
        
        return Page(
            id = this[PagesTable.id],
            title = this[PagesTable.title],
            ownerId = this[PagesTable.ownerId],
            customDomain = this[PagesTable.customDomain],
            whatsapp = this[PagesTable.whatsapp],
            theme = try { PageThemeConfig.valueOf(this[PagesTable.theme]) } catch(e: Exception) { PageThemeConfig.ROYAL },
            components = components
        )
    }

    private fun ResultRow.toProduct(): Product {
        val detailsRaw = this[ProductsTable.detailsJson]
        return try { json.decodeFromString<Product>(detailsRaw) } catch (e: Exception) { 
            Product(
                id = this[ProductsTable.id],
                name = this[ProductsTable.name],
                price = this[ProductsTable.price],
                description = this[ProductsTable.description] ?: "",
                stock = this[ProductsTable.stock],
                categoryId = this[ProductsTable.categoryId]?.value // Corrigido: usando .value para EntityID
            )
        }
    }

    private fun ResultRow.toCategory(): Category {
        return Category(
            id = this[CategoriesTable.id].value,
            name = this[CategoriesTable.name],
            ownerId = this[CategoriesTable.ownerId]
        )
    }
}
