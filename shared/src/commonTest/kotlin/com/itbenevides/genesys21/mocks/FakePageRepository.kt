package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.model.*
import com.itbenevides.genesys21.domain.repository.PageRepository

class FakePageRepository : PageRepository {
    private val pages = mutableListOf<Page>()
    private val products = mutableListOf<Product>()
    private val categories = mutableListOf<Category>()

    override suspend fun getPages(token: String): List<Page> {
        return pages.filter { it.ownerId == token }
    }

    override suspend fun getPublicPage(id: String): Result<Page> = runCatching {
        pages.find { it.id == id } ?: throw Exception("Not found")
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> = runCatching {
        pages.find { it.customDomain == domain } ?: throw Exception("Not found")
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> = runCatching {
        if (isEditing) {
            val index = pages.indexOfFirst { it.id == page.id }
            if (index != -1) pages[index] = page else pages.add(page)
        } else {
            pages.add(page)
        }
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> = runCatching {
        pages.removeIf { it.id == id }
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        return Result.success("/fake/url/$fileName")
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> {
        return Result.success(products)
    }

    override suspend fun saveProduct(product: Product, token: String): Result<Unit> = runCatching {
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) products[index] = product else products.add(product)
    }

    override suspend fun deleteProduct(id: String, token: String): Result<Unit> = runCatching {
        products.removeIf { it.id == id }
    }

    override suspend fun getCategories(token: String): Result<List<Category>> {
        return Result.success(categories)
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> = runCatching {
        categories.add(category)
    }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> = runCatching {
        categories.removeIf { it.id == id }
    }
    
    // Helper para os testes
    fun clear() {
        pages.clear()
        products.clear()
        categories.clear()
    }
}
