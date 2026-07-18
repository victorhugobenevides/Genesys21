package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryPageRepository : PageRepository {
    private val pagesDB = ConcurrentHashMap<String, Page>()
    private val categoriesDB = ConcurrentHashMap<String, Category>()

    override suspend fun getPages(token: String): List<Page> {
        // Mock simplification: in memory doesn't join with stores
        return pagesDB.values.filter { it.deletedAt == null }
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        return pagesDB[id]?.let { if (it.deletedAt == null) Result.success(it) else Result.failure(Exception("Deleted")) } ?: Result.failure(Exception("Page not found"))
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> {
        return pagesDB.values.find { it.customDomain == domain && it.deletedAt == null }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Domain not linked"))
    }

    override suspend fun savePage(
        page: Page,
        token: String,
        isEditing: Boolean,
    ): Result<Unit> {
        pagesDB[page.id] = page
        return Result.success(Unit)
    }

    override suspend fun deletePage(
        id: String,
        token: String,
    ): Result<Unit> {
        val page = pagesDB[id] ?: return Result.failure(Exception("Not found"))
        pagesDB[id] = page.copy(deletedAt = System.currentTimeMillis())
        return Result.success(Unit)
    }

    override suspend fun uploadImage(
        bytes: ByteArray,
        fileName: String,
        token: String,
    ): Result<String> {
        return Result.failure(Exception("Use /upload endpoint"))
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> {
        val products =
            pagesDB.values
                .filter { it.deletedAt == null }
                .flatMap { page ->
                    page.components.filterIsInstance<PageComponent.ProductList>().flatMap { it.products }
                }
                .distinctBy { it.id }

        return Result.success(products)
    }

    override suspend fun getCategories(token: String): Result<List<Category>> {
        return Result.success(categoriesDB.values.filter { it.deletedAt == null }.toList())
    }

    override suspend fun saveCategory(
        category: Category,
        token: String,
    ): Result<Unit> {
        val catId = category.id.ifBlank { java.util.UUID.randomUUID().toString() }
        categoriesDB[catId] = category.copy(id = catId)
        return Result.success(Unit)
    }

    override suspend fun deleteCategory(
        id: String,
        token: String,
    ): Result<Unit> {
        val cat = categoriesDB[id] ?: return Result.failure(Exception("Not found"))
        categoriesDB[id] = cat.copy(deletedAt = System.currentTimeMillis())
        return Result.success(Unit)
    }

    override suspend fun getAllPublicPageIds(): Result<List<String>> {
        return Result.success(pagesDB.keys().toList())
    }
}
