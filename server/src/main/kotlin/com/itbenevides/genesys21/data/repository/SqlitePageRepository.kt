package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class SqlitePageRepository : PageRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getPages(token: String): List<Page> = dbQuery {
        val pagesQuery = if (token.isBlank()) {
            PagesTable.selectAll()
        } else {
            PagesTable.selectAll().where { PagesTable.ownerId eq token }
        }
        
        pagesQuery.map { row ->
            val pageId = row[PagesTable.id]
            val components = fetchComponentsForPage(pageId)
            row.toPage(components)
        }
    }

    override suspend fun getPublicPage(id: String): Result<Page> = try {
        dbQuery {
            PagesTable.selectAll().where { PagesTable.id eq id }
                .singleOrNull()?.let { row ->
                    val components = fetchComponentsForPage(id)
                    Result.success(row.toPage(components))
                } ?: Result.failure(Exception("Página não encontrada"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> = try {
        dbQuery {
            val searchDomain = domain.lowercase().removePrefix("www.")
            PagesTable.selectAll().where { 
                (PagesTable.customDomain.lowerCase() eq searchDomain) or 
                (PagesTable.customDomain.lowerCase() eq "www.$searchDomain")
            }.firstOrNull()?.let { row ->
                val components = fetchComponentsForPage(row[PagesTable.id])
                Result.success(row.toPage(components))
            } ?: Result.failure(Exception("Domínio $domain não vinculado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> = try {
        dbQuery {
            val formattedDomain = page.customDomain?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
            val formattedWhatsapp = page.whatsapp?.trim()?.takeIf { it.isNotBlank() }

            // 1. Centralização de Domínio e WhatsApp
            if (token.isNotBlank()) {
                PagesTable.update({ PagesTable.ownerId eq token }) {
                    it[customDomain] = formattedDomain
                    it[whatsapp] = formattedWhatsapp
                }
            }

            // 2. Upsert da Página
            val exists = PagesTable.selectAll().where { PagesTable.id eq page.id }.count() > 0
            if (exists) {
                PagesTable.update({ PagesTable.id eq page.id }) {
                    it[title] = page.title
                    it[ownerId] = token
                    it[theme] = page.theme.name
                    it[customDomain] = formattedDomain
                    it[whatsapp] = formattedWhatsapp
                }
            } else {
                PagesTable.insert {
                    it[id] = page.id
                    it[title] = page.title
                    it[ownerId] = token
                    it[theme] = page.theme.name
                    it[customDomain] = formattedDomain
                    it[whatsapp] = formattedWhatsapp
                }
            }

            // 3. Normalização: Salvar Componentes e Produtos
            PageComponentsTable.deleteWhere { pageId eq page.id }
            
            page.components.forEachIndexed { index, component ->
                val componentId = PageComponentsTable.insertAndGetId {
                    it[pageId] = page.id
                    it[type] = component::class.simpleName ?: "Unknown"
                    it[customLabel] = component.customLabel
                    it[isFilterable] = component.isFilterable
                    it[order] = index
                    it[content] = json.encodeToString(component)
                }

                if (component is PageComponent.ProductList) {
                    saveProductsForComponent(componentId.value, component.products, token)
                }
            }
            
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> = try {
        dbQuery {
            val deleted = PagesTable.deleteWhere { (PagesTable.id eq id) and (ownerId eq token) }
            if (deleted > 0) Result.success(Unit) else Result.failure(Exception("Falha ao excluir"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        return Result.failure(Exception("Use /upload"))
    }

    private fun saveProductsForComponent(componentId: Int, products: List<Product>, ownerId: String) {
        products.forEachIndexed { index, product ->
            // 1. Upsert do Produto
            val productExists = ProductsTable.selectAll().where { ProductsTable.id eq product.id }.count() > 0
            if (!productExists) {
                ProductsTable.insert {
                    it[id] = product.id
                    it[this.ownerId] = ownerId
                    it[name] = product.name
                    it[price] = product.price
                    it[description] = product.description
                    it[category] = product.category
                    it[stock] = product.stock
                }
            } else {
                ProductsTable.update({ ProductsTable.id eq product.id }) {
                    it[name] = product.name
                    it[price] = product.price
                    it[description] = product.description
                    it[category] = product.category
                    it[stock] = product.stock
                }
            }

            // 2. Normalização de Imagens do Produto
            ProductImagesTable.deleteWhere { productId eq product.id }
            product.imageUrls.forEachIndexed { imgIndex, url ->
                ProductImagesTable.insert {
                    it[productId] = product.id
                    it[imageUrl] = url
                    it[order] = imgIndex
                }
            }

            // 3. Vincular ao Componente
            ComponentProductsTable.insert {
                it[this.componentId] = componentId
                it[productId] = product.id
                it[order] = index
            }
        }
    }

    private fun fetchComponentsForPage(pageId: String): List<PageComponent> {
        return PageComponentsTable.selectAll()
            .where { PageComponentsTable.pageId eq pageId }
            .orderBy(PageComponentsTable.order to SortOrder.ASC)
            .map { row ->
                val componentId = row[PageComponentsTable.id].value
                val content = row[PageComponentsTable.content] ?: "{}"
                val component = json.decodeFromString<PageComponent>(content)
                
                if (component is PageComponent.ProductList) {
                    val products = fetchProductsForComponent(componentId)
                    component.copy(products = products)
                } else {
                    component
                }
            }
    }

    private fun fetchProductsForComponent(componentId: Int): List<Product> {
        return (ComponentProductsTable innerJoin ProductsTable)
            .selectAll().where { ComponentProductsTable.componentId eq componentId }
            .orderBy(ComponentProductsTable.order to SortOrder.ASC)
            .map { row ->
                val productId = row[ProductsTable.id]
                val images = ProductImagesTable.selectAll()
                    .where { ProductImagesTable.productId eq productId }
                    .orderBy(ProductImagesTable.order to SortOrder.ASC)
                    .map { it[ProductImagesTable.imageUrl] }

                Product(
                    id = productId,
                    name = row[ProductsTable.name],
                    price = row[ProductsTable.price],
                    imageUrls = images,
                    description = row[ProductsTable.description] ?: "",
                    category = row[ProductsTable.category] ?: "",
                    stock = row[ProductsTable.stock]
                )
            }
    }

    private fun ResultRow.toPage(components: List<PageComponent>) = Page(
        id = this[PagesTable.id],
        title = this[PagesTable.title],
        ownerId = this[PagesTable.ownerId],
        customDomain = this[PagesTable.customDomain],
        whatsapp = this[PagesTable.whatsapp],
        components = components,
        theme = try { PageThemeConfig.valueOf(this[PagesTable.theme]) } catch (e: Exception) { PageThemeConfig.DEFAULT }
    )
}
