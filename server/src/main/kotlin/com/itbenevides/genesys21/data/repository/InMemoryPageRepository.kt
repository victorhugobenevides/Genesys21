package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository

class InMemoryPageRepository : PageRepository {
    private val pages = mutableListOf<Page>()
    private val products = mutableListOf<Product>()
    private val categories = mutableListOf<Category>()

    override suspend fun getPages(token: String): List<Page> {
        return pages.filter { it.ownerId == token }
    }

    override suspend fun getPublicPage(id: String): Result<Page> = runCatching {
        pages.find { it.id == id } ?: throw Exception("Page not found")
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> = runCatching {
        pages.find { it.customDomain == domain } ?: throw Exception("Domain not found")
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> = runCatching {
        if (page.ownerId != token && page.ownerId != null) {
            throw SecurityException("Unauthorized")
        }
        val pageWithOwner = if (page.ownerId == null) page.copy(ownerId = token) else page

        if (isEditing) {
            val index = pages.indexOfFirst { it.id == pageWithOwner.id }
            if (index != -1) pages[index] = pageWithOwner else throw Exception("Page to edit not found")
        } else {
            pages.add(pageWithOwner)
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> = runCatching {
        val removed = pages.removeIf { it.id == id && it.ownerId == token }
        if (!removed) throw Exception("Page not found or unauthorized")
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        return Result.failure(UnsupportedOperationException("Not implemented"))
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> {
        return Result.success(products)
    }

    override suspend fun saveProduct(product: Product, token: String): Result<Unit> = runCatching {
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) {
            products[index] = product
        } else {
            products.add(product)
        }
    }

    override suspend fun deleteProduct(id: String, token: String): Result<Unit> = runCatching {
        products.removeIf { it.id == id }
    }

    override suspend fun getCategories(token: String): Result<List<Category>> {
        return Result.success(categories.filter { it.ownerId == token })
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> = runCatching {
        if (category.ownerId != token) throw SecurityException("Unauthorized")
        val categoryId = category.id ?: 0
        if (categoryId > 0) {
            val index = categories.indexOfFirst { it.id == categoryId }
            if (index != -1) categories[index] = category else throw Exception("Category not found")
        } else {
            val maxId = categories.map { it.id ?: 0 }.maxOrNull() ?: 0
            categories.add(category.copy(id = maxId + 1))
        }
    }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> = runCatching {
        val removed = categories.removeIf { it.id == id && it.ownerId == token }
        if (!removed) throw Exception("Category not found or unauthorized")
    }
}
