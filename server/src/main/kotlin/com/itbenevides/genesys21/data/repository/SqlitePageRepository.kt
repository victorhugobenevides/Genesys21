package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.data.database.*
import com.itbenevides.genesys21.data.database.DatabaseFactory.dbQuery
import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SqlitePageRepository : PageRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getPages(token: String): List<Page> =
        dbQuery {
            val pagesQuery =
                if (token.isBlank()) {
                    PagesTable.selectAll().where { PagesTable.deletedAt.isNull() }
                } else {
                    (PagesTable innerJoin StoresTable)
                        .selectAll().where { (StoresTable.ownerId eq token) and (PagesTable.deletedAt.isNull()) }
                }

            pagesQuery.map { row ->
                val pageId = row[PagesTable.id]
                val components = fetchComponentsForPage(pageId)
                row.toPage(components)
            }
        }

    override suspend fun getPublicPage(id: String): Result<Page> =
        try {
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

    override suspend fun getPageByDomain(domain: String): Result<Page> =
        try {
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

    override suspend fun savePage(
        page: Page,
        token: String,
        isEditing: Boolean,
    ): Result<Unit> =
        try {
            dbQuery {
                // Validação de Posse (Multi-tenancy)
                val isOwner = StoresTable.selectAll()
                    .where { (StoresTable.id eq page.storeId) and (StoresTable.ownerId eq token) }
                    .count() > 0

                if (!isOwner) throw Exception("Acesso negado: Você não é o dono desta loja.")

                val formattedDomain = page.customDomain?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
                val formattedWhatsapp = page.whatsapp?.trim()?.takeIf { it.isNotBlank() }

                // Verifica se o domínio já está em uso por OUTRA página
                if (formattedDomain != null) {
                    val ownerOfDomain =
                        PagesTable.selectAll()
                            .where { PagesTable.customDomain eq formattedDomain }
                            .firstOrNull()?.get(PagesTable.id)

                    if (ownerOfDomain != null && ownerOfDomain != page.id) {
                        throw Exception("unique_domain_error: Este domínio já está vinculado a outra vitrine.")
                    }
                }

                val exists = PagesTable.selectAll().where { PagesTable.id eq page.id }.count() > 0
                if (exists) {
                    PagesTable.update({ PagesTable.id eq page.id }) {
                        it[title] = page.title
                        it[storeId] = page.storeId
                        it[theme] = page.theme.name
                        it[customDomain] = formattedDomain
                        it[whatsapp] = formattedWhatsapp
                        it[updatedAt] = System.currentTimeMillis()
                    }
                } else {
                    PagesTable.insert {
                        it[id] = page.id
                        it[title] = page.title
                        it[storeId] = page.storeId
                        it[theme] = page.theme.name
                        it[customDomain] = formattedDomain
                        it[whatsapp] = formattedWhatsapp
                    }
                }

                // CORREÇÃO: Sincroniza apenas o WhatsApp globalmente para a LOJA.
                if (isEditing && page.storeId.isNotBlank() && formattedWhatsapp != null) {
                    StoresTable.update({ StoresTable.id eq page.storeId }) {
                        it[whatsapp] = formattedWhatsapp
                        it[updatedAt] = System.currentTimeMillis()
                    }
                }

                PageComponentsTable.deleteWhere { pageId eq page.id }

                page.components.forEachIndexed { index, component ->
                    val componentId =
                        PageComponentsTable.insertAndGetId {
                            it[pageId] = page.id
                            it[type] = component::class.simpleName ?: "Unknown"
                            it[customLabel] = component.customLabel
                            it[isFilterable] = component.isFilterable
                            it[order] = index
                            it[content] = json.encodeToString(component)
                        }

                    val finalComponentId = componentId
                    if (component is PageComponent.ProductList) {
                        saveProductsForComponent(finalComponentId.value, component.products, page.storeId)
                    }
                }

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deletePage(
        id: String,
        token: String,
    ): Result<Unit> =
        try {
            dbQuery {
                val updated = PagesTable.update({ (PagesTable.id eq id) and (StoresTable.ownerId eq token) }) {
                    it[deletedAt] = System.currentTimeMillis()
                }
                if (updated > 0) Result.success(Unit) else Result.failure(Exception("Falha ao excluir"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        token: String,
    ): Result<String> {
        return Result.failure(Exception("Use /upload"))
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> =
        try {
            dbQuery {
                val products =
                    (ProductsTable leftJoin CategoriesTable)
                        .selectAll()
                        .where { (StoresTable.ownerId eq token) and (ProductsTable.deletedAt.isNull()) }
                        .map { row ->
                            fetchProductFromRow(row)
                        }
                Result.success(products)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getCategories(token: String): Result<List<Category>> =
        try {
            dbQuery {
                val categories =
                    (CategoriesTable innerJoin StoresTable)
                        .selectAll()
                        .where { (StoresTable.ownerId eq token) and (CategoriesTable.deletedAt.isNull()) }
                        .map { row ->
                            Category(
                                id = row[CategoriesTable.id],
                                storeId = row[CategoriesTable.storeId],
                                name = row[CategoriesTable.name],
                                parentId = row[CategoriesTable.parentId],
                                icon = row[CategoriesTable.icon],
                                color = row[CategoriesTable.color],
                                createdAt = row[CategoriesTable.createdAt],
                                updatedAt = row[CategoriesTable.updatedAt],
                                deletedAt = row[CategoriesTable.deletedAt]
                            )
                        }
                Result.success(categories)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun saveCategory(
        category: Category,
        token: String,
    ): Result<Unit> =
        try {
            dbQuery {
                // Valida se o usuário é o dono da Store antes de salvar a categoria
                val isOwner = StoresTable.selectAll()
                    .where { (StoresTable.id eq category.storeId) and (StoresTable.ownerId eq token) }
                    .count() > 0

                if (!isOwner) throw Exception("Acesso negado: Você não é o dono desta loja.")

                val exists = CategoriesTable.selectAll().where { CategoriesTable.id eq category.id }.count() > 0
                if (exists) {
                    CategoriesTable.update({ CategoriesTable.id eq category.id }) {
                        it[name] = category.name
                        it[parentId] = category.parentId
                        it[icon] = category.icon
                        it[color] = category.color
                        it[updatedAt] = System.currentTimeMillis()
                    }
                } else {
                    CategoriesTable.insert {
                        it[id] = category.id.ifBlank { java.util.UUID.randomUUID().toString() }
                        it[storeId] = category.storeId
                        it[name] = category.name
                        it[parentId] = category.parentId
                        it[icon] = category.icon
                        it[color] = category.color
                    }
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteCategory(
        id: String,
        token: String,
    ): Result<Unit> =
        try {
            dbQuery {
                // Busca a storeId da categoria para validar posse
                val storeId = CategoriesTable.select(CategoriesTable.storeId)
                    .where { CategoriesTable.id eq id }
                    .firstOrNull()?.get(CategoriesTable.storeId)

                if (storeId == null) throw Exception("Categoria não encontrada.")

                val isOwner = StoresTable.selectAll()
                    .where { (StoresTable.id eq storeId) and (StoresTable.ownerId eq token) }
                    .count() > 0

                if (!isOwner) throw Exception("Acesso negado: Você não tem permissão para excluir esta categoria.")

                val updated = CategoriesTable.update({ CategoriesTable.id eq id }) {
                    it[deletedAt] = System.currentTimeMillis()
                }
                if (updated > 0) Result.success(Unit) else Result.failure(Exception("Falha ao excluir categoria"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getAllPublicPageIds(): Result<List<String>> =
        try {
            dbQuery {
                val ids = PagesTable.select(PagesTable.id).map { it[PagesTable.id] }
                Result.success(ids)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun saveProductsForComponent(
        componentId: Int,
        products: List<Product>,
        storeId: String,
    ) {
        products.forEachIndexed { index, product ->
            var effectiveCategoryId = product.categoryId
            if (effectiveCategoryId == null && !product.categoryName.isNullOrBlank()) {
                val existingCat =
                    CategoriesTable.selectAll()
                        .where { (CategoriesTable.storeId eq storeId) and (CategoriesTable.name eq product.categoryName!!) }
                        .firstOrNull()

                effectiveCategoryId =
                    if (existingCat != null) {
                        existingCat[CategoriesTable.id]
                    } else {
                        val newCatId = java.util.UUID.randomUUID().toString()
                        CategoriesTable.insert {
                            it[id] = newCatId
                            it[this.storeId] = storeId
                            it[this.name] = product.categoryName!!
                        }
                        newCatId
                    }
            }

            val productExists = ProductsTable.selectAll().where { ProductsTable.id eq product.id }.count() > 0
            if (!productExists) {
                ProductsTable.insert {
                    it[id] = product.id.ifBlank { java.util.UUID.randomUUID().toString() }
                    it[this.storeId] = storeId
                    it[name] = product.name
                    it[price] = product.price
                    it[description] = product.description
                    it[categoryId] = effectiveCategoryId
                    it[stock] = product.stock
                }
            } else {
                ProductsTable.update({ ProductsTable.id eq product.id }) {
                    it[name] = product.name
                    it[price] = product.price
                    it[description] = product.description
                    it[categoryId] = effectiveCategoryId
                    it[stock] = product.stock
                    it[updatedAt] = System.currentTimeMillis()
                }
            }

            ProductImagesTable.deleteWhere { productId eq product.id }
            product.imageUrls.forEachIndexed { imgIndex, url ->
                ProductImagesTable.insert {
                    it[id] = java.util.UUID.randomUUID().toString()
                    it[productId] = product.id
                    it[imageUrl] = url
                    it[order] = imgIndex
                }
            }

            ComponentProductsTable.insert {
                it[ComponentProductsTable.componentId] = componentId
                it[ComponentProductsTable.productId] = product.id
                it[ComponentProductsTable.order] = index
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
            .join(CategoriesTable, JoinType.LEFT, ProductsTable.categoryId, CategoriesTable.id)
            .selectAll().where { ComponentProductsTable.componentId eq componentId }
            .orderBy(ComponentProductsTable.order to SortOrder.ASC)
            .map { row ->
                fetchProductFromRow(row)
            }
    }

    private fun fetchProductFromRow(row: ResultRow): Product {
        val productId = row[ProductsTable.id]
        val images =
            ProductImagesTable.selectAll()
                .where { ProductImagesTable.productId eq productId }
                .orderBy(ProductImagesTable.updatedAt to SortOrder.ASC) // Use updatedAt for order if image order is not enough
                .map { it[ProductImagesTable.imageUrl] }

        return Product(
            id = productId,
            storeId = row[ProductsTable.storeId],
            name = row[ProductsTable.name],
            price = row[ProductsTable.price],
            imageUrls = images,
            description = row[ProductsTable.description] ?: "",
            categoryId = row[ProductsTable.categoryId],
            categoryName = row.getOrNull(CategoriesTable.name),
            stock = row[ProductsTable.stock],
            createdAt = row[ProductsTable.createdAt],
            updatedAt = row[ProductsTable.updatedAt],
            deletedAt = row[ProductsTable.deletedAt]
        )
    }

    private fun ResultRow.toPage(components: List<PageComponent>) =
        Page(
            id = this[PagesTable.id],
            storeId = this[PagesTable.storeId],
            title = this[PagesTable.title],
            customDomain = this[PagesTable.customDomain],
            whatsapp = this[PagesTable.whatsapp],
            components = components,
            theme =
                try {
                    PageThemeConfig.valueOf(this[PagesTable.theme])
                } catch (e: Exception) {
                    PageThemeConfig.DEFAULT
                },
            createdAt = this[PagesTable.createdAt],
            updatedAt = this[PagesTable.updatedAt],
            deletedAt = this[PagesTable.deletedAt]
        )
}
