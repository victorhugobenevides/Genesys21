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

    // Configuração de JSON resiliente e explícita para o servidor
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

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
    } catch (e: Exception) { Result.failure(e) }

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
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> = try {
        dbQuery {
            val formattedDomain = page.customDomain?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
            val formattedWhatsapp = page.whatsapp?.trim()?.takeIf { it.isNotBlank() }

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

            // Limpa componentes antigos e salva a nova estrutura consolidada
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
        println("LOG_SERVER_ERROR: Falha ao salvar página: ${e.message}")
        Result.failure(e) 
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> = try {
        dbQuery {
            PagesTable.deleteWhere { (PagesTable.id eq id) and (ownerId eq token) }
            Result.success(Unit)
        }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> = Result.failure(Exception("Use /upload"))

    override suspend fun getAllProducts(token: String): Result<List<Product>> = dbQuery {
        val products = (ProductsTable leftJoin CategoriesTable).selectAll().where { ProductsTable.ownerId eq token }.map { fetchProductFromRow(it) }
        Result.success(products)
    }

    override suspend fun getCategories(token: String): Result<List<Category>> = dbQuery {
        val categories = CategoriesTable.selectAll().where { CategoriesTable.ownerId eq token }.map { row ->
            Category(id = row[CategoriesTable.id].value, ownerId = row[CategoriesTable.ownerId], name = row[CategoriesTable.name], icon = row[CategoriesTable.icon], color = row[CategoriesTable.color])
        }
        Result.success(categories)
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> = try {
        dbQuery {
            if (category.id != null) {
                CategoriesTable.update({ (CategoriesTable.id eq category.id) and (CategoriesTable.ownerId eq token) }) {
                    it[name] = category.name; it[icon] = category.icon; it[color] = category.color
                }
            } else {
                CategoriesTable.insert { it[ownerId] = token; it[name] = category.name; it[icon] = category.icon; it[color] = category.color }
            }
            Result.success(Unit)
        }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> = try {
        dbQuery {
            CategoriesTable.deleteWhere { (CategoriesTable.id eq id) and (ownerId eq token) }
            Result.success(Unit)
        }
    } catch (e: Exception) { Result.failure(e) }

    private fun saveProductsForComponent(componentId: Int, products: List<Product>, ownerId: String) {
        products.forEachIndexed { index, product ->
            var effectiveCategoryId = product.categoryId
            if (effectiveCategoryId == null && !product.categoryName.isNullOrBlank()) {
                val existingCat = CategoriesTable.selectAll().where { (CategoriesTable.ownerId eq ownerId) and (CategoriesTable.name eq product.categoryName!!) }.firstOrNull()
                effectiveCategoryId = if (existingCat != null) existingCat[CategoriesTable.id].value
                else CategoriesTable.insertAndGetId { it[CategoriesTable.ownerId] = ownerId; it[CategoriesTable.name] = product.categoryName!! }.value
            }

            val productExists = ProductsTable.selectAll().where { ProductsTable.id eq product.id }.count() > 0
            if (!productExists) {
                ProductsTable.insert { it[id] = product.id; it[this.ownerId] = ownerId; it[name] = product.name; it[price] = product.price; it[description] = product.description; it[categoryId] = effectiveCategoryId; it[stock] = product.stock }
            } else {
                ProductsTable.update({ ProductsTable.id eq product.id }) { it[name] = product.name; it[price] = product.price; it[description] = product.description; it[categoryId] = effectiveCategoryId; it[stock] = product.stock }
            }

            ProductImagesTable.deleteWhere { productId eq product.id }
            product.imageUrls.forEachIndexed { imgIndex, url -> ProductImagesTable.insert { it[productId] = product.id; it[imageUrl] = url; it[order] = imgIndex } }
            ComponentProductsTable.insert { it[this.componentId] = componentId; it[productId] = product.id; it[order] = index }
        }
    }

    private fun fetchComponentsForPage(pageId: String): List<PageComponent> {
        return PageComponentsTable.selectAll()
            .where { PageComponentsTable.pageId eq pageId }
            .orderBy(PageComponentsTable.order to SortOrder.ASC)
            .mapNotNull { row ->
                val content = row[PageComponentsTable.content] ?: return@mapNotNull null
                try {
                    // Tenta desserializar graciosamente para lidar com migrações
                    val component = json.decodeFromString<PageComponent>(content)
                    if (component is PageComponent.ProductList) {
                        val products = fetchProductsForComponent(row[PageComponentsTable.id].value)
                        component.copy(products = products)
                    } else component
                } catch (e: Exception) {
                    println("LOG_SERVER: Falha ao ler bloco antigo (${row[PageComponentsTable.id].value}). Ignorando para evitar Erro 500.")
                    null
                }
            }
    }

    private fun fetchProductsForComponent(componentId: Int): List<Product> = (ComponentProductsTable innerJoin ProductsTable leftJoin CategoriesTable).selectAll().where { ComponentProductsTable.componentId eq componentId }.orderBy(ComponentProductsTable.order to SortOrder.ASC).map { fetchProductFromRow(it) }

    private fun fetchProductFromRow(row: ResultRow): Product {
        val productId = row[ProductsTable.id]
        val images = ProductImagesTable.selectAll().where { ProductImagesTable.productId eq productId }.orderBy(ProductImagesTable.order to SortOrder.ASC).map { it[ProductImagesTable.imageUrl] }
        return Product(id = productId, name = row[ProductsTable.name], price = row[ProductsTable.price], imageUrls = images, description = row[ProductsTable.description] ?: "", categoryId = row[ProductsTable.categoryId]?.value, categoryName = row.getOrNull(CategoriesTable.name), stock = row[ProductsTable.stock])
    }

    private fun ResultRow.toPage(components: List<PageComponent>) = Page(id = this[PagesTable.id], title = this[PagesTable.title], ownerId = this[PagesTable.ownerId], customDomain = this[PagesTable.customDomain], whatsapp = this[PagesTable.whatsapp], components = components, theme = try { PageThemeConfig.valueOf(this[PagesTable.theme]) } catch (e: Exception) { PageThemeConfig.DEFAULT })
}
