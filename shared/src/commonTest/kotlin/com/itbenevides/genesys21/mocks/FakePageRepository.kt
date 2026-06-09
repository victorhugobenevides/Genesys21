package com.itbenevides.genesys21.mocks

import com.itbenevides.genesys21.domain.model.Category
import com.itbenevides.genesys21.domain.model.Page
import com.itbenevides.genesys21.domain.model.Product
import com.itbenevides.genesys21.domain.repository.PageRepository

class FakePageRepository : PageRepository {
    private val pages = mutableListOf<Page>()
    private val categories = mutableListOf<Category>()
    private val products = mutableListOf<Product>()
    var shouldReturnError = false

    override suspend fun getPages(token: String): List<Page> {
        if (shouldReturnError) throw Exception("Erro de teste")
        return pages
    }

    override suspend fun getPublicPage(id: String): Result<Page> {
        if (shouldReturnError) return Result.failure(Exception("Erro"))
        val page = pages.find { it.id == id }
        return if (page != null) Result.success(page) else Result.failure(Exception("Não encontrado"))
    }

    override suspend fun getPageByDomain(domain: String): Result<Page> {
        if (shouldReturnError) return Result.failure(Exception("Erro"))
        val page = pages.find { it.customDomain == domain }
        return if (page != null) Result.success(page) else Result.failure(Exception("Não encontrado"))
    }

    override suspend fun savePage(page: Page, token: String, isEditing: Boolean): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Erro ao salvar"))
        if (isEditing) {
            val index = pages.indexOfFirst { it.id == page.id }
            if (index != -1) pages[index] = page else return Result.failure(Exception("Não encontrado"))
        } else {
            pages.add(page)
        }
        return Result.success(Unit)
    }

    override suspend fun deletePage(id: String, token: String): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Erro ao deletar"))
        val removed = pages.removeAll { it.id == id }
        return if (removed) Result.success(Unit) else Result.failure(Exception("Não encontrado"))
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String, token: String): Result<String> {
        if (shouldReturnError) return Result.failure(Exception("Erro upload"))
        return Result.success("http://mock/$fileName")
    }

    override suspend fun getAllProducts(token: String): Result<List<Product>> {
        if (shouldReturnError) return Result.failure(Exception("Erro produtos"))
        return Result.success(products)
    }

    override suspend fun getCategories(token: String): Result<List<Category>> {
        if (shouldReturnError) return Result.failure(Exception("Erro categorias"))
        return Result.success(categories)
    }

    override suspend fun saveCategory(category: Category, token: String): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Erro salvar categoria"))
        categories.add(category)
        return Result.success(Unit)
    }

    override suspend fun deleteCategory(id: Int, token: String): Result<Unit> {
        if (shouldReturnError) return Result.failure(Exception("Erro deletar categoria"))
        categories.removeAll { it.id == id }
        return Result.success(Unit)
    }
}
